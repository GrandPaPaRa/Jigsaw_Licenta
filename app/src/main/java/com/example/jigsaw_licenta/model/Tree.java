package com.example.jigsaw_licenta.model;

import com.example.jigsaw_licenta.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Tree {
    private final Config config;
    private final List<Node> nodes;
    private final Jigsaw rootState;
    private final int rootIndex;

    public Tree(Jigsaw initialState, Config config) {
        this.config = config;
        this.nodes = new ArrayList<>();
        this.rootState = initialState.cloneState();
        this.rootIndex = createNode();
    }

    public byte findBestAction() {
        for (int iter = 0; iter < config.maxIters; iter++) {
            if(iter % 1000 == 0) System.out.println("Iteration: " + iter);

            Jigsaw state = rootState.cloneState();
            int nodeIndex = rootIndex;
            int depth = 0;

            // Selection phase
            while (!state.hasFinished() && depth < config.maxDepth) {
                Node node = nodes.get(nodeIndex);
                List<Byte> legalActions = state.legalActions();

                if (node.children.size() < legalActions.size()) {
                    break; // Not fully expanded
                }

                byte bestAction = selectBestAction(nodeIndex, state);
                state.performAction(bestAction);
                nodeIndex = node.children.get(bestAction);
                depth++;
            }

            // Expansion
            if (!state.hasFinished() && depth < config.maxDepth) {
                Node node = nodes.get(nodeIndex);
                List<Byte> legalActions = state.legalActions();

                for (byte action : legalActions) {
                    if (!node.children.containsKey(action)) {
                        int childIndex = createNode();
                        nodes.get(childIndex).setParent(nodeIndex);
                        node.setChild(action, childIndex);

                        // Simulation
                        int score = simulate(state.cloneState(), config.maxDepth - depth);

                        // Backpropagation
                        backpropagate(score, 1, childIndex);
                        break;
                    }
                }
            } else {
                // Terminal state or max depth reached
                int score = state.eval();
                backpropagate(score, 1, nodeIndex);
            }
        }

        return getMostVisitedAction(rootIndex);
    }

    private byte selectBestAction(int nodeIndex, Jigsaw state) {
        Node node = nodes.get(nodeIndex);
        byte bestAction = -1;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Byte, Integer> entry : node.children.entrySet()) {
            Node child = nodes.get(entry.getValue());
            float value = child.ucb(config.c, node.visits);
            if (value > bestValue) {
                bestValue = value;
                bestAction = entry.getKey();
            }
        }

        return bestAction;
    }

    private int simulate(Jigsaw state, int maxDepth) {
        int depth = 0;
        Random random = new Random();

        while (!state.hasFinished() && depth < maxDepth) {
            List<Byte> actions = state.legalActions();
            if (actions.isEmpty()) break;

            byte randomAction = actions.get(random.nextInt(actions.size()));
            state.performAction(randomAction);
            depth++;
        }

        return state.eval();
    }

    private void backpropagate(int winDelta, int visitDelta, int nodeIndex) {
        while (nodeIndex != -1) {
            Node node = nodes.get(nodeIndex);
            node.update(winDelta, visitDelta);
            nodeIndex = node.parent != null ? node.parent : -1;
        }
    }

    private byte getMostVisitedAction(int nodeIndex) {
        Node node = nodes.get(nodeIndex);
        byte bestAction = -1;
        int mostVisits = -1;

        for (Map.Entry<Byte, Integer> entry : node.children.entrySet()) {
            Node child = nodes.get(entry.getValue());
            if (child.visits > mostVisits) {
                mostVisits = child.visits;
                bestAction = entry.getKey();
            }
        }

        return bestAction;
    }

    private int createNode() {
        int index = nodes.size();
        nodes.add(new Node());
        return index;
    }
}