package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;

/**
 * Adds a clear stage progress HUD: stage name, gate progress, run time,
 * and phase messaging.
 */
public class AlaskaProgressMooseRushView extends AlaskaShieldMooseRushView {
    private static final String TAG = "YouRushProgress";
    private static final int STATE_RUNNING = 4;
    private static final float PHASE_POP_SECONDS = 1.2f;

    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barBounds = new RectF();
    private final RectF fillBounds = new RectF();
    private final RectF stagePill = new RectF();

    private Field stateField;
    private Field pausedField;
    private Field gatesPassedField;
    private Field selectedStageField;
    private Field bossActiveField;
    private Field stagesField;

    private long lastProgressFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private boolean lastBossActive = false;
    private int lastState = -1;
    private int goalGates = 1;
    private float runTimer = 0f;
    private float phasePopTimer = 0f;
    private String stageName = "ALASKA";
    private String phaseText = "";

    public AlaskaProgressMooseRushView(Context context) {
        super(context);
        bindProgressFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeProgressDeltaSeconds();
        updateProgress(dt);
        super.onDraw(canvas);
        drawStagePill(canvas);
        drawGateProgress(canvas);
        drawRunTimer(canvas);
        drawPhasePopup(canvas);
        if (phasePopTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindProgressFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            gatesPassedField = core.getDeclaredField("gatesPassed");
            selectedStageField = core.getDeclaredField("selectedStage");
            bossActiveField = core.getDeclaredField("bossActive");
            stagesField = core.getDeclaredField("STAGES");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            gatesPassedField.setAccessible(true);
            selectedStageField.setAccessible(true);
            bossActiveField.setAccessible(true);
            stagesField.setAccessible(true);
            bindingReady = true;
            lastState = stateField.getInt(this);
            lastBossActive = bossActiveField.getBoolean(this);
            loadStageInfo();
            Log.d(TAG, "Progress HUD binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Progress HUD disabled.", exception);
        }
    }

    private float computeProgressDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastProgressFrameNanos != 0L) {
            dt = Math.min((now - lastProgressFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastProgressFrameNanos = now;
        return dt;
    }

    private void updateProgress(float dt) {
        phasePopTimer = Math.max(0f, phasePopTimer - dt);
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            boolean paused = pausedField.getBoolean(this);
            boolean bossActive = bossActiveField.getBoolean(this);

            if (state == STATE_RUNNING && lastState != STATE_RUNNING) {
                runTimer = 0f;
                loadStageInfo();
                phaseText = "RUN START";
                phasePopTimer = PHASE_POP_SECONDS;
            }

            if (state == STATE_RUNNING && !paused) {
                runTimer += dt;
            }

            if (bossActive && !lastBossActive) {
                phaseText = "STAGE CHALLENGE";
                phasePopTimer = PHASE_POP_SECONDS;
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            lastState = state;
            lastBossActive = bossActive;
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Progress update unavailable.", exception);
            }
        }
    }

    private void loadStageInfo() throws IllegalAccessException, NoSuchFieldException {
        int selectedStage = selectedStageField.getInt(this);
        Object stagesValue = stagesField.get(null);
        if (!(stagesValue instanceof Object[])) {
            stageName = "ALASKA";
            goalGates = 1;
            return;
        }
        Object[] stages = (Object[]) stagesValue;
        if (selectedStage < 0 || selectedStage >= stages.length) {
            stageName = "ALASKA";
            goalGates = 1;
            return;
        }
        Object stage = stages[selectedStage];
        stageName = getStringField(stage, "name", "ALASKA");
        goalGates = Math.max(1, getIntField(stage, "goalGates", 1));
    }

    private String getStringField(Object target, String fieldName, String fallback) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value == null ? fallback : String.valueOf(value);
    }

    private int getIntField(Object target, String fieldName, int fallback) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value instanceof Integer ? (Integer) value : fallback;
    }

    private boolean isRunning() {
        if (!bindingReady) {
            return false;
        }
        try {
            return stateField.getInt(this) == STATE_RUNNING;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private void drawStagePill(Canvas canvas) {
        if (!isRunning()) {
            return;
        }
        stagePill.set(dp(12), dp(58), Math.min(dp(210), getWidth() - dp(12)), dp(84));
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(Color.argb(170, 7, 22, 41));
        canvas.drawRoundRect(stagePill, dp(11), dp(11), progressPaint);
        progressPaint.setTextAlign(Paint.Align.LEFT);
        progressPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        progressPaint.setTextSize(dp(10));
        progressPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText(shortText(stageName, 24), stagePill.left + dp(10), stagePill.top + dp(17), progressPaint);
    }

    private void drawGateProgress(Canvas canvas) {
        if (!isRunning()) {
            return;
        }
        try {
            int gatesPassed = gatesPassedField.getInt(this);
            boolean bossActive = bossActiveField.getBoolean(this);
            float pct = bossActive ? 1f : Math.min(1f, gatesPassed / (float) goalGates);
            float width = Math.min(getWidth() - dp(92), dp(260));
            float left = (getWidth() - width) / 2f;
            float top = dp(108);
            barBounds.set(left, top, left + width, top + dp(9));
            fillBounds.set(left, top, left + width * pct, top + dp(9));

            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setColor(Color.argb(150, 7, 22, 41));
            canvas.drawRoundRect(barBounds, dp(5), dp(5), progressPaint);
            progressPaint.setColor(bossActive ? Color.rgb(255, 98, 84) : Color.rgb(101, 230, 176));
            canvas.drawRoundRect(fillBounds, dp(5), dp(5), progressPaint);

            progressPaint.setTextAlign(Paint.Align.CENTER);
            progressPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            progressPaint.setTextSize(dp(10));
            progressPaint.setColor(Color.WHITE);
            canvas.drawText(bossActive ? "CHALLENGE PHASE" : "GATES " + gatesPassed + "/" + goalGates, getWidth() / 2f, top + dp(24), progressPaint);
        } catch (IllegalAccessException exception) {
            // Non-critical HUD only.
        }
    }

    private void drawRunTimer(Canvas canvas) {
        if (!isRunning()) {
            return;
        }
        progressPaint.setTextAlign(Paint.Align.RIGHT);
        progressPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        progressPaint.setTextSize(dp(11));
        progressPaint.setColor(Color.argb(220, 255, 255, 255));
        canvas.drawText("TIME " + Math.round(runTimer) + "s", getWidth() - dp(14), dp(66), progressPaint);
    }

    private void drawPhasePopup(Canvas canvas) {
        if (phasePopTimer <= 0f || phaseText == null || phaseText.isEmpty()) {
            return;
        }
        float pct = phasePopTimer / PHASE_POP_SECONDS;
        int alpha = Math.round(220 * pct);
        progressPaint.setTextAlign(Paint.Align.CENTER);
        progressPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        progressPaint.setTextSize(dp(20 + 4 * pct));
        progressPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawText(phaseText, getWidth() / 2f, getHeight() * 0.24f, progressPaint);
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
