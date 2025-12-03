package com.beantalk.util;


import com.beantalk.model.User;

import java.sql.*;
import java.time.LocalDateTime;

// Data Access Object for Users table
public class UserDAO {
    // Dang ki user moi
    public static boolean registerUser(String username, String passwordHash, String email) {
        String sql = "INSERT INTO Users (username, password_hash, email, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, email);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Kiem tra username da ton tai hay chua
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    // Lay user bang username
    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("last_seen") != null ?
                                rs.getTimestamp("last_seen").toLocalDateTime() : null
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    // update last seen
    public static void updateLastSeen(int userID) {
        String sql = "UPDATE Users SET last_seen = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last_seen: " + e.getMessage());
        }
    }

    // verify login
    public static User verifyLogin(String username, String plainPassword) {
        User user = getUserByUsername(username);

        if (user != null) {
            // kiem tra password
            if (SecurityUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
                updateLastSeen(user.getUserID());
                return user;
            }
        }
        return null;
    }

    // Lay user bang ID
    public static User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("last_seen") != null ?
                                rs.getTimestamp("last_seen").toLocalDateTime() : null
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Test DAO
     */
    public static void main(String[] args) {
        System.out.println("=== USER DAO TEST ===\n");

        // Test 1: Register user 1
        System.out.println("1. Testing register user 1...");
        String testUsername1 = "alice_test";
        String testPassword1 = "password123";
        String testEmail1 = "alice@beantalk.com";

        if (!usernameExists(testUsername1)) {
            String hashedPassword = SecurityUtil.hashPassword(testPassword1);
            boolean success = registerUser(testUsername1, hashedPassword, testEmail1);
            System.out.println("Register result: " + (success ? "✅ SUCCESS" : "❌ FAILED"));
        } else {
            System.out.println("User alice_test already exists");
        }

        // Test 2: Register user 2
        System.out.println("\n2. Testing register user 2...");
        String testUsername2 = "bob_test";
        String testPassword2 = "password456";
        String testEmail2 = "bob@beantalk.com";

        if (!usernameExists(testUsername2)) {
            String hashedPassword = SecurityUtil.hashPassword(testPassword2);
            boolean success = registerUser(testUsername2, hashedPassword, testEmail2);
            System.out.println("Register result: " + (success ? "✅ SUCCESS" : "❌ FAILED"));
        } else {
            System.out.println("User bob_test already exists");
        }

        // Test 3: Login user 1
        System.out.println("\n3. Testing login alice...");
        User user = verifyLogin(testUsername1, testPassword1);
        if (user != null) {
            System.out.println("✅ Login successful!");
            System.out.println("User: " + user.getUsername());
            System.out.println("User ID: " + user.getUserID());
        }

        System.out.println("\n=== TEST COMPLETED ===");
    }
}
