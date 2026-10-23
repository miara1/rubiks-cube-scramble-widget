package io.github.scramblewidget;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

record Settings(Hotkey toggle, Hotkey next) {
    static final String DEFAULT_TOGGLE = "Ctrl+Shift+Space";
    static final String DEFAULT_NEXT = "Ctrl+Shift+F9";

    static Path directory() {
        String appData = System.getenv("APPDATA");
        Path base = appData == null ? Path.of(System.getProperty("user.home"), ".config") : Path.of(appData);
        return base.resolve("RubiksCubeScrambleWidget");
    }

    static Settings load(Path file) throws IOException {
        Properties values = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                values.load(reader);
            }
        } else {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write("""
                        # Restart the widget after changing shortcuts.
                        # Keys: Space, A-Z, 0-9, F1-F11. Modifiers: Ctrl, Shift, Alt.
                        # Avoid Ctrl+Alt with letters: it can interfere with AltGr typing.
                        toggle=Ctrl+Shift+Space
                        next=Ctrl+Shift+F9
                        """);
            }
        }
        Hotkey toggle = Hotkey.parse(values.getProperty("toggle", DEFAULT_TOGGLE));
        Hotkey next = Hotkey.parse(values.getProperty("next", DEFAULT_NEXT));
        if (toggle.sameBinding(next)) throw new IllegalArgumentException("The two shortcuts must be different.");
        return new Settings(toggle, next);
    }
}
