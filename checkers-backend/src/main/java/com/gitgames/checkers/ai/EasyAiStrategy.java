package com.gitgames.checkers.ai;

import com.gitgames.checkers.engine.MoveValidator;
import com.gitgames.checkers.model.Board;
import com.gitgames.checkers.model.Move;
import com.gitgames.checkers.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EasyAiStrategy implements AiStrategy {

    private final MoveValidator moveValidator;
    private final Random rand = new Random();

    public EasyAiStrategy(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    @Override
    public Move selectMove(Board board, Player player) {
        List<Move> moves = moveValidator.getLegalMoves(board, player);
        if (moves.isEmpty()) return null;

        int maxCaptures = 0;
        for (Move m : moves) {
            maxCaptures = Math.max(maxCaptures, m.getCaptured().size());
        }

        List<Move> best = new ArrayList<>();
        for (Move m : moves) {
            if (m.getCaptured().size() == maxCaptures) {
                best.add(m);
            }
        }

        return best.get(rand.nextInt(best.size()));
    }
}
