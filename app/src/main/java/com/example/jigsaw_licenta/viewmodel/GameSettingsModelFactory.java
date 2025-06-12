package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameSettingsModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final GameViewModel gameViewModel;

    public GameSettingsModelFactory(Application application, GameViewModel gameViewModel) {
        this.application = application;
        this.gameViewModel = gameViewModel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GameSettingsViewModel.class)) {
            return (T) new GameSettingsViewModel(application, gameViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
