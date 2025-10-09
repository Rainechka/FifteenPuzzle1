package com.example.fifteenpuzzle.controller;

import com.example.fifteenpuzzle.model.GameBoard;
import com.example.fifteenpuzzle.model.MoveRequest;
import com.example.fifteenpuzzle.model.ResetRequest;
import com.example.fifteenpuzzle.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(Map.of(
                "movable", result.isMovable(),
                "board", result.getBoard(),
                "solved", result.isSolved(),
                "size", result.getSize()
        ));
    }

    @GetMapping("/{gameId}/board")
    public ResponseEntity<?> getBoard(@PathVariable String gameId) {
        int[] board = gameService.getBoard(gameId);
        int size = gameService.getGameSize(gameId);
        return ResponseEntity.ok(Map.of(
                "board", board,
                "size", size
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
}