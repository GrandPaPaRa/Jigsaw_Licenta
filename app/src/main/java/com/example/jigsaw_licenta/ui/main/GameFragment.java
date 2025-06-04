package com.example.jigsaw_licenta.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.model.Piece;
import com.example.jigsaw_licenta.model.PieceType;
import com.example.jigsaw_licenta.viewmodel.SharedViewModel;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private ImageView boardImage;
    private Button generatePieceButton;
    private ConstraintLayout gameContainer;
    private Float currentScale = 1.0f;
    private List<Piece> gamePieces = new ArrayList<>();
    public GameFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Activity-scoped SharedViewModel instance.
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false); // Replace with your game fragment layout
        boardImage = view.findViewById(R.id.board); // Replace with your ImageView IDs
        generatePieceButton = view.findViewById(R.id.generatePieceButton);
        gameContainer = view.findViewById(R.id.gameContainer);
        return view;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getImageScale().observe(getViewLifecycleOwner(), scale -> {
            // This lambda will be called when the scale value changes
            if (scale != null) {
                applyScaleToImageViews(scale);
            }
        });

        // Apply initial scale if needed
        currentScale = sharedViewModel.getImageScale().getValue();
        if (currentScale != null) {
            applyScaleToImageViews(currentScale);
        }
        generatePieceButton.setOnClickListener(v -> generateAndAddRandomPiece());
    }
    private void generateAndAddRandomPiece() {
        PieceType randomType = PieceType.getRandomPieceType(); // Uses the equal odds version for now

        ImageView newPieceView = new ImageView(requireContext());

        int pieceDrawableResId;
        switch (randomType) {
            case DOT:
                pieceDrawableResId = R.drawable.dot;
                break;
            case LINE:
                pieceDrawableResId = R.drawable.line;
                break;
            case LED:
                pieceDrawableResId = R.drawable.led;
                break;
            case ZED:
                pieceDrawableResId = R.drawable.zed;
                break;
            case NED:
                pieceDrawableResId = R.drawable.ned;
                break;
            case SQUARE:
                pieceDrawableResId = R.drawable.square;
                break;
            default:
                pieceDrawableResId = R.drawable.ic_launcher_background;
                break;
        }
        newPieceView.setImageResource(pieceDrawableResId);
        newPieceView.setAdjustViewBounds(true); // Important for WRAP_CONTENT with certain scale types
        // newPieceView.setScaleType(ImageView.ScaleType.FIT_CENTER); // Or another appropriate ScaleType

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, // width
                ConstraintLayout.LayoutParams.WRAP_CONTENT  // height
        );
        newPieceView.setLayoutParams(params);
        newPieceView.setX(100);
        newPieceView.setY(100);


        // --- Apply Current Scale (after layout params are set) ---
        // Scaling will now apply to the wrapped content size
        newPieceView.setScaleX(currentScale);
        newPieceView.setScaleY(currentScale);

        setDraggableTouchListener(newPieceView);
        Piece newGamePiece = new Piece(randomType, newPieceView);
        gameContainer.addView(newPieceView);
        gamePieces.add(newGamePiece);

        gameContainer.requestLayout();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setDraggableTouchListener(View pieceView) {
        pieceView.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Ensure the view is being dragged within the gameContainer bounds
                // This is a simplified boundary check. More robust checks might be needed.
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        v.performClick(); // For accessibility
                        // Optional: Bring to front when touched
                        v.bringToFront();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Clamp position to stay within parent bounds (gameContainer)
                        // Note: v.getWidth/Height might not be fully accurate if scaled without layout update.
                        // For simplicity, this uses the view's current X/Y.
                        float clampedX = Math.max(0, Math.min(newX, gameContainer.getWidth() - v.getWidth()));
                        float clampedY = Math.max(0, Math.min(newY, gameContainer.getHeight() - v.getHeight()));

                        v.setX(clampedX);
                        v.setY(clampedY);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }
    private void applyScaleToImageViews(float scale) {
        scaleImageView(boardImage, scale);
        for (Piece piece : gamePieces) {
            ImageView pieceView = piece.getImageView();
            if (pieceView != null) {
                pieceView.setScaleX(scale);
                pieceView.setScaleY(scale);
            }
        }
    }
    private void scaleImageView(ImageView imageView, float scale) {
        if (imageView != null) {
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
        }
    }
}
