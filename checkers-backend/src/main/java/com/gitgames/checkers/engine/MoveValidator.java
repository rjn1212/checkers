package com.gitgames.checkers.engine;

import com.gitgames.checkers.model.*;

import java.util.*;

public class MoveValidator {

    public List<Move> getLegalMoves(Board board, Player player) {
        List<Move> allMoves = new ArrayList<>();
        List<Move> jumpMoves = new ArrayList<>();

        for (Piece piece : board.getPiecesForPlayer(player)) {
            List<Move> moves = getMovesForPiece(board, piece);
            for (Move m : moves) {
                if (m.isJump()) {
                    jumpMoves.add(m);
                }
                allMoves.add(m);
            }
        }

        if (!jumpMoves.isEmpty()) {
            return jumpMoves;
        }
        return allMoves;
    }

    public List<Move> getMovesForPiece(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();

        List<Move> jumps = getJumpSequences(board, piece, piece.getCol(), piece.getRow(),
                new HashSet<>(), new ArrayList<>(), new ArrayList<>(), piece.isKing());
        if (!jumps.isEmpty()) {
            moves.addAll(jumps);
        } else {
            moves.addAll(getSimpleMoves(board, piece));
        }

        return moves;
    }

    public Move findMoveByDestination(List<Move> moves, int toCol, int toRow) {
        Move best = null;
        for (Move m : moves) {
            if (m.getTo().col() == toCol && m.getTo().row() == toRow) {
                if (best == null || m.getCaptured().size() > best.getCaptured().size()) {
                    best = m;
                }
            }
        }
        return best;
    }

    private int[][] getDirections(Piece piece) {
        if (piece.isKing()) {
            return new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        }
        if (piece.getOwner() == Player.BLACK) {
            return new int[][]{{-1, 1}, {1, 1}};
        } else {
            return new int[][]{{-1, -1}, {1, -1}};
        }
    }

    private boolean wouldBecomeKing(Piece piece, int toRow) {
        if (piece.isKing()) return false;
        if (piece.getOwner() == Player.BLACK && toRow == 7) return true;
        if (piece.getOwner() == Player.RED && toRow == 0) return true;
        return false;
    }

    private List<Move> getSimpleMoves(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();
        int col = piece.getCol();
        int row = piece.getRow();

        for (int[] dir : getDirections(piece)) {
            int nc = col + dir[0];
            int nr = row + dir[1];
            if (nc >= 0 && nc < 8 && nr >= 0 && nr < 8 && board.getPieceAt(nc, nr) == null) {
                List<Position> steps = new ArrayList<>();
                steps.add(new Position(nc, nr));
                boolean kings = wouldBecomeKing(piece, nr);
                moves.add(new Move(new Position(col, row), steps, new ArrayList<>(), kings));
            }
        }
        return moves;
    }

    private List<Move> getJumpSequences(Board board, Piece piece,
                                         int col, int row, Set<Position> alreadyCaptured,
                                         List<Position> stepsAcc, List<Position> capturedAcc,
                                         boolean isKing) {
        List<Move> results = new ArrayList<>();

        int[][] dirs = isKing
                ? new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}
                : getDirections(piece);

        for (int[] dir : dirs) {
            int mc = col + dir[0];
            int mr = row + dir[1];
            int lc = col + 2 * dir[0];
            int lr = row + 2 * dir[1];

            if (lc < 0 || lc >= 8 || lr < 0 || lr >= 8) continue;

            Piece middle = board.getPieceAt(mc, mr);
            if (middle == null) continue;
            if (middle.getOwner() == piece.getOwner()) continue;

            Position midPos = new Position(mc, mr);
            if (alreadyCaptured.contains(midPos)) continue;

            Piece landing = board.getPieceAt(lc, lr);
            if (landing != null && !(lc == piece.getCol() && lr == piece.getRow())) continue;

            List<Position> newSteps = new ArrayList<>(stepsAcc);
            newSteps.add(new Position(lc, lr));
            List<Position> newCaptured = new ArrayList<>(capturedAcc);
            newCaptured.add(midPos);
            Set<Position> newAlready = new HashSet<>(alreadyCaptured);
            newAlready.add(midPos);

            boolean promotesHere = wouldBecomeKing(piece, lr);
            boolean kingAfterThis = isKing || promotesHere;

            if (promotesHere && !isKing) {
                results.add(new Move(piece.getPosition(), newSteps, newCaptured, true));
            } else {
                List<Move> continuations = getJumpSequences(board, piece, lc, lr,
                        newAlready, newSteps, newCaptured, kingAfterThis);

                if (continuations.isEmpty()) {
                    results.add(new Move(piece.getPosition(), newSteps, newCaptured,
                            wouldBecomeKing(piece, lr)));
                } else {
                    results.addAll(continuations);
                }
            }
        }

        return results;
    }
}
