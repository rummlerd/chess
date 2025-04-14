package websocket;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.ConnectCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    public final ConcurrentHashMap<ConnectionKey, Connection> connections = new ConcurrentHashMap<>();

    public synchronized void add(Session session, ConnectCommand cmd, String userName) {
        var connection = new Connection(session, cmd.getAuthToken(), cmd.getGameID(), userName);
        ConnectionKey key = new ConnectionKey(cmd.getAuthToken(), cmd.getGameID());
        connections.put(key, connection);
    }

    public void remove(String authToken, Integer gameID) {
        ConnectionKey key = new ConnectionKey(authToken, gameID);
        connections.remove(key);
    }

    public void broadcast(String excludeAuthToken, Integer gameID, ServerMessage message) throws Exception {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session().isOpen()) {
                if (!c.authToken().equals(excludeAuthToken) &&
                        message.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)
                        && c.gameID().equals(gameID)) {
                    c.send(message);
                } else if (c.authToken().equals(excludeAuthToken) &&
                        message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)
                        && c.gameID().equals(gameID)) {
                    c.send(message);
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open
        for (var c : removeList) {
            connections.remove(new ConnectionKey(c.authToken(), c.gameID()));
        }
    }

    public void reloadBoard(LoadGameMessage loadGame, boolean once, String userName) throws Exception {
        if (once) {
            for (var c : connections.values()) {
                if (userName.equals(c.userName())) {
                    LoadGameMessage newLoadGame = new LoadGameMessage(loadGame.getGame(), userName);
                    c.send(newLoadGame);
                }
            }
        } else {
            for (var c : connections.values()) {
                if (c.session().isOpen() && c.gameID().equals(loadGame.getGame().gameID())) {
                    LoadGameMessage newLoadGame = new LoadGameMessage(loadGame.getGame(), c.userName());
                    c.send(newLoadGame);
                }
            }
        }
    }
}
