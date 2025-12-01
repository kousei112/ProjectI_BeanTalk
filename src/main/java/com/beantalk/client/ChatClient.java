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
 * Chat Client - Version với UI callbacks và Group Chat
 */
public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Gson gson;
    private String username;
    private int userID;

    // Callbacks cho UI
    private BiConsumer<Boolean, String> loginCallback;
    private BiConsumer<Boolean, String> registerCallback;
    private Consumer<MessageData> newMessageCallback;
    private Consumer<String> userJoinedCallback;
    private Consumer<String> userLeftCallback;
    private Consumer<List<String>> onlineUsersCallback;
    private Consumer<GroupData> groupCreatedCallback;
    private Consumer<List<GroupData>> userGroupsCallback;
    private Consumer<List<String>> groupMembersCallback;
    private BiConsumer<Integer, String> groupNameUpdatedCallback;

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
                        this.userID = json.get("userID").getAsInt();
                        loginCallback.accept(true, "Login successful");
                    }
                    // Auto request online users và groups
                    getOnlineUsers();
                    getUserGroups();
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
                        String receiver = json.has("receiver") ? json.get("receiver").getAsString() : null;
                        Integer groupId = json.has("groupId") ? json.get("groupId").getAsInt() : null;
                        newMessageCallback.accept(new MessageData(sender, content, receiver, groupId));
                    }
                    break;

                case "GROUP_CREATED":
                    if (groupCreatedCallback != null) {
                        int groupId = json.get("groupId").getAsInt();
                        String groupName = json.get("groupName").getAsString();
                        groupCreatedCallback.accept(new GroupData(groupId, groupName));
                    }
                    getUserGroups(); // Refresh groups list
                    break;

                case "USER_GROUPS":
                    if (userGroupsCallback != null) {
                        List<GroupData> groups = new ArrayList<>();
                        JsonArray array = json.getAsJsonArray("groups");
                        for (int i = 0; i < array.size(); i++) {
                            JsonObject g = array.get(i).getAsJsonObject();
                            groups.add(new GroupData(
                                    g.get("groupId").getAsInt(),
                                    g.get("groupName").getAsString()
                            ));
                        }
                        userGroupsCallback.accept(groups);
                    }
                    break;

                case "GROUP_MEMBERS":
                    if (groupMembersCallback != null) {
                        List<String> members = new ArrayList<>();
                        JsonArray array = json.getAsJsonArray("members");
                        for (int i = 0; i < array.size(); i++) {
                            members.add(array.get(i).getAsString());
                        }
                        groupMembersCallback.accept(members);
                    }
                    break;

                case "GROUP_NAME_UPDATED":
                    if (groupNameUpdatedCallback != null) {
                        int groupId = json.get("groupId").getAsInt();
                        String newName = json.get("newName").getAsString();
                        groupNameUpdatedCallback.accept(groupId, newName);
                    }
                    getUserGroups(); // Refresh groups list
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
     * Gửi tin nhắn (private hoặc group)
     */
    public void sendMessage(String receiver, String content, Integer groupId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "SEND_MESSAGE");
        if (groupId != null) {
            json.addProperty("groupId", groupId);
        } else {
            json.addProperty("receiver", receiver);
        }
        json.addProperty("content", content);
        writer.println(json.toString());
    }

    /**
     * Tạo group mới
     */
    public void createGroup(String groupName, List<String> members) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "CREATE_GROUP");
        json.addProperty("groupName", groupName);

        JsonArray membersArray = new JsonArray();
        for (String member : members) {
            membersArray.add(member);
        }
        json.add("members", membersArray);

        writer.println(json.toString());
    }

    /**
     * Lấy danh sách groups của user
     */
    public void getUserGroups() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "GET_USER_GROUPS");
        writer.println(json.toString());
    }

    /**
     * Lấy danh sách members của group
     */
    public void getGroupMembers(int groupId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "GET_GROUP_MEMBERS");
        json.addProperty("groupId", groupId);
        writer.println(json.toString());
    }

    /**
     * Đổi tên group
     */
    public void renameGroup(int groupId, String newName) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "RENAME_GROUP");
        json.addProperty("groupId", groupId);
        json.addProperty("newName", newName);
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

    public void setGroupCreatedCallback(Consumer<GroupData> callback) {
        this.groupCreatedCallback = callback;
    }

    public void setUserGroupsCallback(Consumer<List<GroupData>> callback) {
        this.userGroupsCallback = callback;
    }

    public void setGroupMembersCallback(Consumer<List<String>> callback) {
        this.groupMembersCallback = callback;
    }

    public void setGroupNameUpdatedCallback(BiConsumer<Integer, String> callback) {
        this.groupNameUpdatedCallback = callback;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getUserID() {
        return userID;
    }

    /**
     * Data class cho message
     */
    public static class MessageData {
        public final String sender;
        public final String content;
        public final String receiver;
        public final Integer groupId;

        public MessageData(String sender, String content, String receiver, Integer groupId) {
            this.sender = sender;
            this.content = content;
            this.receiver = receiver;
            this.groupId = groupId;
        }
    }

    /**
     * Data class cho group
     */
    public static class GroupData {
        public final int groupId;
        public final String groupName;

        public GroupData(int groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }
    }
}