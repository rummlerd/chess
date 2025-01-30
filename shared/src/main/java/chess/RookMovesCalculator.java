package chess;

import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator {
    private static final int[][] ROOK_DIRECTIONS = {
            {0, 1}, // Right
            {0, -1}, // Left
            {1, 0}, // Down
            {-1, 0} // Up
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculatorUtils.generateMoves(board, myPosition, ROOK_DIRECTIONS, false);
    }
}
