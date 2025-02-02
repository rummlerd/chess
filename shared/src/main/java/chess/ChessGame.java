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
    private ChessMove lastMove;
    private boolean enPassantValid;

    public ChessGame() {
        team = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        lastMove = null;
        enPassantValid = false;
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
        // Check for enPassant possibility
        if (lastMove != null) { // Can not be first move of the game
            ChessPosition lastStartPos = lastMove.getStartPosition();
            ChessPosition lastEndPos = lastMove.getEndPosition();
            ChessPiece lastPiece = board.getPiece(lastEndPos);
            ChessPiece newPiece = board.getPiece(startPosition);

            if (lastPiece.getPieceType() == ChessPiece.PieceType.PAWN && newPiece.getPieceType() == ChessPiece.PieceType.PAWN && // Both pieces must be pawns
                    Math.abs(lastStartPos.getRow() - lastEndPos.getRow()) == 2 && // Previous move must have been a pawn advancing two squares
                    startPosition.getRow() == lastEndPos.getRow() && // Pawns must be on the same row
                    (startPosition.getColumn() == lastEndPos.getColumn() - 1 || // Pawns must be in adjacent columns
                            startPosition.getColumn() == lastEndPos.getColumn() + 1)) {
                int direction = (newPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1; // Advance one row up for white, one down for black
                enPassantValid = true;
                validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow() + direction, lastEndPos.getColumn()), null)); // Add the valid en passant move
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
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        if (board.getPiece(startPosition) == null) {
            throw new InvalidMoveException("No piece at starting location");
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != team) {
            throw new InvalidMoveException("It is not your turn");
        }
        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (validMoves != null) {
            if (validMoves.contains(move)) {
                if (enPassantValid &&
                        board.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.PAWN &&
                        board.getPiece(endPosition) == null &&
                        startPosition.getColumn() - endPosition.getColumn() != 0) {
                    board.removePiece(new ChessPosition(startPosition.getRow(), endPosition.getColumn()));
                }
                board.makeMove(move);
                enPassantValid = false;
                lastMove = move;
                team = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
                return;
            }
        }
        throw new InvalidMoveException("Invalid move");
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
        if (isInCheck(teamColor)) { return false; }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition currentPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> validMoves = validMoves(currentPosition);
                        if (!validMoves.isEmpty()) {
                            return false; // If any valid move exists, the team is not in stalemate
                        }
                    }
                }
            }
        }
        return true; // Stalemate if no valid moves exist
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
