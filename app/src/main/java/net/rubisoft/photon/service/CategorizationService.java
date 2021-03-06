package net.rubisoft.photon.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.rubisoft.photon.R;
import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;
import net.rubisoft.photon.content.ImageContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorizationService extends IntentService {
    public CategorizationService() {
        super("CategorizationService");
        mCategorizer = ImaggaCategorizer.getInstance();
    }

    private static final String LOG_TAG = CategorizationService.class.getSimpleName();
    private static final String[] PROJECTION =
            new String[] { ImageContract.ImageEntry._ID, ImageContract.ImageEntry.IMAGE_URI };
    private static final int COL_ID = 0;
    private static final int COL_URI = 1;
    private static Map<String, Integer> mCategoryMap;
    private static final int CATEGORIZATION_NOTIFICATION = 1;

    private Categorizer mCategorizer;
    NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Started");
        categorizeImages();
        Log.v(LOG_TAG, "Finished");
    }

    private void categorizeImages() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // get image File from intent
        if (mCategoryMap == null) {
            populateCategoryMap();
            if (mCategoryMap == null || mCategoryMap.size() == 0) {
                Log.e(LOG_TAG, "Can't create category map! Stopping");
                return;
            }
        }

        Cursor cursor = getContentResolver().query(ImageContract.ImageEntry.uncategorizedImagesUri(),
                PROJECTION, null, null, null);

        if (cursor == null) {
            Log.v(LOG_TAG, "Nothing to categorize");
            return;
        }

        NotificationCompat.Builder builder;
        int categorizedCount = 0;
        try {
            int uncategorizedImages = cursor.getCount();
            if (uncategorizedImages == 0) {
                Log.v(LOG_TAG, "Nothing to categorize");
                return;
            }

            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.camera)
                    .setContentTitle(getString(R.string.categorization_notification_title));

            while (cursor.moveToNext()) {
                // Display notification
                builder.setContentText(getString(R.string.categorization_notification_text,
                        categorizedCount + 1, uncategorizedImages));
                mNotificationManager.notify(CATEGORIZATION_NOTIFICATION, builder.build());

                int imageId = cursor.getInt(COL_ID);
                String imageUri = cursor.getString(COL_URI);
                List<Categorizer.Categorization> categories = mCategorizer.categorizeImage(
                        Uri.parse(imageUri));

                if (categories != null) {
                    List<ContentValues> contentValues = new ArrayList<>(categories.size());
                    for (Categorizer.Categorization categorization : categories) {
                        ContentValues values = new ContentValues();
                        values.put(ImageContract.CategorizedImageEntry.IMAGE_ID, imageId);
                        String category = categorization.getCategory();
                        Integer categoryId = mCategoryMap.get(category);
                        if (categoryId == null) {
                            Log.d(LOG_TAG, "Unrecognized category " + category);
                            continue;
                        }
                        values.put(ImageContract.CategorizedImageEntry.CATEGORY_ID, categoryId);
                        values.put(ImageContract.CategorizedImageEntry.CONFIDENCE,
                                categorization.getConfidence());
                        contentValues.add(values);
                    }

                    Uri insertUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(imageId);
                    if (contentValues.size() > 0) {
                        int insertCount = getContentResolver().bulkInsert(insertUri,
                                contentValues.toArray(new ContentValues[contentValues.size()]));
                        if (insertCount != contentValues.size()) {
                            Log.d(LOG_TAG, "Something went wrong when inserting categorizations");
                        }
                    } else {
                        Log.d(LOG_TAG, "No categories found for this image");
                    }
                    categorizedCount++;
                }
            }
        } finally {
            cursor.close();
        }

        builder.setContentTitle(getString(R.string.categorization_done_notification_title))
                .setContentText(getResources().getQuantityString
                        (R.plurals.categorization_done_notification_text, categorizedCount, categorizedCount));
        mNotificationManager.notify(CATEGORIZATION_NOTIFICATION, builder.build());

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
