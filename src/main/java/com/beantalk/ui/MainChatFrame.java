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
 * Main Chat Frame với Group Chat support
 */
public class MainChatFrame extends JFrame {
    private ChatClient client;
    private String username;

    // UI Components
    private JPanel welcomePanel;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;

    // Left panel - Users
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel onlineCountLabel;

    // Left panel - Groups
    private DefaultListModel<String> groupListModel;
    private JList<String> groupList;
    private JLabel groupCountLabel;

    // Right panel - Group members
    private JPanel membersPanel;
    private DefaultListModel<String> membersListModel;
    private JList<String> membersList;

    private JLabel chatWithLabel;

    private String currentReceiver = null;
    private Integer currentGroupId = null;
    private List<ChatClient.GroupData> userGroups;

    public MainChatFrame(ChatClient client) {
        this.client = client;
        this.username = client.getUsername();

        initComponents();
        setupCallbacks();

        // Request online users và groups
        client.getOnlineUsers();
        client.getUserGroups();
    }

    private void initComponents() {
        setTitle("BeanTalk - " + username);
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ========== LEFT PANEL: USERS & GROUPS ==========
        JPanel leftPanel = createLeftPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ========== CENTER PANEL: CHAT AREA ==========
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ========== RIGHT PANEL: GROUP MEMBERS ==========
        membersPanel = createMembersPanel();
        membersPanel.setVisible(false); // Ẩn ban đầu
        mainPanel.add(membersPanel, BorderLayout.EAST);

        add(mainPanel);

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
    }

