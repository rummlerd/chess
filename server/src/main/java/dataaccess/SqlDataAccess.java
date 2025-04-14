package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import httpmessages.GameResult;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.sql.Types.NULL;

public class SqlDataAccess implements DataAccess {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
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
            `authToken` VARCHAR(256) NOT NULL UNIQUE,
            `username` VARCHAR(256) NOT NULL,
            PRIMARY KEY (`authToken`),
            FOREIGN KEY (`username`) REFERENCES UserData(`username`) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS GameData (
            `gameID` INT NOT NULL UNIQUE,
            `whiteUsername` VARCHAR(256),
            `blackUsername` VARCHAR(256),
            `gameName` VARCHAR(256) NOT NULL,
            `game` TEXT NOT NULL,
            PRIMARY KEY (`gameID`),
            FOREIGN KEY (`whiteUsername`) REFERENCES UserData(`username`) ON DELETE SET NULL,
            FOREIGN KEY (`blackUsername`) REFERENCES UserData(`username`) ON DELETE SET NULL,
            INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public SqlDataAccess() {
        try {
            configureDatabase();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM AuthData");
        executeUpdate("DELETE FROM GameData");
        executeUpdate("DELETE FROM UserData");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try {
            getUser(user.username());
            throw new DataAccessException("already taken");
        } catch (DataAccessException e) {
            if (e.getMessage().equals("unauthorized")) {
                String statement = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
                // Hash password
                String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
                executeUpdate(statement, user.username(), hashedPassword, user.email());
            } else {
                throw e;
            }
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        List<String> res = executeQuery("SELECT * FROM UserData WHERE username=?", username).getFirst();
        return new UserData(res.get(0), res.get(1), res.get(2));
    }

    @Override
    public boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        // Read the previously hashed password from the database
        var hashedPassword = executeQuery("SELECT password FROM UserData WHERE username=?", username).getFirst().getFirst();

        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        getUser(username); // Throws unauthorized if user does not exist

        String authToken = UUID.randomUUID().toString();
        String statement = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authToken, username);

        return authToken;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        List<String> res = executeQuery("SELECT * FROM AuthData WHERE authToken=?", authToken).getFirst();
        return new AuthData(res.getFirst(), res.getLast());
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        getAuth(authToken); // Throws error if unauthorized
        String statement = "DELETE FROM AuthData WHERE authToken=?";
        executeUpdate(statement, authToken);
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        getAuth(authToken); // Throws unauthorized
        Random random = new Random();
        int gameID = 0;
        boolean uniqueGameID = false;
        while (!uniqueGameID) {
            gameID = 1000 + random.nextInt(9000);
            try {
                getGame(gameID);
            } catch (DataAccessException e) {
                if (e.getMessage().contains("unauthorized")) {
                    uniqueGameID = true;
                }
            }
        }
        ChessGame game = new ChessGame();
        String statement = "INSERT INTO GameData (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement, gameID, null, null, gameName, game);

        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        List<String> res = executeQuery("SELECT * FROM GameData WHERE gameID=?", gameID).getFirst();
        ChessGame game = gson.fromJson(res.get(4), ChessGame.class);
        return new GameData(gameID, res.get(1), res.get(2), res.get(3), game);
    }

    @Override
    public void updateGame(int gameID, ChessGame updatedGame) throws DataAccessException {
        String statement = "UPDATE GameData SET game=? WHERE gameID=?";
        executeUpdate(statement, updatedGame, gameID);
    }

    @Override
    public List<GameResult> getAllGames(String authToken) throws DataAccessException {
        getAuth(authToken);
        List<GameResult> gameResults = new ArrayList<>();
        try {
            List<List<String>> res = executeQuery("SELECT * FROM GameData");
            for (List<String> row : res) {
                gameResults.add(new GameResult(Integer.parseInt(row.get(0)), row.get(1), row.get(2), row.get(3)));
            }
            return gameResults;
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                return gameResults;
            }
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void addUserToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        AuthData authData = getAuth(authToken);
        GameData game = getGame(gameID);
        String statement = "UPDATE GameData SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        if (playerColor == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) {
            executeUpdate(statement, authData.username(), game.blackUsername(), game.game(), gameID);
        } else if (playerColor == ChessGame.TeamColor.BLACK && game.blackUsername() == null) {
            executeUpdate(statement, game.whiteUsername(), authData.username(), game.game(), gameID);
        } else {
            throw new DataAccessException("already taken");
        }
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                prepareStatement(ps, params);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to update database: " + e.getMessage());
        }
    }

    /**
     * Helper method to handle adding parameters to the prepared statement
     *
     * @param ps prepared statement
     * @param params parameters to replace ? placeholders in SQL statement
     */
    private void prepareStatement(PreparedStatement ps, Object... params) throws SQLException {
        for (var i = 0; i < params.length; i++) {
            var param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case ChessGame p -> ps.setString(i + 1, gson.toJson(p));
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
    }

    /**
     * Execute provided query statement
     *
     * @param statement string to become prepared statement
     * @return retrieved values from database as a list of a list of strings
     */
    private List<List<String>> executeQuery(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                prepareStatement(ps, params);

                return processQueryResult(ps.executeQuery());
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to execute query: " + e.getMessage());
        }
    }

    /**
     * Given a ResultSet object, this method returns rows of length specified
     *
     * @param rs ResultSet object to extract values from
     * @return Strings extracted from rs
     * @throws SQLException if anything with executeQuery fails
     * @throws DataAccessException if requested object was not found, meaning the request is unauthorized (invalid username or authToken)
     */
    private List<List<String>> processQueryResult(ResultSet rs) throws SQLException, DataAccessException {
        try (rs) {
            List<List<String>> results = new ArrayList<>();
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    row.add(rs.getString(i + 1));
                }
                results.add(row);
            }
            if (results.isEmpty()) {
                throw new DataAccessException("unauthorized");
            }

            return results;
        }
    }

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
