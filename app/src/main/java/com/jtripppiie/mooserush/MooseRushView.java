package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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
import java.util.Calendar;
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
    private static final String PREF_TRAIL_TOKENS = "trail_tokens";
    private static final String PREF_UNLOCKED_OUTFITS = "unlocked_outfits";
    private static final String PREF_TOTAL_MISSIONS = "total_missions";
    private static final String PREF_TRAIL_BADGES = "trail_badges";
    private static final String PREF_DAILY_COMPLETED_DAY = "daily_completed_day";
    private static final String PREF_DAILY_STREAK = "daily_streak";

    private static final int[] OUTFIT_COLORS = {
            Color.rgb(52, 134, 196),
            Color.rgb(228, 81, 87),
            Color.rgb(65, 155, 104),
            Color.rgb(156, 105, 202),
            Color.rgb(255, 177, 70),
            Color.rgb(132, 213, 232),
            Color.rgb(255, 98, 84),
            Color.rgb(255, 246, 207)
    };
    private static final String[] OUTFIT_NAMES = {
            "BLUE TRAIL",
            "FIRE PARKA",
            "FOREST RUN",
            "PURPLE SKY",
            "GOLD RUSH",
            "AURORA ICE",
            "SALMON FLASH",
            "MIDNIGHT GLOW"
    };
    private static final int[] OUTFIT_TOKEN_COSTS = {
            0, 0, 0, 0, 35, 55, 75, 100
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
            new StageConfig("Dark Winter", "Eagles dive through low light.", SEASON_DARKNESS, "Eagle Boss", "EAGLE", 9, 4, 188, 1.95f, 3),
            new StageConfig("Bear Country", "Polar bears and wolves join the final sprint.", SEASON_WINTER, "Polar Bear Boss", "BEAR", 10, 6, 198, 1.85f, 4)
    };
    private static final int SPRITE_SHEET_FRAMES = 6;
    private static final float PLAYER_START_X_FRACTION = 0.265f;
    private static final float PLAYER_HEAD_DRAW_OFFSET = 2.18f;
    private static final float PLAYFIELD_BOTTOM_MARGIN_DP = 62f;
    private static final float JUMP_CEILING_TOP_MARGIN_DP = 40f;
    private static final float AURORA_METER_MAX = 100f;
    private static final float AURORA_RUSH_SECONDS = 6.5f;
    private static final float AURORA_FOCUS_SECONDS = 5.0f;
    private static final int BOSS_STATE_ENTER = 0;
    private static final int BOSS_STATE_TELL = 1;
    private static final int BOSS_STATE_ATTACK = 2;
    private static final int BOSS_STATE_RECOVER = 3;
    private static final int BOSS_PATTERN_LUNGE = 0;
    private static final int BOSS_PATTERN_SNOW_WAVE = 1;
    private static final int BOSS_PATTERN_SUMMON = 2;
    private static final int ATTACK_ICE = 0;
    private static final int ATTACK_SHOCKWAVE = 1;
    private static final int WEATHER_CLEAR = 0;
    private static final int WEATHER_AURORA = 1;
    private static final int WEATHER_RAIN = 2;
    private static final int WEATHER_SNOW = 3;
    private static final int[][] MOOSE_FRAME_TRIMS = {
            {14, 226, 362, 527},
            {0, 224, 362, 528},
            {0, 223, 362, 527},
            {0, 217, 362, 528},
            {0, 233, 362, 528},
            {0, 229, 336, 527}
    };
    private static final int[][] BEAR_FRAME_TRIMS = {
            {21, 263, 362, 499},
            {0, 292, 358, 502},
            {6, 293, 354, 500},
            {0, 288, 362, 502},
            {0, 292, 362, 504},
            {0, 283, 339, 504}
    };
    private static final int[][] POLAR_BEAR_FRAME_TRIMS = {
            {30, 273, 362, 476},
            {0, 275, 362, 476},
            {0, 275, 360, 477},
            {10, 275, 362, 476},
            {22, 275, 361, 477},
            {2, 275, 346, 477}
    };
    private static final int[][] WOLF_FRAME_TRIMS = {
            {28, 266, 362, 469},
            {0, 275, 362, 461},
            {0, 266, 362, 467},
            {0, 274, 362, 469},
            {0, 272, 362, 468},
            {0, 271, 342, 468}
    };
    private static final int[][] SALMON_FRAME_TRIMS = {
            {22, 296, 361, 436},
            {34, 262, 362, 436},
            {0, 260, 362, 456},
            {0, 298, 362, 438},
            {0, 308, 362, 492},
            {0, 305, 352, 497}
    };
    private static final int[][] EAGLE_FRAME_TRIMS = {
            {17, 146, 362, 490},
            {0, 258, 307, 494},
            {0, 396, 334, 596},
            {8, 221, 362, 501},
            {0, 151, 362, 505},
            {0, 376, 345, 509}
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spriteBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Random random = new Random();
    private final List<Gate> gates = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Star> stars = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<BossAttack> bossAttacks = new ArrayList<>();
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
    private final RectF dailyButtonBounds = new RectF();
    private final RectF leftPadBounds = new RectF();
    private final RectF rightPadBounds = new RectF();
    private final RectF jumpPadBounds = new RectF();
    private final RectF firePadBounds = new RectF();
    private final RectF tempRect = new RectF();
    private final Rect spriteSourceRect = new Rect();
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
    private int missionStarGoal = 0;
    private int missionComboGoal = 0;
    private int missionsCompleted = 0;
    private int totalMissionsCompleted = 0;
    private int trailTokens = 0;
    private int runTokensEarned = 0;
    private int runCacheTokens = 0;
    private int runExpeditionBonusTokens = 0;
    private int runFocusPickups = 0;
    private int runWeatherFronts = 0;
    private int routeMilestoneIndex = 0;
    private int unlockedOutfitMask = RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK;
    private int livesLostThisRun = 0;
    private int auroraRushes = 0;
    private int dailyCompletedDay = Integer.MIN_VALUE;
    private int dailyStreak = 0;
    private int dailyTokensEarned = 0;
    private int trailBadgeMask = 0;
    private int runBadgesEarnedMask = 0;
    private int runBadgeTokensEarned = 0;
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
    private boolean missionHurdlesComplete = false;
    private boolean missionStarsComplete = false;
    private boolean missionComboComplete = false;
    private boolean perfectRun = true;
    private boolean runRewardsAwarded = false;
    private boolean dailyBonusAwarded = false;
    private boolean campReached = false;
    private int jumpsUsed = 0;

    private long lastFrameNanos = 0L;
    private float splashTimer = 0f;
    private float spawnCooldown = 0f;
    private float hazardCooldown = 0f;
    private float groundScroll = 0f;
    private float sceneryScroll = 0f;
    private float spriteClock = 0f;
    private float runnerClock = 0f;
    private float bossTimer = 0f;
    private float bossStateTimer = 0f;
    private float bossPatternTimer = 0f;
    private float bossStunTimer = 0f;
    private float bossTellX = 0f;
    private float bossTellY = 0f;
    private float shotCooldown = 0f;
    private float damageFlash = 0f;
    private float screenShake = 0f;
    private float worldFlash = 0f;
    private float stageClearTimer = 0f;
    private float readyTimer = 0f;
    private float runCalloutTimer = 0f;
    private float bossWarningTimer = 0f;
    private float coyoteTimer = 0f;
    private float jumpBufferTimer = 0f;
    private float auroraMeter = 0f;
    private float auroraRushTimer = 0f;
    private float auroraFocusTimer = 0f;
    private float routeMilestoneTimer = 0f;
    private float weatherFrontTimer = 0f;
    private float weatherFrontDuration = 0f;
    private float playerX;
    private float playerY;
    private float playerVelocityY;
    private float playerRadius;
    private float bossX = 0f;
    private float bossY = 0f;
    private float bossVelocityY = 0f;
    private int bossState = BOSS_STATE_ENTER;
    private int bossPattern = BOSS_PATTERN_LUNGE;
    private int bossPatternCount = 0;
    private int weatherFront = WEATHER_CLEAR;
    private String runCallout = "";
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
        trailTokens = prefs.getInt(PREF_TRAIL_TOKENS, 0);
        unlockedOutfitMask = prefs.getInt(PREF_UNLOCKED_OUTFITS, RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK);
        totalMissionsCompleted = prefs.getInt(PREF_TOTAL_MISSIONS, 0);
        trailBadgeMask = prefs.getInt(PREF_TRAIL_BADGES, 0);
        dailyCompletedDay = prefs.getInt(PREF_DAILY_COMPLETED_DAY, Integer.MIN_VALUE);
        dailyStreak = prefs.getInt(PREF_DAILY_STREAK, 0);
        if (!isOutfitUnlocked(selectedOutfit)) {
            selectedOutfit = firstUnlockedOutfit();
        }
        debugOverlay = prefs.getBoolean(PREF_DEBUG_OVERLAY, false);
        gameState.muted = prefs.getBoolean(PREF_MUTED, false);
        gameState.xp = prefs.getInt(PREF_XP, 0);
        gameState.updateLevel();

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        spriteBitmapPaint.setFilterBitmap(true);

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
        playerRadius = gameplayDp(20.5f);
        playerX = playerStartX();
        playerY = getGroundY() - playerRadius;
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
        runnerClock += dt * 0.75f;
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
            } else if (dailyButtonBounds.contains(x, y)) {
                startDailyRush();
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
                handleOutfitTap();
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
        powerUps.clear();
        shots.clear();
        bossAttacks.clear();
        effects.clearAll();
        selectedOutfit = effectiveOutfitIndex();
        score = 0;
        runStageScore = 0;
        gatesPassed = 0;
        gameState.resetRun();
        setupRunMissions(stage);
        bossTimer = 0f;
        bossStateTimer = 0f;
        bossPatternTimer = 0f;
        bossStunTimer = 0f;
        bossState = BOSS_STATE_ENTER;
        bossPattern = BOSS_PATTERN_LUNGE;
        bossPatternCount = 0;
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
        runnerClock = 0f;
        damageFlash = 0f;
        screenShake = 0f;
        worldFlash = 0f;
        stageClearTimer = 0f;
        runCalloutTimer = 0f;
        bossWarningTimer = 0f;
        auroraMeter = 0f;
        auroraRushTimer = 0f;
        auroraFocusTimer = 0f;
        auroraRushes = 0;
        runTokensEarned = 0;
        runCacheTokens = 0;
        runExpeditionBonusTokens = 0;
        runFocusPickups = 0;
        runWeatherFronts = 0;
        routeMilestoneIndex = 0;
        livesLostThisRun = 0;
        dailyTokensEarned = 0;
        runBadgesEarnedMask = 0;
        runBadgeTokensEarned = 0;
        perfectRun = true;
        runRewardsAwarded = false;
        dailyBonusAwarded = false;
        campReached = false;
        runCallout = "";
        routeMilestoneTimer = 0f;
        weatherFrontTimer = 5.5f + selectedStage * 0.65f;
        weatherFrontDuration = 0f;
        weatherFront = WEATHER_CLEAR;
        state = STATE_READY;
        readyTimer = 0f;
        playerX = playerStartX();
        playerY = getGroundY() - playerRadius;
        playerVelocityY = 0f;
        grounded = true;
        jumpsUsed = 0;
        coyoteTimer = RunnerTuning.COYOTE_SECONDS;
        jumpBufferTimer = 0f;
        stageAttempts++;
        logEvent("Start stage: " + stage.name + ". Goal " + stage.goalGates + " hurdles, boss HP " + stage.bossHealth + ".");
    }

    private void setupRunMissions(StageConfig stage) {
        missionStarGoal = 2 + Math.min(3, selectedStage);
        missionComboGoal = 4 + selectedStage * 2;
        missionsCompleted = 0;
        missionHurdlesComplete = false;
        missionStarsComplete = false;
        missionComboComplete = false;
        logEvent("Missions: clear " + stage.goalGates + ", collect " + missionStarGoal + " stars, combo " + missionComboGoal + ".");
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
        shotCooldown = auroraFocusTimer > 0f ? 0.22f : 0.32f;
        playSound("throw");
        logEvent("Snowball fired.");
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        StageConfig stage = STAGES[selectedStage];
        float tension = DifficultyCurve.tension(selectedStage, gatesPassed, stage.goalGates);
        float gateSpeed = dp(RunnerTuning.scrollSpeedDp(stage.baseSpeed, gatesPassed)) * DifficultyCurve.speedMultiplier(tension);
        gateSpeed *= worldSpeedMultiplier();
        float gravity = selectedSeason == SEASON_DARKNESS ? dp(RunnerTuning.DARKNESS_GRAVITY_DP) : dp(RunnerTuning.GRAVITY_DP);
        if (auroraFocusTimer > 0f) {
            gravity *= 0.88f;
        }
        if (weatherFront == WEATHER_SNOW) {
            gravity *= 1.04f;
        } else if (weatherFront == WEATHER_AURORA) {
            gravity *= 0.96f;
        }
        float horizontalSpeed = dp(210);
        boolean wasGrounded = grounded;

        spriteClock += dt * (5.5f + Math.min(4.5f, gatesPassed * 0.28f));
        runnerClock += dt * (grounded ? 1.22f + Math.min(0.34f, gatesPassed * 0.018f) : 0.55f);
        updateVisualEffects(dt);
        updateWeatherFront(dt);
        shotCooldown = Math.max(0f, shotCooldown - dt);
        damageFlash = Math.max(0f, damageFlash - dt);
        updateAuroraRush(dt);
        updateAuroraFocus(dt);
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
        float ceiling = dp(JUMP_CEILING_TOP_MARGIN_DP) + playerRadius;
        if (playerY < ceiling) {
            playerY = ceiling;
            if (playerVelocityY < 0f) {
                playerVelocityY = 0f;
            }
        }

        updateShots(dt);
        updateStars(dt, gateSpeed);
        updatePowerUps(dt, gateSpeed);
        updateBossAttacks(dt);
        updateRouteMilestones();
        updateCampCheckpoint();

        if (!bossDefeated && !bossActive && gatesPassed >= stage.goalGates) {
            startBossPhase();
        }

        if (bossActive) {
            updateBoss(dt);
        } else {
            updateGates(dt, gateSpeed);
            updateHazards(dt, gateSpeed * 1.15f);
        }

        if (bossActive
                && bossStunTimer <= 0f
                && circleHitsCircle(playerX, playerY, playerRadius * 0.76f, bossX, bossHurtCenterY(), bossContactRadius())) {
            endGame(STAGES[selectedStage].bossName + " got you.");
            return;
        }

        for (BossAttack attack : bossAttacks) {
            if (circleHitsCircle(playerX, playerY, playerRadius * 0.80f, attack.x, attack.y, attack.radius)) {
                endGame(attack.label + " hit you.");
                return;
            }
        }

        for (Gate gate : gates) {
            if (hitsGate(gate)) {
                endGame("Antler hurdle bonk.");
                return;
            }
        }

        for (Hazard hazard : hazards) {
            if ("THIN ICE".equals(hazard.label)) {
                tempRect.set(hazard.x - hazard.radius * 1.35f, getGroundY() - dp(18), hazard.x + hazard.radius * 1.35f, getGroundY() + dp(4));
                if (circleHitsRect(playerX, playerY, playerRadius * 0.62f, tempRect)) {
                    endGame("Thin ice cracked.");
                    return;
                }
                continue;
            }
            float hazardHitRadius = hazard.radius * (hazard.roaring ? 0.92f : 0.74f);
            float hazardHitY = hazard.roaring ? hazard.y - hazard.radius * 0.55f : hazard.y;
            if (circleHitsCircle(playerX, playerY, playerRadius * 0.82f, hazard.x, hazardHitY, hazardHitRadius)) {
                endGame(hazard.label + " got you.");
                return;
            }
        }
    }

    private void updateGates(float dt, float gateSpeed) {
        spawnCooldown -= dt;
        if (spawnCooldown <= 0f) {
            spawnGate();
            float tension = DifficultyCurve.tension(selectedStage, gatesPassed, STAGES[selectedStage].goalGates);
            spawnCooldown = DifficultyCurve.gateCooldown(RunnerTuning.nextGateCooldown(STAGES[selectedStage].spawnSeconds, gatesPassed), tension);
        }

        Iterator<Gate> iterator = gates.iterator();
        while (iterator.hasNext()) {
            Gate gate = iterator.next();
            gate.x -= gateSpeed * dt;

            if (!gate.passed && gate.x + gate.width < playerX) {
                gate.passed = true;
                gatesPassed++;
                gameState.gatesPassed = gatesPassed;
                gameState.addCombo();
                int awarded = addScore(10, "Hurdle cleared");
                effects.spawnScorePopup("+" + awarded, gate.x + gate.width / 2f, getGroundY() - gate.height - dp(20), Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(gate.x + gate.width / 2f, getGroundY() - gate.height, 8, Color.rgb(255, 218, 121));
                addAuroraMeter(8f, "Hurdle rhythm");
                showComboCallout();
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
            float tension = DifficultyCurve.tension(selectedStage, gatesPassed, STAGES[selectedStage].goalGates);
            hazardCooldown = DifficultyCurve.hazardCooldown(RunnerTuning.nextHazardCooldown(selectedStage, gatesPassed), tension);
        }

        Iterator<Hazard> iterator = hazards.iterator();
        while (iterator.hasNext()) {
            Hazard hazard = iterator.next();
            hazard.age += dt;
            updateHazardRoar(hazard, dt);
            hazard.x -= speed * hazard.speedMultiplier * dt;
            if (!hazard.roaring) {
                hazard.y = hazard.baseY + hazardBobOffset(hazard);
            }
            maybeAwardNearMiss(hazard);
            if (!hazard.passed && hazard.x + hazard.radius < playerX) {
                hazard.passed = true;
                gameState.addCombo();
                int awarded = addScore(4, hazard.label + " dodged");
                effects.spawnScorePopup("DODGE +" + awarded, hazard.x, hazard.y - hazard.radius, Color.rgb(210, 232, 238));
                addAuroraMeter(6f, "Hazard dodge");
                showComboCallout();
            }
            if (hazard.x + hazard.radius < -dp(36)) {
                iterator.remove();
            }
        }
    }

    private float hazardBobOffset(Hazard hazard) {
        float phase = hazardVisualPhase(hazard);
        if ("BEAR".equals(hazard.label) || "POLAR".equals(hazard.label)) {
            return (float) Math.sin(phase * 1.05f) * dp(0.18f);
        }
        if ("MOOSE".equals(hazard.label)) {
            return (float) Math.sin(phase * 1.20f) * dp(0.28f);
        }
        if ("WOLF".equals(hazard.label)) {
            return (float) Math.sin(phase * 2.9f) * dp(0.62f);
        }
        if ("EAGLE".equals(hazard.label) || "DARK".equals(hazard.label)) {
            return (float) Math.sin(phase * 1.55f) * dp(4.6f);
        }
        if ("SALMON".equals(hazard.label)) {
            return (float) Math.sin(phase * 3.1f) * dp(2.4f);
        }
        if ("AVALANCHE".equals(hazard.label)) {
            return (float) Math.sin(phase * 6.8f) * dp(0.8f);
        }
        return (float) Math.sin(phase * 1.6f) * dp(0.36f);
    }

    private float hazardVisualPhase(Hazard hazard) {
        return hazard.age + hazard.phase;
    }

    private void maybeAwardNearMiss(Hazard hazard) {
        if (hazard.nearMissAwarded || hazard.passed) {
            return;
        }
        float dx = Math.abs(hazard.x - playerX);
        if (dx > playerRadius * 0.95f) {
            return;
        }
        float hazardHitRadius = hazard.radius * (hazard.roaring ? 0.92f : 0.74f);
        float hazardHitY = hazard.roaring ? hazard.y - hazard.radius * 0.55f : hazard.y;
        float dy = hazardHitY - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float hitDistance = playerRadius * 0.82f + hazardHitRadius;
        float nearDistance = hitDistance + dp(20);
        if (distance > hitDistance && distance < nearDistance) {
            hazard.nearMissAwarded = true;
            gameState.addCombo();
            int awarded = addScore(6, "Near miss");
            effects.spawnScorePopup("NEAR +" + awarded, playerX + dp(20), playerY - playerRadius * 1.6f, Color.rgb(132, 213, 232));
            effects.spawnSparkBurst(playerX, playerY, 7, Color.rgb(132, 213, 232));
            addAuroraMeter(16f, "Near miss");
            showComboCallout();
        }
    }

    private void updateHazardRoar(Hazard hazard, float dt) {
        if (!isRoaringBear(hazard.label)) {
            return;
        }
        if (hazard.roaring) {
            hazard.roarTimer -= dt;
            if (hazard.roarTimer <= 0f) {
                hazard.roaring = false;
                hazard.speedMultiplier = hazard.baseSpeedMultiplier;
            }
            return;
        }
        if (hazard.roarUsed) {
            return;
        }
        float triggerX = playerX + dp(170);
        if (hazard.x < triggerX && hazard.x > playerX + dp(70)) {
            hazard.roaring = true;
            hazard.roarUsed = true;
            hazard.roarTimer = 0.72f;
            hazard.speedMultiplier = hazard.baseSpeedMultiplier * 0.58f;
            effects.spawnScorePopup("ROAR", hazard.x, hazard.y - hazard.radius * 2.2f, Color.rgb(255, 246, 207));
            effects.spawnDustBurst(hazard.x, getGroundY(), 10, Color.argb(185, 235, 245, 248));
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

            Hazard hitHazard = hitHazardForShot(shot);
            if (hitHazard != null) {
                iterator.remove();
                stunHazard(hitHazard);
                continue;
            }

            if (bossActive && circleHitsCircle(shot.x, shot.y, shot.radius, bossX, bossY, bossRadius())) {
                iterator.remove();
                int damage = bossWeakWindowActive() ? 2 : 1;
                bossHealth -= damage;
                bossStunTimer = Math.max(bossStunTimer, selectedStage == 4 ? 0.20f : 0.12f);
                damageFlash = 0.16f;
                screenShake = Math.max(screenShake, 0.12f);
                worldFlash = Math.max(worldFlash, 0.10f);
                int awarded = addScore(25, "Boss hit");
                effects.spawnScorePopup((damage > 1 ? "WEAK x2 +" : "HIT +") + awarded, bossX, bossY - bossRadius(), damage > 1 ? Color.rgb(255, 218, 121) : Color.rgb(255, 246, 207));
                effects.spawnSparkBurst(bossX, bossY, damage > 1 ? 22 : 14, damage > 1 ? Color.rgb(255, 218, 121) : Color.rgb(255, 98, 84));
                addAuroraMeter(damage > 1 ? 14f : 8f, "Boss hit");
                playSound("hit");
                logEvent(STAGES[selectedStage].bossName + " hit. HP " + Math.max(0, bossHealth) + "/" + bossMaxHealth + ".");
                if (bossHealth <= 0) {
                    completeStage();
                    return;
                }
            }
        }
    }

    private Hazard hitHazardForShot(Shot shot) {
        for (Hazard hazard : hazards) {
            float hitRadius = hazard.radius * (hazard.roaring ? 0.96f : 0.82f);
            float hitY = hazard.roaring ? hazard.y - hazard.radius * 0.55f : hazard.y;
            if (circleHitsCircle(shot.x, shot.y, shot.radius, hazard.x, hitY, hitRadius)) {
                return hazard;
            }
        }
        return null;
    }

    private void stunHazard(Hazard hazard) {
        hazards.remove(hazard);
        gameState.addCombo();
        int awarded = addScore(12, hazard.label + " stunned");
        effects.spawnScorePopup("STUN +" + awarded, hazard.x, hazard.y - hazard.radius * 1.3f, Color.rgb(132, 213, 232));
        effects.spawnSparkBurst(hazard.x, hazard.y, 12, Color.rgb(230, 248, 255));
        addAuroraMeter(10f, "Wildlife stunned");
        showRunCallout("STUN, THEN MOVE", 0.95f);
        playSound("hit");
        checkMissionProgress();
    }

    private void startBossPhase() {
        gates.clear();
        hazards.clear();
        stars.clear();
        powerUps.clear();
        bossAttacks.clear();
        bossActive = true;
        bossTimer = 0f;
        bossStateTimer = 0f;
        bossPatternTimer = 0f;
        bossStunTimer = 0f;
        bossState = BOSS_STATE_ENTER;
        bossPattern = BOSS_PATTERN_LUNGE;
        bossPatternCount = 0;
        bossHealth = STAGES[selectedStage].bossHealth;
        bossMaxHealth = bossHealth;
        bossX = getWidth() + dp(70);
        bossY = bossLaneCenterY(selectedStage);
        bossVelocityY = 0f;
        bossWarningTimer = 2.2f;
        showRunCallout("FIRE TO DEFEAT. DODGE TELLS.", 2.2f);
        screenShake = Math.max(screenShake, 0.08f);
        logEvent("Boss phase: " + STAGES[selectedStage].bossName + ". Use FIRE.");
    }

    private void updateBoss(float dt) {
        bossTimer += dt;
        bossStateTimer += dt;
        bossPatternTimer += dt;
        bossStunTimer = Math.max(0f, bossStunTimer - dt);
        StageConfig stage = STAGES[selectedStage];
        float phasePressure = bossMaxHealth <= 0 ? 0f : 1f - bossHealth / (float) bossMaxHealth;
        float stateSpeed = 1f + phasePressure * (selectedStage == 4 ? 0.55f : 0.35f);

        if (bossState == BOSS_STATE_ENTER && bossX <= bossRestX(selectedStage) + dp(12)) {
            enterBossTell(BOSS_PATTERN_LUNGE);
        }

        if (bossState == BOSS_STATE_TELL && bossStateTimer >= bossTellDuration() / stateSpeed) {
            beginBossAttack();
        } else if (bossState == BOSS_STATE_ATTACK && bossStateTimer >= bossAttackDuration() / stateSpeed) {
            enterBossRecover();
        } else if (bossState == BOSS_STATE_RECOVER && bossStateTimer >= bossRecoverDuration() / stateSpeed) {
            enterBossTell(nextBossPattern());
        }

        float desiredX = bossDesiredX();
        bossX += (desiredX - bossX) * Math.min(1f, dt * bossTrackingRate());

        float laneY = bossLaneCenterY(selectedStage);
        float desiredY;
        if (selectedStage == 1) {
            desiredY = laneY + (float) Math.sin(bossTimer * 2.4f) * dp(8);
        } else if (selectedStage == 3) {
            desiredY = laneY + (float) Math.sin(bossTimer * 1.8f) * dp(24);
        } else {
            desiredY = laneY + (float) Math.sin(bossTimer * 2.0f) * dp(selectedStage == 4 ? 2.2f : 1.2f);
            if (bossState == BOSS_STATE_TELL) {
                desiredY -= (float) Math.sin(Math.min(1f, bossStateTimer / bossTellDuration()) * Math.PI) * dp(selectedStage == 4 ? 7 : 4);
            }
        }
        bossVelocityY += (desiredY - bossY) * dt * 22f;
        bossVelocityY *= Math.max(0f, 1f - dt * 9f);
        bossY += bossVelocityY * dt;

        if (bossTimer > 32f) {
            endGame(stage.bossName + " outlasted you.");
        }
    }

    private void enterBossTell(int pattern) {
        bossState = BOSS_STATE_TELL;
        bossStateTimer = 0f;
        bossPattern = pattern;
        bossTellX = playerX;
        bossTellY = playerY;
        if (pattern == BOSS_PATTERN_LUNGE) {
            showRunCallout(selectedStage == 4 ? "BEAR CHARGE: BACK UP" : "DODGE THE CHARGE", 0.9f);
        } else if (pattern == BOSS_PATTERN_SNOW_WAVE) {
            showRunCallout(selectedStage == 4 ? "ICE THROW: JUMP" : "PROJECTILES: JUMP", 0.9f);
        } else {
            showRunCallout("WILDLIFE RUSH: STUN OR DODGE", 0.9f);
        }
        effects.spawnScorePopup("!", bossTellX, Math.max(dp(86), bossTellY - playerRadius * 2.2f), Color.rgb(255, 98, 84));
    }

    private void beginBossAttack() {
        bossState = BOSS_STATE_ATTACK;
        bossStateTimer = 0f;
        if (bossPattern == BOSS_PATTERN_SNOW_WAVE) {
            spawnBossSnowWave();
        } else if (bossPattern == BOSS_PATTERN_SUMMON) {
            spawnBossSummons();
        } else {
            screenShake = Math.max(screenShake, selectedStage == 4 ? 0.11f : 0.07f);
            effects.spawnDustBurst(bossX - bossRadius(), getGroundY(), selectedStage == 4 ? 18 : 10, Color.argb(190, 235, 245, 248));
        }
    }

    private void enterBossRecover() {
        bossState = BOSS_STATE_RECOVER;
        bossStateTimer = 0f;
        bossPatternCount++;
    }

    private int nextBossPattern() {
        if (selectedStage == 4 && bossHealth <= bossMaxHealth / 2 && bossPatternCount % 3 == 2) {
            return BOSS_PATTERN_SUMMON;
        }
        return bossPatternCount % 2 == 0 ? BOSS_PATTERN_LUNGE : BOSS_PATTERN_SNOW_WAVE;
    }

    private float bossDesiredX() {
        if (bossState == BOSS_STATE_ENTER) {
            return bossRestX(selectedStage);
        }
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            float pressure = selectedStage == 4 ? dp(106) : dp(128);
            return Math.min(bossRestX(selectedStage), Math.max(playerX + pressure, getWidth() * 0.48f));
        }
        if (bossState == BOSS_STATE_TELL) {
            return bossRestX(selectedStage) + (float) Math.sin(bossStateTimer * 18f) * dp(selectedStage == 4 ? 3f : 2f);
        }
        return bossRestX(selectedStage);
    }

    private float bossTrackingRate() {
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            return selectedStage == 4 ? 8.5f : 6.5f;
        }
        if (bossState == BOSS_STATE_ENTER) {
            return 2.7f;
        }
        return 3.4f;
    }

    private float bossTellDuration() {
        return selectedStage == 4 ? 0.82f : 0.72f;
    }

    private float bossAttackDuration() {
        return bossPattern == BOSS_PATTERN_LUNGE ? 0.62f : 0.42f;
    }

    private float bossRecoverDuration() {
        return selectedStage == 4 ? 0.72f : 0.62f;
    }

    private void spawnBossSnowWave() {
        float baseY = getGroundY() - dp(22);
        int count = selectedStage == 4 && bossHealth <= bossMaxHealth / 2 ? 3 : 2;
        for (int i = 0; i < count; i++) {
            float y = i == 1 ? baseY - dp(54) : baseY;
            if (i == 2) {
                y = baseY - dp(98);
            }
            bossAttacks.add(new BossAttack(bossX - bossRadius() * 0.75f - i * dp(18), y, dp(i == 0 ? 13 : 11), -dp(250 + selectedStage * 24 + i * 34), i == 0 ? -dp(8) : dp(0), ATTACK_ICE, "Ice"));
        }
        effects.spawnDustBurst(bossX - bossRadius(), getGroundY(), 12, Color.argb(185, 235, 245, 248));
        playSound("throw");
    }

    private void spawnBossSummons() {
        String label = selectedStage == 4 ? "WOLF" : STAGES[selectedStage].hazardLabel;
        float radius = gameplayDp(hazardRadiusDp(label));
        float y = hazardSpawnY(label, radius);
        hazards.add(new Hazard(getWidth() + dp(34), y, radius, hazardSpeedMultiplier(label) + 0.10f, random.nextFloat() * 4f, label, null));
        bossAttacks.add(new BossAttack(bossX - bossRadius(), getGroundY() - dp(14), dp(18), -dp(210), 0f, ATTACK_SHOCKWAVE, "Shockwave"));
        screenShake = Math.max(screenShake, 0.12f);
        effects.spawnScorePopup("ROAR", bossX, bossY - bossRadius(), Color.rgb(255, 246, 207));
    }

    private void updateBossAttacks(float dt) {
        Iterator<BossAttack> iterator = bossAttacks.iterator();
        while (iterator.hasNext()) {
            BossAttack attack = iterator.next();
            attack.age += dt;
            float speedScale = worldSpeedMultiplier();
            attack.x += attack.vx * dt * speedScale;
            attack.y += attack.vy * dt * speedScale;
            if (attack.type == ATTACK_ICE) {
                attack.vy += dp(42) * dt;
                attack.spin += dt * 8f;
                if (random.nextFloat() < 0.42f) {
                    effects.spawnParticle(attack.x + attack.radius * 0.8f, attack.y, dp(35), -dp(10 + random.nextFloat() * 24), attack.radius * 0.30f, Color.argb(140, 230, 248, 255), 0.24f);
                }
            } else {
                attack.radius += dp(13) * dt;
            }
            if (attack.x + attack.radius < -dp(40) || attack.y > getHeight() + dp(40) || attack.age > 4f) {
                iterator.remove();
            }
        }
    }

    private void completeStage() {
        StageConfig stage = STAGES[selectedStage];
        bossActive = false;
        bossDefeated = true;
        int clearBonus = stageClearBonus(selectedStage, gameState.bestCombo, gameState.stars);
        int awarded = addScore(clearBonus, "Stage clear bonus");
        effects.spawnScorePopup("CLEAR +" + awarded, getWidth() / 2f, getHeight() * 0.38f, Color.rgb(255, 218, 121));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.38f, 26, Color.rgb(255, 218, 121));
        showRunCallout("STAGE CLEAR", 2.0f);
        worldFlash = Math.max(worldFlash, 0.24f);
        playSound("medal");
        if (score > bestScore) {
            bestScore = score;
        }
        if (selectedStage < STAGES.length - 1 && unlockedStage < selectedStage + 1) {
            unlockedStage = selectedStage + 1;
        }
        awardExpeditionBonusIfEarned();
        awardRunTokens(true);
        prefs.edit()
                .putInt(PREF_BEST_SCORE, bestScore)
                .putInt(PREF_UNLOCKED_STAGE, unlockedStage)
                .putInt(PREF_XP, gameState.xp)
                .putInt(PREF_TRAIL_TOKENS, trailTokens)
                .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                .putInt(PREF_DAILY_STREAK, dailyStreak)
                .apply();
        state = STATE_STAGE_CLEAR;
        stageClearTimer = 0f;
        logEvent("Stage cleared: " + stage.name + ". Score " + score + ".");
    }

    private void awardExpeditionBonusIfEarned() {
        if (runExpeditionBonusTokens > 0) {
            return;
        }
        int gradeScore = expeditionGradeScore(true);
        if (gradeScore < 5) {
            return;
        }
        runExpeditionBonusTokens = 8 + selectedStage * 2 + (gradeScore >= 7 ? 6 : 0);
        int awarded = addScore(45 + selectedStage * 8, "Expedition bonus");
        effects.spawnScorePopup("EXPEDITION +" + runExpeditionBonusTokens + "T", getWidth() / 2f, getHeight() * 0.40f, Color.rgb(77, 219, 184));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.40f, 24, Color.rgb(77, 219, 184));
        showRunCallout("EXPEDITION GRADE " + expeditionGrade(true), 1.55f);
        logEvent("Expedition bonus +" + runExpeditionBonusTokens + " tokens, score +" + awarded + ".");
    }

    private void endGame(String reason) {
        gameState.breakCombo();
        perfectRun = false;
        if (gameState.shieldActive) {
            gameState.shieldActive = false;
            resetAfterHit();
            playSound("hurt");
            logEvent("Shield absorbed hit: " + reason);
            return;
        }
        if (gameState.lives > 1) {
            gameState.lives--;
            livesLostThisRun++;
            resetAfterHit();
            playSound("hurt");
            logEvent("Life lost: " + reason + " Lives " + gameState.lives + ".");
            return;
        }
        livesLostThisRun++;
        awardRunTokens(false);
        state = STATE_GAME_OVER;
        screenShake = Math.max(screenShake, 0.18f);
        worldFlash = Math.max(worldFlash, 0.18f);
        if (score > bestScore) {
            bestScore = score;
            prefs.edit()
                    .putInt(PREF_BEST_SCORE, bestScore)
                    .putInt(PREF_XP, gameState.xp)
                    .putInt(PREF_TRAIL_TOKENS, trailTokens)
                    .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                    .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                    .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                    .putInt(PREF_DAILY_STREAK, dailyStreak)
                    .apply();
        } else {
            prefs.edit()
                    .putInt(PREF_XP, gameState.xp)
                    .putInt(PREF_TRAIL_TOKENS, trailTokens)
                    .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                    .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                    .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                    .putInt(PREF_DAILY_STREAK, dailyStreak)
                    .apply();
        }
        logEvent("Game over: " + reason + " Score " + score + ".");
    }

    private void awardRunTokens(boolean stageCleared) {
        if (runRewardsAwarded) {
            return;
        }
        runRewardsAwarded = true;
        int dailyReward = awardDailyRushIfComplete(stageCleared);
        int badgeReward = awardTrailBadges(stageCleared);
        runTokensEarned = RunRewardEconomy.tokensForRun(missionsCompleted, gameState.bestCombo, gameState.stars, stageCleared, perfectRun) + dailyReward + badgeReward + runCacheTokens + runExpeditionBonusTokens;
        trailTokens += runTokensEarned;
        if (runTokensEarned > 0) {
            effects.spawnScorePopup("TOKENS +" + runTokensEarned, getWidth() / 2f, getHeight() * 0.46f, Color.rgb(255, 218, 121));
            logEvent("Trail Tokens +" + runTokensEarned + ". Bank " + trailTokens + ".");
        }
    }

    private int awardDailyRushIfComplete(boolean stageCleared) {
        int today = currentDailyDayKey();
        if (dailyBonusAwarded || dailyCompletedDay == today || selectedStage != dailyStageIndex()) {
            return 0;
        }
        if (!stageCleared && gatesPassed < dailyGateGoal()) {
            return 0;
        }
        int reward = RunRewardEconomy.dailyReward(dailyStreak);
        dailyStreak = RunRewardEconomy.nextDailyStreak(dailyCompletedDay, today, dailyStreak);
        dailyCompletedDay = today;
        dailyBonusAwarded = true;
        dailyTokensEarned = reward;
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.42f, 22, Color.rgb(77, 219, 184));
        showRunCallout("DAILY RUSH COMPLETE", 1.8f);
        logEvent("Daily Rush complete. Streak " + dailyStreak + ", reward " + reward + ".");
        return reward;
    }

    private int awardTrailBadges(boolean stageCleared) {
        runBadgesEarnedMask = TrailBadgeCatalog.newlyEarnedMask(
                trailBadgeMask,
                score,
                selectedStage,
                unlockedStage,
                gatesPassed,
                gameState.stars,
                gameState.bestCombo,
                missionsCompleted,
                stageCleared,
                perfectRun,
                dailyRushCompleteToday(),
                dailyStreak,
                auroraRushes);
        int earnedCount = TrailBadgeCatalog.badgeCount(runBadgesEarnedMask);
        if (earnedCount <= 0) {
            runBadgeTokensEarned = 0;
            return 0;
        }
        trailBadgeMask |= runBadgesEarnedMask;
        runBadgeTokensEarned = TrailBadgeCatalog.tokensForNewBadges(earnedCount);
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.30f, 20 + earnedCount * 3, Color.rgb(255, 246, 207));
        effects.spawnScorePopup("BADGE +" + runBadgeTokensEarned, getWidth() / 2f, getHeight() * 0.30f, Color.rgb(255, 246, 207));
        showRunCallout(earnedCount == 1 ? "PASSPORT BADGE" : "PASSPORT BADGES", 1.7f);
        playSound("medal");
        logEvent("Trail Passport earned " + earnedCount + " badge(s): " + firstRunBadgeName() + ".");
        return runBadgeTokensEarned;
    }

    private void resetAfterHit() {
        gates.clear();
        hazards.clear();
        stars.clear();
        powerUps.clear();
        shots.clear();
        bossAttacks.clear();
        effects.clearParticles();
        playerX = playerStartX();
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

    private int addScore(int amount, String reason) {
        int multiplier = activeScoreMultiplier();
        int awarded = amount * multiplier;
        score += awarded;
        runStageScore += awarded;
        gameState.addScore(awarded);
        if (awarded >= 10) {
            String suffix = multiplier > 1 ? " x" + multiplier : "";
            logEvent(reason + " +" + awarded + suffix + ".");
        }
        return awarded;
    }

    private int activeScoreMultiplier() {
        int multiplier = scoreMultiplierForCombo(gameState.combo);
        if (auroraRushTimer > 0f) {
            multiplier = Math.min(5, multiplier + 1);
        }
        return multiplier;
    }

    private void showComboCallout() {
        int multiplier = activeScoreMultiplier();
        if (gameState.combo >= 3 || multiplier > 1) {
            showRunCallout((auroraRushTimer > 0f ? "AURORA " : "") + "COMBO " + gameState.combo + "  SCORE x" + multiplier, 1.15f);
        }
        checkMissionProgress();
    }

    private void showRunCallout(String label, float seconds) {
        runCallout = label;
        runCalloutTimer = Math.max(runCalloutTimer, seconds);
    }

    private void updateAuroraRush(float dt) {
        if (auroraRushTimer <= 0f) {
            return;
        }
        auroraRushTimer = Math.max(0f, auroraRushTimer - dt);
        if (auroraRushTimer <= 0f) {
            auroraMeter = 0f;
            showRunCallout("AURORA BANKED", 0.95f);
        }
    }

    private void updateAuroraFocus(float dt) {
        if (auroraFocusTimer <= 0f) {
            return;
        }
        auroraFocusTimer = Math.max(0f, auroraFocusTimer - dt);
        if (auroraFocusTimer <= 0f) {
            showRunCallout("FOCUS FADED", 0.80f);
        }
    }

    private float worldSpeedMultiplier() {
        float multiplier = auroraFocusTimer > 0f ? 0.78f : 1f;
        if (weatherFront == WEATHER_RAIN) {
            multiplier *= 1.04f;
        } else if (weatherFront == WEATHER_SNOW) {
            multiplier *= 0.94f;
        } else if (weatherFront == WEATHER_AURORA) {
            multiplier *= 0.97f;
        }
        return multiplier;
    }

    private void activateAuroraFocus(float x, float y) {
        auroraFocusTimer = AURORA_FOCUS_SECONDS;
        shotCooldown = Math.min(shotCooldown, 0.08f);
        effects.spawnSparkBurst(x, y, 20, Color.rgb(77, 219, 184));
        effects.spawnScorePopup("FOCUS", x, y - dp(16), Color.rgb(77, 219, 184));
        showRunCallout("AURORA FOCUS", 1.25f);
        playSound("medal");
        logEvent("Aurora Focus collected.");
    }

    private void updateWeatherFront(float dt) {
        if (state != STATE_RUNNING || bossActive) {
            return;
        }
        if (weatherFrontDuration > 0f) {
            weatherFrontDuration = Math.max(0f, weatherFrontDuration - dt);
            if (weatherFrontDuration <= 0f) {
                weatherFront = WEATHER_CLEAR;
                weatherFrontTimer = 6.5f + random.nextFloat() * 5.0f + selectedStage * 0.35f;
                showRunCallout("WEATHER CLEARED", 0.75f);
            }
            return;
        }
        if (gatesPassed < Math.max(2, STAGES[selectedStage].goalGates / 3)) {
            return;
        }
        weatherFrontTimer -= dt;
        if (weatherFrontTimer <= 0f) {
            startWeatherFront();
        }
    }

    private void startWeatherFront() {
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage == 4;
        boolean dark = selectedSeason == SEASON_DARKNESS || STAGES[selectedStage].season == SEASON_DARKNESS;
        if (winter) {
            weatherFront = WEATHER_SNOW;
            showRunCallout("SNOW FRONT", 1.10f);
        } else if (dark || random.nextFloat() < 0.38f) {
            weatherFront = WEATHER_AURORA;
            showRunCallout("AURORA FRONT", 1.10f);
        } else {
            weatherFront = WEATHER_RAIN;
            showRunCallout("COASTAL RAIN", 1.10f);
        }
        weatherFrontDuration = 4.2f + selectedStage * 0.35f;
        runWeatherFronts++;
        addAuroraMeter(weatherFront == WEATHER_AURORA ? 18f : 8f, "Weather front");
        effects.spawnSparkBurst(getWidth() * 0.50f, getHeight() * 0.24f, 14, weatherFront == WEATHER_AURORA ? Color.rgb(77, 219, 184) : Color.rgb(210, 232, 238));
        logEvent("Weather front: " + weatherFrontLabel() + ".");
    }

    private void addAuroraMeter(float amount, String reason) {
        if (state != STATE_RUNNING || auroraRushTimer > 0f) {
            return;
        }
        auroraMeter = Math.min(AURORA_METER_MAX, auroraMeter + amount);
        if (auroraMeter >= AURORA_METER_MAX) {
            activateAuroraRush(reason);
        }
    }

    private void activateAuroraRush(String reason) {
        auroraMeter = AURORA_METER_MAX;
        auroraRushTimer = AURORA_RUSH_SECONDS;
        auroraRushes++;
        worldFlash = Math.max(worldFlash, 0.18f);
        screenShake = Math.max(screenShake, 0.08f);
        effects.spawnSparkBurst(playerX, playerY - playerRadius * 1.2f, 24, Color.rgb(132, 213, 232));
        showRunCallout("AURORA RUSH", 1.8f);
        playSound("medal");
        logEvent("Aurora Rush triggered by " + reason + ".");
    }

    private void checkMissionProgress() {
        StageConfig stage = STAGES[selectedStage];
        if (!missionHurdlesComplete && gatesPassed >= stage.goalGates) {
            missionHurdlesComplete = true;
            completeMission("HURDLE HERO");
        }
        if (!missionStarsComplete && gameState.stars >= missionStarGoal) {
            missionStarsComplete = true;
            completeMission("STAR TRAIL");
        }
        if (!missionComboComplete && gameState.bestCombo >= missionComboGoal) {
            missionComboComplete = true;
            completeMission("COMBO KID");
        }
    }

    private void updateRouteMilestones() {
        StageConfig stage = STAGES[selectedStage];
        int nextGate = routeGateForIndex(routeMilestoneIndex, stage.goalGates);
        if (nextGate <= 0 || gatesPassed < nextGate || bossActive) {
            return;
        }
        String label = routeMilestoneLabel(selectedStage, routeMilestoneIndex);
        routeMilestoneIndex++;
        routeMilestoneTimer = 2.4f;
        gameState.addCombo();
        int awarded = addScore(14 + selectedStage * 3, "Route milestone");
        effects.spawnScorePopup(label + " +" + awarded, getWidth() / 2f, getHeight() * 0.27f, Color.rgb(255, 218, 121));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.28f, 16, Color.rgb(255, 218, 121));
        addAuroraMeter(10f, "Route milestone");
        showRunCallout(label, 1.45f);
        logEvent("Route milestone reached: " + label + ".");
    }

    private int routeGateForIndex(int index, int goalGates) {
        if (index == 0) return Math.max(1, Math.round(goalGates * 0.25f));
        if (index == 1) return Math.max(2, Math.round(goalGates * 0.50f));
        if (index == 2) return Math.max(3, Math.round(goalGates * 0.75f));
        return -1;
    }

    private String routeMilestoneLabel(int stage, int index) {
        String[][] labels = {
                {"SUN RIDGE", "TUNDRA CAMP", "GOLDEN PASS"},
                {"RIVER FORK", "FISH CAMP", "SPAWNING FALLS"},
                {"BIRCH CUT", "MOOSE MEADOW", "ANTLER PASS"},
                {"LOW LIGHT", "AURORA CAMP", "OWL RIDGE"},
                {"ICE FLATS", "DEN CAMP", "WHITEOUT PASS"}
        };
        return labels[clampInt(stage, 0, labels.length - 1)][clampInt(index, 0, 2)];
    }

    private void updateCampCheckpoint() {
        StageConfig stage = STAGES[selectedStage];
        int campGate = Math.max(2, Math.round(stage.goalGates * 0.50f));
        if (campReached || bossActive || gatesPassed < campGate) {
            return;
        }
        campReached = true;
        if (gameState.lives < 3) {
            gameState.lives++;
            effects.spawnScorePopup("+LIFE", playerX, playerY - playerRadius * 2.2f, Color.rgb(255, 98, 84));
        } else if (!gameState.shieldActive) {
            gameState.shieldActive = true;
            effects.spawnScorePopup("CAMP SHIELD", playerX, playerY - playerRadius * 2.2f, Color.rgb(132, 213, 232));
        } else {
            int awarded = addScore(20, "Camp supplies");
            effects.spawnScorePopup("SUPPLIES +" + awarded, playerX, playerY - playerRadius * 2.2f, Color.rgb(255, 218, 121));
        }
        effects.spawnSparkBurst(playerX, getGroundY() - dp(24), 22, Color.rgb(255, 177, 70));
        showRunCallout("TRAIL CAMP RESTOCK", 1.55f);
        playSound("medal");
        logEvent("Trail camp reached.");
    }

    private void completeMission(String label) {
        missionsCompleted++;
        int awarded = addScore(35 + selectedStage * 5, "Mission " + label);
        effects.spawnScorePopup(label + " +" + awarded, getWidth() / 2f, getHeight() * 0.34f, Color.rgb(132, 213, 232));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.34f, 18, Color.rgb(132, 213, 232));
        totalMissionsCompleted++;
        prefs.edit().putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted).apply();
        addAuroraMeter(22f, "Mission complete");
        showRunCallout("MISSION COMPLETE", 1.45f);
        playSound("medal");
        logEvent("Mission complete: " + label + ".");
    }

    static int scoreMultiplierForCombo(int combo) {
        return ArcadeScoring.scoreMultiplierForCombo(combo);
    }

    static int stageClearBonus(int selectedStage, int bestCombo, int stars) {
        return ArcadeScoring.stageClearBonus(selectedStage, bestCombo, stars);
    }

    private void spawnGate() {
        float gateWidth = gameplayDp(34) + random.nextFloat() * gameplayDp(18);
        float hurdleHeight = RunnerTuning.gateHeight(getResources().getDisplayMetrics().density, selectedStage, gatesPassed, random.nextFloat());
        gates.add(new Gate(getWidth() + gateWidth, hurdleHeight, gateWidth));
        if (random.nextFloat() < 0.72f) {
            float starY = getGroundY() - hurdleHeight - gameplayDp(34 + random.nextFloat() * 18);
            stars.add(new Star(getWidth() + gateWidth + gameplayDp(46), Math.max(dp(86), starY), gameplayDp(8)));
        }
        if (!gameState.shieldActive && gatesPassed >= 1 && random.nextFloat() < powerUpSpawnChance()) {
            float shieldY = getGroundY() - hurdleHeight - gameplayDp(58 + random.nextFloat() * 16);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(86), Math.max(dp(92), shieldY), gameplayDp(10), "SHIELD"));
        }
        if (gatesPassed >= 2 && random.nextFloat() < cacheSpawnChance()) {
            float cacheY = getGroundY() - hurdleHeight - gameplayDp(24 + random.nextFloat() * 26);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(126), Math.max(dp(88), cacheY), gameplayDp(9.5f), "CACHE"));
        }
        if (gatesPassed >= 3 && auroraFocusTimer <= 0f && random.nextFloat() < focusSpawnChance()) {
            float focusY = getGroundY() - hurdleHeight - gameplayDp(72 + random.nextFloat() * 18);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(166), Math.max(dp(86), focusY), gameplayDp(10.5f), "FOCUS"));
        }
    }

    private float powerUpSpawnChance() {
        if (selectedStage == 0) return 0.18f;
        if (selectedStage >= 4) return 0.25f;
        return 0.22f;
    }

    private float cacheSpawnChance() {
        if (selectedStage >= 4) return 0.24f;
        if (selectedStage >= 2) return 0.18f;
        return 0.12f;
    }

    private float focusSpawnChance() {
        if (selectedStage >= 4) return 0.16f;
        if (selectedStage >= 2) return 0.13f;
        return 0.10f;
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
                gameState.addCombo();
                int awarded = addScore(15, "Star collected");
                effects.spawnScorePopup("STAR +" + awarded, star.x, star.y - dp(12), Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(star.x, star.y, 12, Color.rgb(255, 218, 121));
                addAuroraMeter(18f, "Star collected");
                showComboCallout();
                playSound("medal");
                continue;
            }
            if (star.x + star.radius < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void updatePowerUps(float dt, float speed) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.x -= speed * dt;
            powerUp.spin += dt * 5.5f;
            powerUp.bob += dt * 4.5f;
            if (circleHitsCircle(playerX, playerY, playerRadius * 1.05f, powerUp.x, powerUp.y, powerUp.radius * 1.55f)) {
                iterator.remove();
                activatePowerUp(powerUp);
                continue;
            }
            if (powerUp.x + powerUp.radius < -dp(28)) {
                iterator.remove();
            }
        }
    }

    private void activatePowerUp(PowerUp powerUp) {
        if ("SHIELD".equals(powerUp.type)) {
            gameState.shieldActive = true;
            gameState.addCombo();
            int awarded = addScore(20, "Shield pickup");
            effects.spawnScorePopup("SHIELD +" + awarded, powerUp.x, powerUp.y - dp(12), Color.rgb(132, 213, 232));
            effects.spawnSparkBurst(powerUp.x, powerUp.y, 16, Color.rgb(132, 213, 232));
            addAuroraMeter(12f, "Shield pickup");
            showRunCallout("AURORA SHIELD", 1.35f);
            playSound("medal");
            checkMissionProgress();
        } else if ("CACHE".equals(powerUp.type)) {
            gameState.addCombo();
            int tokenBonus = selectedStage >= 4 ? 2 : 1;
            runCacheTokens += tokenBonus;
            runTokensEarned += tokenBonus;
            int awarded = addScore(18 + selectedStage * 2, "Trail cache");
            effects.spawnScorePopup("CACHE +" + tokenBonus + "T", powerUp.x, powerUp.y - dp(12), Color.rgb(255, 218, 121));
            effects.spawnSparkBurst(powerUp.x, powerUp.y, 16, Color.rgb(255, 218, 121));
            addAuroraMeter(14f, "Trail cache");
            showRunCallout("TRAIL CACHE +" + awarded, 1.10f);
            playSound("medal");
            checkMissionProgress();
        } else if ("FOCUS".equals(powerUp.type)) {
            gameState.addCombo();
            runFocusPickups++;
            int awarded = addScore(16, "Aurora Focus");
            effects.spawnScorePopup("FOCUS +" + awarded, powerUp.x, powerUp.y - dp(12), Color.rgb(77, 219, 184));
            addAuroraMeter(18f, "Aurora Focus");
            activateAuroraFocus(powerUp.x, powerUp.y);
            checkMissionProgress();
        }
    }

    private void updateVisualEffects(float dt) {
        screenShake = Math.max(0f, screenShake - dt);
        worldFlash = Math.max(0f, worldFlash - dt);
        runCalloutTimer = Math.max(0f, runCalloutTimer - dt);
        bossWarningTimer = Math.max(0f, bossWarningTimer - dt);
        routeMilestoneTimer = Math.max(0f, routeMilestoneTimer - dt);
        effects.update(dt);
    }

    private void spawnHazard() {
        String label = selectHazardLabel();
        float radius = gameplayDp(hazardRadiusDp(label));
        float y = hazardSpawnY(label, radius);
        float speed = hazardSpeedMultiplier(label);
        hazards.add(new Hazard(getWidth() + dp(50), y, radius, speed, random.nextFloat() * 4f, label, null));
    }

    private String selectHazardLabel() {
        StageConfig stage = STAGES[selectedStage];
        float roll = random.nextFloat();
        if (selectedStage == 3) {
            if (gatesPassed >= 4 && roll < 0.34f) {
                return "WOLF";
            }
            return "EAGLE";
        }
        if (selectedStage == 4) {
            if (gatesPassed >= 4 && roll < 0.16f) {
                return "THIN ICE";
            }
            if (gatesPassed >= 7 && roll < 0.20f) {
                return "AVALANCHE";
            }
            if (gatesPassed >= 6 && roll < 0.34f) {
                return "POLAR";
            }
            if (gatesPassed >= 3 && roll < 0.68f) {
                return "WOLF";
            }
            return roll < 0.84f ? "BEAR" : "POLAR";
        }
        if (selectedStage == 2 && gatesPassed >= 5 && roll < 0.25f) {
            return "SALMON";
        }
        return stage.hazardLabel;
    }

    private float hazardRadiusDp(String label) {
        if ("POLAR".equals(label)) return 24f;
        if ("BEAR".equals(label)) return 23f;
        if ("MOOSE".equals(label)) return 22f;
        if ("WOLF".equals(label)) return 18f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 18f;
        if ("SALMON".equals(label)) return 17f;
        if ("AVALANCHE".equals(label)) return 22f;
        if ("THIN ICE".equals(label)) return 20f;
        return 15 + Math.min(8, selectedStage * 2);
    }

    private float hazardSpeedMultiplier(String label) {
        float variance = random.nextFloat() * 0.10f;
        if ("POLAR".equals(label)) return 0.84f + variance;
        if ("BEAR".equals(label)) return 0.90f + variance;
        if ("MOOSE".equals(label)) return 0.88f + variance;
        if ("WOLF".equals(label)) return 1.08f + variance;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 1.02f + variance;
        if ("SALMON".equals(label)) return 0.96f + variance;
        if ("AVALANCHE".equals(label)) return 1.18f + variance;
        if ("THIN ICE".equals(label)) return 0.98f + variance;
        return 0.86f + random.nextFloat() * 0.32f;
    }

    private float hazardSpawnY(String label, float radius) {
        float lowBand = getGroundY() - radius - dp(6);
        if ("EAGLE".equals(label) || "DARK".equals(label)) {
            return getGroundY() - dp(132) - random.nextFloat() * dp(72);
        }
        if ("SALMON".equals(label)) {
            return getGroundY() - dp(88) - random.nextFloat() * dp(64);
        }
        if ("AVALANCHE".equals(label)) {
            return getGroundY() - radius - dp(2);
        }
        if ("THIN ICE".equals(label)) {
            return getGroundY() - radius * 0.54f;
        }
        if ("SUN".equals(label) && random.nextFloat() < 0.30f) {
            return getGroundY() - dp(110) - random.nextFloat() * dp(48);
        }
        return lowBand;
    }

    private boolean isRoaringBear(String label) {
        return "BEAR".equals(label) || "POLAR".equals(label);
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
        textPaint.setTextSize(dp(11));
        canvas.drawText("TRIPPERDEE LABS · " + BuildConfig.BUILD_BADGE, getWidth() / 2f, getHeight() * 0.34f, textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(42));
        canvas.drawText("YOU  RUSH", getWidth() / 2f, getHeight() * 0.47f, textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Alaska platform runner", getWidth() / 2f, getHeight() * 0.59f, textPaint);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("For the best playing experience, rotate your phone.", getWidth() / 2f, getHeight() * 0.71f, textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap to continue", getWidth() / 2f, getHeight() * 0.83f, textPaint);
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
        setButton(dailyButtonBounds, x, y - dp(58), dp(230), dp(42));
        setButton(debugButtonBounds, x - dp(58), y + dp(160), dp(104), dp(36));
        setButton(muteButtonBounds, x + dp(58), y + dp(160), dp(104), dp(36));

        drawDailyRushButton(canvas, dailyButtonBounds);
        drawButton(canvas, primaryButtonBounds, "PLAY " + STAGES[selectedStage].name);
        drawButton(canvas, secondaryButtonBounds, playerPhoto == null ? "CREATE YOUR SPRITE" : "EDIT YOUR SPRITE");
        drawButton(canvas, thirdButtonBounds, "ALASKA MAP");
        drawSmallButton(canvas, debugButtonBounds, "DEBUG: " + (debugOverlay ? "ON" : "OFF"));
        drawSmallButton(canvas, muteButtonBounds, gameState.muted ? "MUTED" : "AUDIO");

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Best " + bestScore + "   Tokens " + trailTokens + "   Missions " + totalMissionsCompleted, getWidth() / 2f, getHeight() - dp(45), textPaint);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Stages " + (unlockedStage + 1) + "/" + STAGES.length
                + "   Outfits " + unlockedOutfitCount() + "/" + OUTFIT_COLORS.length
                + "   Badges " + TrailBadgeCatalog.badgeCount(trailBadgeMask) + "/" + TrailBadgeCatalog.BADGE_COUNT,
                getWidth() / 2f, getHeight() - dp(25), textPaint);
    }

    private void drawDailyRushButton(Canvas canvas, RectF bounds) {
        boolean complete = dailyRushCompleteToday();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(complete ? Color.argb(218, 18, 52, 48) : Color.argb(230, 77, 219, 184));
        canvas.drawRoundRect(bounds, dp(13), dp(13), paint);
        paint.setColor(Color.argb(50, 255, 255, 255));
        canvas.drawRoundRect(bounds.left + dp(3), bounds.top + dp(3), bounds.right - dp(3), bounds.top + bounds.height() * 0.46f, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(Color.argb(220, 255, 246, 207));
        canvas.drawRoundRect(bounds, dp(13), dp(13), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9));
        textPaint.setColor(complete ? Color.rgb(210, 232, 238) : Color.rgb(8, 18, 30));
        canvas.drawText("DAILY RUSH", bounds.centerX(), bounds.top + dp(15), textPaint);
        textPaint.setTextSize(dp(10.5f));
        textPaint.setColor(complete ? Color.WHITE : Color.rgb(8, 18, 30));
        canvas.drawText(dailyRushLine(), bounds.centerX(), bounds.top + dp(31), textPaint);
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
        drawButton(canvas, outfitButtonBounds, outfitButtonLabel());
        drawOutfitSwatch(canvas, outfitButtonBounds);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(playerPhoto == null ? Color.rgb(255, 218, 121) : Color.WHITE);
        canvas.drawText(playerPhoto == null ? "Default runner is active." : "Photo sprite ready for this run.", x, y + buttonGap * 3f + dp(42), textPaint);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Tokens " + trailTokens + "   Outfit " + outfitStatusLabel(), x, y + buttonGap * 3f + dp(62), textPaint);
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
        drawWeatherFront(canvas);

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }
        for (Hazard hazard : hazards) {
            drawHazard(canvas, hazard);
        }
        for (Star star : stars) {
            drawStar(canvas, star);
        }
        for (PowerUp powerUp : powerUps) {
            drawPowerUp(canvas, powerUp);
        }
        for (Shot shot : shots) {
            drawShot(canvas, shot);
        }
        for (BossAttack attack : bossAttacks) {
            drawBossAttack(canvas, attack);
        }
        if (bossActive) {
            drawBoss(canvas);
        }

        drawPlayerLaneGuide(canvas);
        drawAuroraRushTrail(canvas);
        drawAuroraFocusTrail(canvas);
        effects.drawParticles(canvas);
        drawShieldAura(canvas);
        drawCharacter(canvas, playerX, playerY - playerRadius * PLAYER_HEAD_DRAW_OFFSET, playerRadius);
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

        Drawable treeAsset = assets.tree(winter);
        drawParallaxTreeLayer(canvas, treeAsset, winter, 0.18f, 118, winter ? 74 : 70, 20, -30, 130);
        drawParallaxTreeLayer(canvas, treeAsset, winter, 0.30f, 94, winter ? 106 : 94, 10, 18, winter ? 220 : 190);
        if (winter || dark) {
            drawParallaxTreeLayer(canvas, treeAsset, true, 0.42f, 72, 124, 8, 46, 235);
            drawSnowDriftMist(canvas);
        } else {
            drawParallaxTreeLayer(canvas, treeAsset, false, 0.39f, 86, 112, 8, 52, 210);
        }
    }

    private void drawParallaxTreeLayer(Canvas canvas, Drawable treeAsset, boolean winter, float speedFactor, float spacingDp, float heightDp, float groundOffsetDp, float staggerDp, int alpha) {
        float spacing = dp(spacingDp);
        float scroll = sceneryScroll * speedFactor;
        float offset = scroll % spacing;
        int firstIndex = (int) Math.floor(scroll / spacing);
        int count = (int) Math.ceil(getWidth() / spacing) + 5;
        float baseGround = getGroundY() + dp(groundOffsetDp);

        for (int i = -2; i < count; i++) {
            int treeIndex = firstIndex + i;
            float x = i * spacing - offset + dp(staggerDp);
            float heightVariance = (Math.floorMod(treeIndex, 3) - 1) * dp(winter ? 9 : 7);
            float treeHeight = dp(heightDp) + heightVariance;
            float treeWidth = treeHeight * (winter ? 0.70f : 0.64f);
            float yOffset = Math.floorMod(treeIndex, 2) == 0 ? dp(5) : 0f;
            float rootSink = dp(winter ? 14 : 10);
            float treeBottom = baseGround + yOffset + rootSink;

            if (treeAsset != null) {
                drawDrawableAlpha(
                        canvas,
                        treeAsset,
                        x - treeWidth * 0.50f,
                        treeBottom - treeHeight,
                        x + treeWidth * 0.50f,
                        treeBottom,
                        alpha
                );
            } else {
                drawFallbackTree(canvas, x, treeBottom, treeHeight, treeWidth, winter, alpha);
            }
        }
    }

    private void drawFallbackTree(Canvas canvas, float x, float ground, float treeHeight, float treeWidth, boolean winter, int alpha) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(winter
                ? Color.argb(alpha, 226, 240, 245)
                : Color.argb(alpha, 45, 108, 70));
        float trunkWidth = Math.max(dp(7), treeWidth * 0.18f);
        canvas.drawRoundRect(x - trunkWidth / 2f, ground - treeHeight * 0.36f, x + trunkWidth / 2f, ground, dp(4), dp(4), paint);
        PathCompat.triangle(canvas, paint, x - treeWidth * 0.48f, ground - treeHeight * 0.22f, x, ground - treeHeight, x + treeWidth * 0.48f, ground - treeHeight * 0.22f);
        paint.setColor(winter
                ? Color.argb(alpha, 171, 199, 210)
                : Color.argb(alpha, 33, 83, 56));
        PathCompat.triangle(canvas, paint, x - treeWidth * 0.34f, ground - treeHeight * 0.32f, x, ground - treeHeight * 0.85f, x + treeWidth * 0.34f, ground - treeHeight * 0.32f);
    }

    private void drawSnowDriftMist(Canvas canvas) {
        float ground = getGroundY();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(42, 248, 252, 253));
        for (float x = -(sceneryScroll * 0.20f) % dp(210) - dp(80); x < getWidth() + dp(240); x += dp(210)) {
            canvas.drawOval(x, ground - dp(21), x + dp(150), ground + dp(8), paint);
            canvas.drawOval(x + dp(72), ground - dp(35), x + dp(236), ground - dp(2), paint);
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

    private void drawWeatherFront(Canvas canvas) {
        if (weatherFront == WEATHER_CLEAR || weatherFrontDuration <= 0f) {
            return;
        }
        float pct = Math.min(1f, weatherFrontDuration / Math.max(0.01f, 4.2f + selectedStage * 0.35f));
        paint.setStyle(Paint.Style.FILL);
        if (weatherFront == WEATHER_AURORA) {
            paint.setColor(Color.argb(Math.round(42 + 52 * pct), 77, 219, 184));
            for (float x = -(sceneryScroll * 0.34f) % dp(260) - dp(80); x < getWidth() + dp(300); x += dp(260)) {
                PathCompat.ribbon(canvas, paint, x, dp(98), x + dp(62), dp(74), x + dp(132), dp(116), x + dp(222), dp(82), dp(8));
            }
        } else {
            int color = weatherFront == WEATHER_SNOW ? Color.argb(150, 248, 252, 253) : Color.argb(120, 132, 213, 232);
            paint.setColor(color);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(weatherFront == WEATHER_SNOW ? dp(2.2f) : dp(1.8f));
            paint.setStyle(Paint.Style.STROKE);
            float spacing = weatherFront == WEATHER_SNOW ? dp(58) : dp(42);
            float slant = weatherFront == WEATHER_SNOW ? dp(18) : dp(30);
            for (float x = -(sceneryScroll * 0.72f) % spacing - dp(60); x < getWidth() + dp(110); x += spacing) {
                for (float y = dp(82); y < getGroundY() - dp(12); y += spacing * 0.78f) {
                    canvas.drawLine(x, y, x - slant, y + dp(18), paint);
                }
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
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

    private void drawPlayerLaneGuide(Canvas canvas) {
        if (state == STATE_SPLASH || state == STATE_MENU || state == STATE_MAP || state == STATE_CUSTOMIZE) {
            return;
        }
        float ground = getGroundY();
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 4.0f);
        float comboBoost = Math.min(1f, gameState.combo / 8f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(74 + Math.round(56 * comboBoost), 255, 218, 121));
        canvas.drawOval(playerX - playerRadius * 1.55f, ground - dp(9), playerX + playerRadius * 1.55f, ground + dp(8), paint);

        paint.setColor(Color.argb(52 + Math.round(42 * pulse), 255, 246, 207));
        canvas.drawOval(playerX - playerRadius * 1.10f, ground - dp(6), playerX + playerRadius * 1.10f, ground + dp(5), paint);

        if (state == STATE_RUNNING && grounded) {
            paint.setColor(Color.argb(70, 210, 232, 238));
            for (int i = 0; i < 3; i++) {
                float width = playerRadius * (1.4f + i * 0.45f);
                float y = ground - dp(21 + i * 7);
                float left = playerX - playerRadius * (2.1f + i * 0.58f) - (groundScroll * 0.24f % dp(18));
                canvas.drawRoundRect(left, y, left + width, y + dp(3), dp(2), dp(2), paint);
            }
        }
    }

    private void drawAuroraRushTrail(Canvas canvas) {
        if (auroraRushTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, auroraRushTimer / AURORA_RUSH_SECONDS);
        float ground = getGroundY();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(74 + 90 * pct), 77, 219, 184));
        for (int i = 0; i < 5; i++) {
            float y = ground - dp(44 + i * 18);
            float offset = (sceneryScroll * (0.45f + i * 0.08f)) % dp(230);
            for (float x = -offset - dp(40); x < getWidth() + dp(260); x += dp(230)) {
                canvas.drawRoundRect(x, y, x + dp(98 + i * 12), y + dp(4), dp(3), dp(3), paint);
            }
        }
        paint.setColor(Color.argb(Math.round(56 + 66 * pct), 255, 218, 121));
        canvas.drawOval(playerX - playerRadius * 2.0f, ground - dp(15), playerX + playerRadius * 2.0f, ground + dp(12), paint);
    }

    private void drawAuroraFocusTrail(Canvas canvas) {
        if (auroraFocusTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, auroraFocusTimer / AURORA_FOCUS_SECONDS);
        float ground = getGroundY();
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 9f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(48 + 70 * pct), 77, 219, 184));
        canvas.drawOval(playerX - playerRadius * (1.8f + pulse * 0.25f), ground - dp(18), playerX + playerRadius * (1.8f + pulse * 0.25f), ground + dp(12), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.8f));
        paint.setColor(Color.argb(Math.round(140 + 80 * pct), 255, 246, 207));
        canvas.drawCircle(playerX, playerY - playerRadius * 0.35f, playerRadius * (1.35f + pulse * 0.12f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHazard(Canvas canvas, Hazard hazard) {
        Bitmap sheet = sheetForHazard(hazard.label);
        float xRadius = hazard.radius * hazardHorizontalScale(hazard.label);
        float yRadius = hazard.radius * hazardVerticalScale(hazard.label);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(85, 0, 0, 0));
        canvas.drawOval(hazard.x - xRadius * 0.72f, hazard.y + yRadius * 0.66f, hazard.x + xRadius * 0.72f, hazard.y + yRadius * 0.88f, paint);

        drawHazardMotionAccent(canvas, hazard, xRadius, yRadius);

        Bitmap roarSprite = roarSpriteForHazard(hazard.label);
        if (hazard.roaring && roarSprite != null) {
            drawRoarHazardSprite(canvas, hazard, roarSprite, yRadius);
        } else if (sheet != null) {
            drawAnimatedHazardSheet(canvas, hazard, sheet, yRadius);
        } else if (hazard.drawable != null) {
            drawDrawable(canvas, hazard.drawable, hazard.x - xRadius, hazard.y - yRadius, hazard.x + xRadius, hazard.y + yRadius);
        } else if ("AVALANCHE".equals(hazard.label)) {
            drawAvalancheHazard(canvas, hazard, xRadius, yRadius);
        } else if ("THIN ICE".equals(hazard.label)) {
            drawThinIceHazard(canvas, hazard, xRadius, yRadius);
        } else {
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(hazard.x, hazard.y, hazard.radius, paint);
        }
        drawHazardBadge(canvas, hazard, yRadius);
    }

    private void drawAvalancheHazard(Canvas canvas, Hazard hazard, float xRadius, float yRadius) {
        float phase = hazardVisualPhase(hazard);
        float roll = (float) Math.sin(phase * 7.0f) * dp(2.2f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(248, 252, 253));
        canvas.drawCircle(hazard.x + roll, hazard.y, hazard.radius * 1.05f, paint);
        paint.setColor(Color.rgb(190, 207, 216));
        canvas.drawCircle(hazard.x - xRadius * 0.30f + roll * 0.4f, hazard.y - yRadius * 0.25f, hazard.radius * 0.28f, paint);
        canvas.drawCircle(hazard.x + xRadius * 0.22f - roll * 0.3f, hazard.y + yRadius * 0.10f, hazard.radius * 0.20f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(190, 132, 213, 232));
        canvas.drawCircle(hazard.x + roll, hazard.y, hazard.radius * 1.05f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawThinIceHazard(Canvas canvas, Hazard hazard, float xRadius, float yRadius) {
        float ground = getGroundY();
        float phase = hazardVisualPhase(hazard);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(142, 132, 213, 232));
        canvas.drawOval(hazard.x - xRadius * 1.12f, ground - dp(16), hazard.x + xRadius * 1.12f, ground + dp(5), paint);
        paint.setColor(Color.argb(185, 248, 252, 253));
        canvas.drawOval(hazard.x - xRadius * 0.92f, ground - dp(12), hazard.x + xRadius * 0.92f, ground + dp(2), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(2.1f));
        paint.setColor(Color.rgb(20, 86, 108));
        float crack = (float) Math.sin(phase * 4.4f) * dp(2);
        canvas.drawLine(hazard.x - xRadius * 0.72f, ground - dp(7), hazard.x - xRadius * 0.20f, ground - dp(2) + crack, paint);
        canvas.drawLine(hazard.x - xRadius * 0.20f, ground - dp(2) + crack, hazard.x + xRadius * 0.20f, ground - dp(9), paint);
        canvas.drawLine(hazard.x + xRadius * 0.20f, ground - dp(9), hazard.x + xRadius * 0.66f, ground - dp(4) - crack, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHazardMotionAccent(Canvas canvas, Hazard hazard, float xRadius, float yRadius) {
        paint.setStyle(Paint.Style.FILL);
        float phase = hazardVisualPhase(hazard);
        if ("SALMON".equals(hazard.label)) {
            paint.setColor(Color.argb(100, 132, 213, 232));
            for (int i = 0; i < 3; i++) {
                float bubbleX = hazard.x + xRadius * (0.35f + i * 0.24f);
                float bubbleY = hazard.y + (float) Math.sin(phase * 4.0f + i) * dp(7) + yRadius * 0.18f;
                canvas.drawCircle(bubbleX, bubbleY, dp(2.4f + i * 0.6f), paint);
            }
        } else if ("EAGLE".equals(hazard.label) || "DARK".equals(hazard.label)) {
            paint.setColor(Color.argb(78, 255, 255, 255));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(dp(2.2f));
            paint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < 3; i++) {
                float y = hazard.y + yRadius * (-0.35f + i * 0.32f);
                canvas.drawLine(hazard.x + xRadius * 0.28f, y, hazard.x + xRadius * (0.92f + i * 0.14f), y - dp(7 + i * 3), paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeCap(Paint.Cap.BUTT);
        } else if ("WOLF".equals(hazard.label)) {
            paint.setColor(Color.argb(92, 235, 245, 248));
            for (int i = 0; i < 3; i++) {
                float left = hazard.x + xRadius * (0.10f + i * 0.20f);
                float top = hazard.y + yRadius * (0.58f + i * 0.05f);
                canvas.drawOval(left, top, left + xRadius * 0.38f, top + yRadius * 0.14f, paint);
            }
        } else if ("AVALANCHE".equals(hazard.label)) {
            paint.setColor(Color.argb(118, 235, 245, 248));
            for (int i = 0; i < 4; i++) {
                float left = hazard.x + xRadius * (-0.62f + i * 0.28f);
                float top = hazard.y + yRadius * (0.20f + i * 0.04f);
                canvas.drawOval(left, top, left + xRadius * 0.56f, top + yRadius * 0.30f, paint);
            }
        } else if ("THIN ICE".equals(hazard.label)) {
            paint.setColor(Color.argb(92, 77, 219, 184));
            canvas.drawOval(hazard.x - xRadius * 1.25f, getGroundY() - dp(19), hazard.x + xRadius * 1.25f, getGroundY() + dp(7), paint);
        }
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

    private Bitmap sheetForHazard(String label) {
        if ("MOOSE".equals(label)) {
            return assets.mooseWalkSheet();
        }
        if ("BEAR".equals(label)) {
            return assets.bearWalkSheet();
        }
        if ("POLAR".equals(label)) {
            return assets.polarBearWalkSheet();
        }
        if ("WOLF".equals(label)) {
            return assets.wolfRunSheet();
        }
        if ("SALMON".equals(label)) {
            return assets.salmonSwimSheet();
        }
        if ("EAGLE".equals(label) || "DARK".equals(label)) {
            return assets.eagleFlySheet();
        }
        return null;
    }

    private Bitmap roarSpriteForHazard(String label) {
        if ("BEAR".equals(label)) {
            return assets.bearRoarSprite();
        }
        if ("POLAR".equals(label)) {
            return assets.polarBearRoarSprite();
        }
        return null;
    }

    private void drawRoarHazardSprite(Canvas canvas, Hazard hazard, Bitmap sprite, float yRadius) {
        if (sprite == null || sprite.getWidth() <= 0 || sprite.getHeight() <= 0) {
            return;
        }
        float height = yRadius * ("POLAR".equals(hazard.label) ? 3.75f : 3.58f);
        float width = height * (sprite.getWidth() / (float) sprite.getHeight());
        float bottom = getGroundY() + dp(1);
        float shake = (float) Math.sin(hazardVisualPhase(hazard) * 13.0f) * dp(0.7f);
        tempRect.set(hazard.x - width * 0.50f + shake, bottom - height, hazard.x + width * 0.50f + shake, bottom);
        canvas.drawBitmap(sprite, null, tempRect, spriteBitmapPaint);
    }

    private float hazardHorizontalScale(String label) {
        if ("MOOSE".equals(label)) return 1.70f;
        if ("BEAR".equals(label)) return 1.60f;
        if ("POLAR".equals(label)) return 1.72f;
        if ("WOLF".equals(label)) return 1.74f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 1.65f;
        if ("SALMON".equals(label)) return 1.35f;
        if ("AVALANCHE".equals(label)) return 1.38f;
        if ("THIN ICE".equals(label)) return 1.85f;
        return 1.0f;
    }

    private float hazardVerticalScale(String label) {
        if ("MOOSE".equals(label) || "BEAR".equals(label)) return 1.08f;
        if ("POLAR".equals(label)) return 1.02f;
        if ("WOLF".equals(label)) return 0.84f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 1.00f;
        if ("SALMON".equals(label)) return 0.80f;
        if ("AVALANCHE".equals(label)) return 1.00f;
        if ("THIN ICE".equals(label)) return 0.42f;
        return 1.0f;
    }

    private void drawAnimatedHazardSheet(Canvas canvas, Hazard hazard, Bitmap sheet, float yRadius) {
        String label = hazard.label;
        float phase = hazardVisualPhase(hazard);
        float rate = hazardAnimationRate(label);
        int frame = Math.floorMod((int) (phase * rate), 6);
        if (hazard.roaring) {
            frame = 3;
        }
        float centerY = hazard.y;
        float height = yRadius * 2.35f;
        float rotation = 0f;

        if ("SALMON".equals(label)) {
            centerY += (float) Math.sin(phase * 7.0f) * dp(3.0f);
            height = yRadius * 2.0f;
            rotation = (float) Math.sin(phase * 6.0f) * 9.0f;
        } else if ("EAGLE".equals(label) || "DARK".equals(label)) {
            centerY += (float) Math.sin(phase * 1.8f) * dp(4.5f);
            height = yRadius * 2.55f;
            rotation = (float) Math.sin(phase * 1.4f) * 2.2f;
        } else if ("BEAR".equals(label)) {
            centerY += (float) Math.sin(phase * 1.55f) * dp(0.35f);
            height = yRadius * 2.45f;
        } else if ("POLAR".equals(label)) {
            centerY += (float) Math.sin(phase * 1.45f) * dp(0.30f);
            height = yRadius * 2.35f;
        } else if ("WOLF".equals(label)) {
            centerY += (float) Math.sin(phase * 10.0f) * dp(1.6f);
            height = yRadius * 2.30f;
        } else if ("MOOSE".equals(label)) {
            centerY += (float) Math.sin(phase * 7.0f) * dp(1.0f);
            height = yRadius * 2.45f;
        }

        if (hazard.roaring) {
            centerY = getGroundY() - yRadius * 1.50f;
            height = yRadius * ("POLAR".equals(label) ? 3.35f : 3.22f);
            rotation = (float) Math.sin(phase * 18.0f) * 3.5f;
        }

        drawSpriteSheetFrame(canvas, sheet, SPRITE_SHEET_FRAMES, frame, hazard.x, centerY, height, rotation);
    }

    private float hazardAnimationRate(String label) {
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 3.05f;
        if ("BEAR".equals(label) || "POLAR".equals(label)) return 2.85f;
        if ("MOOSE".equals(label)) return 3.10f;
        if ("WOLF".equals(label)) return 4.80f;
        if ("SALMON".equals(label)) return 4.25f;
        return 3.0f;
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

    private void drawPowerUp(Canvas canvas, PowerUp powerUp) {
        float bob = (float) Math.sin(powerUp.bob) * dp(4);
        float pulse = 1f + (float) Math.sin(powerUp.spin * 1.8f) * 0.10f;
        float r = powerUp.radius * pulse;
        float x = powerUp.x;
        float y = powerUp.y + bob;

        paint.setStyle(Paint.Style.FILL);
        boolean cache = "CACHE".equals(powerUp.type);
        boolean focus = "FOCUS".equals(powerUp.type);
        paint.setColor(cache ? Color.argb(82, 255, 218, 121) : focus ? Color.argb(90, 77, 219, 184) : Color.argb(80, 132, 213, 232));
        canvas.drawCircle(x, y, r * 2.0f, paint);
        if (cache) {
            paint.setColor(Color.rgb(134, 78, 38));
            canvas.drawRoundRect(x - r * 1.05f, y - r * 0.66f, x + r * 1.05f, y + r * 0.70f, r * 0.26f, r * 0.26f, paint);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawRoundRect(x - r * 0.86f, y - r * 0.48f, x + r * 0.86f, y - r * 0.18f, r * 0.18f, r * 0.18f, paint);
            paint.setColor(Color.rgb(255, 246, 207));
            PathCompat.star(canvas, paint, x, y + r * 0.23f, r * 0.34f);
        } else if (focus) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.rgb(77, 219, 184));
            canvas.drawCircle(x, y, r * 1.18f, paint);
            canvas.drawCircle(x, y, r * 0.62f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 246, 207));
            PathCompat.triangle(canvas, paint, x - r * 0.22f, y + r * 0.72f, x + r * 0.95f, y - r * 0.08f, x - r * 0.12f, y - r * 0.80f);
            paint.setColor(Color.rgb(77, 219, 184));
            canvas.drawCircle(x + r * 0.30f, y - r * 0.10f, r * 0.24f, paint);
        } else {
            paint.setColor(Color.rgb(132, 213, 232));
            canvas.drawCircle(x, y, r * 1.15f, paint);
            paint.setColor(Color.rgb(12, 24, 36));
            canvas.drawCircle(x, y, r * 0.76f, paint);
            paint.setColor(Color.rgb(255, 246, 207));
            PathCompat.star(canvas, paint, x, y - r * 0.05f, r * 0.52f);
        }
    }

    private void drawShieldAura(Canvas canvas) {
        if (!gameState.shieldActive) {
            return;
        }
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 6.0f);
        float headY = playerY - playerRadius * PLAYER_HEAD_DRAW_OFFSET;
        float centerY = headY + playerRadius * 1.70f;
        float width = playerRadius * (2.65f + pulse * 0.20f);
        float height = playerRadius * (4.15f + pulse * 0.25f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(42 + Math.round(38 * pulse), 132, 213, 232));
        canvas.drawOval(playerX - width * 0.50f, centerY - height * 0.50f, playerX + width * 0.50f, centerY + height * 0.50f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(150 + Math.round(70 * pulse), 255, 246, 207));
        canvas.drawOval(playerX - width * 0.50f, centerY - height * 0.50f, playerX + width * 0.50f, centerY + height * 0.50f, paint);
        paint.setStyle(Paint.Style.FILL);
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

    private void drawBossAttack(Canvas canvas, BossAttack attack) {
        if (attack.type == ATTACK_SHOCKWAVE) {
            float h = Math.max(dp(8), attack.radius * 0.55f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(118, 235, 245, 248));
            canvas.drawOval(attack.x - attack.radius * 1.55f, attack.y - h, attack.x + attack.radius * 1.55f, attack.y + h, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.argb(185, 255, 246, 207));
            canvas.drawOval(attack.x - attack.radius * 1.55f, attack.y - h, attack.x + attack.radius * 1.55f, attack.y + h, paint);
            paint.setStyle(Paint.Style.FILL);
            return;
        }

        int saved = canvas.save();
        canvas.rotate(attack.spin * 30f, attack.x, attack.y);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(160, 132, 213, 232));
        PathCompat.triangle(canvas, paint, attack.x - attack.radius * 1.30f, attack.y, attack.x + attack.radius * 0.65f, attack.y - attack.radius, attack.x + attack.radius * 0.75f, attack.y + attack.radius);
        paint.setColor(Color.rgb(248, 252, 253));
        PathCompat.triangle(canvas, paint, attack.x - attack.radius * 0.78f, attack.y, attack.x + attack.radius * 0.36f, attack.y - attack.radius * 0.62f, attack.x + attack.radius * 0.42f, attack.y + attack.radius * 0.62f);
        canvas.restoreToCount(saved);
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
        float radius = bossRadius();
        Bitmap bossSheet = sheetForBoss(selectedStage);
        float xRadius = radius * bossHorizontalScale(selectedStage);
        float yRadius = radius * bossVerticalScale(selectedStage);
        float shadowY = getGroundY() + dp(4);
        int shadowAlpha = selectedStage == 1 || selectedStage == 3 ? 64 : 132;

        paint.setStyle(Paint.Style.FILL);
        drawBossTell(canvas, radius);
        if (damageFlash > 0f) {
            paint.setColor(Color.argb(125, 255, 255, 255));
            canvas.drawOval(bossX - xRadius * 0.96f, bossY - yRadius * 0.96f, bossX + xRadius * 0.96f, bossY + yRadius * 0.96f, paint);
        }
        if (bossWeakWindowActive()) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(bossTimer * 18f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.4f));
            paint.setColor(Color.argb(150 + Math.round(70 * pulse), 255, 218, 121));
            canvas.drawOval(bossX - xRadius * 1.02f, bossY - yRadius * 1.02f, bossX + xRadius * 1.02f, bossY + yRadius * 1.02f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        paint.setColor(Color.argb(shadowAlpha, 0, 0, 0));
        canvas.drawOval(bossX - xRadius * 0.88f, shadowY - dp(9), bossX + xRadius * 0.88f, shadowY + dp(9), paint);

        if (bossSheet != null) {
            drawAnimatedBossSheet(canvas, bossSheet, radius);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(bossX, bossY, radius, paint);
        }

        drawBossHealthBar(canvas);
    }

    private void drawBossTell(Canvas canvas, float radius) {
        if (bossState != BOSS_STATE_TELL) {
            return;
        }
        float pct = Math.min(1f, bossStateTimer / Math.max(0.01f, bossTellDuration()));
        float alphaPulse = 0.45f + 0.55f * (float) Math.sin(bossStateTimer * 24f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.4f));
        paint.setColor(Color.argb(Math.round((95 + 120 * pct) * alphaPulse), 255, 98, 84));
        if (bossPattern == BOSS_PATTERN_LUNGE) {
            float left = playerX + dp(36);
            float right = Math.min(getWidth() - dp(36), bossX - radius * 0.35f);
            float top = getGroundY() - dp(56);
            float bottom = getGroundY() - dp(8);
            canvas.drawRoundRect(left, top, right, bottom, dp(12), dp(12), paint);
        } else if (bossPattern == BOSS_PATTERN_SNOW_WAVE) {
            canvas.drawCircle(bossX - radius * 0.9f, getGroundY() - dp(24), dp(18 + pct * 10), paint);
            canvas.drawCircle(bossX - radius * 1.05f, getGroundY() - dp(78), dp(15 + pct * 8), paint);
        } else {
            canvas.drawCircle(bossX, bossY - radius * 0.6f, radius * (0.82f + pct * 0.26f), paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private Bitmap sheetForBoss(int stage) {
        if (stage == 1) {
            return assets.salmonSwimSheet();
        }
        if (stage == 2) {
            return assets.mooseWalkSheet();
        }
        if (stage == 3) {
            return assets.eagleFlySheet();
        }
        if (stage == 4) {
            return assets.polarBearWalkSheet();
        }
        return null;
    }

    private float bossHorizontalScale(int stage) {
        if (stage == 1) return 1.60f;
        if (stage == 2) return 1.72f;
        if (stage == 3) return 1.72f;
        if (stage == 4) return 1.74f;
        return 1.30f;
    }

    private float bossVerticalScale(int stage) {
        if (stage == 1) return 1.02f;
        if (stage == 2) return 1.08f;
        if (stage == 3) return 1.10f;
        if (stage == 4) return 1.04f;
        return 1.30f;
    }

    private void drawAnimatedBossSheet(Canvas canvas, Bitmap sheet, float radius) {
        float phase = bossTimer + selectedStage * 0.37f;
        float rate = selectedStage == 3 ? 3.05f : selectedStage == 1 ? 4.25f : selectedStage == 4 ? 3.55f : 2.85f;
        if (bossState == BOSS_STATE_TELL) {
            rate *= 0.45f;
        } else if (bossState == BOSS_STATE_ATTACK) {
            rate *= 1.55f;
        }
        int frame = Math.floorMod((int) (phase * rate), 6);
        if (bossStunTimer > 0f) {
            frame = Math.floorMod(frame + 2, 6);
        }
        float centerY = bossY;
        float height = radius * 3.3f;
        float rotation = 0f;

        if (selectedStage == 1) {
            centerY += (float) Math.sin(phase * 2.5f) * dp(4.0f);
            height = radius * 2.45f;
            rotation = (float) Math.sin(phase * 2.1f) * 7.0f;
        } else if (selectedStage == 3) {
            centerY += (float) Math.sin(phase * 1.9f) * dp(5.0f);
            height = radius * 3.15f;
            rotation = (float) Math.sin(phase * 1.5f) * 2.4f;
        } else if (selectedStage == 4) {
            height = radius * 2.95f;
            if (bossState == BOSS_STATE_TELL) {
                rotation = (float) Math.sin(phase * 18f) * 1.8f;
                centerY -= dp(2);
            } else if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
                rotation = -3.0f;
                centerY += dp(3);
            } else if (bossState == BOSS_STATE_RECOVER) {
                rotation = 1.4f;
            }
        }

        drawSpriteSheetFrame(canvas, sheet, SPRITE_SHEET_FRAMES, frame, bossX, centerY, height, rotation);
    }

    private void drawSpriteSheetFrame(Canvas canvas, Bitmap sheet, int frames, int frameIndex, float centerX, float centerY, float height, float rotationDegrees) {
        if (sheet == null || frames <= 0 || sheet.getWidth() <= 0 || sheet.getHeight() <= 0) {
            return;
        }

        int frameWidth = sheet.getWidth() / frames;
        if (frameWidth <= 0) {
            return;
        }

        int safeFrame = Math.floorMod(frameIndex, frames);
        int[] trim = spriteSheetTrim(sheet, safeFrame);
        if (trim == null) {
            spriteSourceRect.set(safeFrame * frameWidth, 0, (safeFrame + 1) * frameWidth, sheet.getHeight());
        } else {
            setTrimmedSpriteSource(spriteSourceRect, safeFrame, frameWidth, sheet.getHeight(), trim);
        }

        float width = height * (spriteSourceRect.width() / (float) spriteSourceRect.height());
        tempRect.set(centerX - width * 0.50f, centerY - height * 0.50f, centerX + width * 0.50f, centerY + height * 0.50f);

        int saved = canvas.save();
        if (Math.abs(rotationDegrees) > 0.01f) {
            canvas.rotate(rotationDegrees, centerX, centerY);
        }
        canvas.drawBitmap(sheet, spriteSourceRect, tempRect, spriteBitmapPaint);
        canvas.restoreToCount(saved);
    }

    private int[] spriteSheetTrim(Bitmap sheet, int safeFrame) {
        if (sheet == assets.mooseWalkSheet()) return MOOSE_FRAME_TRIMS[safeFrame];
        if (sheet == assets.bearWalkSheet()) return BEAR_FRAME_TRIMS[safeFrame];
        if (sheet == assets.polarBearWalkSheet()) return POLAR_BEAR_FRAME_TRIMS[safeFrame];
        if (sheet == assets.wolfRunSheet()) return WOLF_FRAME_TRIMS[safeFrame];
        if (sheet == assets.salmonSwimSheet()) return SALMON_FRAME_TRIMS[safeFrame];
        if (sheet == assets.eagleFlySheet()) return EAGLE_FRAME_TRIMS[safeFrame];
        return null;
    }

    static void setTrimmedSpriteSource(Rect out, int safeFrame, int frameWidth, int sheetHeight, int[] trim) {
        int[] values = trimmedSpriteSourceValues(safeFrame, frameWidth, sheetHeight, trim);
        out.set(values[0], values[1], values[2], values[3]);
    }

    static int[] trimmedSpriteSourceValues(int safeFrame, int frameWidth, int sheetHeight, int[] trim) {
        return SpriteSheetMath.trimmedSourceValues(safeFrame, frameWidth, sheetHeight, trim);
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
        return bossRadiusForStage(selectedStage);
    }

    private float bossRadiusForStage(int stage) {
        if (stage == 4) return gameplayDp(38);
        if (stage == 2) return gameplayDp(34);
        return gameplayDp(30);
    }

    private float bossAttackX(int stage) {
        float rightAnchor = getWidth() - dp(stage == 4 ? 94 : 82);
        float forwardLimit = Math.max(playerX + dp(stage == 4 ? 164 : 142), getWidth() * (stage == 4 ? 0.64f : 0.60f));
        forwardLimit = Math.min(forwardLimit, rightAnchor - dp(54));
        float surge = 0.5f + 0.5f * (float) Math.sin(bossTimer * (0.95f + stage * 0.08f));
        return rightAnchor - (rightAnchor - forwardLimit) * surge;
    }

    private float bossRestX(int stage) {
        return getWidth() - dp(stage == 4 ? 102 : 86);
    }

    private float bossHurtCenterY() {
        if (selectedStage == 1 || selectedStage == 3) {
            return bossY;
        }
        return bossY + bossRadius() * 0.16f;
    }

    private float bossContactRadius() {
        float radius = bossRadius();
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            return radius * (selectedStage == 4 ? 0.96f : 0.88f);
        }
        return radius * (selectedStage == 4 ? 0.72f : 0.66f);
    }

    private boolean bossWeakWindowActive() {
        return bossActive && bossState == BOSS_STATE_RECOVER && bossStateTimer < bossRecoverDuration() * 0.72f;
    }

    private float bossLaneCenterY(int stage) {
        if (stage == 1) {
            return clamp(getGroundY() - dp(92), dp(116), getGroundY() - dp(72));
        }
        if (stage == 3) {
            return clamp(getGroundY() - dp(154), dp(112), getGroundY() - dp(118));
        }
        return getGroundY() - bossDrawHeight(stage) * 0.5f + dp(1);
    }

    private float bossDrawHeight(int stage) {
        float radius = bossRadiusForStage(stage);
        if (stage == 1) return radius * 2.45f;
        if (stage == 3) return radius * 3.15f;
        if (stage == 4) return radius * 2.95f;
        if (sheetForBoss(stage) != null) return radius * 3.3f;
        return radius * 2f;
    }

    private void drawCharacter(Canvas canvas, float x, float y, float radius) {
        spriteRenderer.drawRunner(canvas, playerFrame(x, y, radius));
    }

    private void drawCharacterPreview(Canvas canvas, float x, float y, float radius) {
        spriteRenderer.drawStanding(canvas, playerFrame(x, y, radius));
    }

    private SpriteRenderer.PlayerFrame playerFrame(float x, float y, float radius) {
        int outfitColor = playerPhoto == null ? Color.rgb(255, 218, 121) : OUTFIT_COLORS[selectedOutfit];
        return new SpriteRenderer.PlayerFrame(x, y, radius, runnerClock, grounded, playerVelocityY, playerPhoto, outfitColor);
    }

    private void drawReadyScreen(Canvas canvas) {
        StageConfig stage = STAGES[selectedStage];
        float panelWidth = Math.min(getWidth() - dp(56), dp(462));
        float panelHeight = dp(192);
        float left = (getWidth() - panelWidth) / 2f;
        float top = Math.max(dp(84), getHeight() * 0.26f);
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);
        float pulse = 0.5f + 0.5f * (float) Math.sin(readyTimer * 4.0f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(220, 12, 20, 31));
        canvas.drawRoundRect(panel, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.4f));
        paint.setColor(Color.argb(195 + Math.round(45 * pulse), 255, 218, 121));
        canvas.drawRoundRect(panel, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(13));
        canvas.drawText("2.0 BETA EXPEDITION · LEVEL " + (selectedStage + 1), getWidth() / 2f, top + dp(28), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(28));
        canvas.drawText(stage.name, getWidth() / 2f, top + dp(64), textPaint);

        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(stage.line, getWidth() / 2f, top + dp(94), textPaint);

        float chipTop = top + dp(112);
        float chipWidth = (panelWidth - dp(52)) / 3f;
        drawBriefingChip(canvas, left + dp(16), chipTop, chipWidth, "GOAL", stage.goalGates + " HURDLES");
        drawBriefingChip(canvas, left + dp(26) + chipWidth, chipTop, chipWidth, "BOSS", stage.bossName);
        drawBriefingChip(canvas, left + dp(36) + chipWidth * 2f, chipTop, chipWidth, "MISSION", missionBriefLine());

        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Avoid wildlife. Fire stuns animals and damages bosses.", getWidth() / 2f, top + dp(166), textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("Tap to launch", getWidth() / 2f, top + dp(184), textPaint);
    }

    private void drawBriefingChip(Canvas canvas, float left, float top, float width, String label, String value) {
        float height = dp(40);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(170, 255, 255, 255));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(8), dp(8), paint);
        paint.setColor(Color.argb(210, 8, 18, 30));
        canvas.drawRoundRect(left + dp(1), top + dp(1), left + width - dp(1), top + height - dp(1), dp(7), dp(7), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(label, left + width / 2f, top + dp(14), textPaint);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(value, left + width / 2f, top + dp(29), textPaint);
    }

    private void drawHud(Canvas canvas) {
        float barLeft = dp(10);
        float barTop = dp(10);
        float barRight = getWidth() - dp(10);
        float barBottom = dp(68);
        float progress = STAGES[selectedStage].goalGates <= 0 ? 0f : Math.min(1f, gatesPassed / (float) STAGES[selectedStage].goalGates);
        if (bossActive) {
            progress = bossMaxHealth <= 0 ? 0f : 1f - Math.max(0f, bossHealth / (float) bossMaxHealth);
        }
        int multiplier = activeScoreMultiplier();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(172, 10, 18, 29));
        canvas.drawRoundRect(barLeft, barTop, barRight, barBottom, dp(10), dp(10), paint);
        paint.setColor(Color.argb(54, 255, 255, 255));
        canvas.drawRoundRect(barLeft + dp(2), barTop + dp(2), barRight - dp(2), barTop + dp(18), dp(9), dp(9), paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("SCORE", dp(20), dp(25), textPaint);
        textPaint.setTextSize(dp(17));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(score), dp(20), dp(49), textPaint);
        textPaint.setTextSize(dp(9));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("RUN +" + runStageScore, dp(82), dp(49), textPaint);

        float progressLeft = getWidth() * 0.34f;
        float progressRight = getWidth() * 0.66f;
        float progressTop = dp(19);
        float progressBottom = dp(29);
        paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRoundRect(progressLeft, progressTop, progressRight, progressBottom, dp(6), dp(6), paint);
        paint.setColor(bossActive ? Color.rgb(255, 98, 84) : Color.rgb(255, 218, 121));
        canvas.drawRoundRect(progressLeft, progressTop, progressLeft + (progressRight - progressLeft) * progress, progressBottom, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(190, 255, 255, 255));
        canvas.drawRoundRect(progressLeft, progressTop, progressRight, progressBottom, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(255, 218, 121));
        String objective = bossActive
                ? "FIRE BOSS " + Math.max(0, bossHealth) + "/" + bossMaxHealth + "  " + bossPatternLabel()
                : "HURDLES " + gatesPassed + "/" + STAGES[selectedStage].goalGates;
        canvas.drawText(objective, getWidth() / 2f, dp(44), textPaint);
        textPaint.setColor(Color.WHITE);
        String comboLabel = (auroraRushTimer > 0f ? "AURORA " : "") + "COMBO " + gameState.combo + "   SCORE x" + multiplier;
        canvas.drawText(comboLabel, getWidth() / 2f, dp(58), textPaint);
        drawAuroraMeter(canvas, progressLeft, progressRight, dp(61), dp(65));

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(STAGES[selectedStage].name, getWidth() - dp(20), dp(25), textPaint);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("BEST " + bestScore + "   LV " + gameState.level + "   T " + trailTokens + (gameState.muted ? "  MUTE" : ""), getWidth() - dp(20), dp(47), textPaint);

        drawHudIcons(canvas);
        drawActionOverlay(canvas);
        drawJourneyTracker(canvas);
        drawMissionTracker(canvas);

        if (debugOverlay) {
            drawDebugOverlay(canvas);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawAuroraMeter(Canvas canvas, float left, float right, float top, float bottom) {
        float pct = auroraRushTimer > 0f
                ? Math.min(1f, auroraRushTimer / AURORA_RUSH_SECONDS)
                : Math.min(1f, auroraMeter / AURORA_METER_MAX);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(145, 4, 12, 20));
        canvas.drawRoundRect(left, top, right, bottom, dp(3), dp(3), paint);
        paint.setColor(auroraRushTimer > 0f ? Color.rgb(77, 219, 184) : Color.rgb(132, 213, 232));
        canvas.drawRoundRect(left, top, left + (right - left) * pct, bottom, dp(3), dp(3), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(170, 255, 246, 207));
        canvas.drawRoundRect(left, top, right, bottom, dp(3), dp(3), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHudIcons(Canvas canvas) {
        float lifeX = dp(145);
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

        if (gameState.shieldActive) {
            float shieldX = getWidth() - dp(54);
            float shieldY = dp(22);
            paint.setColor(Color.rgb(132, 213, 232));
            canvas.drawCircle(shieldX, shieldY, dp(7), paint);
            paint.setColor(Color.rgb(12, 24, 36));
            canvas.drawCircle(shieldX, shieldY, dp(4.5f), paint);
            paint.setColor(Color.WHITE);
            PathCompat.star(canvas, paint, shieldX, shieldY, dp(3.2f));
        }

        if (auroraFocusTimer > 0f) {
            float focusX = getWidth() - dp(72);
            float focusY = dp(22);
            paint.setColor(Color.rgb(77, 219, 184));
            canvas.drawCircle(focusX, focusY, dp(7), paint);
            paint.setColor(Color.rgb(8, 18, 30));
            canvas.drawCircle(focusX, focusY, dp(4.6f), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.4f));
            paint.setColor(Color.WHITE);
            canvas.drawCircle(focusX, focusY, dp(3.3f), paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawJourneyTracker(Canvas canvas) {
        if (state == STATE_SPLASH || state == STATE_MENU || state == STATE_MAP || state == STATE_CUSTOMIZE) {
            return;
        }
        if (bossWarningTimer > 0f || runCalloutTimer > 0f) {
            return;
        }
        float width = Math.min(getWidth() - dp(92), dp(360));
        float height = dp(20);
        float left = (getWidth() - width) / 2f;
        float top = bossActive ? dp(84) : dp(94);
        float progress = STAGES[selectedStage].goalGates <= 0 ? 0f : Math.min(1f, gatesPassed / (float) STAGES[selectedStage].goalGates);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(122, 8, 18, 30));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(7), dp(7), paint);
        paint.setColor(Color.argb(120, 77, 219, 184));
        canvas.drawRoundRect(left, top, left + width * progress, top + height, dp(7), dp(7), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(165, 255, 246, 207));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(7), dp(7), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8.5f));
        textPaint.setColor(Color.WHITE);
        String leg = bossActive ? "BOSS TERRITORY" : routeMilestoneLabel(selectedStage, clampInt(routeMilestoneIndex, 0, 2));
        String focus = auroraFocusTimer > 0f ? "  FOCUS " + Math.round(auroraFocusTimer) : "";
        String weather = weatherFront == WEATHER_CLEAR ? "" : "  " + weatherFrontLabel().toUpperCase();
        canvas.drawText(leg + (campReached ? "  CAMP RESTOCKED" : "") + focus + weather, getWidth() / 2f, top + dp(13.5f), textPaint);
    }

    private void drawMissionTracker(Canvas canvas) {
        if (state == STATE_SPLASH || state == STATE_MENU || state == STATE_MAP || state == STATE_CUSTOMIZE) {
            return;
        }
        if (bossActive) {
            return;
        }
        if (bossWarningTimer > 0f || runCalloutTimer > 0f) {
            return;
        }
        float width = Math.min(getWidth() - dp(70), dp(430));
        float height = dp(22);
        float left = (getWidth() - width) / 2f;
        float top = dp(69);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(138, 8, 18, 30));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(160, 132, 213, 232));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8.5f));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText(missionProgressLine(), getWidth() / 2f, top + dp(14.5f), textPaint);
    }

    private void drawActionOverlay(Canvas canvas) {
        if (bossWarningTimer > 0f) {
            float pct = Math.min(1f, bossWarningTimer / 2.2f);
            float width = Math.min(getWidth() - dp(64), dp(420));
            float height = dp(44);
            float left = (getWidth() - width) / 2f;
            float top = dp(76);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(205 * pct), 82, 18, 24));
            canvas.drawRoundRect(left, top, left + width, top + height, dp(10), dp(10), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.argb(Math.round(230 * pct), 255, 218, 121));
            canvas.drawRoundRect(left, top, left + width, top + height, dp(10), dp(10), paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(19));
            textPaint.setColor(Color.argb(Math.round(255 * pct), 255, 246, 207));
            canvas.drawText("BOSS INCOMING", getWidth() / 2f, top + dp(29), textPaint);
            return;
        }

        if (runCalloutTimer <= 0f || runCallout.length() == 0) {
            return;
        }
        float pct = Math.min(1f, runCalloutTimer / 1.15f);
        float width = Math.min(getWidth() - dp(86), Math.max(dp(190), runCallout.length() * dp(10)));
        float height = dp(32);
        float left = (getWidth() - width) / 2f;
        float top = dp(78);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(190 * pct), 8, 18, 30));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(9), dp(9), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(Math.round(220 * pct), 255, 218, 121));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(9), dp(9), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.argb(Math.round(255 * pct), 255, 246, 207));
        canvas.drawText(runCallout, getWidth() / 2f, top + dp(22), textPaint);
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
        float panelHeight = dp(344);
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
        canvas.drawText("Missions " + missionsCompleted + "/3 · Combo " + gameState.bestCombo + " · Rank " + runRank(), getWidth() / 2f, top + dp(142), textPaint);
        canvas.drawText("Tokens +" + runTokensEarned + " · Bank " + trailTokens + " · Rush " + auroraRushes, getWidth() / 2f, top + dp(166), textPaint);
        canvas.drawText(dailyResultLine(), getWidth() / 2f, top + dp(188), textPaint);
        canvas.drawText(expeditionLine(false), getWidth() / 2f, top + dp(210), textPaint);
        canvas.drawText(badgeSummaryLine(), getWidth() / 2f, top + dp(232), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Tap anywhere to retry", getWidth() / 2f, top + dp(255), textPaint);

        setButton(secondaryButtonBounds, top + dp(294), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(294), dp(118), dp(36));
        thirdButtonBounds.offset(dp(64), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "CUSTOMIZE");
    }

    private void drawStageClearPanel(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(40), dp(348));
        float panelHeight = dp(348);
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
        canvas.drawText("Rank " + runRank() + " · Missions " + missionsCompleted + "/3 · Stars " + gameState.stars, getWidth() / 2f, top + dp(134), textPaint);
        canvas.drawText("Tokens +" + runTokensEarned + " · Bank " + trailTokens + " · " + (perfectRun ? "Perfect run" : "Lives lost " + livesLostThisRun), getWidth() / 2f, top + dp(160), textPaint);
        canvas.drawText(dailyResultLine(), getWidth() / 2f, top + dp(184), textPaint);
        canvas.drawText(expeditionLine(true), getWidth() / 2f, top + dp(207), textPaint);
        canvas.drawText(badgeSummaryLine(), getWidth() / 2f, top + dp(230), textPaint);
        canvas.drawText(stageClearLine(), getWidth() / 2f, top + dp(254), textPaint);

        setButton(secondaryButtonBounds, top + dp(292), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(292), dp(118), dp(36));
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
        textPaint.setTextSize(isLandscape() ? dp(8.5f) : dp(9.5f));
        canvas.drawText("TRIPPERDEE LABS · " + BuildConfig.BUILD_BADGE, getWidth() / 2f, isLandscape() ? dp(18) : dp(25), textPaint);

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
        if (!isOutfitUnlocked(selectedOutfit)) {
            paint.setColor(Color.argb(150, 0, 0, 0));
            canvas.drawRoundRect(left, top, left + size, top + size, dp(5), dp(5), paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(24, 30, 38));
        canvas.drawRoundRect(left, top, left + size, top + size, dp(5), dp(5), paint);
        paint.setStyle(Paint.Style.FILL);

        if (!isOutfitUnlocked(selectedOutfit)) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(9));
            textPaint.setColor(Color.WHITE);
            canvas.drawText("LOCK", left + size / 2f, top + size * 0.64f, textPaint);
        }
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

    private void drawDrawableAlpha(Canvas canvas, Drawable drawable, float left, float top, float right, float bottom, int alpha) {
        drawable.setAlpha(clampInt(alpha, 0, 255));
        drawable.setBounds(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
        drawable.draw(canvas);
        drawable.setAlpha(255);
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

    private void startDailyRush() {
        selectedStage = dailyStageIndex();
        selectedSeason = STAGES[selectedStage].season;
        backdropCacheKey = Integer.MIN_VALUE;
        saveChoices();
        logEvent("Daily Rush selected: " + STAGES[selectedStage].name + ".");
        startGame();
    }

    private void saveChoices() {
        prefs.edit()
                .putInt(PREF_SELECTED_STAGE, selectedStage)
                .putInt(PREF_SELECTED_SEASON, selectedSeason)
                .putInt(PREF_OUTFIT, effectiveOutfitIndex())
                .putInt(PREF_TRAIL_TOKENS, trailTokens)
                .putInt(PREF_UNLOCKED_OUTFITS, unlockedOutfitMask)
                .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                .putInt(PREF_DAILY_STREAK, dailyStreak)
                .apply();
    }

    private void handleOutfitTap() {
        if (!isOutfitUnlocked(selectedOutfit)) {
            int cost = OUTFIT_TOKEN_COSTS[selectedOutfit];
            if (RunRewardEconomy.canUnlockOutfit(trailTokens, unlockedOutfitMask, selectedOutfit, OUTFIT_TOKEN_COSTS)) {
                trailTokens -= cost;
                unlockedOutfitMask = RunRewardEconomy.unlockOutfit(unlockedOutfitMask, selectedOutfit);
                saveChoices();
                effects.spawnSparkBurst(getWidth() * 0.68f, getHeight() * 0.44f, 18, OUTFIT_COLORS[selectedOutfit]);
                showRunCallout("OUTFIT UNLOCKED", 1.2f);
                playSound("medal");
                logEvent("Unlocked outfit: " + OUTFIT_NAMES[selectedOutfit] + ".");
                return;
            }
            logEvent("Need " + Math.max(0, cost - trailTokens) + " more Trail Tokens for " + OUTFIT_NAMES[selectedOutfit] + ".");
        }
        selectedOutfit = (selectedOutfit + 1) % OUTFIT_COLORS.length;
        saveChoices();
        logEvent("Outfit preview: " + OUTFIT_NAMES[selectedOutfit] + ".");
    }

    private boolean isOutfitUnlocked(int index) {
        return RunRewardEconomy.isOutfitUnlocked(unlockedOutfitMask, index);
    }

    private int effectiveOutfitIndex() {
        return isOutfitUnlocked(selectedOutfit) ? selectedOutfit : firstUnlockedOutfit();
    }

    private int firstUnlockedOutfit() {
        for (int i = 0; i < OUTFIT_COLORS.length; i++) {
            if (isOutfitUnlocked(i)) {
                return i;
            }
        }
        unlockedOutfitMask = RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK;
        return 0;
    }

    private int unlockedOutfitCount() {
        int count = 0;
        for (int i = 0; i < OUTFIT_COLORS.length; i++) {
            if (isOutfitUnlocked(i)) {
                count++;
            }
        }
        return count;
    }

    private String outfitButtonLabel() {
        String name = OUTFIT_NAMES[selectedOutfit];
        if (isOutfitUnlocked(selectedOutfit)) {
            return "OUTFIT: " + name;
        }
        return "UNLOCK " + OUTFIT_TOKEN_COSTS[selectedOutfit] + " TOKENS";
    }

    private String outfitStatusLabel() {
        if (isOutfitUnlocked(selectedOutfit)) {
            return OUTFIT_NAMES[selectedOutfit] + " unlocked";
        }
        int missing = Math.max(0, OUTFIT_TOKEN_COSTS[selectedOutfit] - trailTokens);
        return OUTFIT_NAMES[selectedOutfit] + " locked, need " + missing;
    }

    private int currentDailyDayKey() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        int offset = calendar.getTimeZone().getOffset(now);
        return (int) ((now + offset) / 86_400_000L);
    }

    private int dailyStageIndex() {
        int playableStageCount = clampInt(unlockedStage + 1, 1, STAGES.length);
        return RunRewardEconomy.dailyStageIndex(currentDailyDayKey(), playableStageCount);
    }

    private int dailyGateGoal() {
        StageConfig stage = STAGES[dailyStageIndex()];
        return RunRewardEconomy.dailyGateGoal(currentDailyDayKey(), stage.goalGates);
    }

    private boolean dailyRushCompleteToday() {
        return dailyCompletedDay == currentDailyDayKey();
    }

    private String dailyRushLine() {
        StageConfig stage = STAGES[dailyStageIndex()];
        if (dailyRushCompleteToday()) {
            return "Complete · streak " + Math.max(1, dailyStreak);
        }
        int reward = RunRewardEconomy.dailyReward(dailyStreak);
        return stage.name + " · " + dailyGateGoal() + " gates · +" + reward;
    }

    private String dailyResultLine() {
        if (dailyTokensEarned > 0) {
            return "Daily +" + dailyTokensEarned + " · streak " + dailyStreak;
        }
        if (dailyRushCompleteToday()) {
            return "Daily complete · streak " + dailyStreak;
        }
        return "Daily open · " + STAGES[dailyStageIndex()].name;
    }

    private String badgeSummaryLine() {
        int earnedCount = TrailBadgeCatalog.badgeCount(runBadgesEarnedMask);
        int totalBadges = TrailBadgeCatalog.badgeCount(trailBadgeMask);
        if (earnedCount > 0) {
            return "Badge +" + earnedCount + " · " + firstRunBadgeName() + " · +" + runBadgeTokensEarned;
        }
        return "Passport " + totalBadges + "/" + TrailBadgeCatalog.BADGE_COUNT + " badges";
    }

    private String firstRunBadgeName() {
        for (int i = 0; i < TrailBadgeCatalog.BADGE_COUNT; i++) {
            if (TrailBadgeCatalog.hasBadge(runBadgesEarnedMask, i)) {
                return TrailBadgeCatalog.badgeName(i);
            }
        }
        return "Trail Badge";
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

    private String missionBriefLine() {
        return missionStarGoal + " STARS";
    }

    private String missionProgressLine() {
        return "MISSIONS " + missionsCompleted + "/3  GATES " + Math.min(gatesPassed, STAGES[selectedStage].goalGates) + "/" + STAGES[selectedStage].goalGates
                + "  STARS " + gameState.stars + "/" + missionStarGoal
                + "  COMBO " + gameState.bestCombo + "/" + missionComboGoal;
    }

    private int expeditionGradeScore(boolean stageCleared) {
        int score = 0;
        if (stageCleared) score += 2;
        if (routeMilestoneIndex >= 3) score += 2;
        if (campReached) score += 1;
        if (runFocusPickups > 0) score += 1;
        if (runWeatherFronts > 0) score += 1;
        if (missionsCompleted >= 3) score += 1;
        if (perfectRun && stageCleared) score += 1;
        return score;
    }

    private String expeditionGrade(boolean stageCleared) {
        int gradeScore = expeditionGradeScore(stageCleared);
        if (gradeScore >= 8) return "S";
        if (gradeScore >= 6) return "A";
        if (gradeScore >= 4) return "B";
        if (gradeScore >= 2) return "C";
        return "D";
    }

    private String expeditionLine(boolean stageCleared) {
        return "Expedition " + expeditionGrade(stageCleared)
                + " · Route " + Math.min(3, routeMilestoneIndex) + "/3"
                + " · Camp " + (campReached ? "yes" : "missed")
                + " · Focus " + runFocusPickups
                + " · Weather " + runWeatherFronts;
    }

    private String weatherFrontLabel() {
        if (weatherFront == WEATHER_AURORA) return "Aurora";
        if (weatherFront == WEATHER_RAIN) return "Rain";
        if (weatherFront == WEATHER_SNOW) return "Snow";
        return "Clear";
    }

    private String bossPatternLabel() {
        if (!bossActive) {
            return "";
        }
        if (bossState == BOSS_STATE_TELL) {
            if (bossPattern == BOSS_PATTERN_LUNGE) return "TELL: CHARGE";
            if (bossPattern == BOSS_PATTERN_SNOW_WAVE) return "TELL: ICE";
            return "TELL: SUMMON";
        }
        if (bossState == BOSS_STATE_ATTACK) {
            if (bossPattern == BOSS_PATTERN_LUNGE) return "CHARGE";
            if (bossPattern == BOSS_PATTERN_SNOW_WAVE) return "ICE";
            return "SUMMON";
        }
        if (bossState == BOSS_STATE_RECOVER) {
            return bossWeakWindowActive() ? "WEAK x2" : "RECOVER";
        }
        return "ENTER";
    }

    private String runRank() {
        if (score >= 850 || gameState.bestCombo >= 14) {
            return "S";
        }
        if (score >= 560 || gameState.bestCombo >= 10) {
            return "A";
        }
        if (score >= 320 || gameState.bestCombo >= 6) {
            return "B";
        }
        if (score >= 140 || gameState.bestCombo >= 3) {
            return "C";
        }
        return "D";
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
        return getHeight() - dp(PLAYFIELD_BOTTOM_MARGIN_DP);
    }

    private float playerStartX() {
        float preferred = getWidth() * PLAYER_START_X_FRACTION;
        float min = dp(78);
        float max = Math.max(min, getWidth() * 0.34f);
        return clamp(preferred, min, max);
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
        final float baseY;
        final float radius;
        final float baseSpeedMultiplier;
        float speedMultiplier;
        final float phase;
        final String label;
        final Drawable drawable;
        boolean passed = false;
        boolean nearMissAwarded = false;
        boolean roaring = false;
        boolean roarUsed = false;
        float roarTimer = 0f;
        float age = 0f;

        Hazard(float x, float y, float radius, float speedMultiplier, float phase, String label, Drawable drawable) {
            this.x = x;
            this.y = y;
            this.baseY = y;
            this.radius = radius;
            this.baseSpeedMultiplier = speedMultiplier;
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

    private static class BossAttack {
        float x;
        float y;
        float vx;
        float vy;
        float radius;
        final int type;
        final String label;
        float age = 0f;
        float spin = 0f;

        BossAttack(float x, float y, float radius, float vx, float vy, int type, String label) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.vx = vx;
            this.vy = vy;
            this.type = type;
            this.label = label;
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

    private static class PowerUp {
        float x;
        final float y;
        final float radius;
        final String type;
        float spin = 0f;
        float bob = 0f;

        PowerUp(float x, float y, float radius, String type) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.type = type;
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
