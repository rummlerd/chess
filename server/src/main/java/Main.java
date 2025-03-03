import chess.*;

import server.ChessServer;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

public class Main {
    public static void main(String[] args) {
        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            DataAccess dataAccess = new MemoryDataAccess();

            var server = new ChessServer().run(port, dataAccess);
            port = server.port();
            System.out.println("Server is running at http://localhost:" + port);

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Throwable ex) {
            //FIXME add error handling
        }

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}