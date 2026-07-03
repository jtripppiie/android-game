package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Adds a thin game-feel layer over MooseRushView without changing core gameplay.
 *
 * This is intentionally visual/tactile polish only:
 * - tap ripples
 * - tiny screen jolt on input
 * - haptic tap feedback when available
 *
 * Keeping this separate makes the giant Canvas game easier to debug.
 */
public class JuicyMooseRushView extends MooseRushView {
    private static final float RIPPLE_LIFETIME_SECONDS = 0.38f;
    private static final float SHAKE_LIFETIME_SECONDS = 0.10f;

    private final Paint polishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<TouchRipple> ripples = new ArrayList<>();
    private final Random random = new Random();

    private long lastPolishFrameNanos = 0L;
    private float shakeTimer = 0f;
    private float shakeX = 0f;
    private float shakeY = 0f;

    public JuicyMooseRushView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastPolishFrameNanos != 0L) {
            dt = Math.min((now - lastPolishFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastPolishFrameNanos = now;
        updatePolish(dt);

        canvas.save();
        canvas.translate(shakeX, shakeY);
        super.onDraw(canvas);
        canvas.restore();

        drawRipples(canvas);

        if (shakeTimer > 0f || !ripples.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int index = event.getActionIndex();
            addTapFeedback(event.getX(index), event.getY(index));
        }
        return super.onTouchEvent(event);
    }

    private void addTapFeedback(float x, float y) {
        ripples.add(new TouchRipple(x, y));
        shakeTimer = SHAKE_LIFETIME_SECONDS;
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        postInvalidateOnAnimation();
    }

    private void updatePolish(float dt) {
        if (shakeTimer > 0f) {
            shakeTimer = Math.max(0f, shakeTimer - dt);
            float strength = shakeTimer / SHAKE_LIFETIME_SECONDS;
            float max = dpFloat(2.6f) * strength;
            shakeX = (random.nextFloat() - 0.5f) * max;
            shakeY = (random.nextFloat() - 0.5f) * max;
        } else {
            shakeX = 0f;
            shakeY = 0f;
        }

        Iterator<TouchRipple> iterator = ripples.iterator();
        while (iterator.hasNext()) {
            TouchRipple ripple = iterator.next();
            ripple.age += dt;
            if (ripple.age >= RIPPLE_LIFETIME_SECONDS) {
                iterator.remove();
            }
        }
    }

    private void drawRipples(Canvas canvas) {
        for (TouchRipple ripple : ripples) {
            float pct = Math.max(0f, Math.min(1f, ripple.age / RIPPLE_LIFETIME_SECONDS));
            float radius = dpFloat(12f) + pct * dpFloat(38f);
            int alpha = Math.round((1f - pct) * 145f);

            polishPaint.setStyle(Paint.Style.STROKE);
            polishPaint.setStrokeWidth(dpFloat(2.2f));
            polishPaint.setColor(Color.argb(alpha, 255, 218, 121));
            canvas.drawCircle(ripple.x, ripple.y, radius, polishPaint);

            polishPaint.setStyle(Paint.Style.FILL);
            polishPaint.setColor(Color.argb(Math.round(alpha * 0.35f), 255, 255, 255));
            canvas.drawCircle(ripple.x, ripple.y, radius * 0.36f, polishPaint);
        }
    }

    private float dpFloat(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class TouchRipple {
        final float x;
        final float y;
        float age = 0f;

        TouchRipple(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
