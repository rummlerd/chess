import chess.*;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            DataAccess dataAccess = new MemoryDataAccess(); // Quickly switch between MemoryDataAccess and SqlDataAccess

            var server = new Server(dataAccess);
            server.run(port);
            port = server.port();

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Throwable ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}