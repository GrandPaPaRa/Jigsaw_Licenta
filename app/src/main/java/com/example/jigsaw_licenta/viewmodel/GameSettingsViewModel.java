package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class GameSettingsViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "GameSettingsPrefs";
    private static final String KEY_IMAGE_SCALE = "imageScale";
    private static final String KEY_BOARD_ROWS = "boardRows";
    private static final String KEY_BOARD_COLS = "boardCols";
    private static final String KEY_PREVIEW_QUEUE_SIZE = "previewQueueSize";
    private static final int DEFAULT_PREVIEW_QUEUE_SIZE = 3;
    private static final String KEY_HINT_TIME_SECONDS = "hintTimeSeconds";
    private static final int DEFAULT_HINT_TIME_SECONDS = 5;
    private static final float DEFAULT_SCALE = 1.0f;
    public static final int DEFAULT_ROWS = 4;
    public static final int DEFAULT_COLS = 6;

    private final MutableLiveData<Float> imageScale = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardRows = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardCols = new MutableLiveData<>();
    private final MutableLiveData<Integer> previewQueueSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> hintTimeSeconds = new MutableLiveData<>(10);
    private final SharedPreferences sharedPreferences;
    private GameViewModel gameViewModel;

    public GameSettingsViewModel(@NonNull Application application, GameViewModel gameViewModel) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);

        imageScale.setValue(sharedPreferences.getFloat(KEY_IMAGE_SCALE, DEFAULT_SCALE));
        boardRows.setValue(sharedPreferences.getInt(KEY_BOARD_ROWS, DEFAULT_ROWS));
        boardCols.setValue(sharedPreferences.getInt(KEY_BOARD_COLS, DEFAULT_COLS));
        previewQueueSize.setValue(sharedPreferences.getInt(KEY_PREVIEW_QUEUE_SIZE, DEFAULT_PREVIEW_QUEUE_SIZE));
        hintTimeSeconds.setValue(sharedPreferences.getInt(KEY_HINT_TIME_SECONDS, DEFAULT_HINT_TIME_SECONDS));
        this.gameViewModel = gameViewModel;

    }
    public GameViewModel getGameViewModel() {
        return gameViewModel;
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

            //warning about resetting the game here

            gameViewModel.resetGame(boardRows.getValue(), boardCols.getValue());

            gameViewModel.saveGameState();
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

            //warning about resetting the game here

            gameViewModel.resetGame(boardRows.getValue(), boardCols.getValue());

            gameViewModel.saveGameState();
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

    // Preview Queue Size
    public void setPreviewQueueSize(int size) {
        if (size >= 0 && size <= 10 && (previewQueueSize.getValue() == null || previewQueueSize.getValue() != size)) {
            previewQueueSize.setValue(size);
            sharedPreferences.edit().putInt(KEY_PREVIEW_QUEUE_SIZE, size).apply();
        }
    }

    public LiveData<Integer> getPreviewQueueSize() {
        return previewQueueSize;
    }
    public void setHintTimeSeconds(int seconds) {
        if (seconds >= 0 && seconds <= 30 &&
                (hintTimeSeconds.getValue() == null || hintTimeSeconds.getValue() != seconds)) {
            hintTimeSeconds.setValue(seconds);
            sharedPreferences.edit().putInt(KEY_HINT_TIME_SECONDS, seconds).apply();
        }
    }

    public LiveData<Integer> getHintTimeSeconds() {
        return hintTimeSeconds;
    }
    public LiveData<Float> getTimeTrialDuration(){
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }
}