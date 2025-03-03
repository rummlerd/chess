package service;

import dataaccess.DataAccess;
import model.UserData;
import model.AuthData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData registerRequest) {
        String username = registerRequest.username();
        dataAccess.createUser(registerRequest);
        return new AuthData(dataAccess.createAuth(username), username);
    }

    public AuthData login(UserData loginRequest) throws IllegalArgumentException {
        String username = loginRequest.username();
        UserData user = dataAccess.getUser(username);
        if (user.password().equals(loginRequest.password())) {
            return new AuthData(dataAccess.createAuth(username), username);
        }
        throw new IllegalArgumentException("unauthorized");
    }
}
