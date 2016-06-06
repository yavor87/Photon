package net.rubisoft.photon.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.content.ImagesCacheDBHelper;

public class CacheService extends IntentService {
    public CacheService() {
        super("CacheService");
    }

    public static String MODE_KEY = "mode";
    public static int MODE_IMAGES = 1;
    public static int MODE_CATEGORIES = 2;
    public static int MODE_ALL = 3;
    private static String LOG_TAG = CacheService.class.getSimpleName();

    @Override
    protected void onHandleIntent(Intent intent) {
        final int mode = intent.getIntExtra(MODE_KEY, MODE_ALL);

        if ((mode & MODE_IMAGES) == MODE_IMAGES) {
            cacheImages();
        }
        if ((mode & MODE_CATEGORIES) == MODE_CATEGORIES) {
            cacheCategories();
        }
    }

    private void cacheImages() {
        SQLiteDatabase db = new ImagesCacheDBHelper(this).getWritableDatabase();
        try {
            ContentResolver resolver = getContentResolver();
            ContentValues[] values = getImages();
            if (values == null)
                return;

            int stored = 0;
            for (ContentValues value : values) {
                Uri row = resolver.insert(ImageContract.ImageEntry.CONTENT_URI, value);
                if (row != null) {
                    stored++;
                } else {
                    break;
                }
            }
            Log.v(LOG_TAG, "Stored " + stored + " images in db");
        } finally {
            db.close();
        }
    }

    private void cacheCategories() {
        SQLiteDatabase db = new ImagesCacheDBHelper(this).getWritableDatabase();
        try {
            Categorizer categorizer = ImaggaCategorizer.getInstance();
            String[] categories = categorizer.getCategories();
            if (categories == null)
                return;

            ContentValues[] values = new ContentValues[categories.length];
            for (int i = 0; i < categories.length; i++) {
                ContentValues value = new ContentValues();
                value.put(ImageContract.CategoryEntry.NAME, categories[i]);
                values[i] = value;
            }
            int stored = getContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI, values);
            Log.v(LOG_TAG, "Stored " + stored + " categories in db");
        } finally {
            db.close();
        }
    }

    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    @Nullable
    private ContentValues[] getImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
            return null;

        ContentResolver resolver = getContentResolver();

        final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return null;
        }

        ContentValues[] results = new ContentValues[cursor.getCount()];
        int i = 0;
        try {
            if (cursor.moveToFirst()) {
                final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                do {
                    int imageId = cursor.getInt(idColumn);
                    ContentValues values = new ContentValues();
                    values.put(ImageContract.ImageEntry._ID, imageId);
                    values.put(ImageContract.ImageEntry.IMAGE_URI, "file:" + cursor.getString(dataColumn));

                    // Obtain thumbnail
                    Cursor thumbnail = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                            new String[]{ MediaStore.Images.Thumbnails.DATA },
                            MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                            new String[]{Integer.toString(imageId)}, null);
                    try {
                        if (thumbnail != null && thumbnail.moveToFirst()) {
                            values.put(ImageContract.ImageEntry.THUMBNAIL_URI, "file:" +
                                    thumbnail.getString(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        thumbnail.close();
                    }

                    results[i++] = values;
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return results;
    }
}
