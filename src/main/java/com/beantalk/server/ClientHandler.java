package com.beantalk.server;

import com.beantalk.model.User;
import com.beantalk.model.Message;
import com.beantalk.util.UserDAO;
import com.beantalk.util.MessageDAO;
import com.beantalk.util.SecurityUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * ClientHandler - Xử lý từng client connection
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private Gson gson;
    private int userID;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Xử lý message từ client
     */
    private void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "LOGIN":
                    handleLogin(json);
                    break;

                case "REGISTER":
                    handleRegister(json);
                    break;

                case "SEND_MESSAGE":
                    handleSendMessage(json);
                    break;

                case "GET_ONLINE_USERS":
                    handleGetOnlineUsers();
                    break;

                case "DISCONNECT":
                    disconnect();
                    break;

                default:
                    sendError("Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendError("Invalid message format: " + e.getMessage());
        }
    }

    /**
     * Xu li dang nhap - kiem tra database
     */
    private void handleLogin(JsonObject json) {
        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();

        System.out.println("🔐 Login attempt: " + username);

        // verify voi database
        User user = UserDAO.verifyLogin(username, password);

        if (user != null) {
            // login thanh cong
            this.username = username;
            this.userID = user.getUserID();

            JsonObject response = new JsonObject();
            response.addProperty("type", "LOGIN_SUCCESS");
            response.addProperty("username", username);
            response.addProperty("userID", userID);
            sendMessage(response.toString());

            // thong bao cho cac clients khac
            JsonObject notification = new JsonObject();
            notification.addProperty("type", "USER_JOINED");
            notification.addProperty("username", username);
            ChatServer.broadcast(notification.toString(), this);

            System.out.println("✅ " + username + " logged in (ID: " + userID + ")");
        } else {
            // login that bai
            JsonObject response = new JsonObject();
            response.addProperty("type", "LOGIN_FAILED");
            response.addProperty("message", "Invalid username or password");
            sendMessage(response.toString());
            System.out.println("❌ Login failed: " + username);
        }
    }

    /**
     * Xu li dang ki - luu vao database
     */
    private void handleRegister(JsonObject json) {
        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();
        String email = json.get("email").getAsString();

        System.out.println("📝 Register attempt: " + username);

        // kiem tra username da ton tai hay chua
        if (UserDAO.usernameExists(username)) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "REGISTER_FAILED");
            response.addProperty("message", "Username already exists");
            sendMessage(response.toString());
            System.out.println("❌ Register failed: Username exists - " + username);
            return;
        }
        // hash password va luu vao database
        String hashedPassword = SecurityUtil.hashPassword(password);
        boolean success = UserDAO.registerUser(username, hashedPassword, email);

        if (success) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "REGISTER_SUCCESS");
            response.addProperty("message", "Registration successful! Please login.");
            sendMessage(response.toString());
            System.out.println("✅ New user registered: " + username);
        } else {
            JsonObject response = new JsonObject();
            response.addProperty("type", "REGISTER_FAILED");
            response.addProperty("message", "Registration failed! Please try again.");
            sendMessage(response.toString());
            System.out.println("❌ Register failed: Database error");
        }
    }

    /**
     * Xu li gui tin nhan - luu vao database (encrypted)
     */
    private void handleSendMessage(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String receiver = json.get("receiver").getAsString();
        String content = json.get("content").getAsString();

        System.out.println("💬 Message: " + username + " -> " + receiver);

        // Encrypt message
        String encryptedContent = SecurityUtil.encryptMessage(content);

        // Xác định receiverId
        Integer receiverId = null;

        if (!receiver.equals("ALL")) {
            // Private message - phải tìm receiver
            User receiverUser = UserDAO.getUserByUsername(receiver);

            if (receiverUser == null) {
                // User không tồn tại
                sendError("User not found: " + receiver);
                System.out.println("❌ User not found: " + receiver);
                return; // RETURN NGAY - không lưu vào DB
            }

            receiverId = receiverUser.getUserID();
        }

        // Lưu vào database
        try {
            boolean saved = MessageDAO.saveMessage(
                    this.userID,
                    receiverId,  // null nếu broadcast, có giá trị nếu private
                    null,        // group_id
                    encryptedContent,
                    "TEXT",
                    null         // file_path
            );

            if (saved) {
                System.out.println("💾 Message saved (encrypted)");
            } else {
                System.err.println("❌ Failed to save message");
            }
        } catch (Exception e) {
            System.err.println("❌ Error saving message: " + e.getMessage());
            e.printStackTrace();
        }

        // Gửi message đến receiver
        JsonObject message = new JsonObject();
        message.addProperty("type", "NEW_MESSAGE");
        message.addProperty("sender", this.username);
        message.addProperty("receiver", receiver);
        message.addProperty("content", content);

        if (receiver.equals("ALL")) {
            // Broadcast
            ChatServer.broadcast(message.toString(), this);
            System.out.println("📤 Broadcast: " + username + " -> ALL");
        } else {
            // Private message
            boolean sent = ChatServer.sendToUser(receiver, message.toString());
            if (sent) {
                // Confirm gửi lại cho sender
                sendMessage(message.toString());
                System.out.println("📤 Private: " + username + " -> " + receiver);
            } else {
                sendError("User " + receiver + " is not online");
                System.out.println("❌ User offline: " + receiver);
            }
        }
    }

    /**
     * Load lich su doan chat tu database
     */
    private void handleLoadHistory(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String otherUsername = json.get("username").getAsString();
        User otherUser = UserDAO.getUserByUsername(otherUsername);

        if (otherUser == null) {
            sendError("User not found: " + otherUsername);
            return;
        }

        System.out.println("📜 Loading history: " + username + " <-> " + otherUsername);

        // lay 50 tin nhan gan nhat
        List<Message> messages = MessageDAO.getChatHistory(this.userID, otherUser.getUserID(), 50);


        JsonObject response = new JsonObject();
        response.addProperty("type", "CHAT_HISTORY");
        response.addProperty("count", messages.size());

        // decrypt messages
        StringBuilder history = new StringBuilder();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            String decrypted = SecurityUtil.decryptMessage(msg.getContentEncrypted());
            String senderName = (msg.getSenderID() == this.userID) ? "You" : otherUsername;

            history.append("[").append(senderName).append("]: ").append(decrypted).append("\n");
        }

        response.addProperty("history", history.toString());
        sendMessage(response.toString());
        System.out.println("✅ Loaded " + messages.size() + " messages");
    }

    /**
     * Lay danh sach user online
     */
    private void handleGetOnlineUsers() {
        JsonObject response = new JsonObject();
        response.addProperty("type", "ONLINE_USERS");
        response.add("users", gson.toJsonTree(ChatServer.getOnlineUsers()));
        sendMessage(response.toString());
        System.out.println("👥 Sent online users list");
    }

    /**
     * Gửi message đến client
     */
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    /**
     * Gửi error message
     */
    private void sendError(String error) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "ERROR");
        response.addProperty("message", error);
        sendMessage(response.toString());
    }

    /**
     * Disconnect client
     */
    public void disconnect() {
        try {
            ChatServer.removeClient(this);
            if (username != null) {
                JsonObject notification = new JsonObject();
                notification.addProperty("type", "USER_LEFT");
                notification.addProperty("username", username);
                ChatServer.broadcast(notification.toString(), this);

                // Update last_seen
                if (userID != 0) {
                    UserDAO.updateLastSeen(userID);
                }
                System.out.println("👋 " + username + " disconnected");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Getters
     */
    public String getUsername() {
        return username;
    }
}