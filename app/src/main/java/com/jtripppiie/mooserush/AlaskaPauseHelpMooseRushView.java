package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import java.lang.reflect.Field;

/**
 * Beta quality-of-life layer: pause and quick help overlay.
 *
 * This uses the existing core paused flag so the gameplay update loop stops
 * while the overlay is open, then resumes cleanly without changing the stage.
 */
public class AlaskaPauseHelpMooseRushView extends AlaskaHazardWarningMooseRushView {
    private static final String TAG = "YouRushPauseHelp";
    private static final int STATE_RUNNING = 4;

    private final Paint pausePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF pauseButtonBounds = new RectF();
    private final RectF resumeButtonBounds = new RectF();
    private final RectF panelBounds = new RectF();

    private Field stateField;
    private Field pausedField;
    private boolean bindingReady = false;
    private boolean overlayOpen = false;
    private boolean warningLogged = false;

    public AlaskaPauseHelpMooseRushView(Context context) {
        super(context);
        bindPauseFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        applyPauseFlag();
        super.onDraw(canvas);
        drawPauseButton(canvas);
        drawPauseOverlay(canvas);
        if (overlayOpen) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int index = event.getActionIndex();
            float x = event.getX(index);
            float y = event.getY(index);

            if (overlayOpen) {
                if (resumeButtonBounds.contains(x, y)) {
                    setOverlayOpen(false);
                }
                return true;
            }

            if (isRunningState() && pauseButtonBounds.contains(x, y)) {
                setOverlayOpen(true);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void resume() {
        super.resume();
        overlayOpen = false;
        writePaused(false);
    }

    private void bindPauseFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            bindingReady = true;
            Log.d(TAG, "Pause/help binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Pause/help binding failed; overlay disabled.", exception);
        }
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

    private void setOverlayOpen(boolean open) {
        overlayOpen = open;
        writePaused(open);
        performHapticFeedback(open ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.VIRTUAL_KEY);
        Log.d(TAG, open ? "Pause/help opened." : "Pause/help closed.");
        postInvalidateOnAnimation();
    }

    private void applyPauseFlag() {
        if (overlayOpen) {
            writePaused(true);
        }
    }

    private void writePaused(boolean value) {
        if (!bindingReady || pausedField == null) {
            return;
        }
        try {
            pausedField.setBoolean(this, value);
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Unable to write paused flag.", exception);
            }
        }
    }

    private void drawPauseButton(Canvas canvas) {
        if (!isRunningState() || overlayOpen) {
            pauseButtonBounds.setEmpty();
            return;
        }

        float width = dp(74);
        float height = dp(32);
        float right = getWidth() - dp(12);
        float top = dp(54);
        pauseButtonBounds.set(right - width, top, right, top + height);

        pausePaint.setStyle(Paint.Style.FILL);
        pausePaint.setColor(Color.argb(190, 7, 22, 41));
        canvas.drawRoundRect(pauseButtonBounds, dp(12), dp(12), pausePaint);

        pausePaint.setStyle(Paint.Style.STROKE);
        pausePaint.setStrokeWidth(dp(1.5f));
        pausePaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(pauseButtonBounds, dp(12), dp(12), pausePaint);
        pausePaint.setStyle(Paint.Style.FILL);

        pausePaint.setTextAlign(Paint.Align.CENTER);
        pausePaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        pausePaint.setTextSize(dp(10));
        pausePaint.setColor(Color.WHITE);
        canvas.drawText("PAUSE", pauseButtonBounds.centerX(), pauseButtonBounds.centerY() + dp(4), pausePaint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        if (!overlayOpen) {
            resumeButtonBounds.setEmpty();
            return;
        }

        pausePaint.setStyle(Paint.Style.FILL);
        pausePaint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), pausePaint);

        float width = Math.min(getWidth() - dp(44), dp(340));
        float height = dp(280);
        float left = (getWidth() - width) / 2f;
        float top = (getHeight() - height) / 2f;
        panelBounds.set(left, top, left + width, top + height);

        pausePaint.setStyle(Paint.Style.FILL);
        pausePaint.setColor(Color.rgb(7, 22, 41));
        canvas.drawRoundRect(panelBounds, dp(24), dp(24), pausePaint);

        pausePaint.setStyle(Paint.Style.STROKE);
        pausePaint.setStrokeWidth(dp(2));
        pausePaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(panelBounds, dp(24), dp(24), pausePaint);
        pausePaint.setStyle(Paint.Style.FILL);

        pausePaint.setTextAlign(Paint.Align.CENTER);
        pausePaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        pausePaint.setColor(Color.rgb(255, 218, 121));
        pausePaint.setTextSize(dp(24));
        canvas.drawText("PAUSED", panelBounds.centerX(), top + dp(45), pausePaint);

        pausePaint.setColor(Color.WHITE);
        pausePaint.setTextSize(dp(13));
        canvas.drawText("LEFT / RIGHT: move", panelBounds.centerX(), top + dp(88), pausePaint);
        canvas.drawText("JUMP: hop upward", panelBounds.centerX(), top + dp(116), pausePaint);
        canvas.drawText("THROW: launch snowball", panelBounds.centerX(), top + dp(144), pausePaint);
        canvas.drawText("TREE: appears when close", panelBounds.centerX(), top + dp(172), pausePaint);
        canvas.drawText("Close dodges and combos add points", panelBounds.centerX(), top + dp(200), pausePaint);

        resumeButtonBounds.set(panelBounds.centerX() - dp(72), panelBounds.bottom - dp(56), panelBounds.centerX() + dp(72), panelBounds.bottom - dp(18));
        pausePaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(resumeButtonBounds, dp(14), dp(14), pausePaint);
        pausePaint.setColor(Color.rgb(7, 22, 41));
        pausePaint.setTextSize(dp(13));
        canvas.drawText("RESUME", resumeButtonBounds.centerX(), resumeButtonBounds.centerY() + dp(5), pausePaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
