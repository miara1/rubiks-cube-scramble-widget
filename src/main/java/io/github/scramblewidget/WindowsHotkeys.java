package io.github.scramblewidget;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.MSG;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;

final class WindowsHotkeys implements AutoCloseable {
    private static final int WM_HOTKEY = 0x0312;
    private static final int WM_QUIT = 0x0012;
    private static final int MOD_NOREPEAT = 0x4000;
    private volatile int threadId;

    CompletableFuture<List<String>> start(Settings settings, Runnable toggle, Runnable next) {
        CompletableFuture<List<String>> ready = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            User32 user32 = null;
            List<Integer> registered = new ArrayList<>();
            try {
                user32 = User32.INSTANCE;
                MSG message = new MSG();
                user32.PeekMessage(message, null, 0, 0, 0); // Create this thread's message queue.
                threadId = Kernel32.INSTANCE.GetCurrentThreadId();
                List<String> errors = new ArrayList<>();
                register(user32, 1, settings.toggle(), registered, errors);
                register(user32, 2, settings.next(), registered, errors);
                ready.complete(errors);
                int status;
                while ((status = user32.GetMessage(message, null, 0, 0)) > 0) {
                    if (message.message == WM_HOTKEY) {
                        int id = message.wParam.intValue();
                        if (id == 1 || id == 2) SwingUtilities.invokeLater(id == 1 ? toggle : next);
                    }
                }
                if (status == -1) throw new IllegalStateException("Windows hotkey message loop failed: " + Native.getLastError());
            } catch (Throwable error) {
                if (!ready.completeExceptionally(error)) {
                    App.LOG.log(java.util.logging.Level.SEVERE, "Global shortcuts stopped", error);
                    SwingUtilities.invokeLater(() -> App.showError("Global shortcuts stopped. You can still use the tray icon.\nRestart the widget to try again."));
                }
            } finally {
                if (user32 != null) for (int id : registered) user32.UnregisterHotKey(null, id);
                threadId = 0;
            }
        }, "windows-hotkeys");
        thread.setDaemon(true);
        thread.start();
        return ready;
    }

    private static void register(User32 api, int id, Hotkey hotkey, List<Integer> registered, List<String> errors) {
        if (api.RegisterHotKey(null, id, hotkey.modifiers() | MOD_NOREPEAT, hotkey.key())) registered.add(id);
        else errors.add(hotkey.label() + " (Windows error " + Native.getLastError() + ")");
    }

    @Override public void close() {
        int id = threadId;
        if (id != 0) User32.INSTANCE.PostThreadMessage(id, WM_QUIT, new WPARAM(0), null);
    }
}
