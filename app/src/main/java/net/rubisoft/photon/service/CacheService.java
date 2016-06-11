package net.rubisoft.photon.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;

public class CacheService extends IntentService {
    public CacheService() {
        super("CacheService");
    }

    public static final String BROADCAST_ACTION = "net.rubisoft.photon.service.CACHING_COMPLETE";
    public static final String MODE_KEY = "mode";
    public static final int MODE_IMAGES = 1;
    public static final int MODE_CATEGORIES = 2;
    public static final int MODE_ALL = 3;
    private static final String LOG_TAG = CacheService.class.getSimpleName();

    @Override
    protected void onHandleIntent(Intent intent) {
        final int mode = intent.getIntExtra(MODE_KEY, MODE_ALL);

        if ((mode & MODE_IMAGES) == MODE_IMAGES) {
            cacheImages();
        }
        if ((mode & MODE_CATEGORIES) == MODE_CATEGORIES) {
            cacheCategories();
        }

        sendBroadcast(new Intent(BROADCAST_ACTION));
    }

    private void cacheImages() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return;

        ContentValues[] values = getImages(this);
        if (values != null) {
            int stored = getContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI, values);
            Log.v(LOG_TAG, "Stored " + stored + " images in db");
        }
    }

    private void cacheCategories() {
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
    }

    private ContentValues[] getImages(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN };
        final String[] thumb_projection = { MediaStore.Images.Thumbnails.DATA };
        final int COL_DATA = 0;
        final int COL_ID = 1;
        final int COL_TAKEN = 2;
        final int COL_THUMB_DATA = 0;

        ContentResolver resolver = context.getContentResolver();
        Cursor imageCursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (imageCursor == null)
            return null;

        try {
            int count = imageCursor.getCount();
            if (count == 0)
                return null;

            ContentValues[] valuesArr = new ContentValues[count];
            int i = 0;
            while (imageCursor.moveToNext()) {
                int imageId = imageCursor.getInt(COL_ID);
                ContentValues values = new ContentValues();
                values.put(ImageContract.ImageEntry._ID, imageId);
                values.put(ImageContract.ImageEntry.DATE_TAKEN, imageCursor.getLong(COL_TAKEN));
                values.put(ImageContract.ImageEntry.IMAGE_URI, "file:" + imageCursor.getString(COL_DATA));

                // Obtain thumbnail
                Cursor thumbnail = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumb_projection,
                        MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                        new String[]{Integer.toString(imageId)}, null);
                try {
                    if (thumbnail != null && thumbnail.moveToFirst()) {
                        values.put(ImageContract.ImageEntry.THUMBNAIL_URI, "file:" +
                                thumbnail.getString(COL_THUMB_DATA));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (thumbnail != null)
                        thumbnail.close();
                }

                valuesArr[i++] = values;
            }

            return valuesArr;
        } finally {
            imageCursor.close();
        }
    }
}
