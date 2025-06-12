package com.example.jigsaw_licenta.model;

import com.example.jigsaw_licenta.utils.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Jigsaw implements Environment<Jigsaw, Byte> {

    // --- Default values (can be used as fallbacks or for a default constructor) ---
    public static final int MAX_ITERATIONS = 500000;
    public static final int DEFAULT_ROWS = 4;
    public static final int DEFAULT_COLS = 6;
    public static final int TOTAL_FIGURES = 6; // This remains constant as per your request
    public static final int QUEUE_CAPACITY = 50;

    // --- Instance-specific fields ---
    private int rows;
    private int cols;
    private int skipActionValue; // Renamed from SKIP_ACTION to avoid confusion with static final
    private int totalActionsValue; // Renamed from TOTAL_ACTIONS
    private long[] figures;
    private PieceQueue pieceQueue;
    public long board; // Bitmask for the placed pieces
    private int figureIndex;
    public int quantity;

    private static final Random random = new Random();

    /**
     * Default constructor: uses DEFAULT_ROWS and DEFAULT_COLS.
     */
    public Jigsaw() {
        this(DEFAULT_ROWS, DEFAULT_COLS, new PieceQueue(System.currentTimeMillis(),QUEUE_CAPACITY));
    }

    public Jigsaw(int rows, int cols, PieceQueue pieceQueue) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive.");
        }
        this.rows = rows;
        this.cols = cols;
        this.figures = generateFigures();
        this.skipActionValue = rows * cols;
        this.totalActionsValue = this.skipActionValue + 1;

        this.pieceQueue = pieceQueue;
        this.figureIndex = pieceQueue.peek().ordinal(); // Get first piece from queue
        this.quantity = 0;

    }
    public Jigsaw(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive.");
        }
        this.rows = rows;
        this.cols = cols;
        this.figures = generateFigures();
        this.skipActionValue = rows * cols;
        this.totalActionsValue = this.skipActionValue + 1;

        this.pieceQueue = new PieceQueue(System.currentTimeMillis(),QUEUE_CAPACITY);
        this.figureIndex = pieceQueue.peek().ordinal(); // Get first piece from queue
        this.quantity = 0;

    }
    // --- Getters ---
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    // Making skipAction and totalActions accessible if needed, but they are derived
    public int getSkipActionValue() {
        return skipActionValue;
    }

    public int getTotalActionsValue() {
        return totalActionsValue;
    }

    public int getFigureIndex() {
        return figureIndex;
    }
    public PieceQueue getPieceQueue() {
        return pieceQueue;
    }

    public void setFigureIndex(int figureIndex) {
        if (figureIndex < 0 || figureIndex >= TOTAL_FIGURES) {
            throw new IllegalArgumentException("Figure index must be between 0 and " + (TOTAL_FIGURES - 1));
        }
        this.figureIndex = figureIndex;
    }

    // Add these new methods
    public PieceType getCurrentPieceType() {
        return pieceQueue.peek();
    }

    public void consumePiece() {
        pieceQueue.pop();
        quantity++;
        if (!pieceQueue.isEmpty()) {
            figureIndex = pieceQueue.peek().ordinal();
        }
    }
    //For this to work we need to sync the figures with the piece types(enums) (the orders)
    public long[] generateFigures() {
        List<Long> figures = new ArrayList<>();

        // 1. Single dot (top-left)
        figures.add(1L << (rows * cols - 1));

        // 2. Vertical line (3 dots)
        if (rows >= 3) {
            long vertical = 0;
            for (int i = 0; i < 3; i++) {
                vertical |= (1L << (rows * cols - 1 - i * cols));
            }
            figures.add(vertical);
        }

        // 3. L-shape
        if (rows >= 2 && cols >= 2) {
            long lShape = (1L << (rows * cols - 1)) |
                    (1L << (rows * cols - 1 - cols)) |
                    (1L << (rows * cols - 1 - cols - 1));
            figures.add(lShape);
        }

        // 4. Reverse L-shape
        if (rows >= 2 && cols >= 2) {
            long reverseL = (1L << (rows * cols - 1)) |
                    (1L << (rows * cols - 2)) |
                    (1L << (rows * cols - 1 - cols - 1));
            figures.add(reverseL);
        }

        // 5. Square (2x2)
        if (rows >= 2 && cols >= 2) {
            long square = (1L << (rows * cols - 1)) |
                    (1L << (rows * cols - 1 - 1)) |
                    (1L << (rows * cols - 1 - cols)) |
                    (1L << (rows * cols - 1 - cols - 1));
            figures.add(square);
        }

        // 6. Z-shape
        if (rows >= 2 && cols >= 3) {
            long zPiece = (1L << (rows * cols - 1)) |
                    (1L << (rows * cols - 2)) |
                    (1L << (rows * cols - 1 - cols - 1)) |
                    (1L << (rows * cols - 1 - cols - 2));
            figures.add(zPiece);
        }

        return figures.stream().mapToLong(i->i).toArray();
    }

