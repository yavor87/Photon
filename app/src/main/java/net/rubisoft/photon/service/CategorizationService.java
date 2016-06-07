package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorizationService extends IntentService {
    public CategorizationService() {
        super("CategorizationService");
        mCategorizer = ImaggaCategorizer.getInstance();
    }

    private static final String LOG_TAG = CacheService.class.getSimpleName();
    private static final String[] PROJECTION =
            new String[] { ImageContract.ImageEntry._ID, ImageContract.ImageEntry.IMAGE_URI };
    private static final int COL_ID = 0;
    private static final int COL_URI = 1;
    private Categorizer mCategorizer;
    private static Map<String, Integer> mCategoryMap;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Started");

        // get image File from intent
        if (mCategoryMap == null) {
            populateCategoryMap();
            if (mCategoryMap == null) {
                Log.e(LOG_TAG, "Can't create category map! Stopping");
                return;
            }
        }

        Cursor cursor = getContentResolver().query(ImageContract.ImageEntry.uncategorizedImagesUri(),
                PROJECTION, null, null, null);

        if (cursor == null)
            return;

        int categorizedCount = 0;
        try {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(COL_ID);
                String imageUri = cursor.getString(COL_URI);
                List<Categorizer.Categorization> categories = mCategorizer.categorizeImage(
                        Uri.parse(imageUri));

                ContentValues[] contentValues = new ContentValues[categories.size()];
                int i = 0;
                for (Categorizer.Categorization categorization : categories) {
                    ContentValues values = new ContentValues();
                    values.put(ImageContract.CategorizedImageEntry.IMAGE_ID, imageId);
                    values.put(ImageContract.CategorizedImageEntry.CATEGORY_ID,
                            mCategoryMap.get(categorization.getCategory()));
                    values.put(ImageContract.CategorizedImageEntry.CONFIDENCE,
                            categorization.getConfidence());
                    contentValues[i++] = values;
                }

                Uri insertUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(imageId);
                getContentResolver().bulkInsert(insertUri, contentValues);

                categorizedCount++;
            }
        } finally {
            cursor.close();
        }

        Log.v(LOG_TAG, "Categorized " + categorizedCount + " images");
    }

    private void populateCategoryMap() {
        Cursor categoriesCursor = getContentResolver().query(
                ImageContract.CategoryEntry.CONTENT_URI, null, null, null, null);
        if (categoriesCursor == null)
            return;

        mCategoryMap = new HashMap<>();
        try {
            while (categoriesCursor.moveToNext()) {
                String category = categoriesCursor.getString(categoriesCursor
                        .getColumnIndex(ImageContract.CategoryEntry.NAME));
                int id = categoriesCursor.getInt(categoriesCursor
                        .getColumnIndex(ImageContract.CategoryEntry._ID));
                mCategoryMap.put(category, id);
            }
        } finally {
            categoriesCursor.close();
        }
    }
}
