package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrailBadgeCatalogTest {

    @Test
    public void badgeBitsAreStableAndCountable() {
        int badgeMask = (1 << 0) | (1 << 4) | (1 << 9);

        assertTrue(TrailBadgeCatalog.hasBadge(badgeMask, 0));
        assertTrue(TrailBadgeCatalog.hasBadge(badgeMask, 4));
        assertTrue(TrailBadgeCatalog.hasBadge(badgeMask, 9));
        assertFalse(TrailBadgeCatalog.hasBadge(badgeMask, 8));
        assertFalse(TrailBadgeCatalog.hasBadge(badgeMask, 99));
        assertEquals(3, TrailBadgeCatalog.badgeCount(badgeMask));
    }

    @Test
    public void newlyEarnedMaskIgnoresAlreadyUnlockedBadges() {
        int existing = (1 << 0) | (1 << 1);

        int earned = TrailBadgeCatalog.newlyEarnedMask(
                existing,
                120,
                1,
                1,
                6,
                1,
                2,
                0,
                false,
                false,
                false,
                0,
                0);

        assertEquals(0, earned);
    }

    @Test
    public void stageClearCanEarnSkillProgressAndPassportBadges() {
        int earned = TrailBadgeCatalog.newlyEarnedMask(
                0,
                900,
                4,
                4,
                10,
                5,
                12,
                3,
                true,
                true,
                true,
                2,
                1);

        assertTrue(TrailBadgeCatalog.hasBadge(earned, 0));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 1));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 2));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 3));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 4));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 5));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 6));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 7));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 8));
        assertTrue(TrailBadgeCatalog.hasBadge(earned, 9));
        assertEquals(10, TrailBadgeCatalog.badgeCount(earned));
        assertEquals(180, TrailBadgeCatalog.tokensForNewBadges(TrailBadgeCatalog.badgeCount(earned)));
    }

    @Test
    public void fallbackNameKeepsResultTextSafe() {
        assertEquals("First Trail", TrailBadgeCatalog.badgeName(0));
        assertEquals("Trail Badge", TrailBadgeCatalog.badgeName(99));
    }
}
