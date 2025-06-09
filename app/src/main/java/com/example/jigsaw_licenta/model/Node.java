package com.example.jigsaw_licenta.model;

import java.util.HashMap;
import java.util.Map;

public class Node {
    public int visits;
    public int wins;
    public Integer parent; // null if root
    public Map<Byte, Integer> children;

    public Node() {
        this.visits = 0;
        this.wins = 0;
        this.parent = null;
        this.children = new HashMap<>();
    }

    public void setParent(int parentIndex) {
        this.parent = parentIndex;
    }

    public void setChild(byte action, int childIndex) { // Changed from A to byte
        children.put(action, childIndex);
    }

    public void update(int winDelta, int visitDelta) {
        this.wins += winDelta;
        this.visits += visitDelta;
    }

    public float ucb(float c, int parentVisits) {
        if (visits == 0) return Float.MAX_VALUE;
        float exploitation = (float) wins / visits;
        float exploration = c * (float) Math.sqrt(Math.log(parentVisits) / visits);
        return exploitation + exploration;
    }
}