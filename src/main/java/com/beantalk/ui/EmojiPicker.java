package com.beantalk.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * emoji picker dialog
 */
public class EmojiPicker extends JDialog{
    private JTextField targetField;

    private static final String[][] EMOTICONS = {
            // Happy
            {":)", "Happy"},
            {":D", "Big smile"},
            {"XD", "Laugh"},
            {";)", "Wink"},
            {":P", "Tongue out"},
            {"^_^", "Cheerful"},
            {"^o^", "Excited"},
            {"(^_^)", "Happy face"},

            // Love
            {"<3", "Heart"},
            {"<3<3", "Hearts"},
            {":*", "Kiss"},
            {":-*", "Kiss"},
            {"*_*", "Starry eyes"},
            {"♥", "Heart symbol"},

            // Sad
            {":(", "Sad"},
            {":'(", "Crying"},
            {"T_T", "Tears"},
            {"ToT", "Sobbing"},
            {";_;", "Crying"},
            {"QQ", "Crying eyes"},

            // Surprised
            {":O", "Surprised"},
            {"O_O", "Shocked"},
            {"O.O", "Wide eyes"},
            {"@_@", "Dizzy"},

            // Angry
            {">:(", "Angry"},
            {">_<", "Frustrated"},
            {"D:<", "Very angry"},
            {"-_-", "Annoyed"},

            // Cool
            {"B)", "Cool"},
            {"8)", "Cool glasses"},
            {"(•_•)", "Cool face"},
            {"( •_•)>⌐■-■", "Deal with it"},

            // Other
    };

    public EmojiPicker(JFrame parent, JTextField targetField) {
        super(parent, "Choose Emoticon", false);
        this.targetField = targetField;
        initComponents();
    }

    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("😊 Choose an Emoticon", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Emoticon panel
        JPanel emoticonPanel = new JPanel();
        emoticonPanel.setLayout(new GridLayout(0, 3, 10, 10));
        emoticonPanel.setBackground(Color.WHITE);
        emoticonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (String[] emoticon : EMOTICONS) {
            JButton emoticonButton = new JButton();
            emoticonButton.setLayout(new BorderLayout());

            // Emoticon text (larger)
            JLabel emoticonLabel = new JLabel(emoticon[0], SwingConstants.CENTER);
            emoticonLabel.setFont(new Font("Arial", Font.BOLD, 20));

            // Description (smaller)
            JLabel descLabel = new JLabel(emoticon[1], SwingConstants.CENTER);
            descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            descLabel.setForeground(Color.GRAY);

            JPanel buttonContent = new JPanel();
            buttonContent.setLayout(new BoxLayout(buttonContent, BoxLayout.Y_AXIS));
            buttonContent.setOpaque(false);
            buttonContent.add(emoticonLabel);
            buttonContent.add(descLabel);

            emoticonButton.add(buttonContent, BorderLayout.CENTER);
            emoticonButton.setFocusPainted(false);
            emoticonButton.setBackground(Color.WHITE);
            emoticonButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            emoticonButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            emoticonButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    emoticonButton.setBackground(new Color(230, 240, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    emoticonButton.setBackground(Color.WHITE);
                }
            });

            final String emoticonText = emoticon[0];
            emoticonButton.addActionListener(e -> {
                targetField.setText(targetField.getText() + " " + emoticonText + " ");
                targetField.requestFocus();
                dispose();
            });

            emoticonPanel.add(emoticonButton);
        }

        JScrollPane scrollPane = new JScrollPane(emoticonPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }
}
