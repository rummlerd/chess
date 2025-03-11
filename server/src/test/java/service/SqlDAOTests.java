package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.UUID;

public class SqlDAOTests {
    private static DataAccess dataAccess;
    private static UserService userService;
    private static final UserData user = new UserData("testUser", "test", "test@");

    @BeforeAll
    public static void switchToTestDB() throws Exception {
        DatabaseManager.renameDatabase("test_db");

        dataAccess = new SqlDataAccess();
        userService = new UserService(dataAccess);
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

    // FIXME add test for clearing all tables
}
