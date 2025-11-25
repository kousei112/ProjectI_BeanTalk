package com.beantalk.model;

import java.time.LocalDateTime;

public class Group {
    private int groupID;
    private String groupName;
    private int createdBy;
    private LocalDateTime createdAt;

    public Group(int groupID, String groupName, int createdBy, LocalDateTime createdAt) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Group(String groupName, int createdBy) {
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getGroupID() { return groupID; }
    public void setGroupID(int groupId) { this.groupID = groupID; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}