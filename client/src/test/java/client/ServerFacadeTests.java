package client;

import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import websocket.NotificationHandler;
import websocket.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static ServerFacade facade;
    private static Server server;
    private static final UserData USER_DATA = new UserData("player1", "password", "p1@email.com");
    private static final GameData TEST_GAME = new GameData(0, null, null, "Test Game", null);

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String serverUrl = "http://localhost:" + port;

        facade = new ServerFacade(serverUrl, null);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clearDatabase();
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void clearDatabaseTest() throws Exception {
        facade.register(USER_DATA);
        facade.clearDatabase();
        try {
            facade.login(USER_DATA);
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void register() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerMissingInput() {
        try {
            facade.register(new UserData(null, null, null));
            Assertions.fail("Should have thrown 'bad request' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: bad request", e.getMessage(), "Should be 'Error: bad request'");
        }
    }

    @Test
    void registerExistingUser() {
        try {
            facade.register(USER_DATA);
            facade.register(USER_DATA);
            Assertions.fail("Should have thrown 'already taken' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: already taken", e.getMessage(), "Should be 'Error: already taken'");
        }
    }

    @Test
    void login() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.logout(authData.authToken());
        Object res2 = facade.login(USER_DATA);
        AuthData authData2 = (AuthData) res2;
        assertTrue(authData2.authToken().length() > 10);
    }

    @Test
    void loginNonExistingUser() {
        try {
            Object res = facade.register(USER_DATA);
            AuthData authData = (AuthData) res;
            facade.logout(authData.authToken());
            facade.login(new UserData(null, null, null));
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void logout() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.logout(authData.authToken());
        try {
            facade.listGames(authData.authToken());
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void logoutInvalidToken() {
        try {
            facade.logout("invalid_token");
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void createGame() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.createGame(authData.authToken(), TEST_GAME);
        List<GameResult> games = facade.listGames(authData.authToken());
        assertTrue(games.stream().anyMatch(game -> game.gameName().equals("Test Game")),
                "Created game should appear in the list");
    }

    @Test
    void createGameUnauthorized() {
        try {
            facade.createGame("invalid_token", TEST_GAME);
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void listGames() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.createGame(authData.authToken(), new GameData(0, null, null, "Game One", null));
        facade.createGame(authData.authToken(), new GameData(0, null, null, "Game Two", null));
        List<httpmessages.GameResult> games = facade.listGames(authData.authToken());
        assertTrue(games.stream().anyMatch(game -> game.gameName().equals("Game One")),
                "Game One should appear in the list");
        assertTrue(games.stream().anyMatch(game -> game.gameName().equals("Game Two")),
                "Game Two should appear in the list");
    }

    @Test
    void listGamesUnauthorized() {
        try {
            facade.listGames("invalid_token");
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void playGame() {
        try {
            Object res = facade.register(USER_DATA);
            AuthData authData = (AuthData) res;
            facade.createGame(authData.authToken(), TEST_GAME);
            List<httpmessages.GameResult> games = facade.listGames(authData.authToken());
            int gameID = games.getFirst().gameID();
            facade.playGame(gameID, "WHITE", authData.authToken());
        } catch (Exception e) {
            Assertions.fail("Should not have thrown error");
        }
    }

    @Test
    void playGameUnauthorized() {
        try {
            facade.playGame(1, "WHITE", "invalid_token");
            Assertions.fail("Should have thrown 'bad request' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: bad request", e.getMessage(), "Should be 'Error: bad request'");
        }
    }

    @Test
    void playGameInvalidGameID() {
        try {
            Object res = facade.register(USER_DATA);
            AuthData authData = (AuthData) res;
            facade.playGame(-1, "WHITE", authData.authToken());
            Assertions.fail("Should have thrown 'bad request' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: bad request", e.getMessage(), "Should be 'Error: bad request'");
        }
    }

    @Test
    void drawGameContainsPieces() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.createGame(authData.authToken(), TEST_GAME);
        List<httpmessages.GameResult> games = facade.listGames(authData.authToken());
        int gameID = games.getFirst().gameID();

        String boardString = facade.drawGame(gameID, authData.authToken(), true);

        Assertions.assertTrue(boardString.contains("K"), "Board should contain a king");
        Assertions.assertTrue(boardString.contains("P"), "Board should contain pawns");
    }

    @Test
    void drawGameUnauthorized() {
        try {
            facade.drawGame(1, "invalid_token", true);
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }

    @Test
    void drawGameInvalidGameID() {
        try {
            Object res = facade.register(USER_DATA);
            AuthData authData = (AuthData) res;
            facade.drawGame(-1, authData.authToken(), true);
            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Should be 'Error: unauthorized'");
        }
    }
}
