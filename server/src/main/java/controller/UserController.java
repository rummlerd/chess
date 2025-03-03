package controller;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Spark;
import service.UserService;
import request.RegisterResult;
import dataaccess.DataAccess;
import model.UserData;

public class UserController {
    private final service.UserService userService;
    private final Gson gson = new Gson();

    public UserController(DataAccess dataAccess) {
        this.userService = new UserService(dataAccess);
    }

    public void setupRoutes() {
        Spark.post("/user", this::registerUser);
    }

    private Object registerUser(Request req, Response res) {
        res.type("application/json");

        try {
            UserData registerRequest = gson.fromJson(req.body(), UserData.class);

            if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
                res.status(400);
                return gson.toJson(new RegisterResult("Error: bad request"));
            }

            // Call the service layer to handle the registration
            RegisterResult result = userService.register(registerRequest);

            if (result.getMessage() == null) {
                res.status(200);
            } else if (result.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }

            return gson.toJson(result);
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new RegisterResult("Error: internal server error")); //FIXME error should be a description of error
        }
    }
}
