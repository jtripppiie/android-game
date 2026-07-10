package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameMathTest {

    @Test
    public void clampBounds() {
        assertEquals(5f, GameMath.clamp(5f, 0f, 10f), 0.0001f);
        assertEquals(0f, GameMath.clamp(-3f, 0f, 10f), 0.0001f);
        assertEquals(10f, GameMath.clamp(42f, 0f, 10f), 0.0001f);
    }

    @Test
    public void clampIntBounds() {
        assertEquals(2, GameMath.clampInt(2, 0, 4));
        assertEquals(0, GameMath.clampInt(-9, 0, 4));
        assertEquals(4, GameMath.clampInt(99, 0, 4));
    }

    @Test
    public void overlappingCirclesHit() {
        assertTrue(GameMath.circleHitsCircle(0f, 0f, 5f, 4f, 0f, 5f));
    }

    @Test
    public void distantCirclesMiss() {
        assertFalse(GameMath.circleHitsCircle(0f, 0f, 2f, 100f, 100f, 2f));
    }

    @Test
    public void touchingCirclesAtExactRangeDoNotHit() {
        // distance 10 == combined radius 10 -> strictly-less means no hit
        assertFalse(GameMath.circleHitsCircle(0f, 0f, 5f, 10f, 0f, 5f));
    }

    @Test
    public void circleInsideRectHits() {
        assertTrue(GameMath.circleHitsRect(5f, 5f, 2f, 0f, 0f, 10f, 10f));
    }

    @Test
    public void circleTouchingRectEdgeHits() {
        assertTrue(GameMath.circleHitsRect(11f, 5f, 2f, 0f, 0f, 10f, 10f));
    }

    @Test
    public void circleFarFromRectMisses() {
        assertFalse(GameMath.circleHitsRect(50f, 50f, 2f, 0f, 0f, 10f, 10f));
    }

    @Test
    public void circleOnDiagonalLaserSegmentHits() {
        assertTrue(GameMath.circleHitsSegment(5f, 5f, 1f, 0f, 0f, 10f, 10f, 1f));
    }

    @Test
    public void circleInsideOldBoundingBoxButAwayFromLaserMisses() {
        assertFalse(GameMath.circleHitsSegment(2f, 8f, 0.5f, 0f, 0f, 10f, 10f, 0.5f));
    }

    @Test
    public void circleHitsLaserEndCap() {
        assertTrue(GameMath.circleHitsSegment(11f, 0f, 0.6f, 0f, 0f, 10f, 0f, 0.6f));
    }
}
