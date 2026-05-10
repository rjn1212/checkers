package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Board;

import java.util.List;

public record BoardDto(List<PieceDto> pieces) {
    public static BoardDto from(Board board) {
        return new BoardDto(board.getPieces().stream().map(PieceDto::from).toList());
    }
}
