package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;

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
    public void onMessage(Session session, String msg) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, new Gson().fromJson(msg, ConnectCommand.class));
                case MAKE_MOVE -> handleMove(new Gson().fromJson(msg, MakeMoveCommand.class));
                case LEAVE -> handleLeave(command);
                case RESIGN -> handleResign(command);
                case REDRAW -> handleRedraw(command);
                case HIGHLIGHT -> handleHighlight(session, new Gson().fromJson(msg, HighlightCommand.class));
            }
        }
        catch (Exception e) {
            ServerMessage error = new ErrorMessage("Error: " + e.getMessage());
            session.getRemote().sendString(error.toString());
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
        connections.broadcast(command.getAuthToken(), command.getGameID(), loadGame);
        connections.broadcast(command.getAuthToken(), command.getGameID(), notification);
    }

    private void handleMove(MakeMoveCommand command) throws Exception {
        GameData gameData = getValidGame(command);
        ChessGame game = gameData.game();

        String userName = userService.getAuth(command.getAuthToken()).username();
        String whiteUserName = gameData.whiteUsername();
        String blackUserName = gameData.blackUsername();

        ChessGame.TeamColor currentTeamPlayer;
        if (userName.equals(whiteUserName)) {
            currentTeamPlayer = ChessGame.TeamColor.WHITE;
        } else {
            currentTeamPlayer = ChessGame.TeamColor.BLACK;
        }
        if (!game.getTeamTurn().equals(currentTeamPlayer)) {
            throw new Exception("not your turn");
        }

        game.makeMove(command.getMove());
        gameService.updateGame(gameData.gameID(), game);

        String startPosition = positionToString(command.getMove().startPosition());
        String endPosition = positionToString(command.getMove().endPosition());
        String message = String.format("%s moved their piece at %s to %s", userName, startPosition, endPosition);

        String checkMessage = null;
        boolean alert = false;
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            alert = true;
            checkMessage = String.format("%s is in checkmate, game over", whiteUserName);
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            alert = true;
            checkMessage = String.format("%s is in checkmate, game over", blackUserName);
        } else if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            alert = true;
            checkMessage = String.format("%s is in stalemate, game over", whiteUserName);
        } else if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            alert = true;
            checkMessage = String.format("%s is in stalemate, game over", blackUserName);
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            alert = true;
            checkMessage = String.format("%s is in check", whiteUserName);
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            alert = true;
            checkMessage = String.format("%s is in check", blackUserName);
        }
        if (alert) {
            message = message + "\n" + checkMessage;
        }

        LoadGameMessage loadGame = new LoadGameMessage(gameData, userName);
        connections.reloadBoard(loadGame, false, userName);

        ServerMessage moveNotification = new Notification(message);
        connections.broadcast(command.getAuthToken(), command.getGameID(), moveNotification);
    }

    private void handleRedraw(UserGameCommand command) throws Exception {
        GameData gameData = getValidGame(command);
        String userName = userService.getAuth(command.getAuthToken()).username();

        LoadGameMessage loadGame = new LoadGameMessage(gameData, userName);
        connections.reloadBoard(loadGame, true, userName);
    }

    private void handleLeave(UserGameCommand command) throws Exception {
        GameData game = getValidGame(command);

        String userName = userService.getAuth(command.getAuthToken()).username();

        if (userName.equals(game.whiteUsername()) || userName.equals(game.blackUsername())) {
            gameService.leaveGame(command.getAuthToken(), command.getGameID());
        }
        connections.remove(command.getAuthToken(), command.getGameID());

        String message = String.format("%s has left the game", userService.getAuth(command.getAuthToken()).username());
        ServerMessage leaveNotification = new Notification(message);
        connections.broadcast(command.getAuthToken(), command.getGameID(), leaveNotification);
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
        connections.broadcast(null, command.getGameID(), leaveNotification);
    }

    private void handleHighlight(Session session, HighlightCommand command) throws Exception {
        GameData gameData = getValidGame(command);
        ChessGame game = gameData.game();

        Collection<ChessMove> validMoves = game.validMoves(command.getPosition());
        if (!validMoves.isEmpty()) {
            String userName = userService.getAuth(command.getAuthToken()).username();

            String board;
            if (userName.equals(gameData.blackUsername())) {
                board = game.getBoard().buildBoardView(false, validMoves);
            } else {
                board = game.getBoard().buildBoardView(true, validMoves);
            }
            ServerMessage notification = new Notification(board);
            session.getRemote().sendString(notification.toString());
        } else {
            throw new Exception("no valid moves");
        }
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
