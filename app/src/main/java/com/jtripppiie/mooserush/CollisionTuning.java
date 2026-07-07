package com.jtripppiie.mooserush;

final class CollisionTuning {
    static final float PLAYER_BOSS_CONTACT_RADIUS_SCALE = 0.76f;
    static final float PLAYER_BOSS_ATTACK_RADIUS_SCALE = 0.80f;
    static final float PLAYER_BOSS_LASER_RADIUS_SCALE = 0.74f;
    static final float PLAYER_THIN_ICE_RADIUS_SCALE = 0.62f;
    static final float PLAYER_HAZARD_RADIUS_SCALE = 0.82f;
    static final float PLAYER_NEAR_MISS_X_SCALE = 0.95f;

    static final float HAZARD_BODY_RADIUS_SCALE = 0.74f;
    static final float HAZARD_ROARING_RADIUS_SCALE = 0.92f;
    static final float HAZARD_SHOT_RADIUS_SCALE = 0.82f;
    static final float HAZARD_ROARING_SHOT_RADIUS_SCALE = 0.96f;
    static final float HAZARD_ROARING_HIT_Y_OFFSET_SCALE = 0.55f;

    static final float STAR_PLAYER_RADIUS_SCALE = 0.95f;
    static final float STAR_RADIUS_SCALE = 1.35f;
    static final float POWERUP_PLAYER_RADIUS_SCALE = 1.05f;
    static final float POWERUP_RADIUS_SCALE = 1.55f;

    static final float SHOT_BOSS_ATTACK_RADIUS_SCALE = 1.45f;
    static final float BOSS_ATTACK_SHOT_RADIUS_SCALE = 1.20f;
    static final float SHOT_LOG_RADIUS_SCALE = 1.55f;

    private CollisionTuning() {
    }

    static float hazardRadiusScale(boolean roaring) {
        return roaring ? HAZARD_ROARING_RADIUS_SCALE : HAZARD_BODY_RADIUS_SCALE;
    }

    static float hazardShotRadiusScale(boolean roaring) {
        return roaring ? HAZARD_ROARING_SHOT_RADIUS_SCALE : HAZARD_SHOT_RADIUS_SCALE;
    }

    static float hazardHitY(float y, float radius, boolean roaring) {
        return roaring ? y - radius * HAZARD_ROARING_HIT_Y_OFFSET_SCALE : y;
    }
}
