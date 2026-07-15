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
        // A later biome may start a little sharper, but it still needs a
        // readable opening beat. The previous 0.12/stage made stage five begin
        // at 25% extra speed before the player cleared a single obstacle.
        float stagePressure = Math.min(0.24f, Math.max(0, stageIndex) * 0.06f);
        // runPressure rises as you get closer to the boss.
        float runPressure = goalGates <= 0 ? 0f : Math.min(0.56f, Math.max(0, gatesPassed) / (float) goalGates * 0.56f);
        return Math.min(1f, stagePressure + runPressure);
    }

    static float speedMultiplier(float tension) {
        // Difficulty should come from decisions, not unreadable scroll speed.
        return 1f + Math.min(1f, Math.max(0f, tension)) * 0.38f;
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
