package com.example.jigsaw_licenta.viewmodel;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
public class GameTimeTrialViewModel extends GameViewModel{

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_TIME_REMAINING = "timeRemaining";
    private static final String KEY_TOTAL_SCORE = "totalScore";
    private static final String KEY_GAMES_PLAYED = "gamesPlayed";
    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<Long> timeRemaining = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timeExpired = new MutableLiveData<>(false);
    private final MutableLiveData<Float> totalScore = new MutableLiveData<>();
    private final MutableLiveData<Integer> gamesPlayed = new MutableLiveData<>();
    private CountDownTimer countDownTimer;

    public GameTimeTrialViewModel(@NonNull Application application) {
        super(application);

        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        timeRemaining.setValue(sharedPreferences.getLong(KEY_TIME_REMAINING, 0));
        totalScore.setValue(sharedPreferences.getFloat(KEY_TOTAL_SCORE, 0));
        gamesPlayed.setValue(sharedPreferences.getInt(KEY_GAMES_PLAYED, 0));

    }
    @Override
    public LiveData<Long> getTimeRemaining() {
        return timeRemaining;
    }
    @Override
    public LiveData<Boolean> getTimeExpired() {
        return timeExpired;
    }
    @Override
    public void startTimer(long durationMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timeRemaining.setValue(durationMillis);
        timeExpired.setValue(false);

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining.setValue(millisUntilFinished);
                sharedPreferences.edit()
                        .putLong(KEY_TIME_REMAINING, millisUntilFinished)
                        .apply();
            }

            @Override
            public void onFinish() {
                timeRemaining.setValue(0L);
                timeExpired.setValue(true);
                sharedPreferences.edit()
                        .remove(KEY_TIME_REMAINING)
                        .apply();

                // Reset the expired flag after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    timeExpired.setValue(false);
                }, 1000);
            }
        }.start();
    }
    @Override
    public void setTimeExpired(boolean expired){
        timeExpired.setValue(expired);
    }
    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeRemaining.setValue(0L);
        timeExpired.setValue(false);
        resetScore();
        sharedPreferences.edit().remove(KEY_TIME_REMAINING).apply();
    }
    public void resumeTimer() {
        Long remaining = timeRemaining.getValue();
        if (remaining != null && remaining > 0) {
            startTimer(remaining);
        }
    }

    public void saveGameScore(float score) {
        float newTotal = (totalScore.getValue() != null ? totalScore.getValue() : 0) + score;
        int newGamesCount = (gamesPlayed.getValue() != null ? gamesPlayed.getValue() : 0) + 1;
        totalScore.setValue(newTotal);
        gamesPlayed.setValue(newGamesCount);
        sharedPreferences.edit()
                .putFloat(KEY_TOTAL_SCORE, newTotal)
                .putInt(KEY_GAMES_PLAYED, newGamesCount)
                .apply();
    }
    public void resetScore() {
        totalScore.setValue(0f);
        gamesPlayed.setValue(0);
        sharedPreferences.edit()
                .remove(KEY_TOTAL_SCORE)
                .remove(KEY_GAMES_PLAYED)
                .apply();
    }
    public LiveData<Float> getTotalScore() {
        return totalScore;
    }

    public LiveData<Integer> getGamesPlayed() {
        return gamesPlayed;
    }
}
