package io.github.scramblewidget;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle;

/** All TNoodle work runs on one worker, with at most one scramble prepared ahead. */
final class ScrambleService implements AutoCloseable {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "scramble-generator");
        thread.setDaemon(true);
        return thread;
    });
    private ThreeByThreeCubePuzzle puzzle;
    private CompletableFuture<String> prepared = generate();

    private CompletableFuture<String> generate() {
        return CompletableFuture.supplyAsync(() -> {
            if (puzzle == null) puzzle = new ThreeByThreeCubePuzzle();
            return puzzle.generateScramble();
        }, worker);
    }

    synchronized CompletableFuture<String> next() {
        CompletableFuture<String> result = prepared;
        prepared = generate();
        return result;
    }

    @Override public void close() {
        worker.shutdownNow();
    }
}
