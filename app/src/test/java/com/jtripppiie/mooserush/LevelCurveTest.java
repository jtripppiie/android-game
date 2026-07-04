package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LevelCurveTest {

    @Test
    public void levelIndexStartsAtZero() {
        assertEquals(0, LevelCurve.levelIndex(0));
        assertEquals(0, LevelCurve.levelIndex(149));
    }

    @Test
    public void levelIndexCrossesThresholds() {
        assertEquals(1, LevelCurve.levelIndex(150));
        assertEquals(2, LevelCurve.levelIndex(400));
        assertEquals(2, LevelCurve.levelIndex(799));
        assertEquals(3, LevelCurve.levelIndex(800));
    }

    @Test
    public void levelIndexCapsAtMax() {
        int maxIndex = LevelCurve.LEVEL_XP.length - 1;
        assertEquals(maxIndex, LevelCurve.levelIndex(8200));
        assertEquals(maxIndex, LevelCurve.levelIndex(999_999));
    }

    @Test
    public void negativeXpIsFloored() {
        assertEquals(0, LevelCurve.levelIndex(-500));
    }

    @Test
    public void floorAndGoalMatchThresholds() {
        assertEquals(150, LevelCurve.currentFloor(1));
        assertEquals(400, LevelCurve.nextGoal(1));
    }

    @Test
    public void goalClampsAtMaxLevel() {
        int maxIndex = LevelCurve.LEVEL_XP.length - 1;
        int top = LevelCurve.LEVEL_XP[maxIndex];
        assertEquals(top, LevelCurve.nextGoal(maxIndex));
        assertEquals(top, LevelCurve.currentFloor(maxIndex));
    }

    @Test
    public void progressIsBounded() {
        assertEquals(0f, LevelCurve.progress(0), 0.0001f);
        assertTrue(LevelCurve.progress(275) > 0f);
        assertTrue(LevelCurve.progress(275) < 1f);
        assertEquals(1f, LevelCurve.progress(50_000), 0.0001f);
    }

    @Test
    public void progressIsHalfwayBetweenThresholds() {
        // Level 1 floor 150, next goal 400, midpoint = 275
        assertEquals(0.5f, LevelCurve.progress(275), 0.0001f);
    }
}
