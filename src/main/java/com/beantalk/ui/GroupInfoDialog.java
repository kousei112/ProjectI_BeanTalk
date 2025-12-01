package com.beantalk.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog để hiển thị thông tin group và cho phép rename
 */
public class GroupInfoDialog extends JDialog {
    private String groupName;
    private List<String> members;
    private boolean nameChanged = false;
    private String newGroupName;

    public GroupInfoDialog(JFrame parent, String groupName, List<String> members) {
        super(parent, "Group Information", true);
        this.groupName = groupName;
        this.members = members;

        initComponents();
    }

    private void initComponents() {
        setSize(400, 500);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title with icon
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(25, 118, 210));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(groupName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton renameButton = new JButton("✏️");
        renameButton.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 16));
        renameButton.setBackground(new Color(33, 150, 243));
        renameButton.setForeground(Color.WHITE);
        renameButton.setFocusPainted(false);
        renameButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        renameButton.setToolTipText("Rename Group");
        renameButton.addActionListener(e -> handleRename(titleLabel));
        headerPanel.add(renameButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Members panel
        JPanel membersPanel = new JPanel(new BorderLayout(10, 10));
        membersPanel.setBackground(Color.WHITE);

        JLabel membersLabel = new JLabel("Members (" + members.size() + ")");
        membersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        membersPanel.add(membersLabel, BorderLayout.NORTH);

        // Members list
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String member : members) {
            model.addElement(member);
        }

        JList<String> membersList = new JList<>(model);
        membersList.setFont(new Font("Arial", Font.PLAIN, 14));
        membersList.setCellRenderer(new MemberCellRenderer());

        JScrollPane scrollPane = new JScrollPane(membersList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        membersPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(membersPanel, BorderLayout.CENTER);

        // Close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(25, 118, 210));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new EmptyBorder(10, 30, 10, 30));
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleRename(JLabel titleLabel) {
        String newName = JOptionPane.showInputDialog(
                this,
                "Enter new group name:",
                "Rename Group",
                JOptionPane.PLAIN_MESSAGE
        );

        if (newName != null && !newName.trim().isEmpty()) {
            newGroupName = newName.trim();
            titleLabel.setText(newGroupName);
            nameChanged = true;

            JOptionPane.showMessageDialog(
                    this,
                    "Group renamed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public boolean isNameChanged() {
        return nameChanged;
    }

    public String getNewGroupName() {
        return newGroupName;
    }

    public String getGroupName() {
        return newGroupName != null ? newGroupName : groupName;
    }

    /**
     * Custom cell renderer for members list
     */
    private class MemberCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setText("  👤 " + value);
            label.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 14));
            label.setBorder(new EmptyBorder(8, 10, 8, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 230, 255));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}