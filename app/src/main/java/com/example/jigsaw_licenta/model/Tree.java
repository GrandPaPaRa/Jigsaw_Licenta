package com.example.jigsaw_licenta.model;

import com.example.jigsaw_licenta.utils.Config;
import com.example.jigsaw_licenta.utils.Environment;
import com.example.jigsaw_licenta.utils.Stats;

import java.util.*;

public class Tree<T extends Environment<T, A>, A> {
    private List<Node<A>> nodes;
    private T rootState;
    private int rootIndex;
    private Config config;

    public Tree(T rootState, Config config) {
        this.rootState = rootState;
        this.config = config;
        this.nodes = new ArrayList<>(config.maxIters);
        this.rootIndex = 0;
        nodes.add(new Node<>());
    }

    public void compute(Callback<T, A> callback) {
        for (int iter = 1; iter <= config.maxIters; iter++) {
            if (iter % config.callbackInterval == 0 || iter == config.maxIters) {
                List<Stats.Pair<A, Integer>> actions = new ArrayList<>();
                for (Map.Entry<A, Integer> entry : nodes.get(rootIndex).children.entrySet()) {
                    actions.add(new Stats.Pair<>(entry.getKey(), nodes.get(entry.getValue()).visits));
                }

                final int finalIter = iter;
                final List<Stats.Pair<A, Integer>> finalActions = actions;

                callback.call(new Stats<A>() {{
                    iters = finalIter;
                    this.actions = finalActions;
                }});
            }

            T state = rootState.cloneState();
            int stateDepth = 0;
            int index = select(rootIndex, config.c, state, stateDepth);

            if (state.hasFinished() || stateDepth > config.maxDepth) {
                backpropagate(state.eval(), 1, index);
                continue;
            }

            index = expand(index, state);
            int rolloutDepth = config.maxDepth - stateDepth;
            int score = simulate(state, rolloutDepth);
            backpropagate(score, 1, index);
        }
    }

    private int createNode() {
        nodes.add(new Node<>());
        return nodes.size() - 1;
    }

    private int select(int index, float c, T state, int depth) {
        Node<A> node = nodes.get(index);
        if (state.hasFinished()) return index;

        List<A> legalActions = state.legalActions();

        boolean fullyExpanded = legalActions.stream().allMatch(a -> node.children.containsKey(a));
        if (!fullyExpanded) return index;

        A bestAction = legalActions.stream().max(Comparator.comparingDouble(a -> {
            int childIndex = node.children.get(a);
            return nodes.get(childIndex).ucb(c, node.visits);
        })).orElse(null);

        if (bestAction == null) return index;

        state.performAction(bestAction);
        return select(node.children.get(bestAction), c, state, depth + 1);
    }

    private int expand(int index, T state) {
        List<A> legalActions = state.legalActions();
        Node<A> node = nodes.get(index);

        for (A action : legalActions) {
            if (!node.children.containsKey(action)) {
                int newIndex = createNode();
                nodes.get(newIndex).setParent(index);
                node.setChild(action, newIndex);
                return newIndex;
            }
        }
        throw new RuntimeException("No expandable action found");
    }

    private int simulate(T state, int maxDepth) {
        int depth = 0;
        Random random = new Random();

        while (!state.hasFinished() && depth < maxDepth) {
            List<A> legalActions = state.legalActions();
            A randomAction = legalActions.get(random.nextInt(legalActions.size()));
            state.performAction(randomAction);
            depth++;
        }
        return state.eval();
    }

    private void backpropagate(int score, int visits, int index) {
        Integer cursor = index;
        while (cursor != null) {
            Node<A> node = nodes.get(cursor);
            node.update(score, visits);
            cursor = node.parent;
        }
    }

    public interface Callback<T, A> {
        void call(Stats<A> stats);
    }
}
