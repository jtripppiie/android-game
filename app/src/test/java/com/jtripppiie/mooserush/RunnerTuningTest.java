package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunnerTuningTest {

    @Test
    public void gateCooldownHasFairMinimum() {
        assertEquals(1.05f, RunnerTuning.nextGateCooldown(0.4f, 99), 0.0001f);
    }

    @Test
    public void hazardCooldownHasFairMinimum() {
        assertEquals(1.28f, RunnerTuning.nextHazardCooldown(4, 999), 0.0001f);
    }

    @Test
    public void gateHeightStaysInJumpableBand() {
        float density = 3f;
        float height = RunnerTuning.gateHeight(density, 2, 20, 1f);
        assertTrue(height <= 72f * density);
        assertTrue(height >= 24f * density);
    }

    @Test
    public void snowbankGateHeightStaysLowLikeAPile() {
        float density = 3f;
        float height = RunnerTuning.gateHeight(density, 4, 20, 1f);
        assertTrue(height <= 27f * density);
        assertTrue(height >= 14f * density);
    }

    @Test
    public void icebergGateHeightStaysLowerThanOldSpikeWalls() {
        float density = 3f;
        float height = RunnerTuning.gateHeight(density, 3, 20, 1f);
        assertTrue(height <= 46f * density);
        assertTrue(height >= 21f * density);
    }

    @Test
    public void scrollSpeedRampsSlowly() {
        assertEquals(150f, RunnerTuning.scrollSpeedDp(150f, 0), 0.0001f);
        assertEquals(214f, RunnerTuning.scrollSpeedDp(150f, 99), 0.0001f);
    }

    @Test
    public void doubleJumpHasEnoughLiftForFollowUpHazard() {
        float doubleJumpApexDp = RunnerTuning.DOUBLE_JUMP_VELOCITY_DP
                * RunnerTuning.DOUBLE_JUMP_VELOCITY_DP
                / (2f * RunnerTuning.GRAVITY_DP);

        assertTrue(doubleJumpApexDp >= 96f);
    }

    @Test
    public void normalGravityKeepsJumpFloatierThanDarkness() {
        assertTrue(RunnerTuning.GRAVITY_DP < RunnerTuning.DARKNESS_GRAVITY_DP);
        assertTrue(RunnerTuning.GRAVITY_DP <= 1600f);
    }
}
