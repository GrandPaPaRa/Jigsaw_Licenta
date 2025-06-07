package com.example.jigsaw_licenta.viewmodel;

import static com.example.jigsaw_licenta.model.PieceType.getRandomPieceType;

import androidx.lifecycle.ViewModel;

import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.Piece;
import com.example.jigsaw_licenta.model.PieceType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class GameViewModel extends ViewModel {
    private static final int QUEUE_SIZE = 5;
    public boolean isHoldingPiece = false;
    private final Random random = new Random();
    private Jigsaw jigsaw;
    private final List<Piece> pieceList = new ArrayList<>();
    private Queue<PieceType> nextPieceQueue = new LinkedList<>();
    private float currentScale = 1.0f;
    public GameViewModel() {
        generateInitialPieceQueue();
    }
    private void generateInitialPieceQueue() {
        if(nextPieceQueue == null)
            nextPieceQueue = new LinkedList<>();
        else
            nextPieceQueue.clear();
        for (int i = 0; i < QUEUE_SIZE; i++) {
            nextPieceQueue.add(generateRandomPiece());
        }
    }
    private PieceType generateRandomPiece() {
        // Your logic to generate a random piece
        return PieceType.getRandomPieceType(); // For example
    }
    public void generateAndAddRandomPiece() {

        if (nextPieceQueue == null) return;

        if (!nextPieceQueue.isEmpty()) {
            nextPieceQueue.poll(); // remove used piece
        }
        nextPieceQueue.add(generateRandomPiece()); // add new one at the end

    }
    public PieceType getNextPiece() {
        if (nextPieceQueue == null || nextPieceQueue.isEmpty()) {
            return null;
        }
        return nextPieceQueue.peek();

    }    public void setJigsaw(Jigsaw jigsaw) {
        this.jigsaw = jigsaw;
    }

    public Jigsaw getJigsaw() {
        return jigsaw;
    }
    public Queue<PieceType> getNextPieceQueue() {
        return nextPieceQueue;
    }

    public void fillQueue(int size) {
        while (nextPieceQueue.size() < size) {
            nextPieceQueue.add(getRandomPieceType());
        }
    }

    public PieceType popNextPiece() {
        PieceType next = nextPieceQueue.poll();
        nextPieceQueue.add(getRandomPieceType()); // refill
        return next;
    }

    public void addPiece(Piece piece) {
        pieceList.add(piece);
    }

    public List<Piece> getPieceList() {
        return pieceList;
    }

    public void clearPieces() {
        pieceList.clear();
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float scale) {
        this.currentScale = scale;
    }
    public int getMoveCount() {
        return jigsaw != null ? jigsaw.quantity : 0;
    }
    public void removePiece(Piece piece) {
        pieceList.remove(piece);
    }
}
