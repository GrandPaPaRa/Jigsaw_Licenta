package com.example.jigsaw_licenta.model;

public class Board {
    private final int rows;
    private final int columns;
    private final Piece[][] grid;
    public Board(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Board dimensions (rows and columns) must be positive.");
        }
        this.rows = rows;
        this.columns = columns;
        this.grid = new Piece[rows][columns]; // Initialize the grid with nulls
    }
    public int getRows() {
        return rows;
    }
    public int getColumns() {
        return columns;
    }
}
