package net.rubisoft.photon;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import net.rubisoft.photon.service.CacheService;

public class PhotonApp extends Application {
    public static final String INITIALIZED_KEY = "initialized";
    public static final String PREFERENCES = "preferences";

    @Override
    public void onCreate() {
        super.onCreate();

        Intent cacheServiceIntent = new Intent(this, CacheService.class);
        int mode = CacheService.MODE_IMAGES;

        SharedPreferences pref = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if (!pref.getBoolean(INITIALIZED_KEY, false)) {
            mode = CacheService.MODE_ALL;
            pref.edit().putBoolean(INITIALIZED_KEY, true).apply();
        }

        cacheServiceIntent.putExtra(CacheService.MODE_KEY, mode);
        startService(cacheServiceIntent);
    }
}
