package websocket;

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
        while (!result.equals("\tquit")) {
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
        if (message.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)
                && message instanceof Notification notification) {
            System.out.println("\n"+ notification.message);
        } else if (message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)
                && message instanceof LoadGameMessage loadGame) {
            if (loadGame.userName.equals(loadGame.game.blackUsername())) {
                System.out.println("\n"+ loadGame.game.game().getBoard().toStringFromBlack());
            } else {
                System.out.println("\n"+ loadGame.game.game().getBoard().toStringFromWhite());
            }
        }
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n[" + client.getStatus() + "] >>> ");
    }
}
