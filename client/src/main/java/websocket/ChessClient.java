package websocket;

import model.GameData;
import model.AuthData;
import model.UserData;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private static State status;
    private AuthData authData;

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
                case "list" -> ""; //FIXME list();
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
                        register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                        login <USERNAME> <PASSWORD> - to play chess
                        quit - playing chess
                        help - with possible commands
                    """;
        }
        else {
            return """
                        create <NAME> - a game
                        list - games
                        join <ID> [WHITE|BLACK] - a game
                        observe <ID> - a game
                        logout - when you are done
                        quit - playing chess
                        help - with possible commands
                   """;
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
