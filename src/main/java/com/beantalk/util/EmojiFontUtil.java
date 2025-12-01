package com.beantalk.util;

import java.awt.*;

public class EmojiFontUtil {
    private static Font emojiFont;

    static {
        loadEmojiFont();
    }

    private static void loadEmojiFont() {
        String[] emojiFonts = {
                "Segoe UI Emoji",      // Windows 8+
                "Apple Color Emoji",   // macOS
                "Noto Color Emoji",    // Linux
                "Segoe UI Symbol",     // Windows 7
                "Symbola",             // Universal fallback
                "DejaVu Sans"          // Linux fallback
        };

        emojiFont = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();

        for (String fontName : emojiFonts) {
            for (String available : availableFonts) {
                if (available.equalsIgnoreCase(fontName)) {
                    emojiFont = new Font(fontName, Font.PLAIN, 14);
                    System.out.println("✅ Loaded emoji font: " + fontName);
                    return;
                }
            }
        }
        System.out.println("⚠️ No emoji font found, using default font");
        emojiFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }

    public static Font getEmojiFont(int size) {
        return emojiFont.deriveFont((float) size);
    }

    public static Font getEmojiFont(int style, int size) {
        return emojiFont.deriveFont(style, size);
    }

    // test
//    public static void main(String[] args) {
//        System.out.println("Testing emoji support...\n");
//
//        Font font = getEmojiFont(16);
//        System.out.println("Current emoji font: " + font.getFontName());
//
//        // Test các emoji thường dùng
//        String[] testEmojis = {"👥", "😊", "💬", "📩", "✅", "❌", "🔐", "📝", "👋", "💾"};
//
//        System.out.println("\nEmoji support test:");
//        for (String emoji : testEmojis) {
//            boolean canDisplay = font.canDisplayUpTo(emoji) == -1;
//            System.out.println(emoji + " - " + (canDisplay ? "✓ Supported" : "✗ Not supported"));
//        }
//    }
}
