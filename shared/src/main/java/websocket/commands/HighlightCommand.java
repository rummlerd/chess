package websocket.commands;

import chess.ChessPosition;

public class HighlightCommand extends UserGameCommand {
    private final ChessPosition position;

    public HighlightCommand(String authToken, Integer gameID, ChessPosition position) {
        super(CommandType.HIGHLIGHT, authToken, gameID);
        this.position = position;
    }

    public ChessPosition getPosition() {
        return position;
    }
}
