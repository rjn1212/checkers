import javax.swing.*;
import java.awt.*;

public class CheckerBoardViewer {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Checkers");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(660, 700);
            frame.setLayout(new BorderLayout());

            CheckerBoardComponent component = new CheckerBoardComponent();
            frame.add(component, BorderLayout.CENTER);

            JLabel statusLabel = new JLabel("Welcome to Checkers");
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            frame.add(statusLabel, BorderLayout.SOUTH);

            frame.setVisible(true);

            GameController controller = new GameController(component, statusLabel, frame);
            controller.startGame();
        });
    }
}
