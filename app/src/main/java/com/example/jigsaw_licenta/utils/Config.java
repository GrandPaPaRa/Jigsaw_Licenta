package com.example.jigsaw_licenta.utils;

import java.util.List;

//Hyper params for the Monte Carlo Tree Search
public class Config {
    public int maxIters = 15000;
    public int maxDepth = 12;
    public float c = (float) Math.sqrt(2.0);
    public Config(int maxIters, int maxDepth, float c) {
        this.maxIters = maxIters;
        this.maxDepth = maxDepth;
        this.c = c;
    }
    public Config() {}
}