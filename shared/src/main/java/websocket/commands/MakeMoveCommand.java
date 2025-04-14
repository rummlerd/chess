package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;
    private final String userName;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move, String userName) {
        super(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.userName = userName;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getUserName() {
        return userName;
    }
}
