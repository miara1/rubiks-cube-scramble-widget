package io.github.scramblewidget;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import javax.swing.SwingUtilities;

@EnabledOnOs(OS.WINDOWS)
@EnabledIfSystemProperty(named = "desktopTests", matches = "true")
class WindowsDesktopTest {
    @Test void realGlobalShortcutsToggleWidgetAndRequestNextWithoutTakingFocus() throws Exception {
        Settings settings = new Settings(Hotkey.parse("Ctrl+Shift+Alt+F10"), Hotkey.parse("Ctrl+Shift+Alt+F11"));
        CountDownLatch shown = new CountDownLatch(1);
        CountDownLatch hidden = new CountDownLatch(1);
        CountDownLatch next = new CountDownLatch(1);
        WidgetWindow[] window = new WidgetWindow[1];
        SwingUtilities.invokeAndWait(() -> window[0] = new WidgetWindow(settings, next::countDown));
        try (var hotkeys = new WindowsHotkeys()) {
            assertTrue(hotkeys.start(settings, () -> {
                window[0].toggle();
                if (window[0].isVisible()) shown.countDown(); else hidden.countDown();
            }, next::countDown).get(5, TimeUnit.SECONDS).isEmpty(), "Test shortcuts must be available");
            Robot robot = new Robot();
            robot.setAutoDelay(35);
            press(robot, KeyEvent.VK_F10);
            assertTrue(shown.await(5, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(window[0].isVisible());
                assertFalse(window[0].isFocused());
                assertTrue(window[0].isAlwaysOnTop());
            });
            press(robot, KeyEvent.VK_F11);
            assertTrue(next.await(5, TimeUnit.SECONDS));
            press(robot, KeyEvent.VK_F10);
            assertTrue(hidden.await(5, TimeUnit.SECONDS));
        } finally {
            SwingUtilities.invokeAndWait(() -> window[0].dispose());
        }
    }

    private static void press(Robot robot, int key) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }
}
