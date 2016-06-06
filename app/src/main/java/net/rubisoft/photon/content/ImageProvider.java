package net.rubisoft.photon.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ImageProvider extends ContentProvider {
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
    public String getType(@NonNull Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
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
            case Categories: {
                long _id = db.insert(ImageContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ImageContract.CategoryEntry.buildCategoryUri(_id);
                break;
            }
            case CategoriesForImage : {
                long _id = db.insert(ImageContract.CategorizedImageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ImageContract.CategoryEntry.buildImageWithCategoriesUri(_id);
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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;
        if (match == Categories) {
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
        }
        if (returnCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null)
            selection = "1";

        switch (match) {
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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
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
                ImageContract.CategoryEntry.TABLE_NAME + " ON " +
                ImageContract.CategorizedImageEntry.TABLE_NAME + "." +
                ImageContract.CategorizedImageEntry.CATEGORY_ID + " = " +
                ImageContract.CategoryEntry.TABLE_NAME + "." + ImageContract.CategoryEntry._ID);

        return builder.query(mOpenHelper.getReadableDatabase(), projection,
                ImageContract.CategorizedImageEntry.IMAGE_ID + "=?",
                new String[] { uri.getPathSegments().get(1) }, null, null, sortOrder);
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_IMAGE + "/#/categories", CategoriesForImage);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY, Categories);
        matcher.addURI(ImageContract.CONTENT_AUTHORITY, ImageContract.PATH_CATEGORY + "/#", Category);
        return matcher;
    }
}
