package com.example.jigsaw_licenta.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.Piece;
import com.example.jigsaw_licenta.model.PieceQueue;
import com.example.jigsaw_licenta.model.PieceType;

import java.util.ArrayList;

import java.util.List;


public class GameViewModel extends AndroidViewModel {
    protected String SAVE_PREFS_NAME = "JigsawPrefs";
    private static final String KEY_BOARD = "board";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_SEED = "seed";
    private static final String KEY_POSITION = "position";
    private static final String KEY_PIECES = "pieces";
    private static final String KEY_PIECE_TYPE = "type_";
    private static final String KEY_PIECE_PLACED = "placed_";
    private static final String KEY_PIECE_SPOT = "spot_";
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
    public void saveGameState() {
        SharedPreferences prefs = getApplication().getSharedPreferences(SAVE_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (jigsaw != null) {
            editor.putLong(KEY_BOARD, jigsaw.board);
            editor.putInt(KEY_QUANTITY, jigsaw.quantity);

            PieceQueue queue = jigsaw.getPieceQueue();
            editor.putLong(KEY_SEED, queue.getSeed());
            editor.putInt(KEY_POSITION, queue.getPosition());
        }
        // Save piece list
        editor.putInt(KEY_PIECES, pieceList.size());
        for (int i = 0; i < pieceList.size(); i++) {
            Piece piece = pieceList.get(i);
            editor.putString(KEY_PIECE_TYPE + i, piece.getType().name());
            editor.putBoolean(KEY_PIECE_PLACED + i, piece.isPlaced());
            editor.putInt(KEY_PIECE_SPOT + i, piece.getActionSpot());
        }
        editor.apply();
    }

    public void loadGameState(int rowsToUse, int colsToUse) {
        SharedPreferences prefs = getApplication().getSharedPreferences(SAVE_PREFS_NAME, Context.MODE_PRIVATE);

        int rows = rowsToUse;
        int cols = colsToUse;
        long board = prefs.getLong(KEY_BOARD, 0L);
        int quantity = prefs.getInt(KEY_QUANTITY, 0);
        long seed = prefs.getLong(KEY_SEED, System.currentTimeMillis());
        int position = prefs.getInt(KEY_POSITION, 0);

        // Recreate the game state
        PieceQueue queue = new PieceQueue(seed, Jigsaw.QUEUE_CAPACITY);
        queue.setPosition(position);

        jigsaw = new Jigsaw(rows, cols, queue);
        jigsaw.board = board;
        jigsaw.quantity = quantity;

        pieceList.clear();
        int pieceCount = prefs.getInt(KEY_PIECES, 0);
        for (int i = 0; i < pieceCount; i++) {
            PieceType type = PieceType.valueOf(prefs.getString(KEY_PIECE_TYPE + i, PieceType.DOT.name()));
            boolean isPlaced = prefs.getBoolean(KEY_PIECE_PLACED + i, false);
            int actionSpot = prefs.getInt(KEY_PIECE_SPOT + i, -1);

            // Create new piece (ImageView will be recreated in renderPieces())
            Piece piece = new Piece(type, null, isPlaced);
            piece.setActionSpot(actionSpot);
            pieceList.add(piece);
        }

    }

    private static void clearSavedGame(Application application, String savePrefsName) {
        SharedPreferences prefs = application.getSharedPreferences(savePrefsName, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    public static void clearSavedGames(Application application) {
        clearSavedGame(application, "JigsawPrefs");
        clearSavedGame(application, "JigsawPrefs_TimeTrial");
        GameTimeTrialViewModel.resetTimeTrialTimer(application);
    }
    public LiveData<Long> getTimeRemaining(){
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
