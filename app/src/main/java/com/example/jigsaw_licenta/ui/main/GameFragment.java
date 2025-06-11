package com.example.jigsaw_licenta.ui.main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
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
// Import your Jigsaw class
import com.example.jigsaw_licenta.model.Jigsaw;
import com.example.jigsaw_licenta.model.Piece;
import com.example.jigsaw_licenta.model.PieceType;
import com.example.jigsaw_licenta.model.Tree;
import com.example.jigsaw_licenta.utils.Config;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;
import com.example.jigsaw_licenta.viewmodel.SharedViewModel;
import com.example.jigsaw_licenta.viewmodel.SharedViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private GameViewModel gameViewModel;
    private GridLayout boardContainer;
    private ImageView ghostView;
    private ImageView hintGhostView;
    //just for reference to set the PiecePreview not the actual view
    private ImageView nextPiecePreview;
    private List<PieceType> upcomingPieceList = new ArrayList<>();
    private ImageView hintButton;
    private TextView movesTextView;
    private Button resetGameButton;
    private ImageView discardZone;
    private ConstraintLayout gameContainer;
    private float currentScale = 1.0f;
    private List<Piece> gamePieces = new ArrayList<>();
    private Jigsaw jigsawGame;
    private int baseTileSize;
    private float snapThreshold ;
    private Tree currentTree = null;
    private Byte cachedHintAction = null;

    public GameFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseTileSize = ContextCompat.getDrawable(requireContext(), R.drawable.board_tile).getIntrinsicHeight();

        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        SharedViewModelFactory factory = new SharedViewModelFactory(requireActivity().getApplication(), gameViewModel);
        sharedViewModel = new ViewModelProvider(requireActivity(),factory).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        boardContainer = view.findViewById(R.id.boardContainer);
        nextPiecePreview = view.findViewById(R.id.nextPiecePreview);
        resetGameButton = view.findViewById(R.id.resetGameButton);
        movesTextView = view.findViewById(R.id.movesTextView);
        discardZone = view.findViewById(R.id.discardZone);
        gameContainer = view.findViewById(R.id.gameContainer);
        hintButton = view.findViewById(R.id.HintButton);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update next piece preview
        if(!gameViewModel.isHoldingPiece){
            nextPiecePreview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    nextPiecePreview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    generateAndAddRandomPiece();
                }
            });
        }

        initializeGameUI();

        //Initialize game Queue Preview
        upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(sharedViewModel.getPreviewQueueSize().getValue() + 1));
        upcomingPieceList.remove(0); //so it wont the current preview in the list
        updateUpcomingPiecePreviewUI();

        //initialize hint computation
        startHintComputation(jigsawGame);

        resetGameButton.setOnClickListener(v -> resetGame());

        hintButton.setOnClickListener(v -> {
            if (cachedHintAction != null && hintButton.isEnabled()) {
                showHint(cachedHintAction);
                hintButton.setEnabled(false);
            }
        });
    }

    private void initializeGameUI() {
        // Reset ghost view
        ghostView = null;
        hintGhostView = null;

        // Fetch rows and cols
        Integer savedRows = sharedViewModel.getBoardRows().getValue();
        Integer savedCols = sharedViewModel.getBoardCols().getValue();

        int rowsToUse = (savedRows != null && savedRows > 0) ? savedRows : Jigsaw.DEFAULT_ROWS;
        int colsToUse = (savedCols != null && savedCols > 0) ? savedCols : Jigsaw.DEFAULT_COLS;

        // Recreate Jigsaw if needed
        if (gameViewModel.getJigsaw() == null || rowsToUse != gameViewModel.getJigsaw().getRows()
                || colsToUse != gameViewModel.getJigsaw().getCols()) {
            jigsawGame = new Jigsaw(rowsToUse, colsToUse);
            gameViewModel.setJigsaw(jigsawGame);
        } else {
            jigsawGame = gameViewModel.getJigsaw();
        }

        // Fetch initial scale
        Float initialScale = sharedViewModel.getImageScale().getValue();
        if (initialScale != null) {
            currentScale = initialScale;
        }

        // Load all pieces
        gamePieces.clear();
        gamePieces.addAll(gameViewModel.getPieceList());

        snapThreshold = baseTileSize * currentScale;
        gameViewModel.setCurrentScale(currentScale);

        // Render board and pieces after layout
        renderBoard(jigsawGame.getRows(), jigsawGame.getCols(), currentScale);

        boardContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boardContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                renderPieces();

            }
        });
    }

    private void renderBoard(int rows, int cols, float scale) {
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
        Log.d("GameFragment", "Board rendered with rows=" + rows + ", cols=" + cols + ", scale=" + scale);
    }
    private void renderPieces() {
        if (gameContainer == null || getContext() == null) return;

        // First remove all piece views to avoid duplicates
        for (Piece piece : gamePieces) {
            if (piece.getImageView() != null && piece.getImageView().getParent() != null) {
                gameContainer.removeView(piece.getImageView());
            }
        }

        // Now add and render all pieces with proper scaling
        for (Piece piece : gamePieces) {
            ImageView pieceView = piece.getImageView();
            if (pieceView == null) {
                // If the piece doesn't have an ImageView, create one
                pieceView = new ImageView(getContext());
                piece.setImageView(pieceView);
            }

            // Get the appropriate drawable resource
            int pieceDrawableResId = getDrawableResourceForPieceType(piece.getType());
            Drawable pieceDrawable = ContextCompat.getDrawable(requireContext(), pieceDrawableResId);
            if (pieceDrawable == null) continue;

            // Configure the ImageView
            pieceView.setImageResource(pieceDrawableResId);
            pieceView.setAdjustViewBounds(true);
            pieceView.setScaleType(ImageView.ScaleType.FIT_XY);

            // Calculate scaled dimensions
            int scaledWidth = (int) (pieceDrawable.getIntrinsicWidth() * currentScale);
            int scaledHeight = (int) (pieceDrawable.getIntrinsicHeight() * currentScale);

            // Set layout parameters with scaled dimensions
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    scaledWidth,
                    scaledHeight
            );
            pieceView.setLayoutParams(params);

            // Set position based on whether piece is placed or not
            if (piece.isPlaced()) {
                // If piece is placed, position it according to its actionSpot
                snapViewToActionIndex(piece.getActionSpot(), pieceView);
                //setDraggableTouchListener(pieceView, (int) pieceView.getX(), (int) pieceView.getY());
            } else {
                int[] previewLocation = new int[2];
                nextPiecePreview.getLocationOnScreen(previewLocation);

                int centerX = previewLocation[0] - (int)(pieceDrawable.getIntrinsicWidth() * currentScale / 2);
                int centerY = previewLocation[1] - (int)(pieceDrawable.getIntrinsicHeight() * currentScale / 2);

                pieceView.setX(centerX);
                pieceView.setY(centerY);
                setDraggableTouchListener(pieceView, centerX, centerY);
            }

            // Add to container
            gameContainer.addView(pieceView);
        }

        gameContainer.requestLayout();
    }

    private void generateAndAddRandomPiece() {
        PieceType nextType = gameViewModel.getJigsaw().getCurrentPieceType();
        if (nextType == null) return;

        // Create the piece view
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

        // Position at the preview location
        int[] previewLocation = new int[2];
        nextPiecePreview.getLocationOnScreen(previewLocation);

        int centerX = previewLocation[0] - (int)(drawable.getIntrinsicWidth() * currentScale / 2);
        int centerY = previewLocation[1] - (int)(drawable.getIntrinsicHeight() * currentScale / 2);


        newPieceView.setX(centerX);
        newPieceView.setY(centerY);

        setDraggableTouchListener(newPieceView, centerX, centerY);

        // Create the piece and add to game
        Piece newGamePiece = new Piece(nextType, newPieceView);
        gameContainer.addView(newPieceView);
        gamePieces.add(newGamePiece);
        gameViewModel.addPiece(newGamePiece);

        gameContainer.requestLayout();
        gameViewModel.isHoldingPiece = true;
    }
    private int getDrawableResourceForPieceType(PieceType type) {
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
    private void setDraggableTouchListener(View pieceView, int originalX, int originalY) {
        pieceView.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Ensure the view is being dragged within the gameContainer bounds
                // This is a simplified boundary check. More robust checks might be needed.
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;
                Piece matchedPiece = getPieceFromView(pieceView);

                //Dont move If piece placed
                if(matchedPiece != null && matchedPiece.isPlaced())
                    return true;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        v.performClick(); // For accessibility
                        // Optional: Bring to front when touched
                        v.bringToFront();

                        jigsawGame.setFigureIndex(matchedPiece.getType().ordinal());

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Clamp position to stay within parent bounds (gameContainer)
                        // Note: v.getWidth/Height might not be fully accurate if scaled without layout update.
                        // For simplicity, this uses the view's current X/Y.
                        float clampedX = Math.max(0, Math.min(newX, gameContainer.getWidth() - v.getWidth()));
                        float clampedY = Math.max(0, Math.min(newY, gameContainer.getHeight() - v.getHeight()));

                        v.setX(clampedX);
                        v.setY(clampedY);



                        // Ghost logic
                        Integer validIndex = getValidSnapIndex(v);
                        Log.d("GameFragment_Touch", "ACTION_MOVE - validIndex: " + validIndex); // Logging the variable
                        if (validIndex != null) {
                            //Reset the trash can

                            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
                            if (ghostView == null) {
                                ghostView = new ImageView(requireContext());
                                ghostView.setAdjustViewBounds(true);
                                ghostView.setScaleType(ImageView.ScaleType.FIT_XY);
                                ghostView.setAlpha(0.2f); // Semi-transparent
                                ghostView.setEnabled(false);
                                ghostView.setClickable(false);
                            }

                            if (ghostView.getParent() == null) {
                                gameContainer.addView(ghostView);
                            }

                            // Copy the drawable and size directly from the dragged view
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
                            snapViewToActionIndex(validIndex, ghostView); // Move ghost to correct tile

                        } else {
                            if (ghostView != null) {
                                ghostView.setVisibility(View.GONE);
                            }
                            updateDiscardZoneBackground(v);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        //DISCARD TO TRASH
                        if(isViewInDiscardZone(v)){
                            //ui update
                            removePieceFromGame(v);
                            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
                            //logical update
                            jigsawGame.performAction((byte)(jigsawGame.getRows() * jigsawGame.getCols()));
                            //update moves counter
                            updateMovesCounter();

                            //reset hintGhost
                            if (hintGhostView != null) {
                                hintGhostView.setVisibility(View.GONE);
                                gameContainer.removeView(hintGhostView);
                                hintGhostView = null;
                            }

                            //Preview update
                            upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(sharedViewModel.getPreviewQueueSize().getValue() + 1));
                            upcomingPieceList.remove(0);
                            updateUpcomingPiecePreviewUI();

                            // Update next piece preview
                            generateAndAddRandomPiece();

                            //Generate new Hint
                            startHintComputation(jigsawGame);
                        }
                        //SNAP TO GRID
                        else{
                            Integer snapIndex = getValidSnapIndex(v);
                            //Successful snap
                            if (snapIndex != null){

                                snapViewToActionIndex(snapIndex, v);
                                ghostView.setVisibility(View.GONE);

                                matchedPiece = getPieceFromView(pieceView);
                                if(matchedPiece != null){
                                    matchedPiece.setPlaced(true);
                                    matchedPiece.setActionSpot(snapIndex);
                                }

                                //logical update
                                jigsawGame.performAction(snapIndex.byteValue());

                                //update moves counter
                                updateMovesCounter();

                                //reset hintGhost
                                if (hintGhostView != null) {
                                    hintGhostView.setVisibility(View.GONE);
                                    gameContainer.removeView(hintGhostView);
                                    hintGhostView = null;
                                }

                                //Preview update
                                upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(sharedViewModel.getPreviewQueueSize().getValue() + 1));
                                upcomingPieceList.remove(0);
                                updateUpcomingPiecePreviewUI();



                                //*** CHECK FOR WIN ***
                                if(jigsawGame.hasFinished()){
                                    showGameOverDialog();
                                    //resetGame();
                                    return true;
                                }

                                // Update next piece preview
                                generateAndAddRandomPiece();

                                //Generate new Hint
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

    @Nullable
    private Integer getValidSnapIndex(View pieceView) {
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
    private void snapViewToActionIndex(int actionIndex, View pieceView) {
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

        // Optional: Adjust for pieceView's own dimensions (e.g., center align or top-left)
        pieceView.setX(targetX);
        pieceView.setY(targetY);
    }
    private Piece getPieceFromView(View pieceView){
        for (Piece piece : gamePieces) {
            if (piece.getImageView() == pieceView) {
                return piece;
            }
        }
        return null;
    }
    private boolean isViewInDiscardZone(View pieceView) {
        int[] pieceLoc = new int[2];
        int[] discardLoc = new int[2];

        pieceView.getLocationOnScreen(pieceLoc);
        discardZone.getLocationOnScreen(discardLoc);

        // Center of the piece
        float pieceCenterX = pieceLoc[0] + pieceView.getWidth() / 2f;
        float pieceCenterY = pieceLoc[1] + pieceView.getHeight() / 2f;

        // Bounds of the discard zone
        int discardLeft = discardLoc[0];
        int discardTop = discardLoc[1];
        int discardRight = discardLeft + discardZone.getWidth();
        int discardBottom = discardTop + discardZone.getHeight();

        // Check if center is inside discard bounds
        boolean isInsideBounds = pieceCenterX >= discardLeft && pieceCenterX <= discardRight &&
                pieceCenterY >= discardTop && pieceCenterY <= discardBottom;

        // Center of discard zone
        float discardCenterX = discardLeft + discardZone.getWidth() / 2f;
        float discardCenterY = discardTop + discardZone.getHeight() / 2f;

        // Distance between centers
        float dx = pieceCenterX - discardCenterX;
        float dy = pieceCenterY - discardCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Final condition: inside OR close enough
        return isInsideBounds || distance <= snapThreshold;
    }
    private void removePieceFromGame(View pieceView) {
        Piece piece = getPieceFromView(pieceView);
        if (piece != null) {
            gameContainer.removeView(pieceView);
            gamePieces.remove(piece);
            gameViewModel.removePiece(piece); // Add this method if needed
        }
    }
    private void showGameOverDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("You Win")
                .setMessage("Congratulations!")
                .setPositiveButton("Play Again", (dialog, which) -> resetGame())
                .setCancelable(false)
                .show();
    }
    private void resetGame() {
        onDestroyView();
        // Clear current pieces
        gamePieces.clear();
        gameViewModel.clearPieces();

        // Reset Jigsaw game
        gameViewModel.resetGame(sharedViewModel.getBoardRows().getValue(), sharedViewModel.getBoardCols().getValue());

        //Generate new Hint
        startHintComputation(jigsawGame);

        upcomingPieceList = new ArrayList<>(jigsawGame.getPieceQueue().peekNext(sharedViewModel.getPreviewQueueSize().getValue() + 1));
        upcomingPieceList.remove(0); //so it wont the current preview in the list
        updateUpcomingPiecePreviewUI();

        // Re-render
        initializeGameUI();
        updateMovesCounter();

        generateAndAddRandomPiece();

        //gameViewModel.isHoldingPiece = false;
    }
    private void updateMovesCounter() {
        if (jigsawGame != null) {
            movesTextView.setText("Moves: " + jigsawGame.quantity);
        }
    }
    private void updateDiscardZoneBackground(View pieceView) {
        if (isViewInDiscardZone(pieceView)) {
            discardZone.setBackgroundColor(Color.parseColor("#FFCCCC")); // Light red
        } else {
            discardZone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.discard_default));
            // Default gray
        }
    }
    private void showHint(int hintAction) {
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

        // Animate ghost
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
    private ImageView findPreviewView(){
        for (Piece piece : gamePieces) {
            ImageView pieceView = piece.getImageView();
            if (pieceView != null && !piece.isPlaced()) {
                return pieceView;
            }
    }
        return null;
    }
    private void updateUpcomingPiecePreviewUI() {
        LinearLayout previewContainer = requireView().findViewById(R.id.piecePreviewContainer);
        previewContainer.removeAllViews();

        for (PieceType pieceType : upcomingPieceList) {
            ImageView preview = new ImageView(requireContext());
            preview.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            preview.setImageResource(getDrawableResourceForPieceType(pieceType));
            preview.setPadding(8, 0, 8, 0);
            previewContainer.addView(preview);
        }
    }

    private void startHintComputation(Jigsaw jigsawGame) {
        // Cancel previous MCTS
        if (currentTree != null) {
            currentTree.cancel();
        }

        cachedHintAction = null;
        hintButton.setEnabled(false);

        Thread mctsThread = new Thread(() -> {
            long startTime = System.nanoTime();

            Tree tree = new Tree(jigsawGame, new Config(100000,10,(float) Math.sqrt(2.0),sharedViewModel.getHintTimeSeconds().getValue() * 1000));
            currentTree = tree; //(float) Math.sqrt(2.0)

            byte bestAction = tree.findBestAction();

            if (bestAction != -1) {
                cachedHintAction = bestAction;
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
        //reset GhostView
        ghostView = null;
        //reset hintGhost
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
    }
}