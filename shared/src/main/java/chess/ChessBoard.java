package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    private final ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRowIndex()][position.getColumnIndex()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRowIndex()][position.getColumnIndex()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j] = null;
            }
        }

        ChessGame.TeamColor color;
        int index;

        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                color = ChessGame.TeamColor.WHITE;
                index = 1;
            } else {
                color = ChessGame.TeamColor.BLACK;
                index = 8;
            }
            addPiece(new ChessPosition(index, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
            addPiece(new ChessPosition(index, 2), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(index, 3), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(index, 4), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
            addPiece(new ChessPosition(index, 5), new ChessPiece(color, ChessPiece.PieceType.KING));
            addPiece(new ChessPosition(index, 6), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(index, 7), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(index, 8), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        }

        for (int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    public ChessMove makeMove(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionType = move.getPromotionPiece();
        ChessPiece myPiece = getPiece(startPosition);
        ChessGame.TeamColor myColor = myPiece.getTeamColor();

        if (promotionType == null) {
            addPiece(endPosition, myPiece);
        } else {
            addPiece(endPosition, new ChessPiece(myPiece.getTeamColor(), promotionType));
        }

        removePiece(startPosition);

        if (true) { //FIXME Return null if move puts king in check or checkmate
            return null;
        } else {
            return move;
        }
    }

    public void removePiece(ChessPosition position) {
        squares[position.getRowIndex()][position.getColumnIndex()] = null;
    }

    /**
     * Finds the king of a given team
     * @param teamColor color of team
     * @return position of king
     */
    public ChessPosition findKing(ChessGame.TeamColor teamColor) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                if (getPiece(position) != null) {
                    if (getPiece(position).getPieceType() == ChessPiece.PieceType.KING && getPiece(position).getTeamColor() == teamColor) {
                        return position;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for (int i = 7; i > -1; i--) {
            builder.append('|');
            for (int j = 0; j < 8; j++) {
                if (squares[i][j] != null) {
                    builder.append(squares[i][j].toString());
                }
                else {
                    builder.append(' ');
                }
                builder.append('|');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ChessBoard clone = (ChessBoard) super.clone(); // Shallow copy

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i,j);
                ChessPiece piece = getPiece(position);
                if (piece != null) {
                    clone.addPiece(position, (ChessPiece) piece.clone());
                }
            }
        }

        return clone;
    }
}
