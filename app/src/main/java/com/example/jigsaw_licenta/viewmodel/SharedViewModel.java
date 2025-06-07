package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SharedViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "GameSettingsPrefs"; // Consolidated prefs name
    private static final String KEY_IMAGE_SCALE = "imageScale";
    private static final String KEY_BOARD_ROWS = "boardRows";
    private static final String KEY_BOARD_COLS = "boardCols";

    private static final float DEFAULT_SCALE = 1.0f;
    public static final int DEFAULT_ROWS = 4; // Public for easy access by Settings
    public static final int DEFAULT_COLS = 6; // Public

    private final MutableLiveData<Float> imageScale = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardRows = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardCols = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    public SharedViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);

        imageScale.setValue(sharedPreferences.getFloat(KEY_IMAGE_SCALE, DEFAULT_SCALE));
        boardRows.setValue(sharedPreferences.getInt(KEY_BOARD_ROWS, DEFAULT_ROWS));
        boardCols.setValue(sharedPreferences.getInt(KEY_BOARD_COLS, DEFAULT_COLS));
    }

    // --- Image Scale ---
    public void setImageScale(float scale) {
        if (imageScale.getValue() == null || imageScale.getValue() != scale) {
            imageScale.setValue(scale);
            sharedPreferences.edit().putFloat(KEY_IMAGE_SCALE, scale).apply();
        }
    }
    public LiveData<Float> getImageScale() {
        return imageScale;
    }

    // --- Board Rows ---
    public void setBoardRows(int rows) {
        if (rows > 0 && (boardRows.getValue() == null || boardRows.getValue() != rows)) {
            boardRows.setValue(rows);
            sharedPreferences.edit().putInt(KEY_BOARD_ROWS, rows).apply();
        }
    }
    public LiveData<Integer> getBoardRows() {
        return boardRows;
    }

    // --- Board Columns ---
    public void setBoardCols(int cols) {
        if (cols > 0 && (boardCols.getValue() == null || boardCols.getValue() != cols)) {
            boardCols.setValue(cols);
            sharedPreferences.edit().putInt(KEY_BOARD_COLS, cols).apply();
        }
    }
    public LiveData<Integer> getBoardCols() {
        return boardCols;
    }

    // --- Convenience for setting standard dimensions ---
    public void setStandardBoardDimensions() {
        setBoardRows(DEFAULT_ROWS);
        setBoardCols(DEFAULT_COLS);
    }
}