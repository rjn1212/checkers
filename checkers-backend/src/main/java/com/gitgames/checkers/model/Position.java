package com.gitgames.checkers.model;

public record Position(int col, int row) {
    public boolean isValid() {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    public boolean isPlayableSquare() {
        return (col + row) % 2 == 1;
    }
}
