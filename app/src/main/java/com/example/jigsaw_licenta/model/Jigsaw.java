package com.example.jigsaw_licenta.model;

import com.example.jigsaw_licenta.utils.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Jigsaw implements Environment<Jigsaw, Byte> {
    public static final int ROWS = 4;
    public static final int COLS = 6;
    public static final int SKIP_ACTION = 24;
    public static final int TOTAL_FIGURES = 6;
    public static final int TOTAL_ACTIONS = 25;

    public static final int[] ALL_FIGURES = {
            0b100000000000000000000000,
            0b100000100000100000000000,
            0b100000110000000000000000,
            0b110000010000000000000000,
            0b110000110000000000000000,
            0b110000011000000000000000
    };

    public int board;
    public byte figureIndex;
    public byte quantity;

    private static final Random random = new Random();

    public Jigsaw() {
        this.board = 0;
        this.figureIndex = randomFigure();
        this.quantity = 0;
    }

    private static byte randomFigure() {
        return (byte) random.nextInt(TOTAL_FIGURES);
    }

    public static byte index(byte row, byte col) {
        return (byte) (6 * row + col);
    }

    public void toggleCoord(byte row, byte col) {
        int x = 1 << (ROWS * COLS - 1);
        this.board ^= (x >>> (row * COLS + col));
    }

    public boolean coord(byte row, byte col) {
        int x = 1 << (ROWS * COLS - 1);
        return (this.board & (x >>> (row * COLS + col))) != 0;
    }

    public int figure() {
        return ALL_FIGURES[figureIndex];
    }

    public boolean inFigure(byte action, byte index) {
        return ((figure() >>> action) & ((1 << (ROWS * COLS - 1)) >>> index)) != 0;
    }

    private boolean isLegal(byte action) {
        if (action == SKIP_ACTION) return true;

        int figure = figure();
        int shifted = figure >>> action;

        if ((board & shifted) != 0) return false;

        // Ensure shape fits after right shift
        if ((shifted << action) != figure) return false;

        // Check shape doesn’t overflow horizontally
        int columnMask = ~(0xFF << COLS);
        for (int row = 0; row < ROWS; row++) {
            int figureRow = (figure >>> (COLS * row)) & columnMask;
            if (((figureRow >>> (action % COLS)) << (action % COLS)) != figureRow) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasFinished() {
        return this.board == 0xFFFFFF;
    }

    @Override
    public void performAction(Byte action) {
        if (action != SKIP_ACTION) {
            this.board |= (figure() >>> action);
        }
        this.quantity++;
        this.figureIndex = randomFigure();
    }

    @Override
    public List<Byte> legalActions() {
        List<Byte> actions = new ArrayList<>();
        for (byte i = 0; i < TOTAL_ACTIONS; i++) {
            if (isLegal(i)) {
                actions.add(i);
            }
        }
        return actions;
    }

    @Override
    public int eval() {
        return hasFinished() ? 1 : 0;
    }

    @Override
    public Jigsaw cloneState() {
        Jigsaw copy = new Jigsaw();
        copy.board = this.board;
        copy.figureIndex = this.figureIndex;
        copy.quantity = this.quantity;
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========").append(quantity).append("=========\n");

        int boardMask = 1 << (ROWS * COLS - 1);
        int figureMask = 1 << (ROWS * COLS - 1);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if ((board & boardMask) != 0) {
                    sb.append("🟩");
                } else if (isLegal((byte)(i * COLS + j))) {
                    sb.append("🟦");
                } else {
                    sb.append("⬛");
                }
                boardMask >>>= 1;
            }

            sb.append(" ");

            for (int j = 0; j < COLS; j++) {
                if ((figure() & figureMask) != 0) {
                    sb.append("🟥");
                } else {
                    sb.append("  ");
                }
                figureMask >>>= 1;
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
