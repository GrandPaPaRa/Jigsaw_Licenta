package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AiSettingsViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "AiSettingsPrefs";
    private static final String KEY_ITERATIONS = "iterationsPerMove";
    private static final String KEY_SIMULATION_EXPLORATION_RATE = "simulationExplorationRate";;
    private static final String KEY_EXPLORATION = "explorationFactor";
    private static final String KEY_DEPTH_COEF = "depthCoefficient";
    private static final String KEY_GAMES_TO_SIMULATE = "gamesToSimulate";

    private static final int DEFAULT_ITERATIONS = 10000;
    private static final float DEFAULT_EXPLORATION = 1.4f;
    private static final float DEFAULT_SIMULATION_EXPLORATION_RATE = 0.9f;
    private static final float DEFAULT_DEPTH_COEF = 2.0f;
    private static final int DEFAULT_GAMES_TO_SIMULATE = 10;

    private final MutableLiveData<Integer> iterationsPerMove = new MutableLiveData<>();
    private final MutableLiveData<Float> simulationExplorationFactor = new MutableLiveData<>();
    private final MutableLiveData<Float> explorationFactor = new MutableLiveData<>();
    private final MutableLiveData<Float> depthCoefficient = new MutableLiveData<>();
    private final MutableLiveData<Integer> gamesToSimulate = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;
    private GameSettingsViewModel gameSettingsViewModel;

    public AiSettingsViewModel(@NonNull Application application, GameSettingsViewModel gameSettingsViewModel) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);

        this.gameSettingsViewModel = gameSettingsViewModel;

        iterationsPerMove.setValue(sharedPreferences.getInt(KEY_ITERATIONS, DEFAULT_ITERATIONS));
        simulationExplorationFactor.setValue(sharedPreferences.getFloat(KEY_SIMULATION_EXPLORATION_RATE, DEFAULT_SIMULATION_EXPLORATION_RATE));
        explorationFactor.setValue(sharedPreferences.getFloat(KEY_EXPLORATION, DEFAULT_EXPLORATION));
        depthCoefficient.setValue(sharedPreferences.getFloat(KEY_DEPTH_COEF, DEFAULT_DEPTH_COEF));
        gamesToSimulate.setValue(sharedPreferences.getInt(KEY_GAMES_TO_SIMULATE, DEFAULT_GAMES_TO_SIMULATE));
    }
    public GameSettingsViewModel getGameSettingsViewModel() {
        return gameSettingsViewModel;
    }

    // Iterations per move
    public void setIterationsPerMove(int iterations) {
        if (iterations >= 0 && iterations <= 100000 &&
                (iterationsPerMove.getValue() == null || iterationsPerMove.getValue() != iterations)) {
            iterationsPerMove.setValue(iterations);
            sharedPreferences.edit().putInt(KEY_ITERATIONS, iterations).apply();
        }
    }
    public LiveData<Integer> getIterationsPerMove() {
        return iterationsPerMove;
    }

    // Time per move
    public void setSimulationExplorationFactor(float simulationExplorationRate) {
        if (simulationExplorationRate >= 0 && simulationExplorationRate <= 1 &&
                (simulationExplorationFactor.getValue() == null || simulationExplorationFactor.getValue() != simulationExplorationRate)) {
            simulationExplorationFactor.setValue(simulationExplorationRate);
            sharedPreferences.edit().putFloat(KEY_SIMULATION_EXPLORATION_RATE, simulationExplorationRate).apply();
        }
    }
    public LiveData<Float> getSimulationExplorationFactor() {
        return simulationExplorationFactor;
    }

    // Exploration factor
    public void setExplorationFactor(float factor) {
        if (factor >= 0 && factor <= 4 &&
                (explorationFactor.getValue() == null || explorationFactor.getValue() != factor)) {
            explorationFactor.setValue(factor);
            sharedPreferences.edit().putFloat(KEY_EXPLORATION, factor).apply();
        }
    }
    public LiveData<Float> getExplorationFactor() {
        return explorationFactor;
    }

    // Depth coefficient
    public void setDepthCoefficient(float coef) {
        if (coef >= 1 && coef <= 4 &&
                (depthCoefficient.getValue() == null || depthCoefficient.getValue() != coef)) {
            depthCoefficient.setValue(coef);
            sharedPreferences.edit().putFloat(KEY_DEPTH_COEF, coef).apply();
        }
    }
    public LiveData<Float> getDepthCoefficient() {
        return depthCoefficient;
    }

    // Games to simulate
    public void setGamesToSimulate(int games) {
        if (games >= 0 && games <= 10000 &&
                (gamesToSimulate.getValue() == null || gamesToSimulate.getValue() != games)) {
            gamesToSimulate.setValue(games);
            sharedPreferences.edit().putInt(KEY_GAMES_TO_SIMULATE, games).apply();
        }
    }
    public LiveData<Integer> getGamesToSimulate() {
        return gamesToSimulate;
    }
}