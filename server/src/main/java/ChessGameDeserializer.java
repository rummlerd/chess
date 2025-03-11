import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChessGameDeserializer {
    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private static ChessGame chessGame = new ChessGame();
    private static String serializedGame = gson.toJson(chessGame);

    public static void main(String[] args) {
        System.out.println(serializedGame);

        ChessGame game = gson.fromJson(serializedGame, ChessGame.class);
        System.out.println(game.toString());
    }
}


