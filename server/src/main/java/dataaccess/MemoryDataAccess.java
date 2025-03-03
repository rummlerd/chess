package dataaccess;

import java.util.*;
import java.util.UUID;

import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<String, GameData> games = new HashMap<>();


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
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return auth;
    }
}
