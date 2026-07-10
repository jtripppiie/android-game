package com.jtripppiie.mooserush;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SprayTuningTest {

    @Test
    public void coneHitsForwardTargetsInsideRange() {
        assertTrue(SprayTuning.coneHitsPoint(100f, 200f, 180f, 214f, 10f, 1f));
        assertTrue(SprayTuning.coneHitsPoint(100f, 200f, 242f, 250f, 12f, 1f));
        assertTrue(SprayTuning.coneHitsPoint(100f, 200f, 300f, 250f, 12f, 1f));
    }

    @Test
    public void coneRejectsTargetsBehindOrPastRange() {
        assertFalse(SprayTuning.coneHitsPoint(100f, 200f, 80f, 200f, 10f, 1f));
        assertFalse(SprayTuning.coneHitsPoint(100f, 200f, 325f, 200f, 10f, 1f));
    }

    @Test
    public void coneWidensAsItTravelsForward() {
        assertFalse(SprayTuning.coneHitsPoint(100f, 200f, 110f, 236f, 4f, 1f));
        assertTrue(SprayTuning.coneHitsPoint(100f, 200f, 230f, 236f, 4f, 1f));
    }

    @Test
    public void finalStageSpawnsSprayMoreOften() {
        assertTrue(SprayTuning.spawnChance(4) > SprayTuning.spawnChance(2));
        assertTrue(SprayTuning.spawnChance(2) > SprayTuning.spawnChance(0));
    }
}
