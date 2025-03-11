package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.*;
import java.util.zip.Adler32;

import static java.sql.Types.NULL;

public class SqlDataAccess implements DataAccess {
    private final Gson gson = new GsonBuilder().serializeNulls().create();

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
        List<String> res = executeQuery("SELECT * FROM userdata WHERE username='" + username + "'", 3).getFirst();
        if (res.getFirst() == null) { // FIXME may be able to delete this for loop entirely, once passing all tests try it
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
        List<String> res = executeQuery("SELECT * FROM authdata WHERE authToken='" + authToken + "'", 2).getFirst();
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
        getAuth(authToken); // Throws unauthorized
        Random random = new Random();
        int gameID = 0;
        boolean uniqueGameID = false;
        while (!uniqueGameID) {
            gameID = 1000 + random.nextInt(9000);
            try{
                getGame(gameID);
            } catch (DataAccessException e) {
                if (e.getMessage().contains("unauthorized")) {
                    uniqueGameID = true;
                }
            }
        }
        ChessGame game = new ChessGame();
        String statement = "INSERT INTO gamedata (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement, gameID, null, null, gameName, game);

        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        List<String> res = executeQuery("SELECT * FROM gamedata WHERE gameID='" + gameID + "'", 5).getFirst();
        ChessGame game = gson.fromJson(res.get(4), ChessGame.class);
        return new GameData(gameID, res.get(1), res.get(2), res.get(3), game);
    }

    @Override
    public List<GameResult> getAllGames(String authToken) throws DataAccessException {
        getAuth(authToken);
        List<GameResult> gameResults = new ArrayList<>();
        try {
            List<List<String>> res = executeQuery("SELECT * FROM gamedata", 4);
            for (List<String> row : res) {
                gameResults.add(new GameResult(
                        Integer.parseInt(row.get(0)),
                        row.get(1),
                        row.get(2),
                        row.get(3)
                ));
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
    public void addUser(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        AuthData authData = getAuth(authToken);
        GameData game = getGame(gameID);
        String statement = "UPDATE gamedata SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
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
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    else if (param instanceof ChessGame p) {
                        ps.setString(i + 1, gson.toJson(p));
                    }
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

    private List<List<String>> executeQuery(String statement, int colsCount) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    List<List<String>> results = new ArrayList<>();
                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 0; i < colsCount; i++) {
                            row.add(rs.getString(i + 1));
                        }
                        results.add(row);
                    }
                    if (results.isEmpty()) {
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
