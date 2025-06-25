package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AiSettingsTimeTrialViewModel extends AiSettingsViewModel {
    private static final float DEFAULT_EXPLORATION = 1.4f;
    private static final float DEFAULT_SIMULATION_EXPLORATION_RATE = 1.0f;
    private static final float DEFAULT_DEPTH_COEF = 2.4f;
    private final MutableLiveData<Float> simulationExplorationFactor = new MutableLiveData<>();
    private final MutableLiveData<Float> explorationFactor = new MutableLiveData<>();
    private final MutableLiveData<Float> depthCoefficient = new MutableLiveData<>();

    public AiSettingsTimeTrialViewModel(@NonNull Application application) {

        super(application, null);

        simulationExplorationFactor.setValue(DEFAULT_SIMULATION_EXPLORATION_RATE);
        explorationFactor.setValue(DEFAULT_EXPLORATION);
        depthCoefficient.setValue(DEFAULT_DEPTH_COEF);
    }

    // Override parent setters to make them do nothing (immutable)
    @Override
    public void setIterationsPerMove(int iterations) {
        // No operation - values are immutable in this subclass
    }

    @Override
    public void setSimulationExplorationFactor(float simulationExplorationRate) {
        // No operation - values are immutable in this subclass
    }

    @Override
    public void setExplorationFactor(float factor) {
        // No operation - values are immutable in this subclass
    }

    @Override
    public void setDepthCoefficient(float coef) {
        // No operation - values are immutable in this subclass
    }

    @Override
    public void setGamesToSimulate(int games) {
        // No operation - values are immutable in this subclass
    }

    @Override
    public GameSettingsViewModel getGameSettingsViewModel() {
        // Return null since we don't want this dependency
        return null;
    }
    @Override
    public LiveData<Float> getSimulationExplorationFactor() {
        return simulationExplorationFactor;
    }
    @Override
    public LiveData<Float> getExplorationFactor() {
        return explorationFactor;
    }
    @Override
    public LiveData<Float> getDepthCoefficient() {
        return depthCoefficient;
    }
}