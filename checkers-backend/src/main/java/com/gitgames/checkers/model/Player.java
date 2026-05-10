package com.gitgames.checkers.model;

public enum Player {
    BLACK,
    RED;

    public Player opponent() {
        return this == BLACK ? RED : BLACK;
    }
}
