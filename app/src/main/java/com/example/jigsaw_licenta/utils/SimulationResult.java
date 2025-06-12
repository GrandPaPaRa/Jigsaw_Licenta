package com.example.jigsaw_licenta.utils;

public class SimulationResult {
    public int gameIndex;
    public int movesPerformed;
    public float score;

    public SimulationResult(int gameIndex, int movesPerformed, float score) {
        this.gameIndex = gameIndex;
        this.movesPerformed = movesPerformed;
        this.score = score;
    }
}