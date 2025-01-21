package chess;

import java.util.Collection;
import java.util.ArrayList;

public class RookMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            // Reset currentPosition to starting Position
            ChessPosition currentPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());

            //loop through all valid position for each diagonal
            while (true) {
                if (i == 0) { // right
                    currentPosition = new ChessPosition(currentPosition.getRow(), currentPosition.getColumn() + 1);
                }
                else if (i == 1) { // left
                    currentPosition = new ChessPosition(currentPosition.getRow(), currentPosition.getColumn() - 1);
                }
                else if (i == 2) { // down
                    currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn());
                }
                else { // up
                    currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn());
                }

                //Exit loop if the position is not in bounds or is occupied by the same team
                if (!currentPosition.isInBounds()) {
                    break; //break if current position is not in bounds
                }
                else if (board.getPiece(currentPosition) != null) {
                    if (board.getPiece(currentPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        validMoves.add(new ChessMove(myPosition, currentPosition, null));
                    }
                    break; //break if occupied
                }

                //add position to validMoves
                validMoves.add(new ChessMove(myPosition, currentPosition, null));
            }

        }

        return validMoves;
    }
}

