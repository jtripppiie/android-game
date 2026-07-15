package com.jtripppiie.mooserush;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WildlifeScaleTest {
    private static final float RUNNER_VISUAL_HEIGHT_DP = 20.5f * 1.20f * 4.08f;
    @Test
    public void largeWildlifeReadLargerThanWolf() {
        assertTrue(WildlifeScale.visualHeightDp("MOOSE") > WildlifeScale.visualHeightDp("WOLF"));
        assertTrue(WildlifeScale.visualHeightDp("BEAR") > WildlifeScale.visualHeightDp("WOLF"));
        assertTrue(WildlifeScale.visualHeightDp("POLAR") > WildlifeScale.visualHeightDp("BEAR"));
    }

    @Test
    public void mooseHasLongestGroundSilhouette() {
        assertTrue(WildlifeScale.halfWidthDp("MOOSE") > WildlifeScale.halfWidthDp("POLAR"));
        assertTrue(WildlifeScale.halfWidthDp("POLAR") > WildlifeScale.halfWidthDp("WOLF"));
    }

    @Test
    public void flyingAndSwimmingWildlifeStayBelowHeavyAnimalMass() {
        assertTrue(WildlifeScale.visualHeightDp("EAGLE") < WildlifeScale.visualHeightDp("BEAR"));
        assertTrue(WildlifeScale.visualHeightDp("SALMON") < WildlifeScale.visualHeightDp("WOLF"));
    }

    @Test
    public void largeWildlifeReadsAtBelievableHumanScale() {
        assertTrue(WildlifeScale.visualHeightDp("MOOSE") > RUNNER_VISUAL_HEIGHT_DP * 1.25f);
        assertTrue(WildlifeScale.visualHeightDp("POLAR") > RUNNER_VISUAL_HEIGHT_DP * 1.10f);
        assertTrue(WildlifeScale.visualHeightDp("BEAR") >= RUNNER_VISUAL_HEIGHT_DP);
        assertTrue(WildlifeScale.visualHeightDp("WOLF") < RUNNER_VISUAL_HEIGHT_DP * 0.65f);
    }

    @Test
    public void largeGroundAnimalsCannotVisiblyPassThroughRunner() {
        assertTrue(WildlifeScale.halfWidthDp("MOOSE") / WildlifeScale.collisionRadiusDp("MOOSE") < 2.20f);
        assertTrue(WildlifeScale.halfWidthDp("POLAR") / WildlifeScale.collisionRadiusDp("POLAR") < 2.20f);
        assertTrue(WildlifeScale.halfWidthDp("BEAR") / WildlifeScale.collisionRadiusDp("BEAR") < 2.20f);
        assertTrue(WildlifeScale.halfWidthDp("WOLF") / WildlifeScale.collisionRadiusDp("WOLF") < 2.20f);
    }
}
