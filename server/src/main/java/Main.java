import chess.*;

import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            var server = new Server();
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