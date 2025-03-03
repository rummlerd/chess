package service;

import dataaccess.DataAccess;
import model.UserData;
import request.RegisterResult;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(UserData registerRequest) {
        try {
            dataAccess.createUser(registerRequest);
            //FIXME return username and authToken
            return new RegisterResult(registerRequest.username(), dataAccess.createAuth(registerRequest.username()));
        } catch (IllegalArgumentException e) {
            return new RegisterResult("Error: " + e.getMessage());
        }
    }
}
