package com.example.jigsaw_licenta.ui.main;

import static com.example.jigsaw_licenta.model.Jigsaw.MAX_ITERATIONS;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.Piece;
import com.example.jigsaw_licenta.model.PieceType;
import com.example.jigsaw_licenta.model.StopType;
import com.example.jigsaw_licenta.model.Tree;
import com.example.jigsaw_licenta.utils.Config;
import com.example.jigsaw_licenta.utils.GameInterface;
import com.example.jigsaw_licenta.viewmodel.AiSettingsModelFactory;
import com.example.jigsaw_licenta.viewmodel.AiSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.example.jigsaw_licenta.viewmodel.GameSettingsModelFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGameFragment extends Fragment {

    protected GameSettingsViewModel gameSettingsViewModel;
    protected GameViewModel gameViewModel;
    protected AiSettingsViewModel aiSettingsViewModel;
    protected GridLayout boardContainer;
    protected ImageView ghostView;
    protected ImageView hintGhostView;
    protected ImageView nextPiecePreview;
    protected List<PieceType> upcomingPieceList = new ArrayList<>();
    protected ImageView hintButton;
    protected TextView movesTextView;
    protected Button resetGameButton;
    protected ImageView discardZone;
    protected ConstraintLayout gameContainer;
    LinearLayout previewContainer;
    protected float currentScale = 1.0f;
    protected List<Piece> gamePieces = new ArrayList<>();
    protected Jigsaw jigsawGame;
    protected int baseTileSize;
    protected float snapThreshold;
    protected Tree currentTree = null;
    protected Byte cachedHintAction = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseTileSize = ContextCompat.getDrawable(requireContext(), R.drawable.board_tile).getIntrinsicHeight();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), container, false);
        initializeViews(view);
        return view;
    }

    protected abstract int getLayoutResource();


    protected abstract void initializeViews(View view);


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!gameViewModel.isHoldingPiece) {
            nextPiecePreview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    nextPiecePreview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    generateAndAddRandomPiece();
                }
            });
        }

        initializeGameUI();

        upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(gameSettingsViewModel.getPreviewQueueSize().getValue() + 1));
        upcomingPieceList.remove(0);
        updateUpcomingPiecePreviewUI();

        startHintComputation(jigsawGame);

        resetGameButton.setOnClickListener(v -> onResetButtonClick());

        hintButton.setOnClickListener(v -> {
            if (cachedHintAction != null && hintButton.isEnabled()) {
                showHint(cachedHintAction);
                hintButton.setEnabled(false);
            }
        });
    }


    protected void initializeGameUI() {
        ghostView = null;
        hintGhostView = null;

        Integer savedRows = gameSettingsViewModel.getBoardRows().getValue();
        Integer savedCols = gameSettingsViewModel.getBoardCols().getValue();

        int rowsToUse = (savedRows != null && savedRows > 0) ? savedRows : Jigsaw.DEFAULT_ROWS;
        int colsToUse = (savedCols != null && savedCols > 0) ? savedCols : Jigsaw.DEFAULT_COLS;

        if (gameViewModel.getJigsaw() == null || rowsToUse != gameViewModel.getJigsaw().getRows()
                || colsToUse != gameViewModel.getJigsaw().getCols()) {
            jigsawGame = new Jigsaw(rowsToUse, colsToUse);
            gameViewModel.setJigsaw(jigsawGame);
        } else {
            jigsawGame = gameViewModel.getJigsaw();
        }

        Float initialScale = gameSettingsViewModel.getImageScale().getValue();
        if (initialScale != null) {
            currentScale = initialScale;
        }

        gamePieces.clear();
        gamePieces.addAll(gameViewModel.getPieceList());

        snapThreshold = baseTileSize * currentScale;
        gameViewModel.setCurrentScale(currentScale);

        renderBoard(jigsawGame.getRows(), jigsawGame.getCols(), currentScale);

        boardContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boardContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                renderPieces();
            }
        });
    }

    protected void renderBoard(int rows, int cols, float scale) {
        if (getContext() == null) return;

        boardContainer.removeAllViews();
        boardContainer.setRowCount(rows);
        boardContainer.setColumnCount(cols);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.board_tile);
        int baseSizePx = drawable.getIntrinsicHeight();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ImageView tileView = new ImageView(requireContext());

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (int) (baseSizePx * scale);
                params.height = (int) (baseSizePx * scale);
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);

                tileView.setLayoutParams(params);
                tileView.setImageDrawable(drawable);
                tileView.setScaleType(ImageView.ScaleType.FIT_XY);

                boardContainer.addView(tileView);
            }
        }
        Log.d("BaseGameFragment", "Board rendered with rows=" + rows + ", cols=" + cols + ", scale=" + scale);
    }

    protected void renderPieces() {
        if (gameContainer == null || getContext() == null) return;

        for (Piece piece : gamePieces) {
            if (piece.getImageView() != null && piece.getImageView().getParent() != null) {
                gameContainer.removeView(piece.getImageView());
            }
        }

        for (Piece piece : gamePieces) {
            ImageView pieceView = piece.getImageView();
            if (pieceView == null) {
                pieceView = new ImageView(getContext());
                piece.setImageView(pieceView);
            }

            int pieceDrawableResId = getDrawableResourceForPieceType(piece.getType());
            Drawable pieceDrawable = ContextCompat.getDrawable(requireContext(), pieceDrawableResId);
            if (pieceDrawable == null) continue;

            pieceView.setImageResource(pieceDrawableResId);
            pieceView.setAdjustViewBounds(true);
            pieceView.setScaleType(ImageView.ScaleType.FIT_XY);

            int scaledWidth = (int) (pieceDrawable.getIntrinsicWidth() * currentScale);
            int scaledHeight = (int) (pieceDrawable.getIntrinsicHeight() * currentScale);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    scaledWidth,
                    scaledHeight
            );
            pieceView.setLayoutParams(params);

            if (piece.isPlaced()) {
                snapViewToActionIndex(piece.getActionSpot(), pieceView);
            } else {
                int[] previewLocation = new int[2];
                nextPiecePreview.getLocationOnScreen(previewLocation);

                int centerX = previewLocation[0] - (int)(pieceDrawable.getIntrinsicWidth() * currentScale / 2);
                int centerY = previewLocation[1] - (int)(pieceDrawable.getIntrinsicHeight() * currentScale / 2);

                pieceView.setX(centerX);
                pieceView.setY(centerY);
                setDraggableTouchListener(pieceView, centerX, centerY);
            }

            if (piece.getImageView() != null) {
                ViewGroup parent = (ViewGroup) piece.getImageView().getParent();
                if (parent != null) {
                    parent.removeView(piece.getImageView());
                }
            }
            gameContainer.addView(pieceView);
        }

        gameContainer.requestLayout();
    }

    protected void generateAndAddRandomPiece() {
        PieceType nextType = gameViewModel.getJigsaw().getCurrentPieceType();
        if (nextType == null) return;

        ImageView newPieceView = new ImageView(requireContext());
        int pieceDrawableResId = getDrawableResourceForPieceType(nextType);

        newPieceView.setImageResource(pieceDrawableResId);
        newPieceView.setAdjustViewBounds(true);
        newPieceView.setScaleType(ImageView.ScaleType.FIT_XY);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), pieceDrawableResId);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                (int) (drawable.getIntrinsicWidth() * currentScale),
                (int) (drawable.getIntrinsicHeight() * currentScale)
        );

        newPieceView.setLayoutParams(params);

        int[] previewLocation = new int[2];
        nextPiecePreview.getLocationOnScreen(previewLocation);

        int centerX = previewLocation[0] - (int)(drawable.getIntrinsicWidth() * currentScale / 2);
        int centerY = previewLocation[1] - (int)(drawable.getIntrinsicHeight() * currentScale / 2);

        newPieceView.setX(centerX);
        newPieceView.setY(centerY);

        setDraggableTouchListener(newPieceView, centerX, centerY);

        Piece newGamePiece = new Piece(nextType, newPieceView);
        gameContainer.addView(newPieceView);
        gamePieces.add(newGamePiece);
        gameViewModel.addPiece(newGamePiece);

        gameContainer.requestLayout();
        gameViewModel.isHoldingPiece = true;
    }

    protected int getDrawableResourceForPieceType(PieceType type) {
        switch (type) {
            case DOT:
                return R.drawable.dot;
            case LINE:
                return R.drawable.line;
            case LED:
                return R.drawable.led;
            case ZED:
                return R.drawable.zed;
            case NED:
                return R.drawable.ned;
            case SQUARE:
                return R.drawable.square;
            default:
                return R.drawable.ic_launcher_background;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setDraggableTouchListener(View pieceView, int originalX, int originalY) {
        pieceView.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;
                Piece matchedPiece = getPieceFromView(pieceView);

                if(matchedPiece != null && matchedPiece.isPlaced())
                    return true;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        v.performClick();
                        v.bringToFront();

                        jigsawGame.setFigureIndex(matchedPiece.getType().ordinal());
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float clampedX = Math.max(0, Math.min(newX, gameContainer.getWidth() - v.getWidth()));
                        float clampedY = Math.max(0, Math.min(newY, gameContainer.getHeight() - v.getHeight()));

                        v.setX(clampedX);
                        v.setY(clampedY);

                        Integer validIndex = getValidSnapIndex(v);
                        Log.d("BaseGameFragment_Touch", "ACTION_MOVE - validIndex: " + validIndex);
                        if (validIndex != null) {
                            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
                            if (ghostView == null) {
                                ghostView = new ImageView(requireContext());
                                ghostView.setAdjustViewBounds(true);
                                ghostView.setScaleType(ImageView.ScaleType.FIT_XY);
                                ghostView.setAlpha(0.2f);
                                ghostView.setEnabled(false);
                                ghostView.setClickable(false);
                            }

                            if (ghostView.getParent() == null) {
                                gameContainer.addView(ghostView);
                            }

                            if (v instanceof ImageView) {
                                ImageView original = (ImageView) v;
                                Drawable originalDrawable = original.getDrawable();
                                ghostView.setImageDrawable(originalDrawable);

                                int width = v.getWidth();
                                int height = v.getHeight();

                                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
                                ghostView.setLayoutParams(params);
                            }

                            ghostView.setVisibility(View.VISIBLE);
                            snapViewToActionIndex(validIndex, ghostView);
                        } else {
                            if (ghostView != null) {
                                ghostView.setVisibility(View.GONE);
                            }
                            updateDiscardZoneBackground(v);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(isViewInDiscardZone(v)){
                            removePieceFromGame(v);
                            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
                            jigsawGame.performAction((byte)(jigsawGame.getRows() * jigsawGame.getCols()));
                            updateMovesCounter();

                            if (hintGhostView != null) {
                                hintGhostView.setVisibility(View.GONE);
                                gameContainer.removeView(hintGhostView);
                                hintGhostView = null;
                            }

                            upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(gameSettingsViewModel.getPreviewQueueSize().getValue() + 1));
                            upcomingPieceList.remove(0);
                            updateUpcomingPiecePreviewUI();

                            generateAndAddRandomPiece();
                            startHintComputation(jigsawGame);
                        }
                        else{
                            Integer snapIndex = getValidSnapIndex(v);
                            if (snapIndex != null){
                                snapViewToActionIndex(snapIndex, v);
                                ghostView.setVisibility(View.GONE);

                                matchedPiece = getPieceFromView(pieceView);
                                if(matchedPiece != null){
                                    matchedPiece.setPlaced(true);
                                    matchedPiece.setActionSpot(snapIndex);
                                }

                                jigsawGame.performAction(snapIndex.byteValue());
                                updateMovesCounter();

                                if (hintGhostView != null) {
                                    hintGhostView.setVisibility(View.GONE);
                                    gameContainer.removeView(hintGhostView);
                                    hintGhostView = null;
                                }

                                upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(gameSettingsViewModel.getPreviewQueueSize().getValue() + 1));
                                upcomingPieceList.remove(0);
                                updateUpcomingPiecePreviewUI();

                                if(jigsawGame.hasFinished()){
                                    onFinishedGame();

                                    return true;
                                }

                                generateAndAddRandomPiece();
                                startHintComputation(jigsawGame);
                            }else{
                                v.setX(originalX);
                                v.setY(originalY);
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
    protected abstract void onFinishedGame();
    protected abstract void onResetButtonClick();
    @Nullable
    protected Integer getValidSnapIndex(View pieceView) {
        Drawable tileDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.board_tile);
        if (tileDrawable == null) return null;

        int tileSize = Math.round(tileDrawable.getIntrinsicHeight() * currentScale);
        int rows = jigsawGame.getRows();
        int cols = jigsawGame.getCols();

        int[] boardPos = new int[2];
        boardContainer.getLocationOnScreen(boardPos);
        float boardX = boardPos[0] + tileSize / 2f;
        float boardY = boardPos[1] + tileSize / 2f;

        float[][] tileX = new float[rows][cols];
        float[][] tileY = new float[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tileX[r][c] = boardX + c * tileSize;
                tileY[r][c] = boardY + r * tileSize;
            }
        }

        float px = pieceView.getX();
        float py = pieceView.getY();

        float minDistance = Float.MAX_VALUE;
        int bestRow = -1;
        int bestCol = -1;

        Piece matchedPiece = getPieceFromView(pieceView);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float dx = tileX[r][c] - px;
                float dy = tileY[r][c] - py;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < minDistance && jigsawGame.isLegal((byte) (r * cols + c), matchedPiece.getType())) {
                    minDistance = dist;
                    bestRow = r;
                    bestCol = c;
                }
            }
        }

        int actionIndex = bestRow * cols + bestCol;

        if (matchedPiece == null) return null;

        if (minDistance <= snapThreshold * currentScale &&
                jigsawGame.isLegal((byte) actionIndex, matchedPiece.getType())) {
            return actionIndex;
        }

        return null;
    }

    protected void snapViewToActionIndex(int actionIndex, View pieceView) {
        Drawable tileDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.board_tile);
        if (tileDrawable == null) return;

        int tileSize = Math.round(tileDrawable.getIntrinsicHeight() * currentScale);
        int cols = jigsawGame.getCols();
        int row = actionIndex / cols;
        int col = actionIndex % cols;

        int[] boardPos = new int[2];
        boardContainer.getLocationOnScreen(boardPos);
        float boardX = boardPos[0];
        float boardY = boardPos[1];

        float targetX = boardX + col * tileSize;
        float targetY = boardY + row * tileSize;

        pieceView.setX(targetX);
        pieceView.setY(targetY);
    }

    protected Piece getPieceFromView(View pieceView) {
        for (Piece piece : gamePieces) {
            if (piece.getImageView() == pieceView) {
                return piece;
            }
        }
        return null;
    }

    protected boolean isViewInDiscardZone(View pieceView) {
        int[] pieceLoc = new int[2];
        int[] discardLoc = new int[2];

        pieceView.getLocationOnScreen(pieceLoc);
        discardZone.getLocationOnScreen(discardLoc);

        float pieceCenterX = pieceLoc[0] + pieceView.getWidth() / 2f;
        float pieceCenterY = pieceLoc[1] + pieceView.getHeight() / 2f;

        int discardLeft = discardLoc[0];
        int discardTop = discardLoc[1];
        int discardRight = discardLeft + discardZone.getWidth();
        int discardBottom = discardTop + discardZone.getHeight();

        boolean isInsideBounds = pieceCenterX >= discardLeft && pieceCenterX <= discardRight &&
                pieceCenterY >= discardTop && pieceCenterY <= discardBottom;

        float discardCenterX = discardLeft + discardZone.getWidth() / 2f;
        float discardCenterY = discardTop + discardZone.getHeight() / 2f;

        float dx = pieceCenterX - discardCenterX;
        float dy = pieceCenterY - discardCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return isInsideBounds || distance <= snapThreshold;
    }

    protected void removePieceFromGame(View pieceView) {
        Piece piece = getPieceFromView(pieceView);
        if (piece != null) {
            gameContainer.removeView(pieceView);
            gamePieces.remove(piece);
            gameViewModel.removePiece(piece);
        }
    }

    protected void resetGame() {
        onDestroyView();
        gamePieces.clear();
        gameViewModel.clearPieces();
        gameViewModel.resetGame(gameSettingsViewModel.getBoardRows().getValue(), gameSettingsViewModel.getBoardCols().getValue());
        jigsawGame = gameViewModel.getJigsaw();
        startHintComputation(jigsawGame);

        upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(gameSettingsViewModel.getPreviewQueueSize().getValue() + 1));
        upcomingPieceList.remove(0);
        updateUpcomingPiecePreviewUI();

        initializeGameUI();
        updateMovesCounter();
        generateAndAddRandomPiece();
    }

    protected void updateMovesCounter() {
        if (jigsawGame != null) {
            movesTextView.setText("Moves: " + jigsawGame.quantity);
        }
    }

    protected void updateDiscardZoneBackground(View pieceView) {
        if (isViewInDiscardZone(pieceView)) {
            discardZone.setBackgroundColor(Color.parseColor("#FFCCCC"));
        } else {
            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
        }
    }

    protected void showHint(int hintAction) {
        if (hintAction < 0 || hintAction > jigsawGame.getRows() * jigsawGame.getCols()) return;

        if (hintGhostView == null) {
            hintGhostView = new ImageView(requireContext());
            hintGhostView.setAdjustViewBounds(true);
            hintGhostView.setScaleType(ImageView.ScaleType.FIT_XY);
            hintGhostView.setAlpha(0.2f);
            hintGhostView.setEnabled(false);
            hintGhostView.setClickable(false);
        }

        ImageView previewView = findPreviewView();
        if (previewView == null) {
            throw new IllegalStateException("Preview view not found. Cannot display hint ghost.");
        }

        hintGhostView.setImageDrawable(previewView.getDrawable());

        int width = previewView.getWidth();
        int height = previewView.getHeight();
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
        hintGhostView.setLayoutParams(params);

        if (hintGhostView.getParent() == null) {
            gameContainer.addView(hintGhostView);
        }

        hintGhostView.setVisibility(View.VISIBLE);

        if (hintAction == jigsawGame.getRows() * jigsawGame.getCols()) {
            int zoneX = (int) discardZone.getX();
            int zoneY = (int) discardZone.getY();
            int zoneWidth = discardZone.getWidth();
            int zoneHeight = discardZone.getHeight();

            int ghostWidth = hintGhostView.getWidth();
            int ghostHeight = hintGhostView.getHeight();

            float ghostX = zoneX + (zoneWidth - ghostWidth) / 2f;
            float ghostY = zoneY + (zoneHeight - ghostHeight) / 2f;

            hintGhostView.setX(ghostX);
            hintGhostView.setY(ghostY);
        } else {
            snapViewToActionIndex(hintAction, hintGhostView);
        }

        AnimatorSet scaleSet = new AnimatorSet();
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(hintGhostView, View.SCALE_X, 1.0f, 1.15f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(hintGhostView, View.SCALE_Y, 1.0f, 1.15f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(hintGhostView, View.SCALE_X, 1.15f, 1.0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(hintGhostView, View.SCALE_Y, 1.15f, 1.0f);

        scaleSet.playTogether(scaleUpX, scaleUpY);
        scaleSet.setDuration(150);

        AnimatorSet scaleBackSet = new AnimatorSet();
        scaleBackSet.playTogether(scaleDownX, scaleDownY);
        scaleBackSet.setDuration(150);

        AnimatorSet fullAnim = new AnimatorSet();
        fullAnim.playSequentially(scaleSet, scaleBackSet);
        fullAnim.start();
    }

    protected ImageView findPreviewView() {
        for (Piece piece : gamePieces) {
            ImageView pieceView = piece.getImageView();
            if (pieceView != null && !piece.isPlaced()) {
                return pieceView;
            }
        }
        return null;
    }

    protected void updateUpcomingPiecePreviewUI() {
        previewContainer.removeAllViews();

        for (PieceType pieceType : upcomingPieceList) {
            ImageView preview = new ImageView(requireContext());
            preview.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            preview.setImageResource(getDrawableResourceForPieceType(pieceType));
            preview.setPadding(8, 0, 8, 0);
            previewContainer.addView(preview);
        }
    }

    protected void startHintComputation(Jigsaw jigsawGame) {
        if (currentTree != null) {
            currentTree.cancel();
        }

        cachedHintAction = null;
        hintButton.setEnabled(false);

        Thread mctsThread = new Thread(() -> {
            long startTime = System.nanoTime();

            float depthCoefficient = aiSettingsViewModel.getDepthCoefficient().getValue();
            float c = aiSettingsViewModel.getExplorationFactor().getValue();
            float explorationRateSimulation = aiSettingsViewModel.getSimulationExplorationFactor().getValue();
            Tree tree = new Tree(jigsawGame,
                    new Config(MAX_ITERATIONS,
                            (int)(depthCoefficient * Math.sqrt(jigsawGame.getRows() * jigsawGame.getCols())),
                            c,
                            explorationRateSimulation,
                            gameSettingsViewModel.getHintTimeSeconds().getValue() * 1000
                    ),
                    StopType.TIME);
            currentTree = tree;

            byte bestAction = tree.findBestActionbyTime();

            if (bestAction != -1) {
                cachedHintAction = bestAction;

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> hintButton.setEnabled(true));
            }

            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;
            System.out.printf("MCTS took %.2f ms%n", durationMs);
        });

        mctsThread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ghostView = null;
        if (hintGhostView != null) {
            hintGhostView.setVisibility(View.GONE);
            gameContainer.removeView(hintGhostView);
            hintGhostView = null;
        }

        if (boardContainer != null) {
            boardContainer.removeAllViews();
        }
        if (gameContainer != null && gamePieces != null) {
            for (Piece piece : gamePieces) {
                if (piece.getImageView() != null && piece.getImageView().getParent() != null) {
                    gameContainer.removeView(piece.getImageView());
                }
            }
            gamePieces.clear();
        }
        //Cancel any ongoing hint computation
        currentTree.cancel();
    }
}