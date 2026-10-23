package io.github.scramblewidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

final class WidgetWindow extends JWindow {
    private static final int WIDTH = 440;
    private static final int HEIGHT = 28;
    private static final int EDGE = 8;
    private static final Font FONT = new Font("Consolas", Font.PLAIN, 12);
    private final JLabel text = new JLabel("Generating…");
    private final JButton refresh = new JButton(Icons.refresh()) {
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover() && isEnabled()) {
                g.setColor(new Color(240, 242, 245));
                g.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    };

    WidgetWindow(Settings settings, Runnable next) {
        setName("RubiksCubeScrambleWidget");
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setAutoRequestFocus(false);
        setType(Type.UTILITY);

        JPanel card = new JPanel(new BorderLayout(6, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 229)),
                BorderFactory.createEmptyBorder(3, 8, 3, 4)));

        text.setFont(FONT);
        text.setForeground(new Color(32, 36, 42));
        text.getAccessibleContext().setAccessibleName("Current 3 by 3 scramble");
        text.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) { fitText(); }
        });
        card.add(text, BorderLayout.CENTER);

        refresh.setPreferredSize(new Dimension(20, 20));
        refresh.setContentAreaFilled(false);
        refresh.setBorder(BorderFactory.createEmptyBorder());
        refresh.setFocusPainted(false);
        refresh.setFocusable(false);
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.setToolTipText("New scramble (" + settings.next().label() + ")");
        refresh.getAccessibleContext().setAccessibleName("New scramble");
        refresh.setEnabled(false);
        refresh.addActionListener(event -> next.run());
        card.add(refresh, BorderLayout.EAST);
        setContentPane(card);
        setSize(WIDTH, HEIGHT);
    }

    private void fitText() {
        int available = text.getWidth() > 0 ? text.getWidth() : WIDTH - 40;
        Font font = FONT;
        while (font.getSize2D() > 6 && text.getFontMetrics(font).stringWidth(text.getText()) > available) {
            font = font.deriveFont(font.getSize2D() - 0.5f);
        }
        text.setFont(font);
    }

    void showAtCorner() {
        GraphicsConfiguration screen = screenAtPointer();
        Rectangle bounds = screen.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen);
        Rectangle work = new Rectangle(bounds.x + insets.left, bounds.y + insets.top,
                bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
        setSize(Math.min(WIDTH, work.width - 2 * EDGE), HEIGHT);
        setLocation(work.x + work.width - getWidth() - EDGE, work.y + work.height - getHeight() - EDGE);
        setVisible(true);
    }

    private static GraphicsConfiguration screenAtPointer() {
        Point pointer = MouseInfo.getPointerInfo().getLocation();
        for (var device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            if (config.getBounds().contains(pointer)) return config;
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    void toggle() {
        if (isVisible()) setVisible(false); else showAtCorner();
    }

    void setBusy(boolean busy) {
        refresh.setEnabled(!busy);
    }

    void showScramble(String scramble) {
        text.setText(scramble);
        fitText();
    }
}
