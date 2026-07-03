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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Alaska-specific survival prototype layer.
 *
 * Adds first-pass survival mechanics on top of the existing Alaska runner
 * without rewriting the full core gameplay file yet.
 *
 * Prototype behavior:
 * - A tree is visible in the left/middle play lane.
 * - Tap CLIMB TREE when available to move the player to a branch briefly.
 * - While climbing, the wrapper pins the private player coordinates through
 *   reflection so normal hazard collision uses the branch position.
 * - Snowballs can now interact with normal hazards, not only bosses.
 */
public class AlaskaSurvivalMooseRushView extends JuicyMooseRushView {
    private static final String TAG = "YouRushSurvival";
    private static final float CLIMB_SECONDS = 2.65f;
    private static final float CLIMB_COOLDOWN_SECONDS = 4.25f;
    private static final float POPUP_LIFETIME_SECONDS = 0.95f;

    private final Paint survivalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF climbButtonBounds = new RectF();
    private final List<SurvivalPopup> survivalPopups = new ArrayList<>();

    private Field playerXField;
    private Field playerYField;
    private Field playerVelocityYField;
    private Field stateField;
    private Field shotsField;
    private Field hazardsField;
    private Field scoreField;
    private Field runStageScoreField;

    private long lastSurvivalFrameNanos = 0L;
    private float climbTimer = 0f;
    private float climbCooldown = 0f;
    private float treePulse = 0f;
    private boolean reflectionReady = false;
    private boolean interactionReflectionReady = false;
    private boolean interactionReflectionWarningLogged = false;

