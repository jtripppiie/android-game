package com.jtripppiie.mooserush;

/*
 * RunnerTuning is the easiest place to learn "game feel."
 *
 * Nothing in this file draws pictures. These numbers are knobs used by the
 * main game loop. Small changes here can make the runner feel floaty, heavy,
 * easy, hard, fast, or unfair.
 *
 * Naming guide:
 * - values ending in SECONDS are time windows
 * - values ending in DP are distances/speeds before Android converts to pixels
 * - cooldown means "how long to wait before spawning the next thing"
 */
final class RunnerTuning {
    // Forgiveness after walking off a ledge. Higher = easier late jumps.
    static final float COYOTE_SECONDS = 0.10f;
    // Forgiveness before landing. Higher = button presses are remembered longer.
    static final float JUMP_BUFFER_SECONDS = 0.12f;
    // First jump strength. Higher = taller jump.
    static final float GROUND_JUMP_VELOCITY_DP = 585f;
    // Second jump strength. Usually lower so double jump helps but is not wild.
    static final float DOUBLE_JUMP_VELOCITY_DP = 560f;
    // Pull downward each second. Higher = runner falls faster.
    static final float GRAVITY_DP = 1600f;
    // Darkness is a little heavier so that stage feels more dangerous.
    static final float DARKNESS_GRAVITY_DP = 1685f;
    // Spawn caps stop the game from becoming impossible spam.
    static final float MIN_GATE_COOLDOWN_SECONDS = 1.05f;
    static final float MIN_HAZARD_COOLDOWN_SECONDS = 1.28f;
    static final float READABLE_GATE_COOLDOWN_SECONDS = 1.20f;
    static final float READABLE_FLOW_GATE_COOLDOWN_SECONDS = 1.05f;
    static final float READABLE_HAZARD_COOLDOWN_SECONDS = 1.45f;
    static final float READABLE_FLOW_HAZARD_COOLDOWN_SECONDS = 1.20f;

    private RunnerTuning() {
    }

    static float nextGateCooldown(float stageSpawnSeconds, int gatesPassed) {
        // Gates appear slightly faster as the run goes on, but never below cap.
        return Math.max(MIN_GATE_COOLDOWN_SECONDS, stageSpawnSeconds - gatesPassed * 0.035f);
    }

    static float nextHazardCooldown(int selectedStage, int gatesPassed) {
        // Later stages and longer runs both ask hazards to show up sooner.
        return Math.max(MIN_HAZARD_COOLDOWN_SECONDS, 2.35f - selectedStage * 0.14f - gatesPassed * 0.035f);
    }

    static float scrollSpeedDp(float baseSpeedDp, int gatesPassed) {
        // Keep this hidden ramp subordinate to the visible difficulty curve.
        return baseSpeedDp + Math.min(36f, gatesPassed * 3.0f);
    }

    static float capScrollSpeedDp(float speedDp, float stageBaseSpeedDp) {
        return Math.min(speedDp, stageBaseSpeedDp * 1.72f);
    }

    static float readableGateCooldown(float proposedSeconds, boolean flowActive) {
        return Math.max(flowActive ? READABLE_FLOW_GATE_COOLDOWN_SECONDS
                : READABLE_GATE_COOLDOWN_SECONDS, proposedSeconds);
    }

    static float readableHazardCooldown(float proposedSeconds, boolean flowActive) {
        return Math.max(flowActive ? READABLE_FLOW_HAZARD_COOLDOWN_SECONDS
                : READABLE_HAZARD_COOLDOWN_SECONDS, proposedSeconds);
    }

    static float gateHeight(float density, int selectedStage, int gatesPassed, float random01) {
        /*
         * random01 is a random number from 0 to 1. Multiplying by range turns it
         * into a random obstacle height. density converts dp-style sizes into
         * real pixels because this method is used directly for obstacle height.
         */
        if (selectedStage == 3) {
            float iceBase = 21f * density;
            float iceRange = (20f + Math.min(5f, gatesPassed * 0.18f)) * density;
            return iceBase + random01 * iceRange;
        }
        if (selectedStage >= 4) {
            float snowBase = 14f * density;
            float snowRange = (10f + Math.min(3f, gatesPassed * 0.10f)) * density;
            return snowBase + random01 * snowRange;
        }
        float base = 24f * density;
        float range = (28f + Math.min(10f, gatesPassed * 0.4f) + selectedStage * 1f) * density;
        return base + random01 * range;
    }
}
