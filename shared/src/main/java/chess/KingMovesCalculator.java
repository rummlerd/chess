package chess;

import java.util.Collection;
import java.util.ArrayList;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            // Reset currentPosition to starting Position
            ChessPosition currentPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());

            //loop through all valid position for each diagonal
            if (i == 0) {
                currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn());
            }
            else if (i == 1) {
                currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn() + 1);
            }
            else if (i == 2) {
                currentPosition = new ChessPosition(currentPosition.getRow(), currentPosition.getColumn() + 1);
            }
            else if (i == 3) {
                currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn() + 1);
            }
            else if (i == 4) {
                currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn());
            }
            else if (i == 5) {
                currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn() - 1);
            }
            else if (i == 6) {
                currentPosition = new ChessPosition(currentPosition.getRow(), currentPosition.getColumn() - 1);
            }
            else {
                currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn() - 1);
            }

            //End iteration if the position is not in bounds or is occupied by the same team
            if (!currentPosition.isInBounds()) {
                continue;
            }
            else if (board.getPiece(currentPosition) != null) {
                if (board.getPiece(currentPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    validMoves.add(new ChessMove(myPosition, currentPosition, null));
                }
                continue;
            }

            //add position to validMoves
            validMoves.add(new ChessMove(myPosition, currentPosition, null));
        }

        return validMoves;
    }
}

