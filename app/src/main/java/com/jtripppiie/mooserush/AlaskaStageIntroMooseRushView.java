package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Adds an arcade-style stage intro briefing when a run starts.
 *
 * This is visual only: it does not pause or rewrite the main gameplay loop.
 */
public class AlaskaStageIntroMooseRushView extends AlaskaComboMooseRushView {
    private static final String TAG = "YouRushStageIntro";
    private static final float INTRO_SECONDS = 1.75f;

    private final Paint introPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panelBounds = new RectF();

    private Field stateField;
    private Field selectedStageField;
    private Field stagesField;

    private long lastIntroFrameNanos = 0L;
    private int lastState = -1;
    private float introTimer = 0f;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private String stageName = "ALASKA RUN";
    private String bossName = "BOSS";
    private int goalGates = 0;

    public AlaskaStageIntroMooseRushView(Context context) {
        super(context);
        bindIntroFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeIntroDeltaSeconds();
        updateIntro(dt);

        super.onDraw(canvas);

        drawStageIntro(canvas);

        if (introTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindIntroFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            selectedStageField = core.getDeclaredField("selectedStage");
            stagesField = core.getDeclaredField("STAGES");
            stateField.setAccessible(true);
            selectedStageField.setAccessible(true);
            stagesField.setAccessible(true);
            bindingReady = true;
            lastState = stateField.getInt(this);
            Log.d(TAG, "Stage intro field binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Stage intro binding failed; visual fallback still works.", exception);
        }
    }

    private float computeIntroDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastIntroFrameNanos != 0L) {
            dt = Math.min((now - lastIntroFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastIntroFrameNanos = now;
        return dt;
    }

    private void updateIntro(float dt) {
        if (introTimer > 0f) {
            introTimer = Math.max(0f, introTimer - dt);
        }

        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            if (state == 4 && lastState != 4) {
                loadStageText();
                introTimer = INTRO_SECONDS;
                Log.d(TAG, "Stage intro shown for " + stageName);
            }
            lastState = state;
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Stage intro update unavailable; game continues.", exception);
            }
        }
    }

    private void loadStageText() throws IllegalAccessException, NoSuchFieldException {
        int selectedStage = selectedStageField.getInt(this);
        Object stagesValue = stagesField.get(null);
        if (!(stagesValue instanceof Object[])) {
            stageName = "ALASKA RUN";
            bossName = "BOSS";
            goalGates = 0;
            return;
        }

        Object[] stages = (Object[]) stagesValue;
        if (selectedStage < 0 || selectedStage >= stages.length) {
            stageName = "ALASKA RUN";
            bossName = "BOSS";
            goalGates = 0;
            return;
        }

        Object stage = stages[selectedStage];
        stageName = getObjectStringField(stage, "name", "ALASKA RUN");
        bossName = getObjectStringField(stage, "bossName", "BOSS");
        goalGates = getObjectIntField(stage, "goalGates", 0);
    }

    private String getObjectStringField(Object target, String fieldName, String fallback) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value == null ? fallback : String.valueOf(value);
    }

    private int getObjectIntField(Object target, String fieldName, int fallback) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value instanceof Integer ? (Integer) value : fallback;
    }

    private void drawStageIntro(Canvas canvas) {
        if (introTimer <= 0f) {
            return;
        }

        float pct = introTimer / INTRO_SECONDS;
        int alpha = Math.round(Math.min(1f, pct * 1.6f) * 220f);
        float panelWidth = Math.min(getWidth() - dp(42), dp(340));
        float panelHeight = dp(160);
        float left = (getWidth() - panelWidth) / 2f;
        float top = getHeight() * 0.22f;
        panelBounds.set(left, top, left + panelWidth, top + panelHeight);

        introPaint.setStyle(Paint.Style.FILL);
        introPaint.setColor(Color.argb(alpha, 7, 22, 41));
        canvas.drawRoundRect(panelBounds, dp(22), dp(22), introPaint);

        introPaint.setStyle(Paint.Style.STROKE);
        introPaint.setStrokeWidth(dp(2));
        introPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawRoundRect(panelBounds, dp(22), dp(22), introPaint);
        introPaint.setStyle(Paint.Style.FILL);

        introPaint.setTextAlign(Paint.Align.CENTER);
        introPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        introPaint.setColor(Color.argb(alpha, 255, 218, 121));
        introPaint.setTextSize(dp(12));
        canvas.drawText("ALASKA STAGE", panelBounds.centerX(), top + dp(28), introPaint);

        introPaint.setColor(Color.argb(alpha, 255, 255, 255));
        introPaint.setTextSize(dp(25));
        canvas.drawText(stageName, panelBounds.centerX(), top + dp(64), introPaint);

        introPaint.setTextSize(dp(13));
        introPaint.setColor(Color.argb(alpha, 210, 232, 238));
        String goalText = goalGates > 0 ? "Clear " + goalGates + " gates, then face " + bossName : "Survive, score, and keep moving";
        canvas.drawText(goalText, panelBounds.centerX(), top + dp(96), introPaint);

        introPaint.setTextSize(dp(12));
        introPaint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawText("Move · Jump · Throw · Climb when close", panelBounds.centerX(), top + dp(126), introPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
