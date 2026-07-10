package com.jtripppiie.mooserush;

/**
 * Pure, Android-free geometry and clamp helpers shared by the game view, kept
 * separate so the core math can be unit tested on the JVM.
 */
public final class GameMath {
    private GameMath() {
    }

    public static float clamp(float value, float min, float max) {
        // Keep value inside [min, max]. Great for screen bounds and timers.
        return Math.max(min, Math.min(max, value));
    }

    public static int clampInt(int value, int min, int max) {
        // Same idea as clamp(), but for whole numbers.
        return Math.max(min, Math.min(max, value));
    }

    public static boolean circleHitsCircle(float ax, float ay, float ar, float bx, float by, float br) {
        /*
         * Two circles overlap when the distance between centers is smaller than
         * both radii added together. We compare squared distance to avoid the
         * slower square-root operation.
         */
        float dx = ax - bx;
        float dy = ay - by;
        float radius = ar + br;
        return dx * dx + dy * dy < radius * radius;
    }

    public static boolean circleHitsRect(float cx, float cy, float radius,
                                         float left, float top, float right, float bottom) {
        /*
         * Find the closest point on the rectangle to the circle center. If that
         * point is inside the circle radius, the circle touches the rectangle.
         */
        float closestX = clamp(cx, left, right);
        float closestY = clamp(cy, top, bottom);
        float dx = cx - closestX;
        float dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }
}
