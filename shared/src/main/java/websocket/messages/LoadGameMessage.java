package websocket.messages;

import com.google.gson.Gson;
import model.GameData;

public class LoadGameMessage extends ServerMessage {
    public GameData game;
    public String userName;

    public LoadGameMessage(GameData game, String userName) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.userName = userName;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
