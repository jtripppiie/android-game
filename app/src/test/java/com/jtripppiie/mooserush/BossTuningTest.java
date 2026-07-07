package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BossTuningTest {

    @Test
    public void phasePressureAndEnrageSpeedUpBossStates() {
        float opening = BossTuning.stateSpeed(false, 12, 12, false);
        float phaseTwo = BossTuning.stateSpeed(false, 6, 12, false);
        float enraged = BossTuning.stateSpeed(false, 2, 12, true);

        assertEquals(1f, opening, 0.0001f);
        assertTrue(phaseTwo > opening);
        assertTrue(enraged > phaseTwo);
    }

    @Test
    public void finalBossAddsLaserPatternDuringPhaseTwo() {
        int lunge = 0;
        int snowWave = 1;
        int summon = 2;
        int laser = 3;

        assertEquals(laser, BossTuning.nextPattern(true, 3, 8, 3, lunge, snowWave, summon, laser));
        assertEquals(summon, BossTuning.nextPattern(false, 3, 8, 2, lunge, snowWave, summon, laser));
        assertEquals(lunge, BossTuning.nextPattern(false, 8, 8, 4, lunge, snowWave, summon, laser));
        assertEquals(snowWave, BossTuning.nextPattern(false, 8, 8, 5, lunge, snowWave, summon, laser));
    }

    @Test
    public void finalBossTellsLongerButEnrageTightensWindows() {
        assertTrue(BossTuning.tellDuration(true, false) > BossTuning.tellDuration(false, false));
        assertTrue(BossTuning.tellDuration(true, true) < BossTuning.tellDuration(true, false));
        assertTrue(BossTuning.attackDuration(true, false, true, false) > BossTuning.attackDuration(false, false, true, false));
        assertTrue(BossTuning.recoverDuration(true, true) < BossTuning.recoverDuration(true, false));
    }
}
