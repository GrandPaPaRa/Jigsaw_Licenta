package com.example.jigsaw_licenta.utils;

import java.util.List;

//Hyper params for the Monte Carlo Tree Search
public class Config {
    public int maxIters = 30000;
    public int maxDepth = 10;
    public float c = (float) Math.sqrt(2.0);
    public int maxMillis = 5000;
    public Config(int maxIters, int maxDepth, float c, int maxMillis) {
        this.maxIters = maxIters;
        this.maxDepth = maxDepth;
        this.c = c;
        this.maxMillis = maxMillis;
    }
    public Config() {}
}