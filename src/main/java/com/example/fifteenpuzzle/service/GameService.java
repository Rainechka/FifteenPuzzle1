package com.example.fifteenpuzzle.service;

import com.example.fifteenpuzzle.model.GameBoard;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GameService {

    private final ConcurrentMap<String, GameBoard> games = new ConcurrentHashMap<>();

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
}