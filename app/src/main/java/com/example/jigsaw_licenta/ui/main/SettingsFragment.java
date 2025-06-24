package com.example.jigsaw_licenta.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.ui.authentication.AuthenticationActivity;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private GameSettingsViewModel gameSettingsViewModel;
    private Slider scaleSlider, rowsSlider, colsSlider;
    private TextView scaleValueText, rowsValueText, colsValueText;
    private Slider previewQueueSlider;
    private TextView previewQueueValueText;
    private Button standardButton;
    private Slider hintTimeSlider;
    private TextView hintTimeValueText;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSettingsViewModel = new ViewModelProvider(requireActivity()).get(GameSettingsViewModel.class);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        scaleSlider = view.findViewById(R.id.scaleSlider);
        rowsSlider = view.findViewById(R.id.rowsSlider);
        colsSlider = view.findViewById(R.id.colsSlider);
        scaleValueText = view.findViewById(R.id.scaleValueText);
        rowsValueText = view.findViewById(R.id.rowsValueText);
        colsValueText = view.findViewById(R.id.colsValueText);
        standardButton = view.findViewById(R.id.standardButton);
        previewQueueSlider = view.findViewById(R.id.previewQueueSlider);
        previewQueueValueText = view.findViewById(R.id.previewQueueValueText);
        hintTimeSlider = view.findViewById(R.id.hintTimeSlider);
        hintTimeValueText = view.findViewById(R.id.hintTimeValueText);
        logoutButton = view.findViewById(R.id.logoutButton);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObserversAndListeners();

        ImageView aiSettingsButton = view.findViewById(R.id.aiSettingsButton);
        aiSettingsButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_aiSettingsFragment);
        });

        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void setupObserversAndListeners() {
        // --- Scale Slider ---
        gameSettingsViewModel.getImageScale().observe(getViewLifecycleOwner(), scale -> {
            if (scale != null) {
                if (scaleSlider.getValue() != scale) { // Avoid feedback loop
                    scaleSlider.setValue(scale);
                }
                scaleValueText.setText(String.format(Locale.US, "%.1fx", scale));
            }
        });
        scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                gameSettingsViewModel.setImageScale(value);
            }
        });

        // --- Rows Slider ---
        gameSettingsViewModel.getBoardRows().observe(getViewLifecycleOwner(), rows -> {

            if (rows != null) {
                if ((int) rowsSlider.getValue() != rows) { // Avoid feedback loop
                    rowsSlider.setValue(rows.floatValue());
                }
                rowsValueText.setText(String.format(Locale.US, "%d Rows", rows));
            }
        });
        rowsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                gameSettingsViewModel.setBoardRows((int) value);
            }
        });

        // --- Cols Slider ---
        gameSettingsViewModel.getBoardCols().observe(getViewLifecycleOwner(), cols -> {
            if (cols != null) {
                if ((int) colsSlider.getValue() != cols) { // Avoid feedback loop
                    colsSlider.setValue(cols.floatValue());
                }
                colsValueText.setText(String.format(Locale.US, "%d Columns", cols));
            }
        });
        colsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                gameSettingsViewModel.setBoardCols((int) value);
            }
        });
        // --- Preview Queue Size ---
        gameSettingsViewModel.getPreviewQueueSize().observe(getViewLifecycleOwner(), size -> {
            if (size != null) {
                if ((int) previewQueueSlider.getValue() != size) {
                    previewQueueSlider.setValue(size);
                }
                previewQueueValueText.setText(String.format(Locale.US, "%d pieces", size));
            }
        });
        previewQueueSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                gameSettingsViewModel.setPreviewQueueSize((int) value);
            }
        });


        gameSettingsViewModel.getHintTimeSeconds().observe(getViewLifecycleOwner(), time -> {
            if (time != null) {
                if ((int) hintTimeSlider.getValue() != time) {
                    hintTimeSlider.setValue(time);
                }
                hintTimeValueText.setText(String.format(Locale.US, "%d seconds", time));
            }
        });

        hintTimeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                gameSettingsViewModel.setHintTimeSeconds((int) value);
            }
            hintTimeValueText.setText(String.format(Locale.US, "%d seconds", (int) value));
        });


        // --- Standard Button ---
        standardButton.setOnClickListener(v -> {
            gameSettingsViewModel.setStandardBoardDimensions();
            // Sliders will update via LiveData observation
        });
    }
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        GameViewModel.clearSavedGames(requireActivity().getApplication());

        startActivity(intent);
        requireActivity().finish();
    }
}