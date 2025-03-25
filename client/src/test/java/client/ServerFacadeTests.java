package client;

import httpmessages.GameResult;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import websocket.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static ServerFacade facade;
    private static Server server;
    private static final UserData USER_DATA = new UserData("player1", "password", "p1@email.com");
    private static final GameData TEST_GAME = new GameData(0, null, null, "Test Game", null);
    private static final String EXPECTED_BOARD_WHITE = """
            \t[1m[48;5;235m[38;5;0m    a  b  c  d  e  f  g  h    [49m
            \t[48;5;235m[38;5;0m 8 [48;5;252m[38;5;12m R [48;5;0m[38;5;12m N [48;5;252m[38;5;12m B [48;5;0m[38;5;12m Q [48;5;252m[38;5;12m K [48;5;0m[38;5;12m B [48;5;252m[38;5;12m N [48;5;0m[38;5;12m R [48;5;235m[38;5;0m 8 [49m[39m
            \t[48;5;235m[38;5;0m 7 [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;235m[38;5;0m 7 [49m[39m
            \t[48;5;235m[38;5;0m 6 [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;235m[38;5;0m 6 [49m[39m
            \t[48;5;235m[38;5;0m 5 [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;235m[38;5;0m 5 [49m[39m
            \t[48;5;235m[38;5;0m 4 [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;235m[38;5;0m 4 [49m[39m
            \t[48;5;235m[38;5;0m 3 [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;235m[38;5;0m 3 [49m[39m
            \t[48;5;235m[38;5;0m 2 [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;235m[38;5;0m 2 [49m[39m
            \t[48;5;235m[38;5;0m 1 [48;5;0m[38;5;160m R [48;5;252m[38;5;160m N [48;5;0m[38;5;160m B [48;5;252m[38;5;160m Q [48;5;0m[38;5;160m K [48;5;252m[38;5;160m B [48;5;0m[38;5;160m N [48;5;252m[38;5;160m R [48;5;235m[38;5;0m 1 [49m[39m
            \t[48;5;235m[38;5;0m    a  b  c  d  e  f  g  h    [49m[39m[22m""";
    private static final String EXPECTED_BOARD_BLACK = """
            \t[1m[48;5;235m[38;5;0m    h  g  f  e  d  c  b  a    [49m
            \t[48;5;235m[38;5;0m 1 [48;5;252m[38;5;160m R [48;5;0m[38;5;160m N [48;5;252m[38;5;160m B [48;5;0m[38;5;160m Q [48;5;252m[38;5;160m K [48;5;0m[38;5;160m B [48;5;252m[38;5;160m N [48;5;0m[38;5;160m R [48;5;235m[38;5;0m 1 [49m[39m
            \t[48;5;235m[38;5;0m 2 [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;0m[38;5;160m P [48;5;252m[38;5;160m P [48;5;235m[38;5;0m 2 [49m[39m
            \t[48;5;235m[38;5;0m 3 [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;235m[38;5;0m 3 [49m[39m
            \t[48;5;235m[38;5;0m 4 [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;235m[38;5;0m 4 [49m[39m
            \t[48;5;235m[38;5;0m 5 [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;235m[38;5;0m 5 [49m[39m
            \t[48;5;235m[38;5;0m 6 [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;0m   [48;5;252m   [48;5;235m[38;5;0m 6 [49m[39m
            \t[48;5;235m[38;5;0m 7 [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;252m[38;5;12m P [48;5;0m[38;5;12m P [48;5;235m[38;5;0m 7 [49m[39m
            \t[48;5;235m[38;5;0m 8 [48;5;0m[38;5;12m R [48;5;252m[38;5;12m N [48;5;0m[38;5;12m B [48;5;252m[38;5;12m Q [48;5;0m[38;5;12m K [48;5;252m[38;5;12m B [48;5;0m[38;5;12m N [48;5;252m[38;5;12m R [48;5;235m[38;5;0m 8 [49m[39m
            \t[48;5;235m[38;5;0m    h  g  f  e  d  c  b  a    [49m[39m[22m""";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String serverUrl = "http://localhost:" + port;

        facade = new ServerFacade(serverUrl);
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
    void drawGame() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.createGame(authData.authToken(), TEST_GAME);
        List<httpmessages.GameResult> games = facade.listGames(authData.authToken());
        int gameID = games.getFirst().gameID();
        String boardString = facade.drawGame(gameID, authData.authToken(), true);
        Assertions.assertEquals(EXPECTED_BOARD_WHITE, boardString, "Board should match the expected starting state");
    }

    @Test
    void drawGameBlack() throws Exception {
        Object res = facade.register(USER_DATA);
        AuthData authData = (AuthData) res;
        facade.createGame(authData.authToken(), TEST_GAME);
        List<httpmessages.GameResult> games = facade.listGames(authData.authToken());
        int gameID = games.getFirst().gameID();
        String boardString = facade.drawGame(gameID, authData.authToken(), false);
        Assertions.assertEquals(EXPECTED_BOARD_BLACK, boardString, "Board should match the expected starting state");
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
