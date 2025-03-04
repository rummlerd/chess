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
        ChessPiece myPiece = board.getPiece(startPosition);
        if (myPiece == null) return null; // Early return if no piece

        Collection<ChessMove> validMoves = getStandardValidMoves(myPiece, startPosition);

        checkEnPassant(validMoves, myPiece, startPosition);
        checkCastling(validMoves, myPiece, startPosition);

        return validMoves;
    }

    private Collection<ChessMove> getStandardValidMoves(ChessPiece myPiece, ChessPosition startPosition) {
        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor teamColor = myPiece.getTeamColor();

        for (ChessMove move : possibleMoves) {
            if (simulateMove(move, teamColor)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private void checkEnPassant(Collection<ChessMove> validMoves, ChessPiece myPiece, ChessPosition startPosition) {
        if (lastMove == null || myPiece.getPieceType() != ChessPiece.PieceType.PAWN) return;

        ChessPosition lastStartPos = lastMove.getStartPosition();
        ChessPosition lastEndPos = lastMove.getEndPosition();
        ChessPiece lastPiece = board.getPiece(lastEndPos);

        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) return;

        if (Math.abs(lastStartPos.getRow() - lastEndPos.getRow()) == 2 && // Pawn moved two squares
                startPosition.getRow() == lastEndPos.getRow() && // Same row
                Math.abs(startPosition.getColumn() - lastEndPos.getColumn()) == 1) { // Adjacent columns

            int direction = (myPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            enPassantValid = true;
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow() + direction,
                    lastEndPos.getColumn()), null));
        }
    }

    private void checkCastling(Collection<ChessMove> validMoves, ChessPiece myPiece, ChessPosition startPosition) {
        if (myPiece.getPieceType() != ChessPiece.PieceType.KING || startPosition.getColumn() != 5) return;

        TeamColor teamColor = myPiece.getTeamColor();

        // Check king-side castling
        if (canCastle(startPosition, 1, teamColor, whiteKingCastle, blackKingCastle)) {
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() + 2), null));
        }

        // Check queen-side castling
        if (canCastle(startPosition, -1, teamColor, whiteQueenCastle, blackQueenCastle)) {
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() - 2), null));
        }
    }

    private boolean canCastle(ChessPosition kingPos, int direction, TeamColor teamColor,
                              boolean whiteCastle, boolean blackCastle) {
        int[] offsets = (direction == 1) ? new int[]{1, 2} : new int[]{1, 2, 3};

        // Check if path is clear
        for (int offset : offsets) {
            if (board.getPiece(new ChessPosition(kingPos.getRow(),
                    kingPos.getColumn() + direction * offset)) != null) {
                return false;
            }
        }

        // Check if the king remains safe during castling
        for (int i = 0; i < 3; i++) {
            if (!simulateMove(new ChessMove(kingPos, new ChessPosition(kingPos.getRow(), kingPos.getColumn() + direction * i), null), teamColor)) {
                return false;
            }
        }

        return (teamColor == TeamColor.WHITE) ? whiteCastle : blackCastle;
    }

    private boolean simulateMove(ChessMove move, TeamColor teamColor) {
        try {
            ChessBoard copiedBoard = board.clone();
            ChessGame copiedGame = new ChessGame();
            copiedGame.setBoard(copiedBoard);
            copiedBoard.makeMove(move);
            return !copiedGame.isInCheck(teamColor);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        validateMove(move);

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece myPiece = board.getPiece(startPosition);
        ChessPiece.PieceType myType = myPiece.getPieceType();
        TeamColor myColor = myPiece.getTeamColor();

        handleSpecialMoves(myType, startPosition, endPosition);

        board.makeMove(move);
        updateCastlingRights(myType, myColor, startPosition, endPosition);

        enPassantValid = false;
        lastMove = move;
        team = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private void validateMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece myPiece = board.getPiece(startPosition);

        if (myPiece == null) {
            throw new InvalidMoveException("No piece at starting location");
        }
        if (myPiece.getTeamColor() != team) {
            throw new InvalidMoveException("It is not your turn");
        }
        if (!validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }
    }

    private void handleSpecialMoves(ChessPiece.PieceType myType, ChessPosition startPosition, ChessPosition endPosition) {
        if (myType == ChessPiece.PieceType.PAWN && enPassantValid &&
                board.getPiece(endPosition) == null &&
                startPosition.getColumn() != endPosition.getColumn()) {
            board.removePiece(new ChessPosition(startPosition.getRow(), endPosition.getColumn()));
        }

        if (myType == ChessPiece.PieceType.KING && Math.abs(startPosition.getColumn() - endPosition.getColumn()) == 2) {
            int rookStartColumn = (startPosition.getColumn() > endPosition.getColumn()) ? 1 : 8;
            int rookEndColumn = (startPosition.getColumn() > endPosition.getColumn()) ? 4 : 6;
            board.makeMove(new ChessMove(new ChessPosition(startPosition.getRow(), rookStartColumn),
                    new ChessPosition(startPosition.getRow(), rookEndColumn), null));
        }
    }

    private void updateCastlingRights(ChessPiece.PieceType myType, TeamColor myColor, ChessPosition startPosition, ChessPosition endPosition) {
        if (myType == ChessPiece.PieceType.KING) {
            if (Math.abs(startPosition.getColumn() - endPosition.getColumn()) != 2) {
                if (myColor == TeamColor.WHITE) {
                    whiteKingCastle = false;
                    whiteQueenCastle = false;
                } else {
                    blackKingCastle = false;
                    blackQueenCastle = false;
                }
            }
        } else if (myType == ChessPiece.PieceType.ROOK) {
            if (myColor == TeamColor.WHITE) {
                if (startPosition.getColumn() > 4) whiteKingCastle = false;
                else whiteQueenCastle = false;
            } else {
                if (startPosition.getColumn() > 4) blackKingCastle = false;
                else blackQueenCastle = false;
            }
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
        return canAnyMoveCapturePosition(teamColor, kingPosition);
    }

    private boolean canAnyMoveCapturePosition(TeamColor teamColor, ChessPosition targetPosition) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece myPiece = board.getPiece(position);
                if (isOppositeTeam(myPiece, teamColor)) {
                    if (canPieceCapturePosition(myPiece, position, targetPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canPieceCapturePosition(ChessPiece piece, ChessPosition from, ChessPosition target) {
        for (ChessMove move : piece.pieceMoves(board, from)) {
            if (move.getEndPosition().equals(target)) {
                return true;
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
        return !canKingEscape(teamColor) && !canOtherPieceSaveKing(teamColor);
    }

    private boolean canKingEscape(TeamColor teamColor) {
        ChessPosition kingPosition = board.findKing(teamColor);
        ChessPiece king = board.getPiece(kingPosition);
        Collection<ChessMove> kingMoves = king.pieceMoves(board, kingPosition);
        return canAnyMoveSaveKing(teamColor, kingMoves);
    }

    private boolean canOtherPieceSaveKing(TeamColor teamColor) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece myPiece = board.getPiece(position);
                if (isSameTeam(myPiece, teamColor)) {
                    Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, position);
                    if (canAnyMoveSaveKing(teamColor, possibleMoves)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canAnyMoveSaveKing(TeamColor teamColor, Collection<ChessMove> moves) {
        for (ChessMove move : moves) {
            if (doesMoveStopCheck(teamColor, move)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesMoveStopCheck(TeamColor teamColor, ChessMove move) {
        try {
            ChessBoard copiedBoard = board.clone();
            ChessGame copiedGame = new ChessGame();
            copiedGame.setBoard(copiedBoard);
            copiedBoard.makeMove(move);
            return !copiedGame.isInCheck(teamColor);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !hasValidMoves(teamColor);
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition currentPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (isSameTeam(currentPiece, teamColor) && !validMoves(currentPosition).isEmpty()) {
                    return true; // If any valid move exists, the team is not in stalemate
                }
            }
        }
        return false; // Stalemate if no valid moves exist
    }

    private boolean isSameTeam(ChessPiece piece, TeamColor teamColor) {
        if (piece != null) {
            return piece.getTeamColor() == teamColor;
        }
        return false;
    }

    private boolean isOppositeTeam(ChessPiece piece, TeamColor teamColor) {
        if (piece != null) {
            return piece.getTeamColor() != teamColor;
        }
        return false;
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
