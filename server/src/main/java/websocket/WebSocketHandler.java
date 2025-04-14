package websocket;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
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
                case MAKE_MOVE -> handleMove(new Gson().fromJson(msg, MakeMoveCommand.class));
                case LEAVE -> handleLeave(command);
                case RESIGN -> handleResign(command);
            }
        }
        catch (Exception e) {
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
        String userName = userService.getAuth(command.getAuthToken()).username();

        connections.add(session, command, userName);
        String message;

        if (command.getTeamColor() != null) {
            message = String.format("%s joined the game as the %s team", userName, command.getTeamColor());
        } else {
            message = String.format("%s is observing the game", userName);
        }

        ServerMessage notification = new Notification(message);
        ServerMessage loadGame = new LoadGameMessage(game, userName);
        connections.broadcast(command.getAuthToken(), notification);
        connections.broadcast(command.getAuthToken(), loadGame);
    }

    private void handleMove(MakeMoveCommand command) throws Exception, InvalidMoveException {
        GameData game = getValidGame(command);

        String userName = userService.getAuth(command.getAuthToken()).username();

        ChessGame.TeamColor currentTeamPlayer;
        if (userName.equals(game.whiteUsername())) {
            currentTeamPlayer = ChessGame.TeamColor.WHITE;
        } else {
            currentTeamPlayer = ChessGame.TeamColor.BLACK;
        }
        if (!game.game().getTeamTurn().equals(currentTeamPlayer)) {
            throw new Exception("not your turn");
        }

        game.game().makeMove(command.getMove());
        gameService.updateGame(game.gameID(), game.game());

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

    private void handleLeave(UserGameCommand command) throws Exception {
        GameData game = getValidGame(command);

        String userName = userService.getAuth(command.getAuthToken()).username();

        if (userName.equals(game.whiteUsername()) || userName.equals(game.blackUsername())) {
            gameService.leaveGame(command.getAuthToken(), command.getGameID());
        }
        connections.remove(command.getAuthToken());

        String message = String.format("%s has left the game", userService.getAuth(command.getAuthToken()).username());
        ServerMessage leaveNotification = new Notification(message);
        connections.broadcast(command.getAuthToken(), leaveNotification);
    }

    private void handleResign(UserGameCommand command) throws Exception {
        GameData game = getValidGame(command);

        String userName = userService.getAuth(command.getAuthToken()).username();

        if (!(userName.equals(game.whiteUsername()) || userName.equals(game.blackUsername()))) {
            throw new Exception("observers cannot resign from game");
        } else if (game.game().isGameOver()) {
            throw new Exception("this game is already over");
        }

        game.game().endGame();
        gameService.updateGame(game.gameID(), game.game());

        String message = String.format("%s has resigned from the game", userService.getAuth(command.getAuthToken()).username());
        ServerMessage leaveNotification = new Notification(message);
        connections.broadcast(null, leaveNotification);
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
}
