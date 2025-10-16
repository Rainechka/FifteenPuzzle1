package com.example.fifteenpuzzle.controller;

import com.example.fifteenpuzzle.model.*;
import com.example.fifteenpuzzle.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> moveTile(
            @PathVariable String gameId,
            @Valid @RequestBody MoveRequest request) {

        GameService.MoveResult result = gameService.moveTile(gameId, request.getTileId());
        long currentTime = gameService.getGameTime(gameId); // ← добавили

        return ResponseEntity.ok(Map.of(
                "movable", result.isMovable(),
                "board", result.getBoard(),
                "solved", result.isSolved(),
                "size", result.getSize(),
                "timeSeconds", currentTime
        ));
    }

    @GetMapping("/{gameId}/board")
    public ResponseEntity<?> getBoard(@PathVariable String gameId) {
        int[] board = gameService.getBoard(gameId);
        int size = gameService.getGameSize(gameId);
        long time = gameService.getGameTime(gameId);
        return ResponseEntity.ok(Map.of(
                "board", board,
                "size", size,
                "timeSeconds", time
        ));
    }

    @PostMapping("/{gameId}/reset")
    public ResponseEntity<?> resetGame(
            @PathVariable String gameId,
            @Valid @RequestBody(required = false) ResetRequest request) {

        int size = (request != null) ? request.getSize() : 4;
        gameService.resetGame(gameId, size);

        GameBoard board = gameService.getGame(gameId);
        return ResponseEntity.ok(Map.of(
                "message", "Game reset",
                "board", board.getBoardAsFlatArray(),
                "size", board.getSize()
        ));
    }

    @PostMapping("/{gameId}/finish")
    public ResponseEntity<?> finishGame(
            @PathVariable String gameId,
            @Valid @RequestBody FinishRequest request) {
        gameService.saveResult(request.getPlayerName(), gameId);
        return ResponseEntity.ok(Map.of("message", "Результат сохранён"));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        List<LeaderboardEntry> leaderboard = gameService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

}