    /**
     * Tạo left panel với users và groups
     */
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 600));
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        // Tabbed pane cho Users và Groups
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Tab 1: Users
        JPanel usersTab = createUsersTab();
        tabbedPane.addTab("👤 Users", usersTab);

        // Tab 2: Groups
        JPanel groupsTab = createGroupsTab();
        tabbedPane.addTab("👥 Groups", groupsTab);

        leftPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel("BeanTalk v1.0", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.add(footerLabel, BorderLayout.SOUTH);

        return leftPanel;
    }

    /**
     * Tạo tab Users
     */
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 118, 210));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        onlineCountLabel = new JLabel("👥 Online (0)");
        onlineCountLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.BOLD, 13));
        onlineCountLabel.setForeground(Color.WHITE);
        header.add(onlineCountLabel, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);

        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Arial", Font.PLAIN, 13));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals(username)) {
                    switchToPrivateChat(selectedUser);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo tab Groups
     */
    private JPanel createGroupsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header với nút Create Group
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(76, 175, 80));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        groupCountLabel = new JLabel("👥 Groups (0)");
        groupCountLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.BOLD, 13));
        groupCountLabel.setForeground(Color.WHITE);
        header.add(groupCountLabel, BorderLayout.CENTER);

        JButton createGroupBtn = new JButton("+");
        createGroupBtn.setFont(new Font("Arial", Font.BOLD, 16));
        createGroupBtn.setBackground(new Color(67, 160, 71));
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setFocusPainted(false);
        createGroupBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        createGroupBtn.setToolTipText("Create New Group");
        createGroupBtn.addActionListener(e -> handleCreateGroup());
        header.add(createGroupBtn, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Group list
        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setFont(new Font("Arial", Font.PLAIN, 13));
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setCellRenderer(new GroupListCellRenderer());
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = groupList.getSelectedIndex();
                if (selectedIndex >= 0 && userGroups != null && selectedIndex < userGroups.size()) {
                    ChatClient.GroupData group = userGroups.get(selectedIndex);
                    switchToGroupChat(group.groupId, group.groupName);
                }
            }
        });

        // Context menu cho group
        JPopupMenu groupPopup = new JPopupMenu();
        JMenuItem viewInfoItem = new JMenuItem("View Info");
        viewInfoItem.addActionListener(e -> showGroupInfo());
        groupPopup.add(viewInfoItem);

        groupList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = groupList.locationToIndex(e.getPoint());
                    groupList.setSelectedIndex(index);
                    groupPopup.show(groupList, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(groupList);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo center panel với chat area
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Chat header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(new Color(33, 150, 243));
        chatHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        chatWithLabel = new JLabel("💬 BeanTalk Chat");
        chatWithLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chatWithLabel.setForeground(Color.WHITE);
        chatHeader.add(chatWithLabel, BorderLayout.CENTER);

        centerPanel.add(chatHeader, BorderLayout.NORTH);

        // Welcome panel
        welcomePanel = createWelcomePanel();

        // Chat panel
        chatPanel = createBackgroundPanel("/images/chatwallpaper.jpg");
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatPanel.setVisible(false);
        chatPanel.setOpaque(false);

        // Container
        JPanel chatContainer = new JPanel(new CardLayout());
        chatContainer.setOpaque(false);
        chatContainer.add(welcomePanel, "WELCOME");
        chatContainer.add(chatPanel, "CHAT");

        chatScrollPane = new JScrollPane(chatContainer);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Message input panel
        JPanel inputPanel = createInputPanel();
        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        return centerPanel;
    }

    /**
     * Tạo members panel (hiển thị bên phải khi vào group)
     */
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(180, 600));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(76, 175, 80));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("👥 Members");
        headerLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.BOLD, 13));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);

        // Members list
        membersListModel = new DefaultListModel<>();
        membersList = new JList<>(membersListModel);
        membersList.setFont(new Font("Arial", Font.PLAIN, 13));
        membersList.setCellRenderer(new MemberCellRenderer());

        JScrollPane scrollPane = new JScrollPane(membersList);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo input panel
     */
    private JPanel createInputPanel() {
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

        // Emoji button
        JButton emojiButton = new JButton("😊");
        emojiButton.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 20));
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(emojiButton);
        buttonPanel.add(sendButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        return inputPanel;
    }

    /**
     * Tạo welcome panel
     */
    private JPanel createWelcomePanel() {
        JPanel panel = createBackgroundPanel("/images/a.jpg");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setOpaque(false);

        panel.add(Box.createVerticalGlue());

        // Logo
        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                Image logoImage = ImageIO.read(logoStream);
                Image scaledLogo = logoImage.getScaledInstance(323, 118, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(logoLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }

        panel.add(Box.createVerticalStrut(30));

        JLabel welcomeLabel = new JLabel("Welcome to BeanTalk!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Select a user or group to start chatting", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.BLACK);
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
                    }
                } catch (Exception e) {
                    System.err.println("Error loading background: " + e.getMessage());
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

    /**
     * Setup callbacks
     */
    private void setupCallbacks() {
        // New message callback
        client.setNewMessageCallback(msg -> {
            SwingUtilities.invokeLater(() -> {
                // Kiểm tra xem có nên hiển thị tin nhắn không
                boolean shouldDisplay = false;
                boolean autoOpenChat = false;

                System.out.println("📩 Received message:");
                System.out.println("  - Sender: " + msg.sender);
                System.out.println("  - Content: " + msg.content);
                System.out.println("  - Receiver: " + msg.receiver);
                System.out.println("  - GroupId: " + msg.groupId);
                System.out.println("  - Current Receiver: " + currentReceiver);
                System.out.println("  - Current GroupId: " + currentGroupId);
                System.out.println("  - My username: " + username);

                if (msg.groupId != null) {
                    // GROUP MESSAGE
                    if (msg.groupId.equals(currentGroupId)) {
                        shouldDisplay = true;
                        System.out.println("  ✅ Display: Group message matches current group");
                    } else if (!msg.sender.equals(username)) {
                        // Tin nhắn group mới mà chưa mở -> đợi user click vào notification
                        System.out.println("  ⏳ Group message for different group - waiting for user action");
                    }
                } else {
                    // PRIVATE MESSAGE

                    // Case 1: Tin nhắn mình gửi đi (sender = mình)
                    if (msg.sender.equals(username)) {
                        // Kiểm tra receiver có phải là người đang chat không
                        if (currentReceiver != null && msg.receiver != null &&
                                msg.receiver.equals(currentReceiver)) {
                            shouldDisplay = true;
                            System.out.println("  ✅ Display: Own message to current receiver");
                        }
                    }
                    // Case 2: Tin nhắn người khác gửi đến mình
                    else if (msg.receiver != null && msg.receiver.equals(username)) {
                        if (currentReceiver != null && currentGroupId == null) {
                            // Đang ở private chat mode
                            if (msg.sender.equals(currentReceiver)) {
                                shouldDisplay = true;
                                System.out.println("  ✅ Display: Message from current receiver");
                            }
                        } else {
                            // Chưa mở chat với ai hoặc đang ở group chat
                            // Tự động mở chat với người gửi
                            shouldDisplay = true;
                            autoOpenChat = true;
                            System.out.println("  ✅ Display: New message - auto opening chat");
                        }
                    }
                }

                // Tự động mở chat nếu cần
                if (autoOpenChat && !msg.sender.equals(username)) {
                    switchToPrivateChat(msg.sender);
                    // Tìm và select user trong list
                    for (int i = 0; i < userListModel.getSize(); i++) {
                        if (userListModel.get(i).equals(msg.sender)) {
                            userList.setSelectedIndex(i);
                            break;
                        }
                    }
                    System.out.println("  📂 Auto-opened chat with: " + msg.sender);
                }

                if (shouldDisplay) {
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    boolean isOwn = msg.sender.equals(username);

                    MessageBubblePanel bubble = new MessageBubblePanel(
                            msg.sender,
                            msg.content,
                            time,
                            isOwn
                    );

                    chatPanel.add(bubble);
                    chatPanel.revalidate();
                    chatPanel.repaint();

                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });

                    System.out.println("  ✅ Message displayed in chat panel");
                } else {
                    System.out.println("  ❌ Message NOT displayed - Current receiver: " + currentReceiver);
                }

                // Notification với callback để mở chat khi click
                if (!msg.sender.equals(username) && !this.isFocused()) {
                    String preview = msg.content.length() > 50
                            ? msg.content.substring(0, 50) + "..."
                            : msg.content;

                    String senderName = msg.sender;
                    Integer groupId = msg.groupId;

                    NotificationUtil.showNotificationWithCallback(
                            msg.sender,
                            preview,
                            this,
                            () -> {
                                // Khi click vào notification, tự động mở chat
                                SwingUtilities.invokeLater(() -> {
                                    if (groupId != null) {
                                        // GROUP MESSAGE - tìm và mở group
                                        for (int i = 0; i < userGroups.size(); i++) {
                                            if (userGroups.get(i).groupId == groupId.intValue()) {
                                                // Chuyển sang tab Groups
                                                JPanel leftPanel = (JPanel) ((JPanel) getContentPane()
                                                        .getComponent(0)).getComponent(0);
                                                JTabbedPane tabbedPane = (JTabbedPane) leftPanel.getComponent(0);
                                                tabbedPane.setSelectedIndex(1); // Tab Groups
                                                // Select group
                                                groupList.setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    } else {
                                        // PRIVATE MESSAGE - tìm user trong list
                                        for (int i = 0; i < userListModel.getSize(); i++) {
                                            if (userListModel.get(i).equals(senderName)) {
                                                // Chuyển sang tab Users
                                                JPanel leftPanel = (JPanel) ((JPanel) getContentPane()
                                                        .getComponent(0)).getComponent(0);
                                                JTabbedPane tabbedPane = (JTabbedPane) leftPanel.getComponent(0);
                                                tabbedPane.setSelectedIndex(0); // Tab Users
                                                // Select user
                                                userList.setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    // Focus và bring to front
                                    setVisible(true);
                                    toFront();
                                    requestFocus();
                                });
                            }
                    );
                }
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

        // User groups callback
        client.setUserGroupsCallback(groups -> {
            SwingUtilities.invokeLater(() -> {
                this.userGroups = groups;
                groupListModel.clear();
                for (ChatClient.GroupData group : groups) {
                    groupListModel.addElement(group.groupName);
                }
                groupCountLabel.setText(String.format("👥 Groups (%d)", groups.size()));
            });
        });

        // Group members callback
        client.setGroupMembersCallback(members -> {
            SwingUtilities.invokeLater(() -> {
                membersListModel.clear();
                for (String member : members) {
                    membersListModel.addElement(member);
                }
            });
        });

        // Group name updated callback
        client.setGroupNameUpdatedCallback((groupId, newName) -> {
            SwingUtilities.invokeLater(() -> {
                if (groupId.equals(currentGroupId)) {
                    chatWithLabel.setText("👥 Group: " + newName);
                }
            });
        });

        // Chat history callback
        client.setChatHistoryCallback(history -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("📜 Received chat history: " + history.size() + " messages");

                // Clear chat panel
                chatPanel.removeAll();

                // Hiển thị từng tin nhắn
                for (ChatClient.MessageData msg : history) {
                    String time = ""; // Có thể parse từ timestamp nếu cần
                    boolean isOwn = msg.sender.equals(username);

                    MessageBubblePanel bubble = new MessageBubblePanel(
                            msg.sender,
                            msg.content,
                            time,
                            isOwn
                    );

                    chatPanel.add(bubble);
                }

                chatPanel.revalidate();
                chatPanel.repaint();

                // Scroll to bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            });
        });

        // Group history callback
        client.setGroupHistoryCallback(history -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("📜 Received group history: " + history.size() + " messages");

                // Clear chat panel
                chatPanel.removeAll();

                // Hiển thị từng tin nhắn
                for (ChatClient.MessageData msg : history) {
                    String time = ""; // Có thể parse từ timestamp nếu cần
                    boolean isOwn = msg.sender.equals(username);

                    MessageBubblePanel bubble = new MessageBubblePanel(
                            msg.sender,
                            msg.content,
                            time,
                            isOwn
                    );

                    chatPanel.add(bubble);
                }

                chatPanel.revalidate();
                chatPanel.repaint();

                // Scroll to bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            });
        });
    }

    /**
     * Chuyển sang private chat
     */
    private void switchToPrivateChat(String user) {
        currentReceiver = user;
        currentGroupId = null;
        chatWithLabel.setText("👤 Chat with: " + user);

        // Clear và hiển thị chat panel
        chatPanel.removeAll();
        welcomePanel.setVisible(false);
        chatPanel.setVisible(true);

        // Hiển thị loading message
        JLabel loadingLabel = new JLabel("Loading chat history...");
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        loadingLabel.setForeground(Color.GRAY);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatPanel.add(Box.createVerticalGlue());
        chatPanel.add(loadingLabel);
        chatPanel.add(Box.createVerticalGlue());

        chatPanel.revalidate();
        chatPanel.repaint();

        // Ẩn members panel
        membersPanel.setVisible(false);

        // Load chat history
        client.getChatHistory(user, 50);
        System.out.println("📜 Requesting chat history with: " + user);
    }

    /**
     * Chuyển sang group chat
     */
    private void switchToGroupChat(int groupId, String groupName) {
        currentReceiver = null;
        currentGroupId = groupId;
        chatWithLabel.setText("👥 Group: " + groupName);

        // Clear và hiển thị chat panel
        chatPanel.removeAll();
        welcomePanel.setVisible(false);
        chatPanel.setVisible(true);

        // Hiển thị loading message
        JLabel loadingLabel = new JLabel("Loading group history...");
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        loadingLabel.setForeground(Color.GRAY);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatPanel.add(Box.createVerticalGlue());
        chatPanel.add(loadingLabel);
        chatPanel.add(Box.createVerticalGlue());

        chatPanel.revalidate();
        chatPanel.repaint();

        // Hiển thị members panel
        membersPanel.setVisible(true);
        client.getGroupMembers(groupId);

        // Load group history
        client.getGroupHistory(groupId, 50);
        System.out.println("📜 Requesting group history: Group#" + groupId);
    }

    /**
     * Xử lý tạo group
     */
    private void handleCreateGroup() {
        List<String> onlineUsers = new java.util.ArrayList<>();
        for (int i = 0; i < userListModel.size(); i++) {
            onlineUsers.add(userListModel.get(i));
        }

        CreateGroupDialog dialog = new CreateGroupDialog(this, onlineUsers, username);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String groupName = dialog.getGroupName();
            List<String> members = dialog.getSelectedMembers();

            client.createGroup(groupName, members);

            JOptionPane.showMessageDialog(this,
                    "Group created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Hiển thị thông tin group
     */
    private void showGroupInfo() {
        int selectedIndex = groupList.getSelectedIndex();
        if (selectedIndex >= 0 && userGroups != null && selectedIndex < userGroups.size()) {
            ChatClient.GroupData group = userGroups.get(selectedIndex);

            // Get members từ server
            client.setGroupMembersCallback(members -> {
                SwingUtilities.invokeLater(() -> {
                    GroupInfoDialog dialog = new GroupInfoDialog(
                            this,
                            group.groupName,
                            members
                    );
                    dialog.setVisible(true);

                    if (dialog.isNameChanged()) {
                        client.renameGroup(group.groupId, dialog.getNewGroupName());
                    }
                });
            });

            client.getGroupMembers(group.groupId);
        }
    }

    /**
     * Gửi message
     */
    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Kiểm tra đã chọn receiver/group chưa
        if (currentGroupId == null && currentReceiver == null) {
            // Chưa chọn -> hiển thị dialog để chọn
            Object[] options = {"Select User", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Please select a user or group to chat with first!",
                    "No Chat Selected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == JOptionPane.YES_OPTION) {
                // Chuyển sang tab Users
                JTabbedPane tabbedPane = (JTabbedPane)
                        ((JPanel) getContentPane().getComponent(0))
                                .getComponent(0);
                tabbedPane.setSelectedIndex(0); // Chọn tab Users
            }

            return;
        }

        // Gửi tin nhắn
        client.sendMessage(currentReceiver, message, currentGroupId);
        messageField.setText("");
        messageField.requestFocus();
    }

    // ========== CELL RENDERERS ==========

    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String user = (String) value;
            if (user.equals(username)) {
                label.setText("  " + user + " (You)");
                label.setFont(new Font("Arial", Font.BOLD, 13));
            } else {
                label.setText("  " + user);
                label.setFont(new Font("Arial", Font.PLAIN, 13));
            }

            label.setBorder(new EmptyBorder(8, 10, 8, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 230, 255));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }

    private class GroupListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            label.setText("  👥 " + value);
            label.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(8, 10, 8, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 230, 200));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }

    private class MemberCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String member = (String) value;
            if (member.equals(username)) {
                label.setText("  👤 " + member + " (You)");
                label.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.BOLD, 12));
            } else {
                label.setText("  👤 " + member);
                label.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 12));
            }

            label.setBorder(new EmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                label.setBackground(new Color(220, 240, 220));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}