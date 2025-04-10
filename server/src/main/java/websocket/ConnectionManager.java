package websocket;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public synchronized void add(Session session, UserGameCommand cmd) {
        String authToken = cmd.getAuthToken();
        var connection = new Connection(session, cmd.getAuthToken(), cmd.getGameID());
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }
}
