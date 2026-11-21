package io.github.scramblewidget;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class ScrambleLibrary {
    record Entry(String scramble, String comment) {}

    private final Path file;

    ScrambleLibrary(Path file) {
        this.file = file.toAbsolutePath();
    }

    List<Entry> load() throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (NoSuchFileException ignored) {
            return List.of();
        } catch (IllegalArgumentException error) {
            throw new IOException("Invalid scramble library file", error);
        }

        if (properties.size() % 2 != 0) throw new IOException("Incomplete scramble library file");
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < properties.size() / 2; i++) {
            String scramble = properties.getProperty("scramble." + i);
            String comment = properties.getProperty("comment." + i);
            if (scramble == null || scramble.isBlank() || comment == null) {
                throw new IOException("Invalid scramble library entry " + i);
            }
            entries.add(new Entry(scramble, comment));
        }
        return entries;
    }

    void save(String scramble, String comment) throws IOException {
        if (scramble.isBlank()) throw new IllegalArgumentException("Scramble must not be empty");
        List<Entry> entries = new ArrayList<>(load());
        entries.addFirst(new Entry(scramble, comment));
        write(entries);
    }

    void delete(int index, Entry expected) throws IOException {
        List<Entry> entries = new ArrayList<>(load());
        if (index < 0 || index >= entries.size() || !entries.get(index).equals(expected)) {
            throw new IOException("The library changed. Reopen it and try again.");
        }
        entries.remove(index);
        write(entries);
    }

    private void write(List<Entry> entries) throws IOException {
        Properties properties = new Properties();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            properties.setProperty("scramble." + i, entry.scramble());
            properties.setProperty("comment." + i, entry.comment());
        }

        Path directory = Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(directory, "scrambles-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary)) {
                properties.store(writer, "Saved scrambles (newest first)");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
