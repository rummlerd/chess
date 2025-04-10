package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(session, command);
            case MAKE_MOVE -> handleMove(command);
            case LEAVE -> handleLeave(command);
            case RESIGN -> handleResign(command);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        connections.add(session, command);
        System.out.println("A websocket connection has been made");
    }

    private void handleMove(UserGameCommand command) {

    }

    private void handleLeave(UserGameCommand command) {
        connections.remove(command.getAuthToken());
        System.out.println("A websocket connection has been terminated");
    }

    private void handleResign(UserGameCommand command) {

    }
}
