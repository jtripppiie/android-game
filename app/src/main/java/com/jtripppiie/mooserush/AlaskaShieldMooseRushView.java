package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Adds aurora shield pickups.
 *
 * A shield pickup protects the player from the next run failure. It clears the
 * immediate field and resumes the run without spending a life.
 */
public class AlaskaShieldMooseRushView extends AlaskaCollectibleMooseRushView {
    private static final String TAG = "YouRushShield";
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final float SHIELD_SPAWN_MIN_SECONDS = 9.0f;
    private static final float SHIELD_SPAWN_RANDOM_SECONDS = 5.0f;
    private static final float SHIELD_POPUP_SECONDS = 1.1f;

    private final Paint shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<ShieldPickup> shields = new ArrayList<>();
    private final Random random = new Random(424242L);

    private Field stateField;
    private Field pausedField;
    private Field playerXField;
    private Field playerYField;
    private Field playerVelocityYField;
    private Field playerRadiusField;
    private Field hazardsField;
    private Field shotsField;
    private Field spawnCooldownField;
    private Field hazardCooldownField;
    private Field damageFlashField;

    private boolean shieldActive = false;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private long lastShieldFrameNanos = 0L;
    private float shieldSpawnTimer = 6.5f;
    private float shieldPopupTimer = 0f;
    private float shieldGlowClock = 0f;
    private String popupText = "";

    public AlaskaShieldMooseRushView(Context context) {
        super(context);
        bindShieldFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeShieldDeltaSeconds();
        updateShield(dt);
        super.onDraw(canvas);
        drawShieldPickups(canvas);
        drawActiveShield(canvas);
        drawShieldPopup(canvas);
        if (shieldActive || !shields.isEmpty() || shieldPopupTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindShieldFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            playerXField = core.getDeclaredField("playerX");
            playerYField = core.getDeclaredField("playerY");
            playerVelocityYField = core.getDeclaredField("playerVelocityY");
            playerRadiusField = core.getDeclaredField("playerRadius");
            hazardsField = core.getDeclaredField("hazards");
            shotsField = core.getDeclaredField("shots");
            spawnCooldownField = core.getDeclaredField("spawnCooldown");
            hazardCooldownField = core.getDeclaredField("hazardCooldown");
            damageFlashField = core.getDeclaredField("damageFlash");

            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            playerXField.setAccessible(true);
            playerYField.setAccessible(true);
            playerVelocityYField.setAccessible(true);
            playerRadiusField.setAccessible(true);
            hazardsField.setAccessible(true);
            shotsField.setAccessible(true);
            spawnCooldownField.setAccessible(true);
            hazardCooldownField.setAccessible(true);
            damageFlashField.setAccessible(true);

            bindingReady = true;
            Log.d(TAG, "Shield binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Shield binding failed; shield pickups disabled.", exception);
        }
    }

