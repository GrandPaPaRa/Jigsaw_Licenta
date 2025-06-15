package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.Piece;

import java.util.ArrayList;

import java.util.List;


public class GameViewModel extends AndroidViewModel {
    public boolean isHoldingPiece = false;
    protected Jigsaw jigsaw;
    protected final List<Piece> pieceList = new ArrayList<>();
    protected float currentScale = 1.0f;
    public GameViewModel(Application application) {
        super(application);
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
    public LiveData<Long> getTimeRemaining() {
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }

    public LiveData<Boolean> getTimeExpired() {
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }
    public void setTimeExpired(boolean expired){
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }
    public void startTimer(long durationMillis){
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }
    public void resetTimer(){
        throw new UnsupportedOperationException(
                "Time trial duration not supported in base GameSettingsViewModel");
    }
}
