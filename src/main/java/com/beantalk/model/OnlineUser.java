package com.beantalk.model;

import java.time.LocalDateTime;

public class OnlineUser {
    private int userID;
    private String status;  // ONLINE, OFFLINE
    private String ipAddress;
    private int port;
    private LocalDateTime lastActive;

    public OnlineUser(int userID, String status, String ipAddress, int port, LocalDateTime lastActive) {
        this.userID = userID;
        this.status = status;
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastActive = lastActive;
    }

    // Getters and Setters
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
}