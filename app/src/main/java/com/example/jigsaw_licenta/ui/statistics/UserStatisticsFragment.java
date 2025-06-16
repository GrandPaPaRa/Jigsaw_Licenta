package com.example.jigsaw_licenta.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.Map;

public class UserStatisticsFragment extends Fragment {
    private FirebaseFirestore db;
    private String userId;
    private TextView nicknameText, statsText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize mock data if needed
        //new FirebaseStatsHelper().initializeMockData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_statistics, container, false);
        nicknameText = view.findViewById(R.id.nicknameText);
        statsText = view.findViewById(R.id.statsText);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button generateMockDataButton = view.findViewById(R.id.generateMockDataButton);
        generateMockDataButton.setOnClickListener(v -> {
            new FirebaseStatsHelper().generateMockDataIfNeeded();
            Toast.makeText(getContext(), "Mock data generated!", Toast.LENGTH_SHORT).show();
            loadUserStats(); // Refresh the displayed stats
        });

        loadUserNickname();
        loadUserStats();
    }

    private void loadUserNickname() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nickname = document.getString("nickname");
                            nicknameText.setText("Nickname: " + (nickname != null ? nickname : "Unknown"));
                        }
                    }
                });
    }

    private void loadUserStats() {
        db.collection("users")
                .document(userId)
                .collection("statistics")
                .document("game_stats")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Build stats string
                            StringBuilder statsBuilder = new StringBuilder();
                            Map<String, Object> boardSizes = (Map<String, Object>) document.get("boardSizes");

                            if (boardSizes != null) {
                                for (String size : FirebaseStatsHelper.BOARD_SIZES) {
                                    Map<String, Object> sizeData = (Map<String, Object>) boardSizes.get(size);
                                    if (sizeData != null) {
                                        statsBuilder.append("\nBoard Size: ").append(size).append("\n");

                                        // Casual stats
                                        Long completed = getLongValue(sizeData, "completed");
                                        statsBuilder.append("Completed: ").append(completed != null ? completed : 0).append("\n");

                                        // Time trial stats
                                        Map<String, Object> timeTrial = (Map<String, Object>) sizeData.get("timeTrial");
                                        if (timeTrial != null) {
                                            statsBuilder.append("Time Trial:\n");
                                            for (int time : FirebaseStatsHelper.TIME_TRIAL_TIMES) {
                                                Map<String, Object> timeData = (Map<String, Object>) timeTrial.get(String.valueOf(time));
                                                if (timeData != null) {
                                                    Long attempts = getLongValue(timeData, "attempts");
                                                    Double highScore = getDoubleValue(timeData, "highScore");
                                                    statsBuilder.append(time).append("s: ")
                                                            .append("High: ").append(highScore != null ? String.format("%.1f", highScore) : "0")
                                                            .append(" (Attempts: ").append(attempts != null ? attempts : 0).append(")\n");
                                                }
                                            }
                                        }
                                        statsBuilder.append("\n");
                                    }
                                }
                            }
                            statsText.setText(statsBuilder.toString());
                        }
                    }
                });
    }

    // Helper methods remain the same
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return null;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return null;
    }
}