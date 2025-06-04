package com.example.jigsaw_licenta.utils;
import java.util.List;
public interface Environment<T, A> {
    boolean hasFinished();
    void performAction(A action);
    List<A> legalActions();
    int eval();
    T cloneState(); // Deep clone for tree branching
}