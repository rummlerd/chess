package chess;

import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator {
    private static final int[][] KNIGHT_DIRECTIONS = {
            {2, 1}, // Up-right
            {2, -1}, // Up-left
            {1, 2}, // Right-up
            {-1, 2}, // Right-down
            {-2, 1}, // Down-right
            {-2, -1}, // Down-left
            {1, -2}, // Left-up
            {-1, -2} // Left-down
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculatorUtils.generateMoves(board, myPosition, KNIGHT_DIRECTIONS, true);
    }
}