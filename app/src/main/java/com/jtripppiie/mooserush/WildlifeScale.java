package com.jtripppiie.mooserush;

/** One visual/gameplay size contract for every animated Alaska animal. */
final class WildlifeScale {
    private WildlifeScale() {
    }

    static boolean supports(String label) {
        return "MOOSE".equals(label) || "BEAR".equals(label) || "POLAR".equals(label)
                || "WOLF".equals(label) || "EAGLE".equals(label) || "DARK".equals(label)
                || "SALMON".equals(label);
    }

    static float collisionRadiusDp(String label) {
        if ("POLAR".equals(label)) return 36f;
        if ("MOOSE".equals(label)) return 34f;
        if ("BEAR".equals(label)) return 32f;
        if ("WOLF".equals(label)) return 19f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 16f;
        if ("SALMON".equals(label)) return 15f;
        return 15f;
    }

    static float visualHeightDp(String label) {
        if ("MOOSE".equals(label)) return 92f;
        if ("POLAR".equals(label)) return 88f;
        if ("BEAR".equals(label)) return 78f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 44f;
        if ("WOLF".equals(label)) return 42f;
        if ("SALMON".equals(label)) return 28f;
        return collisionRadiusDp(label) * 2f;
    }

    static float halfWidthDp(String label) {
        if ("MOOSE".equals(label)) return 78f;
        if ("POLAR".equals(label)) return 70f;
        if ("BEAR".equals(label)) return 62f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 40f;
        if ("WOLF".equals(label)) return 36f;
        if ("SALMON".equals(label)) return 30f;
        return collisionRadiusDp(label);
    }

    static float horizontalRadiusScale(String label) {
        return halfWidthDp(label) / collisionRadiusDp(label);
    }

    static float verticalRadiusScale(String label) {
        return visualHeightDp(label) * 0.5f / collisionRadiusDp(label);
    }
}
