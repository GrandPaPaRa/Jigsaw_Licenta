package com.example.jigsaw_licenta.viewmodel;

import android.app.Application; // Import Application
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel; // Change to AndroidViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SharedViewModel extends AndroidViewModel { // Extend AndroidViewModel

    private static final String PREFS_NAME = "ImageScalePrefs";
    private static final String KEY_IMAGE_SCALE = "imageScale";
    private static final float DEFAULT_SCALE = 1.0f;

    private final MutableLiveData<Float> imageScale = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;

    // Constructor now takes Application
    public SharedViewModel(@NonNull Application application) {
        super(application);
        // Get SharedPreferences instance
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Load the saved scale or use default
        imageScale.setValue(loadScaleFromPrefs());
    }

    private float loadScaleFromPrefs() {
        return sharedPreferences.getFloat(KEY_IMAGE_SCALE, DEFAULT_SCALE);
    }

    private void saveScaleToPrefs(float scale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_IMAGE_SCALE, scale);
        editor.apply(); // Use apply() for asynchronous saving
    }

    public void setImageScale(float scale) {
        if (imageScale.getValue() == null || imageScale.getValue() != scale) {
            imageScale.setValue(scale);
            saveScaleToPrefs(scale); // Save when the value changes
        }
    }

    public LiveData<Float> getImageScale() {
        return imageScale;
    }
}