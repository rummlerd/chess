package service;

import chess.ChessGame;
import dataaccess.DataAccess;

import java.util.List;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) {
        return dataAccess.createGame(authToken, gameName);
    }

    public List<httpmessages.GameResult> getAllGames(String authToken) {
        return dataAccess.getAllGames(authToken);
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        dataAccess.addUser(authToken, playerColor, gameID);
    }
}
