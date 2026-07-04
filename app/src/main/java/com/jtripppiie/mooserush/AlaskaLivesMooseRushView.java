package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Adds a real beta lives/respawn layer.
 *
 * A run starts with three lives. The first two failures respawn the player at
 * the latest checkpoint and clear immediate danger so the run can continue.
 * The final failure keeps the normal game-over flow.
 */
public class AlaskaLivesMooseRushView extends AlaskaPauseHelpMooseRushView {
    private static final String TAG = "YouRushLives";
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STARTING_LIVES = 3;
    private static final float RESPAWN_SECONDS = 1.55f;
    private static final float RESPAWN_POP_SECONDS = 1.05f;

    private final Paint livesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Field stateField;
    private Field playerXField;
    private Field playerYField;
    private Field playerVelocityYField;
    private Field hazardsField;
    private Field shotsField;
    private Field spawnCooldownField;
    private Field hazardCooldownField;
    private Field damageFlashField;
    private Field gatesPassedField;

    private int lives = STARTING_LIVES;
    private int lastState = -1;
    private int lastGatesPassed = 0;
    private float checkpointX = 0f;
    private float checkpointY = 0f;
    private float respawnTimer = 0f;
    private float respawnPopTimer = 0f;
    private long lastLivesFrameNanos = 0L;
    private boolean bindingReady = false;
    private boolean checkpointReady = false;
    private boolean warningLogged = false;

    public AlaskaLivesMooseRushView(Context context) {
        super(context);
        bindLivesFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeLivesDeltaSeconds();
        updateLives(dt);
        super.onDraw(canvas);
        drawLivesHud(canvas);
        drawRespawnMessage(canvas);
        if (respawnTimer > 0f || respawnPopTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindLivesFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            playerXField = core.getDeclaredField("playerX");
            playerYField = core.getDeclaredField("playerY");
            playerVelocityYField = core.getDeclaredField("playerVelocityY");
            hazardsField = core.getDeclaredField("hazards");
            shotsField = core.getDeclaredField("shots");
            spawnCooldownField = core.getDeclaredField("spawnCooldown");
            hazardCooldownField = core.getDeclaredField("hazardCooldown");
            damageFlashField = core.getDeclaredField("damageFlash");
            gatesPassedField = core.getDeclaredField("gatesPassed");

            stateField.setAccessible(true);
            playerXField.setAccessible(true);
            playerYField.setAccessible(true);
            playerVelocityYField.setAccessible(true);
            hazardsField.setAccessible(true);
            shotsField.setAccessible(true);
            spawnCooldownField.setAccessible(true);
            hazardCooldownField.setAccessible(true);
            damageFlashField.setAccessible(true);
            gatesPassedField.setAccessible(true);

            bindingReady = true;
            lastState = stateField.getInt(this);
            lastGatesPassed = gatesPassedField.getInt(this);
            Log.d(TAG, "Lives binding ready.");
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            bindingReady = false;
            Log.w(TAG, "Lives binding failed; normal game-over flow remains active.", exception);
        }
    }

