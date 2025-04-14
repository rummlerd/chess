package websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!"\tquit".equals(result)) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print("\t" + msg);
            }
        }
    }

    public void notify(ServerMessage message) {
        switch (message) {
            case Notification notification when message.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION) ->
                    System.out.println("\n" + notification.message);
            case LoadGameMessage loadGame when message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME) -> {
                if (loadGame.userName.equals(loadGame.game.blackUsername())) {
                    System.out.println("\n" + loadGame.game.game().getBoard().toStringFromBlack());
                } else {
                    System.out.println("\n" + loadGame.game.game().getBoard().toStringFromWhite());
                }
            }
            case ErrorMessage errorMessage when message.getServerMessageType().equals(ServerMessage.ServerMessageType.ERROR) ->
                    System.out.println("\n\t" + errorMessage.errorMessage);
            default -> {
            }
        }
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n[" + client.getStatus() + "] >>> ");
    }
}
