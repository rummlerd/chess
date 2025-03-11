package dataaccess;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String user;
    private static String password;
    private static String connectionUrl;
    private static Properties dbProperties;
    private static final String DB_PROPERTIES_PATH = "src/main/resources/db.properties";

    /*
     * Load the database information for the db.properties file.
     */
    static void loadProperties() {
        if (dbProperties == null) {
            try {
                try (var propStream = new FileInputStream(DB_PROPERTIES_PATH)) {
                    // propStream was originally loading from compile resources: Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")
                    // if (propStream == null) {
                    //    throw new Exception("Unable to load db.properties");
                    //}
                    dbProperties = new Properties();
                    dbProperties.load(propStream);
                    databaseName = dbProperties.getProperty("db.name");
                    user = dbProperties.getProperty("db.user");
                    password = dbProperties.getProperty("db.password");

                    var host = dbProperties.getProperty("db.host");
                    var port = Integer.parseInt(dbProperties.getProperty("db.port"));
                    connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
                }
            } catch (Exception ex) {
                throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
            }
        }
    }

    public static void reloadProperties() {
        dbProperties = null; // Force reload on next call
        loadProperties();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        loadProperties();
        try {
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
