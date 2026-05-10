package com.gitgames.checkers.engine;

import com.gitgames.checkers.model.Board;
import com.gitgames.checkers.model.Player;

public class WinConditionChecker {

    private final MoveValidator moveValidator;

    public WinConditionChecker(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    public boolean isGameOver(Board board, Player currentPlayer) {
        return moveValidator.getLegalMoves(board, currentPlayer).isEmpty();
    }
}
