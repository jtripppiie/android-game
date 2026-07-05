package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DifficultyCurveTest {

    @Test
    public void tensionStartsGentleAndCaps() {
        assertEquals(0f, DifficultyCurve.tension(0, 0, 10), 0.0001f);
        assertTrue(DifficultyCurve.tension(4, 10, 10) <= 1f);
        assertTrue(DifficultyCurve.tension(4, 10, 10) > DifficultyCurve.tension(1, 2, 10));
    }

    @Test
    public void pacingGetsHarderWithoutCrossingFairMinimums() {
        float tension = DifficultyCurve.tension(4, 10, 10);

        assertTrue(DifficultyCurve.speedMultiplier(tension) > 1f);
        assertEquals(1.32f, DifficultyCurve.gateCooldown(0.3f, tension), 0.0001f);
        assertEquals(1.52f, DifficultyCurve.hazardCooldown(0.3f, tension), 0.0001f);
        assertTrue(DifficultyCurve.hazardCooldown(2.4f, tension) < 2.4f);
    }
}
