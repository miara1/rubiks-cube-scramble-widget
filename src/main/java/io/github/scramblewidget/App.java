package io.github.scramblewidget;

import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {
    static final Logger LOG = Logger.getLogger("scramble-widget");
    private static final String TITLE = "Rubik's Cube Scramble Widget";
    private final SingleInstance instance;
    private final Path settingsFile;
    private final Settings settings;
    private final WindowsHotkeys hotkeys = new WindowsHotkeys();
    private final ScrambleService scrambles = new ScrambleService();
    private final WidgetWindow window;
    private TrayIcon tray;
    private boolean busy;
    private boolean closed;

    private App(SingleInstance instance, Path settingsFile, Settings settings) {
        this.instance = instance;
        this.settingsFile = settingsFile;
        this.settings = settings;
        window = new WidgetWindow(settings, this::nextScramble);
    }

    public static void main(String[] args) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            System.err.println("This widget currently supports Windows only.");
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            SingleInstance instance = null;
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Path directory = Settings.directory();
                Files.createDirectories(directory);
                instance = SingleInstance.acquire(directory.resolve("instance.lock"));
                if (instance == null) {
                    JOptionPane.showMessageDialog(null, "The widget is already running.\nUse its tray icon to show it.", TITLE, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                FileHandler log = new FileHandler(directory.resolve("widget.log").toString(), 65536, 1, true);
                log.setFormatter(new SimpleFormatter());
                LOG.addHandler(log);
                Path file = directory.resolve("settings.properties");
                Settings settings = Settings.load(file);
                App app = new App(instance, file, settings);
                app.start(Arrays.asList(args).contains("--hidden"));
            } catch (Exception | LinkageError error) {
                LOG.log(Level.SEVERE, "Cannot start widget", error);
                showError("Cannot start the widget:\n" + error.getMessage()
                        + "\n\nSettings and log: " + Settings.directory());
                if (instance != null) try { instance.close(); } catch (Exception ignored) { }
                System.exit(1);
            }
        });
    }

    private void start(boolean hidden) throws Exception {
        if (!SystemTray.isSupported()) throw new IllegalStateException("The Windows system tray is unavailable.");
        PopupMenu menu = new PopupMenu();
        menu.add(item("Show / hide  (" + settings.toggle().label() + ")", window::toggle));
        menu.add(item("New scramble  (" + settings.next().label() + ")", this::nextScramble));
        menu.addSeparator();
        menu.add(item("Edit shortcuts (restart to apply)", () -> {
            try { new ProcessBuilder("notepad.exe", settingsFile.toString()).start(); }
            catch (Exception error) { showError("Open this file in a text editor:\n" + settingsFile); }
        }));
        CheckboxMenuItem startup = new CheckboxMenuItem("Start with Windows");
        startup.setEnabled(WindowsStartup.launcher() != null);
        try { startup.setState(WindowsStartup.isEnabled()); }
        catch (RuntimeException error) { LOG.log(Level.WARNING, "Cannot read autostart", error); }
        startup.addItemListener(event -> {
            boolean enabled = startup.getState();
            try { WindowsStartup.setEnabled(enabled); }
            catch (RuntimeException error) {
                startup.setState(!enabled);
                showError("Cannot change autostart:\n" + error.getMessage());
            }
        });
        menu.add(startup);
        menu.addSeparator();
        menu.add(item("Quit", this::quit));
        tray = new TrayIcon(Icons.cube(32), TITLE, menu);
        tray.setImageAutoSize(true);
        tray.addActionListener(event -> SwingUtilities.invokeLater(window::toggle));
        SystemTray.getSystemTray().add(tray);

        hotkeys.start(settings, window::toggle, this::nextScramble).whenComplete((errors, error) ->
                SwingUtilities.invokeLater(() -> {
                    if (closed) return;
                    if (error != null) {
                        LOG.log(Level.WARNING, "Cannot register shortcuts", error);
                        showError("Cannot enable global shortcuts. The tray menu still works.\n" + error.getMessage());
                    } else if (!errors.isEmpty()) {
                        showError("These shortcuts could not be registered (they may be in use):\n"
                                + String.join("\n", errors) + "\n\nChoose different shortcuts in:\n" + settingsFile
                                + "\nThen restart the widget. The tray menu still works.");
                    }
                }));
        nextScramble();
        if (!hidden) window.showAtCorner();
        LOG.info("Started; Java " + System.getProperty("java.runtime.version")
                + "; autostart available: " + (WindowsStartup.launcher() != null));
    }

    private static MenuItem item(String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(event -> SwingUtilities.invokeLater(action));
        return item;
    }

    private void nextScramble() {
        if (busy || closed) return;
        busy = true;
        window.setBusy(true);
        scrambles.next().whenComplete((scramble, error) -> SwingUtilities.invokeLater(() -> {
            if (closed) return;
            busy = false;
            window.setBusy(false);
            if (error == null) window.showScramble(scramble);
            else {
                LOG.log(Level.SEVERE, "Cannot generate scramble", error);
                showError("Could not generate a scramble. Press the refresh button to retry.\nLog: "
                        + Settings.directory().resolve("widget.log"));
            }
        }));
    }

    private void quit() {
        if (closed) return;
        closed = true;
        hotkeys.close();
        scrambles.close();
        if (tray != null) SystemTray.getSystemTray().remove(tray);
        window.dispose();
        try { instance.close(); } catch (Exception error) { LOG.log(Level.WARNING, "Cannot release instance lock", error); }
        System.exit(0);
    }

    static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, TITLE, JOptionPane.WARNING_MESSAGE);
    }
}
