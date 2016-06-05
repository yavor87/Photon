package net.rubisoft.photon.categorization;

import android.net.Uri;

public interface Categorizer {
    String[] getCategories();
    Categorization categorizeImage(Uri image);

    public class Categorization {
        public Categorization(Uri image, String category) {
            this.mUri = image;
            this.mCategory = category;
        }

        private Uri mUri;
        private String mCategory;

        public Uri getImage() {
            return mUri;
        }

        public String getCategory() {
            return mCategory;
        }
    }
}
