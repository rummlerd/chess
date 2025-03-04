package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class GameTests {

    @Test
    @DisplayName("Create new game")
    public void createNewGame() {
        String gameName = "testGame";

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
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
    @DisplayName("Fail to create new game due to bad request")
    public void createNewGameBadRequest() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            gameService.createGame(result.authToken(), null);

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to create new game due to unauthorized")
    public void createNewGameUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        try {
            gameService.createGame(UUID.randomUUID().toString(), "testName");

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("List all games")
    public void listAllGames() {
        UserData user = new UserData("testUser", "test", "test@");
        String gameName = "testGame";
        String gameName2 = "testGame2";

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
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
    @DisplayName("Unauthorized to list games")
    public void listAllGamesUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        try {
            gameService.getAllGames(UUID.randomUUID().toString());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Join game successfully")
    public void joinGameSuccessfully() {
        UserData user1 = new UserData("testUser1", "test", "test@");
        UserData user2 = new UserData("testUser2", "test", "test@");
        String gameName = "testGame";

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
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
    @DisplayName("Fail to join game due to bad request")
    public void joinGameBadRequest() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            gameService.joinGame(result.authToken(), ChessGame.TeamColor.WHITE, -1);

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to join game due to unauthorized")
    public void joinGameUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        try {
            gameService.joinGame(UUID.randomUUID().toString(), ChessGame.TeamColor.WHITE, 1234);

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to join game due to color already taken")
    public void joinGameColorAlreadyTaken() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

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
