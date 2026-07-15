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
        float stagePressure = Math.min(0.48f, Math.max(0, stageIndex) * 0.12f);
        // runPressure rises as you get closer to the boss.
        float runPressure = goalGates <= 0 ? 0f : Math.min(0.72f, Math.max(0, gatesPassed) / (float) goalGates * 0.72f);
        return Math.min(1f, stagePressure + runPressure);
    }

    static float speedMultiplier(float tension) {
        // At full tension, speed is 52% higher than normal.
        return 1f + Math.min(1f, Math.max(0f, tension)) * 0.52f;
    }

    static float gateCooldown(float baseCooldown, float tension) {
        // Higher tension means less waiting between gates.
        return Math.max(0.92f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.38f));
    }

    static float hazardCooldown(float baseCooldown, float tension) {
        // Hazards ramp a little more than gates because they are stage flavor.
        return Math.max(1.08f, baseCooldown * (1f - Math.min(1f, Math.max(0f, tension)) * 0.48f));
    }
}
