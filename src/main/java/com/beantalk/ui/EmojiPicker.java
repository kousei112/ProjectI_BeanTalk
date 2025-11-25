package com.beantalk.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * emoji picker dialog
 */
public class EmojiPicker extends JDialog{
    private JTextField targetField;

    private static final String[] EMOJIS = {
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "😚", "😙", "😋", "😛", "😜", "🤪",
            "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨",
            "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
            "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕",
            "🤢", "🤮", "🤧", "🥵", "🥶", "😶‍🌫️", "😵", "🤯",
            "🤠", "🥳", "😎", "🤓", "🧐", "😕", "😟", "🙁",
            "☹️", "😮", "😯", "😲", "😳", "🥺", "😦", "😧",
            "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣",
            "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
            "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "🤙",
            "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💪",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "💔", "❤️‍🔥", "❤️‍🩹", "💕", "💞", "💓", "💗", "💖",
            "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉️", "☸️"
    };

    public EmojiPicker(JFrame parent, JTextField targetField) {
        super(parent, "Choose Emoji", false);
        this.targetField = targetField;
        initComponents();
    }

    private void initComponents() {
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // title
        JLabel titleLabel = new JLabel("😊 Choose an Emoji", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // emoji grid
        JPanel emojiPanel = new JPanel(new GridLayout(0, 8, 5, 5));
        emojiPanel.setBackground(Color.WHITE);
        emojiPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (String emoji : EMOJIS) {
            JButton emojiButton = new JButton(emoji);
            emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            emojiButton.setFocusPainted(false);
            emojiButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            emojiButton.setBackground(Color.WHITE);
            emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            emojiButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    emojiButton.setBackground(new Color(230, 240, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    emojiButton.setBackground(Color.WHITE);
                }
            });

            emojiButton.addActionListener(e -> {
                targetField.setText(targetField.getText() + emoji);
                targetField.requestFocus();
                dispose();
            });

            emojiPanel.add(emojiButton);
        }

        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }
}
