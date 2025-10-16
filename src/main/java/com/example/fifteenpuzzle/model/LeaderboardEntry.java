package com.example.fifteenpuzzle.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class LeaderboardEntry {
    private String playerName;
    private int size;
    private long timeSeconds;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timestamp;

    // Конструкторы, геттеры, сеттеры
    public LeaderboardEntry() {}

    public LeaderboardEntry(String playerName, int size, long timeSeconds) {
        this.playerName = playerName;
        this.size = size;
        this.timeSeconds = timeSeconds;
        this.timestamp = Instant.now();
    }

    // Геттеры и сеттеры
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(long timeSeconds) { this.timeSeconds = timeSeconds; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}