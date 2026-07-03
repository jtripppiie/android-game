package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Adds near-miss reward feedback on top of the Alaska survival prototype.
 *
 * This rewards close dodges so normal play feels more exciting even before
 * the player reaches a boss phase.
 */
public class AlaskaNearMissMooseRushView extends AlaskaSurvivalMooseRushView {
    private static final String TAG = "YouRushNearMiss";
    private static final float NEAR_MISS_RANGE_DP = 44f;
    private static final float POPUP_LIFETIME_SECONDS = 0.85f;
    private static final int NEAR_MISS_SCORE = 3;

    private final Paint nearMissPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<NearMissPopup> nearMissPopups = new ArrayList<>();
    private final IdentityHashMap<Object, Boolean> creditedHazards = new IdentityHashMap<>();

    private Field playerXField;
    private Field playerYField;
    private Field playerRadiusField;
    private Field hazardsField;
    private Field scoreField;
    private Field runStageScoreField;
    private Field stateField;

    private long lastNearMissFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean bindingWarningLogged = false;

    public AlaskaNearMissMooseRushView(Context context) {
        super(context);
        bindNearMissFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeNearMissDeltaSeconds();
        updateNearMissPopups(dt);

        super.onDraw(canvas);

        detectNearMisses();
        drawNearMissPopups(canvas);

        if (!nearMissPopups.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    private void bindNearMissFields() {
        try {
            Class<?> core = MooseRushView.class;
            playerXField = core.getDeclaredField("playerX");
            playerYField = core.getDeclaredField("playerY");
            playerRadiusField = core.getDeclaredField("playerRadius");
            hazardsField = core.getDeclaredField("hazards");
            scoreField = core.getDeclaredField("score");
            runStageScoreField = core.getDeclaredField("runStageScore");
            stateField = core.getDeclaredField("state");

            playerXField.setAccessible(true);
            playerYField.setAccessible(true);
            playerRadiusField.setAccessible(true);
            hazardsField.setAccessible(true);
            scoreField.setAccessible(true);
            runStageScoreField.setAccessible(true);
            stateField.setAccessible(true);

            bindingReady = true;
            Log.d(TAG, "Near-miss field binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Near-miss binding failed; game continues without near-miss bonus.", exception);
        }
    }

    private float computeNearMissDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastNearMissFrameNanos != 0L) {
            dt = Math.min((now - lastNearMissFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastNearMissFrameNanos = now;
        return dt;
    }

    private void updateNearMissPopups(float dt) {
        Iterator<NearMissPopup> iterator = nearMissPopups.iterator();
        while (iterator.hasNext()) {
            NearMissPopup popup = iterator.next();
            popup.age += dt;
            popup.y -= dp(26) * dt;
            if (popup.age >= POPUP_LIFETIME_SECONDS) {
                iterator.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void detectNearMisses() {
        if (!bindingReady) {
            return;
        }

        try {
            if (stateField.getInt(this) != 4) {
                return;
            }

            float playerX = playerXField.getFloat(this);
            float playerY = playerYField.getFloat(this);
            float playerRadius = playerRadiusField.getFloat(this);
            List<Object> hazards = (List<Object>) hazardsField.get(this);
            if (hazards == null || hazards.isEmpty()) {
                creditedHazards.clear();
                return;
            }

            for (Object hazard : hazards) {
                if (creditedHazards.containsKey(hazard)) {
                    continue;
                }

                float hazardX = getFloatField(hazard, "x");
                float hazardY = getFloatField(hazard, "y");
                float hazardRadius = getFloatField(hazard, "radius");
                float distance = distance(playerX, playerY, hazardX, hazardY);
                float collisionRadius = playerRadius * 0.82f + hazardRadius * 0.74f;
                float nearMissRadius = collisionRadius + dp(NEAR_MISS_RANGE_DP);
                boolean closeButSafe = distance > collisionRadius && distance <= nearMissRadius;
                boolean inScoringWindow = hazardX < playerX + dp(52) && hazardX > playerX - dp(132);

                if (closeButSafe && inScoringWindow) {
                    creditedHazards.put(hazard, Boolean.TRUE);
                    addNearMissScore();
                    nearMissPopups.add(new NearMissPopup(playerX, playerY - playerRadius - dp(12), "+3 NEAR MISS"));
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    Log.d(TAG, "Near miss bonus awarded.");
                }
            }

            if (creditedHazards.size() > hazards.size() + 8) {
                creditedHazards.clear();
            }
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException exception) {
            if (!bindingWarningLogged) {
                bindingWarningLogged = true;
                Log.w(TAG, "Near-miss detection unavailable; game continues.", exception);
            }
        }
    }

    private void addNearMissScore() throws IllegalAccessException {
        scoreField.setInt(this, scoreField.getInt(this) + NEAR_MISS_SCORE);
        runStageScoreField.setInt(this, runStageScoreField.getInt(this) + NEAR_MISS_SCORE);
    }

    private float getFloatField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(target);
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void drawNearMissPopups(Canvas canvas) {
        nearMissPaint.setTextAlign(Paint.Align.CENTER);
        nearMissPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        nearMissPaint.setTextSize(dp(13));
        for (NearMissPopup popup : nearMissPopups) {
            float pct = Math.max(0f, Math.min(1f, popup.age / POPUP_LIFETIME_SECONDS));
            int alpha = Math.round((1f - pct) * 230f);
            nearMissPaint.setColor(Color.argb(alpha, 255, 218, 121));
            canvas.drawText(popup.text, popup.x, popup.y, nearMissPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class NearMissPopup {
        final float x;
        float y;
        final String text;
        float age = 0f;

        NearMissPopup(float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }
}
