package com.jtripppiie.mooserush;

final class SprayTuning {
    static final int MAX_CHARGES = 3;
    static final float HOLD_SECONDS = 0.36f;
    static final float COOLDOWN_SECONDS = 1.45f;
    static final float CONE_SECONDS = 0.34f;
    static final float RANGE_DP = 235f;
    static final float HALF_HEIGHT_DP = 76f;
    static final float BASE_HALF_HEIGHT_DP = 24f;
    static final float BACK_REACH_RADIUS_SCALE = 0.35f;
    static final float HIT_RADIUS_Y_SCALE = 0.65f;

    private SprayTuning() {
    }

    static boolean coneHitsPoint(float originX, float originY, float targetX, float targetY, float targetRadius, float density) {
        float range = RANGE_DP * density;
        float dx = targetX - originX;
        if (dx < -targetRadius * BACK_REACH_RADIUS_SCALE || dx > range + targetRadius) {
            return false;
        }

        float widthAtX = BASE_HALF_HEIGHT_DP * density + (dx / Math.max(1f, range)) * HALF_HEIGHT_DP * density;
        return Math.abs(targetY - originY) <= widthAtX + targetRadius * HIT_RADIUS_Y_SCALE;
    }

    static float effectRange(float density, float pct) {
        return RANGE_DP * density * (0.82f + 0.18f * pct);
    }

    static float effectHalfHeight(float density, float pct) {
        return HALF_HEIGHT_DP * density * (0.72f + 0.28f * pct);
    }

    static float spawnChance(int stage) {
        if (stage >= 4) return 0.22f;
        if (stage >= 2) return 0.14f;
        return 0.08f;
    }
}
