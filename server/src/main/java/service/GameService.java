package service;

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

    public List<controller.GameResult> getAllGames(String authToken) {
        return dataAccess.getAllGames(authToken);
    }
}
