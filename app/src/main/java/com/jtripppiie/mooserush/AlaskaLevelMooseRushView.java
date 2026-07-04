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

/** Adds persistent local levels earned from score during active runs. */
public class AlaskaLevelMooseRushView extends AlaskaRunSummaryMooseRushView {
    private static final String TAG = "YouRushLevel";
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_TOTAL_XP = "total_xp";
    private static final int STATE_RUNNING = 4;
    private static final int[] LEVEL_XP = {0, 150, 400, 800, 1350, 2100, 3100, 4500, 6200, 8200};
    private static final float POP_SECONDS = 1.5f;

    private final Paint levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF();
    private final RectF bar = new RectF();
    private final RectF fill = new RectF();

    private SharedPreferences prefs;
    private Field stateField;
    private Field pausedField;
    private Field scoreField;

    private long lastFrameNanos = 0L;
    private boolean ready = false;
    private boolean warningLogged = false;
    private int lastState = -1;
    private int lastScore = 0;
    private int totalXp = 0;
    private int levelIndex = 0;
    private float popTimer = 0f;
    private String popText = "";

    public AlaskaLevelMooseRushView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        totalXp = Math.max(0, prefs.getInt(PREF_TOTAL_XP, 0));
        levelIndex = computeLevel(totalXp);
        bindFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = frameDelta();
        updateLevel(dt);
        super.onDraw(canvas);
        drawLevelHud(canvas);
        drawLevelPopup(canvas);
        if (popTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            scoreField = core.getDeclaredField("score");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            scoreField.setAccessible(true);
            ready = true;
            lastState = stateField.getInt(this);
            lastScore = scoreField.getInt(this);
            Log.d(TAG, "Level binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            ready = false;
            Log.w(TAG, "Level layer disabled.", exception);
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

    private void updateLevel(float dt) {
        popTimer = Math.max(0f, popTimer - dt);
        if (!ready) {
            return;
        }
        try {
            int state = stateField.getInt(this);
            int score = scoreField.getInt(this);
            if (state == STATE_RUNNING && lastState != STATE_RUNNING) {
                lastScore = score;
            }
            if (state == STATE_RUNNING && !pausedField.getBoolean(this)) {
                int gain = score - lastScore;
                if (gain > 0) {
                    addXp(gain);
                }
            }
            lastScore = score;
            lastState = state;
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Level update unavailable.", exception);
            }
        }
    }

    private void addXp(int amount) {
        int oldLevel = levelIndex;
        totalXp += amount;
        levelIndex = computeLevel(totalXp);
        prefs.edit().putInt(PREF_TOTAL_XP, totalXp).apply();
        if (levelIndex > oldLevel) {
            popText = "LEVEL UP " + (levelIndex + 1);
            popTimer = POP_SECONDS;
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            Log.d(TAG, "Level up to " + (levelIndex + 1));
        }
    }

    private int computeLevel(int xp) {
        int index = 0;
        for (int i = 0; i < LEVEL_XP.length; i++) {
            if (xp >= LEVEL_XP[i]) {
                index = i;
            }
        }
        return Math.min(index, LEVEL_XP.length - 1);
    }

    private int currentFloor() {
        return LEVEL_XP[levelIndex];
    }

    private int nextGoal() {
        return levelIndex >= LEVEL_XP.length - 1 ? LEVEL_XP[LEVEL_XP.length - 1] : LEVEL_XP[levelIndex + 1];
    }

    private void drawLevelHud(Canvas canvas) {
        if (!isRunning()) {
            return;
        }
        float width = Math.min(dp(188), getWidth() - dp(28));
        float left = getWidth() - width - dp(12);
        float top = dp(100);
        panel.set(left, top, left + width, top + dp(54));

        levelPaint.setStyle(Paint.Style.FILL);
        levelPaint.setColor(Color.argb(145, 7, 22, 41));
        canvas.drawRoundRect(panel, dp(13), dp(13), levelPaint);

        levelPaint.setTextAlign(Paint.Align.LEFT);
        levelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelPaint.setTextSize(dp(10));
        levelPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("LEVEL " + (levelIndex + 1), left + dp(10), top + dp(17), levelPaint);

        int floor = currentFloor();
        int goal = nextGoal();
        float pct = goal == floor ? 1f : Math.min(1f, Math.max(0f, (totalXp - floor) / (float) (goal - floor)));
        bar.set(left + dp(10), top + dp(27), panel.right - dp(10), top + dp(35));
        fill.set(bar.left, bar.top, bar.left + bar.width() * pct, bar.bottom);
        levelPaint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(bar, dp(4), dp(4), levelPaint);
        levelPaint.setColor(Color.rgb(101, 230, 176));
        canvas.drawRoundRect(fill, dp(4), dp(4), levelPaint);

        levelPaint.setTextAlign(Paint.Align.RIGHT);
        levelPaint.setTextSize(dp(9));
        levelPaint.setColor(Color.WHITE);
        canvas.drawText(totalXp + " XP", panel.right - dp(10), top + dp(49), levelPaint);
    }

    private void drawLevelPopup(Canvas canvas) {
        if (popTimer <= 0f || popText.isEmpty()) {
            return;
        }
        float pct = popTimer / POP_SECONDS;
        int alpha = Math.round(235 * pct);
        levelPaint.setTextAlign(Paint.Align.CENTER);
        levelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelPaint.setTextSize(dp(24));
        levelPaint.setColor(Color.argb(alpha, 101, 230, 176));
        canvas.drawText(popText, getWidth() / 2f, getHeight() * 0.43f, levelPaint);
    }

    private boolean isRunning() {
        try {
            return ready && stateField.getInt(this) == STATE_RUNNING;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
