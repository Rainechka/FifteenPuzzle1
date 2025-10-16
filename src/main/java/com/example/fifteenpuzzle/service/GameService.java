package com.example.fifteenpuzzle.service;

import com.example.fifteenpuzzle.model.GameBoard;
import com.example.fifteenpuzzle.model.LeaderboardEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GameService {

    private final ConcurrentMap<String, GameBoard> games = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final File leaderboardFile;

    public GameService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.leaderboardFile = new File("leaderboard.json");
    }

    @PostConstruct
    public void init() {
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
                Files.writeString(leaderboardFile.toPath(), "[]");
            } catch (IOException e) {
                throw new RuntimeException("Не удалось создать leaderboard.json", e);
            }
        }
    }

    public GameBoard getOrCreateGame(String gameId, int size) {
        return games.computeIfAbsent(gameId, id -> new GameBoard(size));
    }

    public GameBoard getGame(String gameId) {
        return games.get(gameId);
    }

    public void resetGame(String gameId, int size) {
        games.put(gameId, new GameBoard(size));
    }

    public static class MoveResult {
        private final boolean movable;
        private final int[] board;
        private final boolean solved;
        private final int size;

        public MoveResult(boolean movable, int[] board, boolean solved, int size) {
            this.movable = movable;
            this.board = board;
            this.solved = solved;
            this.size = size;
        }

        // Геттеры
        public boolean isMovable() { return movable; }
        public int[] getBoard() { return board; }
        public boolean isSolved() { return solved; }
        public int getSize() { return size; }
    }

    public MoveResult moveTile(String gameId, int tileId) {
        GameBoard board = games.get(gameId);
        if (board == null) {
            // Если игра не создана — создаём с размером по умолчанию 4
            board = getOrCreateGame(gameId, 4);
        }

        // Доп. проверка: tileId в допустимом диапазоне
        if (tileId < 1 || tileId >= board.getSize() * board.getSize()) {
            return new MoveResult(false, board.getBoardAsFlatArray(), board.isSolved(), board.getSize());
        }

        boolean wasMovable = board.canMoveTile(tileId);
        if (wasMovable) {
            board.moveTile(tileId);
        }
        return new MoveResult(wasMovable, board.getBoardAsFlatArray(), board.isSolved(), board.getSize());
    }

    public int[] getBoard(String gameId) {
        GameBoard board = getOrCreateGame(gameId, 4);
        return board.getBoardAsFlatArray();
    }

    public int getGameSize(String gameId) {
        GameBoard board = getOrCreateGame(gameId, 4);
        return board.getSize();
    }

    public GameSession startOrGetSession(String gameId, int size) {
        return sessions.computeIfAbsent(gameId, id -> new GameSession(size));
    }

    public long getGameTime(String gameId) {
        GameSession session = sessions.get(gameId);
        return session != null ? session.getElapsedSeconds() : 0;
    }

    public void finishGame(String gameId) {
        GameSession session = sessions.get(gameId);
        if (session != null) {
            session.finish();
        }
    }

    // === Leaderboard ===
    public List<LeaderboardEntry> getLeaderboard() {
        try {
            String content = Files.readString(leaderboardFile.toPath());
            if (content.trim().isEmpty()) return new ArrayList<>();
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveResult(String playerName, String gameId) {
        GameSession session = sessions.get(gameId);
        if (session == null || session.isFinished()) return;

        long time = session.getElapsedSeconds();
        int size = session.getSize();

        LeaderboardEntry newEntry = new LeaderboardEntry(playerName, size, time);

        List<LeaderboardEntry> all = getLeaderboard();
        // Фильтруем по размеру и добавляем новый результат
        List<LeaderboardEntry> forSize = new ArrayList<>();
        for (LeaderboardEntry e : all) {
            if (e.getSize() == size) {
                forSize.add(e);
            }
        }
        forSize.add(newEntry);
        // Сортируем по времени (по возрастанию)
        forSize.sort(Comparator.comparingLong(LeaderboardEntry::getTimeSeconds));
        // Оставляем только топ-5
        while (forSize.size() > 5) {
            forSize.remove(forSize.size() - 1);
        }

        // Обновляем общий список
        List<LeaderboardEntry> updated = new ArrayList<>();
        for (LeaderboardEntry e : all) {
            if (e.getSize() != size) {
                updated.add(e);
            }
        }
        updated.addAll(forSize);

        try {
            String json = objectMapper.writeValueAsString(updated);
            Files.writeString(leaderboardFile.toPath(), json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить рекорд", e);
        }

        // Завершаем сессию
        finishGame(gameId);
    }
}