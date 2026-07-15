package com.jtripppiie.mooserush;

/*
 * DifficultyCurve turns progress into pressure.
 *
 * A beginner mistake is making difficulty jump suddenly. This class keeps the
 * changes smooth: later stages add some pressure, and passing more gates adds
 * more. The result is a number from 0 to 1 called tension.
 */
final class DifficultyCurve {
    private DifficultyCurve() {
    }

    static float tension(int stageIndex, int gatesPassed, int goalGates) {
        // stagePressure makes later stages start a little harder.
        float stagePressure = Math.min(0.35f, Math.max(0, stageIndex) * 0.075f);
        // runPressure rises as you get closer to the boss.
        float runPressure = goalGates <= 0 ? 0f : Math.min(0.65f, Math.max(0, gatesPassed) / (float) goalGates * 0.65f);
        return Math.min(1f, stagePressure + runPressure);
    }

    static float speedMultiplier(float tension) {
        // At full tension, speed is 28% higher than normal.
        return 1f + Math.min(1f, Math.max(0f, tension)) * 0.28f;
    }

    static float gateCooldown(float baseCooldown, float tension) {
        // Higher tension means less waiting between gates.
        return Math.max(1.32f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.22f));
    }

    static float hazardCooldown(float baseCooldown, float tension) {
        // Hazards ramp a little more than gates because they are stage flavor.
        return Math.max(1.52f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.32f));
    }
}
