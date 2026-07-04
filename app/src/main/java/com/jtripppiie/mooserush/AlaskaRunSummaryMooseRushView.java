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
 * Adds a polished run summary plus persistent local levels.
 */
public class AlaskaRunSummaryMooseRushView extends AlaskaMissionMooseRushView {
    private static final String TAG = "YouRushSummary";
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_BEST_GATES = "best_gates_single_run";
    private static final String PREF_LONGEST_RUN = "longest_run_seconds";
    private static final String PREF_TOTAL_XP = "total_xp";
    private static final String PREF_BEST_GRADE = "best_run_grade";
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;
    private static final int[] LEVEL_XP = {0, 150, 400, 800, 1350, 2100, 3100, 4500, 6200, 8200};
    private static final float LEVEL_POP_SECONDS = 1.5f;

    private final Paint summaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF summaryPanel = new RectF();
    private final RectF levelPanel = new RectF();
    private final RectF xpBar = new RectF();
    private final RectF xpFill = new RectF();

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
    private int lastScore = 0;
    private int bestGates = 0;
    private int longestRunSeconds = 0;
    private int totalXp = 0;
    private int levelIndex = 0;
    private int runStartStars = 0;
    private int finalScore = 0;
    private int finalGates = 0;
    private int finalStars = 0;
    private int finalSeconds = 0;
    private int finalMissions = 0;
    private int finalGradeRank = 0;
    private int bestGradeRank = 0;
    private int finalGradeBonus = 0;
    private float runTimer = 0f;
    private float levelPopTimer = 0f;
    private String levelPopupText = "";
    private String finalStageName = "ALASKA";
    private String finalGrade = "D";

