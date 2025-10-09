// com.example.fifteenpuzzle.service.GameSession.java
package com.example.fifteenpuzzle.service;

import java.time.Instant;

public class GameSession {
    private final int size;
    private Instant startTime;
    private boolean finished = false;

    public GameSession(int size) {
        this.size = size;
        this.startTime = Instant.now();
    }

    public long getElapsedSeconds() {
        if (finished) return 0;
        return java.time.Duration.between(startTime, Instant.now()).toSeconds();
    }

    public void finish() {
        this.finished = true;
    }

    public int getSize() { return size; }
    public boolean isFinished() { return finished; }
}