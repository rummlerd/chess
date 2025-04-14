package websocket;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessGame;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.ConnectCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public synchronized void add(Session session, ConnectCommand cmd, String userName) {
        String authToken = cmd.getAuthToken();
        var connection = new Connection(session, cmd.getAuthToken(), cmd.getGameID(), userName);
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcast(String excludeAuthToken, ServerMessage message) throws Exception {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.getSession().isOpen()) {
                if (!c.getAuthToken().equals(excludeAuthToken) &&
                        message.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)) {
                    c.send(message);
                } else if (c.getAuthToken().equals(excludeAuthToken) &&
                        message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
                    c.send(message);
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

    public void reloadBoard(LoadGameMessage loadGame) throws Exception {
        for (var c : connections.values()) {
            if (c.getSession().isOpen()) {
                String userName = c.getUserName();
                GameData game = loadGame.getGame();
                LoadGameMessage newLoadGame = new LoadGameMessage(game, userName);
                c.send(newLoadGame);
            }
        }
    }
}