//*****FOR DEBUG ONLY*****
//    private static byte randomFigure() {
//        return (byte) random.nextInt(TOTAL_FIGURES);
//    }
//
//    public static byte index(byte row, byte col) {
//        return (byte) (6 * row + col);
//    }
//
//    public void toggleCoord(byte row, byte col) {
//        int x = 1 << (rows * cols - 1);
//        this.board ^= (x >>> (row * cols + col));
//    }
//
//    public boolean coord(byte row, byte col) {
//        int x = 1 << (rows * cols - 1);
//        return (this.board & (x >>> (row * cols + col))) != 0;
//    }

    public long figure() {
        return figures[figureIndex];
    }

    public boolean inFigure(byte action, byte index) {
        return ((figure() >>> action) & ((1L << (rows * cols - 1)) >>> index)) != 0;
    }

    private boolean isLegal(byte action) {
        if (action == skipActionValue) return true;

        long figure = figure();
        long shifted = figure >>> action;

        if ((board & shifted) != 0) return false;

        // Ensure shape fits after right shift
        if ((shifted << action) != figure) return false;

        // Check shape doesn’t overflow horizontally
        //int columnMask = ~(0xFF << cols);
        int columnMask = (1 << cols) - 1;
        for (int row = 0; row < rows; row++) {
            long figureRow = (figure >>> (cols * row)) & columnMask;
            if (((figureRow >>> (action % cols)) << (action % cols)) != figureRow) {
                return false;
            }
        }

        return true;
    }

    public boolean isLegal(byte action, PieceType pieceType) {
        if (action == skipActionValue) return true;

        long figure = figures[pieceType.ordinal()];
        long shifted = figure >>> action;

        // Ensure shape fits after right shift
        if ((shifted << action) != figure) return false;

        if ((board & shifted) != 0) return false;

        // Check shape doesn’t overflow horizontally
        //int columnMask = ~(0xFF << cols);
        int columnMask = (1 << cols) - 1;
        for (int row = 0; row < rows; row++) {
            long figureRow = (figure >>> (cols * row)) & columnMask;
            if (((figureRow >>> (action % cols)) << (action % cols)) != figureRow) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasFinished() {
        //return this.board == 0xFFFFFF;
        return board == (1L << (rows * cols)) - 1;
    }

    @Override
    public void performAction(Byte action) {
        if (action != skipActionValue) {
            this.board |= (figure() >>> action);
        }
        consumePiece(); // Modified to use piece queue
    }

    public void performAction(int action, Piece piece) {
        if (action != skipActionValue) {
            this.board |= (figure() >>> action);
            piece.setPlaced(true);
            piece.setActionSpot(action);
        }
        this.quantity++;

    }

    @Override
    public List<Byte> legalActions() {
        List<Byte> actions = new ArrayList<>();
        for (byte i = 0; i < totalActionsValue; i++) {
            if (isLegal(i)) {
                actions.add(i);
            }
        }
        return actions;
    }

    @Override
    public int eval() {
//        int totalCells = rows * cols;
//        int filledCells = Long.bitCount(board);
//
//        // Board fill progress (0–100)
//        int fillScore = (int) ((filledCells / (float) totalCells));
//
//        int expectedMaxPieces = totalCells / 3;
//        int penalty = (int) (((float) quantity / expectedMaxPieces)); // up to 50 penalty
//
//        int score = Math.max(fillScore - penalty, 0);
//
//
//        if (hasFinished()) {
//            score += 1;
//        }
//
//        return score;
        return hasFinished()? 1 : 0;
    }

    @Override
    public Jigsaw cloneState() {
        Jigsaw copy = new Jigsaw(rows, cols, new PieceQueue(pieceQueue));
        copy.board = this.board;
        copy.figureIndex = this.figureIndex;
        copy.quantity = this.quantity;
        return copy;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========").append(quantity).append("=========\n");

        long boardMask = 1L << (rows * cols - 1);
        long figureMask = 1L << (rows * cols - 1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((board & boardMask) != 0) {
                    sb.append("🟩");
                } else if (isLegal((byte)(i * cols + j))) {
                    sb.append("🟦");
                } else {
                    sb.append("⬛");
                }
                boardMask >>>= 1L;
            }

            sb.append(" ");

            for (int j = 0; j < cols; j++) {
                if ((figure() & figureMask) != 0) {
                    sb.append("🟥");
                } else {
                    sb.append("  ");
                }
                figureMask >>>= 1L;
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}