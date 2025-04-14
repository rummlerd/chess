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
    private boolean gameOver = false;

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
     * Gets all valid moves for a piece at the given location
     * Checks if en passant or castle moves are possible
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at the startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece myPiece = board.getPiece(startPosition);
        if (myPiece == null) {
            return null; // Return early if startPosition is empty
        }

        Collection<ChessMove> validMoves = getStandardValidMoves(myPiece, startPosition);

        if (lastMove != null && myPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            checkEnPassant(validMoves, myPiece, startPosition);
        }
        if (myPiece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getColumn() == 5) {
            checkCastling(validMoves, startPosition);
        }

        return validMoves;
    }

    /**
     * Unlike validMoves, this only validated normal moves: no en passant or castling moves
     * Filter out invalid moves from pieceMoves methods, which returns all possible moves even if they are invalid
     *
     * @param myPiece       ChessPiece object up for movement
     * @param startPosition starting ChessPosition of ChessPiece up for movement
     * @return ArrayList of valid ChessMoves
     */
    private Collection<ChessMove> getStandardValidMoves(ChessPiece myPiece, ChessPosition startPosition) {
        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor teamColor = myPiece.getTeamColor();

        for (ChessMove move : possibleMoves) {
            if (doesMoveStopCheck(teamColor, move)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /**
     * Reviews the current board layout and previous move made to verify if en passant move is valid
     *
     * @param validMoves    add valid en passant moves to this list
     * @param myPiece       ChessPiece object up for movement
     * @param startPosition starting ChessPosition of ChessPiece up for movement
     */
    private void checkEnPassant(Collection<ChessMove> validMoves, ChessPiece myPiece, ChessPosition startPosition) {
        ChessPosition lastStartPos = lastMove.getStartPosition();
        ChessPosition lastEndPos = lastMove.getEndPosition();
        ChessPiece lastPiece = board.getPiece(lastEndPos);

        // Last piece moved must also be a pawn
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        if (Math.abs(lastStartPos.getRow() - lastEndPos.getRow()) == 2 && // Pawn moved two squares
                startPosition.getRow() == lastEndPos.getRow() && // Pawns are next to each other in the same row
                Math.abs(startPosition.getColumn() - lastEndPos.getColumn()) == 1) { // Pawns are in directly adjacent columns

            int direction = (myPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            enPassantValid = true;
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow() + direction,
                    lastEndPos.getColumn()), null));
        }
    }

    /**
     * Adds move to validMoves if any castling moves are possible for startingPiece
     *
     * @param validMoves    add valid castling moves to this ArrayList
     * @param startPosition starting ChessPosition of ChessPiece up for movement
     */
    private void checkCastling(Collection<ChessMove> validMoves, ChessPosition startPosition) {

        // Check king-side castling
        if (canCastle(startPosition, 1, whiteKingCastle, blackKingCastle)) {
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() + 2), null));
        }

        // Check queen-side castling
        if (canCastle(startPosition, -1, whiteQueenCastle, blackQueenCastle)) {
            validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() - 2), null));
        }
    }

    /**
     * Abstract method for checking if castle is valid
     *
     * @param kingPos     currentPosition, piece must be king (checked previously)
     * @param direction   positive (right) for king side, negative (left) for queen side
     * @param whiteCastle false if castle move has been invalidated for white by previous moves
     * @param blackCastle false if castle move has been invalidated for black by previous moves
     * @return true if castle move valid to be added to validMoves
     */
    private boolean canCastle(ChessPosition kingPos, int direction, boolean whiteCastle, boolean blackCastle) {
        int[] offsets = (direction == 1) ? new int[]{1, 2} : new int[]{1, 2, 3};
        TeamColor teamColor = board.getPiece(kingPos).getTeamColor();

        // Check if path is clear
        for (int offset : offsets) {
            if (board.getPiece(new ChessPosition(kingPos.getRow(),
                    kingPos.getColumn() + direction * offset)) != null) {
                return false;
            }
        }

        // Check if the king remains safe during castling
        for (int i = 0; i < 3; i++) {
            if (!doesMoveStopCheck(teamColor, new ChessMove(kingPos, new ChessPosition(kingPos.getRow(),
                    kingPos.getColumn() + direction * i), null))) {
                return false;
            }
        }

        return (teamColor == TeamColor.WHITE) ? whiteCastle : blackCastle;
    }

    /**
     * Makes a move in a chess game, then switches team for next move
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        validateMove(move);

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece myPiece = board.getPiece(startPosition);
        ChessPiece.PieceType myType = myPiece.getPieceType();

        // Update ChessBoard and ChessGame information for special moves
        handleSpecialMoves(myType, startPosition, endPosition);
        updateCastlingRights(myType, myPiece.getTeamColor(), startPosition, endPosition);
        enPassantValid = false;
        lastMove = move;

        board.makeMove(move);

        team = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Throws variety of errors when client attempts to make invalid move
     *
     * @param move ChessMove needing validation
     * @throws InvalidMoveException wrong turn, invalid move, no piece at indicated starting ChessPosition
     */
    private void validateMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece myPiece = board.getPiece(startPosition);

        if (gameOver) {
            throw new InvalidMoveException("This game is over");
        }
        if (myPiece == null) {
            throw new InvalidMoveException("No piece at starting location");
        }
        if (myPiece.getTeamColor() != team) {
            throw new InvalidMoveException("That is not your piece");
        }
        if (!validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }
    }

    /**
     * If en passant move was made, capture the enemy pawn
     * If castle move was made, move the rook as well
     *
     * @param myType        PieceType of piece being moved
     * @param startPosition ChessPosition being moved from
     * @param endPosition   ChessPosition being moved to
     */
    private void handleSpecialMoves(ChessPiece.PieceType myType, ChessPosition startPosition, ChessPosition endPosition) {
        int startRow = startPosition.getRow();
        int startColumn = startPosition.getColumn();
        int endColumn = endPosition.getColumn();

        if (myType == ChessPiece.PieceType.PAWN && enPassantValid &&
                board.getPiece(endPosition) == null && // Pawn is moving into empty space (not capturing directly)
                startColumn != endColumn) { // Pawn is not moving straight (only possible when capturing)
            board.removePiece(new ChessPosition(startRow, endColumn));
        }

        if (myType == ChessPiece.PieceType.KING && Math.abs(startColumn - endColumn) == 2) { // King can only move two spaces when castling
            int rookStartColumn = (startColumn > endColumn) ? 1 : 8; // Find correct rook based on whether king is moving left or right
            int rookEndColumn = (startColumn > endColumn) ? 4 : 6; // Move rook to correct position based on whether king is moving left or right
            board.makeMove(new ChessMove(new ChessPosition(startRow, rookStartColumn),
                    new ChessPosition(startRow, rookEndColumn), null));
        }
    }

    /**
     * If any rook or king moves, change associated boolean to mark castle move as no longer possible
     *
     * @param myType        PieceType, king or rook are of interest
     * @param myColor       teamColor, so correct booleans are changed
     * @param startPosition starting ChessPosition, used to determine which castle possibility to remove
     * @param endPosition   ending ChessPosition of move, to check if king moved > 2 spaces (castled)
     */
    private void updateCastlingRights(ChessPiece.PieceType myType, TeamColor myColor, ChessPosition startPosition, ChessPosition endPosition) {
        int startColumn = startPosition.getColumn();

        if (myType == ChessPiece.PieceType.KING) {
            if (Math.abs(startColumn - endPosition.getColumn()) != 2) {
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
                if (startColumn > 4) {
                    whiteKingCastle = false;
                } else {
                    whiteQueenCastle = false;
                }
            } else {
                if (startColumn > 4) {
                    blackKingCastle = false;
                } else {
                    blackQueenCastle = false;
                }
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

    /**
     * Iterates through entire board to check if any enemy pieces can capture piece in targetPosition
     *
     * @param teamColor      Color of ChessPiece up for capture
     * @param targetPosition ChessPosition of ChessPiece up for capture
     * @return True if ChessPiece in targetPosition can be captured
     */
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

    /**
     * Checks if a single piece can move to capture piece in targetPosition
     *
     * @param piece  ChessPiece that may capture
     * @param from   starting ChessPosition of ChessPiece that may capture
     * @param target ChessPosition of ChessPiece that may be captured
     * @return true if piece can move to targetPosition
     */
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
        boolean checkmate = !canKingEscape(teamColor) && !canOtherPieceSaveKing(teamColor);
        if (checkmate) {
            endGame();
        }
        return checkmate;
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
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
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
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "\nteam=" + team +
                ", \nboard=\n" + board.toString() +
                ", \nlastMove=" + lastMove +
                ", \nenPassantValid=" + enPassantValid +
                ", \nwhiteQueenCastle=" + whiteQueenCastle +
                ", \nwhiteKingCastle=" + whiteKingCastle +
                ", \nblackQueenCastle=" + blackQueenCastle +
                ", \nblackKingCastle=" + blackKingCastle +
                '}';
    }

    public void endGame() {
        gameOver = true;
    }
}
