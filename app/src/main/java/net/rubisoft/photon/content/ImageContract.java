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
        public static final String TABLE_NAME = "categories";

        public static final String NAME = "name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildImagesForCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon()
                    .appendPath("images").build();
        }
    }

    public static class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "images";

        public static final String IMAGE_URI = "uri";
        public static final String THUMBNAIL_URI = "thumb_uri";

        public static final String DEFAULT_SORT_ORDER = ImageEntry.DATE_TAKEN + " DESC";

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

    public static class ImageCategoriesView {
        public static final String VIEW_NAME = "image_categories";

        public static final String _ID = CategoryEntry._ID;
        public static final String IMAGE_ID = CategorizedImageEntry.IMAGE_ID;
        public static final String NAME = CategoryEntry.NAME;
        public static final String CONFIDENCE = CategorizedImageEntry.CONFIDENCE;
    }

    public static class CategorizedImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "categorizedImages";

        public static final String IMAGE_ID = "image_id";
        public static final String CATEGORY_ID = "category_id";
        public static final String CONFIDENCE = "confidence";
    }
}
