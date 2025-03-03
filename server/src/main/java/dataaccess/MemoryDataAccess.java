package dataaccess;

import java.util.*;
import java.util.UUID;

import chess.ChessGame;
import controller.GameResult;
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
    public void createUser(UserData user) {
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        UserData user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        return user;
    }

    @Override
    public String createAuth(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("User does not exist");
        }

        String authToken = UUID.randomUUID().toString();
        authTokens.put(authToken, new AuthData(authToken, username));

        return authToken;
    }

    @Override
    public AuthData getAuth(String authToken) {
        checkAuthToken(authToken);
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        checkAuthToken(authToken);
        authTokens.remove(authToken);
    }

    @Override
    public int createGame(String authToken, String gameName) {
        checkAuthToken(authToken);
        Random random = new Random();
        int gameID = 1000 + random.nextInt(9000); // Generates 1000 to 9999
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public List<GameResult> getAllGames(String authToken) {
        checkAuthToken(authToken);
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

    private void checkAuthToken(String authToken) throws IllegalArgumentException {
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("unauthorized");
        }
    }
}
