package com.example.jigsaw_licenta.model; // Or your preferred package

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public enum PieceType {
    DOT,
    LINE,
    LED,
    ZED,
    NED,
    SQUARE;

    private static final Random random = new Random();
    private static final double EPSILON = 1e-6; // A small tolerance for floating-point comparison
    public static PieceType getRandomPieceType() {
        PieceType[] values = PieceType.values();
        return values[random.nextInt(values.length)];
    }
    public static PieceType getPieceWithOdds(EnumMap<PieceType, Double> odds) {
        if (odds == null) {
            throw new IllegalArgumentException("Odds map cannot be null.");
        }

        // Validate that all PieceTypes are present in the odds map
        if (odds.size() != PieceType.values().length) {
            throw new IllegalArgumentException("Odds must be provided for all PieceTypes. Expected " +
                    PieceType.values().length + " entries, but got " + odds.size());
        }

        double sumOfOdds = 0.0;
        for (Double odd : odds.values()) {
            if (odd == null || odd < 0) {
                throw new IllegalArgumentException("Odds cannot be null or negative.");
            }
            sumOfOdds += odd;
        }

        // Check if the sum of odds is approximately 1.0
        if (Math.abs(sumOfOdds - 1.0) > EPSILON) {
            throw new IllegalArgumentException("The sum of odds must be 1.0. Current sum: " + sumOfOdds);
        }

        double randomValue = random.nextDouble(); // Generates a value between 0.0 (inclusive) and 1.0 (exclusive)
        double cumulativeProbability = 0.0;

        for (Map.Entry<PieceType, Double> entry : odds.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue < cumulativeProbability) {
                return entry.getKey();
            }
        }

        PieceType[] types = PieceType.values();
        return types[types.length - 1]; // Fallback, though ideally the loop logic is sufficient
    }
}