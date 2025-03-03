package server;

import controller.UserController;
import dataaccess.DataAccess;
import spark.Request;
import spark.Response;
import spark.Spark;

public class ChessServer {
    public ChessServer run(int port, DataAccess dataAccess) {
        Spark.port(port);

        Spark.staticFiles.location("public");

        UserController userController = new UserController(dataAccess);
        userController.setupRoutes();
        Spark.delete("/db", (req, res) -> {
            try {
                dataAccess.clear();
                res.status(200);
                return "{}";
            } catch (Exception e) {
                res.status(500);
                return "{\"message\": \"Error: " + e.getMessage() + "\"}";
            }
        });

        Spark.awaitInitialization();
        System.out.println("Server running on http://localhost:" + port);
        return this;
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}

