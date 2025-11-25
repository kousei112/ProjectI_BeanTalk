package com.beantalk.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Chat Client - Version với UI callbacks
 */
public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Gson gson;
    private String username;

    // Callbacks cho UI
    private BiConsumer<Boolean, String> loginCallback;
    private BiConsumer<Boolean, String> registerCallback;
    private Consumer<MessageData> newMessageCallback;
    private Consumer<String> userJoinedCallback;
    private Consumer<String> userLeftCallback;
    private Consumer<List<String>> onlineUsersCallback;

    public ChatClient() {
        this.gson = new Gson();
    }

    /**
     * Kết nối tới server
     */
    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        // Thread để nhận messages từ server
        new Thread(this::receiveMessages).start();
    }

    /**
     * Nhận messages từ server
     */
    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        }
    }

    /**
     * Xử lý message từ server
     */
    private void handleServerMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "LOGIN_SUCCESS":
                    if (loginCallback != null) {
                        loginCallback.accept(true, "Login successful");
                    }
                    // Auto request online users
                    getOnlineUsers();
                    break;

                case "LOGIN_FAILED":
                    if (loginCallback != null) {
                        loginCallback.accept(false, json.get("message").getAsString());
                    }
                    break;

                case "REGISTER_SUCCESS":
                    if (registerCallback != null) {
                        registerCallback.accept(true, json.get("message").getAsString());
                    }
                    break;

                case "REGISTER_FAILED":
                    if (registerCallback != null) {
                        registerCallback.accept(false, json.get("message").getAsString());
                    }
                    break;

                case "NEW_MESSAGE":
                    if (newMessageCallback != null) {
                        String sender = json.get("sender").getAsString();
                        String content = json.get("content").getAsString();
                        String receiver = json.has("receiver") ? json.get("receiver").getAsString() : "ALL";
                        newMessageCallback.accept(new MessageData(sender, content, receiver));
                    }
                    break;

                case "USER_JOINED":
                    if (userJoinedCallback != null) {
                        userJoinedCallback.accept(json.get("username").getAsString());
                    }
                    getOnlineUsers();
                    break;

                case "USER_LEFT":
                    if (userLeftCallback != null) {
                        userLeftCallback.accept(json.get("username").getAsString());
                    }
                    getOnlineUsers();
                    break;

                case "ONLINE_USERS":
                    if (onlineUsersCallback != null) {
                        List<String> users = new ArrayList<>();
                        JsonArray array = json.getAsJsonArray("users");
                        for (int i = 0; i < array.size(); i++) {
                            users.add(array.get(i).getAsString());
                        }
                        onlineUsersCallback.accept(users);
                    }
                    break;

                case "ERROR":
                    System.err.println("Error: " + json.get("message").getAsString());
                    break;

                default:
                    System.out.println("Unknown message: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
    }

    /**
     * Đăng nhập
     */
    public void login(String username, String password) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "LOGIN");
        json.addProperty("username", username);
        json.addProperty("password", password);
        writer.println(json.toString());
    }

    /**
     * Đăng ký
     */
    public void register(String username, String password, String email) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "REGISTER");
        json.addProperty("username", username);
        json.addProperty("password", password);
        json.addProperty("email", email);
        writer.println(json.toString());
    }

    /**
     * Gửi tin nhắn
     */
    public void sendMessage(String receiver, String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "SEND_MESSAGE");
        json.addProperty("receiver", receiver);
        json.addProperty("content", content);
        writer.println(json.toString());
    }

    /**
     * Lấy danh sách users online
     */
    public void getOnlineUsers() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "GET_ONLINE_USERS");
        writer.println(json.toString());
    }

    /**
     * Disconnect
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    // ========== SETTERS CHO CALLBACKS ==========

    public void setLoginCallback(BiConsumer<Boolean, String> callback) {
        this.loginCallback = callback;
    }

    public void setRegisterCallback(BiConsumer<Boolean, String> callback) {
        this.registerCallback = callback;
    }

    public void setNewMessageCallback(Consumer<MessageData> callback) {
        this.newMessageCallback = callback;
    }

    public void setUserJoinedCallback(Consumer<String> callback) {
        this.userJoinedCallback = callback;
    }

    public void setUserLeftCallback(Consumer<String> callback) {
        this.userLeftCallback = callback;
    }

    public void setOnlineUsersCallback(Consumer<List<String>> callback) {
        this.onlineUsersCallback = callback;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Data class cho message
     */
    public static class MessageData {
        public final String sender;
        public final String content;
        public final String receiver;

        public MessageData(String sender, String content, String receiver) {
            this.sender = sender;
            this.content = content;
            this.receiver = receiver;
        }
    }
}