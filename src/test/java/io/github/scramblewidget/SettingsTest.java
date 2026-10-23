package io.github.scramblewidget;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsTest {
    @TempDir Path directory;

    @Test void createsDefaultsAndReadsEditedShortcuts() throws Exception {
        Path file = directory.resolve("settings.properties");
        assertEquals(Hotkey.parse("Ctrl+Shift+Space"), Settings.load(file).toggle());
        Files.writeString(file, "toggle=Alt+F8\nnext=Ctrl+Shift+F10\n");
        Settings custom = Settings.load(file);
        assertEquals(0x77, custom.toggle().key());
        assertEquals(0x0006, custom.next().modifiers());
    }

    @Test void rejectsDuplicateShortcutsRegardlessOfModifierOrder() throws Exception {
        Path file = directory.resolve("settings.properties");
        Files.writeString(file, "toggle=Ctrl+Shift+Space\nnext=Shift+Ctrl+Space\n");
        assertThrows(IllegalArgumentException.class, () -> Settings.load(file));
    }

    @Test void rejectsTypingKeysUnsupportedAndReservedKeys() {
        for (String text : new String[]{"N", "Shift+N", "Ctrl+F12", "Win+N", "Ctrl+Ctrl+N", "Ctrl+", ""}) {
            assertThrows(IllegalArgumentException.class, () -> Hotkey.parse(text), text);
        }
    }
}
