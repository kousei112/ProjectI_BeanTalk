package com.beantalk.util;

import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

/**
 * Security utilities: BCrypt for passwords, AES for messages
 */
public class SecurityUtil {
    private static String AES_SECRET_KEY;

    // Load AES key from config
    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = SecurityUtil.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("ERROR: Cannot find config.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);
            AES_SECRET_KEY = prop.getProperty("aes.secret.key");

        } catch (IOException e) {
            System.err.println("ERROR: Cannot read AES key - " + e.getMessage());
        }
    }

    // ============ BCRYPT - Password Hashing ============

    /**
     * Hash password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    // ============ AES-256 - Message Encryption ============

    /**
     * Encrypt message using AES
     */
    public static String encryptMessage(String plainText) {
        try {
            // Create key from secret (take first 32 bytes for AES-256)
            byte[] keyBytes = AES_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            System.err.println("ERROR: Encryption failed - " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypt message using AES
     */
    public static String decryptMessage(String encryptedText) {
        try {
            byte[] keyBytes = AES_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.err.println("ERROR: Decryption failed - " + e.getMessage());
            return null;
        }
    }

    // ============ TEST ============

    public static void main(String[] args) {
        System.out.println("=== SECURITY UTILITIES TEST ===\n");

        // Test 1: BCrypt Password Hashing
        System.out.println("1. BCrypt Password Hashing:");
        System.out.println("---------------------------");
        String password = "MyPassword123";
        String hashed = hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verify (correct): " + verifyPassword(password, hashed));
        System.out.println("Verify (wrong): " + verifyPassword("WrongPassword", hashed));

        // Test 2: AES Message Encryption
        System.out.println("\n2. AES Message Encryption:");
        System.out.println("---------------------------");
        String message = "Hello, this is a secret message!";
        String encrypted = encryptMessage(message);
        String decrypted = decryptMessage(encrypted);
        System.out.println("Original: " + message);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
        System.out.println("Match: " + message.equals(decrypted));

        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
}