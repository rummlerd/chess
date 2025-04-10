package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    private final String userName;

    public ConnectCommand(String authToken, Integer gameID, String userName) {
        super(CommandType.CONNECT, authToken, gameID);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
