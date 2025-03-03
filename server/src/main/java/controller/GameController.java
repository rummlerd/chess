package controller;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Spark;
import service.GameService;
import dataaccess.DataAccess;
import model.UserData;
import model.AuthData;
import java.util.Map;

public class GameController {
    private final service.GameService gameService;
    private final Gson gson = new Gson();

    public GameController(DataAccess dataAccess) {
        this.gameService = new GameService(dataAccess);
    }

    public void setupRoutes() {
        Spark.post("/game", this::createGame);
    }

    private Object createGame(Request req, Response res) {
        res.type("application/json");

        try {
            String authToken = req.headers("authorization");
            String gameName = gson.fromJson(req.body(), String.class);

            // Validate input (authToken must not be null)
            if (gameName == null) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            int result = gameService.createGame(authToken, gameName);
            res.status(200);
            return gson.toJson(Map.of("gameID", result));
        } catch (IllegalArgumentException e) {
            res.status(401); // unauthorized
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);  // Internal server error
            return gson.toJson(Map.of("message", "Error: unexpected server issue"));
        }
    }
}
