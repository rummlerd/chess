package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public record Connection(Session session, String authToken, Integer gameID, String userName) {

    public void send(ServerMessage msg) throws Exception {
        session.getRemote().sendString(msg.toString());
    }
}
