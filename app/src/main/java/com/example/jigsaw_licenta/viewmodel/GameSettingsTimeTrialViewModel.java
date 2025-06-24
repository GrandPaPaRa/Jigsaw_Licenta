package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class GameSettingsTimeTrialViewModel extends GameSettingsViewModel  {

    private static final String PREFS_NAME = "GameSettingsTimeTrialPrefs";
    private static final String KEY_IMAGE_SCALE = "imageScale";
    private static final String KEY_BOARD_ROWS = "boardRows";
    private static final String KEY_BOARD_COLS = "boardCols";
    private static final String KEY_TIME_TRIAL_DURATION = "timeTrialDuration";
    private static final float DEFAULT_TIME_TRIAL_DURATION = 1.0f;
    private static final int DEFAULT_PREVIEW_QUEUE_SIZE = 6;
    private static final int  DEFAULT_HINT_TIME_SECONDS = 3;
    private static final float DEFAULT_SCALE = 1.0f;
    public static final int DEFAULT_ROWS = 4;
    public static final int DEFAULT_COLS = 6;

    private final MutableLiveData<Float> imageScale = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardRows = new MutableLiveData<>();
    private final MutableLiveData<Integer> boardCols = new MutableLiveData<>();
    private final MutableLiveData<Integer> previewQueueSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> hintTimeSeconds = new MutableLiveData<>(DEFAULT_HINT_TIME_SECONDS);
    private final MutableLiveData<Float> timeTrialDuration = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;

    private final GameViewModel gameViewModel;

    public GameSettingsTimeTrialViewModel(@NonNull Application application, GameViewModel gameViewModel) {
        super(application, gameViewModel);
        this.sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        this.gameViewModel = gameViewModel;

        imageScale.setValue(sharedPreferences.getFloat(KEY_IMAGE_SCALE, DEFAULT_SCALE));
        boardRows.setValue(sharedPreferences.getInt(KEY_BOARD_ROWS, DEFAULT_ROWS));
        boardCols.setValue(sharedPreferences.getInt(KEY_BOARD_COLS, DEFAULT_COLS));
        previewQueueSize.setValue(DEFAULT_PREVIEW_QUEUE_SIZE);
        hintTimeSeconds.setValue(DEFAULT_HINT_TIME_SECONDS);
        timeTrialDuration.setValue(sharedPreferences.getFloat(KEY_TIME_TRIAL_DURATION, DEFAULT_TIME_TRIAL_DURATION));
    }

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

            // Reset game after board change
            gameViewModel.resetGame(rows, boardCols.getValue() != null ? boardCols.getValue() : DEFAULT_COLS);

            gameViewModel.saveGameState();

            resetTimer();
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

            // Reset game after board change
            gameViewModel.resetGame(boardRows.getValue() != null ? boardRows.getValue() : DEFAULT_ROWS, cols);

            gameViewModel.saveGameState();

            resetTimer();
        }
    }

    public LiveData<Integer> getBoardCols() {
        return boardCols;
    }

    public LiveData<Integer> getPreviewQueueSize() {
        return previewQueueSize;
    }

    public LiveData<Integer> getHintTimeSeconds() {
        return hintTimeSeconds;
    }
    public void setTimeTrialDuration(float minutes) {
        if (timeTrialDuration.getValue() == null || timeTrialDuration.getValue() != minutes) {
            timeTrialDuration.setValue(minutes);
            sharedPreferences.edit().putFloat(KEY_TIME_TRIAL_DURATION, minutes).apply();
            getGameViewModel().setTimeExpired(true);
        }
    }
    @Override
    public LiveData<Float> getTimeTrialDuration() {
        return timeTrialDuration;
    }
    public long getDurationMillis() {
        Float minutes = timeTrialDuration.getValue();
        return minutes != null ? (long)(minutes * 60 * 1000) : 0;
    }
    private void resetTimer(){
        // Reset the timer state
        gameViewModel.resetTimer();

        // Start new timer with current duration (now using float minutes)
        Float durationMinutes = timeTrialDuration.getValue();
        if (durationMinutes != null) {
            long durationMillis = (long)(durationMinutes * 60 * 1000); // Convert float minutes to ms
            gameViewModel.startTimer(durationMillis);
        }
    }
}
