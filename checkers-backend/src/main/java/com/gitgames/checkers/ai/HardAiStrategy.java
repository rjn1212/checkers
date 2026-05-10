package com.gitgames.checkers.ai;

import com.gitgames.checkers.engine.GameEngine;
import com.gitgames.checkers.engine.MoveValidator;
import com.gitgames.checkers.engine.WinConditionChecker;
import com.gitgames.checkers.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HardAiStrategy implements AiStrategy {

    private static final int MAX_DEPTH = 8;
    private final GameEngine engine;
    private final MoveValidator moveValidator;
    private final Random rand = new Random();

    public HardAiStrategy(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
        this.engine = new GameEngine(moveValidator, new WinConditionChecker(moveValidator));
    }

    @Override
    public Move selectMove(Board board, Player player) {
        List<Move> moves = moveValidator.getLegalMoves(board, player);
        if (moves.isEmpty()) return null;

        int bestScore = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : moves) {
            Board copy = board.deepCopy();
            engine.applyMove(copy, move);
            int score = minimax(engine, copy, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    false, player);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(rand.nextInt(bestMoves.size()));
    }

    private int minimax(GameEngine engine, Board board, int depth, int alpha, int beta,
                        boolean maximizing, Player aiPlayer) {
        Player current = maximizing ? aiPlayer : aiPlayer.opponent();

        if (depth == 0 || engine.isGameOver(board, current)) {
            return evaluate(board, aiPlayer);
        }

        List<Move> moves = engine.getLegalMoves(board, current);

        if (moves.isEmpty()) {
            return maximizing ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        }

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = board.deepCopy();
                engine.applyMove(copy, move);
                int eval = minimax(engine, copy, depth - 1, alpha, beta, false, aiPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = board.deepCopy();
                engine.applyMove(copy, move);
                int eval = minimax(engine, copy, depth - 1, alpha, beta, true, aiPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluate(Board board, Player aiPlayer) {
        int score = 0;
        for (Piece piece : board.getPieces()) {
            int value = 0;
            int pieceVal = piece.isKing() ? 5 : 2;
            value += pieceVal;

            int col = piece.getCol();
            if (col >= 2 && col <= 5) value += 1;

            if (!piece.isKing()) {
                if (piece.getOwner() == Player.BLACK) {
                    value += piece.getRow();
                } else {
                    value += 7 - piece.getRow();
                }
            }

            if (piece.getOwner() == Player.BLACK && piece.getRow() == 0) value += 1;
            if (piece.getOwner() == Player.RED && piece.getRow() == 7) value += 1;

            if (piece.getOwner() == aiPlayer) {
                score += value;
            } else {
                score -= value;
            }
        }
        return score;
    }
}
