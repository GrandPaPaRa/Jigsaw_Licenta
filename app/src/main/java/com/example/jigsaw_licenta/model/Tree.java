package com.example.jigsaw_licenta.model;

import com.example.jigsaw_licenta.utils.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Tree {
    private final Config config;
    private final List<Node> nodes;
    private Jigsaw rootState;
    private final int rootIndex;
    private volatile boolean shouldCancel = false;
    StopType stopType;
    public void cancel() {
        shouldCancel = true;
    }
    public boolean getShouldCancel() {
        return shouldCancel;
    }
    public Tree(Jigsaw initialState, Config config, StopType stopType) {
        this.config = config;
        this.nodes = new ArrayList<>();
        this.rootState = initialState.cloneState();
        this.rootIndex = createNode();
        this.stopType = stopType;
    }
    public void setJigsaw(Jigsaw jigsaw) {
        this.rootState = jigsaw.cloneState();
    }
    public byte findBestAction(){
        byte result = stopType == StopType.TIME ? findBestActionbyTime() : findBestActionbyIterations();

//        if (rootState.getConsecutiveSkips() >= config.maxDepth / 2 && result == rootState.getSkipActionValue()) {
//            // Force a placement action if any available
//            List<Byte> actions = rootState.legalActions();
//            for (byte action : actions) {
//                if (action != rootState.getSkipActionValue()) {
//                    return action; // Return first legal placement instead of another skip
//                }
//            }
//        }

        return result;
    }
    public byte findBestActionbyIterations(){
        for (int iter = 0; iter < config.maxIters; iter++) {

            //CANCELLATION FROM OUTSIDE THREAD(HINT OVERLAPPING)
            if (shouldCancel) {
                System.out.println("MCTS cancelled.");
                return -1;
            }

            if(iter % 1000 == 0) System.out.println("Iteration: " + iter);

            Jigsaw state = rootState.cloneState();
            int nodeIndex = rootIndex;
            int depth = 0;

            Node node = nodes.get(nodeIndex);
            List<Byte> legalActions = new ArrayList<>();

            // Selection phase
            while (!state.hasFinished() && depth < config.maxDepth) {
                node = nodes.get(nodeIndex);
                legalActions = state.legalActions();

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
                //Node node = nodes.get(nodeIndex);
                //List<Byte> legalActions = state.legalActions();
                //Collections.shuffle(legalActions);
                for (byte action : legalActions) {
                    if (!node.children.containsKey(action)) {
                        int childIndex = createNode();
                        nodes.get(childIndex).setParent(nodeIndex);
                        node.setChild(action, childIndex);

                        // Simulation
                        Jigsaw rolloutState = state.cloneState();
                        rolloutState.performAction(action);
                        int score = simulate(rolloutState, config.maxDepth - depth - 1);

                        //int score = simulate(state.cloneState(), config.maxDepth - depth);

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

        return getMostVisitedAction(rootIndex,config.maxIters);
    }

    public byte findBestActionbyTime() {
        long startTime = System.nanoTime();
        int iterations = 0;
        while(true) {

            //CANCELLATION FROM OUTSIDE THREAD(HINT OVERLAPPING)
            if (shouldCancel) {
                System.out.println("MCTS cancelled.");
                return -1;
            }

            //time limit
            long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
            if (elapsedMillis > config.maxMillis) {
                System.out.println("MCTS time limit reached.");
                break;
            }
            //if(elapsedMillis % 1000 == 0) System.out.println("Elapsed time: " + elapsedMillis);

            Jigsaw state = rootState.cloneState();
            int nodeIndex = rootIndex;
            int depth = 0;

            Node node = nodes.get(nodeIndex);
            List<Byte> legalActions = new ArrayList<>();

            // Selection phase
            while (!state.hasFinished() && depth < config.maxDepth) {
                node = nodes.get(nodeIndex);
                legalActions = state.legalActions();

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
                for (byte action : legalActions) {
                    if (!node.children.containsKey(action)) {
                        int childIndex = createNode();
                        nodes.get(childIndex).setParent(nodeIndex);
                        node.setChild(action, childIndex);

                        // Simulation
                        Jigsaw rolloutState = state.cloneState();
                        rolloutState.performAction(action);
                        int score = simulate(rolloutState, config.maxDepth - depth - 1);
                        //int score = simulate(state.cloneState(), config.maxDepth - depth);

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
            iterations++;
        }

        return getMostVisitedAction(rootIndex,iterations);
    }

    private byte selectBestAction(int nodeIndex, Jigsaw state) {
        Node node = nodes.get(nodeIndex);
        byte bestAction = -1;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Byte, Integer> entry : node.children.entrySet()) {
            Node child = nodes.get(entry.getValue());
            float value = child.ucb(config.c, node.visits);
            if (value > bestValue || (value == bestValue && entry.getKey() > bestAction)) {
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

            byte chosenAction;
            if (random.nextFloat() < config.explorationRateSimulation) {
                chosenAction = actions.get(random.nextInt(actions.size()));
            } else {

                int bestScore = Integer.MIN_VALUE;
                byte bestAction = actions.get(0); // fallback

                for (byte action : actions) {
                    Jigsaw copy = state.cloneState();
                    copy.performAction(action);
                    int score = copy.eval();

                    if (score > bestScore) {
                        bestScore = score;
                        bestAction = action;
                    }
                }

                chosenAction = bestAction;
            }

            state.performAction(chosenAction);
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

    private byte getMostVisitedAction(int nodeIndex, int iterations) {
        Node node = nodes.get(nodeIndex);
        byte bestAction = -1;
        int mostVisits = -1;



        // Sort entries by action (ascending)
        List<Map.Entry<Byte, Integer>> sortedEntries = new ArrayList<>(node.children.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        int tolerance = iterations % sortedEntries.size();

        System.out.println("Action visit counts with tolerance "+tolerance+" :");
        for (Map.Entry<Byte, Integer> entry : sortedEntries) {
            Node child = nodes.get(entry.getValue());
            byte action = entry.getKey();
            int visits = child.visits;

            System.out.println("Action " + action + ": " + visits + " visits");

            if (visits > mostVisits + tolerance) {
                mostVisits = visits;
                bestAction = action;
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