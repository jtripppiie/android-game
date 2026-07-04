package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MooseRushView extends View {
    public interface PhotoRequestListener {
        void onPhotoRequested();
    }

    private static final String TAG = "YouRushGame";

    private static final int STATE_SPLASH = 0;
    private static final int STATE_MENU = 1;
    private static final int STATE_MAP = 2;
    private static final int STATE_CUSTOMIZE = 3;
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;

    private static final int SEASON_SUMMER = 0;
    private static final int SEASON_WINTER = 1;
    private static final int SEASON_MIDNIGHT_SUN = 2;
    private static final int SEASON_DARKNESS = 3;

    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_BEST_SCORE = "best_score";
    private static final String PREF_SELECTED_STAGE = "selected_stage";
    private static final String PREF_SELECTED_SEASON = "selected_season";
    private static final String PREF_UNLOCKED_STAGE = "unlocked_stage";
    private static final String PREF_DEBUG_OVERLAY = "debug_overlay";

    private static final String[] SEASONS = {
            "Summer",
            "Winter",
            "Midnight Sun",
            "Darkness"
    };

    private static final StageConfig[] STAGES = {
            new StageConfig("Midnight Sun Run", "Warm up under the endless sun.", SEASON_MIDNIGHT_SUN, "Sunburn Sprite", "SUN", 6, 2, 230, 1.18f, 0),
            new StageConfig("Salmon Rush", "Flying fish. Questionable physics.", SEASON_SUMMER, "Salmon Boss", "SALMON", 8, 3, 250, 1.05f, 1),
            new StageConfig("Moose Pass", "Antlers everywhere. Stay humble.", SEASON_SUMMER, "Moose Boss", "MOOSE", 10, 4, 270, 0.96f, 2),
            new StageConfig("Dark Winter", "Low light, high panic.", SEASON_DARKNESS, "Darkness Boss", "DARK", 12, 4, 285, 0.90f, 3),
            new StageConfig("Bear Country", "The bear is not impressed.", SEASON_WINTER, "Bear Boss", "BEAR", 14, 6, 300, 0.86f, 4)
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Gate> gates = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<String> debugEvents = new ArrayList<>();
    private final SharedPreferences prefs;

    private final RectF primaryButtonBounds = new RectF();
    private final RectF secondaryButtonBounds = new RectF();
    private final RectF thirdButtonBounds = new RectF();
    private final RectF photoButtonBounds = new RectF();
    private final RectF backButtonBounds = new RectF();
    private final RectF seasonButtonBounds = new RectF();
    private final RectF debugButtonBounds = new RectF();
    private final RectF leftPadBounds = new RectF();
    private final RectF rightPadBounds = new RectF();
    private final RectF jumpPadBounds = new RectF();
    private final RectF firePadBounds = new RectF();
    private final RectF tempRect = new RectF();
    private final RectF bodyBounds = new RectF();
    private final Matrix photoMatrix = new Matrix();

    private final Drawable backgroundMidnightSun;
    private final Drawable backgroundDarkWinter;
    private final Drawable salmonAsset;
    private final Drawable mooseAsset;
    private final Drawable bearAsset;
    private final Drawable antlerGateAsset;

    private PhotoRequestListener photoRequestListener;
    private Bitmap playerPhoto;

    private int state = STATE_SPLASH;
    private int score = 0;
    private int runStageScore = 0;
    private int bestScore = 0;
    private int selectedStage = 0;
    private int selectedSeason = SEASON_MIDNIGHT_SUN;
    private int unlockedStage = 0;
    private int gatesPassed = 0;
    private int stageAttempts = 0;
    private int bossHealth = 0;
    private int bossMaxHealth = 0;

    private boolean paused = false;
    private boolean bossActive = false;
    private boolean bossDefeated = false;
    private boolean debugOverlay = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean firePressed = false;
    private boolean jumpWasPressed = false;
    private boolean fireWasPressed = false;
    private boolean grounded = false;
    private int jumpsUsed = 0;

    private long lastFrameNanos = 0L;
    private float splashTimer = 0f;
    private float spawnCooldown = 0f;
    private float hazardCooldown = 0f;
    private float groundScroll = 0f;
    private float spriteClock = 0f;
    private float bossTimer = 0f;
    private float shotCooldown = 0f;
    private float damageFlash = 0f;
    private float stageClearTimer = 0f;
    private float playerX;
    private float playerY;
    private float playerVelocityY;
    private float playerRadius;
    private float bossX = 0f;
    private float bossY = 0f;
    private float bossVelocityY = 0f;

    public MooseRushView(Context context) {
        super(context);
        setFocusable(true);

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestScore = prefs.getInt(PREF_BEST_SCORE, 0);
        selectedStage = clampInt(prefs.getInt(PREF_SELECTED_STAGE, 0), 0, STAGES.length - 1);
        selectedSeason = clampInt(prefs.getInt(PREF_SELECTED_SEASON, STAGES[selectedStage].season), 0, SEASONS.length - 1);
        unlockedStage = clampInt(prefs.getInt(PREF_UNLOCKED_STAGE, 0), 0, STAGES.length - 1);
        debugOverlay = prefs.getBoolean(PREF_DEBUG_OVERLAY, false);

        backgroundMidnightSun = context.getDrawable(R.drawable.placeholder_background_midnight_sun);
        backgroundDarkWinter = context.getDrawable(R.drawable.placeholder_background_dark_winter);
        salmonAsset = context.getDrawable(R.drawable.placeholder_hazard_salmon);
        mooseAsset = context.getDrawable(R.drawable.placeholder_hazard_moose);
        bearAsset = context.getDrawable(R.drawable.placeholder_hazard_bear);
        antlerGateAsset = context.getDrawable(R.drawable.placeholder_gate_antlers);

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        logEvent("Game view ready. Alaska build loaded.");
    }

    public void setPhotoRequestListener(PhotoRequestListener listener) {
        this.photoRequestListener = listener;
    }

    public void setPlayerPhoto(Bitmap photo) {
        this.playerPhoto = photo;
        logEvent("Player photo updated.");
        invalidate();
    }

    public void resume() {
        paused = false;
        lastFrameNanos = 0L;
        logEvent("Resume.");
        postInvalidateOnAnimation();
    }

    public void pause() {
        paused = true;
        logEvent("Pause.");
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        playerRadius = dp(23);
        playerX = width * 0.28f;
        playerY = height - dp(78) - playerRadius;
        grounded = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.nanoTime();
        float dt = 0f;
        if (lastFrameNanos != 0L) {
            dt = Math.min((now - lastFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastFrameNanos = now;

        if (!paused) {
            if (state == STATE_RUNNING) {
                updateGame(dt);
            } else {
                updatePassive(dt);
            }
        }

        if (state == STATE_SPLASH) {
            drawSplashScreen(canvas);
        } else if (state == STATE_MENU) {
            drawMenuScreen(canvas);
        } else if (state == STATE_MAP) {
            drawMapScreen(canvas);
        } else if (state == STATE_CUSTOMIZE) {
            drawCustomizeScreen(canvas);
        } else {
            drawWorld(canvas);
            drawHud(canvas);
            drawVirtualControls(canvas);
            if (state == STATE_GAME_OVER) {
                drawGameOverPanel(canvas);
            } else if (state == STATE_STAGE_CLEAR) {
                drawStageClearPanel(canvas);
            }
        }

        if (!paused) {
            postInvalidateOnAnimation();
        }
    }

    private void updatePassive(float dt) {
        spriteClock += dt * 2.5f;
        if (state == STATE_SPLASH) {
            splashTimer += dt;
            if (splashTimer > 1.8f) {
                state = STATE_MENU;
                logEvent("Splash complete.");
            }
        } else if (state == STATE_STAGE_CLEAR) {
            stageClearTimer += dt;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX(event.getActionIndex());
        float y = event.getY(event.getActionIndex());

        if (state == STATE_SPLASH && action == MotionEvent.ACTION_DOWN) {
            state = STATE_MENU;
            logEvent("Splash skipped.");
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (handleTap(x, y)) {
                updateHeldControls(event);
                return true;
            }
        }

        if (state == STATE_RUNNING) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_MOVE) {
                updateHeldControls(event);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
                updateHeldControls(event);
            }
            return true;
        }

        return true;
    }

    private boolean handleTap(float x, float y) {
        if (state == STATE_MENU) {
            if (primaryButtonBounds.contains(x, y)) {
                startGame();
            } else if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                logEvent("Opened Alaska map.");
            } else if (thirdButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
                logEvent("Opened customize screen.");
            } else if (debugButtonBounds.contains(x, y)) {
                debugOverlay = !debugOverlay;
                prefs.edit().putBoolean(PREF_DEBUG_OVERLAY, debugOverlay).apply();
                logEvent("Debug overlay " + (debugOverlay ? "on" : "off") + ".");
            }
            return true;
        }

        if (state == STATE_MAP) {
            if (backButtonBounds.contains(x, y)) {
                state = STATE_MENU;
                logEvent("Returned to menu.");
                return true;
            }
            int tappedStage = findTappedStage(x, y);
            if (tappedStage >= 0) {
                selectedStage = tappedStage;
                selectedSeason = STAGES[selectedStage].season;
                saveChoices();
                logEvent("Selected stage: " + STAGES[selectedStage].name + ".");
                startGame();
            }
            return true;
        }

        if (state == STATE_CUSTOMIZE) {
            if (backButtonBounds.contains(x, y)) {
                saveChoices();
                state = STATE_MENU;
                logEvent("Customize saved.");
            } else if (photoButtonBounds.contains(x, y)) {
                if (photoRequestListener != null) {
                    logEvent("Photo picker requested.");
                    photoRequestListener.onPhotoRequested();
                }
            } else if (seasonButtonBounds.contains(x, y)) {
                selectedSeason = (selectedSeason + 1) % SEASONS.length;
                saveChoices();
                logEvent("Season set to " + SEASONS[selectedSeason] + ".");
            }
            return true;
        }

        if (state == STATE_GAME_OVER) {
            if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                logEvent("Game over -> map.");
                return true;
            }
            if (thirdButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
                logEvent("Game over -> customize.");
                return true;
            }
            startGame();
            flap();
            return true;
        }

        if (state == STATE_STAGE_CLEAR) {
            if (secondaryButtonBounds.contains(x, y)) {
                selectNextStage();
                state = STATE_MAP;
                logEvent("Stage clear -> map.");
                return true;
            }
            if (thirdButtonBounds.contains(x, y)) {
                selectNextStage();
                startGame();
                return true;
            }
            state = STATE_MAP;
            return true;
        }

        if (state == STATE_RUNNING) {
            if (!isControlTouch(x, y)) {
                flap();
            }
            return true;
        }

        return false;
    }

    private void updateHeldControls(MotionEvent event) {
        boolean wasJumpPressed = jumpPressed;
        boolean wasFirePressed = firePressed;
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        firePressed = false;

        int actionMasked = event.getActionMasked();
        int pointerUpIndex = actionMasked == MotionEvent.ACTION_POINTER_UP ? event.getActionIndex() : -1;
        boolean actionUp = actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL;

        if (!actionUp) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                if (i == pointerUpIndex) {
                    continue;
                }
                float px = event.getX(i);
                float py = event.getY(i);
                if (leftPadBounds.contains(px, py)) {
                    leftPressed = true;
                } else if (rightPadBounds.contains(px, py)) {
                    rightPressed = true;
                } else if (jumpPadBounds.contains(px, py)) {
                    jumpPressed = true;
                } else if (firePadBounds.contains(px, py)) {
                    firePressed = true;
                }
            }
        }

        if (jumpPressed && !wasJumpPressed) {
            flap();
        }
        if (firePressed && !wasFirePressed) {
            fireSnowball();
        }
        jumpWasPressed = jumpPressed;
        fireWasPressed = firePressed;
    }

    private boolean isControlTouch(float x, float y) {
        return leftPadBounds.contains(x, y)
                || rightPadBounds.contains(x, y)
                || jumpPadBounds.contains(x, y)
                || firePadBounds.contains(x, y);
    }

    private void startGame() {
        StageConfig stage = STAGES[selectedStage];
        gates.clear();
        hazards.clear();
        shots.clear();
        score = 0;
        runStageScore = 0;
        gatesPassed = 0;
        bossTimer = 0f;
        bossActive = false;
        bossDefeated = false;
        bossHealth = stage.bossHealth;
        bossMaxHealth = stage.bossHealth;
        spawnCooldown = 0.55f;
        hazardCooldown = 1.1f;
        shotCooldown = 0f;
        groundScroll = 0f;
        spriteClock = 0f;
        damageFlash = 0f;
        stageClearTimer = 0f;
        state = STATE_RUNNING;
        playerX = getWidth() * 0.28f;
        playerY = getGroundY() - playerRadius;
        playerVelocityY = 0f;
        grounded = true;
        jumpsUsed = 0;
        stageAttempts++;
        logEvent("Start stage: " + stage.name + ". Goal " + stage.goalGates + " gates, boss HP " + stage.bossHealth + ".");
    }

    private void flap() {
        if (state != STATE_RUNNING) {
            return;
        }
        if (grounded) {
            playerVelocityY = -dp(560);
            grounded = false;
            jumpsUsed = 1;
        } else if (jumpsUsed < 2) {
            playerVelocityY = -dp(500);
            jumpsUsed++;
        }
    }

    private void fireSnowball() {
        if (shotCooldown > 0f || state != STATE_RUNNING) {
            return;
        }
        shots.add(new Shot(playerX + playerRadius * 0.9f, playerY + playerRadius * 0.05f, dp(460), dp(7)));
        shotCooldown = 0.32f;
        logEvent("Snowball fired.");
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        StageConfig stage = STAGES[selectedStage];
        float gateSpeed = dp(stage.baseSpeed) + Math.min(dp(90), gatesPassed * dp(7));
        float gravity = selectedSeason == SEASON_DARKNESS ? dp(1750) : dp(1650);
        float horizontalSpeed = dp(210);

        spriteClock += dt * (5.5f + Math.min(4.5f, gatesPassed * 0.28f));
        shotCooldown = Math.max(0f, shotCooldown - dt);
        damageFlash = Math.max(0f, damageFlash - dt);

        if (leftPressed) {
            playerX -= horizontalSpeed * dt;
        }
        if (rightPressed) {
            playerX += horizontalSpeed * dt;
        }
        playerX = clamp(playerX, playerRadius + dp(6), getWidth() - playerRadius - dp(6));

        playerVelocityY += gravity * dt;
        playerY += playerVelocityY * dt;
        groundScroll = (groundScroll + gateSpeed * dt) % dp(48);

        float restY = getGroundY() - playerRadius;
        if (playerY >= restY) {
            playerY = restY;
            playerVelocityY = 0f;
            grounded = true;
            jumpsUsed = 0;
        } else {
            grounded = false;
        }
        float ceiling = dp(44) + playerRadius;
        if (playerY < ceiling) {
            playerY = ceiling;
            if (playerVelocityY < 0f) {
                playerVelocityY = 0f;
            }
        }

        updateShots(dt);

        if (!bossDefeated && !bossActive && gatesPassed >= stage.goalGates) {
            startBossPhase();
        }

        if (bossActive) {
            updateBoss(dt);
        } else {
            updateGates(dt, gateSpeed);
            updateHazards(dt, gateSpeed * 1.15f);
        }

        if (bossActive && circleHitsCircle(playerX, playerY, playerRadius * 0.86f, bossX, bossY, bossRadius())) {
            endGame(STAGES[selectedStage].bossName + " got you.");
            return;
        }

        for (Gate gate : gates) {
            if (hitsGate(gate)) {
                endGame("Antler gate bonk.");
                return;
            }
        }

        for (Hazard hazard : hazards) {
            if (circleHitsCircle(playerX, playerY, playerRadius * 0.82f, hazard.x, hazard.y, hazard.radius * 0.74f)) {
                endGame(hazard.label + " got you.");
                return;
            }
        }
    }

    private void updateGates(float dt, float gateSpeed) {
        spawnCooldown -= dt;
        if (spawnCooldown <= 0f) {
            spawnGate();
            spawnCooldown = Math.max(0.72f, STAGES[selectedStage].spawnSeconds - gatesPassed * 0.018f);
        }

        Iterator<Gate> iterator = gates.iterator();
        while (iterator.hasNext()) {
            Gate gate = iterator.next();
            gate.x -= gateSpeed * dt;

            if (!gate.passed && gate.x + gate.width < playerX) {
                gate.passed = true;
                gatesPassed++;
                addScore(10, "Gate cleared");
                logEvent("Gate " + gatesPassed + "/" + STAGES[selectedStage].goalGates + " cleared.");
            }

            if (gate.x + gate.width < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void updateHazards(float dt, float speed) {
        hazardCooldown -= dt;
        if (hazardCooldown <= 0f) {
            spawnHazard();
            hazardCooldown = Math.max(0.85f, 1.7f - selectedStage * 0.12f - gatesPassed * 0.018f);
        }

        Iterator<Hazard> iterator = hazards.iterator();
        while (iterator.hasNext()) {
            Hazard hazard = iterator.next();
            hazard.x -= speed * hazard.speedMultiplier * dt;
            hazard.y += (float) Math.sin((spriteClock + hazard.phase) * 2.0f) * dp(0.55f);
            if (!hazard.passed && hazard.x + hazard.radius < playerX) {
                hazard.passed = true;
                addScore(4, hazard.label + " dodged");
            }
            if (hazard.x + hazard.radius < -dp(36)) {
                iterator.remove();
            }
        }
    }

    private void updateShots(float dt) {
        Iterator<Shot> iterator = shots.iterator();
        while (iterator.hasNext()) {
            Shot shot = iterator.next();
            shot.x += shot.speed * dt;
            shot.wobble += dt * 9f;
            if (shot.x > getWidth() + dp(24)) {
                iterator.remove();
                continue;
            }

            if (bossActive && circleHitsCircle(shot.x, shot.y, shot.radius, bossX, bossY, bossRadius())) {
                iterator.remove();
                bossHealth--;
                damageFlash = 0.16f;
                addScore(25, "Boss hit");
                logEvent(STAGES[selectedStage].bossName + " hit. HP " + Math.max(0, bossHealth) + "/" + bossMaxHealth + ".");
                if (bossHealth <= 0) {
                    completeStage();
                    return;
                }
            }
        }
    }

    private void startBossPhase() {
        gates.clear();
        hazards.clear();
        bossActive = true;
        bossTimer = 0f;
        bossHealth = STAGES[selectedStage].bossHealth;
        bossMaxHealth = bossHealth;
        bossX = getWidth() + dp(70);
        bossY = getHeight() * 0.42f;
        bossVelocityY = dp(120 + selectedStage * 20);
        logEvent("Boss phase: " + STAGES[selectedStage].bossName + ". Use FIRE.");
    }

    private void updateBoss(float dt) {
        bossTimer += dt;
        StageConfig stage = STAGES[selectedStage];

        float desiredX = getWidth() - dp(78);
        if (bossX > desiredX) {
            bossX -= dp(210) * dt;
        } else {
            bossX = desiredX + (float) Math.sin(bossTimer * (1.7f + selectedStage * 0.18f)) * dp(16 + selectedStage * 4);
        }

        if (selectedStage == 2) {
            bossY = getHeight() * 0.42f + (float) Math.sin(bossTimer * 3.5f) * dp(105);
        } else if (selectedStage == 4) {
            bossY += bossVelocityY * dt;
            if (bossY < dp(105) || bossY > getGroundY() - dp(95)) {
                bossVelocityY *= -1f;
            }
        } else {
            bossY = getHeight() * 0.42f + (float) Math.sin(bossTimer * (2.6f + selectedStage * 0.3f)) * dp(70 + selectedStage * 7);
        }

        if (bossTimer > 22f) {
            endGame(stage.bossName + " outlasted you.");
        }
    }

    private void completeStage() {
        StageConfig stage = STAGES[selectedStage];
        bossActive = false;
        bossDefeated = true;
        addScore(100 + selectedStage * 40, "Boss defeated");
        if (score > bestScore) {
            bestScore = score;
        }
        if (selectedStage < STAGES.length - 1 && unlockedStage < selectedStage + 1) {
            unlockedStage = selectedStage + 1;
        }
        prefs.edit()
                .putInt(PREF_BEST_SCORE, bestScore)
                .putInt(PREF_UNLOCKED_STAGE, unlockedStage)
                .apply();
        state = STATE_STAGE_CLEAR;
        stageClearTimer = 0f;
        logEvent("Stage cleared: " + stage.name + ". Score " + score + ".");
    }

    private void endGame(String reason) {
        state = STATE_GAME_OVER;
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt(PREF_BEST_SCORE, bestScore).apply();
        }
        logEvent("Game over: " + reason + " Score " + score + ".");
    }

    private void addScore(int amount, String reason) {
        score += amount;
        runStageScore += amount;
        if (amount >= 10) {
            logEvent(reason + " +" + amount + ".");
        }
    }

    private void spawnGate() {
        float gateWidth = dp(44) + random.nextFloat() * dp(28);
        float maxHurdle = dp(52) + Math.min(dp(30), gatesPassed * dp(1.2f)) + selectedStage * dp(3);
        float hurdleHeight = dp(38) + random.nextFloat() * maxHurdle;
        gates.add(new Gate(getWidth() + gateWidth, hurdleHeight, gateWidth));
    }

    private void spawnHazard() {
        StageConfig stage = STAGES[selectedStage];
        float radius = dp(20 + Math.min(11, selectedStage * 3));
        // Bias toward low, jump-over heights, with the occasional mid-air flyer.
        float lowBand = getGroundY() - radius - dp(6);
        float y = lowBand;
        if (random.nextFloat() < 0.35f) {
            y = getGroundY() - dp(120) - random.nextFloat() * dp(60);
        }
        Drawable drawable = hazardDrawableForStage(selectedStage);
        hazards.add(new Hazard(getWidth() + dp(50), y, radius, 0.86f + random.nextFloat() * 0.32f, random.nextFloat() * 4f, stage.hazardLabel, drawable));
    }

    private boolean hitsGate(Gate gate) {
        tempRect.set(gate.x, getGroundY() - gate.height, gate.x + gate.width, getGroundY());
        return circleHitsRect(playerX, playerY, playerRadius * 0.82f, tempRect);
    }

    private void drawSplashScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        paint.setColor(Color.argb(184, 0, 0, 0));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(20));
        canvas.drawText("TRIPPERDEELABS", getWidth() / 2f, getHeight() * 0.40f, textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(43));
        canvas.drawText("YOU RUSH", getWidth() / 2f, getHeight() * 0.49f, textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Alaska chaos build", getWidth() / 2f, getHeight() * 0.57f, textPaint);
    }

    private void drawMenuScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "YOU RUSH: ALASKA", "Upload your face. Beat local chaos.");

        drawCharacter(canvas, getWidth() / 2f, getHeight() * 0.33f, dp(28));

        float y = getHeight() * 0.53f;
        setButton(primaryButtonBounds, y, dp(220), dp(48));
        setButton(secondaryButtonBounds, y + dp(62), dp(220), dp(48));
        setButton(thirdButtonBounds, y + dp(124), dp(220), dp(48));
        setButton(debugButtonBounds, y + dp(186), dp(220), dp(40));

        drawButton(canvas, primaryButtonBounds, "PLAY " + STAGES[selectedStage].name);
        drawButton(canvas, secondaryButtonBounds, "ALASKA MAP");
        drawButton(canvas, thirdButtonBounds, "CUSTOMIZE");
        drawSmallButton(canvas, debugButtonBounds, "DEBUG: " + (debugOverlay ? "ON" : "OFF"));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Best " + bestScore + "   Unlocked " + (unlockedStage + 1) + "/" + STAGES.length, getWidth() / 2f, getHeight() - dp(30), textPaint);
    }

    private void drawMapScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "ALASKA MAP", "Pick a stage. Beat bosses to unlock more.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        float startY = dp(132);
        float gap = dp(76);
        for (int i = 0; i < STAGES.length; i++) {
            RectF node = stageBounds(i, startY, gap);
            boolean selected = i == selectedStage;
            boolean unlocked = i <= unlockedStage;

            paint.setStyle(Paint.Style.FILL);
            if (selected) {
                paint.setColor(Color.rgb(255, 218, 121));
            } else if (unlocked) {
                paint.setColor(Color.argb(226, 16, 25, 37));
            } else {
                paint.setColor(Color.argb(214, 55, 58, 65));
            }
            canvas.drawRoundRect(node, dp(16), dp(16), paint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(selected ? Color.rgb(23, 29, 38) : Color.WHITE);
            textPaint.setTextSize(dp(17));
            canvas.drawText((i + 1) + ". " + STAGES[i].name, node.left + dp(16), node.top + dp(27), textPaint);

            textPaint.setTextSize(dp(12));
            textPaint.setColor(selected ? Color.rgb(58, 65, 78) : Color.rgb(204, 223, 230));
            canvas.drawText(STAGES[i].line, node.left + dp(16), node.top + dp(49), textPaint);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(dp(11));
            String bossLabel = unlocked ? STAGES[i].bossName : "LOCKED";
            canvas.drawText(bossLabel, node.right - dp(14), node.top + dp(49), textPaint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Tester note: locked nodes are visible so the full Alaska template is inspectable.", getWidth() / 2f, getHeight() - dp(24), textPaint);
    }

    private void drawCustomizeScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "CUSTOMIZE", "Face, season, and test feel.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        drawCharacter(canvas, getWidth() / 2f, getHeight() * 0.33f, dp(34));

        float y = getHeight() * 0.53f;
        setButton(photoButtonBounds, y, dp(226), dp(48));
        setButton(seasonButtonBounds, y + dp(64), dp(226), dp(48));

        drawButton(canvas, photoButtonBounds, playerPhoto == null ? "ADD YOUR PHOTO" : "CHANGE PHOTO");
        drawButton(canvas, seasonButtonBounds, "SEASON: " + SEASONS[selectedSeason]);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Stage: " + STAGES[selectedStage].name, getWidth() / 2f, y + dp(140), textPaint);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Controls: LEFT / RIGHT / JUMP / FIRE", getWidth() / 2f, y + dp(166), textPaint);
    }

    private void drawWorld(Canvas canvas) {
        drawAlaskaBackdrop(canvas);

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }
        for (Hazard hazard : hazards) {
            drawHazard(canvas, hazard);
        }
        for (Shot shot : shots) {
            drawShot(canvas, shot);
        }
        if (bossActive) {
            drawBoss(canvas);
        }

        drawCharacter(canvas, playerX, playerY, playerRadius);
        drawGround(canvas, getWidth());
    }

    private void drawAlaskaBackdrop(Canvas canvas) {
        boolean dark = selectedSeason == SEASON_DARKNESS || STAGES[selectedStage].season == SEASON_DARKNESS;
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage == 4;
        Drawable background = dark || winter ? backgroundDarkWinter : backgroundMidnightSun;
        if (background != null) {
            drawDrawable(canvas, background, 0, 0, getWidth(), getHeight());
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? Color.rgb(8, 18, 36) : Color.rgb(25, 43, 68));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }

        if (dark) {
            paint.setColor(Color.argb(68, 0, 0, 0));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }

    private void drawGround(Canvas canvas, int width) {
        float groundY = getGroundY();
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage == 4;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(winter ? Color.rgb(225, 235, 238) : Color.rgb(46, 119, 82));
        canvas.drawRect(0, groundY, width, getHeight(), paint);

        paint.setColor(winter ? Color.rgb(190, 207, 216) : Color.rgb(35, 86, 63));
        for (float x = -groundScroll; x < width + dp(60); x += dp(48)) {
            canvas.drawRoundRect(x, groundY + dp(12), x + dp(28), groundY + dp(20), dp(4), dp(4), paint);
        }
    }

    private void drawGate(Canvas canvas, Gate gate) {
        float top = getGroundY() - gate.height;

        if (antlerGateAsset != null) {
            drawDrawable(canvas, antlerGateAsset, gate.x, top, gate.x + gate.width, getGroundY());
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(104, 62, 36));
            canvas.drawRoundRect(gate.x, top, gate.x + gate.width, getGroundY(), dp(10), dp(10), paint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("ANTLERS", gate.x + gate.width / 2f, top - dp(8), textPaint);
    }

    private void drawHazard(Canvas canvas, Hazard hazard) {
        if (hazard.drawable != null) {
            drawDrawable(canvas, hazard.drawable, hazard.x - hazard.radius, hazard.y - hazard.radius, hazard.x + hazard.radius, hazard.y + hazard.radius);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(hazard.x, hazard.y, hazard.radius, paint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(hazard.label, hazard.x, hazard.y + hazard.radius + dp(12), textPaint);
    }

    private void drawShot(Canvas canvas, Shot shot) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(shot.x, shot.y + (float) Math.sin(shot.wobble) * dp(2), shot.radius, paint);
        paint.setColor(Color.rgb(180, 220, 230));
        canvas.drawCircle(shot.x - shot.radius * 0.3f, shot.y - shot.radius * 0.25f, shot.radius * 0.35f, paint);
    }

    private void drawBoss(Canvas canvas) {
        Drawable bossDrawable = bossDrawableForStage();
        float radius = bossRadius();

        if (damageFlash > 0f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawCircle(bossX, bossY, radius * 1.55f, paint);
        }

        if (bossDrawable != null) {
            drawDrawable(canvas, bossDrawable, bossX - radius * 1.30f, bossY - radius * 1.30f, bossX + radius * 1.30f, bossY + radius * 1.30f);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(bossX, bossY, radius, paint);
        }

        drawBossHealthBar(canvas);
    }

    private void drawBossHealthBar(Canvas canvas) {
        float left = dp(62);
        float top = dp(82);
        float right = getWidth() - dp(62);
        float bottom = top + dp(12);
        float pct = bossMaxHealth <= 0 ? 0f : bossHealth / (float) bossMaxHealth;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 0, 0, 0));
        canvas.drawRoundRect(left, top, right, bottom, dp(6), dp(6), paint);
        paint.setColor(Color.rgb(255, 98, 84));
        canvas.drawRoundRect(left, top, left + (right - left) * pct, bottom, dp(6), dp(6), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(STAGES[selectedStage].bossName + " HP " + bossHealth + "/" + bossMaxHealth, getWidth() / 2f, top - dp(7), textPaint);
    }

    private Drawable hazardDrawableForStage(int stage) {
        if (stage == 1) return salmonAsset;
        if (stage == 2) return mooseAsset;
        if (stage == 4) return bearAsset;
        if (stage == 3) return bearAsset;
        return salmonAsset;
    }

    private Drawable bossDrawableForStage() {
        if (selectedStage == 1) return salmonAsset;
        if (selectedStage == 2) return mooseAsset;
        if (selectedStage == 4) return bearAsset;
        if (selectedStage == 3) return bearAsset;
        return salmonAsset;
    }

    private float bossRadius() {
        if (selectedStage == 4) return dp(48);
        if (selectedStage == 2) return dp(42);
        return dp(36);
    }

    private void drawCharacter(Canvas canvas, float x, float y, float radius) {
        float bob = (float) Math.sin(spriteClock * Math.PI * 2f) * dp(2.5f);
        float cycle = (float) Math.sin(spriteClock * Math.PI * 2f);
        float headY = y + bob;

        drawWalkingSpriteBody(canvas, x, headY, radius, cycle);

        if (playerPhoto != null) {
            drawPlayerPhoto(canvas, x, headY, radius);
        } else {
            drawDefaultPlayerHead(canvas, x, headY, radius);
        }
    }

    private void drawWalkingSpriteBody(Canvas canvas, float x, float headY, float radius, float cycle) {
        float bodyTop = headY + radius * 0.72f;
        float bodyBottom = bodyTop + radius * 1.5f;
        float bodyHalfWidth = radius * 0.62f;
        float step = cycle * radius * 0.46f;
        float oppositeStep = -step;

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 218, 121));
        bodyBounds.set(x - bodyHalfWidth, bodyTop, x + bodyHalfWidth, bodyBottom);
        canvas.drawRoundRect(bodyBounds, dp(7), dp(7), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(radius * 0.2f);
        paint.setColor(Color.rgb(255, 177, 70));
        canvas.drawLine(x - radius * 0.5f, bodyTop + radius * 0.42f, x - radius * 1.12f - step * 0.25f, bodyTop + radius * 0.95f + step * 0.12f, paint);
        canvas.drawLine(x + radius * 0.5f, bodyTop + radius * 0.42f, x + radius * 1.12f + step * 0.25f, bodyTop + radius * 0.95f - step * 0.12f, paint);

        paint.setStrokeWidth(radius * 0.25f);
        paint.setColor(Color.rgb(52, 134, 196));
        canvas.drawLine(x - radius * 0.3f, bodyBottom - radius * 0.12f, x - radius * 0.62f + step, bodyBottom + radius * 1.04f, paint);
        canvas.drawLine(x + radius * 0.3f, bodyBottom - radius * 0.12f, x + radius * 0.62f + oppositeStep, bodyBottom + radius * 1.04f, paint);

        paint.setStrokeWidth(radius * 0.33f);
        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawPoint(x - radius * 0.62f + step, bodyBottom + radius * 1.04f, paint);
        canvas.drawPoint(x + radius * 0.62f + oppositeStep, bodyBottom + radius * 1.04f, paint);

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawDefaultPlayerHead(Canvas canvas, float x, float headY, float radius) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 192, 81));
        canvas.drawRoundRect(x - radius, headY - radius, x + radius, headY + radius, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawCircle(x - radius * 0.35f, headY - radius * 0.12f, dp(3), paint);
        canvas.drawCircle(x + radius * 0.35f, headY - radius * 0.12f, dp(3), paint);
        canvas.drawRoundRect(x - radius * 0.35f, headY + radius * 0.25f, x + radius * 0.35f, headY + radius * 0.38f, dp(5), dp(5), paint);
    }

    private void drawPlayerPhoto(Canvas canvas, float x, float headY, float radius) {
        float diameter = radius * 2f;
        BitmapShader shader = new BitmapShader(playerPhoto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = Math.max(diameter / playerPhoto.getWidth(), diameter / playerPhoto.getHeight());
        float dx = x - playerPhoto.getWidth() * scale / 2f;
        float dy = headY - playerPhoto.getHeight() * scale / 2f;
        photoMatrix.reset();
        photoMatrix.setScale(scale, scale);
        photoMatrix.postTranslate(dx, dy);
        shader.setLocalMatrix(photoMatrix);

        paint.setShader(shader);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(x - radius, headY - radius, x + radius, headY + radius, dp(8), dp(8), paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(x - radius, headY - radius, x + radius, headY + radius, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHud(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(36));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(score), getWidth() / 2f, dp(62), textPaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(12));
        canvas.drawText(STAGES[selectedStage].name, dp(14), dp(28), textPaint);
        canvas.drawText("Gates " + gatesPassed + "/" + STAGES[selectedStage].goalGates, dp(14), dp(46), textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(12));
        canvas.drawText("Best " + bestScore, getWidth() - dp(14), dp(28), textPaint);
        canvas.drawText(SEASONS[selectedSeason], getWidth() - dp(14), dp(46), textPaint);

        if (debugOverlay) {
            drawDebugOverlay(canvas);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawDebugOverlay(Canvas canvas) {
        float top = dp(100);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(156, 0, 0, 0));
        canvas.drawRoundRect(dp(10), top, getWidth() - dp(10), top + dp(126), dp(8), dp(8), paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("DEBUG / Alaska Test Build", dp(18), top + dp(17), textPaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("state=" + stateName() + " score=" + score + " boss=" + bossActive + " hp=" + bossHealth, dp(18), top + dp(34), textPaint);
        canvas.drawText("x=" + Math.round(playerX) + " y=" + Math.round(playerY) + " shots=" + shots.size() + " hazards=" + hazards.size(), dp(18), top + dp(50), textPaint);

        int max = Math.min(4, debugEvents.size());
        for (int i = 0; i < max; i++) {
            String event = debugEvents.get(debugEvents.size() - 1 - i);
            canvas.drawText("• " + event, dp(18), top + dp(70 + i * 14), textPaint);
        }
    }

    private void drawVirtualControls(Canvas canvas) {
        float bottom = getHeight() - dp(18);
        float size = dp(54);
        leftPadBounds.set(dp(16), bottom - size, dp(16) + size, bottom);
        rightPadBounds.set(dp(82), bottom - size, dp(82) + size, bottom);
        jumpPadBounds.set(getWidth() - dp(148), bottom - size, getWidth() - dp(148) + size, bottom);
        firePadBounds.set(getWidth() - dp(82), bottom - size, getWidth() - dp(82) + size, bottom);

        drawControlButton(canvas, leftPadBounds, "◀", leftPressed);
        drawControlButton(canvas, rightPadBounds, "▶", rightPressed);
        drawControlButton(canvas, jumpPadBounds, "JUMP", jumpPressed);
        drawControlButton(canvas, firePadBounds, "FIRE", firePressed || shotCooldown > 0f);
    }

    private void drawControlButton(Canvas canvas, RectF bounds, String label, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.argb(230, 255, 218, 121) : Color.argb(178, 16, 25, 37));
        canvas.drawRoundRect(bounds, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(220, 255, 255, 255));
        canvas.drawRoundRect(bounds, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(label.length() > 2 ? dp(10) : dp(20));
        textPaint.setColor(active ? Color.rgb(24, 30, 38) : Color.WHITE);
        canvas.drawText(label, bounds.centerX(), bounds.centerY() + dp(5), textPaint);
    }

    private void drawGameOverPanel(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(40), dp(348));
        float panelHeight = dp(236);
        float left = (getWidth() - panelWidth) / 2f;
        float top = (getHeight() - panelHeight) / 2f;
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(230, 14, 21, 31));
        canvas.drawRoundRect(panel, dp(22), dp(22), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(34));
        canvas.drawText("BONKED", getWidth() / 2f, top + dp(52), textPaint);

        textPaint.setTextSize(dp(15));
        canvas.drawText(deathLine(), getWidth() / 2f, top + dp(90), textPaint);

        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("Score " + score + " · Gates " + gatesPassed + " · Attempt " + stageAttempts, getWidth() / 2f, top + dp(118), textPaint);
        canvas.drawText("Tap anywhere to retry", getWidth() / 2f, top + dp(144), textPaint);

        setButton(secondaryButtonBounds, top + dp(174), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(174), dp(118), dp(36));
        thirdButtonBounds.offset(dp(64), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "CUSTOMIZE");
    }

    private void drawStageClearPanel(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(40), dp(348));
        float panelHeight = dp(236);
        float left = (getWidth() - panelWidth) / 2f;
        float top = (getHeight() - panelHeight) / 2f;
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(232, 10, 42, 38));
        canvas.drawRoundRect(panel, dp(22), dp(22), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(29));
        canvas.drawText("ALASKA SURVIVED", getWidth() / 2f, top + dp(48), textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(STAGES[selectedStage].bossName + " defeated", getWidth() / 2f, top + dp(82), textPaint);
        canvas.drawText("Score " + score + " · Best " + bestScore, getWidth() / 2f, top + dp(108), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(dp(13));
        canvas.drawText(stageClearLine(), getWidth() / 2f, top + dp(137), textPaint);

        setButton(secondaryButtonBounds, top + dp(172), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(172), dp(118), dp(36));
        thirdButtonBounds.offset(dp(64), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "NEXT");
    }

    private void drawTopBrand(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), dp(112), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(12));
        canvas.drawText("TRIPPERDEELABS", getWidth() / 2f, dp(25), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(28));
        canvas.drawText(title, getWidth() / 2f, dp(61), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(dp(13));
        canvas.drawText(subtitle, getWidth() / 2f, dp(88), textPaint);
    }

    private void drawButton(Canvas canvas, RectF bounds, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(234, 255, 218, 121));
        canvas.drawRoundRect(bounds, dp(15), dp(15), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(24, 30, 38));
        canvas.drawText(label, bounds.centerX(), bounds.centerY() + dp(5), textPaint);
    }

    private void drawSmallButton(Canvas canvas, RectF bounds, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(230, 16, 25, 37));
        canvas.drawRoundRect(bounds, dp(12), dp(12), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, bounds.centerX(), bounds.centerY() + dp(4), textPaint);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, float left, float top, float right, float bottom) {
        drawable.setBounds(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
        drawable.draw(canvas);
    }

    private void setButton(RectF bounds, float centerY, float width, float height) {
        float left = (getWidth() - width) / 2f;
        bounds.set(left, centerY - height / 2f, left + width, centerY + height / 2f);
    }

    private void setBackButton() {
        backButtonBounds.set(dp(16), dp(16), dp(88), dp(52));
    }

    private RectF stageBounds(int index, float startY, float gap) {
        float left = dp(22);
        float right = getWidth() - dp(22);
        float top = startY + index * gap;
        return new RectF(left, top, right, top + dp(62));
    }

    private int findTappedStage(float x, float y) {
        float startY = dp(132);
        float gap = dp(76);
        for (int i = 0; i < STAGES.length; i++) {
            if (stageBounds(i, startY, gap).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void selectNextStage() {
        if (selectedStage < STAGES.length - 1) {
            selectedStage++;
            selectedSeason = STAGES[selectedStage].season;
            saveChoices();
        }
    }

    private void saveChoices() {
        prefs.edit()
                .putInt(PREF_SELECTED_STAGE, selectedStage)
                .putInt(PREF_SELECTED_SEASON, selectedSeason)
                .apply();
    }

    private String deathLine() {
        if (bossActive) {
            return STAGES[selectedStage].bossName + " got you.";
        }
        if (score == 0) {
            return "Score 0. Alaska remains undefeated.";
        }
        if (score < 50) {
            return "Score " + score + ". Blame the moose.";
        }
        if (score < 140) {
            return "Score " + score + ". Almost tourist-proof.";
        }
        return "Score " + score + ". Certified chaos legend.";
    }

    private String stageClearLine() {
        if (selectedStage == 4) {
            return "Bear Country cleared. That is suspiciously impressive.";
        }
        return "Next Alaska stage unlocked.";
    }

    private String stateName() {
        if (state == STATE_SPLASH) return "splash";
        if (state == STATE_MENU) return "menu";
        if (state == STATE_MAP) return "map";
        if (state == STATE_CUSTOMIZE) return "customize";
        if (state == STATE_RUNNING) return "running";
        if (state == STATE_STAGE_CLEAR) return "stage_clear";
        return "game_over";
    }

    private void logEvent(String message) {
        debugEvents.add(message);
        while (debugEvents.size() > 10) {
            debugEvents.remove(0);
        }
        Log.d(TAG, message);
    }

    private float getGroundY() {
        return getHeight() - dp(78);
    }

    private boolean circleHitsRect(float cx, float cy, float radius, RectF rect) {
        return GameMath.circleHitsRect(cx, cy, radius, rect.left, rect.top, rect.right, rect.bottom);
    }

    private boolean circleHitsCircle(float ax, float ay, float ar, float bx, float by, float br) {
        return GameMath.circleHitsCircle(ax, ay, ar, bx, by, br);
    }

    private float clamp(float value, float min, float max) {
        return GameMath.clamp(value, min, max);
    }

    private int clampInt(int value, int min, int max) {
        return GameMath.clampInt(value, min, max);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class StageConfig {
        final String name;
        final String line;
        final int season;
        final String bossName;
        final String hazardLabel;
        final int goalGates;
        final int bossHealth;
        final int baseSpeed;
        final float spawnSeconds;
        final int bossType;

        StageConfig(String name, String line, int season, String bossName, String hazardLabel, int goalGates, int bossHealth, int baseSpeed, float spawnSeconds, int bossType) {
            this.name = name;
            this.line = line;
            this.season = season;
            this.bossName = bossName;
            this.hazardLabel = hazardLabel;
            this.goalGates = goalGates;
            this.bossHealth = bossHealth;
            this.baseSpeed = baseSpeed;
            this.spawnSeconds = spawnSeconds;
            this.bossType = bossType;
        }
    }

    private static class Gate {
        float x;
        final float height;
        final float width;
        boolean passed = false;

        Gate(float x, float height, float width) {
            this.x = x;
            this.height = height;
            this.width = width;
        }
    }

    private static class Hazard {
        float x;
        float y;
        final float radius;
        final float speedMultiplier;
        final float phase;
        final String label;
        final Drawable drawable;
        boolean passed = false;

        Hazard(float x, float y, float radius, float speedMultiplier, float phase, String label, Drawable drawable) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedMultiplier = speedMultiplier;
            this.phase = phase;
            this.label = label;
            this.drawable = drawable;
        }
    }

    private static class Shot {
        float x;
        final float y;
        final float speed;
        final float radius;
        float wobble = 0f;

        Shot(float x, float y, float speed, float radius) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.radius = radius;
        }
    }
}
