package com.example.jigsaw_licenta.ui.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;
import com.example.jigsaw_licenta.utils.GameInterface;
import com.example.jigsaw_licenta.viewmodel.AiSettingsModelFactory;
import com.example.jigsaw_licenta.viewmodel.AiSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsModelFactory;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameTimeTrialViewModel;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;

public class GameFragment extends BaseGameFragment implements GameInterface {

    ImageView settingsButton;
    public GameFragment() {
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_game;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        GameSettingsModelFactory factory = new GameSettingsModelFactory(requireActivity().getApplication(), gameViewModel);
        gameSettingsViewModel = new ViewModelProvider(requireActivity(), factory).get(GameSettingsViewModel.class);

        AiSettingsModelFactory aiFactory = new AiSettingsModelFactory(requireActivity().getApplication(), gameSettingsViewModel);
        aiSettingsViewModel = new ViewModelProvider(requireActivity(), aiFactory).get(AiSettingsViewModel.class);
    }

    @Override
    public void resetGame() {
        super.resetGame();
    }

    @Override
    public void initializeViews(View view){

        boardContainer = view.findViewById(R.id.boardContainer);
        nextPiecePreview = view.findViewById(R.id.nextPiecePreview);
        resetGameButton = view.findViewById(R.id.resetGameButton);
        movesTextView = view.findViewById(R.id.movesTextView);
        discardZone = view.findViewById(R.id.discardZone);
        gameContainer = view.findViewById(R.id.gameContainer);
        hintButton = view.findViewById(R.id.HintButton);
        previewContainer = view.findViewById(R.id.piecePreviewContainer);
        progressBarHint = view.findViewById(R.id.hintProgressBar);
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
        settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_gameFragment2_to_settingsFragment2);
        });
    }
    protected void showGameOverDialog() {
        String moves = String.valueOf(jigsawGame.quantity);
        float score = gameQualityScore(jigsawGame.quantity, jigsawGame.getRows() * jigsawGame.getCols());
        new AlertDialog.Builder(requireContext())
                .setTitle("You Win")
                .setMessage("Congratulations!\nMoves: " + moves + "\nScore: " + score)
                .setPositiveButton("Play Again", (dialog, which) -> resetGame())
                .setCancelable(false)
                .show();
    }
    @Override
    protected void onFinishedGame() {
        new FirebaseStatsHelper().updateCasualGame(jigsawGame,requireActivity().getApplication());
        showGameOverDialog();
    }

    @Override
    public void onResetButtonClick() {
        resetGame();
    }
    public float gameQualityScore(int totalMoves, int totalCells) {
        float theoreticalMinMoves = totalCells / 3.0f;
        float efficiency = theoreticalMinMoves / totalMoves;
        // float penaltyFactor = (float) Math.pow(0.9, (totalMoves - theoreticalMinMoves));
        return efficiency * 100;
    }
}