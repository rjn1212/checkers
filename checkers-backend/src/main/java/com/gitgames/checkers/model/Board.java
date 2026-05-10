package com.gitgames.checkers.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] grid = new Piece[8][8];
    private final List<Piece> pieces = new ArrayList<>();

    public void addPiece(Piece piece) {
        pieces.add(piece);
        grid[piece.getRow()][piece.getCol()] = piece;
    }

    public void removePiece(Piece piece) {
        grid[piece.getRow()][piece.getCol()] = null;
        pieces.remove(piece);
    }

    public void movePiece(Piece piece, int col, int row) {
        grid[piece.getRow()][piece.getCol()] = null;
        piece.setPosition(col, row);
        grid[row][col] = piece;
    }

    public Piece getPieceAt(int col, int row) {
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return null;
        return grid[row][col];
    }

    public List<Piece> getPieces() {
        return new ArrayList<>(pieces);
    }

    public List<Piece> getPiecesForPlayer(Player player) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getOwner() == player) result.add(p);
        }
        return result;
    }

    public Board deepCopy() {
        Board copy = new Board();
        for (Piece p : pieces) {
            copy.addPiece(p.clone());
        }
        return copy;
    }

    public static Board createInitialBoard() {
        Board board = new Board();
        int blackId = 0;
        int redId = 0;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((col + row) % 2 == 1) {
                    board.addPiece(new Piece("B" + blackId++, Player.BLACK, col, row));
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((col + row) % 2 == 1) {
                    board.addPiece(new Piece("R" + redId++, Player.RED, col, row));
                }
            }
        }

        return board;
    }
}
