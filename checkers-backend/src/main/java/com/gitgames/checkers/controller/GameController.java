package com.gitgames.checkers.controller;

import com.gitgames.checkers.controller.dto.*;
import com.gitgames.checkers.model.Game;
import com.gitgames.checkers.model.Move;
import com.gitgames.checkers.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse createGame(@RequestBody CreateGameRequest request) {
        Game game = gameService.createGame(request.aiDifficulty(), request.playerSide());
        return GameStateResponse.from(game);
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable String id) {
        Game game = gameService.getGame(id);
        return GameStateResponse.from(game);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abandonGame(@PathVariable String id) {
        gameService.abandonGame(id);
    }

    @GetMapping("/{id}/moves")
    public LegalMovesResponse getLegalMoves(
            @PathVariable String id,
            @RequestParam(required = false) Integer col,
            @RequestParam(required = false) Integer row) {
        List<Move> moves = gameService.getLegalMoves(id, col, row);
        return LegalMovesResponse.from(moves);
    }

    @PostMapping("/{id}/moves")
    public MoveResponse makeMove(@PathVariable String id, @RequestBody MakeMoveRequest request) {
        GameService.MoveResult result = gameService.makeMove(
                id, request.from().toModel(), request.to().toModel());
        return MoveResponse.from(result);
    }
}
