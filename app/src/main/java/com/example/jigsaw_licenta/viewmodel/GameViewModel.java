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
    public boolean isHoldingPiece = false;
    private Jigsaw jigsaw;
    private final List<Piece> pieceList = new ArrayList<>();
    private float currentScale = 1.0f;
    public GameViewModel() {
    }
    public Jigsaw getJigsaw() {
        return jigsaw;
    }
    public void setJigsaw(Jigsaw jigsaw) {
        this.jigsaw = jigsaw;
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
    public void resetGame(int rows, int cols) {

        jigsaw = new Jigsaw(rows, cols);
        clearPieces();
    }
}
