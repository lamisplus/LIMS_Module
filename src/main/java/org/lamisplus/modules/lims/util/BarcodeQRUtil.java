package org.lamisplus.modules.lims.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
public class BarcodeQRUtil {
    public static String generateBarcode(String data, int width, int height) throws Exception {
        return generateTextBarcode(data, width, height);
    }

    private static String generateTextBarcode(String data, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(data)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();

        g2d.drawString(data, x, y);

        g2d.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < data.length(); i++) {
            int barHeight = 20;
            int barWidth = 4;
            int barX = 10 + (i * 8);
            g2d.drawLine(barX, 10, barX, 10 + barHeight);
        }

        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}
