package com.example.jigsaw_licenta.model;

import android.widget.ImageView; // Import ImageView

public class Piece {
    private PieceType type;
    private boolean isPlaced;
    private ImageView imageView; // Reference to the ImageView for this piece
    private int actionSpot; // In witch spot the top left corner of the piece is placed

    // Constructor that takes the type and its view
    public Piece(PieceType type, ImageView imageView) {
        this.type = type;
        this.imageView = imageView;
        this.isPlaced = false; // Default
    }

    public Piece(PieceType type, ImageView imageView, boolean isPlaced) {
        this.type = type;
        this.imageView = imageView;
        this.isPlaced = isPlaced;
    }

    public PieceType getType() {
        return type;
    }
    public boolean isPlaced() {
        return isPlaced;
    }
    public int getActionSpot() {
        return actionSpot;
    }
    public void setActionSpot(int actionSpot) {
        this.actionSpot = actionSpot;
    }
    public ImageView getImageView() {
        return imageView;
    }
    public void setPlaced(boolean placed) {
        isPlaced = placed;
    }
    public void setType(PieceType type) {
        // If you change the type, you might also need to update the imageView's image
        this.type = type;
        // Example: if (this.imageView != null) { updateImageViewResourceBasedOnType(); }
    }
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "type=" + type +
                ", isPlaced=" + isPlaced +
                ", hasImageView=" + (imageView != null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        // Consider adding a unique ID to pieces for more robust equality
        return type == piece.type && isPlaced == piece.isPlaced;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (isPlaced ? 1 : 0);
        return result;
    }
}