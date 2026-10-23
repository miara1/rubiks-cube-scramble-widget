package io.github.scramblewidget;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Render the actual Swing widget (not a mockup) for documentation and visual review. */
public final class RenderPreview {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("docs"));
        String scramble;
        try (var service = new ScrambleService()) {
            scramble = service.next().get(60, TimeUnit.SECONDS);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                var settings = new Settings(Hotkey.parse(Settings.DEFAULT_TOGGLE), Hotkey.parse(Settings.DEFAULT_NEXT));
                WidgetWindow window = new WidgetWindow(settings, () -> {});
                window.showScramble(scramble);
                window.setBusy(false);
                window.addNotify();
                window.validate();
                BufferedImage image = new BufferedImage(window.getWidth() * 2, window.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                graphics.scale(2, 2);
                window.getContentPane().printAll(graphics);
                graphics.dispose();
                ImageIO.write(image, "png", Path.of("docs", "screenshot.png").toFile());
                window.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(Icons.cube(256), "png", png);
        ByteBuffer ico = ByteBuffer.allocate(22 + png.size()).order(ByteOrder.LITTLE_ENDIAN);
        ico.putShort((short) 0).putShort((short) 1).putShort((short) 1);
        ico.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0);
        ico.putShort((short) 1).putShort((short) 32).putInt(png.size()).putInt(22).put(png.toByteArray());
        Files.write(Path.of("docs", "icon.ico"), ico.array());
    }
}
