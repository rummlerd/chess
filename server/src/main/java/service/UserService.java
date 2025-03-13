package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clear();
    }

    public AuthData register(UserData user) throws DataAccessException {
        String username = user.username();

        // Validate that all necessary fields were input
        if (username == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("bad request");
        }

        dataAccess.createUser(user);
        return new AuthData(dataAccess.createAuth(username), username);
    }

    public AuthData login(UserData user) throws DataAccessException {
        String username = user.username();
        if (dataAccess.verifyUser(username, user.password())) {
            return new AuthData(dataAccess.createAuth(username), username);
        }
        throw new DataAccessException("unauthorized");
    }

    public void logout(String authToken) throws DataAccessException {
        dataAccess.deleteAuth(authToken);
    }

    public UserData getUser(String username) throws DataAccessException {
        return dataAccess.getUser(username);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return dataAccess.getAuth(authToken);
    }
}
