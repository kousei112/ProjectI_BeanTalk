package com.beantalk.ui;

import com.beantalk.client.ChatClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.InputStream;


/**
 * Main Chat Frame
 */
public class MainChatFrame extends JFrame {
    private ChatClient client;
    private String username;

    // UI Components
    //private JTextArea chatArea;
    private  JPanel welcomePanel;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel onlineCountLabel;
    private JLabel chatWithLabel;

    private String currentReceiver = null;

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
                    chatWithLabel.setText("Chat with: " + selectedUser);

                    // chuyen tu welcome sang chat
                    welcomePanel.setVisible(false);
                    chatPanel.setVisible(true);

                    // refresh container
                    chatScrollPane.revalidate();
                    chatScrollPane.repaint();
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        // Footer cho left panel
        JLabel footerLabel = new JLabel("BeanTalk v1.0", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.add(footerLabel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ========== RIGHT PANEL: CHAT AREA ==========
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        // Chat header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(new Color(33, 150, 243));
        chatHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        chatWithLabel = new JLabel(" Chat with: ALL");
        chatWithLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chatWithLabel.setForeground(Color.WHITE);
        chatHeader.add(chatWithLabel, BorderLayout.CENTER);

        rightPanel.add(chatHeader, BorderLayout.NORTH);

        // welcom panel
        welcomePanel = createWelcomePanel();

        // chat panel (an ban dau)
        chatPanel = createBackgroundPanel("/images/chatwallpaper.jpg");
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatPanel.setVisible(false);
        chatPanel.setOpaque(false);

        //  container switch welcome va chat
        JPanel chatContainer = new JPanel(new CardLayout());
        chatContainer.setOpaque(false);
        chatContainer.add(welcomePanel, "WELCOME");
        chatContainer.add(chatPanel, "CHAT");

        chatScrollPane = new JScrollPane(chatContainer);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
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

        // sendButton
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

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

    // Tao welcome panel voi anh nen
    private JPanel createWelcomePanel() {
        // ✅ DÙNG PANEL VỚI ẢNH NỀN
        JPanel panel = createBackgroundPanel("/images/a.jpg");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setOpaque(false);  // Để thấy background

        panel.add(Box.createVerticalGlue());

        // them logo
        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                Image logoImage = ImageIO.read(logoStream);
                // scale logo
                Image scaledLogo = logoImage.getScaledInstance(323, 118, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(logoLabel);
            } else {
                // Fallback: dùng emoji nếu không tìm thấy logo
                JLabel iconLabel = new JLabel("💬", SwingConstants.CENTER);
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(iconLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            // Fallback: dùng emoji
            JLabel iconLabel = new JLabel("💬", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(iconLabel);
        }

        panel.add(Box.createVerticalStrut(30));

        // Welcome text với shadow để dễ đọc
        JLabel welcomeLabel = new JLabel("Welcome to BeanTalk!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.BLACK);  // Chu den
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Thêm shadow cho text (để dễ đọc trên ảnh nền)
        welcomeLabel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(new Color(0, 0, 0, 100), 0)
        ));

        JLabel subtitleLabel = new JLabel("Select a user from the list to start chatting", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.BLACK);  // Chu den
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createBackgroundPanel(String imagePath) {
        return new JPanel() {
            private Image backgroundImage;

            {
                try {
                    InputStream imgStream = getClass().getResourceAsStream(imagePath);
                    if (imgStream != null) {
                        backgroundImage = ImageIO.read(imgStream);
                        System.out.println("Background loaded: " + imagePath);
                    } else {
                        System.err.println("Background not found: " + imagePath);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading background: " +  e.getMessage());
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
    }

    private void setupCallbacks() {
        // New message callback
        client.setNewMessageCallback(msg -> {
            SwingUtilities.invokeLater(() -> {
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                boolean isOwn = msg.sender.equals(username);

                // ✅ DÙNG MESSAGE BUBBLE
                MessageBubblePanel bubble = new MessageBubblePanel(
                        msg.sender,
                        msg.content,
                        time,
                        isOwn
                );

                chatPanel.add(bubble);
                chatPanel.revalidate();
                chatPanel.repaint();

                // Scroll to bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });

                // Notification
                if (!isOwn && !this.isFocused()) {
                    String preview = msg.content.length() > 50
                            ? msg.content.substring(0, 50) + "..."
                            : msg.content;
                    NotificationUtil.showNotification(msg.sender, preview, this);
                }
            });
        });

        // User joined callback
        client.setUserJoinedCallback(user -> {
            SwingUtilities.invokeLater(() -> {
                JLabel notifLabel = new JLabel(" " + user + " joined the chat");
                notifLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                notifLabel.setForeground(Color.GRAY);
                notifLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

                chatPanel.add(notifLabel);
                chatPanel.revalidate();
                chatPanel.repaint();
            });
        });

        // User left callback
        client.setUserLeftCallback(user -> {
            SwingUtilities.invokeLater(() -> {
                JLabel notifLabel = new JLabel(" " + user + " left the chat");
                notifLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                notifLabel.setForeground(Color.GRAY);
                notifLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

                chatPanel.add(notifLabel);
                chatPanel.revalidate();
                chatPanel.repaint();
            });
        });

        // Online users callback
        client.setOnlineUsersCallback(users -> {
            SwingUtilities.invokeLater(() -> {
                userListModel.clear();
                for (String user : users) {
                    userListModel.addElement(user);
                }
                onlineCountLabel.setText(String.format(" Online (%d)", users.size()));
            });
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // phai chon user truoc
        if (currentReceiver == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user to chat with!",
                    "No User Selected",
                    JOptionPane.WARNING_MESSAGE
            );
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
                label.setText(" " + user + " (You)");
                label.setFont(new Font("Arial", Font.BOLD, 14));
            } else {
                label.setText(" " + user);
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