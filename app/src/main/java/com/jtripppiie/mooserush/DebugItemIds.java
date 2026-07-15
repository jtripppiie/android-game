package com.jtripppiie.mooserush;

import java.util.Locale;

/** Compact, stable labels used to discuss a specific object from a screenshot. */
final class DebugItemIds {
    private static final String[] STAGE_CODES = {"SUN", "RIV", "MOO", "DRK", "BER"};

    private DebugItemIds() {
    }

    static String format(int stage, String category, int sequence) {
        int safeStage = Math.max(0, Math.min(STAGE_CODES.length - 1, stage));
        String safeCategory = category == null ? "IT" : category.toUpperCase(Locale.ROOT);
        return STAGE_CODES[safeStage] + "-" + safeCategory + String.format(Locale.ROOT, "%02d", Math.max(1, sequence));
    }

    static String player(int stage) {
        int safeStage = Math.max(0, Math.min(STAGE_CODES.length - 1, stage));
        return STAGE_CODES[safeStage] + "-PL";
    }

    static String boss(int stage) {
        int safeStage = Math.max(0, Math.min(STAGE_CODES.length - 1, stage));
        return STAGE_CODES[safeStage] + "-BOSS";
    }
}
