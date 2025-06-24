package com.example.jigsaw_licenta.ui.main;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;
import com.example.jigsaw_licenta.utils.GameInterface;
import com.example.jigsaw_licenta.utils.NetworkUtils;
import com.example.jigsaw_licenta.viewmodel.AiSettingsModelFactory;
import com.example.jigsaw_licenta.viewmodel.AiSettingsTimeTrialViewModel;
import com.example.jigsaw_licenta.viewmodel.AiSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsModelFactory;
import com.example.jigsaw_licenta.viewmodel.GameSettingsTimeTrialModelFactory;
import com.example.jigsaw_licenta.viewmodel.GameSettingsTimeTrialViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameTimeTrialViewModel;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GameTimeTrialFragment extends BaseGameFragment implements GameInterface {
    ImageView settingsButton;
    private TextView timerTextView;
    private TextView totalScoreTextView;
    private TextView gamesPlayedTextView;
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_game_time_trial;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameViewModel = new ViewModelProvider(requireActivity()).get(GameTimeTrialViewModel.class);

        GameSettingsTimeTrialModelFactory factory = new GameSettingsTimeTrialModelFactory(requireActivity().getApplication(), gameViewModel);
        gameSettingsViewModel = new ViewModelProvider(requireActivity(), factory).get(GameSettingsTimeTrialViewModel.class);

        aiSettingsViewModel = new ViewModelProvider(requireActivity()).get(AiSettingsTimeTrialViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTimer();

        settingsButton = view.findViewById(R.id.settingsButtonTimeTrial);
        settingsButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_menu_game_time_trial_to_gameSettingsTimeTrialFragment);
        });
    }

    private void saveScore(){
        int totalMoves = jigsawGame.quantity; // Assuming you have access to jigsaw
        int totalCells = jigsawGame.getRows() * jigsawGame.getCols();

        float score = gameQualityScore(totalMoves, totalCells);

        // Save score to ViewModel
        ((GameTimeTrialViewModel)gameViewModel).saveGameScore(score);

        // Update UI
        updateScoreDisplay();
    }

    @Override
    protected void onFinishedGame() {
        saveScore();
        resetGame();
    }

    @Override
    public void onResetButtonClick() {
        resetGame();
        resetTimer();
    }


    @Override
    public void resetGame() {
        super.resetGame();
    }

    @Override
    public void initializeViews(View view){

        boardContainer = view.findViewById(R.id.boardContainerTimeTrial);
        nextPiecePreview = view.findViewById(R.id.nextPiecePreviewTimeTrial);
        resetGameButton = view.findViewById(R.id.resetGameButtonTimeTrial);
        movesTextView = view.findViewById(R.id.movesTextViewTimeTrial);
        discardZone = view.findViewById(R.id.discardZoneTimeTrial);
        gameContainer = view.findViewById(R.id.gameContainerTimeTrial);
        hintButton = view.findViewById(R.id.HintButtonTimeTrial);
        previewContainer = view.findViewById(R.id.piecePreviewContainerTimeTrial);
        progressBarHint = view.findViewById(R.id.hintProgressBarTimeTrial);
        timerTextView = view.findViewById(R.id.timerTextView);
        totalScoreTextView = view.findViewById(R.id.totalScoreTextView);
        gamesPlayedTextView = view.findViewById(R.id.gamesPlayedTextView);
    }
    private void updateScoreDisplay() {
        GameTimeTrialViewModel vm = (GameTimeTrialViewModel)gameViewModel;

        // Total score
        Float totalScore = vm.getTotalScore().getValue();
        totalScoreTextView.setText(totalScore != null ?
                String.format(Locale.getDefault(), "Score: %.0f", totalScore) :
                "Score: 0");

        // Games played
        Integer gamesCount = vm.getGamesPlayed().getValue();
        gamesPlayedTextView.setText(gamesCount != null ?
                String.format(Locale.getDefault(), "Games: %d", gamesCount) :
                "Games: 0");

    }
    protected void onTimeExpired(){
        float score = ((GameTimeTrialViewModel)gameViewModel).getTotalScore().getValue();

        //****handle saving the score to the database ******
        if(!NetworkUtils.isOfflineMode(requireActivity().getApplication())){
            new FirebaseStatsHelper().updateTimeTrialScore(jigsawGame,
                    (int)(gameSettingsViewModel.getTimeTrialDuration().getValue() * 60),
                    score,
                    getActivity().getApplication());
        }


        //show score and time up dialog

        showTimeUpDialog(score);

    }
    private void setupTimer() {

        ((GameTimeTrialViewModel)gameViewModel).getTotalScore().observe(getViewLifecycleOwner(), score -> {
            if (score != null && score > 0) {
                updateScoreDisplay();
            } else {
                totalScoreTextView.setText("Score: --");
                gamesPlayedTextView.setText("Games: --");
            }
        });

        // Check if timer is expired or needs reset
        if (gameViewModel.getTimeExpired().getValue() != null &&
                gameViewModel.getTimeExpired().getValue()) {

            resetTimer();
        }
        // If timer is not running at all (null or <= 0)
        else if (gameViewModel.getTimeRemaining().getValue() == null ||
                gameViewModel.getTimeRemaining().getValue() <= 0) {

            Float durationMinutes = gameSettingsViewModel.getTimeTrialDuration().getValue();
            if (durationMinutes != null) {
                long durationMillis = (long)(durationMinutes * 60 * 1000); // Convert float minutes to ms
                gameViewModel.startTimer(durationMillis);
            }
        }

        // Observe time remaining
        gameViewModel.getTimeRemaining().observe(getViewLifecycleOwner(), remaining -> {
            updateTimerDisplay(remaining);
        });

        // Handle time expiration
        gameViewModel.getTimeExpired().observe(getViewLifecycleOwner(), expired -> {
            if (expired != null && expired) {
                onTimeExpired();
            }
        });
    }

   private void resetTimer(){
       // Reset the timer state
       gameViewModel.resetTimer();

       // Start new timer with current duration (now using float minutes)
       Float durationMinutes = gameSettingsViewModel.getTimeTrialDuration().getValue();
       if (durationMinutes != null) {
           long durationMillis = (long)(durationMinutes * 60 * 1000); // Convert float minutes to ms
           gameViewModel.startTimer(durationMillis);
       }
   }
    private void updateTimerDisplay(long millisUntilFinished) {
        float totalMinutes = millisUntilFinished / (60f * 1000f);

        if (totalMinutes < 1.0f) {
            // Show seconds when under 1 minute
            int seconds = (int)(millisUntilFinished / 1000);
            timerTextView.setText(String.format(Locale.getDefault(), "%d sec", seconds));
        } else {
            // Show minutes:seconds when 1 minute or more
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                    TimeUnit.MINUTES.toSeconds(minutes);
            timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }

        // Change color when time is running low
        if (totalMinutes < 0.5f) { // Under 30 seconds
            timerTextView.setTextColor(Color.RED);
        } else {
            timerTextView.setTextColor(Color.BLACK);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (gameViewModel != null) {
            ((GameTimeTrialViewModel)gameViewModel).resumeTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (gameViewModel != null) {
            ((GameTimeTrialViewModel)gameViewModel).pauseTimer();
        }
    }
    private void showTimeUpDialog(float score) {
        GameTimeTrialViewModel vm = (GameTimeTrialViewModel)gameViewModel;
        Float totalScore = vm.getTotalScore().getValue();
        Integer gamesPlayed = vm.getGamesPlayed().getValue();

        String message;
        if (score > 0 && totalScore != null && gamesPlayed != null) {
            message = String.format(Locale.getDefault(),

                            "Total Score: %.0f\n" +
                            "Games Played: %d",
                    totalScore,
                    gamesPlayed);
        } else {
            message = "Your time trial has ended";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Time's Up!")
                .setMessage(message)
                .setPositiveButton("Restart Timer", (d, w) -> {
                    resetTimer();
                    resetGame();
                })
                .setCancelable(false)
                .show();
    }
    public float gameQualityScore(int totalMoves, int totalCells) {
        float theoreticalMinMoves = totalCells / 3.0f;
        float efficiency = theoreticalMinMoves / totalMoves;
       // float penaltyFactor = (float) Math.pow(0.9, (totalMoves - theoreticalMinMoves));
        return efficiency * 100;
    }
}
