package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SqlDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UserTests {

    @Test
    @DisplayName("Register new user")
    public void registerNewUser() {
        UserData user = new UserData("testUser", "test", "test@");
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            AuthData result = userService.register(user);

            Assertions.assertNotNull(result.authToken(), "Should return valid AuthToken");
            Assertions.assertEquals(user.username(), result.username(), "Usernames should match");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }
    @Test
    @DisplayName("Register new user on Database")
    public void registerNewUserDatabase() {
        UserData user = new UserData("testUser", "test", "test@");
        DataAccess dataAccess = new SqlDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            AuthData result = userService.register(user);

            Assertions.assertNotNull(result.authToken(), "Should return valid AuthToken");
            Assertions.assertEquals(user.username(), result.username(), "Usernames should match");
        } catch (DataAccessException e) {
            Assertions.fail("Shouldn't throw error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to bad request")
    public void registerNewUserBadRequest() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            userService.register(new UserData("testUser", null, "test@"));

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to bad request on Database")
    public void registerNewUserBadRequestDatabase() {
        DataAccess dataAccess = new SqlDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            userService.register(new UserData("testUser", null, "test@"));

            Assertions.fail("Should have thrown 'bad request' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("bad request", e.getMessage(), "Should be 'bad request' error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to already taken username")
    public void registerNewUserAlreadyTaken() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            userService.register(new UserData("testUser", "test", "test@"));
            userService.register(new UserData("testUser", "test2", "test@2"));

            Assertions.fail("Should have thrown 'already taken' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("already taken", e.getMessage(), "Should be 'already taken' error");
        }
    }

    @Test
    @DisplayName("Fail to register new user due to already taken username on Database")
    public void registerNewUserAlreadyTakenDatabase() {
        DataAccess dataAccess = new SqlDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            userService.register(new UserData("testUser", "test", "test@"));
            userService.register(new UserData("testUser", "test2", "test@2"));

            Assertions.fail("Should have thrown 'already taken' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("already taken", e.getMessage(), "Should be 'already taken' error");
        }
    }

    @Test
    @DisplayName("Logout a user")
    public void logoutUser() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            userService.logout(result.authToken());
            dataAccess.getAuth(result.authToken());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to logout an unauthorized user")
    public void logoutUserUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        try {
            userService.logout(UUID.randomUUID().toString());

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Login a user")
    public void loginUser() {
        UserData user = new UserData("testUser", "test", "test@");

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
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
    @DisplayName("Fail to login user because of unauthorized username")
    public void loginUsernameUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            userService.logout(result.authToken());
            userService.login(new UserData("testUser2", "test", null));

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Fail to login user because of unauthorized password")
    public void loginPasswordUnauthorized() {
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        try {
            AuthData result = userService.register(new UserData("testUser", "test", "test@"));
            userService.logout(result.authToken());
            userService.login(new UserData("testUser", "test2", null));

            Assertions.fail("Should have thrown 'unauthorized' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("unauthorized", e.getMessage(), "Should be 'unauthorized' error");
        }
    }

    @Test
    @DisplayName("Clear application doesn't throw error")
    public void clearApp() {
        UserData user = new UserData("testUser", "test", "test@");

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
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
}
