package io.github.scramblewidget;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle;

class ScrambleServiceTest {
    @Test void generatedScramblesAreValidDistinctAndPassTnoodleDistanceFilter() throws Exception {
        ThreeByThreeCubePuzzle puzzle = new ThreeByThreeCubePuzzle();
        var seen = new HashSet<String>();
        try (var service = new ScrambleService()) {
            for (int i = 0; i < 8; i++) {
                String scramble = service.next().get(60, TimeUnit.SECONDS);
                assertTrue(scramble.matches("[URFDLB][2']?( [URFDLB][2']?)*"), scramble);
                assertTrue(seen.add(scramble));
                var state = puzzle.getSolvedState().applyAlgorithm(scramble);
                assertNull(state.solveIn(puzzle.getWcaMinScrambleDistance() - 1));
            }
        }
    }
}
