package org.lamisplus.modules.lims.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class BarcodeGenerator {
    private static final Map<Character, String> CODE128_CHARSET = new HashMap<>();
    private static final String[] CODE128_PATTERNS = {
            "11011001100", "11001101100", "11001100110", "10010011000", "10010001100",
            "10001001100", "10011001000", "10011000100", "10001100100", "11001001000",
            "11001000100", "11000100100", "10110011100", "10011011100", "10011001110",
            "10111001100", "10011101100", "10011100110", "11001110010", "11001011100",
            "11001001110", "11011100100", "11001110100", "11101101110", "11101001100",
            "11100101100", "11100100110", "11101100100", "11100110100", "11100110010",
            "11011011000", "11011000110", "11000110110", "10100011000", "10001011000",
            "10001000110", "10110001000", "10001101000", "10001100010", "11010001000",
            "11000101000", "11000100010", "10110111000", "10110001110", "10001101110",
            "10111011000", "10111000110", "10001110110", "11101110110", "11010001110",
            "11000101110", "11011101000", "11011100010", "11011101110", "11101011000",
            "11101000110", "11100010110", "11101101000", "11101100010", "11100011010",
            "11101111010", "11001000010", "11110001010", "10100110000", "10100001100",
            "10010110000", "10010000110", "10000101100", "10000100110", "10110010000",
            "10110000100", "10011010000", "10011000010", "10000110100", "10000110010",
            "11000010010", "11001010000", "11110111010", "11000010100", "10001111010",
            "10100111100", "10010111100", "10010011110", "10111100100", "10011110100",
            "10011110010", "11110100100", "11110010100", "11110010010", "11011011110",
            "11011110110", "11110110110", "10101111000", "10100011110", "10001011110",
            "10111101000", "10111100010", "11110101000", "11110100010", "10111011110",
            "10111101110", "11101011110", "11110101110", "11010000100", "11010010000",
            "11010011100", "11000111010"
    };

    static {
        for (int i = 0; i < 95; i++) {
            CODE128_CHARSET.put((char) (32 + i), CODE128_PATTERNS[i]);
        }
    }

    private static final String START_CODE_B = "11010010000";
    private static final String STOP_PATTERN = "11000111010";

    public static String generateBarcode(String data, int width, int height) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Barcode data cannot be null or empty");
        }

        if (width < 100 || height < 50) {
            throw new IllegalArgumentException("Minimum barcode size is 100x50 pixels");
        }

        if (data.length() > 50) {
            throw new IllegalArgumentException("Barcode data too long (max 50 characters)");
        }

        try {
            String barcodePattern = encodeToCode128(data);
            BufferedImage image = createBarcodeImage(barcodePattern, data, width, height);
            return convertToBase64(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    private static String encodeToCode128(String data) {
        StringBuilder pattern = new StringBuilder();
        pattern.append(START_CODE_B);

        int checksum = 104;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            String charPattern = CODE128_CHARSET.get(c);
            if (charPattern == null) {
                throw new IllegalArgumentException("Unsupported character in barcode data: " + c);
            }
            pattern.append(charPattern);
            checksum += (i + 1) * (c - 32);
        }

        checksum = checksum % 103;
        pattern.append(CODE128_PATTERNS[checksum]);
        pattern.append(STOP_PATTERN);

        return pattern.toString();
    }

    private static BufferedImage createBarcodeImage(String barcodePattern, String data,
                                                    int width, int height) {
        int quietZone = 10;
        int moduleWidth = Math.max(1, (width - 2 * quietZone) / barcodePattern.length());
        int barcodeWidth = moduleWidth * barcodePattern.length();
        int actualWidth = barcodeWidth + 2 * quietZone;

        BufferedImage image = new BufferedImage(actualWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, actualWidth, height);

        // Draw barcode pattern
        g2d.setColor(Color.BLACK);
        int x = quietZone;

        for (int i = 0; i < barcodePattern.length(); i++) {
            if (barcodePattern.charAt(i) == '1') {
                g2d.fillRect(x, quietZone, moduleWidth, height - 2 * quietZone - 20);
            }
            x += moduleWidth;
        }

        // Draw human-readable text
        drawBarcodeText(g2d, data, actualWidth, height);

        g2d.dispose();

        // Scale to requested dimensions if different
        if (actualWidth != width) {
            return resizeImage(image, width, height);
        }

        return image;
    }

    private static void drawBarcodeText(Graphics2D g2d, String data, int width, int height) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(data);
        int textHeight = metrics.getHeight();

        // Center the text horizontally, position at bottom with padding
        int textX = (width - textWidth) / 2;
        int textY = height - 5;

        // Optional: Add a white background for the text
        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX - 2, textY - textHeight + 3, textWidth + 4, textHeight - 2);

        g2d.setColor(Color.BLACK);
        g2d.drawString(data, textX, textY);
    }

    private static BufferedImage resizeImage(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    private static String convertToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert image to base64", e);
        }
    }

    // Utility method for bulk generation
    public static Map<String, String> generateBarcodes(Map<String, String> dataMap,
                                                       int width, int height) {
        Map<String, String> results = new HashMap<>();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            try {
                String barcode = generateBarcode(entry.getValue(), width, height);
                results.put(entry.getKey(), barcode);
            } catch (Exception e) {
                results.put(entry.getKey(), "ERROR: " + e.getMessage());
            }
        }
        return results;
    }

    // Validation method
    public static boolean isValidBarcodeData(String data) {
        if (data == null || data.trim().isEmpty() || data.length() > 50) {
            return false;
        }

        for (char c : data.toCharArray()) {
            if (c < 32 || c > 126) {
                return false;
            }
        }

        return true;
    }
}

