package com.jtripppiie.mooserush;

/*
 * CollisionTuning teaches a big game-dev trick:
 * the art and the hitbox do not have to be the same size.
 *
 * Players usually expect fair hitboxes. That often means the invisible hit
 * circle is smaller than the sprite, so a close dodge feels exciting instead of
 * cheap. These scale values multiply sprite radii in MooseRushView.
 */
final class CollisionTuning {
    // Player hit circles for different danger types.
    static final float PLAYER_BOSS_CONTACT_RADIUS_SCALE = 0.76f;
    static final float PLAYER_BOSS_ATTACK_RADIUS_SCALE = 0.80f;
    static final float PLAYER_BOSS_LASER_RADIUS_SCALE = 0.74f;
    static final float PLAYER_GATE_RADIUS_SCALE = 0.68f;
    static final float PLAYER_THIN_ICE_RADIUS_SCALE = 0.62f;
    static final float PLAYER_HAZARD_RADIUS_SCALE = 0.82f;
    // Near-miss checks use x distance so they can reward close passes.
    static final float PLAYER_NEAR_MISS_X_SCALE = 0.95f;
    // Gate rectangles are inset so the visible art can be a little forgiving.
    static final float GATE_HIT_INSET_X_DP = 4f;
    static final float GATE_HIT_TOP_INSET_DP = 6f;

    // Hazard hitboxes change when a hazard is roaring/large.
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
        // Roaring hazards look bigger, so their collision radius grows too.
        return roaring ? HAZARD_ROARING_RADIUS_SCALE : HAZARD_BODY_RADIUS_SCALE;
    }

    static float hazardShotRadiusScale(boolean roaring) {
        // Snowball shots should also respect the larger roaring pose.
        return roaring ? HAZARD_ROARING_SHOT_RADIUS_SCALE : HAZARD_SHOT_RADIUS_SCALE;
    }

    static float hazardHitY(float y, float radius, boolean roaring) {
        // Roaring sprites stand taller, so the hit center moves upward.
        return roaring ? y - radius * HAZARD_ROARING_HIT_Y_OFFSET_SCALE : y;
    }
}
