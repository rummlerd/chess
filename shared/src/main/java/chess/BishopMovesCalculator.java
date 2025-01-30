package chess;

import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator {
    private static final int[][] BISHOP_DIRECTIONS = {
            {1, 1}, // Up-right
            {-1, 1}, // Down-right
            {-1, -1}, // Down-left
            {1, -1}  // Up-left
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculatorUtils.generateMoves(board, myPosition, BISHOP_DIRECTIONS, false);
    }
}
