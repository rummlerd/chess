package controller;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.MemoryDataAccess;
import model.UserData;
import spark.Request;
import spark.Response;
import spark.Spark;
import service.GameService;
import dataaccess.DataAccess;

import java.util.List;
import java.util.Map;

public class GameController {
    private final service.GameService gameService;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public GameController(DataAccess dataAccess) {
        this.gameService = new GameService(dataAccess);
    }

    public void setupRoutes() {
        Spark.post("/game", this::createGame);
        Spark.get("/game", this::listGames);
        Spark.put("/game", this::joinGame);
    }

    private Object createGame(Request req, Response res) {
        res.type("application/json");

        try {
            String authToken = req.headers("authorization");
            String gameName = (String) gson.fromJson(req.body(), Map.class).get("gameName");

            // Validate input (gameName must not be null)
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

    private Object listGames(Request req, Response res) {
        res.type("application/json");

        try {
            String authToken = req.headers("authorization");

            List<GameResult> games = gameService.getAllGames(authToken);
            res.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (IllegalArgumentException e) {
            res.status(401); // unauthorized
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);  // Internal server error
            return gson.toJson(Map.of("message", "Error: unexpected server issue"));
        }
    }

    private Object joinGame(Request req, Response res) {
        res.type("application/json");

        try {
            String authToken = req.headers("authorization");
            GameRequest joinRequest = gson.fromJson(req.body(), GameRequest.class);
            TeamColor playerColor = joinRequest.getPlayerColor();
            int gameID = joinRequest.getGameID();

            // Validate input (gameID must not be null)
            if (playerColor == null || gameID == 0) {
                throw new IllegalArgumentException("bad request");
            }

            gameService.joinGame(authToken, playerColor, gameID);
            res.status(200);
            return "{}";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(403); // already taken
            }
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);  // Internal server error
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
