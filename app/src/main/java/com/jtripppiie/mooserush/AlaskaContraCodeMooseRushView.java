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
 * Adds a classic secret-code layer for unlimited lives.
 *
 * Mobile mapping:
 * UP    = tap upper screen
 * DOWN  = tap lower screen away from controls
 * LEFT  = left control
 * RIGHT = right control
 * B     = throw control
 * A     = jump control
 * START = pause control
 */
public class AlaskaContraCodeMooseRushView extends AlaskaLivesMooseRushView {
    private static final String TAG = "YouRushContra";
    private static final int STATE_RUNNING = 4;

    private static final int CODE_UP = 1;
    private static final int CODE_DOWN = 2;
    private static final int CODE_LEFT = 3;
    private static final int CODE_RIGHT = 4;
    private static final int CODE_B = 5;
    private static final int CODE_A = 6;
    private static final int CODE_START = 7;

    private static final int[] CONTRA_CODE = {
            CODE_UP,
            CODE_UP,
            CODE_DOWN,
            CODE_DOWN,
            CODE_LEFT,
            CODE_RIGHT,
            CODE_LEFT,
            CODE_RIGHT,
            CODE_B,
            CODE_A,
            CODE_START
    };

    private final Paint cheatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Field stateField;
    private Field leftPadBoundsField;
    private Field rightPadBoundsField;
    private Field jumpPadBoundsField;
    private Field firePadBoundsField;
    private Field pauseButtonBoundsField;
    private Field livesField;

    private int codeIndex = 0;
    private boolean unlimitedLives = false;
    private float cheatBannerTimer = 0f;
    private long lastCheatFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean warningLogged = false;

    public AlaskaContraCodeMooseRushView(Context context) {
        super(context);
        bindCheatFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateCheatTimer();
        if (unlimitedLives) {
            keepLivesFull();
        }
        super.onDraw(canvas);
        drawUnlimitedHud(canvas);
        drawCheatBanner(canvas);
        if (cheatBannerTimer > 0f || unlimitedLives) {
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
            int token = classifyContraInput(x, y);
            if (token != 0) {
                advanceContraCode(token);
            }
        }
        return super.onTouchEvent(event);
    }

    private void bindCheatFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            leftPadBoundsField = core.getDeclaredField("leftPadBounds");
            rightPadBoundsField = core.getDeclaredField("rightPadBounds");
            jumpPadBoundsField = core.getDeclaredField("jumpPadBounds");
            firePadBoundsField = core.getDeclaredField("firePadBounds");
            stateField.setAccessible(true);
            leftPadBoundsField.setAccessible(true);
            rightPadBoundsField.setAccessible(true);
            jumpPadBoundsField.setAccessible(true);
            firePadBoundsField.setAccessible(true);

            pauseButtonBoundsField = AlaskaPauseHelpMooseRushView.class.getDeclaredField("pauseButtonBounds");
            pauseButtonBoundsField.setAccessible(true);
            livesField = AlaskaLivesMooseRushView.class.getDeclaredField("lives");
            livesField.setAccessible(true);

            bindingReady = true;
            Log.d(TAG, "Contra code binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Contra code binding failed; cheat disabled.", exception);
        }
    }

    private void updateCheatTimer() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastCheatFrameNanos != 0L) {
            dt = Math.min((now - lastCheatFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastCheatFrameNanos = now;
        cheatBannerTimer = Math.max(0f, cheatBannerTimer - dt);
    }

    private int classifyContraInput(float x, float y) {
        if (!bindingReady || !isRunningState()) {
            return 0;
        }

        try {
            RectF pause = (RectF) pauseButtonBoundsField.get(this);
            RectF left = (RectF) leftPadBoundsField.get(this);
            RectF right = (RectF) rightPadBoundsField.get(this);
            RectF jump = (RectF) jumpPadBoundsField.get(this);
            RectF fire = (RectF) firePadBoundsField.get(this);

            if (pause != null && pause.contains(x, y)) return CODE_START;
            if (left != null && left.contains(x, y)) return CODE_LEFT;
            if (right != null && right.contains(x, y)) return CODE_RIGHT;
            if (fire != null && fire.contains(x, y)) return CODE_B;
            if (jump != null && jump.contains(x, y)) return CODE_A;

            if (y < getHeight() * 0.28f) {
                return CODE_UP;
            }
            if (y > getHeight() * 0.58f && x > getWidth() * 0.28f && x < getWidth() * 0.72f) {
                return CODE_DOWN;
            }
        } catch (IllegalAccessException | ClassCastException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Contra input classification unavailable.", exception);
            }
        }
        return 0;
    }

    private boolean isRunningState() {
        try {
            return bindingReady && stateField.getInt(this) == STATE_RUNNING;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private void advanceContraCode(int token) {
        if (token == CONTRA_CODE[codeIndex]) {
            codeIndex++;
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            if (codeIndex >= CONTRA_CODE.length) {
                activateUnlimitedLives();
            }
            return;
        }

        codeIndex = token == CONTRA_CODE[0] ? 1 : 0;
    }

    private void activateUnlimitedLives() {
        unlimitedLives = true;
        codeIndex = 0;
        cheatBannerTimer = 2.4f;
        keepLivesFull();
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Log.d(TAG, "Contra code accepted. Unlimited lives active.");
        postInvalidateOnAnimation();
    }

    private void keepLivesFull() {
        if (!bindingReady || livesField == null) {
            return;
        }
        try {
            livesField.setInt(this, 99);
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Unable to maintain unlimited lives.", exception);
            }
        }
    }

    private void drawUnlimitedHud(Canvas canvas) {
        if (!unlimitedLives || !isRunningState()) {
            return;
        }
        cheatPaint.setTextAlign(Paint.Align.LEFT);
        cheatPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        cheatPaint.setTextSize(dp(10));
        cheatPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("∞ LIVES", dp(16), dp(132), cheatPaint);
    }

    private void drawCheatBanner(Canvas canvas) {
        if (cheatBannerTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, cheatBannerTimer / 2.4f);
        int alpha = Math.round(230 * pct);
        cheatPaint.setTextAlign(Paint.Align.CENTER);
        cheatPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        cheatPaint.setTextSize(dp(22));
        cheatPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawText("UNLIMITED LIVES", getWidth() / 2f, getHeight() * 0.31f, cheatPaint);
        cheatPaint.setTextSize(dp(12));
        cheatPaint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawText("Contra code accepted", getWidth() / 2f, getHeight() * 0.31f + dp(26), cheatPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
