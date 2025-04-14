package websocket;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public void onMessage(Session session, String msg) {
        try {
            UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, new Gson().fromJson(msg, ConnectCommand.class));
                case MAKE_MOVE -> handleMove(session, new Gson().fromJson(msg, MakeMoveCommand.class));
                case LEAVE -> handleLeave(command);
                case RESIGN -> handleResign(command);
            }
        } catch (Exception e) {
            ServerMessage error = new ErrorMessage("Error: " + e.getMessage());
            try {
                session.getRemote().sendString(error.toString());
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    private void handleConnect(Session session, ConnectCommand command) throws Exception {
        GameData game = getValidGame(command);

        connections.add(session, command);
        String message;

        if (command.getTeamColor() != null) {
            message = String.format("%s joined the game as the %s team", command.getUserName(), command.getTeamColor());
        } else {
            message = String.format("%s is observing the game", command.getUserName());
        }

        ServerMessage notification = new Notification(message);
        ServerMessage loadGame = new LoadGameMessage(game, command.getUserName());
        connections.broadcast(command.getAuthToken(), notification);
        connections.broadcast(command.getAuthToken(), loadGame);
    }

    private void handleMove(Session session, MakeMoveCommand command) throws Exception {
        GameData game = getValidGame(command);
        try {
            System.out.println(game.game());
            game.game().makeMove(command.getMove());
            System.out.println(game.game());
        } catch (InvalidMoveException ex) {
            throw new Exception("Invalid move");
        }
        String startPosition = positionToString(command.getMove().startPosition());
        String endPosition = positionToString(command.getMove().endPosition());
        String message = String.format("%s moved their piece at %s to %s", command.getUserName(), startPosition, endPosition);
        ServerMessage moveNotification = new Notification(message);
        LoadGameMessage loadGame = new LoadGameMessage(game, command.getUserName());
        connections.broadcast(command.getAuthToken(), moveNotification);
        try {
            connections.reloadBoard(loadGame);
        } catch (Exception e) {
            throw new Exception("Failed to reload board");
        }
    }

    public String positionToString(ChessPosition position) {
        int row = position.getRow();  // Should be in range 1–8
        int col = position.getColumn();  // Should be in range 1–8

        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new IllegalArgumentException("Invalid ChessPosition: " + row + ", " + col);
        }

        // Convert column number to a letter
        char colChar = (char) ('a' + col - 1);

        return String.valueOf(colChar) + row;
    }

    private void handleLeave(UserGameCommand command) {
        connections.remove(command.getAuthToken());
        System.out.println("A websocket connection has been terminated");
    }

    private void handleResign(UserGameCommand command) {

    }

    private GameData getValidGame(UserGameCommand command) throws Exception {
        GameData game;
        try {
            gameService.getAllGames(command.getAuthToken());
        } catch (DataAccessException ex) {
            throw new Exception("unauthorized");
        }
        try {
            game = gameService.getGame(command.getGameID());
        } catch (DataAccessException ex) {
            throw new Exception("Invalid game ID");
        }
        if (game == null || game.game() == null) {
            throw new Exception("Invalid game ID");
        }
        return game;
    }
}
