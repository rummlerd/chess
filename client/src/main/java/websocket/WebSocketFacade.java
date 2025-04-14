package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws Exception {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

                    switch (serverMessage.getServerMessageType()) {
                        case NOTIFICATION -> notificationHandler.notify(new Gson().fromJson(message, Notification.class));
                        case LOAD_GAME -> notificationHandler.notify(new Gson().fromJson(message, LoadGameMessage.class));
                        case ERROR -> notificationHandler.notify(new Gson().fromJson(message, ErrorMessage.class));
                    }
                }
            });
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, Integer gameID, ChessGame.TeamColor teamColor) throws Exception {
        var command = new ConnectCommand(authToken, gameID, teamColor);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void leave(String authToken, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
        this.session.close();
    }

    public void move(String authToken, Integer gameID, ChessMove move, String userName) throws Exception {
        var command = new MakeMoveCommand(authToken, gameID, move, userName);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void highlight(String authToken, Integer gameID, ChessPosition position) throws Exception {
        var command = new HighlightCommand(authToken, gameID, position);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void resign(String authToken, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void redraw(String authToken, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.REDRAW, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}
