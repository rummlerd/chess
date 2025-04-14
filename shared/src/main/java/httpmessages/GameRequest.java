package httpmessages;

import chess.ChessGame.TeamColor;

public class GameRequest {
    private final TeamColor playerColor;
    private final int gameID;

    // Constructor for joinGame handler
    public GameRequest(String playerColor, int gameID) {
        if (playerColor == null) {
            this.playerColor = null;
        } else {
            try {
                this.playerColor = TeamColor.valueOf(playerColor);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("bad request");
            }
        }

        this.gameID = gameID;
    }

    public TeamColor getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }
}
