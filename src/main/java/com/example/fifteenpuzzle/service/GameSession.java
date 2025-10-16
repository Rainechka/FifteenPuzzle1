package com.example.fifteenpuzzle.service;

import java.time.Instant;

public class GameSession {
    private final int size;
    private Instant startTime;
    private boolean finished = false;
    private boolean started = false;

    public GameSession(int size) {
        this.size = size;
    }

    public void startIfNotStarted() {
        if (!started && !finished) {
            this.startTime = Instant.now();
            this.started = true;
        }
    }

    public long getElapsedSeconds() {
        if (!started || finished) {
            return 0;
        }
        return java.time.Duration.between(startTime, Instant.now()).toSeconds();
    }

    public void finish() {
        this.finished = true;
    }

    public boolean isFinished() { return finished; }
    public int getSize() { return size; }
    public boolean isStarted() { return started; }
}