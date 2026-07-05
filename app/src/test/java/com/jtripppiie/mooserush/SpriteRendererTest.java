package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SpriteRendererTest {

    @Test
    public void runnerSheetUsesOneSixFrameCyclePerClockUnit() {
        assertEquals(0, SpriteRenderer.runnerSheetFrame(0f));
        assertEquals(1, SpriteRenderer.runnerSheetFrame(0.17f));
        assertEquals(3, SpriteRenderer.runnerSheetFrame(0.50f));
        assertEquals(5, SpriteRenderer.runnerSheetFrame(0.99f));
        assertEquals(0, SpriteRenderer.runnerSheetFrame(1.01f));
    }

    @Test
    public void runnerSheetBodyIsLargeEnoughForGameplayReadability() {
        float radius = 10f;
        float runningHeight = SpriteRenderer.runnerSheetBodyHeight(radius, true);
        float standingHeight = SpriteRenderer.runnerSheetBodyHeight(radius, false);

        assertEquals(26.8f, runningHeight, 0.0001f);
        assertEquals(26.2f, standingHeight, 0.0001f);
        assertTrue(runningHeight > radius * 2.6f);
        assertTrue(runningHeight < radius * 3.0f);
    }
}
