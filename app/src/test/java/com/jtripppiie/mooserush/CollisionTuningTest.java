package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CollisionTuningTest {

    @Test
    public void hazardBodyHitboxGrowsDuringRoar() {
        assertEquals(0.74f, CollisionTuning.hazardRadiusScale(false), 0.0001f);
        assertEquals(0.92f, CollisionTuning.hazardRadiusScale(true), 0.0001f);
        assertTrue(CollisionTuning.hazardRadiusScale(true) > CollisionTuning.hazardRadiusScale(false));
    }

    @Test
    public void thrownSnowballsHaveForgivingWildlifeAndProjectileHits() {
        assertEquals(0.82f, CollisionTuning.hazardShotRadiusScale(false), 0.0001f);
        assertEquals(0.96f, CollisionTuning.hazardShotRadiusScale(true), 0.0001f);
        assertTrue(CollisionTuning.SHOT_BOSS_ATTACK_RADIUS_SCALE > 1f);
        assertTrue(CollisionTuning.BOSS_ATTACK_SHOT_RADIUS_SCALE > 1f);
        assertTrue(CollisionTuning.SHOT_LOG_RADIUS_SCALE > 1f);
    }

    @Test
    public void roaringHazardHitPointMovesTowardHead() {
        assertEquals(100f, CollisionTuning.hazardHitY(100f, 40f, false), 0.0001f);
        assertEquals(78f, CollisionTuning.hazardHitY(100f, 40f, true), 0.0001f);
    }

    @Test
    public void collectiblesRemainMoreForgivingThanBodyContact() {
        assertTrue(CollisionTuning.STAR_RADIUS_SCALE > 1f);
        assertTrue(CollisionTuning.POWERUP_RADIUS_SCALE > CollisionTuning.STAR_RADIUS_SCALE);
        assertTrue(CollisionTuning.POWERUP_PLAYER_RADIUS_SCALE > CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE);
        assertTrue(CollisionTuning.PLAYER_THIN_ICE_RADIUS_SCALE < CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE);
    }

    @Test
    public void gateContactUsesTighterPlayerRadiusThanWildlife() {
        assertTrue(CollisionTuning.PLAYER_GATE_RADIUS_SCALE < CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE);
        assertTrue(CollisionTuning.GATE_HIT_INSET_X_DP > 0f);
        assertTrue(CollisionTuning.GATE_HIT_TOP_INSET_DP > 0f);
    }
}
