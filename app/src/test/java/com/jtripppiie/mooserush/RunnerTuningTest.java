package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunnerTuningTest {

    @Test
    public void gateCooldownHasFairMinimum() {
        assertEquals(1.45f, RunnerTuning.nextGateCooldown(0.4f, 99), 0.0001f);
    }

    @Test
    public void hazardCooldownHasFairMinimum() {
        assertEquals(1.95f, RunnerTuning.nextHazardCooldown(4, 999), 0.0001f);
    }

    @Test
    public void gateHeightStaysInJumpableBand() {
        float density = 3f;
        float height = RunnerTuning.gateHeight(density, 4, 20, 1f);
        assertTrue(height <= 100f * density);
        assertTrue(height >= 32f * density);
    }

    @Test
    public void scrollSpeedRampsSlowly() {
        assertEquals(150f, RunnerTuning.scrollSpeedDp(150f, 0), 0.0001f);
        assertEquals(192f, RunnerTuning.scrollSpeedDp(150f, 99), 0.0001f);
    }
}
