package org.lamisplus.modules.lims.util;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
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


    public static String generateLabeledBarcode(String barcodeData, String sampleId, String serialNumber,
                                                int width, int height) {
        if (barcodeData == null || barcodeData.trim().isEmpty()) {
            throw new IllegalArgumentException("Barcode data cannot be null or empty");
        }

        try {
            String barcodePattern = encodeToCode128(barcodeData);
            return createLabeledBarcodeImage(barcodePattern, barcodeData, sampleId, serialNumber, width, height);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate labeled barcode: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a barcode image with sample ID above and serial number below
     */
    private static String createLabeledBarcodeImage(String barcodePattern, String barcodeData,
                                                    String sampleId, String serialNumber,
                                                    int width, int height) {
        int quietZone = 15;
        int moduleWidth = Math.max(1, (width - 2 * quietZone) / barcodePattern.length());
        int barcodeWidth = moduleWidth * barcodePattern.length();
        int actualWidth = barcodeWidth + 2 * quietZone;

        BufferedImage image = new BufferedImage(actualWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Setup rendering hints for better quality
        setupGraphicsQuality(g2d);

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, actualWidth, height);

        // Calculate text areas
        int textAreaHeight = 35;
        int barcodeAreaHeight = height - (2 * textAreaHeight);
        int barcodeY = textAreaHeight;

        // Draw sample ID (top label)
        drawTopLabel(g2d, sampleId, actualWidth, textAreaHeight);

        // Draw barcode pattern
        drawBarcodePattern(g2d, barcodePattern, moduleWidth, quietZone, barcodeY, barcodeAreaHeight);

        // Draw serial number (bottom label)
        drawBottomLabel(g2d, serialNumber, actualWidth, height, textAreaHeight);

        g2d.dispose();

        // Scale to requested dimensions if different
        if (actualWidth != width) {
            image = resizeImage(image, width, height);
        }

        return convertToBase64(image);
    }

    /**
     * Draws the sample ID above the barcode
     */
    private static void drawTopLabel(Graphics2D g2d, String sampleId, int width, int textAreaHeight) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(sampleId);
        int textX = (width - textWidth) / 2;
        int textY = textAreaHeight - 5; // Position above barcode

        // Optional: Add background for better readability
        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX - 3, textY - metrics.getAscent() + 3, textWidth + 6, metrics.getHeight());

        g2d.setColor(Color.BLACK);
        g2d.drawString(sampleId, textX, textY);

        // Add a small label identifier
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("", textX - 45, textY);
    }

    /**
     * Draws the barcode pattern
     */
    private static void drawBarcodePattern(Graphics2D g2d, String barcodePattern, int moduleWidth,
                                           int quietZone, int startY, int height) {
        g2d.setColor(Color.BLACK);
        int x = quietZone;

        for (int i = 0; i < barcodePattern.length(); i++) {
            if (barcodePattern.charAt(i) == '1') {
                g2d.fillRect(x, startY, moduleWidth, height);
            }
            x += moduleWidth;
        }
    }

    /**
     * Draws the serial number below the barcode
     */
    private static void drawBottomLabel(Graphics2D g2d, String serialNumber, int width, int totalHeight, int textAreaHeight) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(serialNumber);
        int textX = (width - textWidth) / 2;
        int textY = totalHeight - 10; // Position at bottom

        // Optional: Add background for better readability
        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX - 3, textY - metrics.getAscent() + 3, textWidth + 6, metrics.getHeight());

        g2d.setColor(Color.BLACK);
        g2d.drawString(serialNumber, textX, textY);

        // Add a small label identifier
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("", textX - 30, textY);
    }

    /**
     * Sets up graphics quality settings
     */
    private static void setupGraphicsQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Resizes image while maintaining quality
     */
    private static BufferedImage resizeImage(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        setupGraphicsQuality(g2d);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return resized;
    }

    /**
     * Converts image to base64 data URL
     */
    private static String convertToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert image to base64", e);
        }
    }

}

