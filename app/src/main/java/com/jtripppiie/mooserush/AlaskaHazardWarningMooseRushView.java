package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

public class AlaskaHazardWarningMooseRushView extends AlaskaStageIntroMooseRushView {
    private static final String TAG = "YouRushWarnings";
    private static final float WARNING_ZONE_DP = 170f;

    private final Paint warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF warningPill = new RectF();
    private final RectF countPill = new RectF();

    private Field hazardsField;
    private Field stateField;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private long lastWarningFrameNanos = 0L;
    private float warningPulse = 0f;
    private int lastVisibleWarnings = 0;

    public AlaskaHazardWarningMooseRushView(Context context) {
        super(context);
        bindWarningFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateWarningPulse();
        super.onDraw(canvas);
        drawHazardWarnings(canvas);
        drawIncomingCount(canvas);
        if (lastVisibleWarnings > 0) {
            postInvalidateOnAnimation();
        }
    }

    private void bindWarningFields() {
        try {
            Class<?> core = MooseRushView.class;
            hazardsField = core.getDeclaredField("hazards");
            stateField = core.getDeclaredField("state");
            hazardsField.setAccessible(true);
            stateField.setAccessible(true);
            bindingReady = true;
            Log.d(TAG, "Hazard warning binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Hazard warning binding failed; game continues without callouts.", exception);
        }
    }

    private void updateWarningPulse() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastWarningFrameNanos != 0L) {
            dt = Math.min((now - lastWarningFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastWarningFrameNanos = now;
        warningPulse += dt * 5.2f;
    }

    @SuppressWarnings("unchecked")
    private void drawHazardWarnings(Canvas canvas) {
        lastVisibleWarnings = 0;
        if (!bindingReady) {
            return;
        }

        try {
            if (stateField.getInt(this) != 4) {
                return;
            }

            List<Object> hazards = (List<Object>) hazardsField.get(this);
            if (hazards == null || hazards.isEmpty()) {
                return;
            }

            for (Object hazard : hazards) {
                if (lastVisibleWarnings >= 3) {
                    drawAlertRail(canvas);
                    return;
                }

                float hazardX = getFloatField(hazard, "x");
                float hazardY = getFloatField(hazard, "y");
                if (hazardX < getWidth() - dp(WARNING_ZONE_DP) || hazardX > getWidth() + dp(60)) {
                    continue;
                }

                String label = getStringField(hazard, "label");
                drawWarningPill(canvas, label, hazardY, lastVisibleWarnings);
                lastVisibleWarnings++;
            }

            if (lastVisibleWarnings > 0) {
                drawAlertRail(canvas);
            }
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Hazard warning draw unavailable; game continues.", exception);
            }
        }
    }

    private void drawAlertRail(Canvas canvas) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(warningPulse);
        int alpha = 80 + Math.round(90 * pulse);
        warningPaint.setStyle(Paint.Style.FILL);
        warningPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawRoundRect(getWidth() - dp(5), dp(96), getWidth() - dp(2), getHeight() - dp(108), dp(2), dp(2), warningPaint);
    }

    private void drawIncomingCount(Canvas canvas) {
        if (lastVisibleWarnings <= 0) {
            return;
        }
        float width = dp(104);
        float height = dp(24);
        float left = getWidth() - width - dp(10);
        float top = dp(88);
        countPill.set(left, top, left + width, top + height);
        warningPaint.setStyle(Paint.Style.FILL);
        warningPaint.setColor(Color.argb(205, 7, 22, 41));
        canvas.drawRoundRect(countPill, dp(12), dp(12), warningPaint);
        warningPaint.setTextAlign(Paint.Align.CENTER);
        warningPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        warningPaint.setTextSize(dp(9));
        warningPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("INCOMING x" + lastVisibleWarnings, countPill.centerX(), countPill.centerY() + dp(3), warningPaint);
    }

    private void drawWarningPill(Canvas canvas, String label, float hazardY, int index) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(warningPulse + index);
        float width = dp(92);
        float height = dp(28);
        float right = getWidth() - dp(10);
        float centerY = clamp(hazardY + index * dp(4), dp(116), getHeight() - dp(132));
        warningPill.set(right - width, centerY - height / 2f, right, centerY + height / 2f);

        warningPaint.setStyle(Paint.Style.FILL);
        warningPaint.setColor(Color.argb(198 + Math.round(35 * pulse), 7, 22, 41));
        canvas.drawRoundRect(warningPill, dp(14), dp(14), warningPaint);

        warningPaint.setStyle(Paint.Style.STROKE);
        warningPaint.setStrokeWidth(dp(2));
        warningPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(warningPill, dp(14), dp(14), warningPaint);
        warningPaint.setStyle(Paint.Style.FILL);

        warningPaint.setTextAlign(Paint.Align.CENTER);
        warningPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        warningPaint.setTextSize(dp(10));
        warningPaint.setColor(Color.WHITE);
        canvas.drawText("→ " + cleanLabel(label), warningPill.centerX(), warningPill.centerY() + dp(4), warningPaint);
    }

    private String cleanLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return "HAZARD";
        }
        return label.length() > 8 ? label.substring(0, 8) : label;
    }

    private float getFloatField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(target);
    }

    private String getStringField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value == null ? "HAZARD" : String.valueOf(value);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
