package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    /**
     * @return converts 1-based row to 0-based index
     */
    public int getRowIndex() {
        return row - 1;
    }

    /**
     * @return converts 1-based column to 0-based index
     */
    public int getColumnIndex() {
        return col - 1;
    }

    /**
     * @return true if the position is in bounds
     */
    public boolean isInBounds() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    @Override
    public String toString() {
        return "{" + row + ", " + col + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
