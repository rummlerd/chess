package request;

public class RegisterResult {
    private final String username;
    private final String authToken;
    private final String message;

    // Constructor for Success
    public RegisterResult(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
        this.message = null; // No error message in success case
    }

    // Constructor for Failure
    public RegisterResult(String message) {
        this.username = null; // No username in failure case
        this.authToken = null; // No authToken in failure case
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

