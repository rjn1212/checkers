package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Move;

import java.util.List;

public record LegalMovesResponse(
    List<MoveDto> moves,
    boolean mustJump
) {
    public static LegalMovesResponse from(List<Move> moves) {
        boolean mustJump = !moves.isEmpty() && moves.get(0).isJump();
        return new LegalMovesResponse(
            moves.stream().map(MoveDto::from).toList(),
            mustJump
        );
    }
}