    public AlaskaRunSummaryMooseRushView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestGates = prefs.getInt(PREF_BEST_GATES, 0);
        longestRunSeconds = prefs.getInt(PREF_LONGEST_RUN, 0);
        totalXp = Math.max(0, prefs.getInt(PREF_TOTAL_XP, 0));
        bestGradeRank = Math.max(0, prefs.getInt(PREF_BEST_GRADE, 0));
        levelIndex = computeLevel(totalXp);
        bindSummaryFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeSummaryDeltaSeconds();
        updateSummary(dt);
        super.onDraw(canvas);
        drawLevelHud(canvas);
        drawLevelPopup(canvas);
        drawRunSummary(canvas);
        if (levelPopTimer > 0f) {
            postInvalidateOnAnimation();
        }
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
            lastScore = scoreField.getInt(this);
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
        levelPopTimer = Math.max(0f, levelPopTimer - dt);
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            int score = scoreField.getInt(this);
            if (state == STATE_RUNNING && lastState != STATE_RUNNING) {
                runTimer = 0f;
                runStartStars = collectedStarsField.getInt(this);
                lastScore = score;
            }
            if (state == STATE_RUNNING && !pausedField.getBoolean(this)) {
                runTimer += dt;
                int gain = score - lastScore;
                if (gain > 0) {
                    addXp(gain);
                }
            }
            if ((state == STATE_GAME_OVER || state == STATE_STAGE_CLEAR) && lastState == STATE_RUNNING) {
                captureFinalSummary(state);
            }
            lastScore = score;
            lastState = state;
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Run summary update unavailable.", exception);
            }
        }
    }

    private void addXp(int amount) {
        int oldLevel = levelIndex;
        totalXp += amount;
        levelIndex = computeLevel(totalXp);
        prefs.edit().putInt(PREF_TOTAL_XP, totalXp).apply();
        if (levelIndex > oldLevel) {
            levelPopupText = "LEVEL UP " + (levelIndex + 1);
            levelPopTimer = LEVEL_POP_SECONDS;
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

    private void captureFinalSummary(int state) throws IllegalAccessException, NoSuchFieldException {
        finalScore = scoreField.getInt(this);
        finalGates = gatesPassedField.getInt(this);
        finalStars = Math.max(0, collectedStarsField.getInt(this) - runStartStars);
        finalSeconds = Math.max(0, Math.round(runTimer));
        finalMissions = getMissionCompletedCountForSummary();
        finalStageName = loadStageName();
        finalGradeRank = computeRunGradeRank(state == STATE_STAGE_CLEAR);
        finalGrade = gradeName(finalGradeRank);
        finalGradeBonus = gradeBonus(finalGradeRank);
        if (finalGradeBonus > 0) {
            addXp(finalGradeBonus);
        }

        boolean changed = false;
        if (finalGates > bestGates) {
            bestGates = finalGates;
            changed = true;
        }
        if (finalSeconds > longestRunSeconds) {
            longestRunSeconds = finalSeconds;
            changed = true;
        }
        if (finalGradeRank > bestGradeRank) {
            bestGradeRank = finalGradeRank;
            changed = true;
        }
        if (changed) {
            prefs.edit()
                    .putInt(PREF_BEST_GATES, bestGates)
                    .putInt(PREF_LONGEST_RUN, longestRunSeconds)
                    .putInt(PREF_BEST_GRADE, bestGradeRank)
                    .apply();
        }
        Log.d(TAG, "Run summary captured with grade " + finalGrade + ".");
    }

    private int computeRunGradeRank(boolean stageClear) {
        int value = finalScore + finalGates * 30 + finalStars * 25 + finalMissions * 60 + finalSeconds * 2;
        if (stageClear) {
            value += 180;
        }
        if (value >= 900) return 5;
        if (value >= 680) return 4;
        if (value >= 460) return 3;
        if (value >= 260) return 2;
        if (value >= 120) return 1;
        return 0;
    }

    private String gradeName(int rank) {
        switch (rank) {
            case 5: return "S";
            case 4: return "A";
            case 3: return "B";
            case 2: return "C";
            case 1: return "D";
            default: return "F";
        }
    }

    private int gradeBonus(int rank) {
        switch (rank) {
            case 5: return 90;
            case 4: return 60;
            case 3: return 35;
            default: return 0;
        }
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

    private void drawLevelHud(Canvas canvas) {
        if (!isRunning()) {
            return;
        }
        float width = Math.min(dp(188), getWidth() - dp(28));
        float left = getWidth() - width - dp(12);
        float top = dp(84);
        levelPanel.set(left, top, left + width, top + dp(54));

        levelPaint.setStyle(Paint.Style.FILL);
        levelPaint.setColor(Color.argb(145, 7, 22, 41));
        canvas.drawRoundRect(levelPanel, dp(13), dp(13), levelPaint);

        levelPaint.setTextAlign(Paint.Align.LEFT);
        levelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelPaint.setTextSize(dp(10));
        levelPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("LEVEL " + (levelIndex + 1), left + dp(10), top + dp(17), levelPaint);

        int floor = currentFloor();
        int goal = nextGoal();
        float pct = goal == floor ? 1f : Math.min(1f, Math.max(0f, (totalXp - floor) / (float) (goal - floor)));
        xpBar.set(left + dp(10), top + dp(27), levelPanel.right - dp(10), top + dp(35));
        xpFill.set(xpBar.left, xpBar.top, xpBar.left + xpBar.width() * pct, xpBar.bottom);
        levelPaint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(xpBar, dp(4), dp(4), levelPaint);
        levelPaint.setColor(Color.rgb(101, 230, 176));
        canvas.drawRoundRect(xpFill, dp(4), dp(4), levelPaint);

        levelPaint.setTextAlign(Paint.Align.RIGHT);
        levelPaint.setTextSize(dp(9));
        levelPaint.setColor(Color.WHITE);
        canvas.drawText(totalXp + " XP", levelPanel.right - dp(10), top + dp(49), levelPaint);
    }

    private void drawLevelPopup(Canvas canvas) {
        if (levelPopTimer <= 0f || levelPopupText == null || levelPopupText.isEmpty()) {
            return;
        }
        float pct = levelPopTimer / LEVEL_POP_SECONDS;
        int alpha = Math.round(235 * pct);
        levelPaint.setTextAlign(Paint.Align.CENTER);
        levelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        levelPaint.setTextSize(dp(24));
        levelPaint.setColor(Color.argb(alpha, 101, 230, 176));
        canvas.drawText(levelPopupText, getWidth() / 2f, getHeight() * 0.43f, levelPaint);
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
        float height = dp(270);
        float left = (getWidth() - width) / 2f;
        float top = getHeight() * 0.13f;
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

        drawSummaryLine(canvas, "Grade", finalGrade + "  best " + gradeName(bestGradeRank), left + dp(28), top + dp(94));
        drawSummaryLine(canvas, "Score", String.valueOf(finalScore), left + dp(28), top + dp(120));
        drawSummaryLine(canvas, "Gates", finalGates + "  best " + bestGates, left + dp(28), top + dp(146));
        drawSummaryLine(canvas, "Stars", String.valueOf(finalStars), left + dp(28), top + dp(172));
        drawSummaryLine(canvas, "Time", finalSeconds + "s  best " + longestRunSeconds + "s", left + dp(28), top + dp(198));
        drawSummaryLine(canvas, "Missions", finalMissions + "/4  total " + getTotalMissionsForSummary(), left + dp(28), top + dp(224));
        drawSummaryLine(canvas, "Level", (levelIndex + 1) + "  XP " + totalXp + (finalGradeBonus > 0 ? " +" + finalGradeBonus : ""), left + dp(28), top + dp(250));
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

    private boolean isRunning() {
        try {
            return bindingReady && stateField.getInt(this) == STATE_RUNNING;
        } catch (IllegalAccessException exception) {
            return false;
        }
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
