package com.gitgames.checkers.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Game {
    private final String id;
    private final Board board;
    private Player currentTurn;
    private GamePhase phase;
    private Player winner;
    private String winReason;
    private final AiDifficulty aiDifficulty;
    private final Player humanSide;
    private final List<MoveRecord> moveHistory = new ArrayList<>();
    private final Map<Player, String> platformUserIds = new EnumMap<>(Player.class);
    private final Map<Player, Integer> capturedCount = new EnumMap<>(Player.class);
    private final Instant createdAt;
    private Instant lastActivityAt;

    public Game(String id, AiDifficulty aiDifficulty, Player humanSide) {
        this.id = id;
        this.board = Board.createInitialBoard();
        this.currentTurn = Player.BLACK;
        this.phase = GamePhase.PLAYING;
        this.aiDifficulty = aiDifficulty;
        this.humanSide = humanSide;
        this.createdAt = Instant.now();
        this.lastActivityAt = this.createdAt;
        this.capturedCount.put(Player.BLACK, 0);
        this.capturedCount.put(Player.RED, 0);
    }

    public String getId() { return id; }
    public Board getBoard() { return board; }
    public Player getCurrentTurn() { return currentTurn; }
    public GamePhase getPhase() { return phase; }
    public Player getWinner() { return winner; }
    public String getWinReason() { return winReason; }
    public AiDifficulty getAiDifficulty() { return aiDifficulty; }
    public Player getHumanSide() { return humanSide; }
    public List<MoveRecord> getMoveHistory() { return moveHistory; }
    public Map<Player, String> getPlatformUserIds() { return platformUserIds; }
    public Map<Player, Integer> getCapturedCount() { return capturedCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }

    public void setCurrentTurn(Player currentTurn) { this.currentTurn = currentTurn; }
    public void setPhase(GamePhase phase) { this.phase = phase; }
    public void setWinner(Player winner) { this.winner = winner; }
    public void setWinReason(String winReason) { this.winReason = winReason; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public Player getAiSide() {
        return humanSide.opponent();
    }

    public boolean isAiTurn() {
        return currentTurn == getAiSide();
    }

    public void addCaptured(Player capturer, int count) {
        capturedCount.merge(capturer, count, Integer::sum);
    }

    public void recordMove(Player player, Move move) {
        moveHistory.add(MoveRecord.from(player, move));
        lastActivityAt = Instant.now();
    }
}
