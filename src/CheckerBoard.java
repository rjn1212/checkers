import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CheckerBoard {
    private final int numOfSquares;
    private int size;
    private ArrayList<ArrayList<Checker>> board;
    private final ArrayList<Checker> checkers = new ArrayList<>();

    private Set<Point> highlights = Collections.emptySet();
    private Point selectedSquare = null;

    public CheckerBoard(int numOfSquares, int size) {
        this.numOfSquares = numOfSquares;
        this.size = size;
        fillBoard();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumOfSquares() {
        return numOfSquares;
    }

    public ArrayList<Checker> getCheckers() {
        return new ArrayList<>(checkers);
    }

    public void setHighlights(Set<Point> highlights) {
        this.highlights = (highlights != null) ? highlights : Collections.emptySet();
    }

    public void setSelectedSquare(Point selected) {
        this.selectedSquare = selected;
    }

    public void addChecker(Checker checker) {
        checkers.add(checker);
        board.get(checker.getyLoc()).set(checker.getxLoc(), checker);
    }

    public Checker getCheckerAt(int x, int y) {
        if (x < numOfSquares && y < numOfSquares && x >= 0 && y >= 0)
            return board.get(y).get(x);
        else
            return null;
    }

    public boolean isCheckerAt(int x, int y) {
        return getCheckerAt(x, y) != null;
    }

    public void removeChecker(Checker checker) {
        board.get(checker.getyLoc()).set(checker.getxLoc(), null);
        checkers.remove(checker);
    }

    public void moveChecker(Checker checker, int newX, int newY) {
        if (checker != null && newX < numOfSquares && newY < numOfSquares && newX >= 0 && newY >= 0) {
            board.get(checker.getyLoc()).set(checker.getxLoc(), null);
            board.get(newY).set(newX, checker);
            checker.setxLoc(newX);
            checker.setyLoc(newY);
        }
    }

    public CheckerBoard deepCopy() {
        CheckerBoard copy = new CheckerBoard(numOfSquares, size);
        for (Checker c : checkers) {
            Checker cloned = c.clone();
            copy.addChecker(cloned);
        }
        return copy;
    }

    private void fillBoard() {
        board = new ArrayList<>(numOfSquares);
        for (int y = 0; y < numOfSquares; y++) {
            ArrayList<Checker> row = new ArrayList<>(numOfSquares);
            for (int x = 0; x < numOfSquares; x++) {
                row.add(null);
            }
            board.add(row);
        }
    }

    private void drawCheckers(Graphics2D g2) {
        for (Checker checker : checkers) {
            checker.setSize(this.getSize());
            checker.draw(g2);
        }
    }

    public void draw(Graphics2D g2) {
        for (int y = 0; y < numOfSquares; y++) {
            for (int x = 0; x < numOfSquares; x++) {
                if ((x + y) % 2 == 0)
                    g2.setColor(Color.WHITE);
                else
                    g2.setColor(Color.RED);

                g2.fillRect(x * size, y * size, size, size);

                if (selectedSquare != null && selectedSquare.x == x && selectedSquare.y == y) {
                    g2.setColor(new Color(0, 100, 255, 120));
                    g2.fillRect(x * size, y * size, size, size);
                }

                if (highlights.contains(new Point(x, y))) {
                    g2.setColor(new Color(0, 200, 0, 120));
                    g2.fillRect(x * size, y * size, size, size);
                }
            }
        }
        drawCheckers(g2);
    }
}
