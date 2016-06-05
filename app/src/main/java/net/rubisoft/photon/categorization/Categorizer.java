package net.rubisoft.photon.categorization;

import android.net.Uri;

import java.util.List;

public interface Categorizer {
    String[] getCategories();
    List<Categorization> categorizeImage(Uri image);

    public class Categorization {
        public Categorization(String category, float confidence) {
            this.mCategory = category;
            this.mConfidence = confidence;
        }

        private String mCategory;
        private float mConfidence;

        public String getCategory() {
            return mCategory;
        }

        public float getConfidence() {
            return mConfidence;
        }
    }
}
