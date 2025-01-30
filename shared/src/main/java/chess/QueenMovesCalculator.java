package chess;

import java.util.Collection;

public class QueenMovesCalculator implements PieceMovesCalculator {
    private static final int[][] QUEEN_DIRECTIONS = {
            {0, 1}, // Right
            {0, -1}, // Left
            {1, 0}, // Down
            {-1, 0}, // Up
            {1, 1}, // Down-right
            {1, -1}, // Down-left
            {-1, 1}, // Up-right
            {-1, -1} // Up-left
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculatorUtils.generateMoves(board, myPosition, QUEEN_DIRECTIONS, false);
    }
}
