package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Adds a polished run summary over game-over and stage-clear screens.
 */
public class AlaskaRunSummaryMooseRushView extends AlaskaMissionMooseRushView {
    private static final String TAG = "YouRushSummary";
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_BEST_GATES = "best_gates_single_run";
    private static final String PREF_LONGEST_RUN = "longest_run_seconds";
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;

    private final Paint summaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF summaryPanel = new RectF();

    private Field stateField;
    private Field pausedField;
    private Field scoreField;
    private Field gatesPassedField;
    private Field selectedStageField;
    private Field stagesField;
    private Field collectedStarsField;

    private SharedPreferences prefs;
    private long lastSummaryFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private int lastState = -1;
    private int bestGates = 0;
    private int longestRunSeconds = 0;
    private int runStartStars = 0;
    private int finalScore = 0;
    private int finalGates = 0;
    private int finalStars = 0;
    private int finalSeconds = 0;
    private int finalMissions = 0;
    private float runTimer = 0f;
    private String finalStageName = "ALASKA";

    public AlaskaRunSummaryMooseRushView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestGates = prefs.getInt(PREF_BEST_GATES, 0);
        longestRunSeconds = prefs.getInt(PREF_LONGEST_RUN, 0);
        bindSummaryFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeSummaryDeltaSeconds();
        updateSummary(dt);
        super.onDraw(canvas);
        drawRunSummary(canvas);
    }

    private void bindSummaryFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            scoreField = core.getDeclaredField("score");
            gatesPassedField = core.getDeclaredField("gatesPassed");
            selectedStageField = core.getDeclaredField("selectedStage");
            stagesField = core.getDeclaredField("STAGES");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            scoreField.setAccessible(true);
            gatesPassedField.setAccessible(true);
            selectedStageField.setAccessible(true);
            stagesField.setAccessible(true);

            collectedStarsField = AlaskaCollectibleMooseRushView.class.getDeclaredField("collectedStars");
            collectedStarsField.setAccessible(true);

            bindingReady = true;
            lastState = stateField.getInt(this);
            runStartStars = collectedStarsField.getInt(this);
            Log.d(TAG, "Run summary binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Run summary disabled.", exception);
        }
    }

    private float computeSummaryDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastSummaryFrameNanos != 0L) {
            dt = Math.min((now - lastSummaryFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastSummaryFrameNanos = now;
        return dt;
    }

    private void updateSummary(float dt) {
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            if (state == STATE_RUNNING && lastState != STATE_RUNNING) {
                runTimer = 0f;
                runStartStars = collectedStarsField.getInt(this);
            }
            if (state == STATE_RUNNING && !pausedField.getBoolean(this)) {
                runTimer += dt;
            }
            if ((state == STATE_GAME_OVER || state == STATE_STAGE_CLEAR) && lastState == STATE_RUNNING) {
                captureFinalSummary(state);
            }
            lastState = state;
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Run summary update unavailable.", exception);
            }
        }
    }

    private void captureFinalSummary(int state) throws IllegalAccessException, NoSuchFieldException {
        finalScore = scoreField.getInt(this);
        finalGates = gatesPassedField.getInt(this);
        finalStars = Math.max(0, collectedStarsField.getInt(this) - runStartStars);
        finalSeconds = Math.max(0, Math.round(runTimer));
        finalMissions = getMissionCompletedCountForSummary();
        finalStageName = loadStageName();

        boolean changed = false;
        if (finalGates > bestGates) {
            bestGates = finalGates;
            changed = true;
        }
        if (finalSeconds > longestRunSeconds) {
            longestRunSeconds = finalSeconds;
            changed = true;
        }
        if (changed) {
            prefs.edit()
                    .putInt(PREF_BEST_GATES, bestGates)
                    .putInt(PREF_LONGEST_RUN, longestRunSeconds)
                    .apply();
        }
        Log.d(TAG, "Run summary captured for state " + state + ".");
    }

    private String loadStageName() throws IllegalAccessException, NoSuchFieldException {
        int selectedStage = selectedStageField.getInt(this);
        Object stagesValue = stagesField.get(null);
        if (!(stagesValue instanceof Object[])) {
            return "ALASKA";
        }
        Object[] stages = (Object[]) stagesValue;
        if (selectedStage < 0 || selectedStage >= stages.length) {
            return "ALASKA";
        }
        Object stage = stages[selectedStage];
        Field nameField = stage.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        Object value = nameField.get(stage);
        return value == null ? "ALASKA" : String.valueOf(value);
    }

    private void drawRunSummary(Canvas canvas) {
        if (!bindingReady) {
            return;
        }
        int state;
        try {
            state = stateField.getInt(this);
        } catch (IllegalAccessException exception) {
            return;
        }
        if (state != STATE_GAME_OVER && state != STATE_STAGE_CLEAR) {
            return;
        }

        float width = Math.min(getWidth() - dp(38), dp(360));
        float height = dp(220);
        float left = (getWidth() - width) / 2f;
        float top = getHeight() * 0.18f;
        summaryPanel.set(left, top, left + width, top + height);

        summaryPaint.setStyle(Paint.Style.FILL);
        summaryPaint.setColor(Color.argb(218, 7, 22, 41));
        canvas.drawRoundRect(summaryPanel, dp(22), dp(22), summaryPaint);

        summaryPaint.setStyle(Paint.Style.STROKE);
        summaryPaint.setStrokeWidth(dp(2));
        summaryPaint.setColor(state == STATE_STAGE_CLEAR ? Color.rgb(101, 230, 176) : Color.rgb(255, 218, 121));
        canvas.drawRoundRect(summaryPanel, dp(22), dp(22), summaryPaint);
        summaryPaint.setStyle(Paint.Style.FILL);

        summaryPaint.setTextAlign(Paint.Align.CENTER);
        summaryPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        summaryPaint.setTextSize(dp(22));
        summaryPaint.setColor(state == STATE_STAGE_CLEAR ? Color.rgb(101, 230, 176) : Color.rgb(255, 218, 121));
        canvas.drawText(state == STATE_STAGE_CLEAR ? "STAGE CLEAR" : "RUN SUMMARY", summaryPanel.centerX(), top + dp(38), summaryPaint);

        summaryPaint.setTextSize(dp(12));
        summaryPaint.setColor(Color.WHITE);
        canvas.drawText(shortText(finalStageName, 30), summaryPanel.centerX(), top + dp(62), summaryPaint);

        drawSummaryLine(canvas, "Score", String.valueOf(finalScore), left + dp(28), top + dp(94));
        drawSummaryLine(canvas, "Gates", finalGates + "  best " + bestGates, left + dp(28), top + dp(120));
        drawSummaryLine(canvas, "Stars", String.valueOf(finalStars), left + dp(28), top + dp(146));
        drawSummaryLine(canvas, "Time", finalSeconds + "s  best " + longestRunSeconds + "s", left + dp(28), top + dp(172));
        drawSummaryLine(canvas, "Missions", finalMissions + "/4  total " + getTotalMissionsForSummary(), left + dp(28), top + dp(198));
    }

    private void drawSummaryLine(Canvas canvas, String label, String value, float x, float y) {
        summaryPaint.setTextAlign(Paint.Align.LEFT);
        summaryPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        summaryPaint.setTextSize(dp(12));
        summaryPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText(label + ":", x, y, summaryPaint);
        summaryPaint.setColor(Color.WHITE);
        canvas.drawText(value, x + dp(86), y, summaryPaint);
    }

    private String shortText(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
