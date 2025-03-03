package server;

import controller.UserController;
import dataaccess.DataAccess;
import spark.Spark;

public class ChessServer {
    public ChessServer run(int port, DataAccess dataAccess) {
        Spark.port(port);

        Spark.staticFiles.location("public");

        //FIXME add routes Spark.post(...) etc.
        UserController userController = new UserController(dataAccess);
        userController.setupRoutes();

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

