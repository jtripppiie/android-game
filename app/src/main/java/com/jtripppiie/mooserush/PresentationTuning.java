package com.jtripppiie.mooserush;

final class PresentationTuning {
    private PresentationTuning() {}

    static float runnerMaxX(float width, float radius, boolean bossActive) {
        float laneLimit = width * (bossActive ? 0.72f : 0.58f);
        return Math.max(radius, Math.min(width - radius, laneLimit));
    }

    static int worldFlashAlpha(float strength) {
        return Math.min(92, Math.max(0, Math.round(92f * strength / 0.24f)));
    }

    static int blizzardAlpha(float pulse) {
        return Math.round(28f + 14f * Math.max(0f, Math.min(1f, pulse)));
    }

    static float visualSquashY(float value) {
        return Math.max(0.88f, Math.min(1.12f, value));
    }
}
