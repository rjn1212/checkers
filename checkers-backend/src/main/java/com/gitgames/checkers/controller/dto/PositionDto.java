package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Position;

public record PositionDto(int col, int row) {
    public Position toModel() {
        return new Position(col, row);
    }

    public static PositionDto from(Position pos) {
        return new PositionDto(pos.col(), pos.row());
    }
}
