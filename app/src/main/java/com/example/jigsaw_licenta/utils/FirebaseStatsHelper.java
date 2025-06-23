package com.example.jigsaw_licenta.utils;

import android.util.Log;

import com.example.jigsaw_licenta.model.Jigsaw;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class FirebaseStatsHelper {
    private static final String TAG = "FirebaseStatsHelper";
    private static final String USERS_COLLECTION = "users";
    private static final String STATS_COLLECTION = "statistics";
    private static final String GAME_STATS_DOC = "game_stats";
    public static final List<String> BOARD_SIZES = Arrays.asList("4x6", "4x8", "4x10", "6x6", "6x8", "6x10");
    public static final List<Integer> TIME_TRIAL_TIMES = Arrays.asList(30, 60, 120, 180, 300, 600);
    private final FirebaseFirestore db;
    private final String userId;

    public FirebaseStatsHelper() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public void initializeNewUserStats() {
        DocumentReference userStatsRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATS_COLLECTION)
                .document(GAME_STATS_DOC);

        // Create the complete structure with all zeros
        Map<String, Object> statsData = new HashMap<>();
        Map<String, Object> boardSizes = new HashMap<>();

        for (String size : BOARD_SIZES) {
            Map<String, Object> sizeData = new HashMap<>();

            // Casual stats initialized to 0
            sizeData.put("completed", 0);

            // Time trial stats
            Map<String, Object> timeTrialData = new HashMap<>();
            for (int time : TIME_TRIAL_TIMES) {
                Map<String, Object> timeData = new HashMap<>();
                timeData.put("highScore", 0.0);
                timeData.put("attempts", 0);
                timeTrialData.put(String.valueOf(time), timeData);
            }
            sizeData.put("timeTrial", timeTrialData);

            boardSizes.put(size, sizeData);
        }

        statsData.put("boardSizes", boardSizes);

        // Set the document (will create if doesn't exist)
        userStatsRef.set(statsData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "New user stats initialized to zero"))
                .addOnFailureListener(e -> Log.w(TAG, "Error initializing user stats", e));
    }
    public void generateMockDataIfNeeded() {
        DocumentReference userStatsRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATS_COLLECTION)
                .document(GAME_STATS_DOC);

        userStatsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                // Only generate mock data if document doesn't exist OR doesn't have mockDataGenerated flag
                if (!document.exists() || !document.contains("mockDataGenerated")) {
                    createMockData(userStatsRef);
                }
            }
        });
    }

    private void createMockData(DocumentReference userStatsRef) {
        Map<String, Object> statsData = new HashMap<>();
        Map<String, Object> boardSizes = new HashMap<>();
        Random random = new Random();

        // Create structure for each board size
        for (String size : BOARD_SIZES) {
            Map<String, Object> sizeData = new HashMap<>();

            // Casual stats
            sizeData.put("completed", 5 + random.nextInt(10));

            // Time trial stats
            Map<String, Object> timeTrialData = new HashMap<>();
            for (int time : TIME_TRIAL_TIMES) {
                Map<String, Object> timeData = new HashMap<>();
                timeData.put("highScore", 100 + random.nextInt(400));
                timeData.put("attempts", 3 + random.nextInt(7));
                timeTrialData.put(String.valueOf(time), timeData);
            }
            sizeData.put("timeTrial", timeTrialData);

            boardSizes.put(size, sizeData);
        }

        statsData.put("boardSizes", boardSizes);
        statsData.put("mockDataGenerated", true); // Mark as mock data generated

        userStatsRef.set(statsData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Mock data generated for user"))
                .addOnFailureListener(e -> Log.w(TAG, "Error generating mock data", e));
    }

    public void updateCasualGame(Jigsaw game) {
        String boardSize = getBoardKey(game);

        DocumentReference userStatsRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATS_COLLECTION)
                .document(GAME_STATS_DOC);

        userStatsRef.update(
                        "boardSizes." + boardSize + ".completed", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Completed game stats updated"))
                .addOnFailureListener(e -> {
                    // If field doesn't exist (shouldn't happen if initialized)
                    Log.w(TAG, "Error updating stats, initializing", e);
                    initializeNewUserStats(); // Reinitialize if something went wrong
                });
    }
    public void updateTimeTrialScore(Jigsaw game, int timeLimit, float score) {
        String boardSize = getBoardKey(game);
        String timeKey = String.valueOf(timeLimit);

        DocumentReference userStatsRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATS_COLLECTION)
                .document(GAME_STATS_DOC);

        // Always increment attempts
        Map<String, Object> updates = new HashMap<>();
        updates.put("boardSizes." + boardSize + ".timeTrial." + timeKey + ".attempts",
                FieldValue.increment(1));

        // Update high score in transaction
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userStatsRef);
            Double currentHigh = 0.0;

            try {
                currentHigh = snapshot.getDouble(
                        "boardSizes." + boardSize + ".timeTrial." + timeKey + ".highScore");
            } catch (Exception e) {
                // Field doesn't exist (shouldn't happen if initialized properly)
                Log.w(TAG, "High score field missing, initializing", e);
            }

            if (score > currentHigh) {
                transaction.update(userStatsRef,
                        "boardSizes." + boardSize + ".timeTrial." + timeKey + ".highScore",
                        score);
            }
            return null;
        });

        // Execute the updates
        userStatsRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Time trial stats updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating time trial stats", e));
    }
    private String getBoardKey(Jigsaw game){
        return game.getRows() + "x" + game.getCols();
    }
    public interface NicknameCallback {
        void onNicknameLoaded(String nickname);
    }

    public void loadUserNickname(NicknameCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String nickname = documentSnapshot.getString("nickname");
                    callback.onNicknameLoaded(nickname != null ? nickname : "Unknown");
                });
    }

    public interface StatsCallback {
        void onStatsLoaded(Map<String, Object> boardSizes);
    }

    public void loadUserStats(StatsCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATS_COLLECTION)
                .document(GAME_STATS_DOC)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> boardSizes = (Map<String, Object>) documentSnapshot.get("boardSizes");
                        callback.onStatsLoaded(boardSizes);
                    }
                });
    }
    public static void getLeaderboardData(String boardSize, int timeLimit, int limit,
                                          Consumer<List<LeaderboardEntry>> callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collectionGroup("statistics")
                .orderBy("boardSizes." + boardSize + ".timeTrial." + timeLimit + ".highScore", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(statDocs -> {
                    List<Task<DocumentSnapshot>> nicknameTasks = new ArrayList<>();
                    List<LeaderboardEntry> entries = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : statDocs) {
                        try {
                            Map<String, Object> boardSizes = (Map<String, Object>) doc.get("boardSizes");
                            if (boardSizes != null) {
                                Map<String, Object> sizeData = (Map<String, Object>) boardSizes.get(boardSize);
                                if (sizeData != null) {
                                    Map<String, Object> timeTrial = (Map<String, Object>) sizeData.get("timeTrial");
                                    if (timeTrial != null) {
                                        Map<String, Object> timeData = (Map<String, Object>) timeTrial.get(String.valueOf(timeLimit));
                                        if (timeData != null) {
                                            Object scoreObj = timeData.get("highScore");
                                            if (scoreObj instanceof Number) {
                                                double highScore = ((Number) scoreObj).doubleValue();
                                                String userId = doc.getReference().getParent().getParent().getId();
                                                entries.add(new LeaderboardEntry(userId, highScore));
                                                nicknameTasks.add(db.collection("users").document(userId).get());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseStatsHelper", "Error parsing leaderboard entry", e);
                        }
                    }

                    Tasks.whenAllSuccess(nicknameTasks).addOnSuccessListener(taskResults -> {
                        for (int i = 0; i < taskResults.size(); i++) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) taskResults.get(i);
                            String nickname = userDoc.getString("nickname");
                            entries.get(i).nickname = nickname != null ? nickname : "Player";
                        }
                        callback.accept(entries);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStatsHelper", "Failed to fetch leaderboard", e);
                    callback.accept(Collections.emptyList());
                });
    }

    public static class LeaderboardEntry {
        public String userId;
        public double score;
        public String nickname;

        public LeaderboardEntry(String userId, double score) {
            this.userId = userId;
            this.score = score;
            this.nickname = "Loading...";
        }
    }
}