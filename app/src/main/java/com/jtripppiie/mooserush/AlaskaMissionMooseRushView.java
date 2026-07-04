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

/**
 * Adds run missions: short goals that reward skilled play and give the run
 * more structure than survival alone.
 */
public class AlaskaMissionMooseRushView extends AlaskaProgressMooseRushView {
    private static final String TAG = "YouRushMissions";
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_TOTAL_MISSIONS = "total_missions_completed";
    private static final int STATE_RUNNING = 4;
    private static final float MISSION_POP_SECONDS = 1.15f;

    private final Paint missionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF missionPanel = new RectF();

    private Field stateField;
    private Field pausedField;
    private Field scoreField;
    private Field runStageScoreField;
    private Field gatesPassedField;
    private Field bossActiveField;
    private Field collectedStarsField;

    private SharedPreferences prefs;
    private long lastMissionFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private boolean runReady = false;
    private int lastState = -1;
    private int runStartStars = 0;
    private int completedCount = 0;
    private int totalMissions = 0;
    private float runTimer = 0f;
    private float missionPopTimer = 0f;
    private String popupText = "";

    private boolean gateMissionDone = false;
    private boolean starMissionDone = false;
    private boolean timeMissionDone = false;
    private boolean phaseMissionDone = false;

    public AlaskaMissionMooseRushView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        totalMissions = prefs.getInt(PREF_TOTAL_MISSIONS, 0);
        bindMissionFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeMissionDeltaSeconds();
        super.onDraw(canvas);
        updateMissions(dt);
        drawMissionHud(canvas);
        drawMissionPopup(canvas);
        if (missionPopTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindMissionFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            scoreField = core.getDeclaredField("score");
            runStageScoreField = core.getDeclaredField("runStageScore");
            gatesPassedField = core.getDeclaredField("gatesPassed");
            bossActiveField = core.getDeclaredField("bossActive");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            scoreField.setAccessible(true);
            runStageScoreField.setAccessible(true);
            gatesPassedField.setAccessible(true);
            bossActiveField.setAccessible(true);

            collectedStarsField = AlaskaCollectibleMooseRushView.class.getDeclaredField("collectedStars");
            collectedStarsField.setAccessible(true);

            bindingReady = true;
            lastState = stateField.getInt(this);
            Log.d(TAG, "Mission binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Mission layer disabled.", exception);
        }
    }

    private float computeMissionDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastMissionFrameNanos != 0L) {
            dt = Math.min((now - lastMissionFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastMissionFrameNanos = now;
        return dt;
    }

    private void updateMissions(float dt) {
        missionPopTimer = Math.max(0f, missionPopTimer - dt);
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            int gatesPassed = gatesPassedField.getInt(this);
            boolean paused = pausedField.getBoolean(this);

            if (state == STATE_RUNNING && (!runReady || (lastState != STATE_RUNNING && gatesPassed == 0))) {
                resetRunMissions();
            }

            if (state == STATE_RUNNING && !paused) {
                runTimer += dt;
                evaluateMissions();
            }

            if (state != STATE_RUNNING && lastState == STATE_RUNNING) {
                runReady = false;
            }

            lastState = state;
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Mission update unavailable.", exception);
            }
        }
    }

    private void resetRunMissions() throws IllegalAccessException {
        runReady = true;
        runTimer = 0f;
        completedCount = 0;
        gateMissionDone = false;
        starMissionDone = false;
        timeMissionDone = false;
        phaseMissionDone = false;
        runStartStars = collectedStarsField.getInt(this);
        Log.d(TAG, "Run missions reset.");
    }

    private void evaluateMissions() throws IllegalAccessException {
        int gatesPassed = gatesPassedField.getInt(this);
        int starsThisRun = collectedStarsField.getInt(this) - runStartStars;
        boolean bossActive = bossActiveField.getBoolean(this);

        if (!gateMissionDone && gatesPassed >= 4) {
            gateMissionDone = true;
            awardMission("Gate Flow", 40);
        }
        if (!starMissionDone && starsThisRun >= 3) {
            starMissionDone = true;
            awardMission("Star Run", 45);
        }
        if (!timeMissionDone && runTimer >= 45f) {
            timeMissionDone = true;
            awardMission("Long Run", 50);
        }
        if (!phaseMissionDone && bossActive) {
            phaseMissionDone = true;
            awardMission("Challenge Ready", 60);
        }
    }

    private void awardMission(String name, int bonus) throws IllegalAccessException {
        completedCount++;
        totalMissions++;
        scoreField.setInt(this, scoreField.getInt(this) + bonus);
        runStageScoreField.setInt(this, runStageScoreField.getInt(this) + bonus);
        prefs.edit().putInt(PREF_TOTAL_MISSIONS, totalMissions).apply();
        popupText = name + " +" + bonus;
        missionPopTimer = MISSION_POP_SECONDS;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Log.d(TAG, "Mission completed: " + name + " +" + bonus);
    }

    private void drawMissionHud(Canvas canvas) {
        if (!isRunningState()) {
            return;
        }

        float left = dp(12);
        float top = dp(154);
        float width = Math.min(dp(220), getWidth() - dp(24));
        missionPanel.set(left, top, left + width, top + dp(112));

        missionPaint.setStyle(Paint.Style.FILL);
        missionPaint.setColor(Color.argb(142, 7, 22, 41));
        canvas.drawRoundRect(missionPanel, dp(14), dp(14), missionPaint);

        missionPaint.setStyle(Paint.Style.STROKE);
        missionPaint.setStrokeWidth(dp(1));
        missionPaint.setColor(Color.argb(170, 255, 218, 121));
        canvas.drawRoundRect(missionPanel, dp(14), dp(14), missionPaint);
        missionPaint.setStyle(Paint.Style.FILL);

        missionPaint.setTextAlign(Paint.Align.LEFT);
        missionPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        missionPaint.setTextSize(dp(10));
        missionPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("MISSIONS " + completedCount + "/4  ·  TOTAL " + totalMissions, left + dp(10), top + dp(18), missionPaint);

        drawMissionLine(canvas, left + dp(10), top + dp(42), gateMissionDone, "Clear 4 gates");
        drawMissionLine(canvas, left + dp(10), top + dp(62), starMissionDone, "Collect 3 stars");
        drawMissionLine(canvas, left + dp(10), top + dp(82), timeMissionDone, "Survive 45s");
        drawMissionLine(canvas, left + dp(10), top + dp(102), phaseMissionDone, "Reach challenge phase");
    }

    private void drawMissionLine(Canvas canvas, float x, float y, boolean complete, String text) {
        missionPaint.setTextAlign(Paint.Align.LEFT);
        missionPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        missionPaint.setTextSize(dp(10));
        missionPaint.setColor(complete ? Color.rgb(101, 230, 176) : Color.WHITE);
        canvas.drawText((complete ? "✓ " : "□ ") + text, x, y, missionPaint);
    }

    private void drawMissionPopup(Canvas canvas) {
        if (missionPopTimer <= 0f || popupText == null || popupText.isEmpty()) {
            return;
        }
        float pct = missionPopTimer / MISSION_POP_SECONDS;
        int alpha = Math.round(230 * pct);
        missionPaint.setTextAlign(Paint.Align.CENTER);
        missionPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        missionPaint.setTextSize(dp(18 + 5 * pct));
        missionPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawText("MISSION COMPLETE", getWidth() / 2f, getHeight() * 0.38f, missionPaint);
        missionPaint.setTextSize(dp(13));
        missionPaint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawText(popupText, getWidth() / 2f, getHeight() * 0.38f + dp(26), missionPaint);
    }

    private boolean isRunningState() {
        if (!bindingReady) {
            return false;
        }
        try {
            return stateField.getInt(this) == STATE_RUNNING;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    protected int getMissionCompletedCountForSummary() {
        return completedCount;
    }

    protected int getTotalMissionsForSummary() {
        return totalMissions;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
