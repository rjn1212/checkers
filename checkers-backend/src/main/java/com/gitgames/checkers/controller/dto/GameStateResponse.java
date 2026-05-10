package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.Game;
import com.gitgames.checkers.model.GamePhase;
import com.gitgames.checkers.model.Player;

import java.util.Map;

public record GameStateResponse(
    String gameId,
    GamePhase phase,
    Player currentTurn,
    Player humanSide,
    BoardDto board,
    Map<Player, Integer> capturedCount,
    Player winner,
    String winReason
) {
    public static GameStateResponse from(Game game) {
        return new GameStateResponse(
            game.getId(),
            game.getPhase(),
            game.getCurrentTurn(),
            game.getHumanSide(),
            BoardDto.from(game.getBoard()),
            game.getCapturedCount(),
            game.getWinner(),
            game.getWinReason()
        );
    }
}
