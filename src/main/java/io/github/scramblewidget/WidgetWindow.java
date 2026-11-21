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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

final class WidgetWindow extends JWindow {
    private static final int WIDTH = 440;
    private static final int HEIGHT = 28;
    private static final int EDGE = 8;
    private static final Font FONT = new Font("Consolas", Font.PLAIN, 12);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final Color OUTLINE = new Color(203, 213, 225);
    private static final Color INK = new Color(36, 50, 71);
    private static final Color HOVER = new Color(232, 240, 253);
    private final JLabel text = new JLabel("Generating…");
    private final JButton refresh;
    private final JButton save;
    private final JPopupMenu options = new JPopupMenu();
    private String currentScramble;
    private boolean dialogOpen;

    WidgetWindow(Settings settings, Runnable next, ScrambleLibrary library) {
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

        refresh = iconButton(Icons.refresh(), 20, "New scramble", next);
        refresh.setToolTipText("New scramble (" + settings.next().label() + ")");
        refresh.setEnabled(false);

        Runnable saveCurrent = () -> {
            if (currentScramble != null) showDialog(() -> ScrambleDialogs.save(this, library, currentScramble));
        };
        save = iconButton(Icons.bookmark(), 20, "Save scramble", saveCurrent);
        save.setEnabled(false);
        options.setUI(new BasicPopupMenuUI());
        options.setBackground(SURFACE);
        options.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(OUTLINE, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        JMenuItem nextItem = menuItem("New scramble", next);
        options.add(nextItem);
        JMenuItem saveItem = menuItem("Save scramble", saveCurrent);
        options.add(saveItem);
        options.add(menuItem("Scramble library", () -> showDialog(() -> ScrambleDialogs.open(this, library))));
        JButton arrow = iconButton(Icons.menuArrow(), 14, "Scramble options", null);
        arrow.addActionListener(event -> {
            if (options.isVisible()) {
                options.setVisible(false);
                return;
            }
            nextItem.setEnabled(refresh.isEnabled());
            saveItem.setEnabled(currentScramble != null);
            Dimension size = options.getPreferredSize();
            options.show(arrow, arrow.getWidth() - size.width, -size.height - 8);
        });

        JPanel actions = new JPanel(new BorderLayout(2, 0));
        actions.setOpaque(false);
        actions.add(refresh, BorderLayout.WEST);
        actions.add(save, BorderLayout.CENTER);
        actions.add(arrow, BorderLayout.EAST);
        card.add(actions, BorderLayout.EAST);
        setContentPane(card);
        setSize(WIDTH, HEIGHT);
    }

    private static JMenuItem menuItem(String label, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        item.setUI(new BasicMenuItemUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                selectionForeground = INK;
            }

            @Override public Dimension getPreferredSize(JComponent component) {
                Insets padding = item.getInsets();
                var metrics = item.getFontMetrics(item.getFont());
                return new Dimension(metrics.stringWidth(label) + padding.left + padding.right,
                        metrics.getHeight() + padding.top + padding.bottom);
            }

            @Override protected void paintMenuItem(Graphics graphics, JComponent component,
                    Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int gap) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setFont(item.getFont());
                    var metrics = g.getFontMetrics();
                    // Plain text rows don't need the Windows menu's icon and shortcut columns.
                    if (item.getModel().isArmed()) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(HOVER);
                        g.fillRoundRect(0, 0, item.getWidth(), item.getHeight(), 8, 8);
                    }
                    paintText(g, item, new Rectangle(item.getInsets().left,
                            (item.getHeight() - metrics.getHeight()) / 2,
                            metrics.stringWidth(label), metrics.getHeight()), label);
                } finally {
                    g.dispose();
                }
            }
        });
        item.setBackground(SURFACE);
        item.setForeground(INK);
        item.setFont(item.getFont().deriveFont(Font.PLAIN));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        item.addActionListener(event -> action.run());
        return item;
    }

    private static JButton iconButton(Icon icon, int width, String name, Runnable action) {
        JButton button = new JButton(icon) {
            @Override protected void paintComponent(Graphics graphics) {
                if (getModel().isRollover() && isEnabled()) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(240, 242, 245));
                    g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                    g.dispose();
                }
                super.paintComponent(graphics);
            }
        };
        button.setPreferredSize(new Dimension(width, 20));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(name);
        button.getAccessibleContext().setAccessibleName(name);
        if (action != null) button.addActionListener(event -> action.run());
        return button;
    }

    private void fitText() {
        int available = text.getWidth() > 0 ? text.getWidth() : WIDTH - 80;
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
        if (hasOpenDialog()) return;
        if (isVisible()) {
            options.setVisible(false);
            setVisible(false);
        } else showAtCorner();
    }

    boolean hasOpenDialog() {
        return dialogOpen;
    }

    private void showDialog(Runnable action) {
        if (dialogOpen) return;
        dialogOpen = true;
        try {
            action.run();
        } finally {
            dialogOpen = false;
        }
    }

    void setBusy(boolean busy) {
        refresh.setEnabled(!busy);
    }

    void showScramble(String scramble) {
        currentScramble = scramble;
        save.setEnabled(true);
        text.setText(scramble);
        fitText();
    }
}