    private float computeLivesDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastLivesFrameNanos != 0L) {
            dt = Math.min((now - lastLivesFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastLivesFrameNanos = now;
        return dt;
    }

    private void updateLives(float dt) {
        respawnTimer = Math.max(0f, respawnTimer - dt);
        respawnPopTimer = Math.max(0f, respawnPopTimer - dt);
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            if (state == STATE_RUNNING && lastState != STATE_RUNNING) {
                if (lastState != STATE_GAME_OVER || lives <= 1) {
                    resetLivesForNewRun();
                }
            }

            if (state == STATE_RUNNING) {
                updateCheckpoint();
            }

            if (state == STATE_GAME_OVER && lastState == STATE_RUNNING && lives > 1) {
                lives--;
                respawnRun();
                lastState = STATE_RUNNING;
                return;
            }

            lastState = state;
        } catch (IllegalAccessException | ClassCastException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Lives update unavailable; normal game-over flow remains active.", exception);
            }
        }
    }

    private void resetLivesForNewRun() throws IllegalAccessException {
        lives = STARTING_LIVES;
        respawnTimer = 0f;
        respawnPopTimer = 0f;
        lastGatesPassed = gatesPassedField.getInt(this);
        checkpointX = getWidth() * 0.32f;
        checkpointY = getHeight() * 0.42f;
        checkpointReady = true;
        Log.d(TAG, "Lives reset for new run.");
    }

    private void updateCheckpoint() throws IllegalAccessException {
        int gatesPassed = gatesPassedField.getInt(this);
        if (!checkpointReady) {
            checkpointX = getWidth() * 0.32f;
            checkpointY = getHeight() * 0.42f;
            checkpointReady = true;
            lastGatesPassed = gatesPassed;
            return;
        }

        if (gatesPassed > lastGatesPassed) {
            checkpointX = playerXField.getFloat(this);
            checkpointY = playerYField.getFloat(this);
            lastGatesPassed = gatesPassed;
            Log.d(TAG, "Checkpoint updated after gate " + gatesPassed + ".");
        } else if (gatesPassed < lastGatesPassed) {
            lastGatesPassed = gatesPassed;
            checkpointX = getWidth() * 0.32f;
            checkpointY = getHeight() * 0.42f;
        }
    }

    @SuppressWarnings("unchecked")
    private void respawnRun() throws IllegalAccessException {
        List<Object> hazards = (List<Object>) hazardsField.get(this);
        List<Object> shots = (List<Object>) shotsField.get(this);
        if (hazards != null) {
            hazards.clear();
        }
        if (shots != null) {
            shots.clear();
        }

        float safeX = checkpointReady ? checkpointX : getWidth() * 0.32f;
        float safeY = checkpointReady ? checkpointY : getHeight() * 0.42f;
        playerXField.setFloat(this, safeX);
        playerYField.setFloat(this, clamp(safeY, getHeight() * 0.22f, getHeight() * 0.58f));
        playerVelocityYField.setFloat(this, 0f);
        spawnCooldownField.setFloat(this, 0.85f);
        hazardCooldownField.setFloat(this, 1.45f);
        damageFlashField.setFloat(this, 0f);
        stateField.setInt(this, STATE_RUNNING);
        respawnTimer = RESPAWN_SECONDS;
        respawnPopTimer = RESPAWN_POP_SECONDS;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Log.d(TAG, "Respawn used at checkpoint. Lives left: " + lives);
    }

    private void drawLivesHud(Canvas canvas) {
        if (!bindingReady || lives <= 0) {
            return;
        }
        try {
            int state = stateField.getInt(this);
            if (state != STATE_RUNNING && state != STATE_GAME_OVER) {
                return;
            }
        } catch (IllegalAccessException exception) {
            return;
        }

        float startX = dp(16);
        float y = dp(105);
        livesPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < STARTING_LIVES; i++) {
            livesPaint.setColor(i < lives ? Color.rgb(255, 98, 84) : Color.argb(120, 255, 255, 255));
            drawHeart(canvas, startX + i * dp(24), y, dp(8));
        }
    }

    private void drawHeart(Canvas canvas, float x, float y, float size) {
        canvas.drawCircle(x - size * 0.42f, y - size * 0.22f, size * 0.48f, livesPaint);
        canvas.drawCircle(x + size * 0.42f, y - size * 0.22f, size * 0.48f, livesPaint);
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x - size, y);
        path.lineTo(x, y + size * 1.2f);
        path.lineTo(x + size, y);
        path.close();
        canvas.drawPath(path, livesPaint);
    }

    private void drawRespawnMessage(Canvas canvas) {
        if (respawnPopTimer <= 0f) {
            return;
        }
        float pct = respawnPopTimer / RESPAWN_POP_SECONDS;
        int alpha = Math.round(230 * pct);
        livesPaint.setTextAlign(Paint.Align.CENTER);
        livesPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        livesPaint.setTextSize(dp(22 + 4 * pct));
        livesPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawText("CHECKPOINT", getWidth() / 2f, getHeight() * 0.36f, livesPaint);
        livesPaint.setTextSize(dp(13));
        livesPaint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawText("Respawn · Lives left: " + lives, getWidth() / 2f, getHeight() * 0.36f + dp(28), livesPaint);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
