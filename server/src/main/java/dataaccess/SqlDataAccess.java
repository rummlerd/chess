package dataaccess;

import chess.ChessGame;
import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.Adler32;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlDataAccess implements DataAccess {

    public SqlDataAccess() {
        try {
            configureDatabase();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM authdata");
        executeUpdate("DELETE FROM gamedata");
        executeUpdate("DELETE FROM userdata");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try {
            getUser(user.username());
            throw new DataAccessException("already taken");
        } catch (DataAccessException e) {
            if (e.getMessage().equals("unauthorized")) {
                String statement = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
                executeUpdate(statement, user.username(), user.password(), user.email());
            }
            else {
                throw e;
            }
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        List<String> res = executeQuery("SELECT username, password, email FROM userdata WHERE username='" + username + "'", 3);
        if (res.getFirst() == null) {
            throw new DataAccessException("unauthorized");
        }
        return new UserData(res.get(0), res.get(1), res.get(2));
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        getUser(username); // Throws unauthorized if user does not exist

        String authToken = UUID.randomUUID().toString();
        String statement = "INSERT INTO authdata (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authToken, username);

        return authToken;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        List<String> res = executeQuery("SELECT authToken, username FROM authdata WHERE authToken='" + authToken + "'", 2);
        return new AuthData(res.getFirst(), res.getLast());
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        getAuth(authToken); // Throws error if unauthorized
        String statement = "DELETE FROM authdata WHERE authToken=?";
        executeUpdate(statement, authToken);
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

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    //else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    // FIXME change the above line to handle ChessGame objects if needed
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to update database: " + e.getMessage());
        }
    }

    private List<String> executeQuery(String statement, int colsCount) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    List<String> results = new ArrayList<>();
                    if (rs.next()) {
                        for (int i = 0; i < colsCount; i++) {
                            results.add(rs.getString(i + 1));
                        }
                    } else {
                        throw new DataAccessException ("unauthorized");
                    }

                    return results;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to execute query: " + e.getMessage());
        }
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

    private void configureDatabase() throws Exception {
        System.out.println("\nConfiguring Database\n");
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
