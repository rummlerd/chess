package server;

import dataaccess.DataAccess;
import spark.Spark;

public class ChessServer {
    public ChessServer run(int port, DataAccess dataAccess) {
        Spark.port(port);

        Spark.staticFiles.location("public");

        Spark.before((req, res) -> {
            res.type("application/json");  // Set content type globally to JSON
            res.status(200); // Default response status, updated only if error is thrown
        });

        RouteManager routeManager = new RouteManager(dataAccess);
        routeManager.setupRoutes();

        Spark.awaitInitialization();
        System.out.println("Server running on http://localhost:" + port);
        return this;
    }

    public int port() {
        return Spark.port();
    }
}

