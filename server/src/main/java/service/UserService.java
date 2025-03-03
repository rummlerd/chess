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
        dataAccess.createUser(registerRequest);
        return new AuthData(dataAccess.createAuth(registerRequest.username()), registerRequest.username());
    }
}
