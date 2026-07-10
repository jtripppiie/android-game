package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StageBossRulesTest {
    @Test
    public void midnightSunAndBearCountryUseLaserTuning() {
        assertTrue(StageBossRules.usesLaserTuning(0));
        assertTrue(StageBossRules.usesLaserTuning(4));
        assertFalse(StageBossRules.usesLaserTuning(2));
    }

    @Test
    public void darkWinterKeepsItsLungeLungeSummonStrategy() {
        assertEquals(10, StageBossRules.nextPattern(3, 4, 4, 0, 10, 11, 12, 13));
        assertEquals(10, StageBossRules.nextPattern(3, 4, 4, 1, 10, 11, 12, 13));
        assertEquals(12, StageBossRules.nextPattern(3, 4, 4, 2, 10, 11, 12, 13));
    }
}
