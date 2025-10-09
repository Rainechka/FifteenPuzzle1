package com.example.fifteenpuzzle.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ResetRequest {

    @Min(value = 3, message = "Size must be at least 3")
    @Max(value = 5, message = "Size must be at most 5")
    private int size = 4; // default

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}