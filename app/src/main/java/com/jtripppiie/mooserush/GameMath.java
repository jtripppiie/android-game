package com.jtripppiie.mooserush;

/**
 * Pure, Android-free geometry and clamp helpers shared by the game view, kept
 * separate so the core math can be unit tested on the JVM.
 */
public final class GameMath {
    private GameMath() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean circleHitsCircle(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx;
        float dy = ay - by;
        float radius = ar + br;
        return dx * dx + dy * dy < radius * radius;
    }

    public static boolean circleHitsRect(float cx, float cy, float radius,
                                         float left, float top, float right, float bottom) {
        float closestX = clamp(cx, left, right);
        float closestY = clamp(cy, top, bottom);
        float dx = cx - closestX;
        float dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }
}
