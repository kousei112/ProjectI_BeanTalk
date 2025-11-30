package com.beantalk.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * message bubble component
 */
public class MessageBubblePanel extends JPanel{
    private String sender;
    private String message;
    private String time;
    private boolean isOwnMessage;

    public MessageBubblePanel(String sender, String message, String time, boolean isOwnMessage) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.isOwnMessage = isOwnMessage;

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // main bubble panel
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1) // them vien
        ));

        if (isOwnMessage) {
            bubblePanel.setBackground(new Color(220, 248, 198, 230));
        } else {
            bubblePanel.setBackground(new Color(255, 255, 255, 230));
        }

        // Sender name (neu khong phai tin nhan cua minh)
        if (!isOwnMessage) {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            senderLabel.setForeground(new Color(25, 118, 210));
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(senderLabel);
            bubblePanel.add(Box.createVerticalStrut(3));
        }

        // message content
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setBorder(null);
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubblePanel.add(messageArea);

        bubblePanel.add(Box.createVerticalStrut(3));

        // time
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(isOwnMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubblePanel.add(timeLabel);

        // wrapper panel de align bubble sang trai/phai
        JPanel wrapperPanel = new JPanel(new FlowLayout(
                isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(bubblePanel);

        add(wrapperPanel, BorderLayout.CENTER);
    }
}
