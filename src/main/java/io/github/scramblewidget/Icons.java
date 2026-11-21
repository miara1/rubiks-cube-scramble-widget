package io.github.scramblewidget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.Icon;

final class Icons {
    static BufferedImage cube(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.scale(size / 32.0, size / 32.0);
        g.setColor(new Color(45, 49, 55));
        g.fillRoundRect(1, 1, 30, 30, 5, 5);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g.setColor(row == 1 && col == 1 ? new Color(246, 191, 70) : Color.WHITE);
                g.fillRoundRect(4 + col * 9, 4 + row * 9, 7, 7, 1, 1);
            }
        }
        g.dispose();
        return image;
    }

    static Icon refresh() {
        return lineIcon(16, g -> {
            g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 300, Arc2D.OPEN));
            drawPath(g, 20.5, 4.5, 20, 10, 14.5, 9);
        });
    }

    static Icon bookmark() {
        return lineIcon(16, g -> drawPath(g, 6, 21, 6, 4, 18, 4, 18, 21, 12, 16, 6, 21));
    }

    static Icon menuArrow() {
        return lineIcon(10, g -> {
            g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawPath(g, 5, 15, 12, 8, 19, 15);
        });
    }

    private static void drawPath(Graphics2D g, double x, double y, double... points) {
        Path2D path = new Path2D.Double();
        path.moveTo(x, y);
        for (int i = 0; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        g.draw(path);
    }

    private static Icon lineIcon(int size, Consumer<Graphics2D> draw) {
        return new Icon() {
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
            @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.scale(size / 24.0, size / 24.0);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(c.isEnabled() ? new Color(48, 53, 60) : new Color(165, 169, 175));
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                draw.accept(g);
                g.dispose();
            }
        };
    }
}
