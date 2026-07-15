package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PresentationTuningTest {
    @Test public void normalRunnerLanePreservesForwardReactionDistance() {
        assertEquals(580f, PresentationTuning.runnerMaxX(1000f, 20f, false), 0.001f);
        assertEquals(720f, PresentationTuning.runnerMaxX(1000f, 20f, true), 0.001f);
    }

    @Test public void flashesAndWeatherCannotBlankThePlayfield() {
        assertEquals(92, PresentationTuning.worldFlashAlpha(1f));
        assertEquals(92, PresentationTuning.worldFlashAlpha(10f));
        assertTrue(PresentationTuning.blizzardAlpha(1f) <= 42);
    }

    @Test public void animationCannotSeverelyDeformTheRunner() {
        assertEquals(0.88f, PresentationTuning.visualSquashY(0.65f), 0.001f);
        assertEquals(1.12f, PresentationTuning.visualSquashY(1.4f), 0.001f);
    }
}
