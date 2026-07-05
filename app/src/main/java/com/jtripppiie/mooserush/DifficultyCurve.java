package com.jtripppiie.mooserush;

final class DifficultyCurve {
    private DifficultyCurve() {
    }

    static float tension(int stageIndex, int gatesPassed, int goalGates) {
        float stagePressure = Math.min(0.35f, Math.max(0, stageIndex) * 0.075f);
        float runPressure = goalGates <= 0 ? 0f : Math.min(0.65f, Math.max(0, gatesPassed) / (float) goalGates * 0.65f);
        return Math.min(1f, stagePressure + runPressure);
    }

    static float speedMultiplier(float tension) {
        return 1f + Math.min(1f, Math.max(0f, tension)) * 0.16f;
    }

    static float gateCooldown(float baseCooldown, float tension) {
        return Math.max(1.32f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.14f));
    }

    static float hazardCooldown(float baseCooldown, float tension) {
        return Math.max(1.52f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.22f));
    }
}
