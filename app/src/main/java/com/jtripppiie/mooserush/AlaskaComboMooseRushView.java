package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;

/**
 * Adds combo-streak feedback on top of the Alaska gameplay layers.
 *
 * Any score gain can extend the combo. Fast repeated scoring events keep the
 * streak alive and every third combo grants a small bonus.
 */
public class AlaskaComboMooseRushView extends AlaskaNearMissMooseRushView {
    private static final String TAG = "YouRushCombo";
    private static final float COMBO_WINDOW_SECONDS = 2.15f;
    private static final float COMBO_POP_SECONDS = 1.05f;
    private static final int COMBO_BONUS_EVERY = 3;
    private static final int COMBO_BONUS_SCORE = 5;

    private final Paint comboPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Field scoreField;
    private Field runStageScoreField;
    private Field stateField;

    private long lastComboFrameNanos = 0L;
    private int lastObservedScore = 0;
    private int comboCount = 0;
    private float comboTimer = 0f;
    private float comboPopTimer = 0f;
    private boolean bindingReady = false;
    private boolean warningLogged = false;

    public AlaskaComboMooseRushView(Context context) {
        super(context);
        bindComboFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeComboDeltaSeconds();
        updateComboTimers(dt);

        super.onDraw(canvas);

        detectScoreGain();
        drawComboHud(canvas);

        if (comboTimer > 0f || comboPopTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindComboFields() {
        try {
            Class<?> core = MooseRushView.class;
            scoreField = core.getDeclaredField("score");
            runStageScoreField = core.getDeclaredField("runStageScore");
            stateField = core.getDeclaredField("state");
            scoreField.setAccessible(true);
            runStageScoreField.setAccessible(true);
            stateField.setAccessible(true);
            bindingReady = true;
            lastObservedScore = scoreField.getInt(this);
            Log.d(TAG, "Combo field binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Combo binding failed; game continues without combo HUD.", exception);
        }
    }

    private float computeComboDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastComboFrameNanos != 0L) {
            dt = Math.min((now - lastComboFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastComboFrameNanos = now;
        return dt;
    }

    private void updateComboTimers(float dt) {
        if (comboTimer > 0f) {
            comboTimer = Math.max(0f, comboTimer - dt);
            if (comboTimer == 0f) {
                comboCount = 0;
            }
        }
        comboPopTimer = Math.max(0f, comboPopTimer - dt);
    }

    private void detectScoreGain() {
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            int score = scoreField.getInt(this);
            if (state != 4) {
                lastObservedScore = score;
                comboCount = 0;
                comboTimer = 0f;
                return;
            }

            if (score > lastObservedScore) {
                int gained = score - lastObservedScore;
                comboCount = comboTimer > 0f ? comboCount + 1 : 1;
                comboTimer = COMBO_WINDOW_SECONDS;
                comboPopTimer = COMBO_POP_SECONDS;
                Log.d(TAG, "Combo score gain: +" + gained + " combo x" + comboCount);

                if (comboCount > 0 && comboCount % COMBO_BONUS_EVERY == 0) {
                    score += COMBO_BONUS_SCORE;
                    scoreField.setInt(this, score);
                    runStageScoreField.setInt(this, runStageScoreField.getInt(this) + COMBO_BONUS_SCORE);
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    Log.d(TAG, "Combo bonus awarded: +" + COMBO_BONUS_SCORE);
                }

                lastObservedScore = score;
            } else if (score < lastObservedScore) {
                lastObservedScore = score;
            }
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Combo detection unavailable; game continues.", exception);
            }
        }
    }

    private void drawComboHud(Canvas canvas) {
        if (comboCount < 2 || comboTimer <= 0f) {
            return;
        }

        float pct = comboTimer / COMBO_WINDOW_SECONDS;
        float popScale = comboPopTimer > 0f ? 1f + 0.18f * (comboPopTimer / COMBO_POP_SECONDS) : 1f;
        float centerX = getWidth() / 2f;
        float top = dp(96);
        float barWidth = Math.min(getWidth() - dp(72), dp(240));
        float barHeight = dp(8);

        comboPaint.setStyle(Paint.Style.FILL);
        comboPaint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRoundRect(centerX - barWidth / 2f, top, centerX + barWidth / 2f, top + barHeight, dp(5), dp(5), comboPaint);

        comboPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(centerX - barWidth / 2f, top, centerX - barWidth / 2f + barWidth * pct, top + barHeight, dp(5), dp(5), comboPaint);

        comboPaint.setTextAlign(Paint.Align.CENTER);
        comboPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        comboPaint.setTextSize(dp(18) * popScale);
        comboPaint.setColor(Color.WHITE);
        canvas.drawText("COMBO x" + comboCount, centerX, top - dp(9), comboPaint);

        if (comboCount % COMBO_BONUS_EVERY == 0 && comboPopTimer > 0f) {
            comboPaint.setTextSize(dp(12));
            comboPaint.setColor(Color.rgb(255, 218, 121));
            canvas.drawText("+" + COMBO_BONUS_SCORE + " BONUS", centerX, top + dp(28), comboPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
