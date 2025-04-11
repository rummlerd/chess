package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {
    private final GameService gameService;
    private final UserService userService;
    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(DataAccess dataAccess) {
        this.gameService = new GameService(dataAccess);
        this.userService = new UserService(dataAccess);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) throws Exception {
        UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(session, new Gson().fromJson(msg, ConnectCommand.class));
            case MAKE_MOVE -> handleMove(command);
            case LEAVE -> handleLeave(command);
            case RESIGN -> handleResign(command);
        }
    }

    private void handleConnect(Session session, ConnectCommand command) throws Exception {
        connections.add(session, command);
        String message;

        if (command.getTeamColor() != null) {
            message = String.format("%s joined the game as the %s team", command.getUserName(), command.getTeamColor());
        } else {
            message = String.format("%s is observing the game", command.getUserName());
        }

        ServerMessage notification = new Notification(message);
        ServerMessage loadGame = new LoadGameMessage(gameService.getGame(command.getGameID()), command.getUserName());
        connections.broadcast(command.getAuthToken(), notification);
        connections.broadcast(command.getAuthToken(), loadGame);
        System.out.println(loadGame);
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
