package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    Session session;

    public WebSocketFacade(String url) throws Exception {
            url = url.replace("http", "ws");

            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void leave(String authToken, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
        this.session.close();
    }
}
