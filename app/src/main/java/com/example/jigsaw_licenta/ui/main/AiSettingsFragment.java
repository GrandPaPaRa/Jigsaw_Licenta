package com.example.jigsaw_licenta.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.StopType;
import com.example.jigsaw_licenta.model.Tree;
import com.example.jigsaw_licenta.utils.Config;
import com.example.jigsaw_licenta.utils.SimulationData;
import com.example.jigsaw_licenta.utils.SimulationResult;
import com.example.jigsaw_licenta.viewmodel.AiSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiSettingsFragment extends Fragment {
    private AiSettingsViewModel aiSettingsViewModel;
    private Slider iterationsSlider, simulationExplorationRateSlider, explorationSlider, depthCoefSlider, gamesSlider;
    private TextView iterationsValueText, simulationExplorationRateText, explorationValueText,
            depthCoefValueText, gamesValueText;
    Button simulateGamesButton;
    TextView scoreTextView, textBoardInfo;
    private ProgressBar simulationProgressBar;
    private TextView progressTextView;
    int rows, cols;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiSettingsViewModel = new ViewModelProvider(requireActivity()).get(AiSettingsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        iterationsSlider = view.findViewById(R.id.scaleSlider);
        simulationExplorationRateSlider = view.findViewById(R.id.scaleSlider2);
        explorationSlider = view.findViewById(R.id.scaleSlider3);
        depthCoefSlider = view.findViewById(R.id.scaleSlider4);
        gamesSlider = view.findViewById(R.id.scaleSlider5);
        simulationProgressBar = view.findViewById(R.id.simulationProgressBar);
        progressTextView = view.findViewById(R.id.progressTextView);

        iterationsValueText = view.findViewById(R.id.textView4);
        simulationExplorationRateText = view.findViewById(R.id.textView5);
        explorationValueText = view.findViewById(R.id.textView7);
        depthCoefValueText = view.findViewById(R.id.textView14);
        gamesValueText = view.findViewById(R.id.textView16);
        textBoardInfo = view.findViewById(R.id.textBoardInfo);

        simulateGamesButton = view.findViewById(R.id.simulateGamesButton);
        scoreTextView = view.findViewById(R.id.scoreTextView);

        GameSettingsViewModel gameSettingsViewModel = aiSettingsViewModel.getGameSettingsViewModel();
        rows = gameSettingsViewModel.getGameViewModel().getJigsaw().getRows();
        cols = gameSettingsViewModel.getGameViewModel().getJigsaw().getCols();

        textBoardInfo.setText(String.format(Locale.US, "Board Size: %d rows by %d cols", rows, cols));

        setupObserversAndListeners();

        simulateGamesButton.setOnClickListener(this::simulateGames);
    }

    private void setupObserversAndListeners() {
        // Iterations per move
        aiSettingsViewModel.getIterationsPerMove().observe(getViewLifecycleOwner(), iterations -> {
            if (iterations != null) {
                iterationsSlider.setValue(iterations);
                iterationsValueText.setText(String.format(Locale.US, "%,d iterations", iterations));
            }
        });
        iterationsSlider.addOnChangeListener((slider, value, fromUser) -> {
            iterationsValueText.setText(String.format(Locale.US, "%,d iterations", (int) value));
            if (fromUser) {
                aiSettingsViewModel.setIterationsPerMove((int) value);
            }
        });

        // Simulation Exploration Rate
        aiSettingsViewModel.getSimulationExplorationFactor().observe(getViewLifecycleOwner(), simulationExplorationRate -> {
            if (simulationExplorationRate != null) {
                simulationExplorationRateSlider.setValue(simulationExplorationRate);
                simulationExplorationRateText.setText(String.format(Locale.US, "%.1f", simulationExplorationRate));
            }
        });
        simulationExplorationRateSlider.addOnChangeListener((slider, value, fromUser) -> {
            simulationExplorationRateText.setText(String.format(Locale.US, "%.1f",value));
            if (fromUser) {
                aiSettingsViewModel.setSimulationExplorationFactor(value);
            }
        });

        // Exploration factor
        aiSettingsViewModel.getExplorationFactor().observe(getViewLifecycleOwner(), factor -> {
            if (factor != null) {
                explorationSlider.setValue(factor);
                explorationValueText.setText(String.format(Locale.US, "%.1f", factor));
            }
        });
        explorationSlider.addOnChangeListener((slider, value, fromUser) -> {
            explorationValueText.setText(String.format(Locale.US, "%.1f", value));
            if (fromUser) {
                aiSettingsViewModel.setExplorationFactor(value);
            }
        });

        // Depth coefficient
        aiSettingsViewModel.getDepthCoefficient().observe(getViewLifecycleOwner(), coef -> {
            if (coef != null) {
                depthCoefSlider.setValue(coef);
                depthCoefValueText.setText(String.format(Locale.US, "%.1f", coef));
            }
        });
        depthCoefSlider.addOnChangeListener((slider, value, fromUser) -> {
            depthCoefValueText.setText(String.format(Locale.US, "%.1f", value));
            if (fromUser) {
                aiSettingsViewModel.setDepthCoefficient(value);
            }
        });

        // Games to simulate
        aiSettingsViewModel.getGamesToSimulate().observe(getViewLifecycleOwner(), games -> {
            if (games != null) {
                gamesSlider.setValue(games);
                gamesValueText.setText(String.format(Locale.US, "%,d games", games));
            }
        });
        gamesSlider.addOnChangeListener((slider, value, fromUser) -> {
            gamesValueText.setText(String.format(Locale.US, "%,d games", (int) value));
            if (fromUser) {
                aiSettingsViewModel.setGamesToSimulate((int) value);
            }
        });
    }
    public float gameQualityScore(int totalMoves, int totalCells) {
        float theoreticalMinMoves = totalCells / 3.0f; //it can be over this

        float efficiency = theoreticalMinMoves / totalMoves;

        float score = efficiency * 100;

        return score;
    }
    private void simulateGames(View view) {
        scoreTextView.setVisibility(View.INVISIBLE);
        simulateGamesButton.setEnabled(false); // Disable button during simulation

        // Show and initialize progress UI
        requireActivity().runOnUiThread(() -> {
            simulationProgressBar.setVisibility(View.VISIBLE);
            simulationProgressBar.setMax(100);
            simulationProgressBar.setProgress(0);
            progressTextView.setVisibility(View.VISIBLE);
            progressTextView.setText("0/" + aiSettingsViewModel.getGamesToSimulate().getValue() + " games completed");
        });

        Thread simulationThread = new Thread(() -> {
            int games = aiSettingsViewModel.getGamesToSimulate().getValue();
            int movesPerformed;
            byte bestAction;
            float sumScore = 0;
            List<SimulationResult> results = new ArrayList<>();
            float maxScore = Float.MIN_VALUE;

            //values for tree
            int maxIters = aiSettingsViewModel.getIterationsPerMove().getValue();
            int maxDepth = (int) (aiSettingsViewModel.getDepthCoefficient().getValue() * Math.sqrt(rows * cols));
            float c = aiSettingsViewModel.getExplorationFactor().getValue();
            float explorationRateSimulation = aiSettingsViewModel.getSimulationExplorationFactor().getValue();

            for (int i = 0; i < games; i++) {

                long startGameTime = System.nanoTime();

                final int currentProgress = i + 1;
                requireActivity().runOnUiThread(() -> {
                    int progressPercent = (currentProgress * 100) / games;
                    simulationProgressBar.setProgress(progressPercent);
                    progressTextView.setText(currentProgress + "/" + games + " games completed");
                });

                Jigsaw jigsaw = new Jigsaw(rows, cols);
                Tree tree = new Tree(jigsaw, new Config(maxIters, maxDepth, c, explorationRateSimulation),
                        StopType.ITERATIONS);

                movesPerformed = 0;

                while (!jigsaw.hasFinished()) {
                    bestAction = tree.findBestAction();
                    //System.out.println(jigsaw);
                    //System.out.println("Best Action: " + bestAction);
                    jigsaw.performAction(bestAction);
                   //System.out.println("After Placement");
                    //System.out.println(jigsaw);
                    tree = new Tree(jigsaw, new Config(maxIters, maxDepth, c, explorationRateSimulation),
                            StopType.ITERATIONS);
                    movesPerformed++;
                }

                float score = gameQualityScore(movesPerformed, rows * cols);
                sumScore += score;
                results.add(new SimulationResult(i, movesPerformed, score));

                if (score > maxScore) {
                    maxScore = score;
                }

                long endGameTime = System.nanoTime();
                long elapsedGameTimeMs = (endGameTime - startGameTime) / 1_000_000; // Convert to milliseconds

                System.out.println("Game " + i + " took " + elapsedGameTimeMs + " ms");
            }

            // Final UI update
            float finalSumScore = sumScore;
            float finalMaxScore = maxScore;
            requireActivity().runOnUiThread(() -> {
                scoreTextView.setText(String.format(Locale.US, "Average Score: %.2f (Max: %.2f) (Total: %.2f)",
                        finalSumScore / games, finalMaxScore, finalSumScore));
                scoreTextView.setVisibility(View.VISIBLE);
                simulationProgressBar.setVisibility(View.GONE);
                progressTextView.setVisibility(View.GONE);
                simulateGamesButton.setEnabled(true);

                // Save results to file

                SimulationData simulationData = new SimulationData(
                        maxIters,
                        maxDepth,
                        c,
                        explorationRateSimulation,
                        results
                );

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(simulationData);

                // Save to file
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());

                String fileName = String.format(
                        Locale.US,
                        "simulation_result_%dx%d_iters-%d_depth-%d_c-%.2f_explorationRateSimulation-%.2f_%s.json",
                        rows,
                        cols,
                        maxIters,
                        maxDepth,
                        c,
                        explorationRateSimulation,
                        timeStamp
                );

                File dir = requireContext().getExternalFilesDir(null);
                File file = new File(dir, fileName);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                    Log.d("Simulation", "Results saved to: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Simulation", "Failed to save simulation results", e);
                }
            });
        });

        simulationThread.start();
    }

}