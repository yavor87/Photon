package net.rubisoft.photon.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class ImageProvider extends ContentProvider {
    private static final int Images = 100;
    private static final int Image = 101;
    private static final int Categories = 200;
    private static final int Category = 201;
    private static final int CategoriesForImage = 300;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ImagesCacheDBHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new ImagesCacheDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case Images:
                return ImageContract.ImageEntry.CONTENT_TYPE;
            case Image:
                return ImageContract.ImageEntry.CONTENT_ITEM_TYPE;
            case Categories:
                return ImageContract.CategoryEntry.CONTENT_TYPE;
            case Category:
                return ImageContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case CategoriesForImage:
                return ImageContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
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
            // "category/image/#"
            case CategoriesForImage: {
                retCursor = getCategoriesForImage(uri, projection, sortOrder);
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
    public Uri insert(Uri uri, ContentValues values) {
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
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (returnUri != null)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;
        if (match == Images) {
            tableName = ImageContract.ImageEntry.TABLE_NAME;
        } else if (match == Categories) {
            tableName = ImageContract.CategoryEntry.TABLE_NAME;
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
        }
        if (returnCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted = -1;

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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated = -1;
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

    private Cursor getCategoriesForImage(Uri uri, String[] projection, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(ImageContract.CategorizedImageEntry.TABLE_NAME + " JOIN " +
                ImageContract.CategoryEntry.TABLE_NAME + " USING (" +
                ImageContract.CategorizedImageEntry.CATEGORY_ID + ")");

        return builder.query(mOpenHelper.getReadableDatabase(), projection,
                ImageContract.CategorizedImageEntry.IMAGE_ID + "=?",
                new String[] { uri.getLastPathSegment() }, null, null, sortOrder);
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE, Images);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE + "/#", Image);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY, Categories);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY + "/#", Category);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY + "/image/#", CategoriesForImage);
        return matcher;
    }
}
