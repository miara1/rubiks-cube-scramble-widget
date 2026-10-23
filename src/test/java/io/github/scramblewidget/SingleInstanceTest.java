package io.github.scramblewidget;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleInstanceTest {
    @TempDir Path directory;

    @Test void blocksSecondInstanceAndReleasesLockOnExit() throws Exception {
        Path path = directory.resolve("instance.lock");
        try (var first = SingleInstance.acquire(path)) {
            assertNotNull(first);
            assertNull(SingleInstance.acquire(path));
        }
        try (var restarted = SingleInstance.acquire(path)) {
            assertNotNull(restarted);
        }
    }
}
