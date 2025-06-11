package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SharedViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final GameViewModel gameViewModel;

    public SharedViewModelFactory(Application application, GameViewModel gameViewModel) {
        this.application = application;
        this.gameViewModel = gameViewModel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SharedViewModel.class)) {
            return (T) new SharedViewModel(application, gameViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
