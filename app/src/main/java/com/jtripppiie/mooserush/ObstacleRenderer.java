package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

final class ObstacleRenderer {
    private final GameAssets assets;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    ObstacleRenderer(Context context, GameAssets assets) {
        this.assets = assets;
        density = context.getResources().getDisplayMetrics().density;
    }

    boolean drawSpriteGate(Canvas canvas, float x, float height, float width, int stage, String label, float ground) {
        Drawable sprite = obstacleSpriteForStage(stage);
        if (sprite == null) {
            return false;
        }

        float spriteHeight = height + dp(stage == 2 ? 30 : stage == 3 ? 24 : 18);
        float spriteWidth = Math.max(width + dp(stage == 2 ? 54 : 42), spriteHeight * (stage == 2 ? 1.85f : 1.65f));
        float left = x + width * 0.5f - spriteWidth * 0.5f;
        float right = left + spriteWidth;
        float top = ground - spriteHeight;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(stage == 4 ? 96 : 112, 0, 0, 0));
        canvas.drawOval(left + spriteWidth * 0.08f, ground - dp(7), right - spriteWidth * 0.08f, ground + dp(8), paint);
        drawDrawable(canvas, sprite, left, top, right, ground + dp(2));
        drawNameplate(canvas, x, width, top, label);
        return true;
    }

    private Drawable obstacleSpriteForStage(int stage) {
        if (stage == 2) {
            return assets.obstacleAntlerBarricade();
        }
        if (stage == 3) {
            return assets.obstacleIceberg();
        }
        if (stage == 4) {
            return assets.obstacleSnowbank();
        }
        return null;
    }

    private void drawNameplate(Canvas canvas, float x, float obstacleWidth, float top, String label) {
        float plateWidth = Math.min(dp(128), Math.max(dp(54), label.length() * dp(5.8f)));
        float left = x + obstacleWidth / 2f - plateWidth / 2f;
        float plateTop = Math.max(dp(82), top - dp(22));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 8, 18, 30));
        canvas.drawRoundRect(left, plateTop, left + plateWidth, plateTop + dp(17), dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(180, 255, 218, 121));
        canvas.drawRoundRect(left, plateTop, left + plateWidth, plateTop + dp(17), dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(label.length() > 14 ? 7.2f : 8.2f));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText(label, x + obstacleWidth / 2f, plateTop + dp(12), textPaint);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, float left, float top, float right, float bottom) {
        drawable.setBounds(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
        drawable.draw(canvas);
    }

    private float dp(float value) {
        return value * density;
    }
}
