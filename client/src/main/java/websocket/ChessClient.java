package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.List;

public class ChessClient {
    private final ServerFacade server;
    private static State status;
    private AuthData authData;
    private List<httpmessages.GameResult> games;
    private boolean resigning = false;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl, notificationHandler);
        status = State.LOGGED_OUT;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> playGame(params);
                case "observe" -> observe(params);
                case "logout" -> logout();
                case "quit" -> "\tquit";
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resignCheck();
                case "redraw" -> redraw();
                case "highlight" -> highlight(params);
                case "yes" -> yes();
                case "no" -> no();
                default -> help();
            };
        } catch (Exception e) {
            return "\t" + e.getMessage();
        }
    }

    public String resignCheck() {
        resigning = true;
        return "\tare you sure you want to resign? [yes|no]";
    }

    public String yes() throws Exception {
        if (resigning) {
            resigning = false;
            return resign();
        } else {
            return help();
        }
    }

    public String no() {
        if (resigning) {
            resigning = false;
            return "\tdid not resign";
        } else {
            return help();
        }
    }

    public String help() {
        if (status == State.LOGGED_OUT) {
            return """
                        \tregister <USERNAME> <PASSWORD> <EMAIL> - to create an account
                        \tlogin <USERNAME> <PASSWORD> - to play chess
                        \tquit - playing chess
                        \thelp - with possible commands""";
        }
        else if (status == State.LOGGED_IN) {
            return """
                        \tcreate <NAME> - a game
                        \tlist - games
                        \tjoin <ID> [WHITE|BLACK] - a game
                        \tobserve <ID> - a game
                        \tlogout - when you are done
                        \tquit - playing chess
                        \thelp - with possible commands""";
        } else {
            return """
                    \thelp - with possible commands
                    \tredraw - the current state of the board
                    \tleave - the game
                    \tmove <piece position> to <end position> and <promotion piece if applicable>
                    \tresign - from the game
                    \thighlight <piece> - all legal moves for this piece""";
        }
    }

    public String login(String... params) throws Exception {
        isLoggedOut();
        if (params.length >= 2) {
            authData = (AuthData) server.login(new UserData(params[0], params[1], null));
            switchStatus(status.equals(State.LOGGED_OUT));
            return "\tLogged in as " + authData.username();
        }
        return "\tbad request";
    }

    public String register(String... params) throws Exception {
        isLoggedOut();
        if (params.length >= 3) {
            if (params[0].equals("<available>")) {
                return "\tplease use different username";
            }
            authData = (AuthData) server.register(new UserData(params[0], params[1], params[2]));
            switchStatus(status.equals(State.LOGGED_OUT));
            return "\tRegistered as " + authData.username();
        }
        return "\tbad request";
    }

    public String logout() throws Exception {
        isLoggedIn();
        server.logout(authData.authToken());
        authData = new AuthData(null, null);
        switchStatus(status.equals(State.LOGGED_IN));
        return "\tLogged out";
    }

    public String createGame(String... params) throws Exception {
        isLoggedIn();
        if (params.length >= 1) {
            GameData gameData = new GameData(0, null, null, params[0], null);
            server.createGame(authData.authToken(), gameData);
            return "\tGame created";
        }
        return "\tbad request";
    }

    public String listGames() throws Exception {
        isLoggedIn();
        games = server.listGames(authData.authToken());
        if (games.isEmpty()) {
            return "\tNo games available.\n\tcreate <NAME> - a game";
        }
        StringBuilder result = new StringBuilder("\tGames:\n");
        int i = 0;
        for (httpmessages.GameResult game : games) {
            if (i != 0) {
                result.append("\n");
            }
            i++;
            result.append("\t").append(i).append(" - ").append(game.gameName()).append(": ");
            if (game.whiteUsername() != null) {
                result.append(game.whiteUsername());
            } else {
                result.append("<AVAILABLE>");
            }
            result.append(" vs ");
            if (game.blackUsername() != null) {
                result.append(game.blackUsername());
            } else {
                result.append("<AVAILABLE>");
            }
        }
        return result.toString();
    }

    public String playGame(String... params) throws Exception {
        isLoggedIn();
        if (games == null) {
            return "\tplease list games before attempting to join";
        }
        ChessGame.TeamColor color;
        if (params.length >= 2) {
            if (!params[0].matches("\\d+")) {
                return "\tinvalid game number";
            }
            int number = Integer.parseInt(params[0]) - 1;
            int gameID = getGameID(number);
            String playerColor;
            if (params[1].equals("white")) {
                if (games.get(number).whiteUsername() != null) {
                    return "\tcolor already taken";
                }
                playerColor = "WHITE";
                color = ChessGame.TeamColor.WHITE;
            }
            else if (params[1].equals("black")) {
                if (games.get(number).blackUsername() != null) {
                    return "\tcolor already taken";
                }
                playerColor = "BLACK";
                color = ChessGame.TeamColor.BLACK;
            }
            else {
                return "\tinvalid color";
            }
            server.playGame(gameID, playerColor, authData.authToken());
            server.setTeamColor(color);

            // Transition to GAMEPLAY UI
            enterGamePlay();
            return server.drawGame(gameID, authData.authToken(), playerColor.equals("WHITE"));
        }
        return "\tbad request";
    }

    public String observe(String... params) throws Exception {
        isLoggedIn();
        if (games == null) {
            return "\tplease list games before attempting to observe";
        }
        if (params.length >= 1) {
            if (!params[0].matches("\\d+")) {
                return "\tinvalid game number";
            }
            int number = Integer.parseInt(params[0]) - 1;
            int gameID = getGameID(number);

            // Transition to GAMEPLAY UI
            enterGamePlay();
            return server.drawGame(gameID, authData.authToken(), true);
        }
        return "\tbad request";
    }

    public String redraw() throws Exception {
        if (status != State.GAMEPLAY) {
            return help();
        }
        return server.redraw(authData);
    }

    public String highlight(String... params) throws Exception {
        if (status != State.GAMEPLAY) {
            return help();
        } else if (params.length >= 1){
            if (!params[0].matches("^[A-Ha-h][1-8]$")) {
                return "\tinvalid chess position";
            }
            ChessPosition position = stringToPosition(params[0]);
            server.highlight(authData.authToken(), position);
            return "\trequest sent — waiting for server validation";
        }
        return "\tbad request";
    }

    public String leave() throws Exception {
        if (status != State.GAMEPLAY) {
            return help();
        }
        server.leave(authData.authToken());
        exitGamePlay();
        return "\tleft";
    }

    public String resign() throws Exception {
        if (status != State.GAMEPLAY) {
            return help();
        }
        server.resign(authData.authToken());
        return "\tresigned";
    }

    public String move(String... params) throws Exception {
        if (status != State.GAMEPLAY) {
            return help();
        } else if (params.length >= 2){
            if (!params[0].matches("^[A-Ha-h][1-8]$")) {
                return "\tinvalid starting chess position";
            } else if (!params[1].matches("^[A-Ha-h][1-8]$")) {
                return "\tinvalid ending chess position";
            }
            ChessPosition startPosition = stringToPosition(params[0]);
            ChessPosition endPosition = stringToPosition(params[1]);
            ChessPiece.PieceType promotionPiece;
            if (params.length >= 3) {
                promotionPiece = getPromotionPiece(params[2]);
            } else {
                promotionPiece = null;
            }
            ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);
            server.move(authData.authToken(), move);
            return "\tmove sent — waiting for server validation";
        }
        return "\tbad request";
    }

    private ChessPiece.PieceType getPromotionPiece(String input) throws Exception {
        String pieceStr = input.trim().toLowerCase();

        ChessPiece.PieceType type;
        if (pieceStr.matches("^(n|knight)$")) {
            type = ChessPiece.PieceType.KNIGHT;
        } else if (pieceStr.matches("^(r|rook)$")) {
            type = ChessPiece.PieceType.ROOK;
        } else if (pieceStr.matches("^(b|bishop)$")) {
            type = ChessPiece.PieceType.BISHOP;
        } else if (pieceStr.matches("^(q|queen)$")) {
            type = ChessPiece.PieceType.QUEEN;
        } else {
            throw new Exception("Invalid promotion piece type");
        }

        return type;
    }

    private ChessPosition stringToPosition(String position) {
        if (position == null || !position.matches("^[a-hA-H][1-8]$")) {
            throw new IllegalArgumentException("Invalid chess position: " + position);
        }

        // Convert the letter to a column number (a/A = 1, b/B = 2, ..., h/H = 8)
        char colChar = Character.toLowerCase(position.charAt(0));
        int col = colChar - 'a' + 1;

        // Convert the digit to an integer (1-8)
        int row = Character.getNumericValue(position.charAt(1));

        return new ChessPosition(row, col);
    }

    public State getStatus() {
        return status;
    }

    private void isLoggedIn() throws Exception {
        if (status == State.LOGGED_OUT) {
            throw new Exception("\tplease log in");
        }
    }

    private void isLoggedOut() throws Exception {
        if (status == State.LOGGED_IN) {
            throw new Exception("\talready log in");
        }
    }

    private int getGameID(int number) throws Exception {
        if (number >= 0 && number < games.size()) {
            return games.get(number).gameID();
        }
        else {
            throw new Exception("\tinvalid game number");
        }
    }

    /**
     * If user is logged out and attempts to log in, log in
     * If user is logged in and attempts to log out, log out
     * Else do nothing
     */
    private void switchStatus(boolean validRequest) throws Exception {
        if (validRequest) {
            status = (status == State.LOGGED_OUT) ? State.LOGGED_IN : State.LOGGED_OUT;
        }
        else if (status.equals(State.LOGGED_IN)) {
            throw new Exception("\talready logged in");
        }
    }

    private void enterGamePlay() {
        status = State.GAMEPLAY;
    }

    private void exitGamePlay() {
        status = State.LOGGED_IN;
    }
}
