package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import httpmessages.GameRequest;
import httpmessages.GameResult;
import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.List;
import java.util.Map;

public class RouteManager {
    private final DataAccess dataAccess;
    private final GameService gameService;
    private final UserService userService;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public RouteManager(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gameService = new GameService(dataAccess);
        this.userService = new UserService(dataAccess);
    }

    public void setupRoutes() {
        // Exception messages for all handlers
        Spark.exception(Exception.class, (e, req, res) -> {
            if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            res.body("{\"message\": \"Error: " + e.getMessage() + "\"}");
        });

        Spark.delete("/db", this::clearApplication);
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
    }

    private Object clearApplication(Request req, Response res) {
        dataAccess.clear();
        return "{}";
    }

    private Object registerUser(Request req, Response res) throws IllegalArgumentException {
        UserData user = gson.fromJson(req.body(), UserData.class);

        // Validate that all necessary fields were input
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new IllegalArgumentException("bad request");
        }

        AuthData result = userService.register(user);
        return gson.toJson(result);
    }

    private Object login(Request req, Response res) {
        // Deserialize request as a UserData object, just ignore the email field
        UserData user = gson.fromJson(req.body(), UserData.class);

        AuthData result = userService.login(user);
        return gson.toJson(result);
    }

    private Object logout(Request req, Response res) {
        String authToken = req.headers("authorization");

        userService.logout(authToken);
        return "{}";
    }

    private Object listGames(Request req, Response res) {
        String authToken = req.headers("authorization");

        List<GameResult> games = gameService.getAllGames(authToken);
        return gson.toJson(Map.of("games", games));
    }

    private Object createGame(Request req, Response res) throws IllegalArgumentException {
        String authToken = req.headers("authorization");
        String gameName = (String) gson.fromJson(req.body(), Map.class).get("gameName");

        // Validate that a gameName was input
        if (gameName == null) {
            throw new IllegalArgumentException("bad request");
        }

        int result = gameService.createGame(authToken, gameName);
        return gson.toJson(Map.of("gameID", result));
    }

    private Object joinGame(Request req, Response res) throws IllegalArgumentException {
        String authToken = req.headers("authorization");
        GameRequest joinRequest = gson.fromJson(req.body(), GameRequest.class);
        ChessGame.TeamColor playerColor = joinRequest.getPlayerColor();
        int gameID = joinRequest.getGameID();

        // Validate input (gameID must not be null)
        if (playerColor == null || gameID == 0) {
            throw new IllegalArgumentException("bad request");
        }

        gameService.joinGame(authToken, playerColor, gameID);
        return "{}";
    }
}

