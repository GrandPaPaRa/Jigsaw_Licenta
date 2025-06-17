package com.example.jigsaw_licenta.utils;

import java.util.List;

public class SimulationData {
    public int maxIters;
    public int maxDepth;
    public float c;
    public float explorationRateSimulation;
    public List<SimulationResult> results;

    public SimulationData(int maxIters, int maxDepth, float c, float explorationRateSimulation, List<SimulationResult> results) {
        this.maxIters = maxIters;
        this.maxDepth = maxDepth;
        this.c = c;
        this.explorationRateSimulation = explorationRateSimulation;
        this.results = results;
    }
}