package com.gitgames.checkers.engine;

import com.gitgames.checkers.model.*;

import java.util.List;

public class GameEngine {

    private final MoveValidator moveValidator;
    private final WinConditionChecker winConditionChecker;

    public GameEngine(MoveValidator moveValidator, WinConditionChecker winConditionChecker) {
        this.moveValidator = moveValidator;
        this.winConditionChecker = winConditionChecker;
    }

    public MoveValidator getMoveValidator() {
        return moveValidator;
    }

    public void applyMove(Board board, Move move) {
        Piece piece = board.getPieceAt(move.getFrom().col(), move.getFrom().row());
        if (piece == null) return;

        for (Position cap : move.getCaptured()) {
            Piece captured = board.getPieceAt(cap.col(), cap.row());
            if (captured != null) {
                board.removePiece(captured);
            }
        }

        board.movePiece(piece, move.getTo().col(), move.getTo().row());

        if (move.isBecomesKing()) {
            piece.setKing(true);
        }
    }

    public List<Move> getLegalMoves(Board board, Player player) {
        return moveValidator.getLegalMoves(board, player);
    }

    public List<Move> getMovesForPiece(Board board, Piece piece) {
        return moveValidator.getMovesForPiece(board, piece);
    }

    public boolean isGameOver(Board board, Player currentPlayer) {
        return winConditionChecker.isGameOver(board, currentPlayer);
    }

    public Move findMove(List<Move> legalMoves, Position from, Position to) {
        List<Move> movesFromPiece = legalMoves.stream()
                .filter(m -> m.getFrom().equals(from))
                .toList();
        return moveValidator.findMoveByDestination(movesFromPiece, to.col(), to.row());
    }
}
