package com.beantalk.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog để tạo group chat mới
 */
public class CreateGroupDialog extends JDialog {
    private JTextField groupNameField;
    private DefaultListModel<String> availableUsersModel;
    private DefaultListModel<String> selectedUsersModel;
    private JList<String> availableUsersList;
    private JList<String> selectedUsersList;
    private boolean confirmed = false;

    private String groupName;
    private List<String> selectedMembers;

    public CreateGroupDialog(JFrame parent, List<String> onlineUsers, String currentUsername) {
        super(parent, "Create Group Chat", true);
        this.selectedMembers = new ArrayList<>();

        initComponents(onlineUsers, currentUsername);
    }

    private void initComponents(List<String> onlineUsers, String currentUsername) {
        setSize(600, 500);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("👥 Create New Group", SwingConstants.CENTER);
        titleLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 118, 210));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);

        // Group name
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("Group Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        groupNameField = new JTextField();
        groupNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        groupNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(8, 10, 8, 10)
        ));
        namePanel.add(nameLabel, BorderLayout.NORTH);
        namePanel.add(groupNameField, BorderLayout.CENTER);
        centerPanel.add(namePanel, BorderLayout.NORTH);

        // Members selection
        JPanel membersPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        membersPanel.setBackground(Color.WHITE);

        // Available users
        JPanel availablePanel = createUserListPanel("Available Users", onlineUsers, currentUsername);
        // Lấy scroll pane (component thứ 2 của panel)
        JScrollPane availableScrollPane = (JScrollPane) availablePanel.getComponent(1);
        availableUsersList = (JList<String>) availableScrollPane.getViewport().getView();
        availableUsersModel = (DefaultListModel<String>) availableUsersList.getModel();
        membersPanel.add(availablePanel);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(Box.createVerticalGlue());

        JButton addButton = new JButton("Add →");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(e -> moveUser(availableUsersList, availableUsersModel,
                selectedUsersList, selectedUsersModel));

        JButton removeButton = new JButton("← Remove");
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.addActionListener(e -> moveUser(selectedUsersList, selectedUsersModel,
                availableUsersList, availableUsersModel));

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalGlue());
        membersPanel.add(buttonPanel);

        // Selected users
        JPanel selectedPanel = createUserListPanel("Selected Members", new ArrayList<>(), null);
        JScrollPane selectedScrollPane = (JScrollPane) selectedPanel.getComponent(1);
        selectedUsersList = (JList<String>) selectedScrollPane.getViewport().getView();
        selectedUsersModel = (DefaultListModel<String>) selectedUsersList.getModel();
        membersPanel.add(selectedPanel);

        centerPanel.add(membersPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);

        JButton createButton = new JButton("Create Group");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setBackground(new Color(76, 175, 80));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> handleCreate());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        bottomPanel.add(cancelButton);
        bottomPanel.add(createButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createUserListPanel(String title, List<String> users, String excludeUser) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(titleLabel, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String user : users) {
            if (excludeUser == null || !user.equals(excludeUser)) {
                model.addElement(user);
            }
        }

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void moveUser(JList<String> fromList, DefaultListModel<String> fromModel,
                          JList<String> toList, DefaultListModel<String> toModel) {
        List<String> selected = fromList.getSelectedValuesList();

        if (selected.isEmpty()) {
            return;
        }

        for (String user : selected) {
            fromModel.removeElement(user);
            toModel.addElement(user);
        }
    }

    private void handleCreate() {
        groupName = groupNameField.getText().trim();

        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a group name!",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedUsersModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one member!",
                    "No Members",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Collect selected members
        selectedMembers.clear();
        for (int i = 0; i < selectedUsersModel.size(); i++) {
            selectedMembers.add(selectedUsersModel.get(i));
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getSelectedMembers() {
        return selectedMembers;
    }
}