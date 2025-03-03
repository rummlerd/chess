package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import java.util.List;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        return dataAccess.createGame(authToken, gameName);
    }

    public List<httpmessages.GameResult> getAllGames(String authToken) throws DataAccessException {
        return dataAccess.getAllGames(authToken);
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        dataAccess.addUser(authToken, playerColor, gameID);
    }
}
