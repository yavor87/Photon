package net.rubisoft.photon.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ImageProvider extends ContentProvider {
    private static final String LOG_TAG = ImageProvider.class.getSimpleName();
    private static final int Images = 100;
    private static final int Image = 101;
    private static final int UncategorizedImages = 111;
    private static final int Categories = 200;
    private static final int Category = 201;
    private static final int CategoriesForImage = 300;

    private static final String UNCATEGORIZED_FILTER = "NOT EXISTS (SELECT " +
            ImageContract.CategorizedImageEntry.IMAGE_ID + " FROM " +
            ImageContract.CategorizedImageEntry.TABLE_NAME + " WHERE " +
            ImageContract.CategorizedImageEntry.TABLE_NAME + "." +
            ImageContract.CategorizedImageEntry.IMAGE_ID + " = " +
            ImageContract.ImageEntry.TABLE_NAME + "." + ImageContract.ImageEntry._ID + ")";

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ImagesCacheDBHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new ImagesCacheDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case Images:
            case UncategorizedImages:
                return ImageContract.ImageEntry.CONTENT_TYPE;
            case Image:
                return ImageContract.ImageEntry.CONTENT_ITEM_TYPE;
            case Categories:
            case CategoriesForImage:
                return ImageContract.CategoryEntry.CONTENT_TYPE;
            case Category:
                return ImageContract.CategoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query " + uri.toString());

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "image/#"
            case Image: {
                selection = ImageContract.ImageEntry._ID + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
            }
            // "image"
            case Images: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ImageContract.ImageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "uncategorized"
            case UncategorizedImages: {
                retCursor =  mOpenHelper.getReadableDatabase().query(ImageContract.ImageEntry.TABLE_NAME,
                        projection,
                        UNCATEGORIZED_FILTER,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "category/#"
            case Category: {
                selection = ImageContract.CategoryEntry._ID + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
            }
            // "category"
            case Categories: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ImageContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "image/#/categories"
            case CategoriesForImage: {
                selection = ImageContract.CategorizedImageEntry.IMAGE_ID + "=?";
                selectionArgs = new String[] { uri.getPathSegments().get(1) };
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ImageContract.ImageEntry.CATEGORIES_VIEW_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.v(LOG_TAG, "insert " + uri.toString());

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case Images: {
                long _id = db.insert(ImageContract.ImageEntry.TABLE_NAME, null, values);
                if (_id > 0 )
                    returnUri = ImageContract.ImageEntry.buildImageUri(_id);
                break;
            }
            case Categories: {
                long _id = db.insert(ImageContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ImageContract.CategoryEntry.buildCategoryUri(_id);
                break;
            }
            case CategoriesForImage : {
                long _id = db.insert(ImageContract.CategorizedImageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(_id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (returnUri != null)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        Log.v(LOG_TAG, "bulkInsert " + uri.toString() + " " + values.length + " values");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;
        if (match == Images) {
            tableName = ImageContract.ImageEntry.TABLE_NAME;
        } else if (match == Categories) {
            tableName = ImageContract.CategoryEntry.TABLE_NAME;
        } else if (match == CategoriesForImage) {
            tableName = ImageContract.CategorizedImageEntry.TABLE_NAME;
        } else {
            return super.bulkInsert(uri, values);
        }

        int returnCount = 0;
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        if (returnCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "delete " + uri.toString());

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null)
            selection = "1";

        switch (match) {
            case Image : {
                rowsDeleted = db.delete(ImageContract.ImageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case Category : {
                rowsDeleted = db.delete(ImageContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "update " + uri.toString());

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case Image : {
                rowsUpdated = db.update(ImageContract.ImageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case Category : {
                rowsUpdated = db.update(ImageContract.CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE, Images);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE + "/#", Image);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE + "/#/categories", CategoriesForImage);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY, Categories);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY + "/#", Category);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_UNCATEGORIZED_IMAGES, UncategorizedImages);
        return matcher;
    }
}
