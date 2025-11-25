package com.beantalk.ui;

import com.beantalk.client.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login/Register Frame
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private ChatClient client;
    private boolean loginSuccess = false;

    public LoginFrame() {
        initComponents();
        connectToServer();
    }

    private void initComponents() {
        setTitle("BeanTalk - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));

        // Title
        JLabel titleLabel = new JLabel("🌟 BEANTALK CHAT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 210));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);

        // Email (hidden initially)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setVisible(false);
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setVisible(false);
        formPanel.add(emailField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(25, 118, 210));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Status label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 248, 255));
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Enter key support
        passwordField.addActionListener(e -> handleLogin());

        add(mainPanel);
    }

    private void connectToServer() {
        client = new ChatClient();
        client.setLoginCallback(this::onLoginResult);
        client.setRegisterCallback(this::onRegisterResult);

        try {
            client.connect();
            statusLabel.setText("Connected to server");
            statusLabel.setForeground(new Color(76, 175, 80));
        } catch (Exception e) {
            statusLabel.setText("Cannot connect to server!");
            statusLabel.setForeground(Color.RED);
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            statusLabel.setForeground(Color.RED);
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLUE);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        client.setUsername(username);
        client.login(username, password);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in username and password");
            statusLabel.setForeground(Color.RED);
            return;
        }

        statusLabel.setText("Registering...");
        statusLabel.setForeground(Color.BLUE);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        client.register(username, password, email.isEmpty() ? username + "@beantalk.com" : email);

        // Auto login after register
        Timer timer = new Timer(1500, e -> {
            client.setUsername(username);
            client.login(username, password);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void onLoginResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                statusLabel.setText("Login successful!");
                statusLabel.setForeground(new Color(76, 175, 80));

                // Open main chat frame
                Timer timer = new Timer(500, e -> {
                    new MainChatFrame(client).setVisible(true);
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                statusLabel.setText(message);
                statusLabel.setForeground(Color.RED);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }
        });
    }

    private void onRegisterResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                statusLabel.setText("Registration successful! Logging in...");
                statusLabel.setForeground(new Color(76, 175, 80));
            } else {
                statusLabel.setText(message);
                statusLabel.setForeground(Color.RED);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}