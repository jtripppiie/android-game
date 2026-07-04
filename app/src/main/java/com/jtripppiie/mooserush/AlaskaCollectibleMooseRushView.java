package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.HapticFeedbackConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Adds collectible Alaska stars during active runs.
 *
 * Stars give bonus points, and every third collected star restores one normal
 * life when the player is below the standard three-life run limit.
 */
public class AlaskaCollectibleMooseRushView extends AlaskaContraCodeMooseRushView {
    private static final String TAG = "YouRushPickups";
    private static final int STATE_RUNNING = 4;
    private static final int STARTING_LIVES = 3;
    private static final int STAR_SCORE = 12;
    private static final float SPAWN_MIN_SECONDS = 3.8f;
    private static final float SPAWN_RANDOM_SECONDS = 2.2f;
    private static final float POPUP_SECONDS = 1.0f;

    private final Paint pickupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Pickup> pickups = new ArrayList<>();
    private final Random random = new Random(90713L);

    private Field stateField;
    private Field pausedField;
    private Field playerXField;
    private Field playerYField;
    private Field playerRadiusField;
    private Field scoreField;
    private Field runStageScoreField;
    private Field livesField;

    private long lastPickupFrameNanos = 0L;
    private float spawnTimer = 2.8f;
    private float popupTimer = 0f;
    private int collectedStars = 0;
    private boolean bindingReady = false;
    private boolean warningLogged = false;
    private String popupText = "";

    public AlaskaCollectibleMooseRushView(Context context) {
        super(context);
        bindPickupFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computePickupDeltaSeconds();
        super.onDraw(canvas);
        updatePickups(dt);
        drawPickups(canvas);
        drawPickupPopup(canvas);
        if (!pickups.isEmpty() || popupTimer > 0f) {
            postInvalidateOnAnimation();
        }
    }

    private void bindPickupFields() {
        try {
            Class<?> core = MooseRushView.class;
            stateField = core.getDeclaredField("state");
            pausedField = core.getDeclaredField("paused");
            playerXField = core.getDeclaredField("playerX");
            playerYField = core.getDeclaredField("playerY");
            playerRadiusField = core.getDeclaredField("playerRadius");
            scoreField = core.getDeclaredField("score");
            runStageScoreField = core.getDeclaredField("runStageScore");
            stateField.setAccessible(true);
            pausedField.setAccessible(true);
            playerXField.setAccessible(true);
            playerYField.setAccessible(true);
            playerRadiusField.setAccessible(true);
            scoreField.setAccessible(true);
            runStageScoreField.setAccessible(true);

            livesField = AlaskaLivesMooseRushView.class.getDeclaredField("lives");
            livesField.setAccessible(true);

            bindingReady = true;
            Log.d(TAG, "Collectible binding ready.");
        } catch (NoSuchFieldException exception) {
            bindingReady = false;
            Log.w(TAG, "Collectible binding failed; pickups disabled.", exception);
        }
    }

    private float computePickupDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastPickupFrameNanos != 0L) {
            dt = Math.min((now - lastPickupFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastPickupFrameNanos = now;
        return dt;
    }

    private void updatePickups(float dt) {
        popupTimer = Math.max(0f, popupTimer - dt);
        if (!bindingReady || !isActiveRun()) {
            return;
        }

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnStar();
            spawnTimer = SPAWN_MIN_SECONDS + random.nextFloat() * SPAWN_RANDOM_SECONDS;
        }

        try {
            float playerX = playerXField.getFloat(this);
            float playerY = playerYField.getFloat(this);
            float playerRadius = playerRadiusField.getFloat(this);
            Iterator<Pickup> iterator = pickups.iterator();
            while (iterator.hasNext()) {
                Pickup pickup = iterator.next();
                pickup.age += dt;
                pickup.x -= pickup.speed * dt;
                pickup.y += (float) Math.sin(pickup.age * 3.2f) * dp(10) * dt;

                if (pickup.x < -pickup.radius * 2f) {
                    iterator.remove();
                    continue;
                }

                if (distance(playerX, playerY, pickup.x, pickup.y) <= playerRadius + pickup.radius) {
                    iterator.remove();
                    collectStar();
                }
            }
        } catch (IllegalAccessException exception) {
            if (!warningLogged) {
                warningLogged = true;
                Log.w(TAG, "Collectible update unavailable.", exception);
            }
        }
    }

    private boolean isActiveRun() {
        try {
            return stateField.getInt(this) == STATE_RUNNING && !pausedField.getBoolean(this);
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private void spawnStar() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        float yMin = getHeight() * 0.24f;
        float yMax = getHeight() * 0.58f;
        float y = yMin + random.nextFloat() * Math.max(1f, yMax - yMin);
        float radius = dp(14);
        float speed = dp(92 + random.nextInt(54));
        pickups.add(new Pickup(getWidth() + radius * 2f, y, radius, speed));
        Log.d(TAG, "Bonus star spawned.");
    }

    private void collectStar() throws IllegalAccessException {
        collectedStars++;
        scoreField.setInt(this, scoreField.getInt(this) + STAR_SCORE);
        runStageScoreField.setInt(this, runStageScoreField.getInt(this) + STAR_SCORE);
        popupText = "+" + STAR_SCORE + " STAR";

        if (collectedStars % 3 == 0) {
            int currentLives = livesField.getInt(this);
            if (currentLives > 0 && currentLives < STARTING_LIVES) {
                livesField.setInt(this, currentLives + 1);
                popupText = "EXTRA LIFE";
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            } else {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } else {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }

        popupTimer = POPUP_SECONDS;
        Log.d(TAG, "Bonus star collected. Total stars: " + collectedStars);
    }

    private void drawPickups(Canvas canvas) {
        pickupPaint.setStyle(Paint.Style.FILL);
        for (Pickup pickup : pickups) {
            float pulse = 0.85f + 0.15f * (float) Math.sin(pickup.age * 6f);
            drawStar(canvas, pickup.x, pickup.y, pickup.radius * pulse);
        }
    }

    private void drawStar(Canvas canvas, float centerX, float centerY, float radius) {
        Path star = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2.0 + i * Math.PI / 5.0;
            float r = (i % 2 == 0) ? radius : radius * 0.45f;
            float x = centerX + (float) Math.cos(angle) * r;
            float y = centerY + (float) Math.sin(angle) * r;
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.close();

        pickupPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawPath(star, pickupPaint);
        pickupPaint.setStyle(Paint.Style.STROKE);
        pickupPaint.setStrokeWidth(dp(2));
        pickupPaint.setColor(Color.WHITE);
        canvas.drawPath(star, pickupPaint);
        pickupPaint.setStyle(Paint.Style.FILL);
    }

    private void drawPickupPopup(Canvas canvas) {
        if (popupTimer <= 0f || popupText == null || popupText.isEmpty()) {
            return;
        }
        float pct = popupTimer / POPUP_SECONDS;
        int alpha = Math.round(230 * pct);
        pickupPaint.setTextAlign(Paint.Align.CENTER);
        pickupPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        pickupPaint.setTextSize(dp(18 + 4 * pct));
        pickupPaint.setColor(Color.argb(alpha, 255, 218, 121));
        canvas.drawText(popupText, getWidth() / 2f, getHeight() * 0.28f, pickupPaint);
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class Pickup {
        float x;
        float y;
        final float radius;
        final float speed;
        float age = 0f;

        Pickup(float x, float y, float radius, float speed) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
        }
    }
}
