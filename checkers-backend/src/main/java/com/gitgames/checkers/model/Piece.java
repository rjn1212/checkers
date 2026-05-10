package com.gitgames.checkers.model;

public class Piece implements Cloneable {
    private final String id;
    private final Player owner;
    private int col;
    private int row;
    private boolean king;

    public Piece(String id, Player owner, int col, int row) {
        this.id = id;
        this.owner = owner;
        this.col = col;
        this.row = row;
        this.king = false;
    }

    public String getId() { return id; }
    public Player getOwner() { return owner; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isKing() { return king; }

    public void setCol(int col) { this.col = col; }
    public void setRow(int row) { this.row = row; }
    public void setKing(boolean king) { this.king = king; }

    public void setPosition(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public Position getPosition() {
        return new Position(col, row);
    }

    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
