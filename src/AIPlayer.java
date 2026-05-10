import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {

    public enum Difficulty { EASY, HARD }

    private static final int MAX_DEPTH = 8;
    private final int playerNum;
    private final int opponentNum;
    private final Difficulty difficulty;
    private final Random rand = new Random();

    public AIPlayer(int playerNum, Difficulty difficulty) {
        this.playerNum = playerNum;
        this.opponentNum = 1 - playerNum;
        this.difficulty = difficulty;
    }

    public GameRules.Move selectMove(CheckerBoard board) {
        List<GameRules.Move> moves = GameRules.getLegalMoves(board, playerNum);
        if (moves.isEmpty()) return null;

        if (difficulty == Difficulty.EASY) {
            return selectEasy(board, moves);
        } else {
            return selectHard(board, moves);
        }
    }

    private GameRules.Move selectEasy(CheckerBoard board, List<GameRules.Move> moves) {
        int maxCaptures = 0;
        for (GameRules.Move m : moves) {
            maxCaptures = Math.max(maxCaptures, m.getCaptured().size());
        }

        List<GameRules.Move> best = new ArrayList<>();
        for (GameRules.Move m : moves) {
            if (m.getCaptured().size() == maxCaptures) {
                best.add(m);
            }
        }

        return best.get(rand.nextInt(best.size()));
    }

    private GameRules.Move selectHard(CheckerBoard board, List<GameRules.Move> moves) {
        GameRules.Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        List<GameRules.Move> bestMoves = new ArrayList<>();

        for (GameRules.Move move : moves) {
            CheckerBoard newBoard = GameRules.applyMoveOnCopy(board, move);
            int score = minimax(newBoard, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(rand.nextInt(bestMoves.size()));
    }

    private int minimax(CheckerBoard board, int depth, int alpha, int beta, boolean maximizing) {
        int current = maximizing ? playerNum : opponentNum;

        if (depth == 0 || GameRules.isGameOver(board, current)) {
            return evaluate(board);
        }

        List<GameRules.Move> moves = GameRules.getLegalMoves(board, current);

        if (moves.isEmpty()) {
            return maximizing ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        }

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (GameRules.Move move : moves) {
                CheckerBoard newBoard = GameRules.applyMoveOnCopy(board, move);
                int eval = minimax(newBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (GameRules.Move move : moves) {
                CheckerBoard newBoard = GameRules.applyMoveOnCopy(board, move);
                int eval = minimax(newBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluate(CheckerBoard board) {
        int score = 0;
        for (Checker c : board.getCheckers()) {
            int value = 0;
            int pieceVal = c.isKing() ? 5 : 2;
            value += pieceVal;

            // Center columns are more mobile
            int col = c.getxLoc();
            if (col >= 2 && col <= 5) value += 1;

            // Advancement bonus for non-kings
            if (!c.isKing()) {
                if (c.getPlayerNumber() == 0) {
                    value += c.getyLoc(); // closer to row 7 = better
                } else {
                    value += 7 - c.getyLoc(); // closer to row 0 = better
                }
            }

            // Back row protection
            if (c.getPlayerNumber() == 0 && c.getyLoc() == 0) value += 1;
            if (c.getPlayerNumber() == 1 && c.getyLoc() == 7) value += 1;

            if (c.getPlayerNumber() == playerNum) {
                score += value;
            } else {
                score -= value;
            }
        }
        return score;
    }
}
