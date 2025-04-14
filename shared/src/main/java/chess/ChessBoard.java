package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import static chess.EscapeSequences.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    private ChessPiece[][] squares = new ChessPiece[8][8];

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

    public void makeMove(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionType = move.getPromotionPiece();
        ChessPiece myPiece = getPiece(startPosition);
        if (myPiece == null) {
            throw new IllegalStateException("No piece found at " + startPosition);
        }
        ChessGame.TeamColor myColor = myPiece.getTeamColor();

        if (promotionType == null) {
            addPiece(endPosition, myPiece);
        } else {
            addPiece(endPosition, new ChessPiece(myColor, promotionType));
        }

        removePiece(startPosition);
    }

    public void removePiece(ChessPosition position) {
        squares[position.getRowIndex()][position.getColumnIndex()] = null;
    }

    /**
     * Finds the king of a given team
     *
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
        return toStringFromWhite();
    }

    public String toStringFromWhite() {
        return buildBoardView(true, null);
    }

    public String toStringFromBlack() {
        return buildBoardView(false, null);
    }

    public String buildBoardView(boolean fromWhitePerspective, Collection<ChessMove> highlightedMoves) {
        var builder = new StringBuilder();

        // Adjust column labels for white's and black's perspective
        String[] columns = fromWhitePerspective ? new String[] {"a", "b", "c", "d", "e", "f", "g", "h"}
                : new String[] {"h", "g", "f", "e", "d", "c", "b", "a"};

        // Add extra dark grey background for column labels
        builder.append("\t").append(SET_TEXT_BOLD).append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        builder.append("    ");
        for (String column : columns) {
            builder.append(column).append("  ");
        }
        builder.append("  ").append(RESET_BG_COLOR).append("\n");

        // Loop through rows
        for (int i = 0; i < 8; i++) {
            // Reverse rows for black's perspective
            int row = fromWhitePerspective ? 7 - i : i;  // White: 8 to 1, Black: 1 to 8
            builder.append("\t").append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE).append(' ').append(row + 1).append(' ');

            for (int j = 0; j < 8; j++) {
                int col = fromWhitePerspective ? j : 7 - j;
                // Adjust square colors for white and black perspectives
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_LIGHTER_GREY; // White's perspective

                // Determine if this square is part of a move
                boolean isStart = false;
                boolean isEnd = false;
                if (highlightedMoves != null) {
                    for (ChessMove move : highlightedMoves) {
                        int startRow = move.getStartPosition().getRow() - 1;
                        int startCol = move.getStartPosition().getColumn() - 1;
                        int endRow = move.getEndPosition().getRow() - 1;
                        int endCol = move.getEndPosition().getColumn() - 1;

                        if (startRow == row && startCol == col) {
                            isStart = true;
                        } else if (endRow == row && endCol == col) {
                            isEnd = true;
                        }
                    }
                }

                boolean isDarkSquare = (row + col) % 2 == 0;

                // Apply highlight: neon green for start, neon yellow for end, adjusted for square color
                if (isStart) {
                    bgColor = isDarkSquare ? SET_BG_COLOR_HIGHLIGHT_GREEN_DARK : SET_BG_COLOR_HIGHLIGHT_GREEN;
                } else if (isEnd) {
                    bgColor = isDarkSquare ? SET_BG_COLOR_HIGHLIGHT_YELLOW_DARK : SET_BG_COLOR_HIGHLIGHT_YELLOW;
                }

                builder.append(bgColor);

                if (squares[row][col] != null) {
                    // For white pieces (red text)
                    if (squares[row][col].getTeamColor() == ChessGame.TeamColor.WHITE) {
                        builder.append(SET_TEXT_COLOR_RED);
                    }
                    // For black pieces (blue text)
                    else if (squares[row][col].getTeamColor() == ChessGame.TeamColor.BLACK) {
                        builder.append(SET_TEXT_COLOR_BLUE);
                    }
                    builder.append(' ').append(squares[row][col].toString()).append(' ');
                } else {
                    builder.append("   ");
                }
            }

            // End of row
            builder.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE).append(' ').append(row + 1).append(' ');
            builder.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append('\n');
        }

        // Add extra dark grey background for column labels at the bottom
        builder.append("\t").append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        builder.append("    ");  // Two spaces before columns
        for (String column : columns) {
            builder.append(column).append("  ");
        }
        builder.append("  ").append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append(RESET_TEXT_BOLD_FAINT);

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
    public ChessBoard clone() throws CloneNotSupportedException {
        ChessBoard clone = (ChessBoard) super.clone(); // Shallow copy

        ChessPiece[][] cloneSquares = new ChessPiece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (squares[i][j] != null) {
                    cloneSquares[i][j] = squares[i][j].clone();
                }
            }
        }
        clone.squares = cloneSquares;
        return clone;
    }


}
