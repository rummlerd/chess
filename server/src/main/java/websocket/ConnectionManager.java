package websocket;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.commands.ConnectCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public synchronized void add(Session session, ConnectCommand cmd) {
        String authToken = cmd.getAuthToken();
        var connection = new Connection(session, cmd.getAuthToken(), cmd.getGameID());
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcast(String excludeAuthToken, ServerMessage message) throws Exception {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.getSession().isOpen()) {
                if (!c.getAuthToken().equals(excludeAuthToken) && message.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)) {
                    c.send(message);
                } else if (c.getAuthToken().equals(excludeAuthToken) && message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
                    c.send(message);
                    System.out.println("sending a LOAD_GAME message");
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open
        for (var c : removeList) {
            connections.remove(c.getAuthToken());
        }
    }
}
