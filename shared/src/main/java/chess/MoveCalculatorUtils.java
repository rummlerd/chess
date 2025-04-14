package chess;

import java.util.ArrayList;
import java.util.Collection;

public class MoveCalculatorUtils {
    public static Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition startPosition, int[][] directions, boolean singleStep) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        // Iterate over all possible move directions
        for (int[] direction : directions) {
            ChessPosition currentPosition = new ChessPosition(
                    startPosition.getRow() + direction[0],
                    startPosition.getColumn() + direction[1]
            );

            while (true) {
                // Break if out of bounds
                if (!currentPosition.isInBounds()) {
                    break;
                }

                // Add move if occupied by opponent, otherwise break
                if (board.getPiece(currentPosition) != null) {
                    if (board.getPiece(currentPosition).getTeamColor() != board.getPiece(startPosition).getTeamColor()) {
                        validMoves.add(new ChessMove(startPosition, currentPosition, null));
                    }
                    break;
                }

                // Add to valid moves (move is in bounds and unoccupied)
                validMoves.add(new ChessMove(startPosition, currentPosition, null));

                // If single-step piece (King/Knight), break after first move
                if (singleStep) {
                    break;
                }

                // Continue for multiple step pieces (Queen, Rook, Bishop)
                currentPosition = new ChessPosition(
                        currentPosition.getRow() + direction[0],
                        currentPosition.getColumn() + direction[1]
                );
            }
        }

        return validMoves;
    }
}

