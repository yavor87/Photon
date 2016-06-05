package net.rubisoft.photon;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import net.rubisoft.photon.service.CacheService;

public class PhotonApp extends Application {
    public static final String INITIALIZED_KEY = "initialized";
    public static final String PREFERENCES = "preferences";
    private static String LOG_TAG = PhotonApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences pref = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if (!pref.getBoolean(INITIALIZED_KEY, false)) {
            initialConfiguration();
            pref.edit().putBoolean(INITIALIZED_KEY, true).apply();
        }
    }

    private void initialConfiguration() {
        Log.v(LOG_TAG, "Doing initial configuration");
        startService(new Intent(this, CacheService.class));
    }
}
