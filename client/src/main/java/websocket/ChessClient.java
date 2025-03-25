package websocket;

import model.GameData;
import model.AuthData;
import model.UserData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessClient {
    private final ServerFacade server;
    private static State status;
    private AuthData authData;
    private List<httpmessages.GameResult> games;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
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
                case "join" -> ""; //FIXME join(params);
                case "observe" -> ""; //FIXME observe(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            return e.getMessage();
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
        else {
            return """
                        \tcreate <NAME> - a game
                        \tlist - games
                        \tjoin <ID> [WHITE|BLACK] - a game
                        \tobserve <ID> - a game
                        \tlogout - when you are done
                        \tquit - playing chess
                        \thelp - with possible commands""";
        }
    }

    public String login(String... params) throws Exception {
        if (params.length >= 2) {
            authData = (AuthData) server.login(new UserData(params[0], params[1], null));
            switchStatus(status.equals(State.LOGGED_OUT));
            return "\tLogged in as " + authData.username();
        }
        throw new Exception("\tbad request");
    }

    public String register(String... params) throws Exception {
        if (params.length >= 3) {
            authData = (AuthData) server.register(new UserData(params[0], params[1], params[2]));
            switchStatus(status.equals(State.LOGGED_OUT));
            return "\tRegistered as " + authData.username();
        }
        throw new Exception("\tbad request");
    }

    public String logout() throws Exception {
        server.logout(authData.authToken());
        authData = new AuthData(null, null);
        switchStatus(status.equals(State.LOGGED_IN));
        return "\tLogged out";
    }

    public String createGame(String... params) throws Exception {
        if (params.length >= 1) {
            GameData gameData = new GameData(0, null, null, params[0], null);
            server.createGame(authData.authToken(), gameData);
            return "\tGame created";
        }
        throw new Exception("\tbad request");
    }

    public String listGames() throws Exception {
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

    public State getStatus() {
        return status;
    }

    /**
     * If user is logged out and attempts to log in, log in
     * If user is logged in and attempts to log out, log out
     * Else do nothing
     */
    private void switchStatus(boolean validRequest) {
        if (validRequest) {
            status = (status == State.LOGGED_OUT) ? State.LOGGED_IN : State.LOGGED_OUT;
        }
        //FIXME possibly add message "already logged in" if client is attempting to log in again
    }
}
