package com.jtripppiie.mooserush;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpriteSheetTrimTest {

    @Test
    public void trimAddsGuardPixelAtAtlasFrameEdges() {
        int[] source = SpriteSheetMath.trimmedSourceValues(
                2,
                362,
                724,
                new int[]{0, 223, 362, 527}
        );

        assertArrayEquals(new int[]{744, 229, 1066, 521}, source);
    }

    @Test
    public void trimKeepsInteriorBoundsExact() {
        int[] source = SpriteSheetMath.trimmedSourceValues(
                0,
                362,
                724,
                new int[]{22, 296, 361, 436}
        );

        assertArrayEquals(new int[]{28, 302, 355, 430}, source);
    }

    @Test
    public void scoreMultiplierRewardsRealCombos() {
        assertEquals(1, ArcadeScoring.scoreMultiplierForCombo(0));
        assertEquals(1, ArcadeScoring.scoreMultiplierForCombo(2));
        assertEquals(1, ArcadeScoring.scoreMultiplierForCombo(3));
        assertEquals(2, ArcadeScoring.scoreMultiplierForCombo(4));
        assertEquals(3, ArcadeScoring.scoreMultiplierForCombo(7));
        assertEquals(4, ArcadeScoring.scoreMultiplierForCombo(10));
        assertEquals(4, ArcadeScoring.scoreMultiplierForCombo(99));
    }

    @Test
    public void stageClearBonusIncludesStageComboAndStars() {
        assertEquals(100, ArcadeScoring.stageClearBonus(0, 0, 0));
        assertEquals(270, ArcadeScoring.stageClearBonus(2, 10, 4));
        assertEquals(380, ArcadeScoring.stageClearBonus(4, 40, 0));
    }
}
