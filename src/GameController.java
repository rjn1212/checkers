import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class GameController implements CheckerBoardComponent.BoardClickListener {

    private enum GameState {
        WAITING_FOR_SELECTION, WAITING_FOR_DESTINATION, AI_THINKING, GAME_OVER
    }

    private final CheckerBoardComponent component;
    private final JLabel statusLabel;
    private final JFrame frame;
    private CheckerBoard board;
    private int humanPlayer;
    private int aiPlayerNum;
    private int currentPlayer;
    private AIPlayer aiPlayer;
    private GameState state;

    private Checker selectedPiece;
    private List<GameRules.Move> selectedMoves;

    public GameController(CheckerBoardComponent component, JLabel statusLabel, JFrame frame) {
        this.component = component;
        this.statusLabel = statusLabel;
        this.frame = frame;
        this.board = component.getCheckerBoard();
        component.setClickListener(this);
    }

    public void startGame() {
        Object[] sides = {"Black (go first)", "Red"};
        int sideChoice = JOptionPane.showOptionDialog(frame, "Which side do you want to play?",
                "Choose Side", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, sides, sides[0]);
        if (sideChoice < 0) sideChoice = 0;
        humanPlayer = sideChoice;
        aiPlayerNum = 1 - humanPlayer;

        Object[] difficulties = {"Easy", "Hard"};
        int diffChoice = JOptionPane.showOptionDialog(frame, "Select AI difficulty:",
                "Difficulty", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, difficulties, difficulties[0]);
        AIPlayer.Difficulty diff = (diffChoice == 1) ? AIPlayer.Difficulty.HARD : AIPlayer.Difficulty.EASY;
        aiPlayer = new AIPlayer(aiPlayerNum, diff);

        setupPieces();
        currentPlayer = 0;
        component.repaint();

        if (humanPlayer == currentPlayer) {
            beginHumanTurn();
        } else {
            doAiTurn();
        }
    }

    private void setupPieces() {
        for (int i = 0; i < 12; i++) {
            int step = ((i / 4) + 1) % 2;
            Checker black = new Checker(((i % 4) * 2) + step, i / 4, "B" + i, 0);
            board.addChecker(black);

            Checker red = new Checker(7 - ((i % 4) * 2 + step), 7 - (i / 4), "R" + i, 1);
            board.addChecker(red);
        }
    }

    private void beginHumanTurn() {
        state = GameState.WAITING_FOR_SELECTION;
        selectedPiece = null;
        selectedMoves = null;
        board.setHighlights(null);
        board.setSelectedSquare(null);
        component.repaint();

        List<GameRules.Move> legalMoves = GameRules.getLegalMoves(board, humanPlayer);
        if (legalMoves.isEmpty()) {
            endGame(aiPlayerNum);
            return;
        }

        boolean mustJump = legalMoves.get(0).isJump();
        if (mustJump) {
            statusLabel.setText("Your turn - you must jump!");
        } else {
            statusLabel.setText("Your turn - select a piece to move");
        }
    }

    @Override
    public void onSquareClicked(int col, int row) {
        if (state == GameState.AI_THINKING || state == GameState.GAME_OVER) return;

        if (state == GameState.WAITING_FOR_SELECTION) {
            handleSelection(col, row);
        } else if (state == GameState.WAITING_FOR_DESTINATION) {
            handleDestination(col, row);
        }
    }

    private void handleSelection(int col, int row) {
        Checker piece = board.getCheckerAt(col, row);
        if (piece == null || piece.getPlayerNumber() != humanPlayer) return;

        List<GameRules.Move> allLegal = GameRules.getLegalMoves(board, humanPlayer);
        boolean mustJump = !allLegal.isEmpty() && allLegal.get(0).isJump();

        List<GameRules.Move> pieceMoves = GameRules.getMovesForPiece(board, piece);

        if (mustJump) {
            boolean hasJumps = false;
            for (GameRules.Move m : pieceMoves) {
                if (m.isJump()) { hasJumps = true; break; }
            }
            if (!hasJumps) {
                statusLabel.setText("You must jump! Select a piece that can jump.");
                return;
            }
            // Filter to only jump moves
            pieceMoves.removeIf(m -> !m.isJump());
        }

        if (pieceMoves.isEmpty()) {
            statusLabel.setText("That piece has no legal moves.");
            return;
        }

        selectedPiece = piece;
        selectedMoves = pieceMoves;
        state = GameState.WAITING_FOR_DESTINATION;

        Set<Point> dests = GameRules.getDestinations(pieceMoves);
        board.setSelectedSquare(new Point(col, row));
        board.setHighlights(dests);
        component.repaint();

        statusLabel.setText("Click a highlighted square to move");
    }

    private void handleDestination(int col, int row) {
        // Allow re-selecting a different piece
        Checker clickedPiece = board.getCheckerAt(col, row);
        if (clickedPiece != null && clickedPiece.getPlayerNumber() == humanPlayer && clickedPiece != selectedPiece) {
            board.setHighlights(null);
            board.setSelectedSquare(null);
            state = GameState.WAITING_FOR_SELECTION;
            handleSelection(col, row);
            return;
        }

        GameRules.Move move = GameRules.findMoveByDestination(selectedMoves, col, row);
        if (move == null) return;

        board.setHighlights(null);
        board.setSelectedSquare(null);
        GameRules.applyMove(board, move);
        component.repaint();

        if (GameRules.isGameOver(board, aiPlayerNum)) {
            endGame(humanPlayer);
            return;
        }

        currentPlayer = aiPlayerNum;
        doAiTurn();
    }

    private void doAiTurn() {
        state = GameState.AI_THINKING;
        statusLabel.setText("AI is thinking...");

        SwingWorker<GameRules.Move, Void> worker = new SwingWorker<>() {
            @Override
            protected GameRules.Move doInBackground() {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                return aiPlayer.selectMove(board);
            }

            @Override
            protected void done() {
                try {
                    GameRules.Move move = get();
                    if (move == null) {
                        endGame(humanPlayer);
                        return;
                    }

                    // Briefly highlight the AI's move
                    board.setSelectedSquare(new Point(move.getFromX(), move.getFromY()));
                    java.util.Set<Point> dest = new java.util.HashSet<>();
                    dest.add(new Point(move.getToX(), move.getToY()));
                    board.setHighlights(dest);
                    component.repaint();

                    Timer timer = new Timer(400, e -> {
                        GameRules.applyMove(board, move);
                        board.setHighlights(null);
                        board.setSelectedSquare(null);
                        component.repaint();

                        if (GameRules.isGameOver(board, humanPlayer)) {
                            endGame(aiPlayerNum);
                            return;
                        }

                        currentPlayer = humanPlayer;
                        beginHumanTurn();
                    });
                    timer.setRepeats(false);
                    timer.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void endGame(int winner) {
        state = GameState.GAME_OVER;
        String msg = (winner == humanPlayer) ? "You win!" : "You lose!";
        statusLabel.setText(msg);

        int choice = JOptionPane.showOptionDialog(frame,
                msg + " Play again?", "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"New Game", "Quit"}, "New Game");

        if (choice == 0) {
            resetAndStart();
        } else {
            System.exit(0);
        }
    }

    private void resetAndStart() {
        board = new CheckerBoard(8, board.getSize());
        component.setCheckerBoard(board);
        startGame();
    }
}
