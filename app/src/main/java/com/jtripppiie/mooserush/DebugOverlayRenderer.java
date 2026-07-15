package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/*
 * DebugOverlayRenderer draws learning/debug helpers on top of the game.
 *
 * Hitboxes are invisible during normal play. The overlay makes them visible so
 * a developer can answer, "Did I lose because the art touched me, or because
 * the collision shape touched me?"
 */
final class DebugOverlayRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();
    private final float density;

    DebugOverlayRenderer(Context context) {
        density = context.getResources().getDisplayMetrics().density;
    }

    void drawGroundLine(Canvas canvas, float width, float ground) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(Color.argb(190, 255, 218, 121));
        canvas.drawLine(0, ground, width, ground, paint);
        paint.setStyle(Paint.Style.FILL);
        drawObjectBadge(canvas, width, canvas.getHeight(), "GROUND", "", dp(42), ground - dp(8), Color.rgb(255, 218, 121));
    }

    void drawCircle(Canvas canvas, float x, float y, float radius, int color) {
        // Circle overlays are used for player, hazards, pickups, and attacks.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(36, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.1f));
        paint.setColor(Color.argb(168, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    void drawRect(Canvas canvas, RectF rect, int color) {
        // Rect overlays are used for gates, lasers, and other box-shaped danger.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(32, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawRect(rect, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.1f));
        paint.setColor(Color.argb(168, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawRect(rect, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    void drawObjectBadge(Canvas canvas, float viewWidth, float viewHeight, String id, String detail, float x, float y, int accentColor) {
        // Badges are clamped so labels stay on screen even near the edges.
        String label = id == null || id.length() == 0 ? "ITEM" : id;
        float clampedX = clamp(x, dp(18), viewWidth - dp(18));
        float clampedY = clamp(y, dp(54), viewHeight - dp(18));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9.5f));
        textPaint.setFakeBoldText(true);
        float width = Math.max(dp(26), textPaint.measureText(label) + dp(10));
        boolean hasDetail = detail != null && detail.length() > 0;
        if (hasDetail) {
            textPaint.setTextSize(dp(7.2f));
            textPaint.setFakeBoldText(false);
            width = Math.max(width, textPaint.measureText(detail) + dp(10));
            textPaint.setTextSize(dp(9.5f));
            textPaint.setFakeBoldText(true);
        }
        float height = hasDetail ? dp(29) : dp(18);
        tempRect.set(clampedX - width * 0.5f, clampedY - height + dp(5), clampedX + width * 0.5f, clampedY + dp(5));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(218, 8, 18, 30));
        canvas.drawRoundRect(tempRect, dp(7), dp(7), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(accentColor);
        canvas.drawRoundRect(tempRect, dp(7), dp(7), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, clampedX, clampedY, textPaint);
        textPaint.setFakeBoldText(false);
        if (hasDetail) {
            textPaint.setTextSize(dp(7.2f));
            textPaint.setColor(Color.rgb(255, 246, 207));
            canvas.drawText(detail, clampedX, clampedY + dp(10), textPaint);
        }
    }

    private float dp(float value) {
        return value * density;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
