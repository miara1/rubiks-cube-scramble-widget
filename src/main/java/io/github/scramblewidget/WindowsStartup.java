package io.github.scramblewidget;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.nio.file.Files;
import java.nio.file.Path;

/** A per-user startup entry; never requires administrator privileges. */
final class WindowsStartup {
    private static final String RUN = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String NAME = "RubiksCubeScrambleWidget";

    static String launcher() {
        String path = System.getProperty("jpackage.app-path");
        return path != null && Files.isRegularFile(Path.of(path)) ? path : null;
    }

    static boolean isEnabled() {
        return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN, NAME);
    }

    static void setEnabled(boolean enabled) {
        if (enabled) {
            String path = launcher();
            if (path == null) throw new IllegalStateException("Autostart requires the packaged .exe release.");
            Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, RUN);
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, RUN, NAME, "\"" + path + "\" --hidden");
        } else if (isEnabled()) {
            Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, RUN, NAME);
        }
    }
}
