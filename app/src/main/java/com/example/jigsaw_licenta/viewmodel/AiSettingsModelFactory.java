package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class AiSettingsModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final GameSettingsViewModel gameSettingsViewModel;

    public AiSettingsModelFactory(Application application, GameSettingsViewModel gameSettingsViewModel) {
        this.application = application;
        this.gameSettingsViewModel = gameSettingsViewModel;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AiSettingsViewModel.class)) {
            return (T) new AiSettingsViewModel(application, gameSettingsViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
