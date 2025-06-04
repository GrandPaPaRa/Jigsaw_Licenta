package com.example.jigsaw_licenta.utils;
import java.util.List;

public class Stats<A> {
    public int iters;
    public List<Pair<A, Integer>> actions;

    public A bestAction() {
        return actions.stream()
                .max((a, b) -> Integer.compare(a.second, b.second))
                .map(pair -> pair.first)
                .orElse(null);
    }

    public static class Pair<F, S> {
        public F first;
        public S second;
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}