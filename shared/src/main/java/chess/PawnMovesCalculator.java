package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        // iterate twice to check if pawn can capture a piece
        for (int i = 0; i < 2; i++) {
            ChessPosition currentPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());

            if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                if (i == 0) {
                    currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn() - 1);
                }
                else {
                    currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn() + 1);
                }
            }
            else {
                if (i == 0) {
                    currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn() - 1);
                }
                else {
                    currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn() + 1);
                }
            }

            //Add move if new position is in bounds and occupied by the other team, handle promotion cases
            if (currentPosition.isInBounds() && board.getPiece(currentPosition) != null) {
                if (board.getPiece(currentPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    if (myPosition.getRow() == 7 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                        //FIXME add code to promote white pawn. Ask TAs how to incorporate ChessMove.getPromotionPiece()
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
                    }
                    else if (myPosition.getRow() == 2 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                        //FIXME add code to promote black pawn. Ask TAs how to incorporate ChessMove.getPromotionPiece()
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                        validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
                    }
                    else {
                        validMoves.add(new ChessMove(myPosition, currentPosition, board.getPiece(myPosition).getPieceType()));
                    }
                }
            }
        }

        //advance one space
        ChessPosition currentPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());

        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            currentPosition = new ChessPosition(currentPosition.getRow() + 1, currentPosition.getColumn());
        }
        else {
            currentPosition = new ChessPosition(currentPosition.getRow() - 1, currentPosition.getColumn());
        }

        //Add move if new position is in bounds and unoccupied, handle promotion cases and advancing two spaces when applicable
        if (currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
            if (myPosition.getRow() == 7 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                //FIXME add code to promote white pawn. Ask TAs how to incorporate ChessMove.getPromotionPiece()
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
            }
            else if (myPosition.getRow() == 2 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                //FIXME add code to promote black pawn. Ask TAs how to incorporate ChessMove.getPromotionPiece()
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, currentPosition, ChessPiece.PieceType.KNIGHT));
            }
            else {
                validMoves.add(new ChessMove(myPosition, currentPosition, board.getPiece(myPosition).getPieceType()));

                //advance two spaces
                currentPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
                boolean isInStartingPosition = false;

                if (myPosition.getRow() == 2 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                    currentPosition = new ChessPosition(currentPosition.getRow() + 2, currentPosition.getColumn());
                    isInStartingPosition = true;
                }
                else if (myPosition.getRow() == 7 && board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                    currentPosition = new ChessPosition(currentPosition.getRow() - 2, currentPosition.getColumn());
                    isInStartingPosition = true;
                }

                //Add move if new position is in bounds and unoccupied
                if (isInStartingPosition && currentPosition.isInBounds() && board.getPiece(currentPosition) == null) {
                    validMoves.add(new ChessMove(myPosition, currentPosition, board.getPiece(myPosition).getPieceType()));
                }
            }
        }

        return validMoves;
    }
}
