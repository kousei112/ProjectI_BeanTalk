package com.beantalk.ui;

import com.beantalk.client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Chat Frame
 */
public class MainChatFrame extends JFrame {
    private ChatClient client;
    private String username;

    // UI Components
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel onlineCountLabel;
    private JLabel chatWithLabel;

    private String currentReceiver = "ALL";

    public MainChatFrame(ChatClient client) {
        this.client = client;
        this.username = client.getUsername();

        initComponents();
        setupCallbacks();

        // Request online users
        client.getOnlineUsers();
    }

    private void initComponents() {
        setTitle("BeanTalk - " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ========== LEFT PANEL: USER LIST ==========
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 600));
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        // Online header
        JPanel onlineHeader = new JPanel(new BorderLayout());
        onlineHeader.setBackground(new Color(25, 118, 210));
        onlineHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

        onlineCountLabel = new JLabel("👥 Online (0)");
        onlineCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        onlineCountLabel.setForeground(Color.WHITE);
        onlineHeader.add(onlineCountLabel, BorderLayout.CENTER);

        leftPanel.add(onlineHeader, BorderLayout.NORTH);

        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals(username)) {
                    currentReceiver = selectedUser;
                    chatWithLabel.setText("💬 Chat with: " + selectedUser);
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        // Broadcast button
        JButton broadcastButton = new JButton("📢 Broadcast to ALL");
        broadcastButton.setFont(new Font("Arial", Font.BOLD, 12));
        broadcastButton.setBackground(new Color(76, 175, 80));
        broadcastButton.setForeground(Color.WHITE);
        broadcastButton.setFocusPainted(false);
        broadcastButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        broadcastButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        broadcastButton.addActionListener(e -> {
            currentReceiver = "ALL";
            chatWithLabel.setText("💬 Chat with: ALL");
            userList.clearSelection();
        });

        leftPanel.add(broadcastButton, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ========== RIGHT PANEL: CHAT AREA ==========
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        // Chat header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(new Color(33, 150, 243));
        chatHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        chatWithLabel = new JLabel("💬 Chat with: ALL");
        chatWithLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chatWithLabel.setForeground(Color.WHITE);
        chatHeader.add(chatWithLabel, BorderLayout.CENTER);

        rightPanel.add(chatHeader, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(null);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(8, 10, 8, 10)
        ));
        messageField.addActionListener(e -> sendMessage());

        // them emoji button
        JButton emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        emojiButton.setBackground(Color.WHITE);
        emojiButton.setFocusPainted(false);
        emojiButton.setBorder(new EmptyBorder(10, 15, 10, 15));
        emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emojiButton.setToolTipText("Choose Emoji");
        emojiButton.addActionListener(e -> {
            EmojiPicker picker = new EmojiPicker(this, messageField);
            picker.setVisible(true);
        });

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(25, 118, 210));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(10, 30, 10, 30));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());

        // panel cho emoji + send button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(emojiButton);
        buttonPanel.add(sendButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
    }

    private void setupCallbacks() {
        // New message callback
        client.setNewMessageCallback(msg -> {
            SwingUtilities.invokeLater(() -> {
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                String displayName = msg.sender.equals(username) ? "You" : msg.sender;

                chatArea.append(String.format("[%s, %s]\n%s\n\n", displayName, time, msg.content));
                chatArea.setCaretPosition(chatArea.getDocument().getLength());

                // them notification neu tin nhan khong phai cua minh
                if (!msg.sender.equals(username)) {
                    // kiem tra neu window khong focus -> show notification
                    if (!this.isFocused()) {
                        String preview = msg.content.length() > 50
                                ? msg.content.substring(0, 50) + "..."
                                : msg.content;
                        NotificationUtil.showNotification(msg.sender, preview, this);
                    }
                }
            });
        });

        // User joined callback
        client.setUserJoinedCallback(user -> {
            SwingUtilities.invokeLater(() -> {
                chatArea.append(String.format("👋 %s joined the chat\n\n", user));
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        });

        // User left callback
        client.setUserLeftCallback(user -> {
            SwingUtilities.invokeLater(() -> {
                chatArea.append(String.format("👋 %s left the chat\n\n", user));
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        });

        // Online users callback
        client.setOnlineUsersCallback(users -> {
            SwingUtilities.invokeLater(() -> {
                userListModel.clear();
                for (String user : users) {
                    userListModel.addElement(user);
                }
                onlineCountLabel.setText(String.format("👥 Online (%d)", users.size()));
            });
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        client.sendMessage(currentReceiver, message);
        messageField.setText("");
        messageField.requestFocus();
    }

    /**
     * Custom cell renderer cho user list
     */
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String user = (String) value;
            if (user.equals(username)) {
                label.setText("👤 " + user + " (You)");
                label.setFont(new Font("Arial", Font.BOLD, 14));
            } else {
                label.setText("🟢 " + user);
                label.setFont(new Font("Arial", Font.PLAIN, 14));
            }

            label.setBorder(new EmptyBorder(8, 10, 8, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 230, 255));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}