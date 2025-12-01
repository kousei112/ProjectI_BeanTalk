package com.beantalk.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Notification Popup Utility với callback support
 */
public class NotificationUtil {

    /**
     * Hiển thị notification popup (version cũ - tương thích ngược)
     */
    public static void showNotification(String title, String message, JFrame parentFrame) {
        showNotificationWithCallback(title, message, parentFrame, null);
    }

    /**
     * Hiển thị notification popup với callback khi click
     */
    public static void showNotificationWithCallback(String title, String message,
                                                    JFrame parentFrame, Runnable onClickCallback) {
        SwingUtilities.invokeLater(() -> {
            JWindow notification = new JWindow();
            notification.setAlwaysOnTop(true);

            // Panel chính
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            panel.setBackground(new Color(50, 50, 50));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            // Icon
            JLabel iconLabel = new JLabel("💬");
            iconLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 32));
            panel.add(iconLabel, BorderLayout.WEST);

            // Text panel
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(new Color(50, 50, 50));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(Color.WHITE);

            JLabel messageLabel = new JLabel("<html><body style='width: 200px'>" + message + "</body></html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            messageLabel.setForeground(new Color(220, 220, 220));

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(messageLabel);

            panel.add(textPanel, BorderLayout.CENTER);

            // Close button
            JButton closeButton = new JButton("×");
            closeButton.setFont(new Font("Arial", Font.BOLD, 20));
            closeButton.setForeground(Color.WHITE);
            closeButton.setBackground(new Color(50, 50, 50));
            closeButton.setBorder(null);
            closeButton.setFocusPainted(false);
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeButton.addActionListener(e -> notification.dispose());

            JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            closePanel.setBackground(new Color(50, 50, 50));
            closePanel.add(closeButton);
            panel.add(closePanel, BorderLayout.NORTH);

            notification.add(panel);
            notification.pack();

            // Vị trí: Góc dưới bên phải màn hình
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - notification.getWidth() - 20;
            int y = screenSize.height - notification.getHeight() - 50;
            notification.setLocation(x, y);

            // Click để thực hiện callback và focus vào parent frame
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    notification.dispose();

                    // Thực hiện callback nếu có
                    if (onClickCallback != null) {
                        onClickCallback.run();
                    } else {
                        // Behavior mặc định: chỉ focus vào parent frame
                        if (parentFrame != null) {
                            parentFrame.setVisible(true);
                            parentFrame.toFront();
                            parentFrame.requestFocus();
                        }
                    }
                }
            });

            // Hover effect
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    panel.setBackground(new Color(60, 60, 60));
                    textPanel.setBackground(new Color(60, 60, 60));
                    closePanel.setBackground(new Color(60, 60, 60));
                    closeButton.setBackground(new Color(60, 60, 60));
                    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    panel.setBackground(new Color(50, 50, 50));
                    textPanel.setBackground(new Color(50, 50, 50));
                    closePanel.setBackground(new Color(50, 50, 50));
                    closeButton.setBackground(new Color(50, 50, 50));
                    panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });

            notification.setVisible(true);

            // Auto hide sau 5 giây
            Timer timer = new Timer(5000, e -> {
                notification.dispose();
            });
            timer.setRepeats(false);
            timer.start();

            // Animation: Fade in
            Timer fadeIn = new Timer(10, new ActionListener() {
                float opacity = 0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 1f) {
                        opacity = 1f;
                        ((Timer) e.getSource()).stop();
                    }
                    notification.setOpacity(opacity);
                }
            });
            fadeIn.start();
        });
    }

    /**
     * Test notification
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Test");
            testFrame.setSize(400, 300);
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setLocationRelativeTo(null);

            JButton testButton1 = new JButton("Show Notification (No Callback)");
            testButton1.addActionListener(e -> {
                showNotification("Alice", "Hello! This is a test message.", testFrame);
            });

            JButton testButton2 = new JButton("Show Notification (With Callback)");
            testButton2.addActionListener(e -> {
                showNotificationWithCallback(
                        "Bob",
                        "Click me to see the callback!",
                        testFrame,
                        () -> {
                            JOptionPane.showMessageDialog(testFrame,
                                    "Callback executed! You clicked the notification.",
                                    "Callback",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                );
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            buttonPanel.add(testButton1);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(testButton2);

            testFrame.add(buttonPanel);
            testFrame.setVisible(true);
        });
    }
}