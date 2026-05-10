package com.gitgames.checkers.model;

import java.util.List;

public class Move {
    private final Position from;
    private final List<Position> steps;
    private final List<Position> captured;
    private final boolean becomesKing;

    public Move(Position from, List<Position> steps, List<Position> captured, boolean becomesKing) {
        this.from = from;
        this.steps = steps;
        this.captured = captured;
        this.becomesKing = becomesKing;
    }

    public Position getFrom() { return from; }
    public List<Position> getSteps() { return steps; }
    public List<Position> getCaptured() { return captured; }
    public boolean isBecomesKing() { return becomesKing; }

    public Position getTo() {
        return steps.get(steps.size() - 1);
    }

    public boolean isJump() {
        return !captured.isEmpty();
    }

    @Override
    public String toString() {
        return "Move(" + from + " -> " + getTo() + " captures=" + captured.size() + " king=" + becomesKing + ")";
    }
}
