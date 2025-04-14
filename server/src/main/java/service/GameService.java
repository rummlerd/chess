package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        // Validate that a gameName was input
        if (gameName == null) {
            throw new DataAccessException("bad request");
        }
        return dataAccess.createGame(authToken, gameName);
    }

    public List<httpmessages.GameResult> getAllGames(String authToken) throws DataAccessException {
        return dataAccess.getAllGames(authToken);
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        // Validate input (playerColor must not be null, gameID must be 1000 or greater)
        if (playerColor == null || gameID < 1000) {
            throw new DataAccessException("bad request");
        }
        dataAccess.addUserToGame(authToken, playerColor, gameID);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return dataAccess.getGame(gameID);
    }

    public void updateGame(int gameID, ChessGame updatedGame) throws DataAccessException {
        dataAccess.updateGame(gameID, updatedGame);
    }
}
