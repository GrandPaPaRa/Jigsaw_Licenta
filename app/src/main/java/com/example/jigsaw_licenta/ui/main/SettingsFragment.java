package com.example.jigsaw_licenta.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.viewmodel.SharedViewModel;
import com.google.android.material.slider.Slider;


public class SettingsFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private Slider scaleSlider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Activity-scoped SharedViewModel instance.
        // IMPORTANT: Use requireActivity() as the ViewModelStoreOwner to scope it to the Activity.
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        scaleSlider = view.findViewById(R.id.scaleSlider);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set initial slider value from ViewModel if it's already set
        if (sharedViewModel.getImageScale().getValue() != null) {
            scaleSlider.setValue(sharedViewModel.getImageScale().getValue());
        }

        scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                sharedViewModel.setImageScale(value);
            }
        });
    }
}
