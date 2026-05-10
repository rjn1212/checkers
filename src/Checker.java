import java.awt.*;

public class Checker implements Cloneable {

    private final int playerNumber;
    private int xLoc = 0;
    private int yLoc = 0;
    private Color color;
    private int size = 0;
    private String id = "";
    private boolean isKing = false;

    public Checker(int xLoc, int yLoc, int size, int playerNumber) {
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.playerNumber = playerNumber;
        this.color = getColorForPlayer(playerNumber);
        this.size = size;
    }

    public Checker(int xLoc, int yLoc, int playerNumber) {
        this(xLoc, yLoc, 30, playerNumber);
    }

    public Checker(int xLoc, int yLoc, String id, int playerNumber) {
        this(xLoc, yLoc, 30, playerNumber);
        this.id = id;
    }

    public int getPlayerNumber() {
        return this.playerNumber;
    }

    public String getId() {
        return id;
    }

    private static Color getColorForPlayer(int playerNumber) {
        return (playerNumber == 0) ? Color.BLACK : Color.RED;
    }

    @Override
    protected Checker clone() {
        try {
            return (Checker) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return id + "  " + color + "  x: " + xLoc + "  y: " + yLoc + "  king: " + isKing;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public Color getColor() {
        return color;
    }

    public int getxLoc() {
        return xLoc;
    }

    public void setxLoc(int xLoc) {
        this.xLoc = xLoc;
    }

    public int getyLoc() {
        return yLoc;
    }

    public void setyLoc(int yLoc) {
        this.yLoc = yLoc;
    }

    public void setLoc(int xLoc, int yLoc) {
        setxLoc(xLoc);
        setyLoc(yLoc);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillOval(xLoc * size + 3, yLoc * size + 3, size - 6, size - 6);
        g2.setColor(Color.BLACK);
        g2.fillOval(xLoc * size + 4, yLoc * size + 4, size - 8, size - 8);
        g2.setColor(Color.WHITE);
        g2.fillOval(xLoc * size + 5, yLoc * size + 5, size - 10, size - 10);

        g2.setColor(this.getColor());
        g2.fillOval(xLoc * size + 6, yLoc * size + 6, size - 12, size - 12);

        if (isKing) {
            g2.setColor(Color.YELLOW);
            g2.fillOval(xLoc * size + 12, yLoc * size + 12, size - 24, size - 24);
            g2.setColor(this.getColor());
            g2.fillOval(xLoc * size + 15, yLoc * size + 15, size - 30, size - 30);
        }

        int idX = xLoc * size + size / 2 - id.length() * 3;
        int idY = yLoc * size + size / 2 + id.length() * 3;

        g2.setColor(Color.WHITE);
        g2.drawString(id, idX, idY);
    }
}
