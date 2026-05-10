package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Game;
import com.gitgames.checkers.model.GamePhase;
import com.gitgames.checkers.model.Move;
import com.gitgames.checkers.model.Player;
import com.gitgames.checkers.service.GameService;

import java.util.Map;

public record MoveResponse(
    MoveDto humanMove,
    MoveDto aiMove,
    BoardDto board,
    GamePhase phase,
    Player currentTurn,
    Map<Player, Integer> capturedCount,
    Player winner,
    String winReason
) {
    public static MoveResponse from(GameService.MoveResult result) {
        Game game = result.game();
        return new MoveResponse(
            MoveDto.from(result.humanMove()),
            result.aiMove() != null ? MoveDto.from(result.aiMove()) : null,
            BoardDto.from(game.getBoard()),
            game.getPhase(),
            game.getCurrentTurn(),
            game.getCapturedCount(),
            game.getWinner(),
            game.getWinReason()
        );
    }
}
