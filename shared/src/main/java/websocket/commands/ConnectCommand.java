package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    private final String userName;
    private final ChessGame.TeamColor teamColor;

    public ConnectCommand(String authToken, Integer gameID, String userName) {
        super(CommandType.CONNECT, authToken, gameID);
        this.userName = userName;
        this.teamColor = null;
    }

    public ConnectCommand(String authToken, Integer gameID, String userName, ChessGame.TeamColor teamColor) {
        super(CommandType.CONNECT, authToken, gameID);
        this.userName = userName;
        this.teamColor = teamColor;
    }

    public String getUserName() {
        return userName;
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }
}