    private float computeShieldDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastShieldFrameNanos != 0L) {
            dt = Math.min((now - lastShieldFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastShieldFrameNanos = now;
        return dt;
    }

    private void updateShield(float dt) {
        shieldPopupTimer = Math.max(0f, shieldPopupTimer - dt);
        shieldGlowClock += dt * 4.0f;
        if (!bindingReady) {
            return;
        }

        try {
            int state = stateField.getInt(this);
            if (state == STATE_GAME_OVER && shieldActive) {
                useShield();
                return;
            }

            if (state != STATE_RUNNING || pausedField.getBoolean(this)) {
                return;
            }

            shieldSpawnTimer -= dt;
            if (!shieldActive && shieldSpawnTimer <= 0f) {
                spawnShield();
                shieldSpawnTimer = SHIELD_SPAWN_MIN_SECONDS + random.nextFloat() * SHIELD_SPAWN_RANDOM_SECONDS;
            }

            updateShieldPickups(dt);
        } catch (IllegalAccessException | ClassCastException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Shield update unavailable.", exception);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void useShield() throws IllegalAccessException {
        List<Object> hazards = (List<Object>) hazardsField.get(this);
        List<Object> shots = (List<Object>) shotsField.get(this);
        if (hazards != null) {
            hazards.clear();
        }
        if (shots != null) {
            shots.clear();
        }

        float playerY = playerYField.getFloat(this);
        playerYField.setFloat(this, clamp(playerY, getHeight() * 0.24f, getHeight() * 0.55f));
        playerVelocityYField.setFloat(this, 0f);
        spawnCooldownField.setFloat(this, 0.95f);
        hazardCooldownField.setFloat(this, 1.55f);
        damageFlashField.setFloat(this, 0f);
        stateField.setInt(this, STATE_RUNNING);
        shieldActive = false;
        shieldPopupTimer = SHIELD_POPUP_SECONDS;
        popupText = "SHIELD SAVED YOU";
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Log.d(TAG, "Shield absorbed run failure.");
    }

    private void spawnShield() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        float yMin = getHeight() * 0.22f;
        float yMax = getHeight() * 0.52f;
        float y = yMin + random.nextFloat() * Math.max(1f, yMax - yMin);
        shields.add(new ShieldPickup(getWidth() + dp(34), y, dp(17), dp(78)));
        Log.d(TAG, "Shield pickup spawned.");
    }

    private void updateShieldPickups(float dt) throws IllegalAccessException {
        float playerX = playerXField.getFloat(this);
        float playerY = playerYField.getFloat(this);
        float playerRadius = playerRadiusField.getFloat(this);
        Iterator<ShieldPickup> iterator = shields.iterator();
        while (iterator.hasNext()) {
            ShieldPickup shield = iterator.next();
            shield.age += dt;
            shield.x -= shield.speed * dt;
            shield.y += (float) Math.cos(shield.age * 2.4f) * dp(9) * dt;

            if (shield.x < -shield.radius * 2f) {
                iterator.remove();
                continue;
            }

            if (distance(playerX, playerY, shield.x, shield.y) <= playerRadius + shield.radius) {
                iterator.remove();
                collectShield();
            }
        }
    }

    private void collectShield() {
        shieldActive = true;
        popupText = "AURORA SHIELD";
        shieldPopupTimer = SHIELD_POPUP_SECONDS;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Log.d(TAG, "Shield pickup collected.");
    }

    private void drawShieldPickups(Canvas canvas) {
        for (ShieldPickup shield : shields) {
            float pulse = 0.82f + 0.18f * (float) Math.sin(shield.age * 5.0f);
            shieldPaint.setStyle(Paint.Style.FILL);
            shieldPaint.setColor(Color.argb(145, 101, 230, 176));
            canvas.drawCircle(shield.x, shield.y, shield.radius * 1.35f * pulse, shieldPaint);
            shieldPaint.setColor(Color.rgb(210, 232, 238));
            canvas.drawCircle(shield.x, shield.y, shield.radius * 0.72f * pulse, shieldPaint);
            shieldPaint.setStyle(Paint.Style.STROKE);
            shieldPaint.setStrokeWidth(dp(2));
            shieldPaint.setColor(Color.WHITE);
            canvas.drawCircle(shield.x, shield.y, shield.radius * 1.35f * pulse, shieldPaint);
            shieldPaint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawActiveShield(Canvas canvas) {
        if (!shieldActive || !bindingReady) {
            return;
        }
        try {
            float playerX = playerXField.getFloat(this);
            float playerY = playerYField.getFloat(this);
            float playerRadius = playerRadiusField.getFloat(this);
            float pulse = 0.86f + 0.14f * (float) Math.sin(shieldGlowClock);
            shieldPaint.setStyle(Paint.Style.STROKE);
            shieldPaint.setStrokeWidth(dp(3));
            shieldPaint.setColor(Color.argb(205, 101, 230, 176));
            canvas.drawCircle(playerX, playerY, playerRadius * 1.55f * pulse, shieldPaint);
            shieldPaint.setStyle(Paint.Style.FILL);
            shieldPaint.setTextAlign(Paint.Align.LEFT);
            shieldPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            shieldPaint.setTextSize(dp(10));
            shieldPaint.setColor(Color.rgb(101, 230, 176));
            canvas.drawText("SHIELD", dp(16), dp(148), shieldPaint);
        } catch (IllegalAccessException exception) {
            // Visual-only failure.
        }
    }

    private void drawShieldPopup(Canvas canvas) {
        if (shieldPopupTimer <= 0f || popupText == null || popupText.isEmpty()) {
            return;
        }
        float pct = shieldPopupTimer / SHIELD_POPUP_SECONDS;
        int alpha = Math.round(230 * pct);
        shieldPaint.setTextAlign(Paint.Align.CENTER);
        shieldPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        shieldPaint.setTextSize(dp(18 + 4 * pct));
        shieldPaint.setColor(Color.argb(alpha, 101, 230, 176));
        canvas.drawText(popupText, getWidth() / 2f, getHeight() * 0.33f, shieldPaint);
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class ShieldPickup {
        float x;
        float y;
        final float radius;
        final float speed;
        float age = 0f;

        ShieldPickup(float x, float y, float radius, float speed) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
        }
    }
}
