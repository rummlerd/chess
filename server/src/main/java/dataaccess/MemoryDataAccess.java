package dataaccess;

import java.util.*;
import java.util.UUID;

import chess.ChessGame;
import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();


    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        if (user == null) {
            throw new DataAccessException("user not found");
        }
        return user;
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        getUser(username); // getUser will throw error if username not found in memory

        String authToken = UUID.randomUUID().toString();
        authTokens.put(authToken, new AuthData(authToken, username));

        return authToken;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        getAuth(authToken);
        authTokens.remove(authToken);
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        getAuth(authToken);
        Random random = new Random();
        int gameID = 1000 + random.nextInt(9000); // Generates 1000 to 9999
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("game not found");
        }
        return game;
    }

    @Override
    public List<GameResult> getAllGames(String authToken) throws DataAccessException {
        getAuth(authToken);
        List<GameResult> gameResults = new ArrayList<>();
        for (GameData gameData : games.values()) {
            gameResults.add(new GameResult(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName()
            ));
        }
        return gameResults;
    }

    @Override
    public void addUser(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        AuthData authData = getAuth(authToken);
        GameData game = getGame(gameID);
        if (playerColor == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) {
            games.put(gameID, new GameData(gameID, authData.username(), game.blackUsername(), game.gameName(), game.game()));
        } else if (playerColor == ChessGame.TeamColor.BLACK && game.blackUsername() == null) {
            games.put(gameID, new GameData(gameID, game.whiteUsername(), authData.username(), game.gameName(), game.game()));
        } else {
            throw new DataAccessException("already taken");
        }
    }
}
