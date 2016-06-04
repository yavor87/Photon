package net.rubisoft.photon.content;

import android.provider.BaseColumns;

public class ImagesCache {
    public static class Category implements BaseColumns {
        public static String TABLE_NAME = "categories";

        public static String NAME = "name";
    }

    public static class Image implements BaseColumns {
        public static String TABLE_NAME = "images";

        public static String URI = "uri";
    }

    public static class CategorizedImage implements BaseColumns {
        public static String TABLE_NAME = "categorizedImages";

        public static String IMAGE_ID = "image_id";
        public static String CATEGORY_ID = "category_id";
        public static String CONFIDENCE = "confidence";
    }
}
