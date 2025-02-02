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
    private boolean whiteQueenCastle;
    private boolean whiteKingCastle;
    private boolean blackQueenCastle;
    private boolean blackKingCastle;

    public ChessGame() {
        team = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        lastMove = null;
        enPassantValid = false;
        whiteQueenCastle = true;
        whiteKingCastle = true;
        blackQueenCastle = true;
        blackKingCastle = true;
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
        ChessPiece.PieceType myType = myPiece.getPieceType();
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
            if(lastPiece != null) { // last piece may be null if castle or en passant move happened
                if (lastPiece.getPieceType() == ChessPiece.PieceType.PAWN && myType == ChessPiece.PieceType.PAWN && // Both pieces must be pawns
                        Math.abs(lastStartPos.getRow() - lastEndPos.getRow()) == 2 && // Previous move must have been a pawn advancing two squares
                        startPosition.getRow() == lastEndPos.getRow() && // Pawns must be on the same row
                        (startPosition.getColumn() == lastEndPos.getColumn() - 1 || // Pawns must be in adjacent columns
                                startPosition.getColumn() == lastEndPos.getColumn() + 1)) {
                    int direction = (myPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1; // Advance one row up for white, one down for black
                    enPassantValid = true;
                    validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow() + direction, lastEndPos.getColumn()), null)); // Add the valid en passant move
                }
            }
        }
        // Check for check possibility
        if (myType == ChessPiece.PieceType.KING && startPosition.getColumn() == 5) {
            boolean canCastle = true;
            if (board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1)) == null &&
                    board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2)) == null) {
                // King side castle is valid, if king is never in check
                for (int i = 0; i < 3; i++) {
                    try {
                        // Create a copy of the board before trying the move
                        ChessBoard copiedBoard = board.clone();
                        ChessGame copiedGame = new ChessGame();
                        copiedGame.setBoard(copiedBoard);
                        copiedBoard.makeMove(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() + i), null));
                        if (copiedGame.isInCheck(teamColor)) {
                            canCastle = false;
                        }
                    } catch (CloneNotSupportedException e){
                        throw new RuntimeException(e);
                    }
                }
                // If the king was never in check, add King side castle to valid moves
                if (canCastle &&
                        ((teamColor == TeamColor.WHITE && whiteKingCastle) ||
                        (teamColor == TeamColor.BLACK && blackKingCastle))) {
                    validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2), null));
                }
            }
            canCastle = true;
            if (board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1)) == null &&
                    board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2)) == null &&
                    board.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 3)) == null) {
                // Queen side castle is valid if king is never in check
                for (int i = 0; i < 3; i++) {
                    try {
                        // Create a copy of the board before trying the move
                        ChessBoard copiedBoard = board.clone();
                        ChessGame copiedGame = new ChessGame();
                        copiedGame.setBoard(copiedBoard);
                        copiedBoard.makeMove(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() - i), null));
                        if (copiedGame.isInCheck(teamColor)) {
                            canCastle = false;
                        }
                    } catch (CloneNotSupportedException e){
                        throw new RuntimeException(e);
                    }
                }
                // If the king was never in check, add Queen side castle to valid moves
                if (canCastle &&
                        ((teamColor == TeamColor.WHITE && whiteQueenCastle) ||
                        (teamColor == TeamColor.BLACK && blackQueenCastle))) {
                    validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2), null));
                }
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
        ChessPiece myPiece = board.getPiece(startPosition);
        if (myPiece == null) {
            throw new InvalidMoveException("No piece at starting location");
        }
        TeamColor myColor = myPiece.getTeamColor();
        ChessPiece.PieceType myType = myPiece.getPieceType();
        if (myColor != team) {
            throw new InvalidMoveException("It is not your turn");
        }
        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (validMoves != null) {
            if (validMoves.contains(move)) {
                // If this is an en passant move, remove the captured pawn
                if (enPassantValid &&
                        myType == ChessPiece.PieceType.PAWN &&
                        board.getPiece(endPosition) == null &&
                        startPosition.getColumn() - endPosition.getColumn() != 0) {
                    board.removePiece(new ChessPosition(startPosition.getRow(), endPosition.getColumn()));
                }
                // If this is a castle move, move the rook as well
                if (myType == ChessPiece.PieceType.KING && Math.abs(startPosition.getColumn() - endPosition.getColumn()) == 2) {
                    if (startPosition.getColumn() - endPosition.getColumn() > 0) { // King moved left
                        // Move Queen side rook from column 1 to column 4
                        board.makeMove(new ChessMove(new ChessPosition(startPosition.getRow(), 1), new ChessPosition(startPosition.getRow(), 4), null));
                    } else { // King moved right
                        // Move King side rook from column 8 to column 6
                        board.makeMove(new ChessMove(new ChessPosition(startPosition.getRow(), 8), new ChessPosition(startPosition.getRow(), 6), null));
                    }
                }
                board.makeMove(move);
                // Check if this move eliminates the possibility of castling
                if (myType == ChessPiece.PieceType.KING && Math.abs(startPosition.getColumn() - endPosition.getColumn()) != 2) {
                    if (myColor == TeamColor.WHITE) {
                        whiteKingCastle = false;
                        whiteQueenCastle = false;
                    } else {
                        blackKingCastle = false;
                        blackQueenCastle = false;
                    }
                }
                if (myType == ChessPiece.PieceType.ROOK) {
                    if (myColor == TeamColor.WHITE) {
                        if (startPosition.getColumn() > 4) {
                            whiteKingCastle = false;
                        } else {
                            whiteQueenCastle = false;
                        }
                    } else {
                        if (startPosition.getColumn() > 4) {
                            blackKingCastle = false;
                        } else {
                            blackQueenCastle = false;
                        }
                    }
                }
                enPassantValid = false; // Reset the possibility of an en passant move
                lastMove = move; // save the move made for en passant logic
                team = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE; // Change the team to indicate turn over
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
