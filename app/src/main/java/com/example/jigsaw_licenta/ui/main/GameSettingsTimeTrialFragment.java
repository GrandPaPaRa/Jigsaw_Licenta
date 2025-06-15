package com.example.jigsaw_licenta.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.ui.authentication.AuthenticationActivity;
import com.example.jigsaw_licenta.viewmodel.GameSettingsTimeTrialViewModel;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class GameSettingsTimeTrialFragment extends Fragment {

    private GameSettingsTimeTrialViewModel viewModel;
    private Slider scaleSlider, rowsSlider, colsSlider;
    private TextView scaleValueText, rowsValueText, colsValueText;
    private Spinner timeTrialSpinner;
    private Button logoutButtonTimeTrial;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GameSettingsTimeTrialViewModel.class);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_settings_time_trial, container, false);

        scaleSlider = view.findViewById(R.id.sliderScaleTimeTrial);
        rowsSlider = view.findViewById(R.id.sliderRowsTimeTrial);
        colsSlider = view.findViewById(R.id.sliderColsTimeTrial);
        scaleValueText = view.findViewById(R.id.textScaleTimeTrial);
        rowsValueText = view.findViewById(R.id.textRowsTimeTrial);
        colsValueText = view.findViewById(R.id.textColsTimeTrial);
        timeTrialSpinner = view.findViewById(R.id.spinnerTimeTrial);
        logoutButtonTimeTrial = view.findViewById(R.id.logoutButtonTimeTrial);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObserversAndListeners();
        setupTimeTrialSpinner();

        // Set initial values
        Float currentScale = viewModel.getImageScale().getValue();
        if (currentScale != null) {
            scaleSlider.setValue(currentScale);
            scaleValueText.setText(String.format(Locale.US, "%.1fx", currentScale));
        }

        Integer currentRows = viewModel.getBoardRows().getValue();
        if (currentRows != null) {
            rowsSlider.setValue(currentRows);
            rowsValueText.setText(String.format(Locale.US, "%d Rows", currentRows));
        }

        Integer currentCols = viewModel.getBoardCols().getValue();
        if (currentCols != null) {
            colsSlider.setValue(currentCols);
            colsValueText.setText(String.format(Locale.US, "%d Columns", currentCols));
        }

        logoutButtonTimeTrial.setOnClickListener(v -> logoutUser());
    }
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    private void setupTimeTrialSpinner() {
        // Update options to include fractions
        Float[] timeOptions = {0.166f, 0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f};
        String[] displayOptions = new String[timeOptions.length];

        // Create display strings with proper labels
        for (int i = 0; i < timeOptions.length; i++) {
            float val = timeOptions[i];
            if (val < 1.0f) {
                // Convert to seconds
                int seconds = (int)(val * 60);
                displayOptions[i] = seconds + " second" + (seconds != 1 ? "s" : "");
            } else if (val == (int)val) {
                // Whole minutes
                displayOptions[i] = (int)val + " minute" + (val != 1 ? "s" : "");
            } else {
                // Fractional minutes (e.g. 1.5)
                displayOptions[i] = val + " minutes";
            }
        }

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                displayOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeTrialSpinner.setAdapter(adapter);

        // Set initial selection
        Float currentDuration = viewModel.getTimeTrialDuration().getValue();
        if (currentDuration != null) {
            for (int i = 0; i < timeOptions.length; i++) {
                if (Float.compare(timeOptions[i], currentDuration) == 0) {
                    timeTrialSpinner.setSelection(i);
                    break;
                }
            }
        }

        // Update listener
        timeTrialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float selectedMinutes = timeOptions[position];
                viewModel.setTimeTrialDuration(selectedMinutes);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Update observer
        viewModel.getTimeTrialDuration().observe(getViewLifecycleOwner(), minutes -> {
            if (minutes != null) {
                for (int i = 0; i < timeOptions.length; i++) {
                    if (Float.compare(timeOptions[i], minutes) == 0) {
                        if (timeTrialSpinner.getSelectedItemPosition() != i) {
                            timeTrialSpinner.setSelection(i);
                        }
                        break;
                    }
                }
            }
        });
    }


    private void setupObserversAndListeners() {
        viewModel.getImageScale().observe(getViewLifecycleOwner(), scale -> {
            if (scale != null) {
                scaleValueText.setText(String.format(Locale.US, "%.1fx", scale));
                // Don't update slider value here to avoid infinite loop
            }
        });

        scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setImageScale(value);
                scaleValueText.setText(String.format(Locale.US, "%.1fx", value));
            }
        });

        viewModel.getBoardRows().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                rowsValueText.setText(String.format(Locale.US, "%d Rows", rows));
                // Don't update slider value here to avoid infinite loop
            }
        });

        rowsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setBoardRows((int) value);
                rowsValueText.setText(String.format(Locale.US, "%d Rows", (int) value));
            }
        });

        viewModel.getBoardCols().observe(getViewLifecycleOwner(), cols -> {
            if (cols != null) {
                colsValueText.setText(String.format(Locale.US, "%d Columns", cols));
                // Don't update slider value here to avoid infinite loop
            }
        });

        colsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setBoardCols((int) value);
                colsValueText.setText(String.format(Locale.US, "%d Columns", (int) value));
            }
        });
    }
}