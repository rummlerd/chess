package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import httpmessages.GameRequest;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private WebSocketFacade ws;
    private int currentGameID = 0;
    private final NotificationHandler notificationHandler;
    private String userName = null;
    private ChessGame.TeamColor teamColor = null;

    public ServerFacade(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public void clearDatabase() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    public Object register(UserData userData) throws Exception {
        var path = "/user";
        return this.makeRequest(path, userData);
    }

    public Object login(UserData userData) throws Exception {
        var path = "/session";
        userName = userData.username();
        return this.makeRequest(path, userData);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        userName = null;
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    public void createGame(String authToken, GameData gameData) throws Exception {
        var path = "/game";
        this.makeRequest("POST", path, gameData, null, authToken);
    }

    public List<httpmessages.GameResult> listGames(String authToken) throws Exception {
        var path = "/game";
        GameListResponse response = makeRequest("GET", path, null, GameListResponse.class, authToken);
        return response.games;
    }

    public void playGame(int gameID, String playerColor, String authToken) throws Exception {
        var path = "/game";
        makeRequest("PUT", path, new GameRequest(playerColor, gameID), null, authToken);
    }

    public String drawGame(int gameID, String authToken, boolean whitePerspective) throws Exception {
        var path = "/game?id=" + gameID;
        GameData gameData = makeRequest("GET", path, null, GameData.class, authToken);

        // Connect to the WebSocket
        ws = new WebSocketFacade(serverUrl, notificationHandler);
        ws.connect(authToken, gameID, userName, teamColor);
        currentGameID = gameID;

        if (!whitePerspective) {
            return gameData.game().getBoard().toStringFromBlack();
        }
        return gameData.game().getBoard().toStringFromWhite();
    }

    public void leave(String authToken) throws Exception {
        ws.leave(authToken, currentGameID);
    }

    public void move(String authToken, ChessMove move) throws Exception {
        ws.move(authToken, currentGameID, move, userName);
    }

    @SuppressWarnings("unchecked")
    private <T> T makeRequest(String path, Object request) throws Exception {
        return makeRequest("POST", path, request, (Class<T>) AuthData.class, null); // Call overloaded method with null token
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        URL url = (new URI(serverUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);

        // Add authorization header only if authToken is provided
        if (authToken != null && !authToken.isEmpty()) {
            http.setRequestProperty("authorization", authToken);
        }

        // Handle request body only for methods that need it (POST, PUT)
        if ("POST".equals(method) || "PUT".equals(method)) {
            writeBody(request, http);
        }

        http.connect();
        throwIfNotSuccessful(http);
        return readBody(http, responseClass);
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws Exception {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    InputStreamReader reader = new InputStreamReader(respErr);
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("message")) {
                        throw new Exception(json.get("message").getAsString()); // Extracts just the "error" field
                    }
                }
            }
            throw new Exception("other failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    public void setTeamColor(ChessGame.TeamColor color) {
        teamColor = color;
    }
}
