package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.before((req, res) -> {
            res.type("application/json");  // Set content type globally to JSON
            res.status(200); // Default response status, updated only if error is thrown
        });

        DataAccess dataAccess = new MemoryDataAccess(); // Quickly switch between MemoryDataAccess and SqlDataAccess

        RouteManager routeManager = new RouteManager(dataAccess);
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

