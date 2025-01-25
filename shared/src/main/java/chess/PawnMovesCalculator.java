package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPosition currentPosition;

        // iterate twice to check if pawn can capture a piece
        for (int i = 0; i < 2; i++) {
            if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                if (i == 0) {
                    currentPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
                }
                else {
                    currentPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                }
            }
            else {
                if (i == 0) {
                    currentPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
                }
                else {
                    currentPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                }
            }

            //Add move if new position is in bounds and occupied by the other team, handle promotion cases
            if (currentPosition.isInBounds() && board.getPiece(currentPosition) != null) {
                if (board.getPiece(currentPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    //check for promotion
                    if ((board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7)
                            || (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)) {
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
                    } else {
                        validMoves.add(new ChessMove(myPosition, currentPosition, null));
                    }
                }
            }
        }

        //advance one space
        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            currentPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        }
        else {
            currentPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
        }

        //Add move if new position is in bounds and unoccupied, handle promotion cases and advancing two spaces when applicable
        if (currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
            //check for promotion
            if ((board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7)
                    || (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)) {
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
            } else {
                validMoves.add(new ChessMove(myPosition, currentPosition, null));

                //advance two spaces
                boolean isInStartingPosition = false;

                if (myPosition.getRow() == 2 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                    currentPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
                    isInStartingPosition = true;
                }
                else if (myPosition.getRow() == 7 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                    currentPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
                    isInStartingPosition = true;
                }

                //Add move if new position is in bounds and unoccupied
                if (isInStartingPosition && currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
                    validMoves.add(new ChessMove(myPosition, currentPosition, null));
                }
            }
        }

        return validMoves;
    }
}
