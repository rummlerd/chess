package server;

import com.google.gson.Gson;
import spark.*;

public class ChessServer {
    public ChessServer run(int port) {
        Spark.port(port);

        Spark.staticFiles.location("public");

        //FIXME add routes Spark.post(...) etc.
        // Sample route
        Spark.get("/hello", (req, res) -> "Hello, Chess Server!");

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
