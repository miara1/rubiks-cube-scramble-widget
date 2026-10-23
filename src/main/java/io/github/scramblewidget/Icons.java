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
        return new Icon() {
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
            @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.scale(2.0 / 3, 2.0 / 3);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(c.isEnabled() ? new Color(48, 53, 60) : new Color(165, 169, 175));
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 300, Arc2D.OPEN));
                Path2D arrow = new Path2D.Double();
                arrow.moveTo(20.5, 4.5);
                arrow.lineTo(20.0, 10.0);
                arrow.lineTo(14.5, 9.0);
                g.draw(arrow);
                g.dispose();
            }
        };
    }
}
