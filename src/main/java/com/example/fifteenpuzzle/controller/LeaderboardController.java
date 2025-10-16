package com.example.fifteenpuzzle.controller;

import com.example.fifteenpuzzle.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LeaderboardController {

    private final GameService gameService;

    public LeaderboardController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/leaderboard")
    public List<?> getLeaderboard() {
        return gameService.getLeaderboard();
    }
}