    public AlaskaSurvivalMooseRushView(Context context) {
        super(context);
        bindCoreFields();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dt = computeDeltaSeconds();
        updateSurvival(dt);
        applyTreeClimbPosition();

        super.onDraw(canvas);

        handleSnowballHazardInteractions();
        drawClimbTree(canvas);
        drawClimbButton(canvas);
        drawClimbStatus(canvas);
        drawSurvivalPopups(canvas);

        if (climbTimer > 0f || climbCooldown > 0f || !survivalPopups.isEmpty()) {
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
            if (canClimbNow() && climbButtonBounds.contains(x, y)) {
                startTreeClimb();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void bindCoreFields() {
        try {
            Class<?> core = MooseRushView.class;
            playerXField = core.getDeclaredField("playerX");
            playerYField = core.getDeclaredField("playerY");
            playerVelocityYField = core.getDeclaredField("playerVelocityY");
            stateField = core.getDeclaredField("state");
            shotsField = core.getDeclaredField("shots");
            hazardsField = core.getDeclaredField("hazards");
            scoreField = core.getDeclaredField("score");
            runStageScoreField = core.getDeclaredField("runStageScore");

            playerXField.setAccessible(true);
            playerYField.setAccessible(true);
            playerVelocityYField.setAccessible(true);
            stateField.setAccessible(true);
            shotsField.setAccessible(true);
            hazardsField.setAccessible(true);
            scoreField.setAccessible(true);
            runStageScoreField.setAccessible(true);

            reflectionReady = true;
            interactionReflectionReady = true;
            Log.d(TAG, "Alaska survival field binding ready.");
        } catch (NoSuchFieldException exception) {
            reflectionReady = false;
            interactionReflectionReady = false;
            Log.w(TAG, "Alaska survival field binding failed; visual-only fallback active.", exception);
        }
    }

    private float computeDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 0f;
        if (lastSurvivalFrameNanos != 0L) {
            dt = Math.min((now - lastSurvivalFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastSurvivalFrameNanos = now;
        return dt;
    }

    private void updateSurvival(float dt) {
        treePulse += dt * 3.4f;
        if (climbTimer > 0f) {
            climbTimer = Math.max(0f, climbTimer - dt);
            if (climbTimer == 0f) {
                climbCooldown = CLIMB_COOLDOWN_SECONDS;
                Log.d(TAG, "Tree climb ended; cooldown started.");
            }
        } else if (climbCooldown > 0f) {
            climbCooldown = Math.max(0f, climbCooldown - dt);
        }

        Iterator<SurvivalPopup> popupIterator = survivalPopups.iterator();
        while (popupIterator.hasNext()) {
            SurvivalPopup popup = popupIterator.next();
            popup.age += dt;
            popup.y -= dp(24) * dt;
            if (popup.age >= POPUP_LIFETIME_SECONDS) {
                popupIterator.remove();
            }
        }
    }

    private boolean canClimbNow() {
        return climbTimer <= 0f && climbCooldown <= 0f && isRunningState();
    }

    private boolean isRunningState() {
        if (!reflectionReady || stateField == null) {
            return true;
        }
        try {
            return stateField.getInt(this) == 4;
        } catch (IllegalAccessException exception) {
            return true;
        }
    }

    private void startTreeClimb() {
        climbTimer = CLIMB_SECONDS;
        climbCooldown = 0f;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        survivalPopups.add(new SurvivalPopup(treeX(), treeBranchY() - dp(42), "TREE ESCAPE"));
        Log.d(TAG, "Tree climb started: Alaska escape window active.");
        postInvalidateOnAnimation();
    }

    private void applyTreeClimbPosition() {
        if (climbTimer <= 0f || !reflectionReady) {
            return;
        }
        try {
            playerXField.setFloat(this, treeX());
            playerYField.setFloat(this, treeBranchY());
            playerVelocityYField.setFloat(this, 0f);
        } catch (IllegalAccessException exception) {
            Log.w(TAG, "Unable to pin player to tree branch.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSnowballHazardInteractions() {
        if (!interactionReflectionReady || !isRunningState()) {
            return;
        }

        try {
            List<Object> shots = (List<Object>) shotsField.get(this);
            List<Object> hazards = (List<Object>) hazardsField.get(this);
            if (shots == null || hazards == null || shots.isEmpty() || hazards.isEmpty()) {
                return;
            }

            Iterator<Object> shotIterator = shots.iterator();
            while (shotIterator.hasNext()) {
                Object shot = shotIterator.next();
                float shotX = getFloatField(shot, "x");
                float shotY = getFloatField(shot, "y");
                float shotRadius = getFloatField(shot, "radius");

                Iterator<Object> hazardIterator = hazards.iterator();
                boolean shotUsed = false;
                while (hazardIterator.hasNext()) {
                    Object hazard = hazardIterator.next();
                    float hazardX = getFloatField(hazard, "x");
                    float hazardY = getFloatField(hazard, "y");
                    float hazardRadius = getFloatField(hazard, "radius");
                    if (!circlesOverlap(shotX, shotY, shotRadius + dp(4), hazardX, hazardY, hazardRadius)) {
                        continue;
                    }

                    String label = getStringField(hazard, "label");
                    if (isLargeHazard(label)) {
                        setFloatField(hazard, "x", hazardX + dp(120));
                        addScoreBonus(6);
                        survivalPopups.add(new SurvivalPopup(hazardX, hazardY, "+6 SLOWED"));
                        Log.d(TAG, "Snowball slowed hazard: " + label);
                    } else {
                        hazardIterator.remove();
                        addScoreBonus(8);
                        survivalPopups.add(new SurvivalPopup(hazardX, hazardY, "+8 CLEARED"));
                        Log.d(TAG, "Snowball cleared hazard: " + label);
                    }
                    shotIterator.remove();
                    shotUsed = true;
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    break;
                }

                if (shotUsed) {
                    postInvalidateOnAnimation();
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException exception) {
            if (!interactionReflectionWarningLogged) {
                interactionReflectionWarningLogged = true;
                Log.w(TAG, "Snowball hazard interaction unavailable; core gameplay continues.", exception);
            }
        }
    }

    private void addScoreBonus(int bonus) throws IllegalAccessException {
        scoreField.setInt(this, scoreField.getInt(this) + bonus);
        runStageScoreField.setInt(this, runStageScoreField.getInt(this) + bonus);
    }

    private boolean isLargeHazard(String label) {
        return "MOOSE".equals(label) || "BEAR".equals(label) || "DARK".equals(label);
    }

    private float getFloatField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(target);
    }

    private void setFloatField(Object target, String fieldName, float value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setFloat(target, value);
    }

    private String getStringField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(target);
        return value == null ? "HAZARD" : String.valueOf(value);
    }

    private boolean circlesOverlap(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx;
        float dy = ay - by;
        float radius = ar + br;
        return dx * dx + dy * dy < radius * radius;
    }

    private void drawClimbTree(Canvas canvas) {
        float x = treeX();
        float ground = getHeight() - dp(78);
        float trunkTop = treeBranchY() - dp(82);
        float trunkBottom = ground + dp(6);

        survivalPaint.setStyle(Paint.Style.FILL);
        survivalPaint.setColor(Color.rgb(93, 57, 32));
        canvas.drawRoundRect(x - dp(10), trunkTop, x + dp(10), trunkBottom, dp(6), dp(6), survivalPaint);

        survivalPaint.setColor(Color.rgb(31, 107, 72));
        canvas.drawCircle(x, trunkTop + dp(8), dp(36), survivalPaint);
        canvas.drawCircle(x - dp(24), trunkTop + dp(24), dp(31), survivalPaint);
        canvas.drawCircle(x + dp(25), trunkTop + dp(25), dp(31), survivalPaint);
        canvas.drawCircle(x, trunkTop + dp(42), dp(34), survivalPaint);

        survivalPaint.setColor(Color.rgb(128, 82, 46));
        survivalPaint.setStrokeWidth(dp(7));
        survivalPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x - dp(46), treeBranchY(), x + dp(48), treeBranchY() - dp(8), survivalPaint);
        survivalPaint.setStyle(Paint.Style.FILL);

        if (canClimbNow()) {
            int alpha = 120 + Math.round((float) Math.sin(treePulse) * 35f);
            survivalPaint.setStyle(Paint.Style.STROKE);
            survivalPaint.setStrokeWidth(dp(2));
            survivalPaint.setColor(Color.argb(alpha, 255, 218, 121));
            canvas.drawCircle(x, treeBranchY() - dp(8), dp(62), survivalPaint);
            survivalPaint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawClimbButton(Canvas canvas) {
        float width = dp(116);
        float height = dp(42);
        float left = (getWidth() - width) / 2f;
        float top = getHeight() - dp(68);
        climbButtonBounds.set(left, top, left + width, top + height);

        boolean available = canClimbNow();
        boolean climbing = climbTimer > 0f;

        survivalPaint.setStyle(Paint.Style.FILL);
        if (climbing) {
            survivalPaint.setColor(Color.rgb(101, 230, 176));
        } else if (available) {
            survivalPaint.setColor(Color.rgb(255, 218, 121));
        } else {
            survivalPaint.setColor(Color.argb(178, 16, 25, 37));
        }
        canvas.drawRoundRect(climbButtonBounds, dp(14), dp(14), survivalPaint);

        survivalPaint.setStyle(Paint.Style.STROKE);
        survivalPaint.setStrokeWidth(dp(2));
        survivalPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(climbButtonBounds, dp(14), dp(14), survivalPaint);
        survivalPaint.setStyle(Paint.Style.FILL);

        survivalPaint.setColor(available || climbing ? Color.rgb(7, 22, 41) : Color.WHITE);
        survivalPaint.setTextAlign(Paint.Align.CENTER);
        survivalPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        survivalPaint.setTextSize(dp(10));
        canvas.drawText(climbing ? "IN TREE" : available ? "CLIMB TREE" : "TREE REST", climbButtonBounds.centerX(), climbButtonBounds.centerY() + dp(4), survivalPaint);
    }

    private void drawClimbStatus(Canvas canvas) {
        if (climbTimer <= 0f && climbCooldown <= 0f) {
            return;
        }

        String text;
        if (climbTimer > 0f) {
            text = "TREE ESCAPE " + Math.max(1, Math.round(climbTimer)) + "s";
        } else {
            text = "TREE READY IN " + Math.max(1, Math.round(climbCooldown)) + "s";
        }

        survivalPaint.setTextAlign(Paint.Align.CENTER);
        survivalPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        survivalPaint.setTextSize(dp(12));
        survivalPaint.setColor(Color.WHITE);
        canvas.drawText(text, getWidth() / 2f, getHeight() - dp(78), survivalPaint);
    }

    private void drawSurvivalPopups(Canvas canvas) {
        survivalPaint.setTextAlign(Paint.Align.CENTER);
        survivalPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        survivalPaint.setTextSize(dp(13));
        for (SurvivalPopup popup : survivalPopups) {
            float pct = Math.max(0f, Math.min(1f, popup.age / POPUP_LIFETIME_SECONDS));
            int alpha = Math.round((1f - pct) * 230f);
            survivalPaint.setColor(Color.argb(alpha, 255, 255, 255));
            canvas.drawText(popup.text, popup.x, popup.y, survivalPaint);
        }
    }

    private float treeX() {
        return getWidth() * 0.35f;
    }

    private float treeBranchY() {
        return Math.max(dp(128), getHeight() * 0.30f);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class SurvivalPopup {
        final float x;
        float y;
        final String text;
        float age = 0f;

        SurvivalPopup(float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }
}
