package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPosition currentPosition;

        // Save piece and color as they are referenced multiple times
        ChessPiece myPiece = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = myPiece.getTeamColor();

        int direction = (myColor == ChessGame.TeamColor.WHITE) ? 1 : -1;  // 1 for white, -1 for black

        // Check if pawn can advance and capture piece to left or right
        int[][] diagonalAdvances = {{direction, -1}, {direction, 1}};
        for (int[] diagonalAdvance : diagonalAdvances) {
            currentPosition = new ChessPosition(myPosition.getRow() + diagonalAdvance[0], myPosition.getColumn() + diagonalAdvance[1]);
            // Add move if new position is in bounds and occupied by the other team
            if (currentPosition.isInBounds() && board.getPiece(currentPosition) != null
                    && board.getPiece(currentPosition).getTeamColor() != myColor) {
                addMove(validMoves, myPosition, currentPosition, myColor);
            }
        }

        // Advance one space
        currentPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        // Add move if new position is in bounds and unoccupied
        if (currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
            addMove(validMoves, myPosition, currentPosition, myColor);

            // Advance two spaces
            currentPosition = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
            // Add move if new position is in bounds, moving from starting position, and unoccupied
            if (myPosition.getRow() == ((myColor == ChessGame.TeamColor.WHITE) ? 2 : 7)
                    && currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
                validMoves.add(new ChessMove(myPosition, currentPosition, null));
            }
        }

        return validMoves;
    }

    // Method to handle promotion cases in a single spot to eliminate duplicate code
    private void addMove(Collection<ChessMove> moves, ChessPosition startPosition, ChessPosition endPosition, ChessGame.TeamColor color) {
        // Check for promotion
        if (endPosition.getRow() == ((color == ChessGame.TeamColor.WHITE) ? 8 : 1)) {
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
        } else {
            moves.add(new ChessMove(startPosition, endPosition, null));
        }
    }
}
