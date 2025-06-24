package com.example.jigsaw_licenta.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String OFFLINE_MODE_KEY = "offline_mode";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void setOfflineMode(Application application, boolean isOffline) {
        SharedPreferences.Editor editor =
                application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(OFFLINE_MODE_KEY, isOffline);
        editor.apply();
    }

    public static boolean isOfflineMode(Application application) {
        SharedPreferences prefs =
                application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(OFFLINE_MODE_KEY, false);
    }
}