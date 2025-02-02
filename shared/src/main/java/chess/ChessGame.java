package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor team;
    private ChessBoard board;

    public ChessGame() {
        team = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.team = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null) { return null; }
        ChessPiece myPiece = board.getPiece(startPosition);
        TeamColor teamColor = myPiece.getTeamColor();
        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            try {
                // Create a copy of the board before trying the move
                ChessBoard copiedBoard = board.clone();
                ChessGame copiedGame = new ChessGame();
                copiedGame.setBoard(copiedBoard);
                copiedBoard.makeMove(move);
                if (!copiedGame.isInCheck(teamColor)) {
                    validMoves.add(move);
                }
            } catch (CloneNotSupportedException e){
                throw new RuntimeException(e);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.getStartPosition()).getTeamColor() != team) {
            throw new InvalidMoveException("It is not your turn");
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = board.findKing(teamColor);
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece myPiece = board.getPiece(position);
                if (myPiece != null) {
                    if (myPiece.getTeamColor() != teamColor) {
                        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, position);
                        for (ChessMove move : possibleMoves) {
                            if (move.getEndPosition().equals(kingPosition)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        // Check if King can escape checkmate by moving
        ChessPosition kingPosition = board.findKing(teamColor);
        ChessPiece king = board.getPiece(kingPosition);
        Collection<ChessMove> kingMoves = king.pieceMoves(board, kingPosition);
        for (ChessMove kingMove : kingMoves) {
            try {
                ChessBoard copiedBoard = board.clone();
                ChessGame copiedGame = new ChessGame();
                copiedGame.setBoard(copiedBoard);
                copiedBoard.makeMove(kingMove);
                if (!copiedGame.isInCheck(teamColor)) {
                    return false;
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        // Check if by moving another piece, checkmate can be escaped
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece myPiece = board.getPiece(position);

                if (myPiece != null) {
                    if (myPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, position);

                        for (ChessMove move : possibleMoves) {
                            try {
                                ChessBoard copiedBoard = board.clone();
                                ChessGame copiedGame = new ChessGame();
                                copiedGame.setBoard(copiedBoard);
                                copiedBoard.makeMove(move);

                                if (!copiedGame.isInCheck(teamColor)) {
                                    return false; // Checkmate can be escaped
                                }
                            } catch (CloneNotSupportedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
