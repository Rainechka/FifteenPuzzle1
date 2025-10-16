package com.example.fifteenpuzzle.controller;

import com.example.fifteenpuzzle.model.FinishRequest;
import com.example.fifteenpuzzle.model.MoveRequest;
import com.example.fifteenpuzzle.model.ResetRequest;
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
        long currentTime = gameService.getGameTime(gameId);

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
        return ResponseEntity.ok(Map.of("message", "Game reset"));
    }

    @PostMapping("/{gameId}/finish")
    public ResponseEntity<?> finishGame(
            @PathVariable String gameId,
            @Valid @RequestBody FinishRequest request) {
        gameService.saveResult(request.getPlayerName(), gameId);
        return ResponseEntity.ok(Map.of("message", "Результат сохранён"));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<?>> getLeaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }
}