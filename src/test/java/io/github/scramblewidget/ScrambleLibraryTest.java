package io.github.scramblewidget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScrambleLibraryTest {
    @TempDir Path directory;

    @Test void reloadsScramblesInSaveOrderWithMultilineUnicodeAndEmptyComments() throws Exception {
        Path file = directory.resolve("profile").resolve("scrambles.properties");
        ScrambleLibrary library = new ScrambleLibrary(file);
        assertTrue(library.load().isEmpty());
        String comment = "Dobra próba\nF2L: ładny początek\t= wyjątkowy \\";
        library.save("U R2 F'", comment);
        library.save("L B2 D'", "");

        assertEquals(List.of(new ScrambleLibrary.Entry("L B2 D'", ""),
                new ScrambleLibrary.Entry("U R2 F'", comment)), new ScrambleLibrary(file).load());
    }

    @Test void willNotReplaceAnUnreadableLibraryWithNewEntries() throws Exception {
        Path file = directory.resolve("scrambles.properties");
        String incomplete = "scramble.0=U R2 F'\n";
        Files.writeString(file, incomplete);

        assertThrows(IOException.class, () -> new ScrambleLibrary(file).save("L B2 D'", ""));
        assertEquals(incomplete, Files.readString(file));
    }

    @Test void deletesOnlyTheSelectedCopyAndPersistsAnEmptyLibrary() throws Exception {
        Path file = directory.resolve("scrambles.properties");
        ScrambleLibrary library = new ScrambleLibrary(file);
        library.save("U R2 F'", "Keep this comment");
        library.save("U R2 F'", "Remove this comment");
        var entries = library.load();

        assertThrows(IOException.class, () -> library.delete(1, entries.getFirst()));
        library.delete(0, entries.getFirst());
        assertEquals(List.of(entries.getLast()), new ScrambleLibrary(file).load());
        library.delete(0, entries.getLast());
        assertTrue(new ScrambleLibrary(file).load().isEmpty());
    }
}
