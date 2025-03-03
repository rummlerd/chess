package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.List;

public interface DataAccess {
    /**
     * Clear all users, games, and authTokens from the database, for use in testing
     */
    void clear();

    /**
     * Creates a new user. Throws a 403 already taken error if user already exists
     * @param user info to be saved in new user
     */
    void createUser(UserData user) throws IllegalArgumentException;

    /**
     * Returns UserData if user exists, returns null if no user exists
     * @param username of user to be retrieved
     * @return UserData
     */
    UserData getUser(String username) throws IllegalArgumentException;

    /**
     * Create a new authorization. Call after every login or registration. Returns an authToken which can be sent to the client for further actions
     * @param username associated with new authToken
     * @return authToken
     */
    String createAuth(String username) throws IllegalArgumentException;

    /**
     * Retrieve an authorization given an authToken, throws an 401 unauthorized error if authToken not found
     * @param authToken authorization proof
     * @return AuthData
     * @throws IllegalArgumentException "unauthorized" if the authToken does not exist
     */
    AuthData getAuth(String authToken) throws IllegalArgumentException;

    /**
     * Deletes the authToken of a user who wishes to sign off
     * @param authToken authorization of user logging off
     */
    void deleteAuth(String authToken) throws IllegalArgumentException;

    /**
     * @param authToken authorization that user is signed in
     * @param gameName desired name of game from request body
     * @return gameID
     * @throws IllegalArgumentException unauthorized
     */
    int createGame(String authToken, String gameName) throws IllegalArgumentException;

    /**
     * Retrieves a specified game
     * @param gameID ID of desired game
     * @return gameData
     * @throws IllegalArgumentException game not found
     */
    GameData getGame(int gameID) throws IllegalArgumentException;

    /**
     * @param authToken authorization that the user is signed in
     * @return list of all saved games
     * @throws IllegalArgumentException unauthorized error
     */
    List<httpmessages.GameResult> getAllGames(String authToken) throws IllegalArgumentException;

    /**
     * @param authToken authorization that the user is signed in
     * @param playerColor color of team user wishes to become
     * @param gameID game user wishes to join
     * @throws IllegalArgumentException 401 unauthorized, 403 already taken, 500 game not found
     */
    void addUser(String authToken, ChessGame.TeamColor playerColor, int gameID) throws IllegalArgumentException;
}
