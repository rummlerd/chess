package websocket.commands;

import chess.ChessGame;

public class ResignCommand extends UserGameCommand {
    private final ChessGame.TeamColor color;

    public ResignCommand(String authToken, Integer gameID, ChessGame.TeamColor color) {
        super(CommandType.RESIGN, authToken, gameID);
        this.color = color;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}
