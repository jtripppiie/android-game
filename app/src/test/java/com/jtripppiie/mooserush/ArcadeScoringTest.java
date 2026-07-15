package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ArcadeScoringTest {
    @Test public void comboIsAShortActionChainNotPermanentProgress() {
        assertEquals(1, ArcadeScoring.scoreMultiplierForCombo(3));
        assertEquals(2, ArcadeScoring.scoreMultiplierForCombo(4));
        assertEquals(4, ArcadeScoring.scoreMultiplierForCombo(10));
        assertTrue(ArcadeScoring.comboWindowSeconds(10) < ArcadeScoring.comboWindowSeconds(1));
        assertTrue(ArcadeScoring.comboWindowSeconds(100) >= 2.15f);
    }

    @Test public void surgesDoNotStackPastFourTimes() {
        assertEquals(40, ArcadeScoring.actionAward(10, 10, true, 1f));
        assertEquals(30, ArcadeScoring.actionAward(10, 7, false, 1f));
    }

    @Test public void bankedRewardsIgnoreLiveComboAndPerks() {
        assertEquals(240, ArcadeScoring.bankedAward(240));
        assertEquals(0, ArcadeScoring.bankedAward(-10));
    }
}
