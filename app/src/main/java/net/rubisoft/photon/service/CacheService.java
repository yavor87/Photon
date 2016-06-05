package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImagesCache;
import net.rubisoft.photon.content.ImagesCacheDBHelper;
import net.rubisoft.photon.data.ImageProvider;
import net.rubisoft.photon.data.LocalImageProvider;

import java.util.List;

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
            ImageProvider imageProvider = new LocalImageProvider(this);
            List<Uri> images = imageProvider.getImages();
            ContentValues values = new ContentValues();
            int stored = 0;
            for (Uri imageUri : images) {
                values.put(ImagesCache.Image.IMAGE_URI, imageUri.toString());
                long rowId = db.insert(ImagesCache.Image.TABLE_NAME, null, values);
                if (rowId > 0) {
                    stored++;
                }
                values.clear();
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
            ContentValues values = new ContentValues();
            int stored = 0;
            for (String category : categories) {
                values.put(ImagesCache.Category.NAME, category);
                long rowId = db.insert(ImagesCache.Category.TABLE_NAME, null, values);
                if (rowId > 0) {
                    stored++;
                }
                values.clear();
            }
            Log.v(LOG_TAG, "Stored " + stored + " categories in db");
        } finally {
            db.close();
        }
    }
}
