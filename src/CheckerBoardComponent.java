import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckerBoardComponent extends JComponent {

    CheckerBoard cb;
    final int NSQUARES = 8;
    private BoardClickListener clickListener;

    public interface BoardClickListener {
        void onSquareClicked(int col, int row);
    }

    public CheckerBoardComponent() {
        cb = new CheckerBoard(NSQUARES, 80);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickListener == null) return;
                int squareSize = Math.min(getWidth(), getHeight()) / NSQUARES;
                if (squareSize <= 0) return;
                int col = e.getX() / squareSize;
                int row = e.getY() / squareSize;
                if (col >= 0 && col < NSQUARES && row >= 0 && row < NSQUARES) {
                    clickListener.onSquareClicked(col, row);
                }
            }
        });
    }

    public void setClickListener(BoardClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int size = Math.min(getWidth(), getHeight()) / NSQUARES;
        cb.setSize(size);
        cb.draw(g2);
    }

    public CheckerBoard getCheckerBoard() {
        return cb;
    }

    public void setCheckerBoard(CheckerBoard board) {
        this.cb = board;
    }
}
