package controller;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Spark;
import service.UserService;
import dataaccess.DataAccess;
import model.UserData;
import model.AuthData;
import java.util.Map;

public class UserController {
    private final service.UserService userService;
    private final Gson gson = new Gson();

    public UserController(DataAccess dataAccess) {
        this.userService = new UserService(dataAccess);
    }

    public void setupRoutes() {
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
    }

    private Object registerUser(Request req, Response res) {
        res.type("application/json");

        try {
            UserData registerRequest = gson.fromJson(req.body(), UserData.class);

            // Validate input (missing or null fields)
            if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            // Call the service layer to handle the registration
            AuthData result = userService.register(registerRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(400);  // Bad request
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);  // Internal server error
            return gson.toJson(Map.of("message", "Error: unexpected server issue"));
        }
    }

    private Object login(Request req, Response res) {
        res.type("application/json");

        try {
            // Deserialize request as a UserData object, just ignore the email field
            UserData loginRequest = gson.fromJson(req.body(), UserData.class);

            AuthData result = userService.login(loginRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(401);  // unauthorized
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);  // Internal server error
            return gson.toJson(Map.of("message", "Error: unexpected server issue"));
        }
    }
}
