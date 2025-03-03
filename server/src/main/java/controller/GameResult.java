package controller;

public class GameResult {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;

    // Constructor for listGames handler
    public GameResult(int gameID, String whiteUsername, String blackUsername, String gameName) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
    }
}
