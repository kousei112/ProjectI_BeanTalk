package com.beantalk.util;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class FileTransferUtil {
    // thu muc luu file tren server
    private static final String UPLOAD_DIR = "uploads/";

    // max file size 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    static {
        // tao thu muc uploads neu chua co
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            System.out.println("Upload directory ready: " + UPLOAD_DIR);
        } catch (IOException e) {
            System.err.println("Error creating upload directory: " + e.getMessage());
        }
    }

    // convert file thanh Base64 string
    public static String fileToBase64(File file) throws IOException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File too large. Maximum size is 10MB.");
        }
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    // convert base64 string thanh file
    public static String base64ToFile(String base64Data, String originalFileName) throws IOException {
        byte[] fileContent = Base64.getDecoder().decode(base64Data);

        // tao ten file unique
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String filePath = UPLOAD_DIR + uniqueFileName;

        // Ghi file
        Files.write(Paths.get(filePath), fileContent);

        return filePath;
    }

    // lay extension cua file
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }

    // kiem tra xem co phai file anh khong
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
               extension.equals(".png") || extension.equals(".gif") ||
               extension.equals(".bmp");
    }

    // lay ten file tu path
    public static String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    // kiem tra xem file co ton tai khong
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // doc file thanh byte array
    public static byte[] readFileBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // format file size
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // validate file
    public static void validateFile(File file) throws IOException{
        if (!file.exists()) {
            throw new IOException("File does not exist");
        }
        if (!file.isFile()) {
            throw new IOException("Not a valid file");
        }

        if (file.length() == 0) {
            throw new IOException("File is empty");
        }

        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File too large. Maximum size is " +
                    formatFileSize(MAX_FILE_SIZE));
        }
    }

    // test
//    public static void main(String[] args) {
//        System.out.println("FileTransferUtil Test");
//        System.out.println("Upload directory: " + UPLOAD_DIR);
//        System.out.println("Max file size: " + formatFileSize(MAX_FILE_SIZE));
//
//        // Test file extension
//        System.out.println("\nTest file extensions:");
//        System.out.println("test.jpg -> " + getFileExtension("test.jpg"));
//        System.out.println("document.pdf -> " + getFileExtension("document.pdf"));
//
//        // Test image detection
//        System.out.println("\nTest image detection:");
//        System.out.println("photo.jpg is image? " + isImageFile("photo.jpg"));
//        System.out.println("document.pdf is image? " + isImageFile("document.pdf"));
//    }
}
