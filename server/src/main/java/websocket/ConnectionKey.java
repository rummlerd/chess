package websocket;

import java.util.Objects;

public class ConnectionKey {
    private final String authToken;
    private final Integer gameID;

    public ConnectionKey(String authToken, Integer gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionKey that = (ConnectionKey) o;
        return authToken.equals(that.authToken) && gameID.equals(that.gameID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken, gameID);
    }
}

