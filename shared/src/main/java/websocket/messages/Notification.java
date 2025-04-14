package websocket.messages;

import com.google.gson.Gson;

public class Notification extends ServerMessage {
    public final String message;

    public Notification(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
