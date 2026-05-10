package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Piece;
import com.gitgames.checkers.model.Player;

public record PieceDto(
    String id,
    Player owner,
    int col,
    int row,
    boolean king
) {
    public static PieceDto from(Piece piece) {
        return new PieceDto(piece.getId(), piece.getOwner(), piece.getCol(), piece.getRow(), piece.isKing());
    }
}
