package com.gitgames.checkers.model;

import java.time.Instant;
import java.util.List;

public record MoveRecord(
    Player player,
    Position from,
    Position to,
    List<Position> captured,
    boolean becomesKing,
    Instant timestamp
) {
    public static MoveRecord from(Player player, Move move) {
        return new MoveRecord(
            player,
            move.getFrom(),
            move.getTo(),
            move.getCaptured(),
            move.isBecomesKing(),
            Instant.now()
        );
    }
}
