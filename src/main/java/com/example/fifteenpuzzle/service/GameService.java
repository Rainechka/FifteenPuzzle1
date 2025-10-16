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
import java.util.stream.Collectors;

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

    public void resetGame(String gameId, int size) {
        games.put(gameId, new GameBoard(size));
        sessions.remove(gameId); // сбрасываем сессию
    }

    public GameSession startOrGetSession(String gameId, int size) {
        return sessions.computeIfAbsent(gameId, id -> new GameSession(size));
    }

    public long getGameTime(String gameId) {
        GameSession session = sessions.get(gameId);
        return session != null ? session.getElapsedSeconds() : 0;
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

        public boolean isMovable() { return movable; }
        public int[] getBoard() { return board; }
        public boolean isSolved() { return solved; }
        public int getSize() { return size; }
    }

    public MoveResult moveTile(String gameId, int tileId) {
        GameBoard board = games.get(gameId);
        if (board == null) {
            board = getOrCreateGame(gameId, 4);
        }

        GameSession session = startOrGetSession(gameId, board.getSize());

        boolean wasMovable = board.canMoveTile(tileId);
        if (wasMovable) {
            session.startIfNotStarted();
            board.moveTile(tileId);
        }

        boolean solved = board.isSolved();
        // ❗ НЕ завершаем сессию здесь — делаем это только после сохранения результата

        return new MoveResult(wasMovable, board.getBoardAsFlatArray(), solved, board.getSize());
    }

    public int[] getBoard(String gameId) {
        GameBoard board = getOrCreateGame(gameId, 4);
        return board.getBoardAsFlatArray();
    }

    public int getGameSize(String gameId) {
        GameBoard board = getOrCreateGame(gameId, 4);
        return board.getSize();
    }

    // === Leaderboard ===
    public List<LeaderboardEntry> getLeaderboard() {
        try {
            String content = Files.readString(leaderboardFile.toPath());
            if (content.trim().isEmpty()) return new ArrayList<>();
            return objectMapper.readValue(content, new TypeReference<List<LeaderboardEntry>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveResult(String playerName, String gameId) {
        GameSession session = sessions.get(gameId);
        // Допускаем сохранение, если сессия существует и ещё не завершена
        if (session == null || session.isFinished()) {
            return;
        }

        long time = session.getElapsedSeconds();
        if (time <= 0) {
            return; // защита от некорректного времени
        }

        int size = session.getSize();
        LeaderboardEntry newEntry = new LeaderboardEntry(playerName, size, time);

        // Загружаем текущие рекорды
        List<LeaderboardEntry> all = getLeaderboard();

        // Группируем по размеру
        Map<Integer, List<LeaderboardEntry>> grouped = all.stream()
                .collect(Collectors.groupingBy(LeaderboardEntry::getSize));

        // Обновляем список для текущего размера
        List<LeaderboardEntry> forSize = grouped.getOrDefault(size, new ArrayList<>());
        forSize.add(newEntry);
        forSize.sort(Comparator.comparingLong(LeaderboardEntry::getTimeSeconds));
        if (forSize.size() > 5) {
            forSize = forSize.subList(0, 5);
        }

        // Формируем обновлённый общий список
        List<LeaderboardEntry> updated = new ArrayList<>();
        for (Map.Entry<Integer, List<LeaderboardEntry>> entry : grouped.entrySet()) {
            if (entry.getKey() != size) {
                updated.addAll(entry.getValue());
            }
        }
        updated.addAll(forSize);

        // Сохраняем в файл
        try {
            String json = objectMapper.writeValueAsString(updated);
            Files.writeString(leaderboardFile.toPath(), json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить рекорд в leaderboard.json", e);
        }

        // ✅ Завершаем сессию ТОЛЬКО после успешного сохранения
        session.finish();
    }
}