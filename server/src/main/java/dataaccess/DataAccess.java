package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    /**
     * Clear all users, games, and authTokens from the database, for use in testing
     */
    void clear();

    /**
     * Creates a new user. Throws a 403 already taken error if user already exists
     * @param user
     */
    void createUser(UserData user) throws IllegalArgumentException;

    /**
     * Returns UserData if user exists, returns null if no user exists
     * @param username
     * @return UserData
     */
    UserData getUser(String username) throws IllegalArgumentException;

    /**
     * Create a new authorization. Call after every login or registration. Returns an authToken which can be sent to the client for further actions
     * @param username
     * @return authToken
     */
    String createAuth(String username) throws IllegalArgumentException;

    /**
     * Retrieve an authorization given an authToken, throws an 401 unauthorized error if authToken not found
     * @param authToken
     * @return AuthData
     */
    AuthData getAuth(String authToken) throws IllegalArgumentException;

    /**
     * Deletes the authToken of a user who wishes to sign off
     * @param authToken authorization of user logging off
     */
    void deleteAuth(String authToken) throws IllegalArgumentException;

    /**
     * @param authToken authorizationt that user is signed in
     * @param gameName desired name of game from request body
     * @return gameID
     * @throws IllegalArgumentException
     */
    int createGame(String authToken, String gameName) throws IllegalArgumentException;
}
