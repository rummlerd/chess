package service;

import dataaccess.DataAccess;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
/*
    public RegisterResult register(RegisterRequest registerRequest) {
        try {
            dataAccess.createUser(new UserData(registerRequest.username(), registerRequest.password()));
            return new RegisterResult("User registered successfully");
        } catch (IllegalArgumentException e) {
            return new RegisterResult("Error: " + e.getMessage());
        }
    }
 */

    //FIXME public LoginResult login(LoginRequest loginRequest) {}
    //FIXME public void logout(LogoutRequest logoutRequest) {}
}
