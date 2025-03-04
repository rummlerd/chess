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

    public AuthData register(UserData user) throws DataAccessException {
        String username = user.username();
        dataAccess.createUser(user);
        return new AuthData(dataAccess.createAuth(username), username);
    }

    public AuthData login(UserData user) throws DataAccessException {
        String username = user.username();
        if (dataAccess.getUser(username).password().equals(user.password())) {
            return new AuthData(dataAccess.createAuth(username), username);
        }
        throw new DataAccessException("unauthorized");
    }

    public void logout(String authToken) throws DataAccessException {
        dataAccess.deleteAuth(authToken);
    }
}
