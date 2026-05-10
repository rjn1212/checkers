import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.Point;

public class GameRules {

    public static class Move {
        private final int fromX, fromY;
        private final List<int[]> steps;
        private final List<int[]> captured;
        private final boolean becomesKing;

        public Move(int fromX, int fromY, List<int[]> steps, List<int[]> captured, boolean becomesKing) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.steps = steps;
            this.captured = captured;
            this.becomesKing = becomesKing;
        }

        public int getFromX() { return fromX; }
        public int getFromY() { return fromY; }
        public List<int[]> getSteps() { return steps; }
        public List<int[]> getCaptured() { return captured; }
        public boolean isJump() { return !captured.isEmpty(); }
        public boolean becomesKing() { return becomesKing; }

        public int getToX() { return steps.get(steps.size() - 1)[0]; }
        public int getToY() { return steps.get(steps.size() - 1)[1]; }

        @Override
        public String toString() {
            return "Move(" + fromX + "," + fromY + " -> " + getToX() + "," + getToY() +
                    " captures=" + captured.size() + " king=" + becomesKing + ")";
        }
    }

    public static List<Move> getLegalMoves(CheckerBoard board, int playerNum) {
        List<Move> allMoves = new ArrayList<>();
        List<Move> jumpMoves = new ArrayList<>();

        for (Checker c : board.getCheckers()) {
            if (c.getPlayerNumber() == playerNum) {
                List<Move> moves = getMovesForPiece(board, c);
                for (Move m : moves) {
                    if (m.isJump()) {
                        jumpMoves.add(m);
                    }
                    allMoves.add(m);
                }
            }
        }

        if (!jumpMoves.isEmpty()) {
            return jumpMoves;
        }
        return allMoves;
    }

    public static List<Move> getMovesForPiece(CheckerBoard board, Checker piece) {
        List<Move> moves = new ArrayList<>();

        List<Move> jumps = getJumpSequences(board, piece, piece.getxLoc(), piece.getyLoc(),
                new HashSet<>(), new ArrayList<>(), new ArrayList<>(), piece.isKing());
        if (!jumps.isEmpty()) {
            moves.addAll(jumps);
        } else {
            moves.addAll(getSimpleMoves(board, piece));
        }

        return moves;
    }

    public static void applyMove(CheckerBoard board, Move move) {
        Checker piece = board.getCheckerAt(move.getFromX(), move.getFromY());
        if (piece == null) return;

        for (int[] cap : move.getCaptured()) {
            Checker captured = board.getCheckerAt(cap[0], cap[1]);
            if (captured != null) {
                board.removeChecker(captured);
            }
        }

        board.moveChecker(piece, move.getToX(), move.getToY());

        if (move.becomesKing()) {
            piece.setKing(true);
        }
    }

    public static CheckerBoard applyMoveOnCopy(CheckerBoard board, Move move) {
        CheckerBoard copy = board.deepCopy();

        Checker piece = copy.getCheckerAt(move.getFromX(), move.getFromY());
        if (piece == null) return copy;

        for (int[] cap : move.getCaptured()) {
            Checker captured = copy.getCheckerAt(cap[0], cap[1]);
            if (captured != null) {
                copy.removeChecker(captured);
            }
        }

        copy.moveChecker(piece, move.getToX(), move.getToY());

        if (move.becomesKing()) {
            piece.setKing(true);
        }

        return copy;
    }

    public static boolean isGameOver(CheckerBoard board, int currentPlayer) {
        return getLegalMoves(board, currentPlayer).isEmpty();
    }

    private static int[][] getDirections(Checker piece) {
        if (piece.isKing()) {
            return new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        }
        if (piece.getPlayerNumber() == 0) {
            return new int[][]{{-1, 1}, {1, 1}};
        } else {
            return new int[][]{{-1, -1}, {1, -1}};
        }
    }

    private static boolean wouldBecomeKing(Checker piece, int toY) {
        if (piece.isKing()) return false;
        if (piece.getPlayerNumber() == 0 && toY == 7) return true;
        if (piece.getPlayerNumber() == 1 && toY == 0) return true;
        return false;
    }

    private static List<Move> getSimpleMoves(CheckerBoard board, Checker piece) {
        List<Move> moves = new ArrayList<>();
        int x = piece.getxLoc();
        int y = piece.getyLoc();

        for (int[] dir : getDirections(piece)) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8 && board.getCheckerAt(nx, ny) == null) {
                List<int[]> steps = new ArrayList<>();
                steps.add(new int[]{nx, ny});
                boolean kings = wouldBecomeKing(piece, ny);
                moves.add(new Move(x, y, steps, new ArrayList<>(), kings));
            }
        }
        return moves;
    }

    private static List<Move> getJumpSequences(CheckerBoard board, Checker piece,
                                                int x, int y, Set<Point> alreadyCaptured,
                                                List<int[]> stepsAcc, List<int[]> capturedAcc,
                                                boolean isKing) {
        List<Move> results = new ArrayList<>();

        int[][] dirs = isKing
                ? new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}
                : getDirections(piece);

        for (int[] dir : dirs) {
            int mx = x + dir[0];
            int my = y + dir[1];
            int lx = x + 2 * dir[0];
            int ly = y + 2 * dir[1];

            if (lx < 0 || lx >= 8 || ly < 0 || ly >= 8) continue;

            Checker middle = board.getCheckerAt(mx, my);
            if (middle == null) continue;
            if (middle.getPlayerNumber() == piece.getPlayerNumber()) continue;

            Point midPt = new Point(mx, my);
            if (alreadyCaptured.contains(midPt)) continue;

            Checker landing = board.getCheckerAt(lx, ly);
            if (landing != null && !(lx == piece.getxLoc() && ly == piece.getyLoc())) continue;

            List<int[]> newSteps = new ArrayList<>(stepsAcc);
            newSteps.add(new int[]{lx, ly});
            List<int[]> newCaptured = new ArrayList<>(capturedAcc);
            newCaptured.add(new int[]{mx, my});
            Set<Point> newAlready = new HashSet<>(alreadyCaptured);
            newAlready.add(midPt);

            boolean promotesHere = wouldBecomeKing(piece, ly);
            boolean kingAfterThis = isKing || promotesHere;

            if (promotesHere && !isKing) {
                // Promotion ends the turn in standard rules
                results.add(new Move(piece.getxLoc(), piece.getyLoc(), newSteps, newCaptured, true));
            } else {
                List<Move> continuations = getJumpSequences(board, piece, lx, ly,
                        newAlready, newSteps, newCaptured, kingAfterThis);

                if (continuations.isEmpty()) {
                    results.add(new Move(piece.getxLoc(), piece.getyLoc(), newSteps, newCaptured,
                            wouldBecomeKing(piece, ly)));
                } else {
                    results.addAll(continuations);
                }
            }
        }

        return results;
    }

    public static Set<Point> getDestinations(List<Move> moves) {
        Set<Point> destinations = new HashSet<>();
        for (Move m : moves) {
            destinations.add(new Point(m.getToX(), m.getToY()));
        }
        return destinations;
    }

    public static Move findMoveByDestination(List<Move> moves, int toX, int toY) {
        Move best = null;
        for (Move m : moves) {
            if (m.getToX() == toX && m.getToY() == toY) {
                if (best == null || m.getCaptured().size() > best.getCaptured().size()) {
                    best = m;
                }
            }
        }
        return best;
    }
}
