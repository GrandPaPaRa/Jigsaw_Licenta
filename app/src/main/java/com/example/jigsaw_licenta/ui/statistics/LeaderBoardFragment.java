package com.example.jigsaw_licenta.ui.statistics;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaderBoardFragment extends Fragment {
    private static final int LEADERBOARD_LIMIT = 10;
    private static final String TAG = "LeaderBoardFragment";
    private FirebaseFirestore db;
    private LinearLayout leaderboardContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        leaderboardContainer = view.findViewById(R.id.leaderboardContainer);
        loadLeaderboards();
    }

    private void loadLeaderboards() {
        leaderboardContainer.removeAllViews();

        for (String boardSize : FirebaseStatsHelper.BOARD_SIZES) {
            for (int timeLimit : FirebaseStatsHelper.TIME_TRIAL_TIMES) {
                addLeaderboardSection(boardSize, timeLimit);
            }
        }
    }

    private void addLeaderboardSection(String boardSize, int timeLimit) {
        TextView sectionHeader = new TextView(requireContext());
        sectionHeader.setText(String.format(Locale.getDefault(),
                "Board: %s | Time: %ds", boardSize, timeLimit));
        sectionHeader.setTextSize(18);
        sectionHeader.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        sectionHeader.setPadding(0, 16, 0, 8);
        leaderboardContainer.addView(sectionHeader);

        LinearLayout entriesContainer = new LinearLayout(requireContext());
        entriesContainer.setOrientation(LinearLayout.VERTICAL);
        leaderboardContainer.addView(entriesContainer);

        TextView loadingText = new TextView(requireContext());
        loadingText.setText("Loading...");
        loadingText.setPadding(16, 4, 0, 4);
        entriesContainer.addView(loadingText);

        // Query all documents where the path exists, regardless of value
        db.collectionGroup("statistics")
                .orderBy("boardSizes." + boardSize + ".timeTrial." + timeLimit + ".highScore", Query.Direction.DESCENDING)
                .limit(LEADERBOARD_LIMIT)
                .get()
                .addOnCompleteListener(task -> {
                    entriesContainer.removeAllViews();

                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Query failed", task.getException());
                        showErrorMessage(entriesContainer, "Error loading data");
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        showErrorMessage(entriesContainer, "No scores available");
                        return;
                    }

                    List<Task<DocumentSnapshot>> nicknameTasks = new ArrayList<>();
                    List<LeaderboardEntry> entries = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            // Safely get the nested highScore value
                            Map<String,Object> boardSizes = (Map<String,Object>) document.get("boardSizes");
                            if (boardSizes != null) {
                                Map<String,Object> sizeData = (Map<String,Object>) boardSizes.get(boardSize);
                                if (sizeData != null) {
                                    Map<String,Object> timeTrial = (Map<String,Object>) sizeData.get("timeTrial");
                                    if (timeTrial != null) {
                                        Map<String,Object> timeData = (Map<String,Object>) timeTrial.get(String.valueOf(timeLimit));
                                        if (timeData != null) {
                                            Object scoreObj = timeData.get("highScore");
                                            if (scoreObj instanceof Number) {
                                                double highScore = ((Number)scoreObj).doubleValue();
                                                String userId = document.getReference().getParent().getParent().getId();
                                                entries.add(new LeaderboardEntry(userId, highScore));
                                                nicknameTasks.add(db.collection("users").document(userId).get());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document", e);
                        }
                    }

                    // Process all nickname fetches
                    Tasks.whenAllComplete(nicknameTasks).addOnCompleteListener(nickTask -> {
                        entriesContainer.removeAllViews();
                        int rank = 1;

                        for (int i = 0; i < entries.size(); i++) {
                            try {
                                Task<DocumentSnapshot> nickTaskSingle = (Task<DocumentSnapshot>) nicknameTasks.get(i);
                                if (nickTaskSingle.isSuccessful() && nickTaskSingle.getResult() != null) {
                                    DocumentSnapshot userDoc = nickTaskSingle.getResult();
                                    String nickname = userDoc.getString("nickname");
                                    double score = entries.get(i).score;

                                    TextView entry = new TextView(requireContext());
                                    entry.setText(String.format(Locale.getDefault(),
                                            "%d. %s: %.1f", rank++,
                                            nickname != null ? nickname : "Player",
                                            score));
                                    entry.setPadding(16, 4, 0, 4);
                                    entriesContainer.addView(entry);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error displaying entry: " + e.getMessage());
                            }
                        }
                    });
                });
    }

    private void showErrorMessage(LinearLayout container, String message) {
        TextView errorText = new TextView(requireContext());
        errorText.setText(message);
        errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        errorText.setPadding(16, 4, 0, 4);
        container.addView(errorText);
    }

    private static class LeaderboardEntry {
        String userId;
        double score;

        LeaderboardEntry(String userId, double score) {
            this.userId = userId;
            this.score = score;
        }
    }
}