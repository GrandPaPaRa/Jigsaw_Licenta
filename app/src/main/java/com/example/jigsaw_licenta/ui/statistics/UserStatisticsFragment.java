package com.example.jigsaw_licenta.ui.statistics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserStatisticsFragment extends Fragment {
    private Spinner boardSizeSpinner, timeLimitSpinner;
    private TextView nicknameText;
    private TextView casualGamesCompleted, timeTrialStats;
    private FirebaseStatsHelper statsHelper;
    private Map<String, Object> cachedStats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_statistics, container, false);

        casualGamesCompleted = view.findViewById(R.id.casualGamesCompleted);
        timeTrialStats = view.findViewById(R.id.timeTrialStats);
        nicknameText = view.findViewById(R.id.nicknameText);
        boardSizeSpinner = view.findViewById(R.id.boardSizeSpinner);
        timeLimitSpinner = view.findViewById(R.id.timeLimitSpinner);
        Button mockButton = view.findViewById(R.id.generateMockDataButton);

        statsHelper = new FirebaseStatsHelper();

        statsHelper.loadUserNickname(nickname -> nicknameText.setText("Nickname: " + nickname));
        statsHelper.loadUserStats(boardSizes -> {
            // Check fragment state before UI operations
            if (isAdded() && getActivity() != null) {
                cachedStats = boardSizes;
                setupSpinners();
            }
        });

        mockButton.setOnClickListener(v -> statsHelper.generateMockDataIfNeeded());

        return view;
    }

    private void setupSpinners() {

        ArrayAdapter<String> boardAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, FirebaseStatsHelper.BOARD_SIZES);
        boardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSizeSpinner.setAdapter(boardAdapter);


        List<Integer> timeValues = FirebaseStatsHelper.TIME_TRIAL_TIMES;


        List<String> timeSecondsList = timeValues.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());


        List<String> timeDisplayList = timeValues.stream()
                .map(seconds -> {
                    int mins = seconds / 60;
                    int rem = seconds % 60;
                    if (mins > 0 && rem > 0) return mins + " min " + rem + "s";
                    else if (mins > 0) return mins + " min";
                    else return rem + "s";
                })
                .collect(Collectors.toList());

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, timeDisplayList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(timeAdapter);

        // On selection, use the real time in seconds for internal use
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStatsDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        boardSizeSpinner.setOnItemSelectedListener(listener);
        timeLimitSpinner.setOnItemSelectedListener(listener);
    }

    private void updateStatsDisplay() {
        if (cachedStats == null) return;

        String board = boardSizeSpinner.getSelectedItem().toString();
        int timeIndex = timeLimitSpinner.getSelectedItemPosition();
        String time = FirebaseStatsHelper.TIME_TRIAL_TIMES.get(timeIndex).toString();
        int timeSeconds = Integer.parseInt(time);
        String timeDisplay = (timeSeconds >= 60) ?
                (timeSeconds / 60) + " min" + (timeSeconds % 60 != 0 ? " " + (timeSeconds % 60) + "s" : "")
                : timeSeconds + "s";

        Map<String, Object> boardData = (Map<String, Object>) cachedStats.get(board);
        if (boardData == null) return;

        Long completed = getLongValue(boardData, "completed");

        Map<String, Object> timeTrial = (Map<String, Object>) boardData.get("timeTrial");
        if (timeTrial == null) return;

        Map<String, Object> timeData = (Map<String, Object>) timeTrial.get(time);
        if (timeData == null) return;

        Long attempts = getLongValue(timeData, "attempts");
        Double highScore = getDoubleValue(timeData, "highScore");

        casualGamesCompleted.setText("Casual Games Completed: " + completed);

        String timeTrialText = "Time Trial (" + timeDisplay + ")\n" +
                "High Score: " + String.format("%.1f", highScore) + "\n" +
                "Attempts: " + attempts;

        timeTrialStats.setText(timeTrialText);
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

}