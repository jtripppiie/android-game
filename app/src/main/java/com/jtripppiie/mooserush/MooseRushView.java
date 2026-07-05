package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MooseRushView extends View {
    public interface PhotoRequestListener {
        void onPhotoRequested();

        void onPhotoResetRequested();
    }

    private static final String TAG = "YouRushGame";

    private static final int STATE_SPLASH = 0;
    private static final int STATE_MENU = 1;
    private static final int STATE_MAP = 2;
    private static final int STATE_CUSTOMIZE = 3;
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;
    private static final int STATE_READY = 7;

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
    private static final String PREF_MUTED = "muted";
    private static final String PREF_XP = "xp";
    private static final String PREF_OUTFIT = "outfit";

    private static final int[] OUTFIT_COLORS = {
            Color.rgb(52, 134, 196),
            Color.rgb(228, 81, 87),
            Color.rgb(65, 155, 104),
            Color.rgb(156, 105, 202)
    };

    private static final String[] SEASONS = {
            "Summer",
            "Winter",
            "Midnight Sun",
            "Darkness"
    };

    private static final StageConfig[] STAGES = {
            new StageConfig("Midnight Sun Run", "Learn the jump. Clear the hurdles.", SEASON_MIDNIGHT_SUN, "Sunburn Sprite", "SUN", 5, 2, 150, 2.35f, 0),
            new StageConfig("Salmon Rush", "Fish arc in after the first hurdles.", SEASON_SUMMER, "Salmon Boss", "SALMON", 7, 3, 165, 2.15f, 1),
            new StageConfig("Moose Pass", "Moose enemies are real now. Take your time.", SEASON_SUMMER, "Moose Boss", "MOOSE", 8, 4, 178, 2.05f, 2),
            new StageConfig("Dark Winter", "Low light, careful jumps.", SEASON_DARKNESS, "Darkness Boss", "DARK", 9, 4, 188, 1.95f, 3),
            new StageConfig("Bear Country", "A hard final level, not a blur.", SEASON_WINTER, "Bear Boss", "BEAR", 10, 6, 198, 1.85f, 4)
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Gate> gates = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Star> stars = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<String> debugEvents = new ArrayList<>();
    private final SharedPreferences prefs;
    private final GameState gameState = new GameState();
    private final GameAssets assets;
    private final SpriteRenderer spriteRenderer;
    private final VisualEffects effects;

    private final RectF primaryButtonBounds = new RectF();
    private final RectF secondaryButtonBounds = new RectF();
    private final RectF thirdButtonBounds = new RectF();
    private final RectF photoButtonBounds = new RectF();
    private final RectF resetPhotoButtonBounds = new RectF();
    private final RectF backButtonBounds = new RectF();
    private final RectF seasonButtonBounds = new RectF();
    private final RectF outfitButtonBounds = new RectF();
    private final RectF debugButtonBounds = new RectF();
    private final RectF muteButtonBounds = new RectF();
    private final RectF leftPadBounds = new RectF();
    private final RectF rightPadBounds = new RectF();
    private final RectF jumpPadBounds = new RectF();
    private final RectF firePadBounds = new RectF();
    private final RectF tempRect = new RectF();
    private final Path moosePath = new Path();
    private PhotoRequestListener photoRequestListener;
    private Bitmap playerPhoto;
    private Bitmap backdropCache;

    private int state = STATE_SPLASH;
    private int score = 0;
    private int runStageScore = 0;
    private int bestScore = 0;
    private int selectedStage = 0;
    private int selectedSeason = SEASON_MIDNIGHT_SUN;
    private int unlockedStage = 0;
    private int selectedOutfit = 0;
    private int gatesPassed = 0;
    private int stageAttempts = 0;
    private int bossHealth = 0;
    private int bossMaxHealth = 0;
    private int backdropCacheWidth = 0;
    private int backdropCacheHeight = 0;
    private int backdropCacheKey = Integer.MIN_VALUE;

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
    private float sceneryScroll = 0f;
    private float spriteClock = 0f;
    private float bossTimer = 0f;
    private float shotCooldown = 0f;
    private float damageFlash = 0f;
    private float screenShake = 0f;
    private float worldFlash = 0f;
    private float stageClearTimer = 0f;
    private float readyTimer = 0f;
    private float coyoteTimer = 0f;
    private float jumpBufferTimer = 0f;
    private float playerX;
    private float playerY;
    private float playerVelocityY;
    private float playerRadius;
    private float bossX = 0f;
    private float bossY = 0f;
    private float bossVelocityY = 0f;
    private SoundPool soundPool;
    private int soundJump;
    private int soundDoubleJump;
    private int soundThrow;
    private int soundHit;
    private int soundHurt;
    private int soundMedal;

    public MooseRushView(Context context) {
        super(context);
        setFocusable(true);

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        assets = new GameAssets(context);
        spriteRenderer = new SpriteRenderer(context);
        effects = new VisualEffects(context);
        bestScore = prefs.getInt(PREF_BEST_SCORE, 0);
        selectedStage = clampInt(prefs.getInt(PREF_SELECTED_STAGE, 0), 0, STAGES.length - 1);
        selectedSeason = clampInt(prefs.getInt(PREF_SELECTED_SEASON, STAGES[selectedStage].season), 0, SEASONS.length - 1);
        unlockedStage = clampInt(prefs.getInt(PREF_UNLOCKED_STAGE, 0), 0, STAGES.length - 1);
        selectedOutfit = clampInt(prefs.getInt(PREF_OUTFIT, 0), 0, OUTFIT_COLORS.length - 1);
        debugOverlay = prefs.getBoolean(PREF_DEBUG_OVERLAY, false);
        gameState.muted = prefs.getBoolean(PREF_MUTED, false);
        gameState.xp = prefs.getInt(PREF_XP, 0);
        gameState.updateLevel();

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        initAudio();

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

    public void clearPlayerPhoto() {
        this.playerPhoto = null;
        logEvent("Player photo reset to default runner.");
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
    protected void onDetachedFromWindow() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (backdropCache != null) {
            backdropCache.recycle();
            backdropCache = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        playerRadius = gameplayDp(18);
        playerX = width * 0.24f;
        playerY = height - dp(78) - playerRadius;
        grounded = true;
        backdropCache = null;
        backdropCacheWidth = 0;
        backdropCacheHeight = 0;
        backdropCacheKey = Integer.MIN_VALUE;
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
        } else if (state == STATE_READY) {
            drawWorld(canvas);
            drawHud(canvas);
            drawReadyScreen(canvas);
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
        updateVisualEffects(dt);
        if (state == STATE_SPLASH) {
            splashTimer += dt;
            if (splashTimer > 3.0f) {
                state = STATE_MENU;
                logEvent("Splash complete.");
            }
        } else if (state == STATE_READY) {
            readyTimer += dt;
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
                state = STATE_CUSTOMIZE;
                logEvent("Opened customize screen.");
            } else if (thirdButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                logEvent("Opened Alaska map.");
            } else if (debugButtonBounds.contains(x, y)) {
                debugOverlay = !debugOverlay;
                prefs.edit().putBoolean(PREF_DEBUG_OVERLAY, debugOverlay).apply();
                logEvent("Debug overlay " + (debugOverlay ? "on" : "off") + ".");
            } else if (muteButtonBounds.contains(x, y)) {
                gameState.muted = !gameState.muted;
                prefs.edit().putBoolean(PREF_MUTED, gameState.muted).apply();
                logEvent("Audio " + (gameState.muted ? "muted" : "on") + ".");
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
                backdropCacheKey = Integer.MIN_VALUE;
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
            } else if (resetPhotoButtonBounds.contains(x, y)) {
                playerPhoto = null;
                if (photoRequestListener != null) {
                    photoRequestListener.onPhotoResetRequested();
                }
                logEvent("Photo reset requested.");
            } else if (seasonButtonBounds.contains(x, y)) {
                selectedSeason = (selectedSeason + 1) % SEASONS.length;
                saveChoices();
                backdropCacheKey = Integer.MIN_VALUE;
                logEvent("Season set to " + SEASONS[selectedSeason] + ".");
            } else if (outfitButtonBounds.contains(x, y)) {
                selectedOutfit = (selectedOutfit + 1) % OUTFIT_COLORS.length;
                saveChoices();
                logEvent("Outfit color changed.");
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
            requestJump();
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

        if (state == STATE_READY) {
            state = STATE_RUNNING;
            readyTimer = 0f;
            lastFrameNanos = 0L;
            logEvent("Run started after ready screen.");
            return true;
        }

        if (state == STATE_RUNNING) {
            if (!isControlTouch(x, y)) {
                requestJump();
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
            requestJump();
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
        stars.clear();
        shots.clear();
        effects.clearAll();
        score = 0;
        runStageScore = 0;
        gatesPassed = 0;
        gameState.resetRun();
        bossTimer = 0f;
        bossActive = false;
        bossDefeated = false;
        bossHealth = stage.bossHealth;
        bossMaxHealth = stage.bossHealth;
        spawnCooldown = selectedStage == 0 ? 2.0f : 1.65f;
        hazardCooldown = selectedStage == 0 ? 4.0f : 3.25f;
        shotCooldown = 0f;
        groundScroll = 0f;
        sceneryScroll = 0f;
        spriteClock = 0f;
        damageFlash = 0f;
        screenShake = 0f;
        worldFlash = 0f;
        stageClearTimer = 0f;
        state = STATE_READY;
        readyTimer = 0f;
        playerX = getWidth() * 0.24f;
        playerY = getGroundY() - playerRadius;
        playerVelocityY = 0f;
        grounded = true;
        jumpsUsed = 0;
        coyoteTimer = RunnerTuning.COYOTE_SECONDS;
        jumpBufferTimer = 0f;
        stageAttempts++;
        logEvent("Start stage: " + stage.name + ". Goal " + stage.goalGates + " hurdles, boss HP " + stage.bossHealth + ".");
    }

    private void requestJump() {
        if (state != STATE_RUNNING) {
            return;
        }
        jumpBufferTimer = RunnerTuning.JUMP_BUFFER_SECONDS;
        tryConsumeJumpBuffer();
    }

    private void tryConsumeJumpBuffer() {
        if (jumpBufferTimer <= 0f) {
            return;
        }
        if (grounded || coyoteTimer > 0f) {
            playerVelocityY = -dp(RunnerTuning.GROUND_JUMP_VELOCITY_DP);
            grounded = false;
            coyoteTimer = 0f;
            jumpsUsed = 1;
            jumpBufferTimer = 0f;
            effects.spawnDustBurst(playerX, getGroundY(), 7, Color.argb(180, 235, 245, 248));
            screenShake = Math.max(screenShake, 0.04f);
            playSound("jump");
        } else if (jumpsUsed < 2) {
            playerVelocityY = -dp(RunnerTuning.DOUBLE_JUMP_VELOCITY_DP);
            jumpsUsed++;
            jumpBufferTimer = 0f;
            effects.spawnSparkBurst(playerX, playerY + playerRadius * 0.2f, 6, Color.rgb(255, 218, 121));
            playSound("double-jump");
        }
    }

    private void fireSnowball() {
        if (shotCooldown > 0f || state != STATE_RUNNING) {
            return;
        }
        shots.add(new Shot(playerX + playerRadius * 0.9f, playerY + playerRadius * 0.05f, dp(460), gameplayDp(5.5f)));
        effects.spawnSparkBurst(playerX + playerRadius * 1.05f, playerY, 5, Color.WHITE);
        shotCooldown = 0.32f;
        playSound("throw");
        logEvent("Snowball fired.");
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        StageConfig stage = STAGES[selectedStage];
        float gateSpeed = dp(RunnerTuning.scrollSpeedDp(stage.baseSpeed, gatesPassed));
        float gravity = selectedSeason == SEASON_DARKNESS ? dp(RunnerTuning.DARKNESS_GRAVITY_DP) : dp(RunnerTuning.GRAVITY_DP);
        float horizontalSpeed = dp(210);
        boolean wasGrounded = grounded;

        spriteClock += dt * (5.5f + Math.min(4.5f, gatesPassed * 0.28f));
        updateVisualEffects(dt);
        shotCooldown = Math.max(0f, shotCooldown - dt);
        damageFlash = Math.max(0f, damageFlash - dt);
        jumpBufferTimer = Math.max(0f, jumpBufferTimer - dt);
        coyoteTimer = grounded ? RunnerTuning.COYOTE_SECONDS : Math.max(0f, coyoteTimer - dt);
        tryConsumeJumpBuffer();

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
        sceneryScroll = (sceneryScroll + gateSpeed * dt) % dp(3600);

        float restY = getGroundY() - playerRadius;
        if (playerY >= restY) {
            playerY = restY;
            playerVelocityY = 0f;
            grounded = true;
            jumpsUsed = 0;
            coyoteTimer = RunnerTuning.COYOTE_SECONDS;
            if (!wasGrounded) {
                effects.spawnDustBurst(playerX, getGroundY(), 9, Color.argb(185, 235, 245, 248));
                screenShake = Math.max(screenShake, 0.035f);
            }
            tryConsumeJumpBuffer();
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
        updateStars(dt, gateSpeed);

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
                endGame("Antler hurdle bonk.");
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
            spawnCooldown = RunnerTuning.nextGateCooldown(STAGES[selectedStage].spawnSeconds, gatesPassed);
        }

        Iterator<Gate> iterator = gates.iterator();
        while (iterator.hasNext()) {
            Gate gate = iterator.next();
            gate.x -= gateSpeed * dt;

            if (!gate.passed && gate.x + gate.width < playerX) {
                gate.passed = true;
                gatesPassed++;
                gameState.gatesPassed = gatesPassed;
                addScore(10, "Hurdle cleared");
                effects.spawnScorePopup("+10", gate.x + gate.width / 2f, getGroundY() - gate.height - dp(20), Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(gate.x + gate.width / 2f, getGroundY() - gate.height, 8, Color.rgb(255, 218, 121));
                logEvent("Hurdle " + gatesPassed + "/" + STAGES[selectedStage].goalGates + " cleared.");
            }

            if (gate.x + gate.width < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void updateHazards(float dt, float speed) {
        if (gatesPassed < 2) {
            return;
        }
        hazardCooldown -= dt;
        if (hazardCooldown <= 0f) {
            spawnHazard();
            hazardCooldown = RunnerTuning.nextHazardCooldown(selectedStage, gatesPassed);
        }

        Iterator<Hazard> iterator = hazards.iterator();
        while (iterator.hasNext()) {
            Hazard hazard = iterator.next();
            hazard.x -= speed * hazard.speedMultiplier * dt;
            hazard.y += (float) Math.sin((spriteClock + hazard.phase) * 2.0f) * dp(0.55f);
            if (!hazard.passed && hazard.x + hazard.radius < playerX) {
                hazard.passed = true;
                addScore(4, hazard.label + " dodged");
                gameState.addCombo();
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
            if (random.nextFloat() < 0.45f) {
                effects.spawnParticle(shot.x - shot.radius * 0.8f, shot.y, -dp(45), (random.nextFloat() - 0.5f) * dp(26), shot.radius * 0.45f, Color.argb(150, 230, 248, 255), 0.22f);
            }
            if (shot.x > getWidth() + dp(24)) {
                iterator.remove();
                continue;
            }

            if (bossActive && circleHitsCircle(shot.x, shot.y, shot.radius, bossX, bossY, bossRadius())) {
                iterator.remove();
                bossHealth--;
                damageFlash = 0.16f;
                screenShake = Math.max(screenShake, 0.12f);
                worldFlash = Math.max(worldFlash, 0.10f);
                effects.spawnScorePopup("+25", bossX, bossY - bossRadius(), Color.rgb(255, 246, 207));
                effects.spawnSparkBurst(bossX, bossY, 14, Color.rgb(255, 98, 84));
                addScore(25, "Boss hit");
                playSound("hit");
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
        stars.clear();
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
        effects.spawnScorePopup("+" + (100 + selectedStage * 40), getWidth() / 2f, getHeight() * 0.38f, Color.rgb(255, 218, 121));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.38f, 26, Color.rgb(255, 218, 121));
        worldFlash = Math.max(worldFlash, 0.24f);
        playSound("medal");
        if (score > bestScore) {
            bestScore = score;
        }
        if (selectedStage < STAGES.length - 1 && unlockedStage < selectedStage + 1) {
            unlockedStage = selectedStage + 1;
        }
        prefs.edit()
                .putInt(PREF_BEST_SCORE, bestScore)
                .putInt(PREF_UNLOCKED_STAGE, unlockedStage)
                .putInt(PREF_XP, gameState.xp)
                .apply();
        state = STATE_STAGE_CLEAR;
        stageClearTimer = 0f;
        logEvent("Stage cleared: " + stage.name + ". Score " + score + ".");
    }

    private void endGame(String reason) {
        gameState.breakCombo();
        if (gameState.shieldActive) {
            gameState.shieldActive = false;
            resetAfterHit();
            playSound("hurt");
            logEvent("Shield absorbed hit: " + reason);
            return;
        }
        if (gameState.lives > 1) {
            gameState.lives--;
            resetAfterHit();
            playSound("hurt");
            logEvent("Life lost: " + reason + " Lives " + gameState.lives + ".");
            return;
        }
        state = STATE_GAME_OVER;
        screenShake = Math.max(screenShake, 0.18f);
        worldFlash = Math.max(worldFlash, 0.18f);
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt(PREF_BEST_SCORE, bestScore).putInt(PREF_XP, gameState.xp).apply();
        }
        logEvent("Game over: " + reason + " Score " + score + ".");
    }

    private void resetAfterHit() {
        gates.clear();
        hazards.clear();
        stars.clear();
        shots.clear();
        effects.clearParticles();
        playerX = getWidth() * 0.24f;
        playerY = getGroundY() - playerRadius;
        playerVelocityY = 0f;
        grounded = true;
        jumpsUsed = 0;
        coyoteTimer = RunnerTuning.COYOTE_SECONDS;
        jumpBufferTimer = 0f;
        spawnCooldown = 0.75f;
        hazardCooldown = 1.25f;
        damageFlash = 0.18f;
        screenShake = Math.max(screenShake, 0.16f);
        worldFlash = Math.max(worldFlash, 0.16f);
        effects.spawnDustBurst(playerX, getGroundY(), 14, Color.argb(190, 255, 255, 255));
    }

    private void addScore(int amount, String reason) {
        score += amount;
        runStageScore += amount;
        gameState.addScore(amount);
        if (amount >= 10) {
            logEvent(reason + " +" + amount + ".");
        }
    }

    private void spawnGate() {
        float gateWidth = gameplayDp(34) + random.nextFloat() * gameplayDp(18);
        float hurdleHeight = RunnerTuning.gateHeight(getResources().getDisplayMetrics().density, selectedStage, gatesPassed, random.nextFloat());
        gates.add(new Gate(getWidth() + gateWidth, hurdleHeight, gateWidth));
        if (random.nextFloat() < 0.72f) {
            float starY = getGroundY() - hurdleHeight - gameplayDp(34 + random.nextFloat() * 18);
            stars.add(new Star(getWidth() + gateWidth + gameplayDp(46), Math.max(dp(86), starY), gameplayDp(8)));
        }
    }

    private void updateStars(float dt, float speed) {
        Iterator<Star> iterator = stars.iterator();
        while (iterator.hasNext()) {
            Star star = iterator.next();
            star.x -= speed * dt;
            star.spin += dt * 7f;
            if (circleHitsCircle(playerX, playerY, playerRadius * 0.95f, star.x, star.y, star.radius * 1.35f)) {
                iterator.remove();
                gameState.stars++;
                effects.spawnScorePopup("+15", star.x, star.y - dp(12), Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(star.x, star.y, 12, Color.rgb(255, 218, 121));
                addScore(15, "Star collected");
                playSound("medal");
                continue;
            }
            if (star.x + star.radius < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void updateVisualEffects(float dt) {
        screenShake = Math.max(0f, screenShake - dt);
        worldFlash = Math.max(0f, worldFlash - dt);
        effects.update(dt);
    }

    private void spawnHazard() {
        StageConfig stage = STAGES[selectedStage];
        float radius = gameplayDp(15 + Math.min(8, selectedStage * 2));
        // Bias toward low, jump-over heights, with the occasional mid-air flyer.
        float lowBand = getGroundY() - radius - dp(6);
        float y = lowBand;
        if (random.nextFloat() < 0.35f) {
            y = getGroundY() - dp(120) - random.nextFloat() * dp(60);
        }
        Drawable drawable = assets.hazardForStage(selectedStage);
        hazards.add(new Hazard(getWidth() + dp(50), y, radius, 0.86f + random.nextFloat() * 0.32f, random.nextFloat() * 4f, stage.hazardLabel, drawable));
    }

    private boolean hitsGate(Gate gate) {
        tempRect.set(gate.x + dp(4), getGroundY() - gate.height + dp(6), gate.x + gate.width - dp(4), getGroundY());
        return circleHitsRect(playerX, playerY, playerRadius * 0.68f, tempRect);
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
        canvas.drawText("Alaska platform runner", getWidth() / 2f, getHeight() * 0.57f, textPaint);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("For the best playing experience, rotate your phone.", getWidth() / 2f, getHeight() * 0.68f, textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap to continue", getWidth() / 2f, getHeight() * 0.78f, textPaint);
    }

    private void drawMenuScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "YOU RUSH: ALASKA", "Upload your face. Beat local chaos.");

        if (isLandscape()) {
            drawCharacterPreview(canvas, getWidth() * 0.28f, getHeight() * 0.42f, dp(27));
        } else {
            drawCharacterPreview(canvas, getWidth() / 2f, getHeight() * 0.33f, dp(28));
        }

        float y = isLandscape() ? getHeight() * 0.34f : getHeight() * 0.53f;
        float x = isLandscape() ? getWidth() * 0.68f : getWidth() / 2f;
        setButton(primaryButtonBounds, x, y, dp(230), dp(44));
        setButton(secondaryButtonBounds, x, y + dp(54), dp(230), dp(44));
        setButton(thirdButtonBounds, x, y + dp(108), dp(230), dp(44));
        setButton(debugButtonBounds, x - dp(58), y + dp(160), dp(104), dp(36));
        setButton(muteButtonBounds, x + dp(58), y + dp(160), dp(104), dp(36));

        drawButton(canvas, primaryButtonBounds, "PLAY " + STAGES[selectedStage].name);
        drawButton(canvas, secondaryButtonBounds, playerPhoto == null ? "CREATE YOUR SPRITE" : "EDIT YOUR SPRITE");
        drawButton(canvas, thirdButtonBounds, "ALASKA MAP");
        drawSmallButton(canvas, debugButtonBounds, "DEBUG: " + (debugOverlay ? "ON" : "OFF"));
        drawSmallButton(canvas, muteButtonBounds, gameState.muted ? "MUTED" : "AUDIO");

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

        float startY = isLandscape() ? dp(82) : dp(132);
        float gap = isLandscape() ? dp(48) : dp(76);
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
            canvas.drawRoundRect(node, isLandscape() ? dp(10) : dp(16), isLandscape() ? dp(10) : dp(16), paint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(selected ? Color.rgb(23, 29, 38) : Color.WHITE);
            textPaint.setTextSize(isLandscape() ? dp(14) : dp(17));
            canvas.drawText((i + 1) + ". " + STAGES[i].name, node.left + dp(16), node.top + (isLandscape() ? dp(18) : dp(27)), textPaint);

            textPaint.setTextSize(isLandscape() ? dp(10) : dp(12));
            textPaint.setColor(selected ? Color.rgb(58, 65, 78) : Color.rgb(204, 223, 230));
            canvas.drawText(STAGES[i].line, node.left + dp(16), node.top + (isLandscape() ? dp(35) : dp(49)), textPaint);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(isLandscape() ? dp(10) : dp(11));
            String bossLabel = unlocked ? STAGES[i].bossName : "LOCKED";
            canvas.drawText(bossLabel, node.right - dp(14), node.top + (isLandscape() ? dp(35) : dp(49)), textPaint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Tester note: locked nodes are visible so the full Alaska template is inspectable.", getWidth() / 2f, getHeight() - dp(24), textPaint);
    }

    private void drawCustomizeScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "CREATE YOUR SPRITE", "Pick a photo, preview your runner, then play.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        if (isLandscape()) {
            drawCharacterPreview(canvas, getWidth() * 0.30f, getHeight() * 0.45f, dp(34));
        } else {
            drawCharacterPreview(canvas, getWidth() / 2f, getHeight() * 0.33f, dp(34));
        }

        float y = isLandscape() ? getHeight() * 0.29f : getHeight() * 0.46f;
        float x = isLandscape() ? getWidth() * 0.68f : getWidth() / 2f;
        float buttonGap = isLandscape() ? dp(44) : dp(54);
        setButton(photoButtonBounds, x, y, dp(226), isLandscape() ? dp(40) : dp(48));
        setButton(resetPhotoButtonBounds, x, y + buttonGap, dp(226), isLandscape() ? dp(38) : dp(44));
        setButton(seasonButtonBounds, x, y + buttonGap * 2f, dp(226), isLandscape() ? dp(40) : dp(48));
        setButton(outfitButtonBounds, x, y + buttonGap * 3f, dp(226), isLandscape() ? dp(38) : dp(44));

        drawButton(canvas, photoButtonBounds, playerPhoto == null ? "SELECT PLAYER PHOTO" : "CHANGE PLAYER PHOTO");
        drawButton(canvas, resetPhotoButtonBounds, "RESET TO DEFAULT SPRITE");
        drawButton(canvas, seasonButtonBounds, "SEASON: " + SEASONS[selectedSeason]);
        drawButton(canvas, outfitButtonBounds, "OUTFIT COLOR");
        drawOutfitSwatch(canvas, outfitButtonBounds);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(playerPhoto == null ? Color.rgb(255, 218, 121) : Color.WHITE);
        canvas.drawText(playerPhoto == null ? "Default runner is active." : "Photo sprite ready for this run.", x, y + buttonGap * 3f + dp(42), textPaint);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Stage: " + STAGES[selectedStage].name + "   Controls: LEFT / RIGHT / JUMP / FIRE", x, y + buttonGap * 3f + dp(62), textPaint);
    }

    private void drawWorld(Canvas canvas) {
        int saved = canvas.save();
        if (screenShake > 0f) {
            float power = screenShake / 0.18f;
            float shakeX = (float) Math.sin(spriteClock * 19.0f) * dp(4.0f) * power;
            float shakeY = (float) Math.cos(spriteClock * 23.0f) * dp(2.4f) * power;
            canvas.translate(shakeX, shakeY);
        }

        drawAlaskaBackdrop(canvas);
        drawGround(canvas, getWidth());

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }
        for (Hazard hazard : hazards) {
            drawHazard(canvas, hazard);
        }
        for (Star star : stars) {
            drawStar(canvas, star);
        }
        for (Shot shot : shots) {
            drawShot(canvas, shot);
        }
        if (bossActive) {
            drawBoss(canvas);
        }

        effects.drawParticles(canvas);
        drawCharacter(canvas, playerX, playerY - playerRadius * 2.26f, playerRadius);
        effects.drawScorePopups(canvas);
        drawWorldFlash(canvas);
        canvas.restoreToCount(saved);
    }

    private void drawAlaskaBackdrop(Canvas canvas) {
        boolean dark = selectedSeason == SEASON_DARKNESS || STAGES[selectedStage].season == SEASON_DARKNESS;
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage == 4;
        ensureBackdropCache(dark, winter);
        if (backdropCache != null) {
            canvas.drawBitmap(backdropCache, 0, 0, null);
        } else {
            drawStaticBackdrop(canvas, dark, winter);
        }

        drawParallaxScenery(canvas, dark, winter);
    }

    private void ensureBackdropCache(boolean dark, boolean winter) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int key = (dark ? 1 : 0) + (winter ? 2 : 0) + selectedSeason * 10 + selectedStage * 100;
        if (backdropCache != null
                && backdropCacheWidth == width
                && backdropCacheHeight == height
                && backdropCacheKey == key) {
            return;
        }

        backdropCache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        backdropCacheWidth = width;
        backdropCacheHeight = height;
        backdropCacheKey = key;
        Canvas cacheCanvas = new Canvas(backdropCache);
        drawStaticBackdrop(cacheCanvas, dark, winter);
    }

    private void drawStaticBackdrop(Canvas canvas, boolean dark, boolean winter) {
        Drawable background = assets.background(dark, winter);
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

    private void drawParallaxScenery(Canvas canvas, boolean dark, boolean winter) {
        float horizon = getGroundY() - dp(72);
        paint.setStyle(Paint.Style.FILL);

        if (dark) {
            paint.setColor(Color.argb(78, 77, 219, 184));
            for (float x = -(sceneryScroll * 0.06f) % dp(420) - dp(120); x < getWidth() + dp(460); x += dp(420)) {
                PathCompat.ribbon(canvas, paint, x, dp(58), x + dp(90), dp(28), x + dp(190), dp(66), x + dp(282), dp(34), dp(15));
            }
        } else {
            paint.setColor(Color.argb(120, 255, 246, 205));
            for (float x = -(sceneryScroll * 0.07f) % dp(360) - dp(80); x < getWidth() + dp(390); x += dp(360)) {
                canvas.drawCircle(x, dp(58), dp(9), paint);
                canvas.drawCircle(x + dp(18), dp(52), dp(14), paint);
                canvas.drawCircle(x + dp(38), dp(59), dp(10), paint);
                canvas.drawRoundRect(x - dp(4), dp(58), x + dp(52), dp(69), dp(8), dp(8), paint);
            }
        }

        paint.setColor(dark ? Color.rgb(34, 52, 82) : Color.rgb(82, 126, 152));
        for (float x = -(sceneryScroll * 0.14f) % dp(260) - dp(70); x < getWidth() + dp(300); x += dp(260)) {
            PathCompat.triangle(canvas, paint, x, horizon, x + dp(82), horizon - dp(92), x + dp(172), horizon);
            paint.setColor(dark ? Color.rgb(78, 98, 132) : Color.rgb(218, 231, 225));
            PathCompat.triangle(canvas, paint, x + dp(56), horizon - dp(63), x + dp(82), horizon - dp(92), x + dp(110), horizon - dp(62));
            paint.setColor(dark ? Color.rgb(34, 52, 82) : Color.rgb(82, 126, 152));
        }

        paint.setColor(winter ? Color.rgb(232, 242, 246) : Color.rgb(56, 118, 78));
        for (float x = -(sceneryScroll * 0.34f) % dp(150) - dp(40); x < getWidth() + dp(180); x += dp(150)) {
            canvas.drawRoundRect(x, getGroundY() - dp(38), x + dp(16), getGroundY(), dp(4), dp(4), paint);
            PathCompat.triangle(canvas, paint, x - dp(18), getGroundY() - dp(34), x + dp(8), getGroundY() - dp(76), x + dp(34), getGroundY() - dp(34));
            paint.setColor(winter ? Color.rgb(178, 203, 214) : Color.rgb(38, 92, 62));
            PathCompat.triangle(canvas, paint, x - dp(10), getGroundY() - dp(42), x + dp(8), getGroundY() - dp(70), x + dp(24), getGroundY() - dp(42));
            paint.setColor(winter ? Color.rgb(232, 242, 246) : Color.rgb(56, 118, 78));
        }
    }

    private void drawGround(Canvas canvas, int width) {
        float groundY = getGroundY();
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage == 4;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(winter ? Color.rgb(225, 235, 238) : Color.rgb(46, 119, 82));
        canvas.drawRect(0, groundY, width, getHeight(), paint);

        paint.setColor(winter ? Color.rgb(248, 252, 253) : Color.rgb(88, 151, 91));
        canvas.drawRoundRect(0, groundY - dp(5), width, groundY + dp(9), dp(7), dp(7), paint);

        paint.setColor(winter ? Color.rgb(190, 207, 216) : Color.rgb(35, 86, 63));
        for (float x = -groundScroll; x < width + dp(60); x += dp(48)) {
            canvas.drawRoundRect(x, groundY + dp(12), x + dp(28), groundY + dp(20), dp(4), dp(4), paint);
        }

        paint.setColor(winter ? Color.argb(125, 137, 159, 174) : Color.argb(120, 23, 64, 44));
        for (float x = -(groundScroll * 1.32f) % dp(76) - dp(40); x < width + dp(90); x += dp(76)) {
            canvas.drawOval(x, groundY + dp(27), x + dp(44), groundY + dp(34), paint);
        }
    }

    private void drawGate(Canvas canvas, Gate gate) {
        float top = getGroundY() - gate.height;
        float ground = getGroundY();
        float postWidth = Math.max(dp(7), gate.width * 0.18f);
        float railHeight = Math.max(dp(8), gate.height * 0.16f);
        float railTop = top + dp(5);
        float railBottom = railTop + railHeight;
        float leftPost = gate.x + dp(3);
        float rightPost = gate.x + gate.width - postWidth - dp(3);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 0, 0, 0));
        canvas.drawOval(gate.x - dp(10), ground - dp(5), gate.x + gate.width + dp(10), ground + dp(7), paint);

        paint.setColor(Color.rgb(52, 35, 24));
        canvas.drawRoundRect(leftPost - dp(2), railTop, leftPost + postWidth + dp(2), ground, dp(5), dp(5), paint);
        canvas.drawRoundRect(rightPost - dp(2), railTop, rightPost + postWidth + dp(2), ground, dp(5), dp(5), paint);
        canvas.drawRoundRect(gate.x - dp(2), railTop - dp(2), gate.x + gate.width + dp(2), railBottom + dp(2), dp(7), dp(7), paint);

        paint.setColor(Color.rgb(134, 78, 38));
        canvas.drawRoundRect(leftPost, railTop + dp(2), leftPost + postWidth, ground, dp(4), dp(4), paint);
        canvas.drawRoundRect(rightPost, railTop + dp(2), rightPost + postWidth, ground, dp(4), dp(4), paint);
        canvas.drawRoundRect(gate.x, railTop, gate.x + gate.width, railBottom, dp(6), dp(6), paint);

        paint.setColor(Color.rgb(226, 169, 83));
        canvas.drawRoundRect(gate.x + dp(4), railTop + dp(2), gate.x + gate.width - dp(4), railTop + railHeight * 0.46f, dp(4), dp(4), paint);

        paint.setColor(Color.rgb(44, 30, 22));
        canvas.drawRoundRect(gate.x + dp(6), ground - dp(7), gate.x + postWidth + dp(8), ground, dp(3), dp(3), paint);
        canvas.drawRoundRect(gate.x + gate.width - postWidth - dp(8), ground - dp(7), gate.x + gate.width - dp(6), ground, dp(3), dp(3), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.rgb(233, 218, 181));
        canvas.drawLine(gate.x + dp(8), railBottom + dp(8), gate.x + gate.width - dp(8), ground - dp(13), paint);
        canvas.drawLine(gate.x + gate.width - dp(8), railBottom + dp(8), gate.x + dp(8), ground - dp(13), paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHazard(Canvas canvas, Hazard hazard) {
        boolean moose = "MOOSE".equals(hazard.label);
        float xRadius = moose ? hazard.radius * 1.45f : hazard.radius;
        float yRadius = moose ? hazard.radius * 0.96f : hazard.radius;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(85, 0, 0, 0));
        canvas.drawOval(hazard.x - xRadius * 0.72f, hazard.y + yRadius * 0.66f, hazard.x + xRadius * 0.72f, hazard.y + yRadius * 0.88f, paint);

        paint.setColor(Color.argb(80, 255, 246, 207));
        canvas.drawOval(hazard.x - xRadius * 1.08f, hazard.y - yRadius * 1.12f, hazard.x + xRadius * 1.08f, hazard.y + yRadius * 1.12f, paint);

        if (moose) {
            drawMoose(canvas, hazard.x, hazard.y, yRadius);
        } else if (hazard.drawable != null) {
            drawDrawable(canvas, hazard.drawable, hazard.x - xRadius, hazard.y - yRadius, hazard.x + xRadius, hazard.y + yRadius);
        } else {
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(hazard.x, hazard.y, hazard.radius, paint);
        }
        drawHazardBadge(canvas, hazard, yRadius);
    }

    private void drawHazardBadge(Canvas canvas, Hazard hazard, float yRadius) {
        float badgeWidth = Math.max(dp(32), hazard.label.length() * dp(7));
        float badgeHeight = dp(15);
        float left = hazard.x - badgeWidth / 2f;
        float top = hazard.y + yRadius + dp(5);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 18, 25, 34));
        canvas.drawRoundRect(left, top, left + badgeWidth, top + badgeHeight, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(180, 255, 218, 121));
        canvas.drawRoundRect(left, top, left + badgeWidth, top + badgeHeight, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText(hazard.label, hazard.x, top + dp(11), textPaint);
    }

    private void drawStar(Canvas canvas, Star star) {
        float pulse = 1f + (float) Math.sin(star.spin) * 0.12f;
        float r = star.radius * pulse;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 218, 121));
        PathCompat.star(canvas, paint, star.x, star.y, r);
        paint.setColor(Color.argb(170, 255, 255, 255));
        canvas.drawCircle(star.x - r * 0.25f, star.y - r * 0.32f, r * 0.22f, paint);
    }

    private void drawShot(Canvas canvas, Shot shot) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(90, 200, 238, 255));
        canvas.drawCircle(shot.x - shot.radius * 0.9f, shot.y + (float) Math.sin(shot.wobble) * dp(2), shot.radius * 1.7f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(shot.x, shot.y + (float) Math.sin(shot.wobble) * dp(2), shot.radius, paint);
        paint.setColor(Color.rgb(180, 220, 230));
        canvas.drawCircle(shot.x - shot.radius * 0.3f, shot.y - shot.radius * 0.25f, shot.radius * 0.35f, paint);
    }

    private void drawWorldFlash(Canvas canvas) {
        if (worldFlash <= 0f) {
            return;
        }
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(120f * (worldFlash / 0.24f)), 255, 246, 207));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    private void drawBoss(Canvas canvas) {
        Drawable bossDrawable = assets.bossForStage(selectedStage);
        float radius = bossRadius();
        boolean mooseBoss = selectedStage == 2;
        float xRadius = mooseBoss ? radius * 1.72f : radius * 1.30f;
        float yRadius = mooseBoss ? radius * 1.08f : radius * 1.30f;

        if (damageFlash > 0f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawOval(bossX - xRadius * 1.12f, bossY - yRadius * 1.12f, bossX + xRadius * 1.12f, bossY + yRadius * 1.12f, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(90, 255, 218, 121));
        canvas.drawOval(bossX - xRadius * 1.12f, bossY - yRadius * 1.12f, bossX + xRadius * 1.12f, bossY + yRadius * 1.12f, paint);
        paint.setColor(Color.argb(120, 0, 0, 0));
        canvas.drawOval(bossX - xRadius * 0.88f, bossY + yRadius * 0.80f, bossX + xRadius * 0.88f, bossY + yRadius * 1.08f, paint);

        if (mooseBoss) {
            drawMoose(canvas, bossX, bossY, yRadius * 0.92f);
        } else if (bossDrawable != null) {
            drawDrawable(canvas, bossDrawable, bossX - xRadius, bossY - yRadius, bossX + xRadius, bossY + yRadius);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(bossX, bossY, radius, paint);
        }

        drawBossHealthBar(canvas);
    }

    private void drawMoose(Canvas canvas, float x, float y, float size) {
        float bodyLeft = x - size * 1.42f;
        float bodyRight = x + size * 0.42f;
        float bodyTop = y - size * 0.45f;
        float bodyBottom = y + size * 0.32f;
        float legTop = y + size * 0.10f;
        float hoofY = y + size * 0.88f;
        int outlineColor = Color.rgb(18, 11, 8);
        int darkBrown = Color.rgb(42, 25, 15);
        int bodyBrown = Color.rgb(90, 55, 31);
        int highlightBrown = Color.rgb(122, 77, 43);
        int antlerColor = Color.rgb(226, 211, 166);
        int antlerShadow = Color.rgb(122, 101, 67);

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);

        moosePath.reset();
        moosePath.moveTo(bodyLeft + size * 0.10f, bodyTop + size * 0.25f);
        moosePath.cubicTo(bodyLeft + size * 0.35f, bodyTop - size * 0.10f, bodyLeft + size * 1.02f, bodyTop - size * 0.16f, bodyRight - size * 0.05f, bodyTop + size * 0.05f);
        moosePath.cubicTo(bodyRight + size * 0.18f, bodyTop + size * 0.18f, bodyRight + size * 0.20f, bodyBottom - size * 0.04f, bodyRight - size * 0.02f, bodyBottom + size * 0.08f);
        moosePath.cubicTo(bodyLeft + size * 1.15f, bodyBottom + size * 0.24f, bodyLeft + size * 0.32f, bodyBottom + size * 0.18f, bodyLeft + size * 0.02f, y + size * 0.07f);
        moosePath.cubicTo(bodyLeft - size * 0.08f, y - size * 0.02f, bodyLeft - size * 0.02f, bodyTop + size * 0.34f, bodyLeft + size * 0.10f, bodyTop + size * 0.25f);
        moosePath.close();

        paint.setColor(outlineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.18f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawPath(moosePath, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bodyBrown);
        canvas.drawPath(moosePath, paint);

        paint.setColor(darkBrown);
        moosePath.reset();
        moosePath.moveTo(bodyLeft + size * 0.05f, bodyTop + size * 0.16f);
        moosePath.cubicTo(bodyLeft + size * 0.28f, bodyTop - size * 0.27f, bodyLeft + size * 0.78f, bodyTop - size * 0.22f, bodyLeft + size * 1.02f, bodyTop + size * 0.06f);
        moosePath.cubicTo(bodyLeft + size * 0.72f, bodyTop + size * 0.20f, bodyLeft + size * 0.38f, bodyTop + size * 0.20f, bodyLeft + size * 0.05f, bodyTop + size * 0.16f);
        canvas.drawPath(moosePath, paint);

        paint.setColor(highlightBrown);
        tempRect.set(bodyLeft + size * 0.25f, bodyTop + size * 0.13f, bodyRight - size * 0.08f, bodyTop + size * 0.34f);
        canvas.drawOval(tempRect, paint);

        drawMooseLeg(canvas, x - size * 1.05f, legTop, hoofY, size, darkBrown, outlineColor, -0.20f);
        drawMooseLeg(canvas, x - size * 0.46f, legTop + size * 0.05f, hoofY, size, darkBrown, outlineColor, 0.18f);
        drawMooseLeg(canvas, x + size * 0.02f, legTop + size * 0.04f, hoofY, size, darkBrown, outlineColor, -0.12f);
        drawMooseLeg(canvas, x + size * 0.36f, legTop - size * 0.03f, hoofY, size, darkBrown, outlineColor, 0.18f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outlineColor);
        tempRect.set(x + size * 0.23f, y - size * 0.48f, x + size * 0.92f, y + size * 0.03f);
        canvas.drawRoundRect(tempRect, size * 0.18f, size * 0.18f, paint);

        paint.setColor(bodyBrown);
        tempRect.set(x + size * 0.29f, y - size * 0.43f, x + size * 0.88f, y);
        canvas.drawRoundRect(tempRect, size * 0.16f, size * 0.16f, paint);

        paint.setColor(bodyBrown);
        tempRect.set(x + size * 0.72f, y - size * 0.34f, x + size * 1.38f, y + size * 0.04f);
        canvas.drawRoundRect(tempRect, size * 0.18f, size * 0.18f, paint);

        paint.setColor(outlineColor);
        canvas.drawOval(x + size * 1.20f, y - size * 0.15f, x + size * 1.46f, y + size * 0.02f, paint);
        canvas.drawCircle(x + size * 1.12f, y - size * 0.24f, Math.max(dp(1.7f), size * 0.055f), paint);

        moosePath.reset();
        moosePath.moveTo(x + size * 0.52f, y - size * 0.47f);
        moosePath.lineTo(x + size * 0.76f, y - size * 0.78f);
        moosePath.lineTo(x + size * 0.87f, y - size * 0.50f);
        moosePath.close();
        canvas.drawPath(moosePath, paint);

        moosePath.reset();
        moosePath.moveTo(x + size * 0.50f, y - size * 0.02f);
        moosePath.cubicTo(x + size * 0.74f, y + size * 0.23f, x + size * 0.65f, y + size * 0.54f, x + size * 0.48f, y + size * 0.70f);
        moosePath.cubicTo(x + size * 0.50f, y + size * 0.43f, x + size * 0.35f, y + size * 0.21f, x + size * 0.28f, y + size * 0.06f);
        moosePath.close();
        canvas.drawPath(moosePath, paint);

        paint.setColor(antlerShadow);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(dp(3), size * 0.14f));
        canvas.drawLine(x + size * 0.72f, y - size * 0.54f, x + size * 0.46f, y - size * 0.92f, paint);
        canvas.drawLine(x + size * 0.74f, y - size * 0.54f, x + size * 1.03f, y - size * 0.92f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(antlerColor);
        drawPalmAntler(canvas, x + size * 0.47f, y - size * 0.92f, size, -1f);
        drawPalmAntler(canvas, x + size * 1.03f, y - size * 0.92f, size, 1f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(dp(2), size * 0.08f));
        paint.setColor(antlerColor);
        canvas.drawLine(x + size * 0.72f, y - size * 0.54f, x + size * 0.47f, y - size * 0.88f, paint);
        canvas.drawLine(x + size * 0.74f, y - size * 0.54f, x + size * 1.03f, y - size * 0.88f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawMooseLeg(Canvas canvas, float legX, float legTop, float hoofY, float size, int legColor, int outlineColor, float angle) {
        float kneeX = legX + angle * size;
        float kneeY = legTop + size * 0.36f;
        float footX = kneeX + angle * size * 0.32f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(size * 0.22f);
        paint.setColor(outlineColor);
        canvas.drawLine(legX, legTop, kneeX, kneeY, paint);
        canvas.drawLine(kneeX, kneeY, footX, hoofY, paint);

        paint.setStrokeWidth(size * 0.14f);
        paint.setColor(legColor);
        canvas.drawLine(legX, legTop, kneeX, kneeY, paint);
        canvas.drawLine(kneeX, kneeY, footX, hoofY, paint);

        paint.setStrokeWidth(size * 0.16f);
        paint.setColor(outlineColor);
        canvas.drawLine(footX - size * 0.22f, hoofY, footX + size * 0.28f, hoofY, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawPalmAntler(Canvas canvas, float baseX, float baseY, float size, float direction) {
        moosePath.reset();
        moosePath.moveTo(baseX, baseY);
        moosePath.cubicTo(baseX + direction * size * 0.08f, baseY - size * 0.28f, baseX + direction * size * 0.35f, baseY - size * 0.36f, baseX + direction * size * 0.48f, baseY - size * 0.18f);
        moosePath.lineTo(baseX + direction * size * 0.60f, baseY - size * 0.42f);
        moosePath.lineTo(baseX + direction * size * 0.74f, baseY - size * 0.12f);
        moosePath.lineTo(baseX + direction * size * 0.48f, baseY - size * 0.03f);
        moosePath.lineTo(baseX + direction * size * 0.70f, baseY + size * 0.17f);
        moosePath.lineTo(baseX + direction * size * 0.34f, baseY + size * 0.12f);
        moosePath.lineTo(baseX + direction * size * 0.17f, baseY + size * 0.28f);
        moosePath.lineTo(baseX + direction * size * 0.07f, baseY + size * 0.07f);
        moosePath.close();
        canvas.drawPath(moosePath, paint);
    }

    private void drawBossHealthBar(Canvas canvas) {
        float left = dp(62);
        float top = dp(70);
        float right = getWidth() - dp(62);
        float bottom = top + dp(10);
        float pct = bossMaxHealth <= 0 ? 0f : bossHealth / (float) bossMaxHealth;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 0, 0, 0));
        canvas.drawRoundRect(left, top, right, bottom, dp(6), dp(6), paint);
        paint.setColor(Color.rgb(255, 98, 84));
        canvas.drawRoundRect(left, top, left + (right - left) * pct, bottom, dp(6), dp(6), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(STAGES[selectedStage].bossName + " HP " + bossHealth + "/" + bossMaxHealth, getWidth() / 2f, top - dp(6), textPaint);
    }

    private float bossRadius() {
        if (selectedStage == 4) return gameplayDp(38);
        if (selectedStage == 2) return gameplayDp(34);
        return gameplayDp(30);
    }

    private void drawCharacter(Canvas canvas, float x, float y, float radius) {
        spriteRenderer.drawRunner(canvas, playerFrame(x, y, radius));
    }

    private void drawCharacterPreview(Canvas canvas, float x, float y, float radius) {
        spriteRenderer.drawStanding(canvas, playerFrame(x, y, radius));
    }

    private SpriteRenderer.PlayerFrame playerFrame(float x, float y, float radius) {
        int outfitColor = playerPhoto == null ? Color.rgb(255, 218, 121) : OUTFIT_COLORS[selectedOutfit];
        return new SpriteRenderer.PlayerFrame(x, y, radius, spriteClock, grounded, playerVelocityY, playerPhoto, outfitColor);
    }

    private void drawReadyScreen(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(56), dp(430));
        float panelHeight = dp(168);
        float left = (getWidth() - panelWidth) / 2f;
        float top = Math.max(dp(84), getHeight() * 0.26f);
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(220, 12, 20, 31));
        canvas.drawRoundRect(panel, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(panel, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(13));
        canvas.drawText("LEVEL " + (selectedStage + 1), getWidth() / 2f, top + dp(28), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(28));
        canvas.drawText(STAGES[selectedStage].name, getWidth() / 2f, top + dp(64), textPaint);

        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Start slow. Learn the jumps. Then reach the boss.", getWidth() / 2f, top + dp(96), textPaint);
        canvas.drawText("Tap when ready", getWidth() / 2f, top + dp(124), textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("JUMP clears hurdles. FIRE stops bosses.", getWidth() / 2f, top + dp(148), textPaint);
    }

    private void drawHud(Canvas canvas) {
        float barLeft = dp(10);
        float barTop = dp(10);
        float barRight = getWidth() - dp(10);
        float barBottom = dp(58);
        float progress = STAGES[selectedStage].goalGates <= 0 ? 0f : Math.min(1f, gatesPassed / (float) STAGES[selectedStage].goalGates);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(172, 10, 18, 29));
        canvas.drawRoundRect(barLeft, barTop, barRight, barBottom, dp(10), dp(10), paint);
        paint.setColor(Color.argb(54, 255, 255, 255));
        canvas.drawRoundRect(barLeft + dp(2), barTop + dp(2), barRight - dp(2), barTop + dp(18), dp(9), dp(9), paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(STAGES[selectedStage].name, dp(20), dp(24), textPaint);
        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(score), dp(20), dp(47), textPaint);

        float progressLeft = getWidth() * 0.34f;
        float progressRight = getWidth() * 0.66f;
        float progressTop = dp(19);
        float progressBottom = dp(29);
        paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRoundRect(progressLeft, progressTop, progressRight, progressBottom, dp(6), dp(6), paint);
        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(progressLeft, progressTop, progressLeft + (progressRight - progressLeft) * progress, progressBottom, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(190, 255, 255, 255));
        canvas.drawRoundRect(progressLeft, progressTop, progressRight, progressBottom, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText(gatesPassed + "/" + STAGES[selectedStage].goalGates, getWidth() / 2f, dp(44), textPaint);
        textPaint.setColor(Color.WHITE);
        String comboLabel = "Combo x" + Math.max(1, gameState.combo + 1);
        canvas.drawText(comboLabel, getWidth() / 2f, dp(57), textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Best " + bestScore, getWidth() - dp(20), dp(25), textPaint);
        canvas.drawText("Lv " + gameState.level + (gameState.muted ? "  MUTE" : ""), getWidth() - dp(20), dp(45), textPaint);

        drawHudIcons(canvas);

        if (debugOverlay) {
            drawDebugOverlay(canvas);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawHudIcons(Canvas canvas) {
        float lifeX = dp(103);
        float lifeY = dp(42);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            paint.setColor(i < gameState.lives ? Color.rgb(255, 98, 84) : Color.argb(110, 255, 255, 255));
            canvas.drawCircle(lifeX + i * dp(14), lifeY, dp(4.8f), paint);
        }

        float starX = getWidth() - dp(104);
        float starY = dp(42);
        paint.setColor(Color.rgb(255, 218, 121));
        PathCompat.star(canvas, paint, starX, starY - dp(1), dp(6));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(gameState.stars), starX + dp(12), starY + dp(4), textPaint);

        if (shotCooldown > 0f) {
            float pct = Math.min(1f, shotCooldown / 0.32f);
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawCircle(getWidth() - dp(39), dp(42), dp(7), paint);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(getWidth() - dp(39), dp(42), dp(7) * (1f - pct), paint);
        }
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

    @SuppressWarnings("deprecation")
    private void initAudio() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(4)
                    .build();
        } else {
            soundPool = new SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        soundJump = loadGeneratedSound("jump", 660, 80);
        soundDoubleJump = loadGeneratedSound("double_jump", 820, 80);
        soundThrow = loadGeneratedSound("throw", 440, 70);
        soundHit = loadGeneratedSound("hit", 560, 90);
        soundHurt = loadGeneratedSound("hurt", 190, 140);
        soundMedal = loadGeneratedSound("medal", 980, 160);
    }

    private void playSound(String event) {
        if (gameState.muted || soundPool == null) {
            return;
        }
        int soundId = 0;
        if ("jump".equals(event)) {
            soundId = soundJump;
        } else if ("double-jump".equals(event)) {
            soundId = soundDoubleJump;
        } else if ("throw".equals(event)) {
            soundId = soundThrow;
        } else if ("hit".equals(event)) {
            soundId = soundHit;
        } else if ("hurt".equals(event)) {
            soundId = soundHurt;
        } else if ("medal".equals(event)) {
            soundId = soundMedal;
        }
        if (soundId != 0) {
            soundPool.play(soundId, 0.45f, 0.45f, 1, 0, 1f);
        }
    }

    private int loadGeneratedSound(String name, int frequencyHz, int durationMs) {
        try {
            File file = new File(getContext().getCacheDir(), "you_rush_" + name + ".wav");
            writeToneWav(file, frequencyHz, durationMs);
            return soundPool.load(file.getAbsolutePath(), 1);
        } catch (IOException exception) {
            Log.w(TAG, "Unable to prepare sound " + name, exception);
            return 0;
        }
    }

    private void writeToneWav(File file, int frequencyHz, int durationMs) throws IOException {
        int sampleRate = 22050;
        int sampleCount = sampleRate * durationMs / 1000;
        int dataSize = sampleCount * 2;
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeAscii(out, "RIFF");
            writeLittleEndianInt(out, 36 + dataSize);
            writeAscii(out, "WAVEfmt ");
            writeLittleEndianInt(out, 16);
            writeLittleEndianShort(out, 1);
            writeLittleEndianShort(out, 1);
            writeLittleEndianInt(out, sampleRate);
            writeLittleEndianInt(out, sampleRate * 2);
            writeLittleEndianShort(out, 2);
            writeLittleEndianShort(out, 16);
            writeAscii(out, "data");
            writeLittleEndianInt(out, dataSize);

            for (int i = 0; i < sampleCount; i++) {
                double t = i / (double) sampleRate;
                double envelope = 1.0 - (i / (double) sampleCount);
                short sample = (short) (Math.sin(2.0 * Math.PI * frequencyHz * t) * envelope * 12000);
                writeLittleEndianShort(out, sample);
            }
        }
    }

    private void writeAscii(FileOutputStream out, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            out.write(value.charAt(i));
        }
    }

    private void writeLittleEndianInt(FileOutputStream out, int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
        out.write((value >> 16) & 0xff);
        out.write((value >> 24) & 0xff);
    }

    private void writeLittleEndianShort(FileOutputStream out, int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    private void drawVirtualControls(Canvas canvas) {
        float bottom = getHeight() - dp(14);
        float size = dp(48);
        leftPadBounds.set(dp(16), bottom - size, dp(16) + size, bottom);
        rightPadBounds.set(dp(74), bottom - size, dp(74) + size, bottom);
        jumpPadBounds.set(getWidth() - dp(132), bottom - size, getWidth() - dp(132) + size, bottom);
        firePadBounds.set(getWidth() - dp(74), bottom - size, getWidth() - dp(74) + size, bottom);

        drawControlButton(canvas, leftPadBounds, "◀", leftPressed);
        drawControlButton(canvas, rightPadBounds, "▶", rightPressed);
        drawControlButton(canvas, jumpPadBounds, "JUMP", jumpPressed);
        drawControlButton(canvas, firePadBounds, "FIRE", firePressed || shotCooldown > 0f);
    }

    private void drawControlButton(Canvas canvas, RectF bounds, String label, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 0, 0, 0));
        canvas.drawRoundRect(bounds.left, bounds.top + dp(3), bounds.right, bounds.bottom + dp(3), dp(14), dp(14), paint);
        paint.setColor(active ? Color.argb(230, 255, 218, 121) : Color.argb(178, 16, 25, 37));
        canvas.drawRoundRect(bounds, dp(14), dp(14), paint);
        paint.setColor(active ? Color.argb(90, 255, 255, 255) : Color.argb(46, 255, 255, 255));
        canvas.drawRoundRect(bounds.left + dp(3), bounds.top + dp(3), bounds.right - dp(3), bounds.top + bounds.height() * 0.45f, dp(11), dp(11), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(active ? Color.rgb(24, 30, 38) : Color.argb(220, 255, 255, 255));
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
        canvas.drawText("Score " + score + " · Hurdles " + gatesPassed + " · Attempt " + stageAttempts, getWidth() / 2f, top + dp(118), textPaint);
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
        float height = isLandscape() ? dp(76) : dp(112);
        canvas.drawRect(0, 0, getWidth(), height, paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(isLandscape() ? dp(10) : dp(12));
        canvas.drawText("TRIPPERDEELABS", getWidth() / 2f, isLandscape() ? dp(18) : dp(25), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(isLandscape() ? dp(22) : dp(28));
        canvas.drawText(title, getWidth() / 2f, isLandscape() ? dp(45) : dp(61), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(isLandscape() ? dp(11) : dp(13));
        canvas.drawText(subtitle, getWidth() / 2f, isLandscape() ? dp(65) : dp(88), textPaint);
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

    private void drawOutfitSwatch(Canvas canvas, RectF bounds) {
        float size = dp(20);
        float left = bounds.right - dp(34);
        float top = bounds.centerY() - size / 2f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(OUTFIT_COLORS[selectedOutfit]);
        canvas.drawRoundRect(left, top, left + size, top + size, dp(5), dp(5), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(24, 30, 38));
        canvas.drawRoundRect(left, top, left + size, top + size, dp(5), dp(5), paint);
        paint.setStyle(Paint.Style.FILL);
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
        setButton(bounds, getWidth() / 2f, centerY, width, height);
    }

    private void setButton(RectF bounds, float centerX, float centerY, float width, float height) {
        float left = centerX - width / 2f;
        bounds.set(left, centerY - height / 2f, left + width, centerY + height / 2f);
    }

    private void setBackButton() {
        backButtonBounds.set(dp(16), dp(16), dp(88), dp(52));
    }

    private RectF stageBounds(int index, float startY, float gap) {
        float left = dp(22);
        float right = getWidth() - dp(22);
        float top = startY + index * gap;
        return new RectF(left, top, right, top + (isLandscape() ? dp(42) : dp(62)));
    }

    private int findTappedStage(float x, float y) {
        float startY = isLandscape() ? dp(82) : dp(132);
        float gap = isLandscape() ? dp(48) : dp(76);
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
                .putInt(PREF_OUTFIT, selectedOutfit)
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
        if (state == STATE_READY) return "ready";
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

    private boolean isLandscape() {
        return getWidth() > getHeight();
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

    private float gameplayDp(float value) {
        return dp(value * 0.82f);
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

    private static class Star {
        float x;
        final float y;
        final float radius;
        float spin = 0f;

        Star(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    private static class PathCompat {
        private static final Path PATH = new Path();

        static void triangle(Canvas canvas, Paint paint, float ax, float ay, float bx, float by, float cx, float cy) {
            PATH.reset();
            PATH.moveTo(ax, ay);
            PATH.lineTo(bx, by);
            PATH.lineTo(cx, cy);
            PATH.close();
            canvas.drawPath(PATH, paint);
        }

        static void ribbon(Canvas canvas, Paint paint, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, float thickness) {
            PATH.reset();
            PATH.moveTo(ax, ay);
            PATH.cubicTo(bx, by, cx, cy, dx, dy);
            PATH.lineTo(dx, dy + thickness);
            PATH.cubicTo(cx, cy + thickness, bx, by + thickness, ax, ay + thickness);
            PATH.close();
            canvas.drawPath(PATH, paint);
        }

        static void star(Canvas canvas, Paint paint, float cx, float cy, float radius) {
            PATH.reset();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2.0 + i * Math.PI / 5.0;
                float r = i % 2 == 0 ? radius : radius * 0.46f;
                float x = cx + (float) Math.cos(angle) * r;
                float y = cy + (float) Math.sin(angle) * r;
                if (i == 0) {
                    PATH.moveTo(x, y);
                } else {
                    PATH.lineTo(x, y);
                }
            }
            PATH.close();
            canvas.drawPath(PATH, paint);
        }
    }
}
