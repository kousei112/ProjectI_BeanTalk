package com.beantalk.server;

import com.beantalk.util.GroupDAO;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Chat Server - Lắng nghe kết nối từ clients với Group Chat support
 */
public class ChatServer {
    private static final int PORT = 5555;
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   BEANTALK CHAT SERVER");
        System.out.println("=================================");

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port: " + PORT);
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Tạo thread mới để xử lý client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Broadcast message đến tất cả clients
     */
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Broadcast message đến tất cả members của group
     */
    public static void broadcastToGroup(int groupId, String message, ClientHandler sender) {
        // Lấy danh sách user IDs trong group
        List<Integer> memberIds = GroupDAO.getGroupMemberIds(groupId);

        for (ClientHandler client : clientHandlers) {
            // Gửi cho tất cả members, trừ sender (nếu có)
            if (client.getUserID() != 0 &&
                    memberIds.contains(client.getUserID()) &&
                    client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Gửi message đến 1 client cụ thể
     */
    public static boolean sendToUser(String username, String message) {
        for (ClientHandler client : clientHandlers) {
            if (client.getUsername() != null && client.getUsername().equals(username)) {
                client.sendMessage(message);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove client khi disconnect
     */
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getUsername());
        System.out.println("Total clients online: " + clientHandlers.size());
    }

    /**
     * Lấy danh sách users online
     */
    public static List<String> getOnlineUsers() {
        List<String> users = new ArrayList<>();
        for (ClientHandler client : clientHandlers) {
            if (client.getUsername() != null) {
                users.add(client.getUsername());
            }
        }
        return users;
    }

    /**
     * Shutdown server
     */
    private static void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clientHandlers) {
                client.disconnect();
            }
            System.out.println("Server shutdown complete.");
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
}