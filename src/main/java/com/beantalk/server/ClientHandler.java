package com.beantalk.server;

import com.beantalk.model.User;
import com.beantalk.model.Message;
import com.beantalk.model.Group;
import com.beantalk.util.UserDAO;
import com.beantalk.util.MessageDAO;
import com.beantalk.util.GroupDAO;
import com.beantalk.util.SecurityUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * ClientHandler - Xử lý từng client connection với Group Chat support
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

                case "SEND_FILE":
                    handleSendFile(json);
                    break;

                case "CREATE_GROUP":
                    handleCreateGroup(json);
                    break;

                case "GET_USER_GROUPS":
                    handleGetUserGroups();
                    break;

                case "GET_GROUP_MEMBERS":
                    handleGetGroupMembers(json);
                    break;

                case "RENAME_GROUP":
                    handleRenameGroup(json);
                    break;

                case "GET_CHAT_HISTORY":
                    handleGetChatHistory(json);
                    break;

                case "GET_GROUP_HISTORY":
                    handleGetGroupHistory(json);
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
            e.printStackTrace();
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
     * Xu li gui tin nhan - private hoặc group
     */
    private void handleSendMessage(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String content = json.get("content").getAsString();
        Integer groupId = json.has("groupId") ? json.get("groupId").getAsInt() : null;
        String receiver = json.has("receiver") ? json.get("receiver").getAsString() : null;

        // Encrypt message
        String encryptedContent = SecurityUtil.encryptMessage(content);

        if (groupId != null) {
            // GROUP MESSAGE
            handleGroupMessage(groupId, content, encryptedContent);
        } else if (receiver != null) {
            // PRIVATE MESSAGE
            handlePrivateMessage(receiver, content, encryptedContent);
        } else {
            sendError("Must specify either receiver or groupId");
        }
    }

    /**
     * Xử lý group message
     */
    private void handleGroupMessage(int groupId, String content, String encryptedContent) {
        System.out.println("💬 Group message: " + username + " -> Group#" + groupId);

        // Kiểm tra user có phải member không
        if (!GroupDAO.isMember(groupId, this.userID)) {
            sendError("You are not a member of this group");
            return;
        }

        // Lưu vào database
        boolean saved = MessageDAO.saveMessage(
                this.userID,
                null,  // receiver_id = null for group
                groupId,
                encryptedContent,
                "TEXT",
                null
        );

        if (saved) {
            System.out.println("💾 Group message saved");
        }

        // Tạo message object
        JsonObject message = new JsonObject();
        message.addProperty("type", "NEW_MESSAGE");
        message.addProperty("sender", this.username);
        message.addProperty("content", content);
        message.addProperty("groupId", groupId);

        // GỬI CHO CHÍNH SENDER (quan trọng!)
        sendMessage(message.toString());

        // Gửi đến tất cả members khác của group
        ChatServer.broadcastToGroup(groupId, message.toString(), this);
        System.out.println("📤 Group broadcast: " + username + " -> Group#" + groupId);
    }

    /**
     * Xử lý private message
     */
    private void handlePrivateMessage(String receiver, String content, String encryptedContent) {
        System.out.println("💬 Private message: " + username + " -> " + receiver);

        // Tìm receiver
        User receiverUser = UserDAO.getUserByUsername(receiver);

        if (receiverUser == null) {
            sendError("User not found: " + receiver);
            return;
        }

        // Lưu vào database
        boolean saved = MessageDAO.saveMessage(
                this.userID,
                receiverUser.getUserID(),
                null,  // group_id = null for private
                encryptedContent,
                "TEXT",
                null
        );

        if (saved) {
            System.out.println("💾 Private message saved");
        }

        // Tạo message
        JsonObject message = new JsonObject();
        message.addProperty("type", "NEW_MESSAGE");
        message.addProperty("sender", this.username);
        message.addProperty("receiver", receiver);
        message.addProperty("content", content);

        // GỬI LẠI CHO SENDER (để hiển thị tin nhắn của chính mình)
        sendMessage(message.toString());
        System.out.println("📤 Sent back to sender: " + username);

        // Gửi cho receiver
        boolean sent = ChatServer.sendToUser(receiver, message.toString());
        if (sent) {
            System.out.println("📤 Private: " + username + " -> " + receiver);
        } else {
            System.out.println("❌ User offline: " + receiver);
            // Vẫn đã lưu vào DB, khi user online sẽ load history
        }
    }

    /**
     * Xử lý gửi file
     */
    private void handleSendFile(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String fileName = json.get("fileName").getAsString();
        String fileBase64 = json.get("fileData").getAsString();
        String messageType = json.get("messageType").getAsString();
        Integer groupId = json.has("groupId") ? json.get("groupId").getAsInt() : null;
        String receiver = json.has("receiver") ? json.get("receiver").getAsString() : null;

        System.out.println("📎 Receiving file: " + fileName + " (" + messageType + ")");

        try {
            // Lưu file vào server
            String filePath = com.beantalk.util.FileTransferUtil.base64ToFile(fileBase64, fileName);
            System.out.println("💾 File saved: " + filePath);

            // Encrypt file path (hoặc có thể không encrypt)
            String encryptedPath = SecurityUtil.encryptMessage(filePath);

            if (groupId != null) {
                // GROUP FILE
                handleGroupFile(groupId, fileName, filePath, encryptedPath, messageType);
            } else if (receiver != null) {
                // PRIVATE FILE
                handlePrivateFile(receiver, fileName, filePath, encryptedPath, messageType);
            }

        } catch (Exception e) {
            sendError("Failed to save file: " + e.getMessage());
            System.err.println("❌ Error saving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý group file
     */
    private void handleGroupFile(int groupId, String fileName, String filePath,
                                 String encryptedPath, String messageType) {
        // Kiểm tra user có phải member không
        if (!GroupDAO.isMember(groupId, this.userID)) {
            sendError("You are not a member of this group");
            return;
        }

        // Lưu vào database
        boolean saved = MessageDAO.saveMessage(
                this.userID,
                null,
                groupId,
                encryptedPath,
                messageType,
                filePath
        );

        if (saved) {
            System.out.println("💾 File message saved to DB");
        }

        // Tạo notification message
        JsonObject message = new JsonObject();
        message.addProperty("type", "NEW_MESSAGE");
        message.addProperty("sender", this.username);
        message.addProperty("groupId", groupId);
        message.addProperty("messageType", messageType);
        message.addProperty("fileName", fileName);
        message.addProperty("filePath", filePath);
        message.addProperty("content", "[File: " + fileName + "]");

        // Gửi cho sender
        sendMessage(message.toString());

        // Broadcast đến group
        ChatServer.broadcastToGroup(groupId, message.toString(), this);
        System.out.println("📤 File broadcasted to group#" + groupId);
    }

    /**
     * Xử lý private file
     */
    private void handlePrivateFile(String receiver, String fileName, String filePath,
                                   String encryptedPath, String messageType) {
        // Tìm receiver
        User receiverUser = UserDAO.getUserByUsername(receiver);

        if (receiverUser == null) {
            sendError("User not found: " + receiver);
            return;
        }

        // Lưu vào database
        boolean saved = MessageDAO.saveMessage(
                this.userID,
                receiverUser.getUserID(),
                null,
                encryptedPath,
                messageType,
                filePath
        );

        if (saved) {
            System.out.println("💾 File message saved to DB");
        }

        // Tạo message
        JsonObject message = new JsonObject();
        message.addProperty("type", "NEW_MESSAGE");
        message.addProperty("sender", this.username);
        message.addProperty("receiver", receiver);
        message.addProperty("messageType", messageType);
        message.addProperty("fileName", fileName);
        message.addProperty("filePath", filePath);
        message.addProperty("content", "[File: " + fileName + "]");

        // Gửi lại cho sender
        sendMessage(message.toString());

        // Gửi cho receiver
        boolean sent = ChatServer.sendToUser(receiver, message.toString());
        if (sent) {
            System.out.println("📤 File sent to: " + receiver);
        } else {
            System.out.println("❌ User offline: " + receiver);
        }
    }

    /**
     * Tạo group mới
     */
    private void handleCreateGroup(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String groupName = json.get("groupName").getAsString();
        JsonArray membersArray = json.getAsJsonArray("members");

        System.out.println("👥 Creating group: " + groupName);

        // Tạo group trong database
        Integer groupId = GroupDAO.createGroup(groupName, this.userID);

        if (groupId == null) {
            sendError("Failed to create group");
            return;
        }

        // Thêm creator vào group
        GroupDAO.addGroupMember(groupId, this.userID);

        // Thêm các members
        for (int i = 0; i < membersArray.size(); i++) {
            String memberUsername = membersArray.get(i).getAsString();
            User member = UserDAO.getUserByUsername(memberUsername);
            if (member != null) {
                GroupDAO.addGroupMember(groupId, member.getUserID());
            }
        }

        // Thông báo cho creator
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_CREATED");
        response.addProperty("groupId", groupId);
        response.addProperty("groupName", groupName);
        sendMessage(response.toString());

        // Thông báo cho các members
        JsonObject notification = new JsonObject();
        notification.addProperty("type", "GROUP_CREATED");
        notification.addProperty("groupId", groupId);
        notification.addProperty("groupName", groupName);
        ChatServer.broadcastToGroup(groupId, notification.toString(), this);

        System.out.println("✅ Group created: " + groupName + " (ID: " + groupId + ")");
    }

    /**
     * Lấy danh sách groups của user
     */
    private void handleGetUserGroups() {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        List<Group> groups = GroupDAO.getUserGroups(this.userID);

        JsonObject response = new JsonObject();
        response.addProperty("type", "USER_GROUPS");

        JsonArray groupsArray = new JsonArray();
        for (Group group : groups) {
            JsonObject g = new JsonObject();
            g.addProperty("groupId", group.getGroupID());
            g.addProperty("groupName", group.getGroupName());
            groupsArray.add(g);
        }
        response.add("groups", groupsArray);

        sendMessage(response.toString());
        System.out.println("📋 Sent " + groups.size() + " groups to " + username);
    }

    /**
     * Lấy danh sách members của group
     */
    private void handleGetGroupMembers(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        int groupId = json.get("groupId").getAsInt();
        List<String> members = GroupDAO.getGroupMembers(groupId);

        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_MEMBERS");
        response.add("members", gson.toJsonTree(members));

        sendMessage(response.toString());
        System.out.println("👥 Sent " + members.size() + " members of Group#" + groupId);
    }

    /**
     * Đổi tên group
     */
    private void handleRenameGroup(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        int groupId = json.get("groupId").getAsInt();
        String newName = json.get("newName").getAsString();

        boolean success = GroupDAO.updateGroupName(groupId, newName);

        if (success) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "GROUP_NAME_UPDATED");
            response.addProperty("groupId", groupId);
            response.addProperty("newName", newName);

            // Gửi cho tất cả members
            ChatServer.broadcastToGroup(groupId, response.toString(), null);
            System.out.println("✏️ Group#" + groupId + " renamed to: " + newName);
        } else {
            sendError("Failed to rename group");
        }
    }

    /**
     * Lấy lịch sử chat với user khác
     */
    private void handleGetChatHistory(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        String otherUsername = json.get("username").getAsString();
        int limit = json.has("limit") ? json.get("limit").getAsInt() : 50;

        User otherUser = UserDAO.getUserByUsername(otherUsername);
        if (otherUser == null) {
            sendError("User not found: " + otherUsername);
            return;
        }

        System.out.println("📜 Loading chat history: " + username + " <-> " + otherUsername);

        // Lấy tin nhắn từ database
        List<Message> messages = MessageDAO.getChatHistory(this.userID, otherUser.getUserID(), limit);

        // Tạo response
        JsonObject response = new JsonObject();
        response.addProperty("type", "CHAT_HISTORY");

        JsonArray messagesArray = new JsonArray();
        for (Message msg : messages) {
            JsonObject msgObj = new JsonObject();

            // Decrypt message
            String decrypted = SecurityUtil.decryptMessage(msg.getContentEncrypted());

            // Xác định sender
            String senderName = (msg.getSenderID() == this.userID) ? this.username : otherUsername;
            String receiverName = (msg.getSenderID() == this.userID) ? otherUsername : this.username;

            msgObj.addProperty("sender", senderName);
            msgObj.addProperty("content", decrypted);
            msgObj.addProperty("receiver", receiverName);
            msgObj.addProperty("timestamp", msg.getSentAt().toString());

            messagesArray.add(msgObj);
        }

        response.add("messages", messagesArray);
        sendMessage(response.toString());

        System.out.println("✅ Sent " + messages.size() + " messages to " + username);
    }

    /**
     * Lấy lịch sử chat của group
     */
    private void handleGetGroupHistory(JsonObject json) {
        if (this.userID == 0) {
            sendError("You must login first");
            return;
        }

        int groupId = json.get("groupId").getAsInt();
        int limit = json.has("limit") ? json.get("limit").getAsInt() : 50;

        // Kiểm tra user có phải member không
        if (!GroupDAO.isMember(groupId, this.userID)) {
            sendError("You are not a member of this group");
            return;
        }

        System.out.println("📜 Loading group history: Group#" + groupId);

        // Lấy tin nhắn từ database
        List<Message> messages = MessageDAO.getGroupMessages(groupId, limit);

        // Tạo response
        JsonObject response = new JsonObject();
        response.addProperty("type", "GROUP_HISTORY");

        JsonArray messagesArray = new JsonArray();
        for (Message msg : messages) {
            JsonObject msgObj = new JsonObject();

            // Decrypt message
            String decrypted = SecurityUtil.decryptMessage(msg.getContentEncrypted());

            // Lấy sender username
            User sender = UserDAO.getUserByUsername(
                    UserDAO.getUserById(msg.getSenderID()).getUsername()
            );
            String senderName = (sender != null) ? sender.getUsername() : "Unknown";

            msgObj.addProperty("sender", senderName);
            msgObj.addProperty("content", decrypted);
            msgObj.addProperty("groupId", groupId);
            msgObj.addProperty("timestamp", msg.getSentAt().toString());

            messagesArray.add(msgObj);
        }

        response.add("messages", messagesArray);
        sendMessage(response.toString());

        System.out.println("✅ Sent " + messages.size() + " group messages to " + username);
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

    public int getUserID() {
        return userID;
    }
}