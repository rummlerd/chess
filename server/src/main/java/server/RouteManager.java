package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import httpmessages.GameRequest;
import dataaccess.DataAccess;
import model.UserData;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Map;

public class RouteManager {
    private final GameService gameService;
    private final UserService userService;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public RouteManager(DataAccess dataAccess) {
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

    private Object clearApplication(Request req, Response res) throws DataAccessException {
        userService.clearApplication();
        return "{}";
    }

    private Object registerUser(Request req, Response res) throws DataAccessException {
        UserData user = gson.fromJson(req.body(), UserData.class);
        return gson.toJson(userService.register(user));
    }

    private Object login(Request req, Response res) throws DataAccessException {
        // Deserialize request as a UserData object, just ignore the email field
        UserData user = gson.fromJson(req.body(), UserData.class);
        return gson.toJson(userService.login(user));
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        userService.logout(authToken);
        return "{}";
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        return gson.toJson(Map.of("games", gameService.getAllGames(authToken)));
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        String gameName = (String) gson.fromJson(req.body(), Map.class).get("gameName");
        return gson.toJson(Map.of("gameID", gameService.createGame(authToken, gameName)));
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        GameRequest joinRequest = gson.fromJson(req.body(), GameRequest.class);
        ChessGame.TeamColor playerColor = joinRequest.getPlayerColor();
        int gameID = joinRequest.getGameID();

        gameService.joinGame(authToken, playerColor, gameID);
        return "{}";
    }
}

