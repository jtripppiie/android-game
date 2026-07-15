package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RushDirectorTest {
    @Test
    public void cyclesThroughAuthoredEncounterBeats() {
        assertEquals(RushDirector.BEAT_LAUNCH, RushDirector.beatFor(0));
        assertEquals(RushDirector.BEAT_PRECISION, RushDirector.beatFor(1));
        assertEquals(RushDirector.BEAT_WILDLIFE, RushDirector.beatFor(2));
        assertEquals(RushDirector.BEAT_JACKPOT, RushDirector.beatFor(3));
        assertEquals(RushDirector.BEAT_PRECISION, RushDirector.beatFor(4));
    }

    @Test
    public void jackpotCreatesLargestStarTrail() {
        assertEquals(4, RushDirector.starTrailCount(3));
        assertTrue(RushDirector.starTrailCount(3) > RushDirector.starTrailCount(2));
    }

    @Test
    public void flowRaisesSpeedAndCompressesCooldowns() {
        assertTrue(RushDirector.worldSpeedMultiplier(true) > 1f);
        assertTrue(RushDirector.horizontalSpeedMultiplier(true) > 1f);
        assertTrue(RushDirector.gateCooldownMultiplier(1, true) < RushDirector.gateCooldownMultiplier(1, false));
        assertTrue(RushDirector.hazardCooldownMultiplier(2, true) < RushDirector.hazardCooldownMultiplier(2, false));
    }

    @Test
    public void laterRunsBuildMultiThreatWaves() {
        assertEquals(1, RushDirector.hazardWaveSize(0, 1));
        assertEquals(2, RushDirector.hazardWaveSize(2, 5));
        assertEquals(3, RushDirector.hazardWaveSize(4, 6));
        assertTrue(RushDirector.hazardWaveSpacingDp(1) > RushDirector.hazardWaveSpacingDp(0));
        assertTrue(RushDirector.hazardWaveSpacingDp(0) >= 188f);
    }
}
