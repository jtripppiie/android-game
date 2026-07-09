package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
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

    @Test
    public void runnerSheetTrimPreservesVisibleFrameBounds() {
        int[] source = SpriteRenderer.trimmedRunnerSourceValues(
                1,
                328,
                798,
                new int[]{0, 207, 328, 638}
        );

        assertArrayEquals(new int[]{328, 207, 656, 638}, source);
    }

    @Test
    public void fullRunnerFramesDistributeOddSheetWidth() {
        assertEquals(0, SpriteRenderer.fullRunnerFrameLeft(2027, 0));
        assertEquals(338, SpriteRenderer.fullRunnerFrameRight(2027, 0));
        assertEquals(1689, SpriteRenderer.fullRunnerFrameLeft(2027, 5));
        assertEquals(2027, SpriteRenderer.fullRunnerFrameRight(2027, 5));
    }

    @Test
    public void fullRunnerSourceGuardsInternalFrameSeams() {
        assertArrayEquals(new int[]{0, 0, 324, 776}, SpriteRenderer.fullRunnerSourceValues(2027, 776, 0));
        assertArrayEquals(new int[]{352, 0, 662, 776}, SpriteRenderer.fullRunnerSourceValues(2027, 776, 1));
        assertArrayEquals(new int[]{1703, 0, 2027, 776}, SpriteRenderer.fullRunnerSourceValues(2027, 776, 5));
    }
}
