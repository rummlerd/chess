package chess;

import java.util.Collection;

public class KingMovesCalculator implements PieceMovesCalculator {
    private static final int[][] KING_DIRECTIONS = {
            {1, 0}, // Up
            {1, 1}, // Up-right
            {0, 1}, // Right
            {-1, 1}, // Down-right
            {-1, 0}, // Down
            {-1, -1}, // Down-left
            {0, -1}, // Left
            {1, -1} // Up-left
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculatorUtils.generateMoves(board, myPosition, KING_DIRECTIONS, true);
    }
}
