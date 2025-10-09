package com.example.fifteenpuzzle.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class MoveRequest {

    @Min(value = 1, message = "tileId must be at least 1")
    @Max(value = 99, message = "tileId must not exceed 99") // будем проверять точнее в сервисе
    private int tileId;

    public int getTileId() {
        return tileId;
    }

    public void setTileId(int tileId) {
        this.tileId = tileId;
    }
}