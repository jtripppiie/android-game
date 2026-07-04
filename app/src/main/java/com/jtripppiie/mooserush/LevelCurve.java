package com.jtripppiie.mooserush;

/**
 * Pure, Android-free level progression math so it can be unit tested on the JVM.
 */
public final class LevelCurve {
    public static final int[] LEVEL_XP = {0, 150, 400, 800, 1350, 2100, 3100, 4500, 6200, 8200};

    private LevelCurve() {
    }

    /** Returns the zero-based level index for the given total XP. */
    public static int levelIndex(int totalXp) {
        int index = 0;
        for (int i = 0; i < LEVEL_XP.length; i++) {
            if (totalXp >= LEVEL_XP[i]) {
                index = i;
            }
        }
        return Math.min(index, LEVEL_XP.length - 1);
    }

    /** XP required to reach the current level. */
    public static int currentFloor(int levelIndex) {
        return LEVEL_XP[clampIndex(levelIndex)];
    }

    /** XP required to reach the next level, or the max floor when maxed out. */
    public static int nextGoal(int levelIndex) {
        int i = clampIndex(levelIndex);
        return i >= LEVEL_XP.length - 1 ? LEVEL_XP[LEVEL_XP.length - 1] : LEVEL_XP[i + 1];
    }

    /** Progress toward the next level in the range [0, 1]. */
    public static float progress(int totalXp) {
        int level = levelIndex(totalXp);
        int floor = currentFloor(level);
        int goal = nextGoal(level);
        if (goal == floor) {
            return 1f;
        }
        float pct = (totalXp - floor) / (float) (goal - floor);
        return Math.max(0f, Math.min(1f, pct));
    }

    private static int clampIndex(int levelIndex) {
        return Math.max(0, Math.min(LEVEL_XP.length - 1, levelIndex));
    }
}
