package service;

import chess.ChessGame;
import dataaccess.*;
import httpmessages.GameResult;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

public class SqlDAOTests {
    private static DataAccess dataAccess;
    private static UserService userService;
    private static GameService gameService;
    private static final UserData user = new UserData("testUser", "test", "test@");

    @BeforeAll
    public static void switchToTestDB() throws Exception {
        DatabaseManager.renameDatabase("test_db");

        dataAccess = new SqlDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    @BeforeEach
    public void resetTestDB() throws Exception {
        userService.clearApplication();
    }

    @AfterAll
    public static void switchToChessDB() throws Exception {
        DatabaseManager.renameDatabase("chess");
    }
    
    @Test
    @DisplayName("Register new user on Database")
    public void registerNewUserDatabase() {
        try {
            AuthData result = userService.register(user);

            Assertions.assertNotNull(result.authToken(), "Should return valid AuthToken");
            Assertions.assertEquals(user.username(), result.username(), "Usernames should match");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to bad request on Database")
    public void registerNewUserBadRequestDatabase() {
        try {
            userService.register(new UserData("testUser", null, "test@"));

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to already taken username on Database")
    public void registerNewUserAlreadyTakenDatabase() {
        try {
            userService.register(user);
            userService.register(new UserData("testUser", "test2", "test@2"));

            Assertions.fail("Should have thrown 'already taken' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("already taken", e.getMessage(), "Should be 'already taken' error");
        }
    }

    @Test
    @DisplayName("Logout a user on Database")
    public void logoutUserDatabase() {
        try {
            AuthData result = userService.register(user);
            userService.logout(result.authToken());
            dataAccess.getAuth(result.authToken());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to logout an unauthorized user on Database")
    public void logoutUserUnauthorizedDatabase() {
        try {
            userService.logout(UUID.randomUUID().toString());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Login a user on Database")
    public void loginUserDatabase() {
        try {
            AuthData result = userService.register(user);
            userService.logout(result.authToken());
            AuthData loginResult = userService.login(user);

            Assertions.assertNotNull(loginResult.authToken(), "Should return valid AuthToken");
            Assertions.assertEquals(user.username(), loginResult.username(), "Usernames should match");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Fail to login user because of unauthorized username on Database")
    public void loginUsernameUnauthorizedDatabase() {
        try {
            AuthData result = userService.register(user);
            userService.logout(result.authToken());
            userService.login(new UserData("testUser2", "test", null));

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to login user because of unauthorized password on Database")
    public void loginPasswordUnauthorizedDatabase() {
        try {
            AuthData result = userService.register(user);
            userService.logout(result.authToken());
            userService.login(new UserData("testUser", "test2", null));

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Clear application doesn't throw error on database")
    public void clearAppDatabase() {
        try {
            AuthData result = userService.register(user);
            int gameID = gameService.createGame(result.authToken(), "testGame");

            // Check that dataAccess has at least one user, game, and AuthData
            Assertions.assertNotNull(dataAccess.getUser(user.username()), "Must be a user to clear");
            Assertions.assertNotNull(dataAccess.getGame(gameID), "Must be a game to clear");
            Assertions.assertNotNull(dataAccess.getAllGames(result.authToken()), "Must have some AuthData to clear");

            userService.clearApplication();

            // Kind of just assuming that if it cleared AuthData, it cleared everything else too
            dataAccess.getAuth(result.authToken());
            Assertions.fail("Should have thrown unauthorized error due to deleted authData");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    // GameService Tests
    @Test
    @DisplayName("Create new game on database")
    public void createNewGameDatabase() {
        String gameName = "testGame";

        try {
            AuthData result = userService.register(user);
            int gameID = gameService.createGame(result.authToken(), gameName);
            // Error will be thrown here if the gameID is invalid, don't need to check later
            GameData game = dataAccess.getGame(gameID);

            Assertions.assertEquals(gameName, game.gameName(), "Game names must match");
            Assertions.assertNull(game.whiteUsername(), "White username should be null");
            Assertions.assertNull(game.blackUsername(), "Black username should be null");
            Assertions.assertNotNull(game.game(), "The ChessGame should not be null");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Fail to create new game due to bad request on Database")
    public void createNewGameBadRequestDatabase() {
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            gameService.createGame(result.authToken(), null);

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to create new game due to unauthorized on Database")
    public void createNewGameUnauthorizedDatabase() {
        try {
            gameService.createGame(UUID.randomUUID().toString(), "testName");

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("List all games on Database")
    public void listAllGamesDatabase() {
        String gameName = "testGame";
        String gameName2 = "testGame2";

        try {
            AuthData result = userService.register(user);
            gameService.createGame(result.authToken(), gameName);
            gameService.createGame(result.authToken(), gameName2);
            List<GameResult> games = gameService.getAllGames(result.authToken());

            Assertions.assertEquals(2, games.size(), "Should be 2 games");
            // Can't be sure what order games are listed, so check that names match the input names
            boolean foundGame1 = false;
            boolean foundGame2 = false;
            for (GameResult game : games) {
                if (game.gameName().equals(gameName)) {
                    foundGame1 = true;
                } else if (game.gameName().equals(gameName2)) {
                    foundGame2 = true;
                }
            }
            Assertions.assertTrue(foundGame1, "Game 1 should be found");
            Assertions.assertTrue(foundGame2, "Game 2 should be found");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Unauthorized to list games on Database")
    public void listAllGamesUnauthorizedDatabase() {

        try {
            gameService.getAllGames(UUID.randomUUID().toString());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Join game successfully on Database")
    public void joinGameSuccessfullyDatabase() {
        UserData user1 = new UserData("testUser1", "test", "test@");
        UserData user2 = new UserData("testUser2", "test", "test@");
        String gameName = "testGame";

        try {
            AuthData result1 = userService.register(user1);
            AuthData result2 = userService.register(user2);
            int gameID = gameService.createGame(result1.authToken(), gameName);
            gameService.joinGame(result1.authToken(), ChessGame.TeamColor.WHITE, gameID);
            gameService.joinGame(result2.authToken(), ChessGame.TeamColor.BLACK, gameID);
            GameData game = dataAccess.getGame(gameID);

            Assertions.assertEquals(gameName, game.gameName(), "Game name should be unchanged");
            Assertions.assertEquals(user1.username(), game.whiteUsername(), "White username should be correct");
            Assertions.assertEquals(user2.username(), game.blackUsername(), "Black username should be correct");
            Assertions.assertEquals(gameID, game.gameID(), "Game ID should be unchanged");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Fail to join game due to bad request on database")
    public void joinGameBadRequestDatabase() {
        try {
            AuthData result = userService.register(user);
            gameService.joinGame(result.authToken(), ChessGame.TeamColor.WHITE, -1);

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to join game due to unauthorized on Database")
    public void joinGameUnauthorizedDatabase() {
        try {
            gameService.joinGame(UUID.randomUUID().toString(), ChessGame.TeamColor.WHITE, 1234);

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to join game due to color already taken on Database")
    public void joinGameColorAlreadyTakenDatabase() {
        try {
            AuthData result1 = userService.register(new UserData("user1", "test", "test@"));
            AuthData result2 = userService.register(new UserData("user2", "test", "test@"));
            int gameID = gameService.createGame(result1.authToken(), "testGame");
            gameService.joinGame(result1.authToken(), ChessGame.TeamColor.WHITE, gameID);
            // Second user tries to join as WHITE but WHITE is already taken
            gameService.joinGame(result2.authToken(), ChessGame.TeamColor.WHITE, gameID);

            Assertions.fail("Should have thrown 'already taken' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("already taken", e.getMessage(), "Should be 'already taken' error");
        }
    }
}
