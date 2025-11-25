package com.beantalk.util;

import com.beantalk.model.Message;
import com.beantalk.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for Messages table
public class MessageDAO {
    // Luu message moi
    public static boolean saveMessage(int senderID, Integer receiverID, Integer groupID,
                                      String contentEncrypted, String messagetype, String filePath) {
        String sql = "INSERT INTO Messages (sender_id, receiver_id, group_id, content_encrypted, " +
                     "message_type, file_path, sent_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderID);
            if (receiverID != null) {
                stmt.setInt(2, receiverID);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (groupID != null) {
                stmt.setInt(3, groupID);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, contentEncrypted);
            stmt.setString(5, messagetype);
            stmt.setString(6, filePath);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
            return false;
        }
    }

    // lay lich su chat giua 2 users
    public static List<Message> getChatHistory(int user1ID, int user2ID, int limit) {
        String sql = "SELECT TOP (?) * FROM Messages " +
                     "WHERE (sender_id = ? AND receiver_id = ? ) OR (sender_id = ? AND receiver_id = ?) " +
                     "ORDER BY sent_at DESC";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, user1ID);
            stmt.setInt(3, user2ID);
            stmt.setInt(4, user2ID);
            stmt.setInt(5, user1ID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("message_id"),
                        rs.getInt("sender_id"),
                        (Integer) rs.getObject("receiver_id"),
                        (Integer) rs.getObject("group_id"),
                        rs.getString("content_encrypted"),
                        rs.getString("message_type"),
                        rs.getString("file_path"),
                        rs.getTimestamp("sent_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting chat history: " + e.getMessage());
        }
        return messages;
    }

    // lay messages cua group
    public static List<Message> getGroupMessages(int groupID, int limit) {
        String sql = "SELECT TOP (?) * FROM Messages WHERE group_id = ? ORDER BY sent_at DESC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, limit);
            stmt.setInt(2, groupID);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                messages.add(new Message(
                        rs.getInt("message_id"),
                        rs.getInt("sender_id"),
                        (Integer) rs.getObject("receiver_id"),
                        (Integer) rs.getObject("group_id"),
                        rs.getString("content_encrypted"),
                        rs.getString("message_type"),
                        rs.getString("file_path"),
                        rs.getTimestamp("sent_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting group messages: " + e.getMessage());
        }
        return messages;
    }

    // test DAO
    public static void main(String[] args) {
        System.out.println("=== MESSAGE DAO TEST ===\n");

        // Giả sử user_id 1 và 2 đã tồn tại
        int senderId = 1;
        int receiverId = 2;

        // Test 1: Lưu message
        System.out.println("1. Testing save message...");
        String message = "Hello, this is a test message!";
        String encrypted = SecurityUtil.encryptMessage(message);

        boolean success = saveMessage(senderId, receiverId, null, encrypted, "TEXT", null);
        System.out.println("Save result: " + (success ? "✅ SUCCESS" : "❌ FAILED"));

        // Test 2: Lấy lịch sử chat
        System.out.println("\n2. Testing get chat history...");
        List<Message> history = getChatHistory(senderId, receiverId, 10);
        System.out.println("Found " + history.size() + " messages");

        for (Message msg : history) {
            String decrypted = SecurityUtil.decryptMessage(msg.getContentEncrypted());
            System.out.println("  - Message: " + decrypted);
        }

        System.out.println("\n=== TEST COMPLETED ===");
    }
}

