package com.gitgames.checkers.service;

import com.gitgames.checkers.ai.AiStrategy;
import com.gitgames.checkers.ai.EasyAiStrategy;
import com.gitgames.checkers.ai.HardAiStrategy;
import com.gitgames.checkers.engine.GameEngine;
import com.gitgames.checkers.engine.MoveValidator;
import com.gitgames.checkers.engine.WinConditionChecker;
import com.gitgames.checkers.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameEngine gameEngine;
    private final MoveValidator moveValidator;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.moveValidator = new MoveValidator();
        WinConditionChecker winChecker = new WinConditionChecker(moveValidator);
        this.gameEngine = new GameEngine(moveValidator, winChecker);
    }

    public Game createGame(AiDifficulty difficulty, Player playerSide) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Game game = new Game(id, difficulty, playerSide);

        if (game.isAiTurn()) {
            makeAiMove(game);
        }

        gameRepository.save(game);
        return game;
    }

    public Game getGame(String id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    public List<Move> getLegalMoves(String id, Integer col, Integer row) {
        Game game = getGame(id);
        if (game.getPhase() == GamePhase.GAME_OVER) {
            return List.of();
        }

        if (col != null && row != null) {
            Piece piece = game.getBoard().getPieceAt(col, row);
            if (piece == null || piece.getOwner() != game.getCurrentTurn()) {
                return List.of();
            }
            List<Move> allLegal = gameEngine.getLegalMoves(game.getBoard(), game.getCurrentTurn());
            boolean mustJump = !allLegal.isEmpty() && allLegal.get(0).isJump();
            List<Move> pieceMoves = gameEngine.getMovesForPiece(game.getBoard(), piece);
            if (mustJump) {
                return pieceMoves.stream().filter(Move::isJump).toList();
            }
            return pieceMoves;
        }

        return gameEngine.getLegalMoves(game.getBoard(), game.getCurrentTurn());
    }

    public MoveResult makeMove(String id, Position from, Position to) {
        Game game = getGame(id);

        if (game.getPhase() == GamePhase.GAME_OVER) {
            throw new InvalidMoveException("Game is already over");
        }

        if (game.isAiTurn()) {
            throw new InvalidMoveException("It is not your turn");
        }

        List<Move> legalMoves = gameEngine.getLegalMoves(game.getBoard(), game.getCurrentTurn());
        Move move = gameEngine.findMove(legalMoves, from, to);
        if (move == null) {
            throw new InvalidMoveException(
                    "No legal move from (" + from.col() + "," + from.row() + ") to (" + to.col() + "," + to.row() + ")");
        }

        gameEngine.applyMove(game.getBoard(), move);
        game.recordMove(game.getCurrentTurn(), move);
        game.addCaptured(game.getCurrentTurn(), move.getCaptured().size());

        Player humanPlayer = game.getCurrentTurn();
        game.setCurrentTurn(game.getCurrentTurn().opponent());

        if (gameEngine.isGameOver(game.getBoard(), game.getCurrentTurn())) {
            game.setPhase(GamePhase.GAME_OVER);
            game.setWinner(humanPlayer);
            game.setWinReason("No legal moves remaining");
            gameRepository.save(game);
            return new MoveResult(move, null, game);
        }

        Move aiMove = null;
        if (game.isAiTurn()) {
            aiMove = makeAiMove(game);
        }

        gameRepository.save(game);
        return new MoveResult(move, aiMove, game);
    }

    public void abandonGame(String id) {
        gameRepository.deleteById(id);
    }

    private Move makeAiMove(Game game) {
        AiStrategy strategy = createAiStrategy(game.getAiDifficulty());
        Move aiMove = strategy.selectMove(game.getBoard(), game.getCurrentTurn());
        if (aiMove == null) {
            game.setPhase(GamePhase.GAME_OVER);
            game.setWinner(game.getHumanSide());
            game.setWinReason("AI has no legal moves");
            return null;
        }

        gameEngine.applyMove(game.getBoard(), aiMove);
        game.recordMove(game.getCurrentTurn(), aiMove);
        game.addCaptured(game.getCurrentTurn(), aiMove.getCaptured().size());
        game.setCurrentTurn(game.getCurrentTurn().opponent());

        if (gameEngine.isGameOver(game.getBoard(), game.getCurrentTurn())) {
            game.setPhase(GamePhase.GAME_OVER);
            game.setWinner(game.getAiSide());
            game.setWinReason("No legal moves remaining");
        }

        return aiMove;
    }

    private AiStrategy createAiStrategy(AiDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new EasyAiStrategy(moveValidator);
            case HARD -> new HardAiStrategy(moveValidator);
        };
    }

    public record MoveResult(Move humanMove, Move aiMove, Game game) {}
}
