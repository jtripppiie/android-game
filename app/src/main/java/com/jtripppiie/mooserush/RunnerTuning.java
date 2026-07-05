package com.jtripppiie.mooserush;

final class RunnerTuning {
    static final float COYOTE_SECONDS = 0.08f;
    static final float JUMP_BUFFER_SECONDS = 0.10f;
    static final float GROUND_JUMP_VELOCITY_DP = 620f;
    static final float DOUBLE_JUMP_VELOCITY_DP = 540f;
    static final float GRAVITY_DP = 1550f;
    static final float DARKNESS_GRAVITY_DP = 1625f;
    static final float MIN_GATE_COOLDOWN_SECONDS = 1.45f;
    static final float MIN_HAZARD_COOLDOWN_SECONDS = 1.95f;

    private RunnerTuning() {
    }

    static float nextGateCooldown(float stageSpawnSeconds, int gatesPassed) {
        return Math.max(MIN_GATE_COOLDOWN_SECONDS, stageSpawnSeconds - gatesPassed * 0.006f);
    }

    static float nextHazardCooldown(int selectedStage, int gatesPassed) {
        return Math.max(MIN_HAZARD_COOLDOWN_SECONDS, 2.85f - selectedStage * 0.12f - gatesPassed * 0.008f);
    }

    static float scrollSpeedDp(float baseSpeedDp, int gatesPassed) {
        return baseSpeedDp + Math.min(42f, gatesPassed * 3.5f);
    }

    static float gateHeight(float density, int selectedStage, int gatesPassed, float random01) {
        float base = 24f * density;
        float range = (28f + Math.min(10f, gatesPassed * 0.4f) + selectedStage * 1f) * density;
        return base + random01 * range;
    }
}
