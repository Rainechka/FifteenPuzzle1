package com.example.fifteenpuzzle.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FinishRequest {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 20, message = "Имя не должно превышать 20 символов")
    private String playerName;

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}