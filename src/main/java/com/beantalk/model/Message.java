package com.beantalk.model;

import java.awt.print.PrinterGraphics;
import java.time.LocalDateTime;

public class Message {
    private int messageID;
    private int senderID;
    private Integer receiverID;
    private Integer groupID;
    private String contentEncrypted;
    private String messageType;
    private String filePath;
    private LocalDateTime sentAt;

    // constructor day du
    public Message (int messageID, int senderID, Integer receiverID, Integer groupID, String contentEncrypted, String messageType, String filePath, LocalDateTime sentAt) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.groupID = groupID;
        this.contentEncrypted = contentEncrypted;
        this.messageType = messageType;
        this.filePath = filePath;
        this.sentAt = sentAt;
    }

    // constructor cho tin nhan moi
    public Message(int senderID, Integer receiverID, Integer groupID, String contentEncrypted, String messageType) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.groupID = groupID;
        this.contentEncrypted = contentEncrypted;
        this.messageType = messageType;
        this.sentAt = LocalDateTime.now();
    }

    public int getMessageID() {
        return messageID;
    }
    public void setMessageID(int messageId) { this.messageID = messageId; }

    public int getSenderID() { return senderID; }
    public void setSenderID(int senderID) { this.senderID = senderID; }

    public Integer getReceiverID() { return receiverID; }
    public void setReceiverID(Integer receiverID) { this.receiverID = receiverID; }

    public Integer getGroupID() { return groupID; }
    public void setGroupID(Integer groupID) { this.groupID = groupID; }

    public String getContentEncrypted() { return contentEncrypted; }
    public void setContentEncrypted(String contentEncrypted) { this.contentEncrypted = contentEncrypted; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
