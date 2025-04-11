package server;

import dataaccess.DataAccess;
import dataaccess.SqlDataAccess;
import spark.*;
import websocket.WebSocketHandler;

public class Server {
    private final WebSocketHandler webSocketHandler;
    private final RouteManager routeManager;


    public Server() {
        // Quickly switch between MemoryDataAccess and SqlDataAccess
        DataAccess dataAccess = new SqlDataAccess();
        webSocketHandler = new WebSocketHandler(dataAccess);
        routeManager = new RouteManager(dataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        Spark.before((req, res) -> {
            res.type("application/json");  // Set content type globally to JSON
            res.status(200); // Default response status, updated only if error is thrown
        });

        routeManager.setupRoutes();

        Spark.awaitInitialization();
        System.out.println("Server running on http://localhost:" + desiredPort);
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

