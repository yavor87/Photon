package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.categorization.ImaggaCategorizer;

public class CategorizationService extends IntentService {
    public CategorizationService() {
        super("CategorizationService");
        mCategorizer = ImaggaCategorizer.getInstance();
    }

    private Categorizer mCategorizer;

    @Override
    protected void onHandleIntent(Intent intent) {
        // get image File from intent

        Uri imageUri = Uri.parse("http://i.imgur.com/CqmBjo5.jpeg");
        mCategorizer.categorizeImage(imageUri);
    }
}
