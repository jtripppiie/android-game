package com.jtripppiie.mooserush;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WildlifeScaleTest {
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
}
