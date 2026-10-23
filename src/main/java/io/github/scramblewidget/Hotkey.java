package io.github.scramblewidget;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** The small subset of RegisterHotKey bindings we support in settings.properties. */
record Hotkey(int modifiers, int key, String label) {
    static Hotkey parse(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Empty shortcut");
        String[] parts = text.toUpperCase(Locale.ROOT).split("\\+", -1);
        int modifiers = 0;
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (!seen.add(part)) throw new IllegalArgumentException("Repeated modifier: " + text);
            modifiers |= switch (part) {
                case "ALT" -> 0x0001;
                case "CTRL" -> 0x0002;
                case "SHIFT" -> 0x0004;
                default -> throw new IllegalArgumentException("Use Ctrl, Shift or Alt: " + text);
            };
        }
        // Avoid registering ordinary typing keys as global shortcuts.
        if ((modifiers & 0x0003) == 0) throw new IllegalArgumentException("Include Ctrl or Alt: " + text);
        String last = parts[parts.length - 1].trim();
        int key;
        if (last.equals("SPACE")) key = 0x20;
        else if (last.matches("[A-Z0-9]")) key = last.charAt(0);
        else if (last.matches("F([1-9]|1[01])")) key = 0x6F + Integer.parseInt(last.substring(1));
        else throw new IllegalArgumentException("Use Space, A-Z, 0-9 or F1-F11: " + text);
        return new Hotkey(modifiers, key, text.trim());
    }

    boolean sameBinding(Hotkey other) {
        return modifiers == other.modifiers && key == other.key;
    }
}
