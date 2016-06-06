package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.content.ImagesCacheDBHelper;

import java.util.Iterator;

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
        ImageIterator imageIterator = new ImageIterator(this);
        SQLiteDatabase db = new ImagesCacheDBHelper(this).getWritableDatabase();
        try {
            ContentResolver resolver = getContentResolver();
            int stored = 0;
            while (imageIterator.hasNext()) {
                ContentValues value = imageIterator.next();
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
            imageIterator.close();
        }
    }

    private void cacheCategories() {
        Categorizer categorizer = ImaggaCategorizer.getInstance();
        String[] categories = categorizer.getCategories();
        if (categories == null)
            return;

        SQLiteDatabase db = new ImagesCacheDBHelper(this).getWritableDatabase();
        try {
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

    private class ImageIterator implements Iterator<ContentValues> {
        public ImageIterator(Context context) {
            mResolver = context.getContentResolver();
            mImageCursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        }

        ContentResolver mResolver;
        final Cursor mImageCursor;
        final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, };
        final String[] thumb_projection = { MediaStore.Images.Thumbnails.DATA };
        final int COL_DATA = 0;
        final int COL_ID = 1;
        final int COL_THUMB_DATA = 0;

        public void close() {
            mImageCursor.close();
        }

        @Override
        public boolean hasNext() {
            return mImageCursor.moveToNext();
        }

        @Override
        public ContentValues next() {
            int imageId = mImageCursor.getInt(COL_ID);
            ContentValues values = new ContentValues();
            values.put(ImageContract.ImageEntry._ID, imageId);
            values.put(ImageContract.ImageEntry.IMAGE_URI, "file:" + mImageCursor.getString(COL_DATA));

            // Obtain thumbnail
            Cursor thumbnail = mResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumb_projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                    new String[]{ Integer.toString(imageId) }, null);
            try {
                if (thumbnail != null && thumbnail.moveToFirst()) {
                    values.put(ImageContract.ImageEntry.THUMBNAIL_URI, "file:" +
                            thumbnail.getString(COL_THUMB_DATA));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                thumbnail.close();
            }

            return values;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
