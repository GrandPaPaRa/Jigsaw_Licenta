package com.example.jigsaw_licenta.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.viewmodel.SharedViewModel;
import com.google.android.material.slider.Slider;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private Slider scaleSlider, rowsSlider, colsSlider;
    private TextView scaleValueText, rowsValueText, colsValueText;
    private Button standardButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObserversAndListeners();
    }

    private void setupObserversAndListeners() {
        // --- Scale Slider ---
        sharedViewModel.getImageScale().observe(getViewLifecycleOwner(), scale -> {
            if (scale != null) {
                if (scaleSlider.getValue() != scale) { // Avoid feedback loop
                    scaleSlider.setValue(scale);
                }
                scaleValueText.setText(String.format(Locale.US, "%.1fx", scale));
            }
        });
        scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                sharedViewModel.setImageScale(value);
            }
        });

        // --- Rows Slider ---
        sharedViewModel.getBoardRows().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                if ((int) rowsSlider.getValue() != rows) { // Avoid feedback loop
                    rowsSlider.setValue(rows.floatValue());
                }
                rowsValueText.setText(String.format(Locale.US, "%d Rows", rows));
            }
        });
        rowsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                sharedViewModel.setBoardRows((int) value);
            }
        });

        // --- Cols Slider ---
        sharedViewModel.getBoardCols().observe(getViewLifecycleOwner(), cols -> {
            if (cols != null) {
                if ((int) colsSlider.getValue() != cols) { // Avoid feedback loop
                    colsSlider.setValue(cols.floatValue());
                }
                colsValueText.setText(String.format(Locale.US, "%d Columns", cols));
            }
        });
        colsSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                sharedViewModel.setBoardCols((int) value);
            }
        });

        // --- Standard Button ---
        standardButton.setOnClickListener(v -> {
            sharedViewModel.setStandardBoardDimensions();
            // Sliders will update via LiveData observation
        });
    }
}