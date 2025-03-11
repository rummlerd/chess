package dataaccess;

import chess.ChessGame;
import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.List;

public class SqlDataAccess implements DataAccess {

    public SqlDataAccess() {
        try {
            configureDatabase();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        return "";
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameResult> getAllGames(String authToken) throws DataAccessException {
        return List.of();
    }

    @Override
    public void addUser(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {

    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS UserData (
            `username` VARCHAR(256) NOT NULL UNIQUE,
            `password` VARCHAR(256) NOT NULL,
            `email` VARCHAR(256) NOT NULL,
            PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS AuthData (
            `id` INT NOT NULL AUTO_INCREMENT,
            `authToken` VARCHAR(256) NOT NULL,
            `username` VARCHAR(256) NOT NULL,
            PRIMARY KEY (`id`),
            FOREIGN KEY (`username`) REFERENCES UserData(`username`) ON DELETE CASCADE,
            INDEX(authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS GameData (
            `id` INT NOT NULL AUTO_INCREMENT,
            `gameID` INT NOT NULL,
            `whiteUsername` VARCHAR(256),
            `blackUsername` VARCHAR(256),
            `gameName` VARCHAR(256) NOT NULL,
            `game` TEXT NOT NULL,
            PRIMARY KEY (`id`),
            FOREIGN KEY (`whiteUsername`) REFERENCES UserData(`username`) ON DELETE SET NULL,
            FOREIGN KEY (`blackUsername`) REFERENCES UserData(`username`) ON DELETE SET NULL,
            INDEX(gameID),
            INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };



    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Unable to configure database: %s" + ex.getMessage());
        }
    }
}
