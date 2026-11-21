package io.github.scramblewidget;

import java.awt.BorderLayout;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

final class ScrambleDialogs {
    private static final Font MOVES = new Font("Consolas", Font.PLAIN, 12);
    private static final Color SURFACE = new Color(0xf8fafc);
    private static final Color TEXT = new Color(0x243247);
    private static final Color MUTED = new Color(0x53647b);
    private static final Color BORDER = new Color(0xcbd5e1);
    private static final Color ACCENT = new Color(0x285abe);
    private static final Color TINT = new Color(0xe8f0fd);
    private static final Color DELETE = new Color(0xa3303d);

    private ScrambleDialogs() { }

    static void save(Window owner, ScrambleLibrary library, String scramble) {
        JDialog dialog = dialog(owner, "Save scramble");
        JTextArea moves = textArea(2, 44);
        moves.setFont(MOVES);
        moves.setEditable(false);
        moves.setFocusable(false);
        moves.setBackground(TINT);
        moves.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        moves.setText(scramble);
        JTextArea comment = textArea(3, 40);
        JPanel fields = new JPanel(new BorderLayout(0, 8));
        fields.setOpaque(false);
        JLabel label = label("Comment (optional)");
        label.setLabelFor(comment);
        comment.getAccessibleContext().setAccessibleName("Comment (optional)");
        fields.add(moves, BorderLayout.NORTH);
        JPanel editor = new JPanel(new BorderLayout(0, 5));
        editor.setOpaque(false);
        editor.add(label, BorderLayout.NORTH);
        editor.add(scroll(comment), BorderLayout.CENTER);
        fields.add(editor, BorderLayout.CENTER);

        JButton save = button("Save", ACCENT, true);
        save.addActionListener(event -> {
            try {
                library.save(scramble, comment.getText());
                dialog.dispose();
            } catch (IOException error) {
                showError(dialog, "Could not save the scramble.", error);
            }
        });
        dialog.add(fields, BorderLayout.CENTER);
        dialog.add(buttons(closer(dialog, "Cancel"), save), BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(save);
        SwingUtilities.invokeLater(comment::requestFocusInWindow);
        show(dialog, owner);
    }

    static void open(Window owner, ScrambleLibrary library) {
        List<ScrambleLibrary.Entry> entries;
        try {
            entries = new ArrayList<>(library.load());
        } catch (IOException error) {
            showError(null, "Could not open the scramble library.", error);
            return;
        }

        JDialog dialog = dialog(owner, "Scramble library");
        JButton close = closer(dialog, "Close");
        if (entries.isEmpty()) {
            dialog.add(label("No saved scrambles yet."), BorderLayout.CENTER);
            dialog.add(buttons(close), BorderLayout.SOUTH);
            dialog.getRootPane().setDefaultButton(close);
        } else {
            addEntries(dialog, library, entries, close);
        }
        show(dialog, owner);
    }

    private static void addEntries(JDialog dialog, ScrambleLibrary library, List<ScrambleLibrary.Entry> entries, JButton close) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (var entry : entries) model.addElement(entry.scramble());
        JList<String> list = new JList<>(model);
        list.setFont(MOVES);
        list.setForeground(TEXT);
        list.setSelectionForeground(TEXT);
        list.setSelectionBackground(TINT);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> view, Object value,
                    int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(view, value, index, selected, focused);
                setBorder(BorderFactory.createCompoundBorder(getBorder(), BorderFactory.createEmptyBorder(5, 9, 5, 9)));
                return this;
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getAccessibleContext().setAccessibleName("Saved scrambles");
        JScrollPane listPane = scroll(list);
        listPane.setPreferredSize(new Dimension(416, 114));

        JTextArea comment = textArea(3, 40);
        comment.setEditable(false);
        comment.getAccessibleContext().setAccessibleName("Saved comment");
        JButton copy = button("Copy scramble", ACCENT, true);
        JButton delete = button("Delete", DELETE, false);
        Runnable updateSelection = () -> {
            int index = list.getSelectedIndex();
            String note = index < 0 ? "" : entries.get(index).comment();
            comment.setForeground(note.isBlank() ? MUTED : TEXT);
            comment.setText(entries.isEmpty() ? "No saved scrambles yet." : note.isBlank() ? "No comment" : note);
            comment.setCaretPosition(0);
            copy.setEnabled(index >= 0);
            delete.setEnabled(index >= 0);
            copy.setText("Copy scramble");
        };
        copy.addActionListener(event -> {
            String scramble = list.getSelectedValue();
            if (scramble == null) return;
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(scramble), null);
                copy.setText("Copied");
            } catch (IllegalStateException error) {
                showError(dialog, "The clipboard is busy. Try copying again.", error);
            }
        });
        delete.addActionListener(event -> {
            int index = list.getSelectedIndex();
            if (index < 0) return;
            var entry = entries.get(index);
            int answer = JOptionPane.showOptionDialog(dialog, "Delete this saved scramble?\n" + entry.scramble(),
                    "Delete scramble", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, new String[]{"Delete", "Cancel"}, "Cancel");
            if (answer != 0) return;
            try {
                library.delete(index, entry);
                list.clearSelection();
                entries.remove(index);
                model.remove(index);
                if (!entries.isEmpty()) list.setSelectedIndex(Math.min(index, entries.size() - 1));
                updateSelection.run();
            } catch (IOException error) {
                showError(dialog, "Could not delete the scramble.\n" + error.getMessage(), error);
            }
        });
        list.addListSelectionListener(event -> updateSelection.run());
        list.setSelectedIndex(0);

        JPanel details = new JPanel(new BorderLayout(0, 5));
        details.setOpaque(false);
        details.add(label("Comment"), BorderLayout.NORTH);
        details.add(scroll(comment), BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(listPane, BorderLayout.NORTH);
        body.add(details, BorderLayout.CENTER);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(buttons(close, delete, copy), BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(copy);
    }

    private static JTextArea textArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(TEXT);
        area.setCaretColor(ACCENT);
        area.setSelectionColor(TINT);
        area.setSelectedTextColor(TEXT);
        area.setMargin(new Insets(7, 9, 7, 9));
        return area;
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    private static JScrollPane scroll(JComponent view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        view.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                pane.setBorder(BorderFactory.createLineBorder(ACCENT));
            }

            @Override public void focusLost(FocusEvent event) {
                pane.setBorder(BorderFactory.createLineBorder(BORDER));
            }
        });
        return pane;
    }

    private static JDialog dialog(Window anchor, String title) {
        // A JWindow cannot own a JDialog. Keep the dialog independently focusable above the widget.
        GraphicsConfiguration screen = anchor.getGraphicsConfiguration();
        JDialog dialog = new JDialog(null, title, Dialog.ModalityType.APPLICATION_MODAL, screen);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        boolean glass = screen.getDevice()
                .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        if (glass) dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (glass) g.setComposite(AlphaComposite.Src);
                    int radius = glass ? 18 : 0;
                    g.setColor(glass ? new Color(248, 250, 252, 248) : SURFACE);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                    g.setComposite(AlphaComposite.SrcOver);
                    g.setColor(BORDER);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                } finally { g.dispose(); }
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        dialog.setContentPane(panel);
        JLabel heading = label(title);
        heading.setForeground(TEXT);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(heading, BorderLayout.NORTH);
        MouseAdapter drag = new MouseAdapter() {
            private Point offset;
            @Override public void mousePressed(MouseEvent event) {
                offset = event.getLocationOnScreen();
                offset.translate(-dialog.getX(), -dialog.getY());
            }
            @Override public void mouseDragged(MouseEvent event) {
                if (offset != null) dialog.setLocation(event.getXOnScreen() - offset.x, event.getYOnScreen() - offset.y);
            }
        };
        heading.addMouseListener(drag);
        heading.addMouseMotionListener(drag);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        dialog.getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { dialog.dispose(); }
        });
        return dialog;
    }

    private static void show(JDialog dialog, Window owner) {
        dialog.pack();
        GraphicsConfiguration screen = owner.getGraphicsConfiguration();
        Rectangle bounds = screen.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen);
        int x = Math.max(bounds.x + insets.left, Math.min(bounds.x + bounds.width - insets.right - dialog.getWidth(),
                owner.getX() + owner.getWidth() - dialog.getWidth()));
        int y = Math.max(bounds.y + insets.top, owner.getY() - dialog.getHeight() - 6);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }

    private static JButton closer(JDialog dialog, String label) {
        JButton button = button(label, MUTED, false);
        button.addActionListener(event -> dialog.dispose());
        return button;
    }

    private static JPanel buttons(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panel.setOpaque(false);
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    private static JButton button(String text, Color color, boolean primary) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    var model = button.getModel();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (primary || (button.isEnabled() && model.isRollover())) {
                        g.setColor(primary ? (!button.isEnabled() ? BORDER : model.isArmed() || model.isRollover()
                                ? color.darker() : color) : TINT);
                        g.fillRoundRect(1, 1, button.getWidth() - 2, button.getHeight() - 2, 10, 10);
                    }
                    if (button.isFocusOwner()) {
                        g.setColor(primary ? Color.WHITE : ACCENT);
                        g.drawRoundRect(3, 3, button.getWidth() - 7, button.getHeight() - 7, 8, 8);
                    }
                    super.paint(g, component);
                } finally { g.dispose(); }
            }
        });
        button.setForeground(primary ? Color.WHITE : color);
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        return button;
    }

    private static void showError(Window owner, String message, Exception error) {
        App.LOG.log(Level.WARNING, message, error);
        JOptionPane.showMessageDialog(owner, message, "Scramble library", JOptionPane.WARNING_MESSAGE);
    }
}
