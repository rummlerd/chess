package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SqlDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

public class SqlDAOTests {
    private static UserService userService;

    @BeforeAll
    public static void switchToTestDB() throws Exception {
        DatabaseManager.renameDatabase("test_db");

        // Initialize services using new test database
        DataAccess dataAccess = new SqlDataAccess();
        userService = new UserService(dataAccess);
    }

    @BeforeEach
    public void resetTestDB() throws Exception {
        userService.clearApplication();
    }

    @AfterAll
    public static void switchToChessDB() throws Exception {
        // Force DatabaseManager to reload original properties
        DatabaseManager.renameDatabase("chess");
    }
    
    @Test
    @DisplayName("Register new user on Database")
    public void registerNewUserDatabase() {
        UserData user = new UserData("testUser", "test", "test@");
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
            userService.register(new UserData("testUser", "test", "test@"));
            userService.register(new UserData("testUser", "test2", "test@2"));

            Assertions.fail("Should have thrown 'already taken' error");
        } catch (DataAccessException e) {
            Assertions.assertEquals("already taken", e.getMessage(), "Should be 'already taken' error");
        }
    }
}
