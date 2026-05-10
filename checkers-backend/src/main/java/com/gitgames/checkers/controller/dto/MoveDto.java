package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Move;

import java.util.List;

public record MoveDto(
    PositionDto from,
    PositionDto to,
    List<PositionDto> steps,
    List<PositionDto> captured,
    boolean becomesKing
) {
    public static MoveDto from(Move move) {
        return new MoveDto(
            PositionDto.from(move.getFrom()),
            PositionDto.from(move.getTo()),
            move.getSteps().stream().map(PositionDto::from).toList(),
            move.getCaptured().stream().map(PositionDto::from).toList(),
            move.isBecomesKing()
        );
    }
}
