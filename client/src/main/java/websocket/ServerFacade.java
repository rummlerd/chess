package websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import httpmessages.GameRequest;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Object login(UserData userData) throws Exception {
        var path = "/session";
        return this.makeRequest("POST", path, userData, AuthData.class);
    }

    public Object register(UserData userData) throws Exception {
        var path = "/user";
        return this.makeRequest("POST", path, userData, AuthData.class);
    }

    public String logout(String authToken) throws Exception {
        var path = "/session";
        return this.makeRequest("DELETE", path, null, null, authToken);
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

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        return makeRequest(method, path, request, responseClass, null); // Call overloaded method with null token
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        try {
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
        } catch (Exception e) {
            throw e;
        } // FIXME add more specific error handling
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
}
