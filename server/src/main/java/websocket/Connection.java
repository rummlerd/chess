package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public class Connection {
    private final Session session;
    private final String authToken;
    private final Integer gameID;
    private final String userName;

    public Connection(Session session, String authToken, Integer gameID, String userName) {
        this.session = session;
        this.authToken = authToken;
        this.gameID = gameID;
        this.userName = userName;
    }

    public Session getSession() {
        return session;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getUserName() {
        return userName;
    }

    public void send(ServerMessage msg) throws Exception {
        session.getRemote().sendString(msg.toString());
    }
}
