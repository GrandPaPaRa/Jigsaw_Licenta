package com.example.jigsaw_licenta.model;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

public class PieceQueue {
    private final Random rand = new Random();
    private final long seed;
    private int position;  // how many pieces have been popped so far
    private final int capacity;
    private final Queue<PieceType> queue;

    public PieceQueue(long seed, int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.seed = seed;
        this.position = 0;
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);

        // Fill initial queue
        for (int i = 0; i < capacity; i++) {
            queue.add(generatePiece(i));
        }
    }

    // Clone constructor
    public PieceQueue(PieceQueue other) {
        this.seed = other.seed;
        this.position = other.position;
        this.capacity = other.capacity;
        this.queue = new ArrayDeque<>(other.queue); // copy queue content
    }

    // Get the piece type at the current position without advancing
    // Peek the front piece without popping
    public PieceType peek() {
        if (queue.isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return queue.peek();
    }

    // Pop the front piece, advance position, and generate next piece to keep queue full
    public PieceType pop() {
        if (queue.isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        PieceType piece = queue.poll();
        position++;

        // Calculate next piece index to generate
        int nextIndex = position + capacity - 1;
        if (nextIndex < Integer.MAX_VALUE) {
            queue.add(generatePiece(nextIndex));
        }
        return piece;
    }

    // Generate piece based on seed + position
    private PieceType generatePiece(int pos) {
        rand.setSeed(seed + pos);
        int index = rand.nextInt(PieceType.values().length);
        return PieceType.values()[index];
    }
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Get current position (number of popped pieces)
    public int getPosition() {
        return position;
    }
    public long getSeed(){return seed;}

    // Set current position (careful: only for advanced use cases)
    public void setPosition(int position) {
        this.position = position;
        queue.clear();
        // Refill queue from new position
        for (int i = 0; i < capacity; i++) {
            queue.add(generatePiece(position + i));
        }
    }
    public java.util.List<PieceType> peekNext(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }

        java.util.List<PieceType> list = new java.util.ArrayList<>(count);
        int i = 0;
        for (PieceType piece : queue) {
            if (i >= count) break;
            list.add(piece);
            i++;
        }
        return list;
    }

    // Get current queue size
    public int size() {
        return queue.size();
    }
}
