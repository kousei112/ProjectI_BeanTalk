package com.beantalk.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Database Connection Manager
public class DatabaseManager {
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    // Load configuration from file
    static {
        loadConfig();
    }
    private static void loadConfig(){
        try (InputStream input = DatabaseManager.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("ERROR: Cannot find config.properties file");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            DB_URL = prop.getProperty("db.url");
            DB_USER = prop.getProperty("db.username", "");
            DB_PASSWORD = prop.getProperty("db.password", "");

        } catch (IOException e) {
            System.err.println("ERROR: Cannot read config file - " + e.getMessage());
        }
    }

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        // If using Windows Authentication (empty username)
        if (DB_USER == null || DB_USER.isEmpty()) {
            return DriverManager.getConnection(DB_URL);
        }
        // If using SQL Authentication
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Test database connection
     */
    public static void main(String[] args) {
        System.out.println("Testing database connection...\n");

        try (Connection conn = getConnection()) {
            System.out.println("SUCCESS: Connected to database!");
            System.out.println("================================");
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("Server: " + conn.getMetaData().getURL());
            System.out.println("User: " + conn.getMetaData().getUserName());
            System.out.println("================================");

        } catch (SQLException e) {
            System.err.println("ERROR: Cannot connect to database!");
            System.err.println("================================");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("================================");

            // Suggestions
            if (e.getMessage().contains("Login failed")) {
                System.err.println("\nSuggestion: Wrong username or password");
            } else if (e.getMessage().contains("TCP/IP")) {
                System.err.println("\nSuggestion: SQL Server is not running");
            } else if (e.getMessage().contains("integrated authentication")) {
                System.err.println("\nSuggestion: Missing Windows Authentication library");
                System.err.println("   -> Should use SQL Authentication instead");
            }
        }
    }
}