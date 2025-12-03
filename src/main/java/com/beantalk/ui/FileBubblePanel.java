package com.beantalk.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Panel hiển thị file/image trong chat
 */
public class FileBubblePanel extends JPanel {
    private String sender;
    private String fileName;
    private String filePath;
    private String messageType;
    private String time;
    private boolean isOwnMessage;

    public FileBubblePanel(String sender, String fileName, String filePath,
                           String messageType, String time, boolean isOwnMessage) {
        this.sender = sender;
        this.fileName = fileName;
        this.filePath = filePath;
        this.messageType = messageType;
        this.time = time;
        this.isOwnMessage = isOwnMessage;

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main bubble panel
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1)
        ));

        if (isOwnMessage) {
            bubblePanel.setBackground(new Color(220, 248, 198, 230));
        } else {
            bubblePanel.setBackground(new Color(255, 255, 255, 230));
        }

        // Sender name (nếu không phải tin nhắn của mình)
        if (!isOwnMessage) {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            senderLabel.setForeground(new Color(25, 118, 210));
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(senderLabel);
            bubblePanel.add(Box.createVerticalStrut(5));
        }

        // File content
        if (messageType.equals("IMAGE")) {
            addImagePreview(bubblePanel);
        } else {
            addFileIcon(bubblePanel);
        }

        bubblePanel.add(Box.createVerticalStrut(5));

        // Time
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(isOwnMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubblePanel.add(timeLabel);

        // Wrapper panel
        JPanel wrapperPanel = new JPanel(new FlowLayout(
                isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(bubblePanel);

        add(wrapperPanel, BorderLayout.CENTER);
    }

    /**
     * Thêm image preview
     */
    private void addImagePreview(JPanel parent) {
        try {
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                BufferedImage originalImage = ImageIO.read(imageFile);

                // Scale image để vừa khung chat
                int maxWidth = 300;
                int maxHeight = 300;

                int width = originalImage.getWidth();
                int height = originalImage.getHeight();

                if (width > maxWidth || height > maxHeight) {
                    double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                    width = (int) (width * scale);
                    height = (int) (height * scale);
                }

                Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Click để xem ảnh full size
                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showFullImage(originalImage);
                    }
                });

                parent.add(imageLabel);

                // File name
                JLabel fileNameLabel = new JLabel(fileName);
                fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                fileNameLabel.setForeground(Color.DARK_GRAY);
                fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                parent.add(fileNameLabel);

            } else {
                addFileNotFound(parent);
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            addFileNotFound(parent);
        }
    }

    /**
     * Thêm file icon (cho file không phải ảnh)
     */
    private void addFileIcon(JPanel parent) {
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filePanel.setOpaque(false);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon
        JLabel iconLabel = new JLabel("📎");
        iconLabel.setFont(com.beantalk.util.EmojiFontUtil.getEmojiFont(Font.PLAIN, 32));
        filePanel.add(iconLabel);

        // File info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(fileName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoPanel.add(nameLabel);

        // File size
        try {
            File file = new File(filePath);
            if (file.exists()) {
                String sizeStr = com.beantalk.util.FileTransferUtil.formatFileSize(file.length());
                JLabel sizeLabel = new JLabel(sizeStr);
                sizeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                sizeLabel.setForeground(Color.GRAY);
                infoPanel.add(sizeLabel);
            }
        } catch (Exception e) {
            // Ignore
        }

        filePanel.add(infoPanel);

        // Download button
        JButton downloadBtn = new JButton("Download");
        downloadBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        downloadBtn.setFocusPainted(false);
        downloadBtn.addActionListener(e -> downloadFile());
        filePanel.add(downloadBtn);

        parent.add(filePanel);
    }

    /**
     * File not found message
     */
    private void addFileNotFound(JPanel parent) {
        JLabel errorLabel = new JLabel("⚠️ File not found: " + fileName);
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(errorLabel);
    }

    /**
     * Hiển thị ảnh full size
     */
    private void showFullImage(BufferedImage image) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                fileName, true);
        dialog.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel(new ImageIcon(image));
        JScrollPane scrollPane = new JScrollPane(imageLabel);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Download file
     */
    private void downloadFile() {
        try {
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                JOptionPane.showMessageDialog(this,
                        "File not found on server!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File destinationFile = fileChooser.getSelectedFile();

                // Copy file
                java.nio.file.Files.copy(
                        sourceFile.toPath(),
                        destinationFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                JOptionPane.showMessageDialog(this,
                        "File downloaded successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error downloading file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}