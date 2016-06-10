package net.rubisoft.photon.content;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ImageContract {
    public static final String CONTENT_AUTHORITY = "net.rubisoft.photon";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_IMAGE = "image";
    public static final String PATH_UNCATEGORIZED_IMAGES = "uncategorized";
    public static final String PATH_CATEGORY = "category";

    public static class CategoryEntry implements BaseColumns {
        public static String TABLE_NAME = "categories";

        public static String NAME = "name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class ImageEntry implements BaseColumns {
        public static String TABLE_NAME = "images";
        public static String CATEGORIES_VIEW_NAME = "image_categories";

        public static String IMAGE_URI = "uri";
        public static String THUMBNAIL_URI = "thumb_uri";

        /**
         * The date & time that the image was taken in units
         * of milliseconds since jan 1, 1970.
         * <P>Type: INTEGER</P>
         */
        public static final String DATE_TAKEN = "datetaken";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_IMAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_IMAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_IMAGE;

        public static Uri buildImageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri uncategorizedImagesUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_UNCATEGORIZED_IMAGES).build();
        }

        public static Uri buildImageWithCategoriesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon()
                    .appendPath("categories").build();
        }
    }

    public static class CategorizedImageEntry implements BaseColumns {
        public static String TABLE_NAME = "categorizedImages";

        public static String IMAGE_ID = "image_id";
        public static String CATEGORY_ID = "category_id";
        public static String CONFIDENCE = "confidence";
    }
}
