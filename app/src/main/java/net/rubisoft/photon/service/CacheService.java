package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.content.ImagesCacheDBHelper;

// TODO: Replace
public class CacheService extends IntentService {
    public CacheService() {
        super("CacheService");
    }

    private static String LOG_TAG = CacheService.class.getSimpleName();

    @Override
    protected void onHandleIntent(Intent intent) {
        cacheCategories();
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
}
