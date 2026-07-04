package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;

/** Adds persistent medal awards based on the run grade. */
public class AlaskaAwardMooseRushView extends AlaskaLevelMooseRushView {
    private static final String TAG = "YouRushAwards";
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_BRONZE = "medal_bronze_total";
    private static final String PREF_SILVER = "medal_silver_total";
    private static final String PREF_GOLD = "medal_gold_total";
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;
    private static final float BANNER_SECONDS = 1.6f;

    private final Paint awardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF awardPanel = new RectF();

    private SharedPreferences prefs;
    private Field stateField;
    private Field gradeRankField;
    private Field gradeTextField;

    private boolean ready = false;
    private boolean warningLogged = false;
    private int lastState = -1;
    private int bronzeTotal = 0;
    private int silverTotal = 0;
    private int goldTotal = 0;
    private int lastAwardRank = 0;
    private float bannerTimer = 0f;
    private long lastFrameNanos = 0L;
    private String awardText = "";
    private String awardSubtext = "";

    public AlaskaAwardMooseRushView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bronzeTotal = prefs.getInt(PREF_BRONZE, 0);
        silverTotal = prefs.getInt(PREF_SILVER, 0);
        goldTotal = prefs.getInt(PREF_GOLD, 0);
        bindFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float dt = frameDelta();
        updateAwardState(dt);
        drawAwardBanner(canvas);
        drawAwardTotals(canvas);
        if (bannerTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            stateField.setAccessible(true);
            gradeRankField = AlaskaRunSummaryMooseRushView.class.getDeclaredField("finalGradeRank");
            gradeTextField = AlaskaRunSummaryMooseRushView.class.getDeclaredField("finalGrade");
            gradeRankField.setAccessible(true);
            gradeTextField.setAccessible(true);
            ready = true;
            lastState = stateField.getInt(this);
            Log.d(TAG, "Award binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            ready = false;
            Log.w(TAG, "Award layer disabled.", exception);
        }
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastFrameNanos != 0L) {
            dt = Math.min((now - lastFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastFrameNanos = now;
        return dt;
    }

    private void updateAwardState(float dt) {
        bannerTimer = Math.max(0f, bannerTimer - dt);
        if (!ready) {
            return;
        }
        try {
            int state = stateField.getInt(this);
            if ((state == STATE_GAME_OVER || state == STATE_STAGE_CLEAR) && lastState == STATE_RUNNING) {
                awardRun();
            }
            lastState = state;
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Award update unavailable.", exception);
            }
        }
    }

    private void awardRun() throws IllegalAccessException {
        int gradeRank = gradeRankField.getInt(this);
        String grade = String.valueOf(gradeTextField.get(this));
        lastAwardRank = gradeRank;
        if (gradeRank >= 5) {
            goldTotal++;
            awardText = "GOLD MEDAL";
        } else if (gradeRank >= 4) {
            silverTotal++;
            awardText = "SILVER MEDAL";
        } else if (gradeRank >= 3) {
            bronzeTotal++;
            awardText = "BRONZE MEDAL";
        } else {
            awardText = "KEEP PUSHING";
        }
        awardSubtext = "Grade " + grade + " · G " + goldTotal + "  S " + silverTotal + "  B " + bronzeTotal;
        if (gradeRank >= 3) {
            prefs.edit()
                    .putInt(PREF_GOLD, goldTotal)
                    .putInt(PREF_SILVER, silverTotal)
                    .putInt(PREF_BRONZE, bronzeTotal)
                    .apply();
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        bannerTimer = BANNER_SECONDS;
        Log.d(TAG, "Award result: " + awardText);
    }

    private void drawAwardBanner(Canvas canvas) {
        if (bannerTimer <= 0f || awardText == null || awardText.isEmpty()) {
            return;
        }
        float pct = bannerTimer / BANNER_SECONDS;
        int alpha = Math.round(230 * pct);
        awardPaint.setTextAlign(Paint.Align.CENTER);
        awardPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        awardPaint.setTextSize(dp(21));
        awardPaint.setColor(Color.argb(alpha, medalRed(), medalGreen(), medalBlue()));
        canvas.drawText(awardText, getWidth() / 2f, getHeight() * 0.49f, awardPaint);
        awardPaint.setTextSize(dp(12));
        awardPaint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawText(awardSubtext, getWidth() / 2f, getHeight() * 0.49f + dp(24), awardPaint);
    }

    private void drawAwardTotals(Canvas canvas) {
        if (!ready || !isSummaryState()) {
            return;
        }
        float width = Math.min(dp(220), getWidth() - dp(32));
        float left = (getWidth() - width) / 2f;
        float top = getHeight() * 0.13f + dp(278);
        awardPanel.set(left, top, left + width, top + dp(34));
        awardPaint.setStyle(Paint.Style.FILL);
        awardPaint.setColor(Color.argb(175, 7, 22, 41));
        canvas.drawRoundRect(awardPanel, dp(12), dp(12), awardPaint);
        awardPaint.setTextAlign(Paint.Align.CENTER);
        awardPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        awardPaint.setTextSize(dp(11));
        awardPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("MEDALS  G " + goldTotal + "  S " + silverTotal + "  B " + bronzeTotal, awardPanel.centerX(), awardPanel.centerY() + dp(4), awardPaint);
    }

    private boolean isSummaryState() {
        try {
            int state = stateField.getInt(this);
            return state == STATE_GAME_OVER || state == STATE_STAGE_CLEAR;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private int medalRed() {
        if (lastAwardRank >= 5) return 255;
        if (lastAwardRank >= 4) return 210;
        if (lastAwardRank >= 3) return 205;
        return 255;
    }

    private int medalGreen() {
        if (lastAwardRank >= 5) return 218;
        if (lastAwardRank >= 4) return 232;
        if (lastAwardRank >= 3) return 127;
        return 218;
    }

    private int medalBlue() {
        if (lastAwardRank >= 5) return 121;
        if (lastAwardRank >= 4) return 238;
        if (lastAwardRank >= 3) return 50;
        return 121;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
