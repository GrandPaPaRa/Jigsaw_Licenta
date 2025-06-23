package com.example.jigsaw_licenta.ui.statistics;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderBoardFragment extends Fragment {
    private static final int LEADERBOARD_LIMIT = 10;
    private static final String TAG = "LeaderBoardFragment";
    private Spinner boardSizeSpinner, timeLimitSpinner, limitSpinner;
    private LinearLayout leaderboardContainer;
    private int spinnerInitCount = 0;
    private boolean initialLoadComplete = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boardSizeSpinner = view.findViewById(R.id.boardSizeSpinner);
        timeLimitSpinner = view.findViewById(R.id.timeLimitSpinner);
        limitSpinner = view.findViewById(R.id.limitSpinner);
        leaderboardContainer = view.findViewById(R.id.leaderboardContainer);

        setupSpinners();
    }

    private void setupSpinners() {
        ArrayAdapter<String> boardAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, FirebaseStatsHelper.BOARD_SIZES);
        boardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSizeSpinner.setAdapter(boardAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                FirebaseStatsHelper.TIME_TRIAL_TIMES.stream()
                        .map(seconds -> {
                            if (seconds < 60) {
                                return seconds + " sec";
                            } else if (seconds % 60 == 0) {
                                return (seconds / 60) + " min";
                            } else {
                                return String.format(Locale.getDefault(), "%.1f min", seconds / 60.0);
                            }
                        })
                        .collect(Collectors.toList()));
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(timeAdapter);
        ArrayAdapter<String> limitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("10", "50", "100"));
        limitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        limitSpinner.setAdapter(limitAdapter);

        AdapterView.OnItemSelectedListener refreshListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerInitCount++;
                if (spinnerInitCount >= 3 && !initialLoadComplete) {
                    initialLoadComplete = true; // only once
                    fetchLeaderboard();
                } else if (initialLoadComplete) {
                    fetchLeaderboard();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        boardSizeSpinner.setOnItemSelectedListener(refreshListener);
        timeLimitSpinner.setOnItemSelectedListener(refreshListener);
        limitSpinner.setOnItemSelectedListener(refreshListener);
    }

    private void fetchLeaderboard() {
        String board = boardSizeSpinner.getSelectedItem().toString();
        int selectedIndex = timeLimitSpinner.getSelectedItemPosition();
        int time = FirebaseStatsHelper.TIME_TRIAL_TIMES.get(selectedIndex); // still in seconds
        int limit = Integer.parseInt(limitSpinner.getSelectedItem().toString());

        leaderboardContainer.removeAllViews();

        FirebaseStatsHelper.getLeaderboardData(board, time, limit, (entries) -> {
            int rank = 1;
            for (FirebaseStatsHelper.LeaderboardEntry entry : entries) {
                TextView text = new TextView(requireContext());
                text.setText(String.format(Locale.getDefault(),
                        "%d. %s: %.1f", rank++, entry.nickname, entry.score));
                text.setTextSize(16f);
                text.setPadding(16, 8, 16, 8);
                leaderboardContainer.addView(text);
            }

            if (entries.isEmpty()) {
                TextView empty = new TextView(requireContext());
                empty.setText("No entries found.");
                leaderboardContainer.addView(empty);
            }
        });
    }
}