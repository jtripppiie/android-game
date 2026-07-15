package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DebugItemIdsTest {
    @Test
    public void idsAreCompactStableAndStageSpecific() {
        assertEquals("SUN-OB01", DebugItemIds.format(0, "ob", 1));
        assertEquals("RIV-AN12", DebugItemIds.format(1, "AN", 12));
        assertEquals("BER-PF03", DebugItemIds.format(4, "pf", 3));
        assertEquals("MOO-PL", DebugItemIds.player(2));
        assertEquals("DRK-BOSS", DebugItemIds.boss(3));
    }
}
