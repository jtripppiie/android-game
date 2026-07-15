package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MooseRushView extends View {
    /*
     * KID-FRIENDLY GAME DEV MAP
     *
     * This whole game lives inside one custom Android View. A View is like a
     * blank piece of paper Android gives us. Every frame we:
     *
     * 1. measure how much time passed since the last frame (dt)
     * 2. update the game world using that time
     * 3. draw the new world to the Canvas
     * 4. ask Android for another frame
     *
     * A beginner can learn a lot by following this path:
     * onDraw() -> updateGame() -> drawWorld() -> drawHud()
     *
     * Most objects in the game follow the same simple rule:
     * x means left/right, y means up/down, and each frame changes them a tiny
     * bit. If an object moves left, we subtract from x. If the runner jumps, we
     * subtract from y first, then gravity adds y back down toward the ground.
     */
    public interface PhotoRequestListener {
        void onPhotoRequested();

        void onPhotoResetRequested();
    }

    private static final String TAG = "YouRushGame";

    /*
     * Game screens are stored as numbers so the drawing code can quickly ask,
     * "What screen are we on?" STATE_RUNNING means the actual game is live.
     */
    private static final int STATE_SPLASH = 0;
    private static final int STATE_MENU = 1;
    private static final int STATE_MAP = 2;
    private static final int STATE_CUSTOMIZE = 3;
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;
    private static final int STATE_STAGE_CLEAR = 6;
    private static final int STATE_READY = 7;
    private static final int STATE_PAUSED = 8;
    private static final int STATE_PASSPORT = 9;

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
    private static final String PREF_BODY_STYLE = "body_style";
    private static final String PREF_TRAIL_TOKENS = "trail_tokens";
    private static final String PREF_UNLOCKED_OUTFITS = "unlocked_outfits";
    private static final String PREF_TOTAL_MISSIONS = "total_missions";
    private static final String PREF_TRAIL_BADGES = "trail_badges";
    private static final String PREF_DAILY_COMPLETED_DAY = "daily_completed_day";
    private static final String PREF_DAILY_STREAK = "daily_streak";
    private static final String PREF_EXPEDITION_LOGS = "expedition_logs";

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
    private static final String[] BODY_STYLE_NAMES = {
            "PHOTO RUNNER",
            "FEMALE RUNNER",
            "MALE RUNNER",
            "TRAIL RUNNER 2.0"
    };

    private static final String[] SEASONS = {
            "Summer",
            "Winter",
            "Midnight Sun",
            "Darkness"
    };

    /*
     * StageConfig is our level recipe. Each row answers:
     * - What is this stage called?
     * - Which season colors/background should it use?
     * - What hazard runs at the player?
     * - How many gates before the boss appears?
     * - How fast and how often should obstacles spawn?
     *
     * Changing these numbers is one of the safest ways to design new levels.
     */
    private static final StageConfig[] STAGES = {
            new StageConfig("Midnight Sun Run", "Vault driftwood logs before the eclipse.", SEASON_MIDNIGHT_SUN, "Midnight Sun", "WOLF", "DRIFTWOOD LOGS", 8, 18, 160, 2.22f, 0),
            new StageConfig("Salmon Rush", "Vault slick river logs while salmon arc in.", SEASON_SUMMER, "Salmon Boss", "SALMON", "RIVER LOGS", 10, 8, 165, 2.15f, 1),
            new StageConfig("Moose Pass", "Vault antler barricades and dodge real moose.", SEASON_SUMMER, "Moose Boss", "MOOSE", "ANTLER BARRICADES", 12, 10, 178, 2.05f, 2),
            new StageConfig("Dark Winter", "Leap jagged icebergs through low light.", SEASON_DARKNESS, "Eagle Boss", "EAGLE", "ICEBERGS", 14, 12, 188, 1.95f, 3),
            new StageConfig("Bear Country", "Survive snowbank barricades and winter wildlife.", SEASON_WINTER, "Polar Bear Boss", "BEAR", "SNOWBANKS", 16, 20, 198, 1.85f, 4)
    };
    private static final int SPRITE_SHEET_FRAMES = 6;
    private static final float PLAYER_START_X_FRACTION = 0.265f;
    private static final float PLAYER_HEAD_DRAW_OFFSET = 2.18f;
    private static final float PLAYER_SPRITE_VISUAL_SCALE = 1.20f;
    private static final float HAZARD_GATE_CLEARANCE_DP = 138f;
    private static final float PLAYFIELD_BOTTOM_MARGIN_DP = 52f;
    private static final float GROUND_LINE_HEIGHT_FRACTION = 0.82f;
    private static final float JUMP_CEILING_TOP_MARGIN_DP = 40f;
    private static final float AURORA_METER_MAX = 100f;
    private static final float AURORA_RUSH_SECONDS = 6.5f;
    private static final float AURORA_FOCUS_SECONDS = 5.0f;
    private static final int FLOW_STREAK_THRESHOLD = 3;
    private static final float FLOW_TIMER_SECONDS = 5.5f;
    private static final float FLOW_MAGNET_RADIUS_DP = 92f;

    /*
     * Expedition perks. Before each run the player drafts one perk that stays
     * active for that stage, turning every attempt into a small build choice.
     * Effects are read at a few well-defined points (score, fire rate, jump,
     * shield, spray) so the perk layer stays contained and easy to reason about.
     */
    private static final int PERK_NONE = -1;
    private static final int PERK_TRAILBLAZER = 0;
    private static final int PERK_AVALANCHE_ARM = 1;
    private static final int PERK_SPRING_STEP = 2;
    private static final int PERK_GLACIER_GUARD = 3;
    private static final int PERK_SPRAY_CANISTER = 4;
    private static final int PERK_COUNT = 5;
    private static final String[] PERK_NAMES = {
            "TRAILBLAZER", "AVALANCHE ARM", "SPRING STEP", "GLACIER GUARD", "SPRAY CANISTER"
    };
    private static final String[] PERK_DESCS = {
            "+25% score", "Faster snowballs", "Higher jump", "Start shielded", "+1 bear spray"
    };
    private static final float FLOW_MAGNET_SPEED_DP = 430f;
    private static final float CHASE_BEAR_SECONDS = 6.8f;
    private static final float CHASE_BEAR_SPEED_BOOST = 1.17f;
    private static final int CHASE_BEAR_ESCAPE_CLEAN_VAULTS = 2;
    private static final int BOSS_STATE_ENTER = 0;
    private static final int BOSS_STATE_TELL = 1;
    private static final int BOSS_STATE_ATTACK = 2;
    private static final int BOSS_STATE_RECOVER = 3;
    private static final int BOSS_PATTERN_LUNGE = 0;
    private static final int BOSS_PATTERN_SNOW_WAVE = 1;
    private static final int BOSS_PATTERN_SUMMON = 2;
    private static final int BOSS_PATTERN_LASER = 3;
    private static final int ATTACK_ICE = 0;
    private static final int ATTACK_SHOCKWAVE = 1;
    private static final int ATTACK_LASER = 2;
    private static final int ROAR_SPRITE_SOURCE_INSET_PX = 34;
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
    private EncounterDirector encounterDirector;
    private EncounterCard activeEncounter;
    private long runSeed;
    /*
     * These lists are the moving things currently alive in the level.
     * Spawning adds objects to a list. Updating moves them. Drawing shows them.
     * When an object scrolls off screen, we remove it so the game stays fast.
     */
    private final List<Gate> gates = new ArrayList<>();
    private final List<RoutePlatform> routePlatforms = new ArrayList<>();
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
    private final ObstacleRenderer obstacleRenderer;
    private final DebugOverlayRenderer debugOverlayRenderer;
    private final VisualEffects effects;
    private final WeatherSystem weather;
    private final Rect[] mooseFrameCrops;
    private final Rect[] bearFrameCrops;
    private final Rect[] polarBearFrameCrops;
    private final Rect[] wolfFrameCrops;
    private final Rect[] salmonFrameCrops;
    private final Rect[] eagleFrameCrops;

    private final RectF primaryButtonBounds = new RectF();
    private final RectF secondaryButtonBounds = new RectF();
    private final RectF thirdButtonBounds = new RectF();
    private final RectF godotButtonBounds = new RectF();
    private final RectF passportButtonBounds = new RectF();
    private final RectF photoButtonBounds = new RectF();
    private final RectF resetPhotoButtonBounds = new RectF();
    private final RectF backButtonBounds = new RectF();
    private final RectF seasonButtonBounds = new RectF();
    private final RectF outfitButtonBounds = new RectF();
    private final RectF bodyStyleButtonBounds = new RectF();
    private final RectF debugButtonBounds = new RectF();
    private final RectF muteButtonBounds = new RectF();
    private final RectF demoButtonBounds = new RectF();
    private final RectF dailyButtonBounds = new RectF();
    private final RectF dpadBounds = new RectF();
    private final RectF leftPadBounds = new RectF();
    private final RectF rightPadBounds = new RectF();
    private final RectF jumpPadBounds = new RectF();
    private final RectF firePadBounds = new RectF();
    private final RectF sprayPadBounds = new RectF();
    private final RectF aimUpPadBounds = new RectF();
    private final RectF aimDownPadBounds = new RectF();
    private final RectF pauseButtonBounds = new RectF();
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
    private int selectedBodyStyle = SpriteRenderer.BODY_STYLE_PHOTO;
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
    private int runTrailMaps = 0;
    private int runRescueKits = 0;
    private int runBearSprays = 0;
    private int runCleanVaults = 0;
    private int cleanVaultStreak = 0;
    private int bestCleanVaultStreak = 0;
    private int activePerk = PERK_NONE;
    private final int[] perkChoices = {PERK_NONE, PERK_NONE, PERK_NONE};
    private final RectF[] perkCardBounds = {new RectF(), new RectF(), new RectF()};
    private int runLogsBlasted = 0;
    private int routeMilestoneIndex = 0;
    private int expeditionLogs = 0;
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
    private int debugUnlockTaps = 0;
    private long debugUnlockLastTapMs = 0L;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean firePressed = false;
    private boolean sprayPressed = false;
    private boolean aimUpPressed = false;
    private boolean aimDownPressed = false;
    private boolean jumpWasPressed = false;
    private boolean fireWasPressed = false;
    private boolean grounded = false;
    private boolean missionHurdlesComplete = false;
    private boolean missionStarsComplete = false;
    private boolean missionComboComplete = false;
    private boolean perfectRun = true;
    private boolean runRewardsAwarded = false;
    private boolean dailyBonusAwarded = false;
    private boolean dailyRushMode = false;
    private boolean campReached = false;
    private boolean bossPhaseTwoAnnounced = false;
    private boolean bossPhaseThreeAnnounced = false;
    private boolean bossEnrageAnnounced = false;
    private boolean runNewBest = false;
    private boolean chaseBearActive = false;
    private boolean demoMode = false;
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
    private float nearMissFlash = 0f;
    private float stageClearTimer = 0f;
    private float readyTimer = 0f;
    private float runCalloutTimer = 0f;
    private float bossWarningTimer = 0f;
    private float eclipseTimer = 0f;
    private float demoTimer = 0f;
    private float coyoteTimer = 0f;
    private float jumpBufferTimer = 0f;
    private float freezeTimer = 0f;
    private float squashY = 1.0f;
    private float squashX = 1.0f;
    private float auroraMeter = 0f;
    private float auroraRushTimer = 0f;
    private float auroraFocusTimer = 0f;
    private float flowTimer = 0f;
    private float respawnGraceTimer = 0f;
    private float routeMilestoneTimer = 0f;
    private float scoutTimer = 0f;
    private float fireHoldTimer = 0f;
    private float aimPadY = 0f;
    private float bearSprayCooldown = 0f;
    private float bearSprayTimer = 0f;
    private float bearSprayOriginX = 0f;
    private float bearSprayOriginY = 0f;
    private float bearSprayDirection = 1f;
    private boolean bearSprayBossHit = false;
    private float chaseBearCooldown = 0f;
    private float chaseBearTimer = 0f;
    private float chaseBearX = 0f;
    private float chaseBearY = 0f;
    private float chaseBearRadius = 0f;
    private float chaseBearPhase = 0f;
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
    private int bearSprayCharges = 0;
    private int chaseBearStartCleanVaults = 0;
    private String runCallout = "";
    private String mapNotice = "";
    private float mapNoticeTimer = 0f;
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
        obstacleRenderer = new ObstacleRenderer(context, assets);
        debugOverlayRenderer = new DebugOverlayRenderer(context);
        effects = new VisualEffects(context);
        weather = new WeatherSystem(context.getResources().getDisplayMetrics().density);
        mooseFrameCrops = completeFrameCrops(assets.mooseWalkSheet());
        bearFrameCrops = completeFrameCrops(assets.bearWalkSheet());
        polarBearFrameCrops = completeFrameCrops(assets.polarBearWalkSheet());
        wolfFrameCrops = completeFrameCrops(assets.wolfRunSheet());
        salmonFrameCrops = completeFrameCrops(assets.salmonSwimSheet());
        eagleFrameCrops = completeFrameCrops(assets.eagleFlySheet());
        bestScore = prefs.getInt(PREF_BEST_SCORE, 0);
        selectedStage = clampInt(prefs.getInt(PREF_SELECTED_STAGE, 0), 0, STAGES.length - 1);
        selectedSeason = clampInt(prefs.getInt(PREF_SELECTED_SEASON, STAGES[selectedStage].season), 0, SEASONS.length - 1);
        unlockedStage = clampInt(prefs.getInt(PREF_UNLOCKED_STAGE, 0), 0, STAGES.length - 1);
        selectedOutfit = clampInt(prefs.getInt(PREF_OUTFIT, 0), 0, OUTFIT_COLORS.length - 1);
        selectedBodyStyle = clampInt(prefs.getInt(PREF_BODY_STYLE, SpriteRenderer.BODY_STYLE_PHOTO), 0, BODY_STYLE_NAMES.length - 1);
        trailTokens = prefs.getInt(PREF_TRAIL_TOKENS, 0);
        unlockedOutfitMask = prefs.getInt(PREF_UNLOCKED_OUTFITS, RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK);
        totalMissionsCompleted = prefs.getInt(PREF_TOTAL_MISSIONS, 0);
        trailBadgeMask = prefs.getInt(PREF_TRAIL_BADGES, 0);
        dailyCompletedDay = prefs.getInt(PREF_DAILY_COMPLETED_DAY, Integer.MIN_VALUE);
        dailyStreak = prefs.getInt(PREF_DAILY_STREAK, 0);
        expeditionLogs = prefs.getInt(PREF_EXPEDITION_LOGS, 0);
        if (!isOutfitUnlocked(selectedOutfit)) {
            selectedOutfit = firstUnlockedOutfit();
        }
        debugOverlay = false;
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
        if (state == STATE_RUNNING) {
            state = STATE_PAUSED;
            clearHeldControls();
            logEvent("Run auto-paused because the app lost focus.");
        }
        paused = true;
        logEvent("Pause.");
    }

    private void clearHeldControls() {
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        firePressed = false;
        sprayPressed = false;
        aimUpPressed = false;
        aimDownPressed = false;
        aimPadY = 0f;
        fireHoldTimer = 0f;
    }

    private void haptic(int feedbackConstant) {
        if (!demoMode) {
            performHapticFeedback(feedbackConstant);
        }
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
        weather.onSizeChanged(width, height);
        backdropCache = null;
        backdropCacheWidth = 0;
        backdropCacheHeight = 0;
        backdropCacheKey = Integer.MIN_VALUE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
         * dt means "delta time": the number of seconds since the last frame.
         *
         * We multiply movement by dt so the game speed feels the same on fast
         * and slow devices. Without dt, a phone drawing 120 frames per second
         * would move everything twice as fast as a phone drawing 60 frames.
         */
        long now = System.nanoTime();
        float dt = 0f;
        if (lastFrameNanos != 0L) {
            dt = Math.min((now - lastFrameNanos) / 1_000_000_000f, 0.033f);
        }
        lastFrameNanos = now;

        if (!paused) {
            /*
             * Update first, draw second. This is the classic game loop:
             * input/control -> physics/world changes -> render the result.
             */
            updateComputerDemo(dt);
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
            // Keep the pre-run briefing readable: the live runner, hazards, HUD,
            // and controls belong to gameplay and only create visual noise here.
            drawAlaskaBackdrop(canvas);
            drawReadyScreen(canvas);
        } else if (state == STATE_PAUSED) {
            drawWorld(canvas);
            drawHud(canvas);
            drawVirtualControls(canvas);
            drawPausePanel(canvas);
        } else {
            drawWorld(canvas);
            drawHud(canvas);
            drawVirtualControls(canvas);
            if (state == STATE_GAME_OVER) {
                drawGameOverPanel(canvas);
            } else if (state == STATE_STAGE_CLEAR) {
                drawStageClearPanel(canvas);
            } else if (state == STATE_PASSPORT) {
                drawPassportScreen(canvas);
            }
        }

        if (!paused) {
            /*
             * This asks Android to call onDraw() again on the next animation
             * frame. That one line is what keeps the game alive and moving.
             */
            postInvalidateOnAnimation();
        }
    }

    private void updateComputerDemo(float dt) {
        if (!demoMode) {
            return;
        }
        /*
         * Computer Run is an auto-player for showing the game to someone.
         * It should demonstrate stages without changing saved progress, so
         * scoring rewards, unlocks, and deaths are skipped elsewhere when
         * demoMode is true.
         */
        demoTimer += dt;
        if (state == STATE_READY && demoTimer > 0.85f) {
            state = STATE_RUNNING;
            readyTimer = 0f;
            demoTimer = 0f;
            lastFrameNanos = 0L;
            showRunCallout("COMPUTER RUN", 1.15f);
            logEvent("Computer Run auto-started.");
            return;
        }
        if (state == STATE_STAGE_CLEAR && stageClearTimer > 2.15f) {
            if (selectedStage < STAGES.length - 1) {
                selectedStage++;
            } else {
                selectedStage = 0;
            }
            selectedSeason = STAGES[selectedStage].season;
            backdropCacheKey = Integer.MIN_VALUE;
            if (!demoMode) {
                saveChoices();
            }
            demoTimer = 0f;
            logEvent("Computer Run advancing to " + STAGES[selectedStage].name + ".");
            startGame();
            return;
        }
        if (state == STATE_GAME_OVER && demoTimer > 1.25f) {
            demoTimer = 0f;
            logEvent("Computer Run retrying " + STAGES[selectedStage].name + ".");
            startGame();
            return;
        }
        if (state != STATE_RUNNING) {
            return;
        }

        /*
         * The demo "thinks" with simple rules instead of real AI:
         * stay near the normal runner lane, back away during some boss tells,
         * jump when danger is close, and fire when there is a target.
         */
        float targetX = playerStartX();
        if (bossActive) {
            targetX = getWidth() * 0.27f;
            if (bossPattern == BOSS_PATTERN_LUNGE && bossState == BOSS_STATE_TELL) {
                targetX = Math.max(dp(74), playerX - dp(90));
            }
        }
        leftPressed = playerX > targetX + dp(10);
        rightPressed = playerX < targetX - dp(10);

        boolean shouldJump = demoShouldJump();
        jumpPressed = shouldJump;
        if (shouldJump) {
            requestJump();
        }

        boolean shouldFire = bossActive || nearestBossAttackTarget(playerX, Float.MAX_VALUE) != null || nearestDestructibleGate(playerX, Float.MAX_VALUE) != null;
        firePressed = shouldFire;
        if (shouldFire) {
            fireSnowball();
        }
    }

    private boolean demoShouldJump() {
        /*
         * This method is the demo player's eyes. It scans each danger list and
         * returns true when something is close enough that a jump would help.
         */
        if (!grounded && jumpsUsed >= 2) {
            return false;
        }
        for (Gate gate : gates) {
            float distance = gate.x - playerX;
            if (distance > dp(34) && distance < dp(148)) {
                return true;
            }
        }
        for (Hazard hazard : hazards) {
            float distance = hazard.x - playerX;
            if (distance > dp(24) && distance < dp(135) && hazard.y > getGroundY() - dp(92)) {
                return true;
            }
        }
        for (BossAttack attack : bossAttacks) {
            if (attack.type == ATTACK_LASER) {
                laserAttackRect(attack, tempRect);
                if (tempRect.left < playerX + dp(72) && tempRect.right > playerX - dp(20)) {
                    return true;
                }
            } else if (attack.x > playerX && attack.x < playerX + dp(142) && attack.y > getGroundY() - dp(98)) {
                return true;
            }
        }
        return bossActive && bossPattern == BOSS_PATTERN_LASER && bossState == BOSS_STATE_TELL;
    }

    private void updatePassive(float dt) {
        spriteClock += dt * 2.5f;
        runnerClock += dt * 0.75f;
        updateVisualEffects(dt);
        weather.update(dt, selectedStage, selectedSeason, false, 0.2f);
        if (state == STATE_MAP) {
            mapNoticeTimer = Math.max(0f, mapNoticeTimer - dt);
        }
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

        if (state == STATE_PASSPORT && action == MotionEvent.ACTION_DOWN) {
            state = STATE_MAP;
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
            if (action == MotionEvent.ACTION_UP) {
                performClick();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            performClick();
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private boolean handleTap(float x, float y) {
        if (state == STATE_MENU) {
            if (y <= (isLandscape() ? dp(32) : dp(40))
                    && x >= getWidth() * 0.20f && x <= getWidth() * 0.80f) {
                long now = System.currentTimeMillis();
                debugUnlockTaps = now - debugUnlockLastTapMs <= 900L ? debugUnlockTaps + 1 : 1;
                debugUnlockLastTapMs = now;
                if (debugUnlockTaps >= 5) {
                    debugOverlay = !debugOverlay;
                    debugUnlockTaps = 0;
                    logEvent("Developer debug overlay " + (debugOverlay ? "enabled" : "disabled") + ".");
                }
            } else if (primaryButtonBounds.contains(x, y)) {
                demoMode = false;
                dailyRushMode = false;
                startGame();
            } else if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
                logEvent("Opened customize screen.");
            } else if (thirdButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                logEvent("Opened Alaska map.");
            } else if (dailyButtonBounds.contains(x, y)) {
                startDailyRush();
            } else if (demoButtonBounds.contains(x, y)) {
                startComputerDemo();
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
            if (passportButtonBounds.contains(x, y)) {
                state = STATE_PASSPORT;
                logEvent("Opened Trail Passport.");
                return true;
            }
            if (unlockedStage >= 4 && godotButtonBounds.contains(x, y)) {
                logEvent("Transitioning to Godot Global Overhaul...");
                prefs.edit().putBoolean("godot_transition_unlocked", true).apply();
                showRunCallout("REPLICATING TO GLOBAL ENGINE...", 5.0f);
                state = STATE_MENU;
                return true;
            }
            int tappedStage = findTappedStage(x, y);
            if (tappedStage >= 0) {
                if (tappedStage > unlockedStage) {
                    showMapNotice("Clear " + STAGES[tappedStage - 1].name + " to unlock.");
                    logEvent("Locked stage tapped: " + STAGES[tappedStage].name + ".");
                    return true;
                }
                selectedStage = tappedStage;
                selectedSeason = STAGES[selectedStage].season;
                demoMode = false;
                dailyRushMode = false;
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
                selectedBodyStyle = SpriteRenderer.BODY_STYLE_PHOTO;
                saveChoices();
                if (photoRequestListener != null) {
                    photoRequestListener.onPhotoResetRequested();
                }
                logEvent("Runner reset to default.");
            } else if (seasonButtonBounds.contains(x, y)) {
                selectedSeason = (selectedSeason + 1) % SEASONS.length;
                saveChoices();
                backdropCacheKey = Integer.MIN_VALUE;
                logEvent("Season set to " + SEASONS[selectedSeason] + ".");
            } else if (outfitButtonBounds.contains(x, y)) {
                handleOutfitTap();
            } else if (bodyStyleButtonBounds.contains(x, y)) {
                selectedBodyStyle = (selectedBodyStyle + 1) % BODY_STYLE_NAMES.length;
                saveChoices();
                logEvent("Runner body: " + BODY_STYLE_NAMES[selectedBodyStyle] + ".");
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
                dailyRushMode = false;
                startGame();
                return true;
            }
            state = STATE_MAP;
            return true;
        }

        if (state == STATE_READY) {
            int tappedPerk = tappedPerkCard(x, y);
            if (tappedPerk >= 0) {
                applyPerkSelection(perkChoices[tappedPerk]);
            }
            state = STATE_RUNNING;
            readyTimer = 0f;
            lastFrameNanos = 0L;
            showRunCallout(stageActionVerb(selectedStage) + " " + STAGES[selectedStage].obstacleName, 1.2f);
            logEvent("Run started after ready screen.");
            return true;
        }

        if (state == STATE_RUNNING) {
            if (pauseButtonBounds.contains(x, y)) {
                state = STATE_PAUSED;
                clearHeldControls();
                haptic(HapticFeedbackConstants.VIRTUAL_KEY);
                logEvent("Run paused.");
                return true;
            }
            if (!isControlTouch(x, y)) {
                requestJump();
            }
            return true;
        }

        if (state == STATE_PAUSED) {
            if (primaryButtonBounds.contains(x, y)) {
                state = STATE_RUNNING;
                lastFrameNanos = 0L;
                haptic(HapticFeedbackConstants.VIRTUAL_KEY);
                logEvent("Run resumed.");
                return true;
            }
            if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                logEvent("Paused run -> map.");
                return true;
            }
            if (thirdButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
                logEvent("Paused run -> customize.");
                return true;
            }
            // Ignore stray taps on the dimmed playfield. Resuming only from the
            // explicit button prevents accidental restarts while changing grip.
            return true;
        }

        return false;
    }

    private void showMapNotice(String message) {
        mapNotice = message;
        mapNoticeTimer = 2.2f;
        effects.spawnSparkBurst(getWidth() / 2f, dp(92), 12, Color.rgb(255, 218, 121));
    }

    private void updateHeldControls(MotionEvent event) {
        boolean wasJumpPressed = jumpPressed;
        boolean wasFirePressed = firePressed;
        boolean wasSprayPressed = sprayPressed;
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        firePressed = false;
        sprayPressed = false;
        aimUpPressed = false;
        aimDownPressed = false;
        aimPadY = 0f;

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
                if (dpadTouchContains(px, py)) {
                    updateDpadControl(px, py);
                } else if (leftPadBounds.contains(px, py)) {
                    leftPressed = true;
                } else if (rightPadBounds.contains(px, py)) {
                    rightPressed = true;
                } else if (jumpPadBounds.contains(px, py)) {
                    jumpPressed = true;
                } else if (firePadBounds.contains(px, py)) {
                    firePressed = true;
                } else if (sprayPadBounds.contains(px, py)) {
                    sprayPressed = true;
                } else if (aimUpPadBounds.contains(px, py)) {
                    aimUpPressed = true;
                } else if (aimDownPadBounds.contains(px, py)) {
                    aimDownPressed = true;
                }
            }
        }

        if (jumpPressed && !wasJumpPressed) {
            requestJump();
        }
        if (!jumpPressed && wasJumpPressed && playerVelocityY < -dp(180)) {
            playerVelocityY *= 0.56f;
            effects.spawnDustBurst(playerX, playerY + playerRadius * 0.55f, 4, Color.argb(110, 235, 245, 248));
        }
        if (firePressed && !wasFirePressed) {
            fireHoldTimer = 0f;
            fireSnowball();
        }
        if (sprayPressed && !wasSprayPressed) {
            useBearSpray();
        }
        if (!firePressed) {
            fireHoldTimer = 0f;
        }
        jumpWasPressed = jumpPressed;
        fireWasPressed = firePressed;
    }

    private void updateDpadControl(float x, float y) {
        float half = Math.max(1f, Math.min(dpadBounds.width(), dpadBounds.height()) * 0.5f);
        float dx = clamp((x - dpadBounds.centerX()) / half, -1f, 1f);
        float dy = clamp((y - dpadBounds.centerY()) / half, -1f, 1f);
        // Small dead zones so a light touch or drag registers immediately. Larger
        // zones made the pad feel laggy and dropped quick direction changes.
        if (dx < -0.12f) {
            leftPressed = true;
        } else if (dx > 0.12f) {
            rightPressed = true;
        }
        if (dy < -0.14f) {
            aimUpPressed = true;
            aimPadY = Math.min(aimPadY, dy);
        } else if (dy > 0.14f) {
            aimDownPressed = true;
            aimPadY = Math.max(aimPadY, dy);
        }
    }

    private boolean isControlTouch(float x, float y) {
        return leftPadBounds.contains(x, y)
                || dpadTouchContains(x, y)
                || rightPadBounds.contains(x, y)
                || jumpPadBounds.contains(x, y)
                || firePadBounds.contains(x, y)
                || sprayPadBounds.contains(x, y)
                || aimUpPadBounds.contains(x, y)
                || aimDownPadBounds.contains(x, y);
    }

    /**
     * D-pad hit test with a forgiving margin. A finger that drifts a little
     * outside the drawn pad while dragging still keeps steering, which stops the
     * runner from stalling mid-swipe. The pad sits alone in the bottom-left
     * corner, so the margin cannot overlap the action buttons on the right.
     */
    private boolean dpadTouchContains(float x, float y) {
        float margin = dp(20);
        return x >= dpadBounds.left - margin && x <= dpadBounds.right + margin
                && y >= dpadBounds.top - margin && y <= dpadBounds.bottom + margin;
    }

    private void startGame() {
        StageConfig stage = STAGES[selectedStage];
        // Custom seasons are a menu preview choice. Actual expeditions always
        // use their authored biome so icebergs cannot appear on summer grass.
        selectedSeason = stage.season;
        backdropCacheKey = Integer.MIN_VALUE;
        runSeed = System.currentTimeMillis() ^ ((long) selectedStage << 32);
        encounterDirector = new EncounterDirector(runSeed);
        activeEncounter = null;
        /*
         * Starting a run is mostly cleaning the table:
         * remove old obstacles, reset timers, reset score, and place the
         * runner back at the beginning. If a bug carries over between runs,
         * this is one of the first places to check.
         */
        gates.clear();
        routePlatforms.clear();
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
        spawnCooldown = selectedStage == 0 ? 1.35f : 1.15f;
        hazardCooldown = selectedStage == 0 ? 2.45f : 1.90f;
        shotCooldown = 0f;
        groundScroll = 0f;
        sceneryScroll = 0f;
        spriteClock = 0f;
        runnerClock = 0f;
        damageFlash = 0f;
        screenShake = 0f;
        worldFlash = 0f;
        respawnGraceTimer = 0f;
        stageClearTimer = 0f;
        runCalloutTimer = 0f;
        bossWarningTimer = 0f;
        auroraMeter = 0f;
        auroraRushTimer = 0f;
        auroraFocusTimer = 0f;
        flowTimer = 0f;
        scoutTimer = 0f;
        auroraRushes = 0;
        runTokensEarned = 0;
        runCacheTokens = 0;
        runExpeditionBonusTokens = 0;
        runFocusPickups = 0;
        runTrailMaps = 0;
        runRescueKits = 0;
        runBearSprays = 0;
        runCleanVaults = 0;
        cleanVaultStreak = 0;
        bestCleanVaultStreak = 0;
        activePerk = PERK_NONE;
        rollPerkChoices();
        runLogsBlasted = 0;
        routeMilestoneIndex = 0;
        livesLostThisRun = 0;
        dailyTokensEarned = 0;
        runBadgesEarnedMask = 0;
        runBadgeTokensEarned = 0;
        perfectRun = true;
        runRewardsAwarded = false;
        dailyBonusAwarded = false;
        campReached = false;
        bossPhaseTwoAnnounced = false;
        bossEnrageAnnounced = false;
        runNewBest = false;
        runCallout = "";
        routeMilestoneTimer = 0f;
        state = STATE_READY;
        readyTimer = 0f;
        playerX = playerStartX();
        playerY = getGroundY() - playerRadius;
        playerVelocityY = 0f;
        grounded = true;
        jumpsUsed = 0;
        coyoteTimer = RunnerTuning.COYOTE_SECONDS;
        jumpBufferTimer = 0f;
        bearSprayCharges = 1;
        fireHoldTimer = 0f;
        bearSprayCooldown = 0f;
        bearSprayTimer = 0f;
        bearSprayDirection = 1f;
        bearSprayBossHit = false;
        chaseBearActive = false;
        chaseBearCooldown = 5.5f + random.nextFloat() * 3.5f;
        chaseBearTimer = 0f;
        chaseBearX = 0f;
        chaseBearY = getGroundY() - gameplayDp(hazardRadiusDp("BEAR"));
        chaseBearRadius = gameplayDp(25f);
        chaseBearPhase = random.nextFloat() * 4f;
        chaseBearStartCleanVaults = 0;
        stageAttempts++;
        logEvent("Start stage: " + stage.name + ". Goal " + stage.goalGates + " " + stage.obstacleName + ", boss HP " + stage.bossHealth + ".");
    }

    private void setupRunMissions(StageConfig stage) {
        missionStarGoal = 2 + Math.min(3, selectedStage);
        missionComboGoal = 4 + selectedStage * 2;
        missionsCompleted = 0;
        missionHurdlesComplete = false;
        missionStarsComplete = false;
        missionComboComplete = false;
        logEvent("Missions: " + stageActionVerb(selectedStage).toLowerCase(Locale.ROOT) + " " + stage.goalGates + ", collect " + missionStarGoal + " stars, combo " + missionComboGoal + ".");
        logEvent("Encounter seed " + Long.toUnsignedString(runSeed, 36).toUpperCase(Locale.ROOT) + ".");
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
            playerVelocityY = -dp(RunnerTuning.GROUND_JUMP_VELOCITY_DP) * perkJumpMultiplier();
            grounded = false;
            coyoteTimer = 0f;
            jumpsUsed = 1;
            jumpBufferTimer = 0f;
            effects.spawnDustBurst(playerX, getGroundY(), 7, Color.argb(180, 235, 245, 248));
            screenShake = Math.max(screenShake, 0.04f);
            haptic(HapticFeedbackConstants.KEYBOARD_TAP);
            playSound("jump");
        } else if (jumpsUsed < 2) {
            playerVelocityY = -dp(RunnerTuning.DOUBLE_JUMP_VELOCITY_DP) * perkJumpMultiplier();
            jumpsUsed++;
            jumpBufferTimer = 0f;
            effects.spawnSparkBurst(playerX, playerY + playerRadius * 0.2f, 6, Color.rgb(255, 218, 121));
            haptic(HapticFeedbackConstants.KEYBOARD_TAP);
            playSound("double-jump");
        }
    }

    private void fireSnowball() {
        if (shotCooldown > 0f || state != STATE_RUNNING) {
            return;
        }
        float startX = runnerHandX();
        float startY = runnerHandY();
        float targetY = aimedThrowTargetY(startX, startY);
        float verticalArc = clamp((targetY - startY) * 1.25f, -dp(210), dp(190));
        boolean empowered = auroraFocusTimer > 0f || flowActive() || bossWeakWindowActive();
        shots.add(new Shot(startX, startY, dp(empowered ? 540 : 500), verticalArc, gameplayDp(empowered ? 7.2f : 5.8f), empowered));
        effects.spawnSparkBurst(startX + dp(8), startY, empowered ? 8 : 5, empowered ? Color.rgb(255, 218, 121) : Color.WHITE);
        shotCooldown = auroraFocusTimer > 0f ? 0.18f : flowActive() ? 0.21f : 0.30f;
        if (hasPerk(PERK_AVALANCHE_ARM)) {
            shotCooldown *= 0.66f;
        }
        playSound("throw");
        logEvent(empowered ? "Powered snowball fired." : "Snowball fired.");
    }

    private float aimedThrowTargetY(float startX, float startY) {
        if (Math.abs(aimPadY) > 0.12f) {
            return startY + dp(128) * aimPadY;
        }
        if (aimUpPressed && !aimDownPressed) {
            return startY - dp(128);
        }
        if (aimDownPressed && !aimUpPressed) {
            return startY + dp(84);
        }
        return throwTargetY(startX, startY);
    }

    private void updateBearSpray(float dt) {
        bearSprayCooldown = Math.max(0f, bearSprayCooldown - dt);
        bearSprayTimer = Math.max(0f, bearSprayTimer - dt);
        // The visible plume lasts several frames, so targets entering it during
        // that window must be affected instead of passing through harmlessly.
        if (state == STATE_RUNNING && bearSprayTimer > 0f) {
            sprayStunWildlife();
            sprayInterruptBoss();
        }
    }

    private void useBearSpray() {
        if (state != STATE_RUNNING || bearSprayCharges <= 0 || bearSprayCooldown > 0f) {
            return;
        }
        boolean rearSpray = chaseBearActive && chaseBearX < playerX && playerX - chaseBearX < dp(310);
        bearSprayCharges--;
        runBearSprays++;
        bearSprayCooldown = SprayTuning.COOLDOWN_SECONDS;
        bearSprayTimer = SprayTuning.CONE_SECONDS;
        bearSprayDirection = rearSpray ? -1f : 1f;
        bearSprayBossHit = false;
        bearSprayOriginX = rearSpray ? runnerBackX() : runnerHandX();
        bearSprayOriginY = runnerHandY();

        boolean chaseInterrupted = sprayInterruptChaseBear(rearSpray);
        int stunned = sprayStunWildlife();
        boolean bossInterrupted = sprayInterruptBoss();
        effects.spawnSparkBurst(bearSprayOriginX + bearSprayDirection * dp(42), bearSprayOriginY, 18, Color.rgb(255, 166, 84));
        effects.spawnDustBurst(bearSprayOriginX + bearSprayDirection * dp(64), bearSprayOriginY + dp(18), 8, Color.argb(150, 255, 218, 121));
        screenShake = Math.max(screenShake, bossInterrupted || chaseInterrupted ? 0.11f : 0.06f);
        playSound("hit");
        if (chaseInterrupted) {
            showRunCallout("CHASE BROKEN", 1.0f);
            logEvent("Rear bear spray broke the chase.");
        } else if (bossInterrupted) {
            showRunCallout("SPRAY INTERRUPT", 0.95f);
            logEvent("Bear spray interrupted boss.");
        } else if (stunned > 0) {
            showRunCallout("BEAR SPRAY STUN", 0.85f);
            logEvent("Bear spray stunned wildlife x" + stunned + ".");
        } else {
            showRunCallout("BEAR SPRAY", 0.80f);
            logEvent("Bear spray used.");
        }
    }

    private int sprayStunWildlife() {
        int stunned = 0;
        Iterator<Hazard> iterator = hazards.iterator();
        while (iterator.hasNext()) {
            Hazard hazard = iterator.next();
            if (!sprayHitsPoint(hazard.x, CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring), hazard.radius)) {
                continue;
            }
            iterator.remove();
            stunned++;
            gameState.addCombo();
            int awarded = addScore(14, "Bear spray stun");
            effects.spawnScorePopup("SPRAY +" + awarded, hazard.x, hazard.y - hazard.radius * 1.3f, Color.rgb(255, 166, 84));
            effects.spawnSparkBurst(hazard.x, hazard.y, 13, Color.rgb(255, 218, 121));
            addAuroraMeter(11f, "Bear spray");
        }
        if (stunned > 0) {
            checkMissionProgress();
        }
        return stunned;
    }

    private boolean sprayInterruptBoss() {
        if (bearSprayBossHit || !bossActive || !sprayHitsPoint(bossX, bossHurtCenterY(), bossContactRadius())) {
            return false;
        }
        bearSprayBossHit = true;
        bossStunTimer = Math.max(bossStunTimer, selectedStage == 4 ? 0.62f : 0.42f);
        damageFlash = Math.max(damageFlash, 0.18f);
        worldFlash = Math.max(worldFlash, 0.10f);
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            enterBossRecover();
        } else if (bossState == BOSS_STATE_TELL) {
            bossStateTimer = Math.max(0f, bossStateTimer - bossTellDuration() * 0.34f);
        }
        int awarded = addScore(18, "Bear spray interrupt");
        effects.spawnScorePopup("SPRAY +" + awarded, bossX, bossY - bossRadius(), Color.rgb(255, 166, 84));
        addAuroraMeter(10f, "Boss sprayed");
        return true;
    }

    private boolean sprayInterruptChaseBear(boolean rearSpray) {
        if (!rearSpray || !chaseBearActive) {
            return false;
        }
        float distance = playerX - chaseBearX;
        if (distance < -chaseBearRadius || distance > dp(325)) {
            return false;
        }
        int awarded = addScore(22, "Chase bear sprayed");
        effects.spawnScorePopup("CHASE +" + awarded, chaseBearX, chaseBearY - chaseBearRadius * 2.0f, Color.rgb(255, 166, 84));
        effects.spawnSparkBurst(chaseBearX, chaseBearY, 18, Color.rgb(255, 218, 121));
        addAuroraMeter(16f, "Chase broken");
        endChaseBear(true);
        return true;
    }

    private boolean sprayHitsPoint(float x, float y, float radius) {
        float testX = bearSprayDirection < 0f ? bearSprayOriginX + (bearSprayOriginX - x) : x;
        return SprayTuning.coneHitsPoint(bearSprayOriginX, bearSprayOriginY, testX, y, radius, getResources().getDisplayMetrics().density);
    }

    private float runnerHandX() {
        return playerX + playerVisualRadius() * 0.54f;
    }

    private float runnerBackX() {
        return playerX - playerVisualRadius() * 0.54f;
    }

    private float runnerHandY() {
        // Chest / throwing-hand height on the DRAWN sprite. The collision center
        // (playerY) sits near the runner's feet, so deriving the hand from the
        // draw geometry stops snowballs and spray from launching at the ankles.
        return playerHeadDrawY() + playerVisualRadius() * 1.02f;
    }

    private float playerVisualRadius() {
        return playerRadius * PLAYER_SPRITE_VISUAL_SCALE;
    }

    private float playerHeadDrawY() {
        float feetLine = (playerY - playerRadius * PLAYER_HEAD_DRAW_OFFSET)
                + SpriteRenderer.runnerFeetDropFromHead(playerRadius, true);
        return feetLine - SpriteRenderer.runnerFeetDropFromHead(playerVisualRadius(), true);
    }

    private float throwTargetY(float startX, float startY) {
        float bestX = Float.MAX_VALUE;
        float targetY = startY - dp(6);
        if (bossActive && bossX > startX) {
            bestX = bossX;
            targetY = bossHurtCenterY();
        }
        BossAttack attack = nearestBossAttackTarget(startX, bestX);
        if (attack != null) {
            bestX = attack.x;
            targetY = attack.y;
        }
        Gate gate = nearestDestructibleGate(startX, bestX);
        if (gate != null) {
            bestX = gate.x;
            targetY = riverLogTop(gate) + riverLogHeight(gate) * 0.50f;
        }
        for (Hazard hazard : hazards) {
            if (hazard.x <= startX || hazard.x >= bestX) {
                continue;
            }
            bestX = hazard.x;
            targetY = CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring);
        }
        return clamp(targetY, dp(74), getGroundY() - dp(18));
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (freezeTimer > 0f) {
            freezeTimer -= dt;
            return;
        }

        StageConfig stage = STAGES[selectedStage];
        /*
         * Difficulty is a curve, not a switch. As the runner passes more gates,
         * tension rises. Tension then makes scrolling and spawning feel faster.
         * This keeps early seconds readable and later seconds exciting.
         */
        float tension = DifficultyCurve.tension(selectedStage, gatesPassed, stage.goalGates);
        float gateSpeed = dp(RunnerTuning.scrollSpeedDp(stage.baseSpeed, gatesPassed)) * DifficultyCurve.speedMultiplier(tension);
        gateSpeed *= worldSpeedMultiplier();
        float gravity = selectedSeason == SEASON_DARKNESS ? dp(RunnerTuning.DARKNESS_GRAVITY_DP) : dp(RunnerTuning.GRAVITY_DP);
        if (auroraFocusTimer > 0f) {
            gravity *= 0.88f;
        }
        float horizontalSpeed = dp(300) * RushDirector.horizontalSpeedMultiplier(flowActive());
        boolean wasGrounded = grounded;

        spriteClock += dt * (5.5f + Math.min(4.5f, gatesPassed * 0.28f));
        runnerClock += dt * (grounded ? 1.52f + Math.min(0.38f, gatesPassed * 0.020f) : 0.68f);
        updateVisualEffects(dt);
        weather.update(dt, selectedStage, selectedSeason, flowActive(), gateSpeed / dp(STAGES[selectedStage].baseSpeed));
        shotCooldown = Math.max(0f, shotCooldown - dt);
        updateHeldFire(dt);
        updateBearSpray(dt);
        damageFlash = Math.max(0f, damageFlash - dt);
        updateAuroraRush(dt);
        updateAuroraFocus(dt);
        updateFlowTimer(dt);
        updateChaseBear(dt, gateSpeed);
        respawnGraceTimer = Math.max(0f, respawnGraceTimer - dt);
        jumpBufferTimer = Math.max(0f, jumpBufferTimer - dt);
        /*
         * Coyote time and jump buffering make the controls feel fair:
         * - coyote time lets you jump a split-second after leaving the ground
         * - jump buffering remembers a jump pressed just before landing
         *
         * These tiny forgiveness windows are common in polished platformers.
         */
        coyoteTimer = grounded ? RunnerTuning.COYOTE_SECONDS : Math.max(0f, coyoteTimer - dt);
        tryConsumeJumpBuffer();

        /*
         * Horizontal movement is direct: pressing left/right changes x.
         * clamp() keeps the runner inside the screen instead of wandering away.
         */
        if (leftPressed) {
            playerX -= horizontalSpeed * dt;
        }
        if (rightPressed) {
            playerX += horizontalSpeed * dt;
        }
        playerX = clamp(playerX, playerRadius + dp(6), getWidth() - playerRadius - dp(6));

        /*
         * Vertical movement is physics-style:
         * velocity changes because of gravity, then position changes because
         * of velocity. Negative velocity moves up; positive velocity falls down.
         */
        float previousPlayerY = playerY;
        playerVelocityY += gravity * dt;
        playerY += playerVelocityY * dt;

        // Squash and stretch: stretch when falling/jumping fast, squash on impact.
        float stretchFactor = Math.abs(playerVelocityY) * 0.00035f;
        squashY = 1.0f + (playerVelocityY < 0 ? stretchFactor : -stretchFactor * 0.5f);
        squashX = 1.0f / squashY;
        // Ease back to normal
        squashY = 1.0f + (squashY - 1.0f) * 0.8f;
        squashX = 1.0f + (squashX - 1.0f) * 0.8f;

        groundScroll = (groundScroll + gateSpeed * dt) % dp(48);
        sceneryScroll = (sceneryScroll + gateSpeed * dt) % dp(3600);
        updateRoutePlatforms(dt, bossActive ? 0f : gateSpeed);

        float restY = getGroundY() - playerRadius;
        RoutePlatform landedPlatform = landingRoutePlatform(previousPlayerY, playerY);
        if (landedPlatform != null) {
            restY = landedPlatform.y - playerRadius;
        }
        if (playerY >= restY) {
            /*
             * Landing means the runner touched the ground line. We snap exactly
             * to the ground so tiny math errors do not make the runner sink.
             */
            playerY = restY;
            playerVelocityY = 0f;
            grounded = true;
            jumpsUsed = 0;
            coyoteTimer = RunnerTuning.COYOTE_SECONDS;
            if (!wasGrounded) {
                effects.spawnDustBurst(playerX, restY + playerRadius, 9, Color.argb(185, 235, 245, 248));
                screenShake = Math.max(screenShake, 0.035f);
                squashY = 0.65f; // Hard squash on landing
                squashX = 1.4f;
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
            /*
             * The running section ends after enough gates. Then we stop spawning
             * normal obstacles and bring in the boss phase.
             */
            startBossPhase();
        }

        if (bossActive) {
            updateBoss(dt);
            updateHazards(dt, gateSpeed * 1.15f, false);
        } else {
            updateGates(dt, gateSpeed);
            updateHazards(dt, gateSpeed * 1.15f, true);
        }

        /*
         * Collision checks are grouped after movement so we test the latest
         * positions. Computer Run skips this block because it is a dry run: it
         * should show the full game without losing lives or changing saves.
         */
        if (!demoMode && respawnGraceTimer <= 0f) {
            if (bossActive
                    && bossStunTimer <= 0f
                    && circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.PLAYER_BOSS_CONTACT_RADIUS_SCALE, bossX, bossHurtCenterY(), bossContactRadius())) {
                endGame(STAGES[selectedStage].bossName + " got you.");
                return;
            }

            for (BossAttack attack : bossAttacks) {
                if (bossAttackHitsPlayer(attack)) {
                    endGame(attack.label + " hit you.");
                    return;
                }
            }

            for (Gate gate : gates) {
                if (hitsGate(gate)) {
                    endGame(STAGES[selectedStage].obstacleName + " bonk.");
                    return;
                }
            }

            for (Hazard hazard : hazards) {
                if ("THIN ICE".equals(hazard.label)) {
                    tempRect.set(hazard.x - hazard.radius * 1.35f, getGroundY() - dp(18), hazard.x + hazard.radius * 1.35f, getGroundY() + dp(4));
                    if (circleHitsRect(playerX, playerY, playerRadius * CollisionTuning.PLAYER_THIN_ICE_RADIUS_SCALE, tempRect)) {
                        endGame("Thin ice cracked.");
                        return;
                    }
                    continue;
                }
                float hazardHitRadius = hazard.radius * CollisionTuning.hazardRadiusScale(hazard.roaring);
                float hazardHitY = CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring);
                if (circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE, hazard.x, hazardHitY, hazardHitRadius)) {
                    endGame(hazard.label + " got you.");
                    return;
                }
            }
            if (chaseBearActive && chaseBearHitsPlayer()) {
                endGame("Chase bear caught you.");
                return;
            }
        }
    }

    private void updateGates(float dt, float gateSpeed) {
        /*
         * A spawn cooldown is a countdown timer. When it reaches zero, we create
         * a new obstacle and reset the countdown for the next one.
         */
        spawnCooldown -= dt;
        if (spawnCooldown <= 0f) {
            spawnGate();
            float tension = DifficultyCurve.tension(selectedStage, gatesPassed, STAGES[selectedStage].goalGates);
            spawnCooldown = DifficultyCurve.gateCooldown(RunnerTuning.nextGateCooldown(STAGES[selectedStage].spawnSeconds, gatesPassed), tension)
                    * RushDirector.gateCooldownMultiplier(gatesPassed, flowActive());
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
                int awarded = addScore(10, stageActionVerb(selectedStage) + " cleared");
                effects.spawnScorePopup("+" + awarded, gate.x + gate.width / 2f, getGroundY() - gate.height - dp(20), Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(gate.x + gate.width / 2f, getGroundY() - gate.height, 8, Color.rgb(255, 218, 121));
                addAuroraMeter(8f, "Route rhythm");
                if (!maybeAwardCleanVault(gate)) {
                    cleanVaultStreak = 0;
                }
                showComboCallout();
                if (!bossActive) {
                    effects.spawnScorePopup(RushDirector.beatLabel(gatesPassed), getWidth() * 0.58f,
                            dp(92), RushDirector.beatFor(gatesPassed) == RushDirector.BEAT_WILDLIFE
                                    ? Color.rgb(255, 98, 84) : Color.rgb(255, 218, 121));
                }
                logEvent(stageActionVerb(selectedStage) + " " + STAGES[selectedStage].obstacleName + " " + gatesPassed + "/" + STAGES[selectedStage].goalGates + ".");
            }

            if (gate.x + gate.width < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void updateRoutePlatforms(float dt, float speed) {
        Iterator<RoutePlatform> iterator = routePlatforms.iterator();
        while (iterator.hasNext()) {
            RoutePlatform platform = iterator.next();
            platform.age += dt;
            platform.x -= speed * dt;
            platform.y = platform.baseY + (platform.moving
                    ? (float) Math.sin(platform.age * 2.15f + platform.phase) * dp(20) : 0f);
            if (platform.x + platform.width < -dp(40)) iterator.remove();
        }
    }

    private RoutePlatform landingRoutePlatform(float oldY, float newY) {
        if (playerVelocityY < 0f) return null;
        float oldBottom = oldY + playerRadius * 0.82f;
        float newBottom = newY + playerRadius * 0.82f;
        RoutePlatform best = null;
        for (RoutePlatform platform : routePlatforms) {
            if (platform.broken || playerX + playerRadius * 0.48f < platform.x
                    || playerX - playerRadius * 0.48f > platform.x + platform.width) continue;
            if (oldBottom <= platform.y + dp(5) && newBottom >= platform.y - dp(2)
                    && (best == null || platform.y < best.y)) {
                best = platform;
            }
        }
        return best;
    }

    private boolean maybeAwardCleanVault(Gate gate) {
        float gateTop = gateColliderTop(gate);
        float playerBottom = playerY + playerRadius * 0.78f;
        float clearance = gateTop - playerBottom;
        if (clearance < -dp(8) || clearance > dp(34) || playerVelocityY > dp(420)) {
            return false;
        }
        cleanVaultStreak++;
        bestCleanVaultStreak = Math.max(bestCleanVaultStreak, cleanVaultStreak);
        int awarded = addScore(8 + selectedStage, "Clean vault");
        runCleanVaults++;
        addAuroraMeter(7f, "Clean vault");
        String label = cleanVaultStreak >= FLOW_STREAK_THRESHOLD ? "FLOW x" + cleanVaultStreak : "CLEAN VAULT";
        effects.spawnScorePopup(label + " +" + awarded, gate.x + gate.width / 2f, gateTop - dp(28), Color.rgb(77, 219, 184));
        effects.spawnSparkBurst(gate.x + gate.width / 2f, gateTop, 8, Color.rgb(77, 219, 184));
        haptic(HapticFeedbackConstants.CLOCK_TICK);
        if (cleanVaultStreak >= FLOW_STREAK_THRESHOLD) {
            activateFlow(gate.x + gate.width / 2f, gateTop);
        }
        if (chaseBearActive && runCleanVaults - chaseBearStartCleanVaults >= CHASE_BEAR_ESCAPE_CLEAN_VAULTS) {
            int chaseAward = addScore(18, "Chase escaped");
            effects.spawnScorePopup("ESCAPE +" + chaseAward, playerX, playerY - playerRadius * 2.1f, Color.rgb(77, 219, 184));
            endChaseBear(true);
        }
        return true;
    }

    private void updateChaseBear(float dt, float gateSpeed) {
        if (bossActive || bossDefeated || state != STATE_RUNNING) {
            if (chaseBearActive) {
                endChaseBear(false);
            }
            return;
        }

        if (!chaseBearActive) {
            if (gatesPassed < 2 || STAGES[selectedStage].goalGates - gatesPassed <= 1) {
                return;
            }
            chaseBearCooldown -= dt;
            if (chaseBearCooldown <= 0f && random.nextFloat() < chaseBearSpawnChance()) {
                startChaseBear();
            } else if (chaseBearCooldown <= -1.25f) {
                chaseBearCooldown = 2.5f + random.nextFloat() * 3.0f;
            }
            return;
        }

        chaseBearTimer = Math.max(0f, chaseBearTimer - dt);
        chaseBearPhase += dt;
        chaseBearY = getGroundY() - chaseBearRadius + (float) Math.sin(chaseBearPhase * 5.6f) * dp(0.6f);

        float escapeProgress = 1f - chaseBearTimer / CHASE_BEAR_SECONDS;
        float desiredX = playerX - dp(82 + 164 * escapeProgress);
        float chaseSpeed = Math.max(dp(54), (gateSpeed * 0.24f + dp(72 + selectedStage * 4)) * (1f - 0.56f * escapeProgress));
        if (chaseBearX < desiredX) {
            chaseBearX = Math.min(desiredX, chaseBearX + chaseSpeed * dt);
        } else {
            chaseBearX = Math.max(desiredX, chaseBearX - dp(44 + 70 * escapeProgress) * dt);
        }

        if (random.nextFloat() < 0.34f + 0.28f * escapeProgress) {
            effects.spawnParticle(chaseBearX - chaseBearRadius * (0.6f + escapeProgress), getGroundY(), -dp(42 + random.nextFloat() * (42 + 58 * escapeProgress)), -dp(10 + random.nextFloat() * 18), dp(2.2f + 1.2f * escapeProgress), Color.argb(Math.round(150 - 42 * escapeProgress), 170, 130, 90), 0.22f + 0.12f * escapeProgress);
        }
        if (chaseBearTimer <= 0f) {
            int awarded = addScore(14, "Chase survived");
            effects.spawnScorePopup("OUTRUN +" + awarded, playerX, playerY - playerRadius * 1.9f, Color.rgb(255, 218, 121));
            endChaseBear(true);
        }
    }

    private float chaseBearSpawnChance() {
        if (selectedStage == 4) return 0.92f;
        if (selectedStage >= 2) return 0.55f;
        return 0.38f;
    }

    private void startChaseBear() {
        chaseBearActive = true;
        chaseBearTimer = CHASE_BEAR_SECONDS;
        chaseBearRadius = gameplayDp(selectedStage == 4 ? 27f : 25f);
        chaseBearX = -chaseBearRadius * 2.25f;
        chaseBearY = getGroundY() - chaseBearRadius;
        chaseBearPhase = random.nextFloat() * 4f;
        chaseBearStartCleanVaults = runCleanVaults;
        chaseBearCooldown = 18.0f + random.nextFloat() * 9.0f;
        screenShake = Math.max(screenShake, 0.09f);
        effects.spawnDustBurst(dp(24), getGroundY(), 12, Color.argb(170, 170, 130, 90));
        showRunCallout("BEAR CHASE: RUN FASTER", 1.35f);
        logEvent("Left bear chase started.");
    }

    private void endChaseBear(boolean escaped) {
        if (!chaseBearActive) {
            return;
        }
        chaseBearActive = false;
        chaseBearTimer = 0f;
        chaseBearCooldown = escaped ? 18.0f + random.nextFloat() * 9.0f : 8.0f + random.nextFloat() * 5.0f;
        if (escaped) {
            showRunCallout("CHASE ESCAPED", 1.0f);
            logEvent("Left bear chase escaped.");
        }
    }

    private boolean chaseBearHitsPlayer() {
        float hitY = chaseBearY - chaseBearRadius * 0.12f;
        return circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE,
                chaseBearX, hitY, chaseBearRadius * 0.78f);
    }

    private void updateHazards(float dt, float speed, boolean allowSpawns) {
        /*
         * Hazards are the moving stage enemies. They wait until the player has
         * passed a couple gates so the run gets a short warm-up first.
         */
        if (allowSpawns && gatesPassed < 1) {
            return;
        }
        if (allowSpawns) {
            hazardCooldown -= dt;
            if (hazardCooldown <= 0f) {
                spawnHazardWave();
                float tension = DifficultyCurve.tension(selectedStage, gatesPassed, STAGES[selectedStage].goalGates);
                hazardCooldown = DifficultyCurve.hazardCooldown(RunnerTuning.nextHazardCooldown(selectedStage, gatesPassed), tension)
                        * RushDirector.hazardCooldownMultiplier(gatesPassed, flowActive());
            }
        }

        Iterator<Hazard> iterator = hazards.iterator();
        while (iterator.hasNext()) {
            Hazard hazard = iterator.next();
            hazard.age += dt;
            updateHazardRoar(hazard, dt);
            updateHazardIntent(hazard, dt);

            if ("ICE SPIKE".equals(hazard.label)) {
                hazard.y += dp(320) * dt;
                hazard.x -= speed * 0.2f * dt;
                if (hazard.y > getGroundY() + dp(20)) {
                    iterator.remove();
                    continue;
                }
            } else {
                hazard.x -= speed * hazard.speedMultiplier * dt;
            }
            resolveCommittedWildlife(hazard);

            if (!hazard.roaring && isDivingHazard(hazard)) {
                hazard.y += (hazard.targetY - hazard.y) * Math.min(1f, dt * 5.8f);
            } else if (!hazard.roaring) {
                /*
                 * baseY is the normal lane position. Bobbing and pouncing add a
                 * small animation offset without permanently moving the lane.
                 */
                hazard.y = hazard.baseY + hazardBobOffset(hazard) + wolfPounceOffset(hazard);
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
        /*
         * Bobbing is just a sine wave. Sine smoothly goes up and down between
         * -1 and 1, which makes sprites feel alive instead of frozen.
         */
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

    private void updateHazardIntent(Hazard hazard, float dt) {
        boolean eagle = "EAGLE".equals(hazard.label) || "DARK".equals(hazard.label);
        boolean charger = "BEAR".equals(hazard.label) || "POLAR".equals(hazard.label)
                || "MOOSE".equals(hazard.label);
        if (!eagle && !charger) return;

        if (hazard.behaviorState == 0 && hazard.x < getWidth() * 0.88f) {
            hazard.behaviorState = 1;
            hazard.intentTimer = eagle ? 0.62f : 0.54f;
            hazard.targetY = eagle
                    ? clamp(playerY, getGroundY() - dp(150), getGroundY() - dp(34)) : hazard.baseY;
            hazard.speedMultiplier = hazard.baseSpeedMultiplier * (eagle ? 0.48f : 0.34f);
        } else if (hazard.behaviorState == 1) {
            hazard.intentTimer -= dt;
            if (hazard.intentTimer <= 0f) {
                hazard.behaviorState = 2;
                hazard.committed = true;
                hazard.speedMultiplier = hazard.baseSpeedMultiplier * (eagle ? 1.42f : 1.72f);
                effects.spawnScorePopup(eagle ? "DIVE" : "CHARGE", hazard.x,
                        hazard.y - hazard.radius * 1.5f, Color.rgb(255, 98, 84));
            }
        } else if (hazard.behaviorState == 2 && hazard.x < playerX - dp(80)) {
            hazard.behaviorState = 3;
            hazard.committed = false;
            hazard.speedMultiplier = hazard.baseSpeedMultiplier * 0.72f;
        }
    }

    private boolean isDivingHazard(Hazard hazard) {
        return hazard.behaviorState >= 1
                && ("EAGLE".equals(hazard.label) || "DARK".equals(hazard.label));
    }

    private void resolveCommittedWildlife(Hazard hazard) {
        if (!hazard.committed || hazard.environmentHit
                || !("BEAR".equals(hazard.label) || "POLAR".equals(hazard.label)
                || "MOOSE".equals(hazard.label))) return;

        for (Gate gate : new ArrayList<>(gates)) {
            if (hazard.x + hazard.radius < gate.x || hazard.x - hazard.radius > gate.x + gate.width) continue;
            hazard.environmentHit = true;
            gates.remove(gate);
            if (!gate.passed) {
                gate.passed = true;
                gatesPassed++;
                gameState.gatesPassed = gatesPassed;
            }
            gameState.addCombo();
            int awarded = addScore(32, "Wildlife route break");
            effects.spawnScorePopup("BAITED BREAK +" + awarded, gate.x, getGroundY() - gate.height,
                    Color.rgb(255, 218, 121));
            effects.spawnDustBurst(gate.x, getGroundY(), 24, Color.argb(190, 235, 245, 248));
            screenShake = Math.max(screenShake, 0.14f);
            addAuroraMeter(15f, "Wildlife route break");
            return;
        }

        for (RoutePlatform platform : routePlatforms) {
            if (!platform.brittle || platform.broken) continue;
            if (hazard.x + hazard.radius >= platform.x && hazard.x - hazard.radius <= platform.x + platform.width
                    && Math.abs(hazard.y - platform.y) < hazard.radius * 2f) {
                platform.broken = true;
                hazard.environmentHit = true;
                effects.spawnScorePopup("BEAR BREAK", platform.x + platform.width / 2f,
                        platform.y - dp(18), Color.rgb(255, 166, 84));
                effects.spawnSparkBurst(platform.x + platform.width / 2f, platform.y, 26, Color.WHITE);
                screenShake = Math.max(screenShake, 0.13f);
                return;
            }
        }
    }

    private float wolfPounceOffset(Hazard hazard) {
        if (!hazard.pouncingWolf || hazard.roaring) {
            return 0f;
        }
        float distance = hazard.x - playerX;
        if (distance < dp(18) || distance > dp(154)) {
            return 0f;
        }
        float progress = 1f - (distance - dp(18)) / dp(136);
        float arc = (float) Math.sin(progress * Math.PI);
        float airborneAllowance = grounded ? 1f : 0.72f;
        return -arc * dp(38) * airborneAllowance;
    }

    private float hazardVisualPhase(Hazard hazard) {
        return hazard.age + hazard.phase;
    }

    private void maybeAwardNearMiss(Hazard hazard) {
        if (hazard.nearMissAwarded || hazard.passed) {
            return;
        }
        float dx = Math.abs(hazard.x - playerX);
        if (dx > playerRadius * CollisionTuning.PLAYER_NEAR_MISS_X_SCALE) {
            return;
        }
        float hazardHitRadius = hazard.radius * CollisionTuning.hazardRadiusScale(hazard.roaring);
        float hazardHitY = CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring);
        float dy = hazardHitY - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float hitDistance = playerRadius * CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE + hazardHitRadius;
        float nearDistance = hitDistance + dp(20);
        if (distance > hitDistance && distance < nearDistance) {
            hazard.nearMissAwarded = true;
            nearMissFlash = 0.25f;
            gameState.addCombo();
            int awarded = addScore(6, "Near miss");
            effects.spawnScorePopup("NEAR +" + awarded, playerX + dp(20), playerY - playerRadius * 1.6f, Color.rgb(132, 213, 232));
            effects.spawnSparkBurst(playerX, playerY, 7, Color.rgb(132, 213, 232));
            addAuroraMeter(16f, "Near miss");
            if (flowActive()) {
                flowTimer = Math.min(FLOW_TIMER_SECONDS + 2.0f, flowTimer + 0.75f);
                effects.spawnScorePopup("FLOW EXTEND", playerX, playerY - playerRadius * 2.35f, Color.rgb(77, 219, 184));
                screenShake = Math.max(screenShake, 0.055f);
            }
            showComboCallout();
        }
    }

    private void updateHazardRoar(Hazard hazard, float dt) {
        // Bears used to briefly rear up as they approached, swelling their
        // hitbox and slowing down, then snap back to walking. That mid-approach
        // change read as unfair and confusing ("the bear keeps changing"), so
        // wildlife now keeps a single consistent walking stance and hitbox for
        // the whole pass. The roar sprite is reserved for the boss.
    }

    private void updateShots(float dt) {
        Iterator<Shot> iterator = shots.iterator();
        while (iterator.hasNext()) {
            Shot shot = iterator.next();
            shot.x += shot.speed * dt;
            shot.y += shot.vy * dt;
            shot.vy += dp(58) * dt;
            shot.wobble += dt * 9f;
            shot.age += dt;
            if (random.nextFloat() < (shot.empowered ? 0.72f : 0.45f)) {
                effects.spawnParticle(shot.x - shot.radius * 0.8f, shot.y, -dp(45), (random.nextFloat() - 0.5f) * dp(26), shot.radius * (shot.empowered ? 0.60f : 0.45f), shot.empowered ? Color.argb(178, 255, 218, 121) : Color.argb(150, 230, 248, 255), 0.22f);
            }
            if (shot.x > getWidth() + dp(32) || shot.y > getHeight() + dp(32)) {
                iterator.remove();
                continue;
            }

            BossAttack hitAttack = hitBossAttackForShot(shot);
            if (hitAttack != null) {
                iterator.remove();
                shatterBossAttack(hitAttack, shot);
                continue;
            }

            RoutePlatform hitPlatform = hitRoutePlatformForShot(shot);
            if (hitPlatform != null) {
                iterator.remove();
                damageRoutePlatform(hitPlatform, shot);
                continue;
            }

            Gate hitGate = hitDestructibleGateForShot(shot);
            if (hitGate != null) {
                iterator.remove();
                destroyGateWithShot(hitGate, shot);
                continue;
            }

            Hazard hitHazard = hitHazardForShot(shot);
            if (hitHazard != null) {
                iterator.remove();
                stunHazard(hitHazard, shot);
                continue;
            }

            if (bossActive && circleHitsCircle(shot.x, shot.y, shot.radius, bossX, bossY, bossRadius())) {
                iterator.remove();
                int damage = bossWeakWindowActive() ? (shot.empowered ? 3 : 2) : (shot.empowered ? 2 : 1);
                bossHealth -= damage;
                bossStunTimer = Math.max(bossStunTimer, selectedStage == 4 ? 0.28f : 0.18f);
                damageFlash = shot.empowered ? 0.22f : 0.16f;
                screenShake = Math.max(screenShake, shot.empowered ? 0.16f : 0.12f);
                worldFlash = Math.max(worldFlash, shot.empowered ? 0.15f : 0.10f);
                if (damage >= 3 && bossState != BOSS_STATE_RECOVER) {
                    enterBossRecover();
                }
                int awarded = addScore(25 + damage * 5, "Boss hit");
                effects.spawnScorePopup((damage >= 3 ? "POWER WEAK +" : damage > 1 ? "WEAK x2 +" : "HIT +") + awarded, bossX, bossY - bossRadius(), damage > 1 ? Color.rgb(255, 218, 121) : Color.rgb(255, 246, 207));
                effects.spawnSparkBurst(bossX, bossY, damage > 1 ? 28 : 14, damage > 1 ? Color.rgb(255, 218, 121) : Color.rgb(255, 98, 84));
                addAuroraMeter(damage > 1 ? 18f : 8f, "Boss hit");
                playSound("hit");
                logEvent(STAGES[selectedStage].bossName + " hit. HP " + Math.max(0, bossHealth) + "/" + bossMaxHealth + ".");
                if (bossHealth <= 0) {
                    completeStage();
                    return;
                }
            }
        }
    }

    private boolean bossAttackHitsPlayer(BossAttack attack) {
        if (attack.type == ATTACK_LASER) {
            float eyeX = bossLaserEyeX();
            float eyeY = bossLaserEyeY();
            float endX = laserAttackEndX(attack);
            float playerHitRadius = playerRadius * CollisionTuning.PLAYER_BOSS_LASER_RADIUS_SCALE;

            if (GameMath.circleHitsSegment(playerX, playerY, playerHitRadius, eyeX, eyeY, endX, attack.y, attack.radius)) {
                return true;
            }

            if (selectedStage == 0) {
                // The Sun boss has two eyes.
                float rightEyeX = bossX + bossRadius() * 0.28f;
                return GameMath.circleHitsSegment(playerX, playerY, playerHitRadius, rightEyeX, eyeY, endX, attack.y, attack.radius);
            }
            return false;
        }
        return circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.PLAYER_BOSS_ATTACK_RADIUS_SCALE, attack.x, attack.y, attack.radius);
    }

    private Hazard hitHazardForShot(Shot shot) {
        for (Hazard hazard : hazards) {
            float hitRadius = hazard.radius * CollisionTuning.hazardShotRadiusScale(hazard.roaring);
            float hitY = CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring);
            if (circleHitsCircle(shot.x, shot.y, shot.radius, hazard.x, hitY, hitRadius)) {
                return hazard;
            }
        }
        return null;
    }

    private BossAttack hitBossAttackForShot(Shot shot) {
        for (BossAttack attack : bossAttacks) {
            if (!bossAttackCanBeShot(attack)) {
                continue;
            }
            if (circleHitsCircle(shot.x, shot.y, shot.radius * CollisionTuning.SHOT_BOSS_ATTACK_RADIUS_SCALE, attack.x, attack.y, attack.radius * CollisionTuning.BOSS_ATTACK_SHOT_RADIUS_SCALE)) {
                return attack;
            }
        }
        return null;
    }

    private BossAttack nearestBossAttackTarget(float startX, float maxX) {
        BossAttack nearest = null;
        float nearestX = maxX;
        for (BossAttack attack : bossAttacks) {
            if (!bossAttackCanBeShot(attack) || attack.x <= startX || attack.x >= nearestX) {
                continue;
            }
            nearest = attack;
            nearestX = attack.x;
        }
        return nearest;
    }

    private boolean bossAttackCanBeShot(BossAttack attack) {
        return attack.type == ATTACK_ICE || (attack.type == ATTACK_LASER && flowActive());
    }

    private void shatterBossAttack(BossAttack attack, Shot shot) {
        freezeTimer = 0.04f; // Tiny impact freeze
        bossAttacks.remove(attack);
        if (attack.type == ATTACK_LASER) {
            int reflectedDamage = shot.empowered ? 3 : 2;
            bossHealth -= reflectedDamage;
            int reflectedScore = addScore(45, "Flow laser reflection");
            effects.spawnScorePopup("BEAM REFLECT +" + reflectedScore, bossX,
                    bossLaserEyeY() - dp(18), Color.rgb(77, 219, 184));
            effects.spawnSparkBurst(bossLaserEyeX(), bossLaserEyeY(), 34, Color.WHITE);
            flowTimer = Math.min(FLOW_TIMER_SECONDS + 2f, flowTimer + 0.8f);
            screenShake = Math.max(screenShake, 0.16f);
            if (bossHealth <= 0) completeStage();
            return;
        }
        int awarded = addScore(shot.empowered ? 24 : 16, "Boss projectile shattered");
        effects.spawnScorePopup("SHATTER +" + awarded, attack.x, attack.y - attack.radius * 1.4f, Color.rgb(132, 213, 232));
        effects.spawnSparkBurst(attack.x, attack.y, shot.empowered ? 22 : 15, shot.empowered ? Color.rgb(255, 218, 121) : Color.rgb(230, 248, 255));
        addAuroraMeter(shot.empowered ? 14f : 9f, "Projectile shattered");
        screenShake = Math.max(screenShake, shot.empowered ? 0.09f : 0.06f);
        showRunCallout("PROJECTILE SHATTERED", 0.75f);
        playSound("hit");
        logEvent("Boss projectile shattered.");
        if ((selectedStage == 1 || selectedStage == 3) && routePlatforms.size() < 5) {
            float platformY = clamp(attack.y + dp(32), getGroundY() - dp(138), getGroundY() - dp(58));
            routePlatforms.add(new RoutePlatform(attack.x - dp(46), platformY, dp(92),
                    false, true, random.nextFloat() * 6f));
            effects.spawnScorePopup("ICE BRIDGE", attack.x, platformY - dp(14), Color.rgb(132, 213, 232));
        }
    }

    private Gate hitDestructibleGateForShot(Shot shot) {
        boolean breakable = selectedStage == 1 || (shot.empowered && (selectedStage == 3 || selectedStage == 4));
        if (!breakable || bossActive) {
            return null;
        }
        for (Gate gate : gates) {
            float logHeight = riverLogHeight(gate);
            float logTop = riverLogTop(gate);
            tempRect.set(gate.x - dp(10), logTop - dp(7), gate.x + gate.width + dp(12), logTop + logHeight + dp(8));
            if (circleHitsRect(shot.x, shot.y, shot.radius * CollisionTuning.SHOT_LOG_RADIUS_SCALE, tempRect)) {
                return gate;
            }
        }
        return null;
    }

    private RoutePlatform hitRoutePlatformForShot(Shot shot) {
        for (RoutePlatform platform : routePlatforms) {
            if (!platform.brittle || platform.broken) continue;
            tempRect.set(platform.x, platform.y - dp(5), platform.x + platform.width, platform.y + dp(18));
            if (circleHitsRect(shot.x, shot.y, shot.radius, tempRect)) return platform;
        }
        return null;
    }

    private void damageRoutePlatform(RoutePlatform platform, Shot shot) {
        platform.hits += shot.empowered ? 2 : 1;
        if (platform.hits < 2) {
            effects.spawnScorePopup("ICE CRACK", shot.x, platform.y - dp(14), Color.rgb(132, 213, 232));
            effects.spawnSparkBurst(shot.x, platform.y, 10, Color.WHITE);
            return;
        }
        platform.broken = true;
        int awarded = addScore(shot.empowered ? 30 : 20, "Brittle route shattered");
        effects.spawnScorePopup("ROUTE SHATTER +" + awarded, platform.x + platform.width / 2f,
                platform.y - dp(18), Color.rgb(77, 219, 184));
        effects.spawnSparkBurst(platform.x + platform.width / 2f, platform.y, 28, Color.rgb(132, 213, 232));
        screenShake = Math.max(screenShake, 0.10f);
        if (flowActive()) flowTimer = Math.min(FLOW_TIMER_SECONDS + 2f, flowTimer + 0.5f);
    }

    private Gate nearestDestructibleGate(float startX, float maxX) {
        if (selectedStage != 1 || bossActive) {
            return null;
        }
        Gate nearest = null;
        float nearestX = maxX;
        for (Gate gate : gates) {
            if (gate.x <= startX || gate.x >= nearestX) {
                continue;
            }
            nearest = gate;
            nearestX = gate.x;
        }
        return nearest;
    }

    private void destroyGateWithShot(Gate gate, Shot shot) {
        gates.remove(gate);
        if (!gate.passed) {
            gate.passed = true;
            gatesPassed++;
            gameState.gatesPassed = gatesPassed;
        }
        gameState.addCombo();
        String reaction = selectedStage == 3 ? "ICE SHATTER" : selectedStage == 4 ? "SNOW ROUTE" : "LOG BOOM";
        int awarded = addScore(shot.empowered ? 28 : 18, reaction);
        float x = gate.x + gate.width * 0.5f;
        float y = riverLogTop(gate) + riverLogHeight(gate) * 0.5f;
        effects.spawnScorePopup(reaction + " +" + awarded, x, y - dp(28), Color.rgb(255, 218, 121));
        effects.spawnSparkBurst(x, y, 24, Color.rgb(226, 169, 83));
        effects.spawnDustBurst(x, getGroundY(), 12, Color.argb(185, 132, 213, 232));
        runLogsBlasted++;
        addAuroraMeter(12f, "Log blasted");
        screenShake = Math.max(screenShake, 0.10f);
        worldFlash = Math.max(worldFlash, 0.08f);
        checkMissionProgress();
        showRunCallout(selectedStage >= 3 ? "FLOW ROUTE OPEN" : "LOG BLASTED", 0.85f);
        showComboCallout();
        playSound("hit");
        logEvent("River log blasted " + gatesPassed + "/" + STAGES[selectedStage].goalGates + ".");
    }

    private void stunHazard(Hazard hazard) {
        stunHazard(hazard, null);
    }

    private void stunHazard(Hazard hazard, Shot shot) {
        float impactX = hazard.x;
        float impactY = hazard.y;
        String impactLabel = hazard.label;
        hazards.remove(hazard);
        gameState.addCombo();
        int awarded = addScore(12, hazard.label + " stunned");
        effects.spawnScorePopup("STUN +" + awarded, hazard.x, hazard.y - hazard.radius * 1.3f, Color.rgb(132, 213, 232));
        effects.spawnSparkBurst(hazard.x, hazard.y, 12, Color.rgb(230, 248, 255));
        addAuroraMeter(10f, "Wildlife stunned");
        showRunCallout("STUN, THEN MOVE", 0.95f);
        playSound("hit");
        checkMissionProgress();

        if (shot == null) return;

        // Bears carry momentum into nearby wildlife; lining up the shot turns
        // one defensive snowball into a deliberate chain reaction.
        if ("BEAR".equals(impactLabel) || "POLAR".equals(impactLabel)) {
            Hazard collision = nearestHazardToPoint(impactX, impactY, dp(150));
            if (collision != null) {
                hazards.remove(collision);
                gameState.addCombo();
                int chainAward = addScore(shot.empowered ? 34 : 24, "Wildlife collision");
                effects.spawnScorePopup("WILDLIFE COLLISION +" + chainAward,
                        collision.x, collision.y - collision.radius, Color.rgb(255, 218, 121));
                effects.spawnSparkBurst(collision.x, collision.y, 22, Color.rgb(255, 166, 84));
                screenShake = Math.max(screenShake, 0.11f);
            }
        }

        // In Dark Winter, interrupting a dive drops an icicle into the ground
        // lane. It can clear space, but the player must still read its fall.
        if (selectedStage == 3 && ("EAGLE".equals(impactLabel) || "DARK".equals(impactLabel))) {
            hazards.add(new Hazard(impactX, -dp(32), gameplayDp(13), 0.8f,
                    random.nextFloat(), "ICE SPIKE", null));
            effects.spawnScorePopup("DROP!", impactX, impactY, Color.rgb(132, 213, 232));
        }

        // FLOW shots bank their remaining force into one nearby target.
        if (shot.empowered) {
            Hazard ricochet = nearestHazardToPoint(impactX, impactY, dp(230));
            if (ricochet != null) {
                hazards.remove(ricochet);
                gameState.addCombo();
                int bankAward = addScore(30, "Flow ricochet");
                effects.spawnScorePopup("RICOCHET +" + bankAward, ricochet.x,
                        ricochet.y - ricochet.radius, Color.rgb(77, 219, 184));
                effects.spawnSparkBurst(ricochet.x, ricochet.y, 20, Color.rgb(77, 219, 184));
                flowTimer = Math.min(FLOW_TIMER_SECONDS + 2f, flowTimer + 0.45f);
            }
        }
    }

    private Hazard nearestHazardToPoint(float x, float y, float maxDistance) {
        Hazard nearest = null;
        float bestDistanceSq = maxDistance * maxDistance;
        for (Hazard candidate : hazards) {
            float dx = candidate.x - x;
            float dy = candidate.y - y;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq < bestDistanceSq) {
                nearest = candidate;
                bestDistanceSq = distanceSq;
            }
        }
        return nearest;
    }

    private void startBossPhase() {
        gates.clear();
        routePlatforms.clear();
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
        buildBossArena();
        showRunCallout("FIRE TO DEFEAT. DODGE TELLS.", 2.2f);
        screenShake = Math.max(screenShake, 0.08f);
        logEvent("Boss phase: " + STAGES[selectedStage].bossName + ". Use FIRE.");
    }

    private void buildBossArena() {
        float ground = getGroundY();
        if (selectedStage == 0) {
            routePlatforms.add(new RoutePlatform(getWidth() * 0.34f, ground - dp(72), dp(86), true, false, 0.4f));
            routePlatforms.add(new RoutePlatform(getWidth() * 0.58f, ground - dp(116), dp(78), true, false, 2.2f));
        } else if (selectedStage == 1) {
            routePlatforms.add(new RoutePlatform(getWidth() * 0.42f, ground - dp(62), dp(118), false, true, 0f));
        } else if (selectedStage == 2) {
            routePlatforms.add(new RoutePlatform(getWidth() * 0.30f, ground - dp(82), dp(92), false, false, 0f));
            routePlatforms.add(new RoutePlatform(getWidth() * 0.55f, ground - dp(124), dp(92), true, false, 1.4f));
        } else if (selectedStage == 3) {
            routePlatforms.add(new RoutePlatform(getWidth() * 0.28f, ground - dp(76), dp(82), true, true, 0.5f));
            routePlatforms.add(new RoutePlatform(getWidth() * 0.52f, ground - dp(132), dp(94), true, true, 2.8f));
        } else {
            routePlatforms.add(new RoutePlatform(getWidth() * 0.26f, ground - dp(66), dp(108), false, true, 0f));
            routePlatforms.add(new RoutePlatform(getWidth() * 0.52f, ground - dp(112), dp(104), true, true, 2.1f));
        }
    }

    private void updateBoss(float dt) {
        /*
         * The boss is a small state machine. A state machine means the boss can
         * be in exactly one mode at a time:
         * ENTER -> TELL -> ATTACK -> RECOVER -> TELL again.
         *
         * "Tell" is the warning before the attack. Good action games show tells
         * so the player has a fair chance to react.
         */
        bossTimer += dt;
        bossStateTimer += dt;
        bossPatternTimer += dt;
        bossStunTimer = Math.max(0f, bossStunTimer - dt);
        StageConfig stage = STAGES[selectedStage];
        boolean enraged = bossEnraged();
        float stateSpeed = BossTuning.stateSpeed(usesLaserBossTuning(), bossHealth, bossMaxHealth, enraged);

        if (!bossPhaseTwoAnnounced && bossMaxHealth > 1 && bossHealth <= bossMaxHealth / 2) {
            bossPhaseTwoAnnounced = true;
            showRunCallout("BOSS PHASE TWO", 1.25f);
            screenShake = Math.max(screenShake, 0.12f);
            effects.spawnSparkBurst(bossX, bossY - bossRadius(), 20, Color.rgb(255, 98, 84));
            logEvent(STAGES[selectedStage].bossName + " entered phase two.");
        }

        if (selectedStage == 4 && !bossPhaseThreeAnnounced && bossHealth <= Math.max(1, bossMaxHealth / 5)) {
            bossPhaseThreeAnnounced = true;
            showRunCallout("FINAL STAND: BLIZZARD", 2.0f);
            screenShake = Math.max(screenShake, 0.22f);
            worldFlash = 1.0f;
            effects.spawnSparkBurst(bossX, bossY, 40, Color.WHITE);
            logEvent("Polar Bear entered FINAL PHASE.");
        }

        if (!bossEnrageAnnounced && bossMaxHealth > 2 && enraged) {
            bossEnrageAnnounced = true;
            showRunCallout("BOSS ENRAGED", 1.1f);
            screenShake = Math.max(screenShake, 0.16f);
            worldFlash = Math.max(worldFlash, 0.18f);
            effects.spawnSparkBurst(bossX, bossY - bossRadius() * 0.4f, 26, Color.rgb(255, 98, 84));
            logEvent(STAGES[selectedStage].bossName + " enraged.");
        }

        int transition = BossStateMachine.transition(
                bossState,
                bossX <= bossRestX(selectedStage) + dp(12),
                bossStateTimer,
                bossTellDuration(),
                bossAttackDuration(),
                bossRecoverDuration(),
                stateSpeed
        );
        if (transition == BossStateMachine.ENTER_TELL) {
            enterBossTell(BOSS_PATTERN_LUNGE);
        } else if (transition == BossStateMachine.BEGIN_ATTACK) {
            beginBossAttack();
        } else if (transition == BossStateMachine.ENTER_RECOVER) {
            enterBossRecover();
        } else if (transition == BossStateMachine.NEXT_TELL) {
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

        /*
         * In normal play, the boss has a survival timer so the player must fight
         * instead of waiting forever. Demo mode skips this so the computer can
         * keep presenting the game even if it plays imperfectly.
         */
        if (!demoMode && bossTimer > BossTuning.BOSS_SURVIVAL_SECONDS) {
            endGame(stage.bossName + " outlasted you.");
        }
    }

    private float bossTimeRemaining() {
        return Math.max(0f, BossTuning.BOSS_SURVIVAL_SECONDS - bossTimer);
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
            showRunCallout(selectedStage == 4 ? "ICE THROW: JUMP OR FIRE" : "PROJECTILES: JUMP OR FIRE", 0.9f);
        } else if (pattern == BOSS_PATTERN_LASER) {
            showRunCallout("EYE BEAM: DODGE", 1.0f);
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
        } else if (bossPattern == BOSS_PATTERN_LASER) {
            spawnBossLaser();
        } else {
            screenShake = Math.max(screenShake, selectedStage == 4 ? 0.11f : 0.07f);
            effects.spawnDustBurst(bossX - bossRadius(), getGroundY(), selectedStage == 4 ? 18 : 10, Color.argb(190, 235, 245, 248));
            if ((selectedStage == 2 || selectedStage == 4) && bossEnraged()) {
                collapseBossPlatform();
            }
        }

        if (selectedStage == 4 && bossPhaseThreeAnnounced) {
            // Final boss bonus pressure: spawn additional icicle hazards from top
            float rx = playerX + (random.nextFloat() - 0.5f) * dp(100);
            hazards.add(new Hazard(rx, -dp(40), gameplayDp(14), 0.8f, random.nextFloat(), "ICE SPIKE", null));
            logEvent("Polar Bear summoned Ice Spike!");
        }

        // Phase-two attacks overlap with a fast wildlife lane. Boss fights now
        // test positioning and prioritization instead of presenting one isolated
        // mechanic at a time.
        if (bossPhaseTwoAnnounced && bossPattern != BOSS_PATTERN_SUMMON && bossPatternCount % 2 == 1) {
            String pressureLabel = selectedStage >= 3 ? "WOLF" : STAGES[selectedStage].hazardLabel;
            float pressureRadius = gameplayDp(hazardRadiusDp(pressureLabel));
            hazards.add(new Hazard(getWidth() + dp(42), hazardSpawnY(pressureLabel, pressureRadius),
                    pressureRadius, hazardSpeedMultiplier(pressureLabel) + 0.12f,
                    random.nextFloat() * 4f, pressureLabel, null));
            showRunCallout("CROSSFIRE", 0.72f);
        }
    }

    private void collapseBossPlatform() {
        RoutePlatform target = null;
        float best = Float.MAX_VALUE;
        for (RoutePlatform platform : routePlatforms) {
            if (platform.broken) continue;
            float distance = Math.abs((platform.x + platform.width / 2f) - playerX);
            if (distance < best) {
                best = distance;
                target = platform;
            }
        }
        if (target == null) return;
        target.broken = true;
        effects.spawnScorePopup("ARENA BREAK", target.x + target.width / 2f,
                target.y - dp(16), Color.rgb(255, 98, 84));
        effects.spawnDustBurst(target.x + target.width / 2f, target.y, 22, Color.argb(190, 235, 245, 248));
        screenShake = Math.max(screenShake, 0.18f);
    }

    private void enterBossRecover() {
        bossState = BOSS_STATE_RECOVER;
        bossStateTimer = 0f;
        bossPatternCount++;
    }

    private int nextBossPattern() {
        return StageBossRules.nextPattern(selectedStage, bossHealth, bossMaxHealth, bossPatternCount,
                BOSS_PATTERN_LUNGE, BOSS_PATTERN_SNOW_WAVE, BOSS_PATTERN_SUMMON, BOSS_PATTERN_LASER);
    }

    private boolean bossEnraged() {
        return bossActive && bossMaxHealth > 2 && bossHealth <= Math.max(1, bossMaxHealth / 4);
    }

    private float bossDesiredX() {
        if (bossState == BOSS_STATE_ENTER) {
            return bossRestX(selectedStage);
        }
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            return bossAttackX(selectedStage);
        }
        if (bossState == BOSS_STATE_TELL) {
            float windup = bossPattern == BOSS_PATTERN_LUNGE ? dp(selectedStage == 4 ? 16 : 10) : 0f;
            return bossRestX(selectedStage) + windup + (float) Math.sin(bossStateTimer * 18f) * dp(selectedStage == 4 ? 3f : 2f);
        }
        return bossRestX(selectedStage);
    }

    private float bossTrackingRate() {
        if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            return (selectedStage == 4 ? 7.2f : 6.2f) + (bossEnraged() ? 0.9f : 0f);
        }
        if (bossState == BOSS_STATE_ENTER) {
            return 2.7f;
        }
        return (bossState == BOSS_STATE_RECOVER ? 4.2f : 3.4f) + (bossEnraged() ? 0.45f : 0f);
    }

    private float bossTellDuration() {
        return BossTuning.tellDuration(usesLaserBossTuning(), bossEnraged());
    }

    private float bossAttackDuration() {
        return BossTuning.attackDuration(usesLaserBossTuning(), bossEnraged(), bossPattern == BOSS_PATTERN_LUNGE, bossPattern == BOSS_PATTERN_LASER);
    }

    private float bossRecoverDuration() {
        return BossTuning.recoverDuration(usesLaserBossTuning(), bossEnraged());
    }

    private boolean usesLaserBossTuning() {
        return StageBossRules.usesLaserTuning(selectedStage);
    }

    private void spawnBossSnowWave() {
        float baseY = getGroundY() - dp(22);
        int count = bossHealth <= bossMaxHealth / 2 ? 3 : 2;
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
        if (bossEnraged()) {
            String secondLabel = selectedStage == 4 ? STAGES[selectedStage].hazardLabel : "WOLF";
            float secondRadius = gameplayDp(hazardRadiusDp(secondLabel));
            float secondY = hazardSpawnY(secondLabel, secondRadius);
            hazards.add(new Hazard(getWidth() + dp(96), secondY, secondRadius, hazardSpeedMultiplier(secondLabel) + 0.02f, random.nextFloat() * 4f, secondLabel, null));
        }
        bossAttacks.add(new BossAttack(bossX - bossRadius(), getGroundY() - dp(14), dp(18), -dp(210), 0f, ATTACK_SHOCKWAVE, "Shockwave"));
        screenShake = Math.max(screenShake, 0.12f);
        effects.spawnScorePopup("ROAR", bossX, bossY - bossRadius(), Color.rgb(255, 246, 207));
    }

    private void spawnBossLaser() {
        float beamX = bossLaserEyeX();
        float targetY = clamp(bossTellY, getGroundY() - dp(128), getGroundY() - dp(38));
        BossAttack laser = new BossAttack(beamX, targetY, dp(selectedStage == 0 ? 6.5f : 5.5f), 0f, 0f, ATTACK_LASER, "Eye beam");
        laser.spin = getWidth() + dp(80);
        bossAttacks.add(laser);
        damagePlatformsAlongLaser(targetY);
        screenShake = Math.max(screenShake, 0.10f);
        effects.spawnSparkBurst(beamX, bossLaserEyeY(), 12, Color.rgb(255, 98, 84));
        playSound("throw");
    }

    private void damagePlatformsAlongLaser(float targetY) {
        for (RoutePlatform platform : routePlatforms) {
            if (!platform.brittle || platform.broken || Math.abs(platform.y - targetY) > dp(24)) continue;
            platform.hits++;
            if (platform.hits >= 2 || bossEnraged()) {
                platform.broken = true;
                effects.spawnScorePopup("LASER SHATTER", platform.x + platform.width / 2f,
                        platform.y - dp(16), Color.rgb(255, 98, 84));
                effects.spawnSparkBurst(platform.x + platform.width / 2f, platform.y, 24, Color.WHITE);
            } else {
                effects.spawnScorePopup("ICE CRACK", platform.x + platform.width / 2f,
                        platform.y - dp(16), Color.rgb(132, 213, 232));
            }
        }
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
            } else if (attack.type == ATTACK_LASER) {
                attack.x = bossLaserEyeX();
                attack.spin = Math.min(getWidth() + dp(80), attack.spin + dp(760) * dt);
                if (random.nextFloat() < 0.62f) {
                    effects.spawnParticle(attack.x - random.nextFloat() * Math.max(dp(30), attack.spin), attack.y, -dp(55), (random.nextFloat() - 0.5f) * dp(18), dp(2.2f), Color.argb(170, 255, 98, 84), 0.18f);
                }
            } else {
                attack.radius += dp(13) * dt;
            }
            float maxAge = attack.type == ATTACK_LASER ? 1.12f : 4f;
            if (attack.x + attack.radius < -dp(40) || attack.y > getHeight() + dp(40) || attack.age > maxAge) {
                iterator.remove();
            }
        }
    }

    private void completeStage() {
        StageConfig stage = STAGES[selectedStage];
        /*
         * Completing a stage is a good example of separating "show feedback" and
         * "save progress." We always show sparks, points, and clear text. We
         * only write rewards/unlocks to storage when this is not Computer Run.
         */
        bossActive = false;
        bossDefeated = true;
        int clearBonus = stageClearBonus(selectedStage, gameState.bestCombo, gameState.stars);
        int awarded = addScore(clearBonus, "Stage clear bonus");
        effects.spawnScorePopup("CLEAR +" + awarded, getWidth() / 2f, getHeight() * 0.38f, Color.rgb(255, 218, 121));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.38f, 26, Color.rgb(255, 218, 121));
        showRunCallout("STAGE CLEAR", 2.0f);
        worldFlash = Math.max(worldFlash, 0.24f);
        if (selectedStage == 0) {
            eclipseTimer = Math.max(eclipseTimer, 4.2f);
            showRunCallout("ECLIPSE! STAGE CLEAR", 2.2f);
        }
        if (selectedStage == 4) {
            showRunCallout("ALASKA SURVIVED! GLOBAL RUSH NEXT?", 3.5f);
        }
        playSound("medal");
        if (!demoMode && score > bestScore) {
            bestScore = score;
            runNewBest = true;
        }
        if (!demoMode && selectedStage < STAGES.length - 1 && unlockedStage < selectedStage + 1) {
            unlockedStage = selectedStage + 1;
        }
        if (!demoMode) {
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
                    .putInt(PREF_EXPEDITION_LOGS, expeditionLogs)
                    .apply();
        } else {
            runTokensEarned = 0;
            dailyTokensEarned = 0;
        }
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
        int newLogs = 1 + (gradeScore >= 7 ? 1 : 0);
        expeditionLogs += newLogs;
        runExpeditionBonusTokens += newLogs * 2;
        int awarded = addScore(45 + selectedStage * 8, "Expedition bonus");
        effects.spawnScorePopup("EXPEDITION +" + newLogs + " LOG", getWidth() / 2f, getHeight() * 0.40f, Color.rgb(77, 219, 184));
        effects.spawnSparkBurst(getWidth() / 2f, getHeight() * 0.40f, 24, Color.rgb(77, 219, 184));
        showRunCallout("EXPEDITION GRADE " + expeditionGrade(true), 1.55f);
        logEvent("Expedition bonus +" + runExpeditionBonusTokens + " tokens, score +" + awarded + ".");
    }

    private void endGame(String reason) {
        freezeTimer = 0.12f; // Briefly freeze the world on hit
        /*
         * endGame() is called whenever the runner would be defeated. Demo mode
         * turns that defeat into a reset because Computer Run is for watching and
         * testing, not for changing save data.
         */
        if (demoMode) {
            perfectRun = false;
            resetAfterHit();
            showRunCallout("COMPUTER RUN: INVINCIBLE", 0.85f);
            logEvent("Computer Run ignored hit: " + reason);
            return;
        }
        gameState.breakCombo();
        perfectRun = false;
        haptic(HapticFeedbackConstants.LONG_PRESS);
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
        gameState.exhaustLives();
        awardRunTokens(false);
        state = STATE_GAME_OVER;
        screenShake = Math.max(screenShake, 0.18f);
        worldFlash = Math.max(worldFlash, 0.18f);
        if (score > bestScore) {
            bestScore = score;
            runNewBest = true;
            prefs.edit()
                    .putInt(PREF_BEST_SCORE, bestScore)
                    .putInt(PREF_XP, gameState.xp)
                    .putInt(PREF_TRAIL_TOKENS, trailTokens)
                    .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                    .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                    .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                    .putInt(PREF_DAILY_STREAK, dailyStreak)
                    .putInt(PREF_EXPEDITION_LOGS, expeditionLogs)
                    .apply();
        } else {
            prefs.edit()
                    .putInt(PREF_XP, gameState.xp)
                    .putInt(PREF_TRAIL_TOKENS, trailTokens)
                    .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                    .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                    .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                    .putInt(PREF_DAILY_STREAK, dailyStreak)
                    .putInt(PREF_EXPEDITION_LOGS, expeditionLogs)
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
        if (!RunRewardEconomy.canClaimDailyReward(
                dailyRushMode,
                dailyBonusAwarded,
                dailyCompletedDay,
                today,
                selectedStage,
                dailyStageIndex())) {
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
        cleanVaultStreak = 0;
        flowTimer = 0f;
        spawnCooldown = 0.75f;
        hazardCooldown = 1.25f;
        respawnGraceTimer = 1.35f;
        damageFlash = 0.18f;
        screenShake = Math.max(screenShake, 0.16f);
        worldFlash = Math.max(worldFlash, 0.16f);
        effects.spawnDustBurst(playerX, getGroundY(), 14, Color.argb(190, 255, 255, 255));
        showRunCallout("RESPAWN GRACE", 1.0f);
    }

    private int addScore(int amount, String reason) {
        int multiplier = activeScoreMultiplier();
        int awarded = Math.round(amount * multiplier * perkScoreMultiplier());
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
        if (flowActive()) {
            multiplier = Math.min(5, multiplier + 1);
        }
        return multiplier;
    }

    private boolean hasPerk(int perk) {
        return activePerk == perk;
    }

    private float perkScoreMultiplier() {
        return hasPerk(PERK_TRAILBLAZER) ? 1.25f : 1f;
    }

    private float perkJumpMultiplier() {
        return hasPerk(PERK_SPRING_STEP) ? 1.12f : 1f;
    }

    private void rollPerkChoices() {
        // Offer three distinct perks each run so the draft always has variety.
        int a = random.nextInt(PERK_COUNT);
        int b = (a + 1 + random.nextInt(PERK_COUNT - 1)) % PERK_COUNT;
        int c = b;
        while (c == a || c == b) {
            c = random.nextInt(PERK_COUNT);
        }
        perkChoices[0] = a;
        perkChoices[1] = b;
        perkChoices[2] = c;
    }

    private void applyPerkSelection(int perk) {
        activePerk = perk;
        if (perk == PERK_GLACIER_GUARD) {
            gameState.shieldActive = true;
        } else if (perk == PERK_SPRAY_CANISTER) {
            bearSprayCharges++;
        }
        showRunCallout("PERK: " + PERK_NAMES[perk], 1.4f);
        logEvent("Perk drafted: " + PERK_NAMES[perk] + ".");
    }

    private int tappedPerkCard(float x, float y) {
        for (int i = 0; i < perkCardBounds.length; i++) {
            if (perkCardBounds[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void showComboCallout() {
        int multiplier = activeScoreMultiplier();
        if (gameState.combo >= 3 || multiplier > 1) {
            String prefix = auroraRushTimer > 0f ? "AURORA " : flowActive() ? "FLOW " : "";
            showRunCallout(prefix + "COMBO " + gameState.combo + "  SCORE x" + multiplier, 1.15f);
        }
        checkMissionProgress();
    }

    private void showRunCallout(String label, float seconds) {
        runCallout = label;
        runCalloutTimer = Math.max(runCalloutTimer, seconds);
    }

    private void updateHeldFire(float dt) {
        if (!firePressed || state != STATE_RUNNING) {
            fireHoldTimer = 0f;
            return;
        }
        fireHoldTimer += dt;
        float repeatDelay = flowActive() ? 0.16f : 0.24f;
        if (hasPerk(PERK_AVALANCHE_ARM)) {
            repeatDelay *= 0.66f;
        }
        if (fireHoldTimer >= repeatDelay && shotCooldown <= 0f) {
            fireHoldTimer = 0f;
            fireSnowball();
        }
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

    private void updateFlowTimer(float dt) {
        if (flowTimer <= 0f) {
            return;
        }
        flowTimer = Math.max(0f, flowTimer - dt);
        if (flowTimer <= 0f && cleanVaultStreak < FLOW_STREAK_THRESHOLD) {
            showRunCallout("FLOW FADED", 0.70f);
        }
    }

    private boolean flowActive() {
        return flowTimer > 0f;
    }

    private void activateFlow(float x, float y) {
        flowTimer = FLOW_TIMER_SECONDS;
        int awarded = 0;
        if (cleanVaultStreak == FLOW_STREAK_THRESHOLD || cleanVaultStreak % 2 == 0) {
            awarded = addScore(12 + cleanVaultStreak * 2, "Flow streak");
            addAuroraMeter(9f + cleanVaultStreak, "Flow streak");
        }
        effects.spawnSparkBurst(x, y, 10 + Math.min(12, cleanVaultStreak * 2), Color.rgb(77, 219, 184));
        if (awarded > 0) {
            effects.spawnScorePopup("FLOW +" + awarded, x, y - dp(42), Color.rgb(77, 219, 184));
        }
        screenShake = Math.max(screenShake, 0.045f);
        showRunCallout("FLOW x" + cleanVaultStreak + "  SPEED + SCORE", 1.20f);
        logEvent("Flow streak x" + cleanVaultStreak + ".");
    }

    private float worldSpeedMultiplier() {
        float multiplier = auroraFocusTimer > 0f ? 0.78f : 1f;
        if (flowActive()) {
            multiplier *= RushDirector.worldSpeedMultiplier(true);
        }
        if (chaseBearActive) {
            multiplier *= CHASE_BEAR_SPEED_BOOST;
        }
        return multiplier;
    }

    private float flowProgress() {
        return flowActive() ? Math.min(1f, flowTimer / FLOW_TIMER_SECONDS) : 0f;
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
            completeMission("TRAIL VAULTER");
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
        if (demoMode) {
            effects.spawnScorePopup(label, getWidth() / 2f, getHeight() * 0.34f, Color.rgb(132, 213, 232));
            return;
        }
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
        if (encounterDirector != null) {
            activeEncounter = encounterDirector.next(selectedStage, gatesPassed, flowActive());
        }
        float gateWidth = gameplayDp(34) + random.nextFloat() * gameplayDp(18);
        float hurdleHeight = RunnerTuning.gateHeight(getResources().getDisplayMetrics().density, selectedStage, gatesPassed, random.nextFloat());
        if (activeEncounter != null) {
            if (activeEncounter.route == EncounterCard.ROUTE_HIGH) {
                hurdleHeight *= 1.18f;
            } else if (activeEncounter.route == EncounterCard.ROUTE_GROUND) {
                hurdleHeight *= 0.76f;
            }
        }
        gates.add(new Gate(getWidth() + gateWidth, hurdleHeight, gateWidth));
        spawnRouteGeometry(activeEncounter, getWidth() + gateWidth);
        int starCount = activeEncounter == null
                ? RushDirector.starTrailCount(gatesPassed) : activeEncounter.starCount;
        if (random.nextFloat() < 0.88f) {
            float baseStarY = getGroundY() - hurdleHeight - gameplayDp(30 + random.nextFloat() * 12);
            for (int i = 0; i < starCount; i++) {
                float arc = (float) Math.sin((i + 1f) / (starCount + 1f) * Math.PI);
                float starY = baseStarY - gameplayDp(arc * (starCount >= 3 ? 22f : 12f));
                float starX = getWidth() + gateWidth + gameplayDp(38 + i * 30);
                stars.add(new Star(starX, Math.max(dp(78), starY), gameplayDp(8)));
            }
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
        if (gatesPassed >= 2 && random.nextFloat() < trailMapSpawnChance()) {
            float mapY = getGroundY() - hurdleHeight - gameplayDp(42 + random.nextFloat() * 18);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(206), Math.max(dp(86), mapY), gameplayDp(9.0f), "MAP"));
        }
        if (gatesPassed >= 4 && gameState.lives < 3 && random.nextFloat() < rescueKitSpawnChance()) {
            float kitY = getGroundY() - hurdleHeight - gameplayDp(52 + random.nextFloat() * 18);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(246), Math.max(dp(88), kitY), gameplayDp(9.6f), "KIT"));
        }
        if (gatesPassed >= 3 && bearSprayCharges < SprayTuning.MAX_CHARGES && random.nextFloat() < bearSpraySpawnChance()) {
            float sprayY = getGroundY() - hurdleHeight - gameplayDp(34 + random.nextFloat() * 20);
            powerUps.add(new PowerUp(getWidth() + gateWidth + gameplayDp(286), Math.max(dp(88), sprayY), gameplayDp(9.4f), "SPRAY"));
        }
    }

    private void spawnRouteGeometry(EncounterCard encounter, float anchorX) {
        if (encounter == null) return;
        float ground = getGroundY();
        if (encounter.route == EncounterCard.ROUTE_HIGH) {
            routePlatforms.add(new RoutePlatform(anchorX + gameplayDp(32), ground - gameplayDp(92),
                    gameplayDp(92), false, flowActive(), random.nextFloat() * 6f));
            routePlatforms.add(new RoutePlatform(anchorX + gameplayDp(158), ground - gameplayDp(132),
                    gameplayDp(88), true, false, random.nextFloat() * 6f));
            routePlatforms.add(new RoutePlatform(anchorX + gameplayDp(278), ground - gameplayDp(104),
                    gameplayDp(104), false, selectedStage >= 3, random.nextFloat() * 6f));
        } else if (encounter.route == EncounterCard.ROUTE_PRECISION) {
            routePlatforms.add(new RoutePlatform(anchorX + gameplayDp(116), ground - gameplayDp(70),
                    gameplayDp(82), selectedStage >= 2, false, random.nextFloat() * 6f));
        } else if (flowActive()) {
            // A low FLOW bridge lets skilled players stay aggressive beneath
            // aerial threats without turning the safe route into a free pass.
            routePlatforms.add(new RoutePlatform(anchorX + gameplayDp(120), ground - gameplayDp(42),
                    gameplayDp(132), false, true, random.nextFloat() * 6f));
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

    private float trailMapSpawnChance() {
        if (routeMilestoneIndex >= 3) return 0.04f;
        return selectedStage >= 3 ? 0.14f : 0.10f;
    }

    private float rescueKitSpawnChance() {
        return selectedStage >= 3 ? 0.18f : 0.12f;
    }

    private float bearSpraySpawnChance() {
        return SprayTuning.spawnChance(selectedStage);
    }

    private void updateStars(float dt, float speed) {
        Iterator<Star> iterator = stars.iterator();
        while (iterator.hasNext()) {
            Star star = iterator.next();
            star.x -= speed * dt;
            star.spin += dt * 7f;
            if (circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.STAR_PLAYER_RADIUS_SCALE, star.x, star.y, star.radius * CollisionTuning.STAR_RADIUS_SCALE)) {
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
            attractPowerUp(powerUp, dt);
            if (circleHitsCircle(playerX, playerY, playerRadius * CollisionTuning.POWERUP_PLAYER_RADIUS_SCALE, powerUp.x, powerUp.y, powerUp.radius * CollisionTuning.POWERUP_RADIUS_SCALE)) {
                iterator.remove();
                activatePowerUp(powerUp);
                continue;
            }
            if (powerUp.x + powerUp.radius < -dp(28)) {
                iterator.remove();
            }
        }
    }

    private void attractPowerUp(PowerUp powerUp, float dt) {
        if (!flowActive()) {
            return;
        }
        float dx = playerX - powerUp.x;
        float dy = playerY - powerUp.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float radius = dp(FLOW_MAGNET_RADIUS_DP) + powerUp.radius;
        if (distance <= 1f || distance > radius) {
            return;
        }
        float pull = dp(FLOW_MAGNET_SPEED_DP) * dt * (1f - distance / radius);
        powerUp.x += dx / distance * pull;
        powerUp.y += dy / distance * pull;
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
        } else if ("MAP".equals(powerUp.type)) {
            gameState.addCombo();
            runTrailMaps++;
            routeMilestoneTimer = Math.max(routeMilestoneTimer, 3.4f);
            scoutTimer = Math.max(scoutTimer, 9.0f + selectedStage * 0.75f);
            int awarded = addScore(22 + selectedStage * 2, "Trail map");
            effects.spawnScorePopup("MAP +" + awarded, powerUp.x, powerUp.y - dp(12), Color.rgb(255, 246, 207));
            effects.spawnSparkBurst(powerUp.x, powerUp.y, 14, Color.rgb(255, 246, 207));
            addAuroraMeter(12f, "Trail map");
            showRunCallout("TRAIL SCOUT ACTIVE", 1.25f);
            playSound("medal");
            checkMissionProgress();
        } else if ("KIT".equals(powerUp.type)) {
            gameState.addCombo();
            runRescueKits++;
            if (gameState.lives < 3) {
                gameState.lives++;
            } else {
                gameState.shieldActive = true;
            }
            int awarded = addScore(20, "Rescue kit");
            effects.spawnScorePopup("KIT +" + awarded, powerUp.x, powerUp.y - dp(12), Color.rgb(255, 98, 84));
            effects.spawnSparkBurst(powerUp.x, powerUp.y, 16, Color.rgb(255, 98, 84));
            showRunCallout("RESCUE KIT", 1.10f);
            playSound("medal");
        } else if ("SPRAY".equals(powerUp.type)) {
            gameState.addCombo();
            bearSprayCharges = Math.min(SprayTuning.MAX_CHARGES, bearSprayCharges + (selectedStage >= 4 ? 2 : 1));
            int awarded = addScore(16, "Bear spray");
            effects.spawnScorePopup("SPRAY x" + bearSprayCharges, powerUp.x, powerUp.y - dp(12), Color.rgb(255, 166, 84));
            effects.spawnSparkBurst(powerUp.x, powerUp.y, 14, Color.rgb(255, 166, 84));
            addAuroraMeter(10f, "Bear spray pickup");
            showRunCallout("SPRAY BUTTON READY", 1.25f);
            playSound("medal");
            checkMissionProgress();
        }
    }

    private void updateVisualEffects(float dt) {
        screenShake = Math.max(0f, screenShake - dt);
        worldFlash = Math.max(0f, worldFlash - dt);
        nearMissFlash = Math.max(0f, nearMissFlash - dt);
        runCalloutTimer = Math.max(0f, runCalloutTimer - dt);
        bossWarningTimer = Math.max(0f, bossWarningTimer - dt);
        eclipseTimer = Math.max(0f, eclipseTimer - dt);
        scoutTimer = Math.max(0f, scoutTimer - dt);
        routeMilestoneTimer = Math.max(0f, routeMilestoneTimer - dt);
        effects.update(dt);
    }

    private void spawnHazard() {
        spawnHazardAt(hazardSpawnX(), selectHazardLabel());
    }

    private void spawnHazardWave() {
        EncounterCard encounter = activeEncounter;
        int count = encounter == null
                ? RushDirector.hazardWaveSize(selectedStage, gatesPassed) : encounter.hazards.length;
        float x = hazardSpawnX();
        for (int i = 0; i < count; i++) {
            String label;
            if (encounter != null) {
                label = encounter.hazards[i];
                if ("STAGE".equals(label)) label = selectHazardLabel();
            } else {
                label = selectHazardLabel();
                if (i == 1 && selectedStage == 3) label = "WOLF";
                else if (i == 1 && selectedStage == 4) label = "BEAR";
            }
            spawnHazardAt(x, label);
            x += gameplayDp(RushDirector.hazardWaveSpacingDp(i));
        }
        if (encounter != null) {
            showRunCallout(encounter.routeLabel() + " · " + encounter.id.replace('_', ' ').toUpperCase(Locale.ROOT), 0.94f);
        } else if (count > 1) {
            showRunCallout(count == 3 ? "THREAT CHAIN x3" : "COMBO THREAT", 0.82f);
        }
    }

    private void spawnHazardAt(float x, String label) {
        float radius = gameplayDp(hazardRadiusDp(label));
        float y = hazardSpawnY(label, radius);
        float speed = hazardSpeedMultiplier(label);
        hazards.add(new Hazard(x, y, radius, speed, random.nextFloat() * 4f, label, null));
        if (shouldForecastHazard(label)) {
            showRunCallout("WATCH: " + label, 0.92f);
            effects.spawnScorePopup("!", getWidth() - dp(58), Math.max(dp(96), y - radius * 1.8f), Color.rgb(255, 218, 121));
        }
    }

    private float hazardSpawnX() {
        float x = getWidth() + dp(50);
        for (Gate gate : gates) {
            if (gate.x > playerX) {
                x = Math.max(x, gate.x + gate.width + dp(HAZARD_GATE_CLEARANCE_DP));
            }
        }
        return x;
    }

    private boolean shouldForecastHazard(String label) {
        return scoutTimer > 0f
                || "BEAR".equals(label)
                || "POLAR".equals(label)
                || "AVALANCHE".equals(label)
                || "THIN ICE".equals(label)
                || "EAGLE".equals(label);
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
        if ("POLAR".equals(label)) return 34f;
        if ("BEAR".equals(label)) return 31f;
        if ("MOOSE".equals(label)) return 26f;
        if ("WOLF".equals(label)) return 18f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 14f;
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
        return lowBand;
    }

    private boolean isRoaringBear(String label) {
        return "BEAR".equals(label) || "POLAR".equals(label);
    }

    private boolean hitsGate(Gate gate) {
        gateHitRect(gate, tempRect);
        return circleHitsRect(playerX, playerY, playerRadius * CollisionTuning.PLAYER_GATE_RADIUS_SCALE, tempRect);
    }

    private void gateHitRect(Gate gate, RectF out) {
        out.set(
                gate.x + dp(CollisionTuning.GATE_HIT_INSET_X_DP),
                gateColliderTop(gate) + dp(CollisionTuning.GATE_HIT_TOP_INSET_DP),
                gate.x + gate.width - dp(CollisionTuning.GATE_HIT_INSET_X_DP),
                getGroundY());
    }

    /**
     * Top edge of the obstacle the player actually has to clear. For sprite and
     * hurdle gates this is the spawn height. For the stage 0/1 river logs the
     * visible art is a low log capped well below {@code gate.height}, so the
     * collider must follow the drawn log instead of the raw spawn height, or the
     * runner bonks an invisible wall above the log.
     */
    private float gateColliderTop(Gate gate) {
        if (selectedStage == 0 || selectedStage == 1) {
            return riverLogTop(gate);
        }
        return getGroundY() - gate.height;
    }

    private void drawSplashScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        paint.setColor(Color.argb(184, 0, 0, 0));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(11));
        canvas.drawText("TRIPPERDEE LABS", getWidth() / 2f, getHeight() * 0.29f, textPaint);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(BuildConfig.BUILD_BADGE, getWidth() / 2f, getHeight() * 0.34f, textPaint);

        textPaint.setColor(Color.WHITE);
        float titleSize = Math.min(dp(46), Math.max(dp(34), getWidth() / 8.0f));
        float wordGap;
        float youWidth;
        float rushWidth;
        float totalWidth;
        float maxTitleWidth = getWidth() - dp(50);
        do {
            textPaint.setTextSize(titleSize);
            wordGap = Math.max(dp(120), titleSize * 2.65f);
            youWidth = textPaint.measureText("YOU");
            rushWidth = textPaint.measureText("RUSH");
            totalWidth = youWidth + rushWidth + wordGap;
            if (totalWidth <= maxTitleWidth || titleSize <= dp(25)) {
                break;
            }
            titleSize -= dp(1.5f);
        } while (true);
        float baseline = getHeight() * 0.48f;
        paint.setStyle(Paint.Style.FILL);
        if (getWidth() < dp(390) || totalWidth > maxTitleWidth) {
            float firstBaseline = getHeight() * 0.445f;
            float secondBaseline = firstBaseline + titleSize * 1.18f;
            float panelWidth = Math.max(youWidth, rushWidth) + dp(50);
            float panelLeft = getWidth() / 2f - panelWidth / 2f;
            float panelRight = getWidth() / 2f + panelWidth / 2f;
            paint.setColor(Color.argb(142, 0, 0, 0));
            canvas.drawRoundRect(panelLeft, firstBaseline - titleSize * 0.88f, panelRight, secondBaseline + dp(13), dp(16), dp(16), paint);
            paint.setColor(Color.argb(185, 255, 218, 121));
            float dividerY = (firstBaseline + secondBaseline) * 0.50f - titleSize * 0.08f;
            canvas.drawRoundRect(panelLeft + dp(16), dividerY - dp(1.1f), panelRight - dp(16), dividerY + dp(1.1f), dp(1.1f), dp(1.1f), paint);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("YOU", getWidth() / 2f, firstBaseline, textPaint);
            textPaint.setColor(Color.rgb(255, 218, 121));
            canvas.drawText("RUSH", getWidth() / 2f, secondBaseline, textPaint);
            baseline = secondBaseline + titleSize * 0.90f;
        } else {
            float startX = getWidth() / 2f - totalWidth / 2f;
            textPaint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.argb(135, 0, 0, 0));
            canvas.drawRoundRect(startX - dp(20), baseline - titleSize * 0.92f, startX + totalWidth + dp(20), baseline + dp(13), dp(14), dp(14), paint);
            paint.setColor(Color.argb(185, 255, 218, 121));
            float dividerX = startX + youWidth + wordGap * 0.50f;
            canvas.drawRoundRect(dividerX - dp(1.3f), baseline - titleSize * 0.72f, dividerX + dp(1.3f), baseline + dp(4), dp(1.3f), dp(1.3f), paint);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("YOU", startX, baseline, textPaint);
            textPaint.setColor(Color.rgb(255, 218, 121));
            canvas.drawText("RUSH", startX + youWidth + wordGap, baseline, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Alaska platform runner", getWidth() / 2f, Math.max(getHeight() * 0.61f, baseline), textPaint);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("For the best playing experience, rotate your phone.", getWidth() / 2f, getHeight() * 0.73f, textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap to continue", getWidth() / 2f, getHeight() * 0.85f, textPaint);
    }

    private void drawMenuScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "YOU RUSH: ALASKA", "Upload your face. Beat local chaos.");

        drawMenuCharacterPreview(canvas);

        float y = isLandscape() ? Math.max(dp(96), getHeight() * 0.31f) : getHeight() * 0.53f;
        float x = isLandscape() ? getWidth() * 0.68f : getWidth() / 2f;
        if (isLandscape()) {
            float wideButtonWidth = dp(230);
            float mainButtonHeight = dp(38);
            float dailyHeight = dp(34);
            float buttonGap = dp(10);
            float smallButtonHeight = dp(30);
            float statsPanelTop = getHeight() - dp(42);
            float dailyY = y;
            float primaryY = dailyY + dailyHeight / 2f + buttonGap + mainButtonHeight / 2f;
            float secondaryY = primaryY + mainButtonHeight + buttonGap;
            float thirdY = secondaryY + mainButtonHeight + buttonGap;
            float maxThirdY = statsPanelTop - dp(8) - mainButtonHeight / 2f;
            if (thirdY > maxThirdY) {
                float shift = thirdY - maxThirdY;
                dailyY -= shift;
                primaryY -= shift;
                secondaryY -= shift;
                thirdY -= shift;
            }
            setButton(dailyButtonBounds, x, dailyY, wideButtonWidth, dailyHeight);
            setButton(primaryButtonBounds, x, primaryY, wideButtonWidth, mainButtonHeight);
            setButton(secondaryButtonBounds, x, secondaryY, wideButtonWidth, mainButtonHeight);
            setButton(thirdButtonBounds, x, thirdY, wideButtonWidth, mainButtonHeight);
            setButton(demoButtonBounds, getWidth() - dp(270), statsPanelTop + dp(21), dp(98), smallButtonHeight);
            setButton(debugButtonBounds, getWidth() - dp(165), statsPanelTop + dp(21), dp(98), smallButtonHeight);
            setButton(muteButtonBounds, getWidth() - dp(60), statsPanelTop + dp(21), dp(98), smallButtonHeight);
        } else {
            setButton(primaryButtonBounds, x, y, dp(230), dp(44));
            setButton(secondaryButtonBounds, x, y + dp(54), dp(230), dp(44));
            setButton(thirdButtonBounds, x, y + dp(108), dp(230), dp(44));
            setButton(dailyButtonBounds, x, y - dp(58), dp(230), dp(42));
            setButton(demoButtonBounds, x, y + dp(154), dp(230), dp(34));
            setButton(debugButtonBounds, x - dp(58), y + dp(196), dp(104), dp(34));
            setButton(muteButtonBounds, x + dp(58), y + dp(196), dp(104), dp(34));
        }

        drawDailyRushButton(canvas, dailyButtonBounds);
        drawButton(canvas, primaryButtonBounds, "Play " + STAGES[selectedStage].name);
        drawButton(canvas, secondaryButtonBounds, playerPhoto == null ? "Create Your Sprite" : "Edit Your Sprite");
        drawButton(canvas, thirdButtonBounds, "ALASKA MAP");
        drawMenuStats(canvas);
        drawSmallButton(canvas, demoButtonBounds, demoMode ? "DEMO: ON" : "COMPUTER RUN");
        drawSmallButton(canvas, muteButtonBounds, gameState.muted ? "MUTED" : "AUDIO");
    }

    private void drawMenuCharacterPreview(Canvas canvas) {
        float x = isLandscape() ? getWidth() * 0.28f : getWidth() / 2f;
        float desiredHeadY = isLandscape() ? getHeight() * 0.45f : getHeight() * 0.33f;
        float topLimit = isLandscape() ? dp(10) : dp(82);
        float bottomLimit = isLandscape() ? getHeight() - dp(8) : getHeight() * 0.50f;
        float radius = isLandscape() ? Math.min(dp(30), getHeight() * 0.19f) : dp(34);
        radius = Math.min(radius, Math.max(dp(10), (bottomLimit - topLimit) / 4.0f));
        float minHeadY = topLimit + radius;
        float maxHeadY = bottomLimit - radius * 3.30f;
        float headY = maxHeadY < minHeadY ? desiredHeadY : clamp(desiredHeadY, minHeadY, maxHeadY);
        SpriteRenderer.PlayerFrame previewFrame = playerFrame(x, headY, radius);
        spriteRenderer.drawStanding(canvas, previewFrame);
    }

    private void drawMenuStats(Canvas canvas) {
        float panelHeight = isLandscape() ? dp(42) : dp(58);
        float top = getHeight() - panelHeight;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(isLandscape() ? 190 : 178, 0, 0, 0));
        canvas.drawRect(0, top, getWidth(), getHeight(), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(isLandscape() ? dp(10.5f) : dp(13));
        textPaint.setColor(Color.WHITE);
        float statsX = isLandscape() ? getWidth() * 0.36f : getWidth() / 2f;
        canvas.drawText("Best " + bestScore + "   Tokens " + trailTokens + "   Logs " + expeditionLogs + "   Missions " + totalMissionsCompleted,
                statsX, top + (isLandscape() ? dp(16) : dp(18)), textPaint);
        textPaint.setTextSize(isLandscape() ? dp(9.5f) : dp(11));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Stages " + (unlockedStage + 1) + "/" + STAGES.length
                + "   Outfits " + unlockedOutfitCount() + "/" + OUTFIT_COLORS.length
                + "   Badges " + TrailBadgeCatalog.badgeCount(trailBadgeMask) + "/" + TrailBadgeCatalog.BADGE_COUNT,
                statsX, top + (isLandscape() ? dp(33) : dp(42)), textPaint);
    }

    private void drawDailyRushButton(Canvas canvas, RectF bounds) {
        boolean complete = dailyRushCompleteToday();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(complete ? Color.argb(246, 13, 42, 39) : Color.argb(250, 77, 219, 184));
        canvas.drawRoundRect(bounds, dp(13), dp(13), paint);
        paint.setColor(Color.argb(50, 255, 255, 255));
        canvas.drawRoundRect(bounds.left + dp(3), bounds.top + dp(3), bounds.right - dp(3), bounds.top + bounds.height() * 0.46f, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(Color.argb(220, 255, 246, 207));
        canvas.drawRoundRect(bounds, dp(13), dp(13), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(isLandscape() ? dp(8.5f) : dp(9));
        textPaint.setColor(complete ? Color.rgb(210, 232, 238) : Color.rgb(8, 18, 30));
        canvas.drawText("DAILY RUSH", bounds.centerX(), bounds.top + (isLandscape() ? dp(12) : dp(15)), textPaint);
        textPaint.setTextSize(isLandscape() ? dp(10) : dp(10.5f));
        textPaint.setColor(complete ? Color.WHITE : Color.rgb(8, 18, 30));
        canvas.drawText(dailyRushLine(), bounds.centerX(), bounds.top + (isLandscape() ? dp(26) : dp(31)), textPaint);
    }

    private void drawMapScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "ALASKA MAP", "Pick a stage. Beat bosses to unlock more.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        setButton(passportButtonBounds, getWidth() - dp(64), isLandscape() ? dp(34) : dp(52), dp(100), isLandscape() ? dp(30) : dp(36));
        drawSmallButton(canvas, passportButtonBounds, "PASSPORT");

        if (unlockedStage >= 4) {
            setButton(godotButtonBounds, getWidth() / 2f, getHeight() - dp(64), dp(180), dp(40));
            paint.setColor(Color.argb(220, 77, 219, 184));
            canvas.drawRoundRect(godotButtonBounds, dp(12), dp(12), paint);
            textPaint.setColor(Color.rgb(12, 18, 30));
            textPaint.setTextSize(dp(13));
            canvas.drawText("ENTER GLOBAL EXPEDITION", godotButtonBounds.centerX(), godotButtonBounds.centerY() + dp(5), textPaint);
        }

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

        if (mapNoticeTimer > 0f && mapNotice.length() > 0) {
            float pct = Math.min(1f, mapNoticeTimer / 2.2f);
            float width = Math.min(getWidth() - dp(44), Math.max(dp(230), mapNotice.length() * dp(7.2f)));
            float height = dp(30);
            float left = (getWidth() - width) / 2f;
            float top = isLandscape() ? getHeight() - dp(68) : getHeight() - dp(86);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(198 * pct), 8, 18, 30));
            canvas.drawRoundRect(left, top, left + width, top + height, dp(9), dp(9), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.2f));
            paint.setColor(Color.argb(Math.round(230 * pct), 255, 218, 121));
            canvas.drawRoundRect(left, top, left + width, top + height, dp(9), dp(9), paint);
            paint.setStyle(Paint.Style.FILL);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(10.5f));
            textPaint.setColor(Color.argb(Math.round(255 * pct), 255, 246, 207));
            canvas.drawText(mapNotice, getWidth() / 2f, top + dp(20), textPaint);
        }

        StageConfig selected = STAGES[selectedStage];
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(isLandscape() ? dp(9.5f) : dp(11));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Selected: " + stageActionVerb(selectedStage).toLowerCase(Locale.ROOT) + " " + selected.obstacleName + " · stun " + selected.hazardLabel
                + " · boss " + selected.bossName, getWidth() / 2f, getHeight() - dp(24), textPaint);
    }

    private void drawCustomizeScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "CREATE YOUR SPRITE", "Pick a photo, preview your runner, then play.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        if (isLandscape()) {
            drawCharacterPreview(canvas, getWidth() * 0.30f, getHeight() * 0.45f, dp(40));
        } else {
            drawCharacterPreview(canvas, getWidth() / 2f, getHeight() * 0.33f, dp(40));
        }

        float y = isLandscape() ? getHeight() * 0.29f : getHeight() * 0.46f;
        float x = isLandscape() ? getWidth() * 0.68f : getWidth() / 2f;
        float buttonGap = isLandscape() ? dp(44) : dp(54);
        setButton(photoButtonBounds, x, y, dp(226), isLandscape() ? dp(40) : dp(48));
        setButton(resetPhotoButtonBounds, x, y + buttonGap, dp(226), isLandscape() ? dp(38) : dp(44));
        setButton(seasonButtonBounds, x, y + buttonGap * 2f, dp(226), isLandscape() ? dp(40) : dp(48));
        setButton(outfitButtonBounds, x, y + buttonGap * 3f, dp(226), isLandscape() ? dp(38) : dp(44));
        setButton(bodyStyleButtonBounds, x, y + buttonGap * 4f, dp(226), isLandscape() ? dp(38) : dp(44));

        drawButton(canvas, photoButtonBounds, playerPhoto == null ? "SELECT PLAYER PHOTO" : "CHANGE PLAYER PHOTO");
        drawButton(canvas, resetPhotoButtonBounds, "RESET TO DEFAULT RUNNER");
        drawButton(canvas, seasonButtonBounds, "SEASON: " + SEASONS[selectedSeason]);
        drawButton(canvas, outfitButtonBounds, outfitButtonLabel());
        drawOutfitSwatch(canvas, outfitButtonBounds);
        drawButton(canvas, bodyStyleButtonBounds, "BODY: " + BODY_STYLE_NAMES[selectedBodyStyle]);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(playerPhoto == null ? Color.rgb(255, 218, 121) : Color.WHITE);
        canvas.drawText(playerPhoto == null ? "Default runner is active." : "Photo sprite ready for this run.", x, y + buttonGap * 4f + dp(42), textPaint);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.rgb(220, 235, 239));
        canvas.drawText("Tokens " + trailTokens + "   Outfit " + outfitStatusLabel(), x, y + buttonGap * 4f + dp(62), textPaint);
    }

    private void drawWorld(Canvas canvas) {
        int saved = canvas.save();

        // Camera juice: subtle lean based on horizontal movement
        float lean = 0f;
        if (leftPressed) lean = -1.2f;
        if (rightPressed) lean = 1.2f;
        if (Math.abs(lean) > 0.1f) {
            canvas.rotate(lean, getWidth() / 2f, getHeight());
        }

        if (screenShake > 0f) {
            float power = screenShake / 0.18f;
            float shakeX = (float) Math.sin(spriteClock * 19.0f) * dp(4.0f) * power;
            float shakeY = (float) Math.cos(spriteClock * 23.0f) * dp(2.4f) * power;
            canvas.translate(shakeX, shakeY);
        }

        drawAlaskaBackdrop(canvas);
        weather.draw(canvas, selectedStage, selectedSeason, flowActive());
        drawGround(canvas, getWidth());

        if (bossPhaseThreeAnnounced && selectedStage == 4) {
            // Draw a heavy blizzard overlay during final boss phase
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(140 + 40 * (float) Math.sin(spriteClock * 12f)), 255, 255, 255));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
        drawChaseBearWarning(canvas);
        drawScoutWarnings(canvas);

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }
        for (RoutePlatform platform : routePlatforms) {
            drawRoutePlatform(canvas, platform);
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
            if (attack.type != ATTACK_LASER) {
                drawBossAttack(canvas, attack);
            }
        }
        if (bossActive) {
            drawBoss(canvas);
        }
        // The eclipse dims the world, but energy attacks must remain luminous
        // and readable. Paint it before lasers and the runner instead of over
        // the completed frame.
        drawEclipseOverlay(canvas);
        for (BossAttack attack : bossAttacks) {
            if (attack.type == ATTACK_LASER) {
                drawBossAttack(canvas, attack);
            }
        }
        drawDebugObjectNumbers(canvas);

        drawPlayerLaneGuide(canvas);
        drawAuroraRushTrail(canvas);
        drawAuroraFocusTrail(canvas);
        effects.drawParticles(canvas);
        drawFlowAura(canvas);
        drawShieldAura(canvas);
        drawRespawnGraceAura(canvas);
        drawChaseBear(canvas);
        float visualRadius = playerVisualRadius();
        float headDrawY = playerHeadDrawY();
        drawCharacter(canvas, playerX, headDrawY, visualRadius);
        drawBearSprayEffect(canvas);
        drawNearMissFlash(canvas);
        effects.drawScorePopups(canvas);
        drawWorldFlash(canvas);
        canvas.restoreToCount(saved);
    }

    private void drawEclipseOverlay(Canvas canvas) {
        if (eclipseTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, eclipseTimer / 4.2f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(188 * pct), 4, 8, 18));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(Color.argb(Math.round(145 * pct), 77, 219, 184));
        canvas.drawCircle(getWidth() * 0.50f, dp(62), dp(24 + 10 * pct), paint);
        paint.setColor(Color.argb(Math.round(230 * pct), 8, 18, 30));
        canvas.drawCircle(getWidth() * 0.50f + dp(7), dp(59), dp(24 + 10 * pct), paint);
    }

    private void drawScoutWarnings(Canvas canvas) {
        if (scoutTimer <= 0f || bossActive) {
            return;
        }
        float nearestX = Float.MAX_VALUE;
        String label = "";
        for (Gate gate : gates) {
            if (gate.x > playerX && gate.x < nearestX) {
                nearestX = gate.x;
                label = obstacleHudName(selectedStage);
            }
        }
        for (Hazard hazard : hazards) {
            if (hazard.x > playerX && hazard.x < nearestX) {
                nearestX = hazard.x;
                label = hazard.label;
            }
        }
        if (label.length() == 0 || nearestX > getWidth() + dp(160)) {
            return;
        }

        float pct = Math.max(0f, Math.min(1f, (nearestX - playerX) / Math.max(1f, getWidth() - playerX)));
        float markerX = playerX + (getWidth() - playerX - dp(34)) * pct;
        float markerY = getGroundY() - dp(30);
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 11f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(80 + Math.round(50 * pulse), 255, 218, 121));
        canvas.drawCircle(markerX, markerY, dp(18), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(180 + Math.round(55 * pulse), 255, 246, 207));
        canvas.drawCircle(markerX, markerY, dp(18), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 246, 207));
        PathCompat.triangle(canvas, paint, markerX, markerY - dp(10), markerX - dp(8), markerY + dp(6), markerX + dp(8), markerY + dp(6));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8.5f));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, markerX, markerY - dp(23), textPaint);
    }

    private void drawAlaskaBackdrop(Canvas canvas) {
        boolean dark = selectedSeason == SEASON_DARKNESS || STAGES[selectedStage].season == SEASON_DARKNESS;
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage >= 3;
        ensureBackdropCache(dark, winter);
        if (backdropCache != null) {
            canvas.drawBitmap(backdropCache, 0, 0, null);
        } else {
            drawStaticBackdrop(canvas, dark, winter);
        }

        drawMidnightSunBossSkyCorrection(canvas, dark, winter);
        drawParallaxScenery(canvas, dark, winter);
    }

    private void drawMidnightSunBossSkyCorrection(Canvas canvas, boolean dark, boolean winter) {
        if (!bossActive || selectedStage != 0 || dark || winter) {
            return;
        }
        float oldSunX = getWidth() * 0.555f;
        float oldSunY = getHeight() * 0.205f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(112, 246, 204, 122));
        canvas.drawCircle(oldSunX, oldSunY, dp(27), paint);
        paint.setColor(Color.argb(86, 170, 215, 206));
        canvas.drawOval(oldSunX - dp(58), oldSunY - dp(22), oldSunX + dp(72), oldSunY + dp(26), paint);
        paint.setColor(Color.argb(118, 255, 246, 207));
        canvas.drawCircle(getWidth() * 0.82f, dp(42), dp(10), paint);
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
        boolean winter = selectedSeason == SEASON_WINTER || STAGES[selectedStage].season == SEASON_WINTER || selectedStage >= 3;
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
        if (selectedStage == 0 || selectedStage == 1) {
            drawRiverLogGate(canvas, gate);
            return;
        }
        if (obstacleRenderer.drawSpriteGate(canvas, gate.x, gate.height, gate.width, selectedStage, STAGES[selectedStage].obstacleName, getGroundY())) {
            return;
        }
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

        int darkColor = gateDarkColor();
        int midColor = gateMidColor();
        int lightColor = gateLightColor();

        paint.setColor(darkColor);
        canvas.drawRoundRect(leftPost - dp(2), railTop, leftPost + postWidth + dp(2), ground, dp(5), dp(5), paint);
        canvas.drawRoundRect(rightPost - dp(2), railTop, rightPost + postWidth + dp(2), ground, dp(5), dp(5), paint);
        canvas.drawRoundRect(gate.x - dp(2), railTop - dp(2), gate.x + gate.width + dp(2), railBottom + dp(2), dp(7), dp(7), paint);

        paint.setColor(midColor);
        canvas.drawRoundRect(leftPost, railTop + dp(2), leftPost + postWidth, ground, dp(4), dp(4), paint);
        canvas.drawRoundRect(rightPost, railTop + dp(2), rightPost + postWidth, ground, dp(4), dp(4), paint);
        canvas.drawRoundRect(gate.x, railTop, gate.x + gate.width, railBottom, dp(6), dp(6), paint);

        paint.setColor(lightColor);
        canvas.drawRoundRect(gate.x + dp(4), railTop + dp(2), gate.x + gate.width - dp(4), railTop + railHeight * 0.46f, dp(4), dp(4), paint);

        paint.setColor(darkColor);
        canvas.drawRoundRect(gate.x + dp(6), ground - dp(7), gate.x + postWidth + dp(8), ground, dp(3), dp(3), paint);
        canvas.drawRoundRect(gate.x + gate.width - postWidth - dp(8), ground - dp(7), gate.x + gate.width - dp(6), ground, dp(3), dp(3), paint);

        drawStageObstacleDetails(canvas, gate, top, railBottom, ground);

        drawObstacleNameplate(canvas, gate, top);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawRoutePlatform(Canvas canvas, RoutePlatform platform) {
        if (platform.broken) return;
        float bottom = platform.y + dp(13);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(105, 0, 0, 0));
        canvas.drawOval(platform.x + dp(5), bottom - dp(2), platform.x + platform.width - dp(5), bottom + dp(7), paint);
        paint.setColor(platform.brittle ? Color.rgb(125, 207, 226)
                : platform.moving ? Color.rgb(77, 219, 184) : Color.rgb(232, 244, 247));
        canvas.drawRoundRect(platform.x, platform.y, platform.x + platform.width, bottom, dp(7), dp(7), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(platform.brittle ? 2.2f : 1.4f));
        paint.setColor(platform.brittle ? Color.WHITE : Color.rgb(35, 86, 108));
        canvas.drawRoundRect(platform.x, platform.y, platform.x + platform.width, bottom, dp(7), dp(7), paint);
        if (platform.brittle) {
            float mid = platform.x + platform.width * 0.52f;
            canvas.drawLine(mid - dp(14), platform.y + dp(2), mid, platform.y + dp(10), paint);
            canvas.drawLine(mid, platform.y + dp(10), mid + dp(18), platform.y + dp(3), paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private int gateDarkColor() {
        if (selectedStage == 3 || selectedStage == 4) return Color.rgb(74, 96, 110);
        if (selectedStage == 1) return Color.rgb(64, 45, 32);
        return Color.rgb(52, 35, 24);
    }

    private int gateMidColor() {
        if (selectedStage == 4) return Color.rgb(210, 225, 232);
        if (selectedStage == 3) return Color.rgb(132, 174, 194);
        if (selectedStage == 1) return Color.rgb(164, 96, 48);
        return Color.rgb(134, 78, 38);
    }

    private int gateLightColor() {
        if (selectedStage == 4) return Color.rgb(248, 252, 253);
        if (selectedStage == 3) return Color.rgb(210, 232, 238);
        if (selectedStage == 1) return Color.rgb(226, 169, 83);
        return Color.rgb(226, 169, 83);
    }

    private void drawStageObstacleDetails(Canvas canvas, Gate gate, float top, float railBottom, float ground) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        if (selectedStage == 2) {
            paint.setStrokeWidth(dp(3));
            paint.setColor(Color.rgb(233, 218, 181));
            canvas.drawLine(gate.x + dp(8), railBottom + dp(8), gate.x + gate.width - dp(8), ground - dp(13), paint);
            canvas.drawLine(gate.x + gate.width - dp(8), railBottom + dp(8), gate.x + dp(8), ground - dp(13), paint);
            paint.setStrokeWidth(dp(2.2f));
            canvas.drawLine(gate.x + gate.width * 0.18f, railBottom + dp(2), gate.x + gate.width * 0.02f, top + dp(4), paint);
            canvas.drawLine(gate.x + gate.width * 0.82f, railBottom + dp(2), gate.x + gate.width * 0.98f, top + dp(4), paint);
            return;
        }
        if (selectedStage == 3) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(248, 252, 253));
            PathCompat.triangle(canvas, paint, gate.x - dp(2), ground, gate.x + gate.width * 0.32f, top + dp(3), gate.x + gate.width * 0.62f, ground);
            PathCompat.triangle(canvas, paint, gate.x + gate.width * 0.28f, ground, gate.x + gate.width * 0.72f, top + gate.height * 0.22f, gate.x + gate.width + dp(3), ground);
            paint.setColor(Color.rgb(190, 207, 216));
            PathCompat.triangle(canvas, paint, gate.x + gate.width * 0.28f, ground - dp(2), gate.x + gate.width * 0.55f, top + gate.height * 0.30f, gate.x + gate.width * 0.78f, ground - dp(2));
            paint.setColor(Color.argb(150, 132, 213, 232));
            canvas.drawRoundRect(gate.x - dp(5), ground - dp(8), gate.x + gate.width + dp(5), ground - dp(2), dp(5), dp(5), paint);
            return;
        }
        if (selectedStage == 4) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(248, 252, 253));
            canvas.drawOval(gate.x - dp(2), ground - gate.height * 0.33f, gate.x + gate.width + dp(2), ground + dp(2), paint);
            paint.setColor(Color.rgb(190, 207, 216));
            canvas.drawOval(gate.x + gate.width * 0.16f, ground - gate.height * 0.45f, gate.x + gate.width * 0.62f, ground - gate.height * 0.16f, paint);
            return;
        }
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.rgb(233, 218, 181));
        canvas.drawLine(gate.x + dp(8), railBottom + dp(8), gate.x + gate.width - dp(8), ground - dp(13), paint);
        canvas.drawLine(gate.x + gate.width - dp(8), railBottom + dp(8), gate.x + dp(8), ground - dp(13), paint);
    }

    private void drawRiverLogGate(Canvas canvas, Gate gate) {
        float ground = getGroundY();
        float top = ground - gate.height;
        float waterTop = ground - dp(16);
        float logHeight = riverLogHeight(gate);
        float logTop = riverLogTop(gate);
        float logBottom = logTop + logHeight;
        float logLeft = gate.x - dp(3);
        float logRight = gate.x + gate.width + dp(3);

        paint.setStyle(Paint.Style.FILL);

        // Only draw water ripples for the actual river stage (Salmon Rush).
        // Midnight Sun driftwood is on dry ground.
        if (selectedStage == 1) {
            paint.setColor(Color.argb(92, 37, 96, 113));
            canvas.drawOval(gate.x - dp(16), waterTop - dp(7), gate.x + gate.width + dp(18), ground + dp(5), paint);
            paint.setColor(Color.argb(138, 132, 213, 232));
            canvas.drawRoundRect(gate.x - dp(9), waterTop, gate.x + gate.width + dp(10), waterTop + dp(8), dp(6), dp(6), paint);
            paint.setColor(Color.argb(160, 210, 232, 238));
            canvas.drawRoundRect(gate.x + dp(9), waterTop + dp(3), gate.x + gate.width * 0.62f, waterTop + dp(5), dp(2), dp(2), paint);
        }

        Drawable logSprite = assets.obstacleRiverLog();
        // Only draw the procedural oval shadow if the sprite is missing. High-quality
        // sprites usually have their own soft shadow baked in or handled differently.
        if (logSprite == null) {
            paint.setColor(Color.argb(128, 0, 0, 0));
            canvas.drawOval(logLeft - dp(4), logBottom - dp(2), logRight + dp(5), logBottom + dp(10), paint);
        }

        boolean driftwood = selectedStage == 0;
        int barkDark = driftwood ? Color.rgb(86, 76, 60) : Color.rgb(62, 35, 22);
        int barkMid = driftwood ? Color.rgb(149, 124, 88) : Color.rgb(126, 70, 34);
        int barkLight = driftwood ? Color.rgb(210, 183, 128) : Color.rgb(194, 112, 50);
        int ringLight = driftwood ? Color.rgb(230, 211, 160) : Color.rgb(226, 169, 83);
        int ringDark = driftwood ? Color.rgb(95, 82, 61) : Color.rgb(82, 54, 33);

        if (logSprite != null) {
            // Draw the raster sprite. We shift it down slightly (logBottom + dp(11))
            // so any baked-in shadow sits on the ground. For driftwood, we add an
            // extra nudge to ensure the log art isn't hovering.
            float extraNudge = driftwood ? dp(4.5f) : 0;
            drawDrawable(canvas, logSprite, logLeft - dp(10), logTop - dp(10) + extraNudge, logRight + dp(8), logBottom + dp(11) + extraNudge);
            drawRiverLogTarget(canvas, logLeft, logRight, logTop, logHeight);
            drawObstacleNameplate(canvas, gate, top);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
            return;
        }

        Path logPath = new Path();
        logPath.moveTo(logLeft + dp(5), logTop + logHeight * 0.25f);
        logPath.cubicTo(logLeft + logHeight * 0.9f, logTop - dp(5), logRight - logHeight * 0.8f, logTop - dp(3), logRight - dp(4), logTop + logHeight * 0.28f);
        logPath.cubicTo(logRight + dp(4), logTop + logHeight * 0.58f, logRight - logHeight * 0.42f, logBottom + dp(4), logRight - logHeight * 1.18f, logBottom - dp(1));
        logPath.lineTo(logLeft + logHeight * 0.58f, logBottom - dp(4));
        logPath.cubicTo(logLeft - dp(7), logBottom - dp(5), logLeft - dp(8), logTop + logHeight * 0.56f, logLeft + dp(5), logTop + logHeight * 0.25f);
        logPath.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(barkDark);
        canvas.drawPath(logPath, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(2.2f));
        paint.setColor(Color.argb(210, 38, 24, 16));
        canvas.drawPath(logPath, paint);
        paint.setStyle(Paint.Style.FILL);

        Path highlightPath = new Path();
        highlightPath.moveTo(logLeft + logHeight * 0.55f, logTop + dp(4));
        highlightPath.cubicTo(logLeft + logHeight * 1.75f, logTop, logRight - logHeight * 0.82f, logTop + dp(2), logRight - dp(9), logTop + logHeight * 0.32f);
        highlightPath.cubicTo(logRight - logHeight * 1.25f, logTop + logHeight * 0.30f, logLeft + logHeight * 1.1f, logTop + logHeight * 0.22f, logLeft + logHeight * 0.55f, logTop + dp(4));
        paint.setColor(barkLight);
        canvas.drawPath(highlightPath, paint);

        paint.setColor(barkMid);
        canvas.drawOval(logLeft - dp(1), logTop + dp(1), logLeft + logHeight * 1.05f, logBottom - dp(1), paint);
        paint.setColor(ringLight);
        canvas.drawOval(logLeft + dp(4), logTop + dp(5), logLeft + logHeight - dp(5), logBottom - dp(5), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(ringDark);
        canvas.drawOval(logLeft + dp(7), logTop + dp(7), logLeft + logHeight - dp(8), logBottom - dp(7), paint);
        canvas.drawOval(logLeft + dp(11), logTop + dp(10), logLeft + logHeight - dp(12), logBottom - dp(10), paint);

        paint.setStrokeWidth(dp(driftwood ? 1.35f : 1.7f));
        paint.setColor(driftwood ? Color.rgb(103, 87, 62) : Color.rgb(78, 45, 27));
        for (float x = logLeft + logHeight + dp(9); x < logRight - dp(10); x += dp(19)) {
            float wobble = (float) Math.sin((x + gate.x) * 0.037f) * dp(2.6f);
            canvas.drawLine(x, logTop + dp(5) + wobble, x + dp(8), logBottom - dp(6) - wobble * 0.5f, paint);
        }
        canvas.drawLine(logLeft + logHeight * 1.35f, logBottom - dp(6), logRight - dp(18), logBottom - dp(4), paint);
        canvas.drawLine(logLeft + logHeight * 1.5f, logTop + dp(8), logRight - dp(30), logTop + dp(6), paint);

        paint.setStrokeWidth(dp(2.4f));
        paint.setColor(driftwood ? Color.rgb(118, 99, 70) : Color.rgb(72, 42, 25));
        canvas.drawLine(logRight - dp(28), logTop + logHeight * 0.22f, logRight - dp(14), logTop - dp(5), paint);
        if (driftwood) {
            canvas.drawLine(logLeft + logHeight * 1.35f, logTop + logHeight * 0.72f, logLeft + logHeight * 1.08f, logBottom + dp(7), paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(driftwood ? 86 : 142, 248, 252, 253));
        canvas.drawRoundRect(logLeft + dp(18), logBottom - dp(2), logRight - dp(14), logBottom + dp(2), dp(3), dp(3), paint);

        if (!driftwood) {
            drawRiverLogTarget(canvas, logLeft, logRight, logTop, logHeight);
        }

        drawObstacleNameplate(canvas, gate, top);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawRiverLogTarget(Canvas canvas, float logLeft, float logRight, float logTop, float logHeight) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(Color.argb(210, 255, 246, 207));
        float targetX = logLeft + (logRight - logLeft) * 0.70f;
        float targetY = logTop + logHeight * 0.48f;
        canvas.drawCircle(targetX, targetY, dp(4.7f), paint);
        canvas.drawLine(targetX - dp(6.5f), targetY, targetX + dp(6.5f), targetY, paint);
        canvas.drawLine(targetX, targetY - dp(5.2f), targetX, targetY + dp(5.2f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private float riverLogHeight(Gate gate) {
        return Math.max(dp(18), Math.min(dp(34), gate.height * 0.34f));
    }

    private float riverLogTop(Gate gate) {
        // Base sits exactly on the ground line so logs read at the same level as
        // icebergs, snowbanks, and the other grounded obstacles.
        return getGroundY() - riverLogHeight(gate);
    }

    private void drawObstacleNameplate(Canvas canvas, Gate gate, float top) {
        String label = STAGES[selectedStage].obstacleName;
        float width = Math.min(dp(128), Math.max(dp(54), label.length() * dp(5.8f)));
        float left = gate.x + gate.width / 2f - width / 2f;
        float plateTop = Math.max(dp(82), top - dp(22));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 8, 18, 30));
        canvas.drawRoundRect(left, plateTop, left + width, plateTop + dp(17), dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(180, 255, 218, 121));
        canvas.drawRoundRect(left, plateTop, left + width, plateTop + dp(17), dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(label.length() > 14 ? 7.2f : 8.2f));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText(label, gate.x + gate.width / 2f, plateTop + dp(12), textPaint);
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
        /*
         * Drawing code is a decision tree. We try the nicest art first:
         * animated sprite sheet, then drawable art, then special hand-drawn
         * shapes, and finally a simple circle fallback. That fallback helps the
         * game keep working even if an art asset is missing.
         */
        Bitmap sheet = sheetForHazard(hazard.label);
        drawHazardIntent(canvas, hazard);
        float xRadius = hazard.radius * hazardHorizontalScale(hazard.label);
        float yRadius = hazard.radius * hazardVerticalScale(hazard.label);
        /*
         * A jumping wolf should look airborne, but its shadow should stay on
         * the ground. Drawing the shadow at baseY sells the jump.
         */
        float shadowY = "WOLF".equals(hazard.label) ? hazard.baseY : hazard.y;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(85, 0, 0, 0));
        canvas.drawOval(hazard.x - xRadius * 0.72f, shadowY + yRadius * 0.66f, hazard.x + xRadius * 0.72f, shadowY + yRadius * 0.88f, paint);

        drawHazardMotionAccent(canvas, hazard, xRadius, yRadius);

        Bitmap roarSprite = roarSpriteForHazard(hazard.label);
        if (hazard.roaring && roarSprite != null) {
            drawRoarHazardSprite(canvas, hazard, roarSprite, yRadius);
        } else if (sheet != null) {
            drawAnimatedHazardSheet(canvas, hazard, sheet, yRadius);
        } else if (hazard.drawable != null) {
            drawDrawable(canvas, hazard.drawable, hazard.x - xRadius, hazard.y - yRadius, hazard.x + xRadius, hazard.y + yRadius);
        } else if ("ICE SPIKE".equals(hazard.label)) {
            drawIceSpikeHazard(canvas, hazard);
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

    private void drawHazardIntent(Canvas canvas, Hazard hazard) {
        if (hazard.behaviorState != 1) return;
        float pulse = 0.55f + 0.45f * (float) Math.sin(hazard.age * 18f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.2f));
        paint.setColor(Color.argb(Math.round(150 + pulse * 95), 255, 98, 84));
        canvas.drawCircle(hazard.x, hazard.y, hazard.radius * (1.45f + pulse * 0.12f), paint);
        if ("EAGLE".equals(hazard.label) || "DARK".equals(hazard.label)) {
            paint.setStrokeWidth(dp(1.5f));
            paint.setColor(Color.argb(190, 255, 218, 121));
            canvas.drawLine(hazard.x, hazard.y, playerX, hazard.targetY, paint);
        } else {
            paint.setColor(Color.argb(190, 255, 218, 121));
            canvas.drawLine(hazard.x - hazard.radius * 1.5f, getGroundY() - dp(5),
                    playerX, getGroundY() - dp(5), paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawChaseBearWarning(Canvas canvas) {
        if (!chaseBearActive) {
            return;
        }
        float pct = Math.min(1f, chaseBearTimer / CHASE_BEAR_SECONDS);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(62 + 58 * pct), 255, 98, 84));
        canvas.drawRect(0, 0, dp(7), getHeight(), paint);
        paint.setColor(Color.argb(Math.round(34 + 44 * pct), 255, 166, 84));
        canvas.drawRect(dp(7), 0, dp(20), getHeight(), paint);
    }

    private void drawChaseBear(Canvas canvas) {
        if (!chaseBearActive) {
            return;
        }
        float xRadius = chaseBearRadius * hazardHorizontalScale("BEAR");
        float yRadius = chaseBearRadius * hazardVerticalScale("BEAR");
        float phase = chaseBearPhase;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(110, 0, 0, 0));
        canvas.drawOval(chaseBearX - xRadius * 0.72f, getGroundY() - dp(10), chaseBearX + xRadius * 0.72f, getGroundY() + dp(8), paint);
        paint.setColor(Color.argb(100, 235, 245, 248));
        for (int i = 0; i < 3; i++) {
            float left = chaseBearX - xRadius * (0.94f + i * 0.22f);
            float top = getGroundY() - dp(18 + i * 8);
            canvas.drawOval(left, top, left + xRadius * 0.52f, top + dp(6), paint);
        }

        Bitmap sheet = assets.bearWalkSheet();
        if (sheet != null) {
            int frame = Math.floorMod((int) (phase * (hazardAnimationRate("BEAR") + 1.0f)), SPRITE_SHEET_FRAMES);
            int saved = canvas.save();
            canvas.scale(-1f, 1f, chaseBearX, chaseBearY);
            drawSpriteSheetFrame(canvas, sheet, SPRITE_SHEET_FRAMES, frame, chaseBearX, chaseBearY, yRadius * 2.52f, 0f);
            canvas.restoreToCount(saved);
        } else {
            paint.setColor(Color.rgb(126, 70, 34));
            canvas.drawOval(chaseBearX - xRadius, chaseBearY - yRadius, chaseBearX + xRadius, chaseBearY + yRadius, paint);
        }

        float badgeWidth = dp(62);
        float badgeTop = chaseBearY + yRadius + dp(5);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(205, 82, 18, 24));
        canvas.drawRoundRect(chaseBearX - badgeWidth / 2f, badgeTop, chaseBearX + badgeWidth / 2f, badgeTop + dp(16), dp(6), dp(6), paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8.5f));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText("CHASE", chaseBearX, badgeTop + dp(11.5f), textPaint);
    }

    private void drawIceSpikeHazard(Canvas canvas, Hazard hazard) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 132, 213, 232));
        Path spike = new Path();
        spike.moveTo(hazard.x, hazard.y + hazard.radius);
        spike.lineTo(hazard.x - hazard.radius * 0.5f, hazard.y - hazard.radius);
        spike.lineTo(hazard.x + hazard.radius * 0.5f, hazard.y - hazard.radius);
        spike.close();
        canvas.drawPath(spike, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(Color.WHITE);
        canvas.drawPath(spike, paint);
        paint.setStyle(Paint.Style.FILL);
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
        } else if ("ICE SPIKE".equals(hazard.label)) {
            drawIceSpikeHazard(canvas, hazard);
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
        int inset = Math.min(ROAR_SPRITE_SOURCE_INSET_PX, Math.min(sprite.getWidth(), sprite.getHeight()) / 8);
        spriteSourceRect.set(inset, inset, sprite.getWidth() - inset, sprite.getHeight() - inset);
        float height = yRadius * ("POLAR".equals(hazard.label) ? 3.75f : 3.58f);
        float width = height * (spriteSourceRect.width() / (float) spriteSourceRect.height());
        float bottom = getGroundY() + dp(1);
        float shake = (float) Math.sin(hazardVisualPhase(hazard) * 13.0f) * dp(0.7f);
        tempRect.set(hazard.x - width * 0.50f + shake, bottom - height, hazard.x + width * 0.50f + shake, bottom);
        canvas.drawBitmap(sprite, spriteSourceRect, tempRect, spriteBitmapPaint);
    }

    private float hazardHorizontalScale(String label) {
        if ("MOOSE".equals(label)) return 1.70f;
        if ("BEAR".equals(label)) return 1.86f;
        if ("POLAR".equals(label)) return 1.94f;
        if ("WOLF".equals(label)) return 1.74f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 1.42f;
        if ("SALMON".equals(label)) return 1.35f;
        if ("AVALANCHE".equals(label)) return 1.38f;
        if ("THIN ICE".equals(label)) return 1.85f;
        return 1.0f;
    }

    private float hazardVerticalScale(String label) {
        if ("MOOSE".equals(label)) return 1.08f;
        if ("BEAR".equals(label)) return 1.16f;
        if ("POLAR".equals(label)) return 1.12f;
        if ("WOLF".equals(label)) return 0.84f;
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 0.88f;
        if ("SALMON".equals(label)) return 0.80f;
        if ("AVALANCHE".equals(label)) return 1.00f;
        if ("THIN ICE".equals(label)) return 0.42f;
        return 1.0f;
    }

    private void drawAnimatedHazardSheet(Canvas canvas, Hazard hazard, Bitmap sheet, float yRadius) {
        String label = hazard.label;
        float phase = hazardVisualPhase(hazard);
        float rate = hazardAnimationRate(label);

        // Ground creatures must plant their feet on the ground line.
        boolean groundAnchored = !("SALMON".equals(label) || "EAGLE".equals(label) || "DARK".equals(label));

        float y = hazard.y;
        if (hazard.roaring) {
            y = getGroundY() - yRadius * 1.50f;
        } else if (groundAnchored) {
            float groundBottom = getGroundY() + dp(1);
            float lift = hazard.y - hazard.baseY;
            float height = yRadius * 2.35f; // Estimated height for anchoring
            y = groundBottom + lift - height * 0.5f;
        }

        spriteRenderer.drawAnimatedHazard(canvas, sheet, label, hazard.x, y, yRadius, phase, rate, hazard.roaring);
    }

    private float hazardAnimationRate(String label) {
        if ("EAGLE".equals(label) || "DARK".equals(label)) return 1.75f;
        if ("BEAR".equals(label) || "POLAR".equals(label)) return 2.35f;
        if ("MOOSE".equals(label)) return 2.55f;
        if ("WOLF".equals(label)) return 3.65f;
        if ("SALMON".equals(label)) return 2.70f;
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
        boolean map = "MAP".equals(powerUp.type);
        boolean kit = "KIT".equals(powerUp.type);
        boolean spray = "SPRAY".equals(powerUp.type);
        paint.setColor(cache ? Color.argb(82, 255, 218, 121)
                : focus ? Color.argb(90, 77, 219, 184)
                : map ? Color.argb(86, 255, 246, 207)
                : kit ? Color.argb(88, 255, 98, 84)
                : spray ? Color.argb(86, 255, 166, 84)
                : Color.argb(80, 132, 213, 232));
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
        } else if (map) {
            paint.setColor(Color.rgb(255, 246, 207));
            canvas.drawRoundRect(x - r * 1.0f, y - r * 0.70f, x + r * 1.0f, y + r * 0.70f, r * 0.16f, r * 0.16f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.4f));
            paint.setColor(Color.rgb(23, 64, 44));
            canvas.drawLine(x - r * 0.45f, y - r * 0.62f, x - r * 0.45f, y + r * 0.62f, paint);
            canvas.drawLine(x + r * 0.20f, y - r * 0.62f, x + r * 0.20f, y + r * 0.62f, paint);
            canvas.drawLine(x - r * 0.78f, y + r * 0.15f, x + r * 0.74f, y - r * 0.25f, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (kit) {
            paint.setColor(Color.rgb(255, 98, 84));
            canvas.drawRoundRect(x - r * 0.92f, y - r * 0.70f, x + r * 0.92f, y + r * 0.70f, r * 0.18f, r * 0.18f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(x - r * 0.18f, y - r * 0.52f, x + r * 0.18f, y + r * 0.52f, r * 0.08f, r * 0.08f, paint);
            canvas.drawRoundRect(x - r * 0.52f, y - r * 0.18f, x + r * 0.52f, y + r * 0.18f, r * 0.08f, r * 0.08f, paint);
        } else if (spray) {
            paint.setColor(Color.rgb(255, 166, 84));
            canvas.drawRoundRect(x - r * 0.46f, y - r * 0.92f, x + r * 0.48f, y + r * 0.88f, r * 0.18f, r * 0.18f, paint);
            paint.setColor(Color.rgb(255, 246, 207));
            canvas.drawRoundRect(x - r * 0.34f, y - r * 0.50f, x + r * 0.36f, y + r * 0.18f, r * 0.10f, r * 0.10f, paint);
            paint.setColor(Color.rgb(37, 45, 52));
            canvas.drawRoundRect(x - r * 0.24f, y - r * 1.10f, x + r * 0.24f, y - r * 0.86f, r * 0.08f, r * 0.08f, paint);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(x + r * 0.76f, y - r * 0.48f, r * 0.22f, paint);
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

    private void drawFlowAura(Canvas canvas) {
        if (!flowActive()) {
            return;
        }
        // Motion blur streaks
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        for (int i = 0; i < 6; i++) {
            float ox = (float) Math.sin(spriteClock * 12f + i) * dp(15);
            float oy = (float) Math.cos(spriteClock * 15f + i) * dp(30);
            paint.setColor(Color.argb(Math.round(40 + 40 * (float) Math.sin(spriteClock * 20f + i)), 255, 255, 255));
            canvas.drawLine(playerX + ox - dp(60), playerY + oy, playerX + ox - dp(10), playerY + oy, paint);
        }

        float pct = flowProgress();
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 10.5f);
        float headY = playerY - playerRadius * PLAYER_HEAD_DRAW_OFFSET;
        float centerY = headY + playerRadius * 1.74f;
        float width = playerRadius * (2.35f + pulse * 0.24f);
        float height = playerRadius * (3.78f + pulse * 0.28f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.8f));
        paint.setColor(Color.argb(Math.round(78 + 92 * pct), 77, 219, 184));
        canvas.drawOval(playerX - width * 0.50f, centerY - height * 0.50f, playerX + width * 0.50f, centerY + height * 0.50f, paint);
        paint.setStrokeWidth(dp(1.1f));
        paint.setColor(Color.argb(Math.round(54 + 64 * pct), 230, 248, 255));
        canvas.drawCircle(playerX, centerY, playerRadius * (1.62f + pulse * 0.12f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawRespawnGraceAura(Canvas canvas) {
        if (respawnGraceTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, respawnGraceTimer / 1.35f);
        float pulse = 0.5f + 0.5f * (float) Math.sin(spriteClock * 14f);
        float headY = playerY - playerRadius * PLAYER_HEAD_DRAW_OFFSET;
        float centerY = headY + playerRadius * 1.70f;
        float width = playerRadius * (2.95f + pulse * 0.22f);
        float height = playerRadius * (4.40f + pulse * 0.30f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(34 + 58 * pct), 255, 246, 207));
        canvas.drawOval(playerX - width * 0.50f, centerY - height * 0.50f, playerX + width * 0.50f, centerY + height * 0.50f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(Math.round(110 + 90 * pct), 255, 218, 121));
        canvas.drawOval(playerX - width * 0.50f, centerY - height * 0.50f, playerX + width * 0.50f, centerY + height * 0.50f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBearSprayEffect(Canvas canvas) {
        if (bearSprayTimer <= 0f) {
            return;
        }
        float pct = Math.min(1f, bearSprayTimer / SprayTuning.CONE_SECONDS);
        float originX = bearSprayOriginX <= 0f ? runnerHandX() : bearSprayOriginX;
        float originY = bearSprayOriginY <= 0f ? runnerHandY() : bearSprayOriginY;
        float density = getResources().getDisplayMetrics().density;
        float range = SprayTuning.effectRange(density, pct);
        float halfHeight = SprayTuning.effectHalfHeight(density, pct);
        float dir = bearSprayDirection < 0f ? -1f : 1f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(82 * pct), 255, 166, 84));
        PathCompat.triangle(canvas, paint, originX, originY, originX + dir * range, originY - halfHeight, originX + dir * range, originY + halfHeight);
        paint.setColor(Color.argb(Math.round(58 * pct), 255, 218, 121));
        PathCompat.triangle(canvas, paint, originX + dir * dp(10), originY, originX + dir * range * 0.82f, originY - halfHeight * 0.46f, originX + dir * range * 0.82f, originY + halfHeight * 0.46f);

        paint.setColor(Color.rgb(255, 166, 84));
        canvas.drawRoundRect(originX - dp(6), originY - dp(9), originX + dp(6), originY + dp(9), dp(3), dp(3), paint);
        paint.setColor(Color.rgb(37, 45, 52));
        canvas.drawRoundRect(originX + dir * dp(1), originY - dp(12), originX + dir * dp(8), originY - dp(8), dp(2), dp(2), paint);
        paint.setColor(Color.rgb(255, 246, 207));
        canvas.drawCircle(originX + dir * dp(14 + 34 * (1f - pct)), originY - dp(13), dp(2.6f), paint);
        canvas.drawCircle(originX + dir * dp(32 + 55 * (1f - pct)), originY + dp(8), dp(2.1f), paint);
        canvas.drawCircle(originX + dir * dp(58 + 44 * (1f - pct)), originY - dp(1), dp(1.8f), paint);
    }

    private void drawShot(Canvas canvas, Shot shot) {
        float y = shot.y + (float) Math.sin(shot.wobble) * dp(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shot.empowered ? Color.argb(118, 255, 218, 121) : Color.argb(92, 200, 238, 255));
        canvas.drawCircle(shot.x - shot.radius * 0.9f, y, shot.radius * (shot.empowered ? 2.05f : 1.70f), paint);
        if (shot.empowered) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.6f));
            paint.setColor(Color.argb(190, 255, 246, 207));
            canvas.drawCircle(shot.x, y, shot.radius * 1.36f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        paint.setColor(shot.empowered ? Color.rgb(255, 246, 207) : Color.WHITE);
        canvas.drawCircle(shot.x, y, shot.radius, paint);
        paint.setColor(shot.empowered ? Color.rgb(255, 166, 84) : Color.rgb(180, 220, 230));
        canvas.drawCircle(shot.x - shot.radius * 0.3f, y - shot.radius * 0.25f, shot.radius * 0.35f, paint);
    }

    private void drawBossAttack(Canvas canvas, BossAttack attack) {
        if (attack.type == ATTACK_LASER) {
            float eyeX = bossLaserEyeX();
            float eyeY = bossLaserEyeY();
            float progress = Math.min(1f, attack.age / 1.12f);
            float endY = attack.y;
            float endX = laserAttackEndX(attack);
            float pulse = 0.86f + (float) Math.sin(attack.age * 22f) * 0.14f;
            float alpha = Math.min(1f, 0.68f + progress * 0.32f);
            float beamHeight = dp(selectedStage == 0 ? 3.0f : 2.6f);

            drawBossLaserBeams(canvas, eyeX, eyeY, endX, endY, beamHeight, alpha, pulse);
            drawBossLaserEyeEmitter(canvas, eyeX, eyeY, pulse);
            paint.setStyle(Paint.Style.FILL);
            return;
        }
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

    private void drawBeamCore(Canvas canvas, float x1, float y1, float x2, float y2, float height, float alpha, float intensity, float pulse) {
        /*
         * To make the laser look like a concentrated beam rather than a blocky
         * rectangle, we stack several semi-transparent additive layers with
         * rounded caps. This creates a hot inner core with a soft energy glow.
         */
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setShader(null);
        paint.setXfermode(null);

        // Dark silhouette: the danger lane stays legible against every biome.
        paint.setStrokeWidth(height * 3.6f * intensity);
        paint.setColor(Color.argb(Math.round(210 * alpha), 38, 4, 18));
        canvas.drawLine(x1, y1, x2, y2, paint);

        // Solid saturated body avoids the old blurry additive smear.
        paint.setStrokeWidth(height * 2.15f * intensity);
        paint.setColor(Color.argb(Math.round(245 * alpha), 222, 24, 52));
        canvas.drawLine(x1, y1, x2, y2, paint);

        paint.setStrokeWidth(height * 1.18f * intensity);
        paint.setColor(Color.argb(Math.round(250 * alpha), 255, 126, 52));
        canvas.drawLine(x1, y1, x2, y2, paint);

        // Stable white core communicates the exact collision center.
        float corePulse = 0.92f + 0.08f * pulse;
        paint.setStrokeWidth(Math.max(dp(1.5f), height * 0.46f * corePulse));
        paint.setColor(Color.argb(Math.round(255 * alpha), 255, 251, 224));
        canvas.drawLine(x1, y1, x2, y2, paint);

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBossLaserBeams(Canvas canvas, float eyeX, float eyeY, float endX, float endY,
                                    float beamHeight, float alpha, float pulse) {
        drawBeamCore(canvas, eyeX, eyeY, endX, endY, beamHeight, alpha, 1.08f, pulse);
        if (selectedStage == 0) {
            // The sun has two eyes; the second beam converges on the SAME impact
            // point so they read as one focused twin ray instead of a messy split.
            float rightEyeX = bossX + bossRadius() * 0.28f;
            drawBeamCore(canvas, rightEyeX, eyeY, endX, endY,
                    beamHeight * 0.82f, alpha * 0.88f, 1.04f, pulse);
            drawBossLaserEyeEmitter(canvas, rightEyeX, eyeY, pulse);
        }
        drawBeamImpact(canvas, endX, endY, beamHeight, alpha, pulse);
    }

    private void drawBeamImpact(Canvas canvas, float x, float y, float beamHeight, float alpha, float pulse) {
        // Concentric impact rings show the exact endpoint without a large halo.
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(null);
        paint.setColor(Color.argb(Math.round(230 * alpha), 55, 8, 18));
        canvas.drawCircle(x, y, beamHeight * (1.45f + pulse * 0.2f), paint);
        paint.setColor(Color.argb(Math.round(245 * alpha), 255, 74, 54));
        canvas.drawCircle(x, y, beamHeight * (0.95f + pulse * 0.15f), paint);
        paint.setColor(Color.argb(Math.round(220 * alpha), 255, 252, 236));
        canvas.drawCircle(x, y, beamHeight * 0.6f, paint);
    }

    private void laserAttackRect(BossAttack attack, RectF out) {
        float eyeX = bossLaserEyeX();
        float eyeY = bossLaserEyeY();
        float endX = laserAttackEndX(attack);
        out.set(Math.min(eyeX, endX), Math.min(eyeY, attack.y) - attack.radius,
                Math.max(eyeX, endX), Math.max(eyeY, attack.y) + attack.radius);
    }

    private float laserAttackEndX(BossAttack attack) {
        return Math.max(-dp(20), attack.x - Math.max(dp(34), attack.spin));
    }

    private float bossLaserEyeX() {
        if (selectedStage == 0) {
            return bossX - bossRadius() * 0.28f;
        }
        if (selectedStage == 4) {
            // Head of the standing polar bear, biased toward the player it faces.
            return bossX - bossRadius() * 0.40f;
        }
        return bossX - bossRadius() * 0.62f;
    }

    private float bossLaserEyeY() {
        if (selectedStage == 0) {
            return bossY - bossRadius() * 0.36f;
        }
        if (selectedStage == 4) {
            // Eye level near the top of the standing (bottom-anchored) bear sprite.
            float bottom = getGroundY() + dp(2);
            return bottom - polarBearBossSpriteHeight(bossRadius()) * 0.82f;
        }
        return bossY - bossRadius() * 0.28f;
    }

    private void drawBossLaserEyeEmitter(Canvas canvas, float x, float y, float pulse) {
        // Compact mechanical aperture. The old additive fireball obscured the
        // boss face and made the beam appear detached from its true origin.
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(null);
        paint.setColor(Color.argb(245, 20, 7, 14));
        canvas.drawCircle(x, y, dp(4.6f), paint);
        paint.setColor(Color.rgb(184, 20, 42));
        canvas.drawCircle(x, y, dp(3.25f), paint);
        paint.setColor(Color.rgb(255, 118, 38));
        canvas.drawCircle(x, y, dp(2.05f), paint);
        paint.setColor(Color.rgb(255, 252, 224));
        canvas.drawCircle(x, y, dp(0.95f), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.1f));
        paint.setColor(Color.argb(Math.round(135 + pulse * 90), 255, 70, 58));
        canvas.drawCircle(x, y, dp(5.7f + pulse * 0.8f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawNearMissFlash(Canvas canvas) {
        if (nearMissFlash <= 0f) {
            return;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(8));
        float alphaPct = nearMissFlash / 0.25f;
        paint.setColor(Color.argb(Math.round(120 * alphaPct), 77, 219, 184));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setStyle(Paint.Style.FILL);
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
        float shadowY = selectedStage == 1 || selectedStage == 3 ? getGroundY() + dp(4) : getGroundY() + dp(1.5f);
        int shadowAlpha = selectedStage == 1 || selectedStage == 3 ? 64 : 132;
        float shadowHalfHeight = selectedStage == 1 || selectedStage == 3 ? dp(9) : dp(5.5f);

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
            paint.setStrokeWidth(dp(1.8f));
            paint.setColor(Color.argb(155 + Math.round(70 * pulse), 255, 246, 207));
            canvas.drawLine(bossX - xRadius * 1.22f, bossY, bossX + xRadius * 1.22f, bossY, paint);
            canvas.drawLine(bossX, bossY - yRadius * 1.22f, bossX, bossY + yRadius * 1.22f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(214, 8, 18, 30));
            float promptWidth = dp(88);
            float promptTop = Math.max(dp(88), bossY - yRadius * 1.22f - dp(30));
            canvas.drawRoundRect(bossX - promptWidth / 2f, promptTop, bossX + promptWidth / 2f, promptTop + dp(22), dp(7), dp(7), paint);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(10.5f));
            textPaint.setColor(Color.rgb(255, 218, 121));
            canvas.drawText("FIRE NOW", bossX, promptTop + dp(15), textPaint);
        }
        paint.setColor(Color.argb(shadowAlpha, 0, 0, 0));
        canvas.drawOval(bossX - xRadius * 0.88f, shadowY - shadowHalfHeight, bossX + xRadius * 0.88f, shadowY + shadowHalfHeight, paint);

        if (selectedStage == 4 && assets.polarBearRoarSprite() != null) {
            drawStandingPolarBearBoss(canvas, radius);
        } else if (bossSheet != null) {
            drawAnimatedBossSheet(canvas, bossSheet, radius);
        } else if (selectedStage == 0) {
            drawMidnightSunBoss(canvas, radius, xRadius, yRadius);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 218, 121));
            canvas.drawCircle(bossX, bossY, radius, paint);
        }

        drawBossHealthBar(canvas);
    }

    /**
     * The Polar Bear Boss reads as a boss only if it rears up on its hind legs.
     * The walk sheet keeps it on all fours, so we draw the dedicated standing
     * roar sprite bottom-anchored to the ground with an idle sway and an
     * attack-lean, instead of the quadruped animation.
     */
    private void drawStandingPolarBearBoss(Canvas canvas, float radius) {
        Bitmap sprite = assets.polarBearRoarSprite();
        if (sprite == null || sprite.getWidth() <= 0 || sprite.getHeight() <= 0) {
            drawAnimatedBossSheet(canvas, sheetForBoss(4), radius);
            return;
        }
        int inset = Math.min(ROAR_SPRITE_SOURCE_INSET_PX, Math.min(sprite.getWidth(), sprite.getHeight()) / 8);
        spriteSourceRect.set(inset, inset, sprite.getWidth() - inset, sprite.getHeight() - inset);
        float height = polarBearBossSpriteHeight(radius);
        float width = height * (spriteSourceRect.width() / (float) spriteSourceRect.height());
        float bottom = getGroundY() + dp(2);
        float sway = (float) Math.sin(bossTimer * 3.0f) * dp(1.6f);
        float lean = 0f;
        if (bossState == BOSS_STATE_TELL) {
            sway = (float) Math.sin(bossTimer * 11f) * dp(2.4f);
        } else if (bossState == BOSS_STATE_ATTACK && bossPattern == BOSS_PATTERN_LUNGE) {
            lean = -dp(7);
        } else if (bossState == BOSS_STATE_RECOVER) {
            lean = dp(4);
        }
        float centerX = bossX + sway + lean;
        tempRect.set(centerX - width * 0.5f, bottom - height, centerX + width * 0.5f, bottom);
        boolean previousFilter = spriteBitmapPaint.isFilterBitmap();
        spriteBitmapPaint.setFilterBitmap(false);
        canvas.drawBitmap(sprite, spriteSourceRect, tempRect, spriteBitmapPaint);
        spriteBitmapPaint.setFilterBitmap(previousFilter);
    }

    private float polarBearBossSpriteHeight(float radius) {
        return radius * 4.05f;
    }

    private void drawMidnightSunBoss(Canvas canvas, float radius, float xRadius, float yRadius) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(bossTimer * 6.0f);
        float centerY = bossY - radius * 0.24f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(68 + Math.round(42 * pulse), 255, 166, 84));
        canvas.drawCircle(bossX, centerY, radius * 1.55f, paint);
        paint.setColor(Color.argb(90, 255, 218, 121));
        for (int i = 0; i < 12; i++) {
            float angle = (float) (i * Math.PI * 2.0 / 12.0 + bossTimer * 0.35f);
            float inner = radius * 1.02f;
            float outer = radius * (1.34f + 0.12f * (float) Math.sin(bossTimer * 4.5f + i));
            float x1 = bossX + (float) Math.cos(angle - 0.10f) * inner;
            float y1 = centerY + (float) Math.sin(angle - 0.10f) * inner;
            float x2 = bossX + (float) Math.cos(angle) * outer;
            float y2 = centerY + (float) Math.sin(angle) * outer;
            float x3 = bossX + (float) Math.cos(angle + 0.10f) * inner;
            float y3 = centerY + (float) Math.sin(angle + 0.10f) * inner;
            PathCompat.triangle(canvas, paint, x1, y1, x2, y2, x3, y3);
        }

        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawCircle(bossX, centerY, radius * 0.98f, paint);
        paint.setColor(Color.rgb(255, 166, 84));
        canvas.drawCircle(bossX, centerY, radius * 0.76f, paint);
        paint.setColor(Color.rgb(82, 18, 24));
        canvas.drawCircle(bossX - radius * 0.28f, centerY - radius * 0.12f, Math.max(dp(2.2f), radius * 0.08f), paint);
        canvas.drawCircle(bossX + radius * 0.28f, centerY - radius * 0.12f, Math.max(dp(2.2f), radius * 0.08f), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(2.0f));
        paint.setColor(Color.rgb(82, 18, 24));
        canvas.drawLine(bossX - radius * 0.24f, centerY + radius * 0.34f, bossX + radius * 0.24f, centerY + radius * 0.34f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);

        if (bossPattern == BOSS_PATTERN_LASER && (bossState == BOSS_STATE_TELL || bossState == BOSS_STATE_ATTACK)) {
            float eyeY = centerY - radius * 0.12f;
            float chargePulse = 0.45f + 0.55f * (float) Math.sin(bossTimer * 28f);
            drawBossLaserEyeEmitter(canvas, bossX - radius * 0.28f, eyeY, chargePulse);
            drawBossLaserEyeEmitter(canvas, bossX + radius * 0.28f, eyeY, chargePulse);
        }

        float badgeWidth = dp(94);
        float badgeTop = Math.max(dp(86), centerY - radius * 1.62f);
        paint.setColor(Color.argb(218, 82, 18, 24));
        canvas.drawRoundRect(bossX - badgeWidth / 2f, badgeTop, bossX + badgeWidth / 2f, badgeTop + dp(18), dp(6), dp(6), paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(8.5f));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText("MIDNIGHT SUN", bossX, badgeTop + dp(12.5f), textPaint);
    }

    private void drawBossTell(Canvas canvas, float radius) {
        if (bossState != BOSS_STATE_TELL) {
            return;
        }
        float pct = Math.min(1f, bossStateTimer / Math.max(0.01f, bossTellDuration()));
        float alphaPulse = 0.45f + 0.55f * (float) Math.sin(bossStateTimer * 24f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(Math.round(34 + 48 * pct), 255, 98, 84));
        if (bossPattern == BOSS_PATTERN_LUNGE) {
            float left = playerX + dp(36);
            float right = Math.min(getWidth() - dp(36), bossX - radius * 0.35f);
            float top = getGroundY() - dp(56);
            float bottom = getGroundY() - dp(8);
            canvas.drawRoundRect(left, top, right, bottom, dp(12), dp(12), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.8f));
            paint.setColor(Color.argb(Math.round((115 + 120 * pct) * alphaPulse), 255, 98, 84));
            canvas.drawRoundRect(left, top, right, bottom, dp(12), dp(12), paint);
        } else if (bossPattern == BOSS_PATTERN_SNOW_WAVE) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(46 + 58 * pct), 132, 213, 232));
            canvas.drawCircle(bossX - radius * 0.9f, getGroundY() - dp(24), dp(18 + pct * 10), paint);
            canvas.drawCircle(bossX - radius * 1.05f, getGroundY() - dp(78), dp(15 + pct * 8), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.6f));
            paint.setColor(Color.argb(Math.round((120 + 110 * pct) * alphaPulse), 248, 252, 253));
            canvas.drawCircle(bossX - radius * 0.9f, getGroundY() - dp(24), dp(18 + pct * 10), paint);
            canvas.drawCircle(bossX - radius * 1.05f, getGroundY() - dp(78), dp(15 + pct * 8), paint);
        } else if (bossPattern == BOSS_PATTERN_LASER) {
            float beamX = bossLaserEyeX();
            float beamY = bossLaserEyeY();
            float endX = beamX + (-dp(20) - beamX) * pct;
            float targetY = clamp(bossTellY, getGroundY() - dp(128), getGroundY() - dp(38));
            float endY = beamY + (targetY - beamY) * pct;
            float pulse = 0.72f + (float) Math.sin(bossStateTimer * 26f) * 0.28f;
            float alpha = pct * 0.62f;
            float beamHeight = dp(selectedStage == 0 ? 4.6f : 3.8f);
            drawBossLaserBeams(canvas, beamX, beamY, endX, endY, beamHeight, alpha, pulse);
            drawBossLaserEyeEmitter(canvas, beamX, beamY, pct);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(38 + 52 * pct), 255, 218, 121));
            canvas.drawCircle(bossX, bossY - radius * 0.6f, radius * (0.82f + pct * 0.26f), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.4f));
            paint.setColor(Color.argb(Math.round((120 + 110 * pct) * alphaPulse), 255, 246, 207));
            canvas.drawCircle(bossX, bossY - radius * 0.6f, radius * (0.82f + pct * 0.26f), paint);
        }
        paint.setStyle(Paint.Style.FILL);

        float bannerWidth = Math.min(getWidth() - dp(82), dp(290));
        float bannerHeight = dp(24);
        float bannerLeft = (getWidth() - bannerWidth) / 2f;
        float bannerTop = dp(114);
        paint.setColor(Color.argb(Math.round(185 + 45 * pct), 8, 18, 30));
        canvas.drawRoundRect(bannerLeft, bannerTop, bannerLeft + bannerWidth, bannerTop + bannerHeight, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(Math.round(185 + 55 * pct), 255, 218, 121));
        canvas.drawRoundRect(bannerLeft, bannerTop, bannerLeft + bannerWidth, bannerTop + bannerHeight, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(10.5f));
        textPaint.setColor(Color.rgb(255, 246, 207));
        canvas.drawText(bossTellInstruction(), getWidth() / 2f, bannerTop + dp(16), textPaint);
    }

    private String bossTellInstruction() {
        if (bossPattern == BOSS_PATTERN_LUNGE) {
            return "RED ZONE: BACK UP";
        }
        if (bossPattern == BOSS_PATTERN_SNOW_WAVE) {
            return selectedStage == 4 ? "ICE THROW: JUMP OR FIRE" : "PROJECTILE: JUMP OR FIRE";
        }
        if (bossPattern == BOSS_PATTERN_LASER) {
            return "EYE BEAM: DODGE";
        }
        return "SUMMON: STUN WILDLIFE";
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
        if (stage == 2) return 1.92f;
        if (stage == 3) return 1.72f;
        if (stage == 4) return 1.74f;
        return 1.30f;
    }

    private float bossVerticalScale(int stage) {
        if (stage == 1) return 1.02f;
        if (stage == 2) return 1.20f;
        if (stage == 3) return 1.10f;
        if (stage == 4) return 1.04f;
        return 1.30f;
    }

    private void drawAnimatedBossSheet(Canvas canvas, Bitmap sheet, float radius) {
        float phase = bossTimer + selectedStage * 0.37f;
        float rate = selectedStage == 3 ? 1.85f : selectedStage == 1 ? 2.70f : selectedStage == 4 ? 2.45f : 2.35f;
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
            if (bossPattern == BOSS_PATTERN_LASER && (bossState == BOSS_STATE_TELL || bossState == BOSS_STATE_ATTACK)) {
                height = radius * 3.65f;
                centerY = getGroundY() - height * 0.48f + bossGroundSink(selectedStage);
                rotation = (float) Math.sin(phase * 12f) * 1.2f;
            } else if (bossState == BOSS_STATE_TELL) {
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
        Rect[] crops = spriteSheetCrops(sheet);
        Rect crop = crops != null && safeFrame < crops.length ? crops[safeFrame] : null;

        int rawLeft = safeFrame * frameWidth;
        int rawRight = (safeFrame + 1) * frameWidth;

        if (crop != null && !crop.isEmpty()) {
            spriteSourceRect.set(crop);
        } else {
            int[] trim = spriteSheetTrim(sheet, safeFrame);
            if (trim == null) {
                spriteSourceRect.set(rawLeft, 0, rawRight, sheet.getHeight());
            } else {
                setTrimmedSpriteSource(spriteSourceRect, safeFrame, frameWidth, sheet.getHeight(), trim);
            }
        }

        // Use fixed cell width for base scaling to prevent jitter.
        float baseWidth = height * (frameWidth / (float) sheet.getHeight());

        // Calculate relative offset of the sourceRect within its frame cell.
        float relCenterX = ((spriteSourceRect.left + spriteSourceRect.right) / 2f - (rawLeft + rawRight) / 2f) / (float) frameWidth;
        float relCenterY = ((spriteSourceRect.top + spriteSourceRect.bottom) / 2f - (sheet.getHeight() / 2f)) / (float) sheet.getHeight();

        float drawCenterX = centerX + relCenterX * baseWidth;
        float drawCenterY = centerY + relCenterY * height;

        float actualDrawWidth = height * (spriteSourceRect.width() / (float) spriteSourceRect.height());
        tempRect.set(drawCenterX - actualDrawWidth * 0.50f, drawCenterY - height * 0.50f, drawCenterX + actualDrawWidth * 0.50f, drawCenterY + height * 0.50f);

        int saved = canvas.save();
        if (Math.abs(rotationDegrees) > 0.01f) {
            canvas.rotate(rotationDegrees, centerX, centerY);
        }
        boolean previousFilter = spriteBitmapPaint.isFilterBitmap();
        spriteBitmapPaint.setFilterBitmap(true);
        canvas.drawBitmap(sheet, spriteSourceRect, tempRect, spriteBitmapPaint);
        spriteBitmapPaint.setFilterBitmap(previousFilter);
        canvas.restoreToCount(saved);
    }

    private Rect[] spriteSheetCrops(Bitmap sheet) {
        if (sheet == assets.mooseWalkSheet()) return mooseFrameCrops;
        if (sheet == assets.bearWalkSheet()) return bearFrameCrops;
        if (sheet == assets.polarBearWalkSheet()) return polarBearFrameCrops;
        if (sheet == assets.wolfRunSheet()) return wolfFrameCrops;
        if (sheet == assets.salmonSwimSheet()) return salmonFrameCrops;
        if (sheet == assets.eagleFlySheet()) return eagleFrameCrops;
        return null;
    }

    private static Rect[] completeFrameCrops(Bitmap sheet) {
        Rect[] crops = SpriteFrameCropper.computeCellContentCrops(sheet, SPRITE_SHEET_FRAMES, 2);
        SpriteFrameCropper.retainFullCellRight(crops, sheet, SPRITE_SHEET_FRAMES);
        return crops;
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
        String bossStatus = hasShootableBossAttack() ? "  FIRE PROJECTILES" : bossWeakWindowActive() ? "  WEAK WINDOW" : bossEnraged() ? "  ENRAGED" : bossPhaseTwoAnnounced ? "  PHASE 2" : "";
        canvas.drawText(STAGES[selectedStage].bossName + " HP " + Math.max(0, bossHealth) + "/" + bossMaxHealth
                + bossStatus + "  ESCAPE " + Math.round(bossTimeRemaining()) + "s", getWidth() / 2f, top - dp(6), textPaint);
    }

    private boolean hasShootableBossAttack() {
        for (BossAttack attack : bossAttacks) {
            if (bossAttackCanBeShot(attack)) {
                return true;
            }
        }
        return false;
    }

    private float bossRadius() {
        return bossRadiusForStage(selectedStage);
    }

    private float bossRadiusForStage(int stage) {
        if (stage == 4) return gameplayDp(38);
        if (stage == 2) return gameplayDp(34);
        if (stage == 0) return gameplayDp(32);
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
        if (stage == 0) {
            return clamp(getGroundY() - dp(118), dp(112), getGroundY() - dp(86));
        }
        if (stage == 1) {
            return clamp(getGroundY() - dp(92), dp(116), getGroundY() - dp(72));
        }
        if (stage == 3) {
            return clamp(getGroundY() - dp(154), dp(112), getGroundY() - dp(118));
        }
        return getGroundY() - bossDrawHeight(stage) * 0.5f + bossGroundSink(stage);
    }

    private float bossDrawHeight(int stage) {
        float radius = bossRadiusForStage(stage);
        if (stage == 1) return radius * 2.45f;
        if (stage == 3) return radius * 3.15f;
        if (stage == 4) return radius * 2.95f;
        if (sheetForBoss(stage) != null) return radius * 3.3f;
        return radius * 2f;
    }

    private float bossGroundSink(int stage) {
        if (stage == 4) return dp(5);
        if (stage == 2) return dp(2);
        return dp(1);
    }

    private void drawCharacter(Canvas canvas, float x, float y, float radius) {
        int saved = canvas.save();
        // Scale from the feet/ground anchor point for grounded squash/stretch
        canvas.scale(squashX, squashY, x, y + radius * 2f);
        spriteRenderer.drawRunner(canvas, playerFrame(x, y, radius));
        canvas.restoreToCount(saved);
    }

    private void drawCharacterPreview(Canvas canvas, float x, float y, float radius) {
        spriteRenderer.drawStanding(canvas, playerFrame(x, y, radius));
    }

    private SpriteRenderer.PlayerFrame playerFrame(float x, float y, float radius) {
        int outfitColor = playerPhoto == null ? Color.rgb(255, 218, 121) : OUTFIT_COLORS[selectedOutfit];
        return new SpriteRenderer.PlayerFrame(x, y, radius, runnerClock, grounded, playerVelocityY, playerPhoto, outfitColor, selectedBodyStyle);
    }

    private void drawReadyScreen(Canvas canvas) {
        StageConfig stage = STAGES[selectedStage];
        boolean compact = getHeight() < dp(420);
        float panelWidth = Math.min(getWidth() - dp(56), dp(462));
        float panelHeight = dp(compact ? 180 : 204);
        float left = (getWidth() - panelWidth) / 2f;
        float top = compact ? dp(70) : Math.max(dp(84), getHeight() * 0.26f);
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
        canvas.drawText(BuildConfig.BUILD_BADGE + " · LEVEL " + (selectedStage + 1), getWidth() / 2f, top + dp(compact ? 23 : 28), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(28));
        canvas.drawText(stage.name, getWidth() / 2f, top + dp(compact ? 54 : 64), textPaint);

        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(stage.line, getWidth() / 2f, top + dp(compact ? 80 : 94), textPaint);

        float chipTop = top + dp(compact ? 94 : 112);
        float chipWidth = (panelWidth - dp(52)) / 3f;
        drawBriefingChip(canvas, left + dp(16), chipTop, chipWidth, stageActionVerb(selectedStage), stage.obstacleName);
        drawBriefingChip(canvas, left + dp(26) + chipWidth, chipTop, chipWidth, "BOSS", stage.bossName);
        drawBriefingChip(canvas, left + dp(36) + chipWidth * 2f, chipTop, chipWidth, "MISSION", missionBriefLine());

        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(stageRuleLine(stage), getWidth() / 2f, top + dp(compact ? 143 : 162), textPaint);
        canvas.drawText("Tap FIRE for snowballs; tap SPRAY to stun close wildlife.", getWidth() / 2f, top + dp(compact ? 156 : 176), textPaint);

        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("DRAFT A PERK", getWidth() / 2f, top + dp(compact ? 173 : 192), textPaint);

        drawPerkDraft(canvas, left, top + panelHeight + dp(compact ? 7 : 12), panelWidth);
    }

    private void drawPerkDraft(Canvas canvas, float left, float cardsTop, float width) {
        float gap = dp(10);
        float cardWidth = (width - gap * 2f) / 3f;
        float availableHeight = getHeight() - cardsTop - dp(6);
        float cardHeight = Math.min(dp(70), Math.max(dp(42), availableHeight));
        for (int i = 0; i < perkCardBounds.length; i++) {
            float cardLeft = left + i * (cardWidth + gap);
            perkCardBounds[i].set(cardLeft, cardsTop, cardLeft + cardWidth, cardsTop + cardHeight);
            int perk = perkChoices[i];
            boolean chosen = activePerk != PERK_NONE && activePerk == perk;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(chosen ? Color.argb(238, 22, 74, 60) : Color.argb(222, 12, 24, 34));
            canvas.drawRoundRect(perkCardBounds[i], dp(12), dp(12), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2f));
            paint.setColor(chosen ? Color.rgb(77, 219, 184) : Color.argb(200, 255, 218, 121));
            canvas.drawRoundRect(perkCardBounds[i], dp(12), dp(12), paint);
            paint.setStyle(Paint.Style.FILL);

            if (perk < 0 || perk >= PERK_COUNT) {
                continue;
            }
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.rgb(255, 218, 121));
            drawTextFit(canvas, PERK_NAMES[perk], perkCardBounds[i].centerX(), cardsTop + dp(24), cardWidth - dp(10), dp(11), dp(8), Paint.Align.CENTER);
            textPaint.setColor(Color.rgb(210, 232, 238));
            drawTextFit(canvas, PERK_DESCS[perk], perkCardBounds[i].centerX(), cardsTop + dp(44), cardWidth - dp(10), dp(9.5f), dp(7), Paint.Align.CENTER);
            textPaint.setTextSize(dp(8.5f));
            textPaint.setColor(chosen ? Color.rgb(77, 219, 184) : Color.rgb(255, 246, 207));
            canvas.drawText(chosen ? "SELECTED" : "TAP TO PICK", perkCardBounds[i].centerX(), cardsTop + dp(61), textPaint);
        }
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
        if (activePerk != PERK_NONE) {
            textPaint.setTextSize(dp(8.5f));
            textPaint.setColor(Color.rgb(77, 219, 184));
            canvas.drawText("PERK " + PERK_NAMES[activePerk], dp(20), dp(62), textPaint);
        }

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
                : RushDirector.beatLabel(gatesPassed) + "  ·  " + stageActionVerb(selectedStage) + " "
                + obstacleHudName(selectedStage) + " " + gatesPassed + "/" + STAGES[selectedStage].goalGates;
        drawTextFit(canvas, objective, getWidth() / 2f, dp(44), progressRight - progressLeft - dp(12), dp(11), dp(8.5f), Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        String comboPrefix = auroraRushTimer > 0f ? "AURORA " : flowActive() ? "FLOW " : "";
        String comboLabel = comboPrefix + "COMBO " + gameState.combo + "   SCORE x" + multiplier;
        drawTextFit(canvas, comboLabel, getWidth() / 2f, dp(58), progressRight - progressLeft - dp(12), dp(11), dp(8.5f), Paint.Align.CENTER);
        drawAuroraMeter(canvas, progressLeft, progressRight, dp(61), dp(65));
        drawFlowMeter(canvas, progressLeft, progressRight, dp(66.5f), dp(70.5f));

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText(STAGES[selectedStage].name, getWidth() - dp(20), dp(25), textPaint);
        textPaint.setTextSize(dp(11));
        textPaint.setColor(Color.WHITE);
        drawTextFit(canvas, "BEST " + bestScore + "   LV " + gameState.level + "   T " + trailTokens + (gameState.muted ? "  MUTE" : ""), getWidth() - dp(20), dp(47), getWidth() * 0.29f, dp(11), dp(8.5f), Paint.Align.RIGHT);

        drawHudIcons(canvas);
        drawPauseButton(canvas);
        drawActionOverlay(canvas);
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

    private void drawFlowMeter(Canvas canvas, float left, float right, float top, float bottom) {
        if (!flowActive()) {
            return;
        }
        float pct = flowProgress();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(118, 4, 12, 20));
        canvas.drawRoundRect(left, top, right, bottom, dp(3), dp(3), paint);
        paint.setColor(Color.rgb(77, 219, 184));
        canvas.drawRoundRect(left, top, left + (right - left) * pct, bottom, dp(3), dp(3), paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(7.5f));
        textPaint.setColor(Color.rgb(230, 248, 255));
        canvas.drawText("FLOW x" + cleanVaultStreak, (left + right) * 0.5f, bottom + dp(8), textPaint);
    }

    private void drawHudIcons(Canvas canvas) {
        float lifeX = dp(145);
        float lifeY = dp(42);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            paint.setColor(i < gameState.lives ? Color.rgb(255, 98, 84) : Color.argb(110, 255, 255, 255));
            canvas.drawCircle(lifeX + i * dp(14), lifeY, dp(4.8f), paint);
        }

        // Keep stars on their own lower row; the BEST/LV/T summary occupies
        // the middle row and previously drew directly through this icon.
        float starX = getWidth() - dp(104);
        float starY = dp(59);
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

        if (flowActive()) {
            float flowX = getWidth() - dp(72);
            float flowY = auroraFocusTimer > 0f ? dp(40) : dp(22);
            paint.setColor(Color.rgb(77, 219, 184));
            canvas.drawCircle(flowX, flowY, dp(7), paint);
            paint.setColor(Color.rgb(8, 18, 30));
            canvas.drawCircle(flowX, flowY, dp(4.4f), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.2f));
            paint.setColor(Color.WHITE);
            canvas.drawCircle(flowX, flowY, dp(2.7f + flowProgress() * 1.1f), paint);
            paint.setStyle(Paint.Style.FILL);
        }

        if (bearSprayCharges > 0 || bearSprayCooldown > 0f) {
            float sprayX = getWidth() - dp(92);
            float sprayY = dp(22);
            paint.setColor(bearSprayCooldown > 0f ? Color.argb(170, 255, 166, 84) : Color.rgb(255, 166, 84));
            canvas.drawRoundRect(sprayX - dp(5), sprayY - dp(8), sprayX + dp(5), sprayY + dp(8), dp(3), dp(3), paint);
            paint.setColor(Color.rgb(255, 246, 207));
            canvas.drawRoundRect(sprayX - dp(3), sprayY - dp(4), sprayX + dp(3), sprayY + dp(3), dp(2), dp(2), paint);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(dp(10));
            textPaint.setColor(Color.WHITE);
            canvas.drawText("x" + bearSprayCharges, sprayX + dp(10), sprayY + dp(4), textPaint);
        }
    }

    private void drawPauseButton(Canvas canvas) {
        if (state != STATE_RUNNING && state != STATE_PAUSED) {
            pauseButtonBounds.setEmpty();
            return;
        }
        pauseButtonBounds.set(getWidth() - dp(78), dp(76), getWidth() - dp(18), dp(106));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(state == STATE_PAUSED ? Color.argb(238, 255, 218, 121) : Color.argb(204, 16, 25, 37));
        canvas.drawRoundRect(pauseButtonBounds, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(Color.argb(230, 255, 255, 255));
        canvas.drawRoundRect(pauseButtonBounds, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(9.5f));
        textPaint.setColor(state == STATE_PAUSED ? Color.rgb(24, 30, 38) : Color.WHITE);
        canvas.drawText(state == STATE_PAUSED ? "RESUME" : "PAUSE", pauseButtonBounds.centerX(), pauseButtonBounds.centerY() + dp(4), textPaint);
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
        String scout = scoutTimer > 0f ? "  SCOUT " + Math.round(scoutTimer) : "";
        drawTextFit(canvas, leg + (campReached ? "  CAMP RESTOCKED" : "") + focus + scout, getWidth() / 2f, top + dp(13.5f), width - dp(12), dp(8.5f), dp(7.2f), Paint.Align.CENTER);
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
        float width = Math.min(getWidth() - dp(70), dp(500));
        float height = dp(24);
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
        String route = routeMilestoneLabel(selectedStage, clampInt(routeMilestoneIndex, 0, 2));
        String status = route + "  ·  " + missionProgressLine();
        drawTextFit(canvas, status, getWidth() / 2f, top + dp(15.5f), width - dp(16), dp(8.8f), dp(7.2f), Paint.Align.CENTER);
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
        drawTextFit(canvas, runCallout, getWidth() / 2f, top + dp(22), width - dp(18), dp(13), dp(9), Paint.Align.CENTER);
    }

    private void drawDebugOverlay(Canvas canvas) {
        float top = dp(74);
        float left = dp(10);
        float width = Math.min(getWidth() - dp(20), dp(236));
        float height = dp(100);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(112, 0, 0, 0));
        canvas.drawRoundRect(left, top, left + width, top + height, dp(8), dp(8), paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(8.8f));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("DEBUG IDS  L/P/G/H/*/U/T/A/B", left + dp(8), top + dp(14), textPaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(stateName() + " score=" + score + " boss=" + bossActive + " hp=" + bossHealth, left + dp(8), top + dp(29), textPaint);
        canvas.drawText("shots=" + shots.size() + " hazards=" + hazards.size() + " x=" + Math.round(playerX), left + dp(8), top + dp(43), textPaint);

        int max = Math.min(3, debugEvents.size());
        for (int i = 0; i < max; i++) {
            String event = debugEvents.get(debugEvents.size() - 1 - i);
            canvas.drawText("> " + event, left + dp(8), top + dp(59 + i * 13), textPaint);
        }
    }

    private void drawDebugObjectNumbers(Canvas canvas) {
        if (!debugOverlay) {
            return;
        }
        drawDebugHitboxes(canvas);
        int number = 1;
        drawDebugObjectBadge(canvas, number++, "P", "PLAYER", playerX, playerY - playerRadius - dp(24), Color.rgb(132, 213, 232));
        for (Gate gate : gates) {
            if (isDebugMarkerVisible(gate.x + gate.width * 0.5f, getGroundY() - gate.height * 0.5f, gate.width)) {
                drawDebugObjectBadge(canvas, number++, "G", debugGateDetail(gate), gate.x + gate.width * 0.5f, getGroundY() - gate.height - dp(34), Color.rgb(255, 218, 121));
            }
        }
        for (Hazard hazard : hazards) {
            float yRadius = hazard.radius * hazardVerticalScale(hazard.label);
            if (isDebugMarkerVisible(hazard.x, hazard.y, hazard.radius * 2f)) {
                drawDebugObjectBadge(canvas, number++, "H", debugHazardSpriteDetail(hazard), hazard.x, hazard.y - yRadius - dp(24), Color.rgb(255, 98, 84));
            }
        }
        for (Star star : stars) {
            if (isDebugMarkerVisible(star.x, star.y, star.radius * 2f)) {
                drawDebugObjectBadge(canvas, number++, "*", "STAR", star.x, star.y - star.radius - dp(18), Color.rgb(255, 218, 121));
            }
        }
        for (PowerUp powerUp : powerUps) {
            if (isDebugMarkerVisible(powerUp.x, powerUp.y, powerUp.radius * 2f)) {
                drawDebugObjectBadge(canvas, number++, "U", debugPowerUpDetail(powerUp), powerUp.x, powerUp.y - powerUp.radius - dp(22), Color.rgb(77, 219, 184));
            }
        }
        for (Shot shot : shots) {
            if (isDebugMarkerVisible(shot.x, shot.y, shot.radius * 2f)) {
                drawDebugObjectBadge(canvas, number++, "T", shot.empowered ? "POWER" : "SNOW", shot.x, shot.y - shot.radius - dp(18), shot.empowered ? Color.rgb(255, 218, 121) : Color.rgb(132, 213, 232));
            }
        }
        for (BossAttack attack : bossAttacks) {
            if (isDebugMarkerVisible(attack.x, attack.y, attack.radius * 2f)) {
                drawDebugObjectBadge(canvas, number++, "A", debugBossAttackDetail(attack), attack.x, attack.y - attack.radius - dp(20), Color.rgb(210, 232, 238));
            }
        }
        if (bossActive) {
            drawDebugObjectBadge(canvas, number, "B", debugBossSpriteDetail(), bossX, bossY - bossRadius() - dp(28), Color.rgb(255, 98, 84));
        }
    }

    private void drawDebugHitboxes(Canvas canvas) {
        drawDebugGroundLine(canvas);
        drawDebugCircle(canvas, playerX, playerY, playerRadius * CollisionTuning.PLAYER_HAZARD_RADIUS_SCALE, Color.rgb(132, 213, 232));
        for (Gate gate : gates) {
            if (!isDebugMarkerVisible(gate.x + gate.width * 0.5f, getGroundY() - gate.height * 0.5f, gate.width)) {
                continue;
            }
            gateHitRect(gate, tempRect);
            drawDebugRect(canvas, tempRect, Color.rgb(255, 218, 121));
        }
        for (Hazard hazard : hazards) {
            if (!isDebugMarkerVisible(hazard.x, hazard.y, hazard.radius * 2f)) {
                continue;
            }
            if ("THIN ICE".equals(hazard.label)) {
                tempRect.set(hazard.x - hazard.radius * 1.35f, getGroundY() - dp(18), hazard.x + hazard.radius * 1.35f, getGroundY() + dp(4));
                drawDebugRect(canvas, tempRect, Color.rgb(132, 213, 232));
            } else {
                drawDebugCircle(canvas, hazard.x, CollisionTuning.hazardHitY(hazard.y, hazard.radius, hazard.roaring), hazard.radius * CollisionTuning.hazardRadiusScale(hazard.roaring), Color.rgb(255, 98, 84));
            }
        }
        for (Star star : stars) {
            if (isDebugMarkerVisible(star.x, star.y, star.radius * 2f)) {
                drawDebugCircle(canvas, star.x, star.y, star.radius * CollisionTuning.STAR_RADIUS_SCALE, Color.rgb(255, 218, 121));
            }
        }
        for (PowerUp powerUp : powerUps) {
            if (isDebugMarkerVisible(powerUp.x, powerUp.y, powerUp.radius * 2f)) {
                drawDebugCircle(canvas, powerUp.x, powerUp.y, powerUp.radius * CollisionTuning.POWERUP_RADIUS_SCALE, Color.rgb(77, 219, 184));
            }
        }
        for (Shot shot : shots) {
            if (isDebugMarkerVisible(shot.x, shot.y, shot.radius * 2f)) {
                drawDebugCircle(canvas, shot.x, shot.y, shot.radius, shot.empowered ? Color.rgb(255, 218, 121) : Color.rgb(132, 213, 232));
            }
        }
        for (BossAttack attack : bossAttacks) {
            if (!isDebugMarkerVisible(attack.x, attack.y, attack.radius * 2f)) {
                continue;
            }
            if (attack.type == ATTACK_LASER) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(attack.radius * 2f);
                paint.setColor(Color.rgb(255, 98, 84));
                canvas.drawLine(bossLaserEyeX(), bossLaserEyeY(), laserAttackEndX(attack), attack.y, paint);
                paint.setStyle(Paint.Style.FILL);
            } else {
                drawDebugCircle(canvas, attack.x, attack.y, attack.radius, Color.rgb(210, 232, 238));
            }
        }
        if (bossActive) {
            drawDebugCircle(canvas, bossX, bossHurtCenterY(), bossContactRadius(), Color.rgb(255, 98, 84));
        }
    }

    private void drawDebugGroundLine(Canvas canvas) {
        debugOverlayRenderer.drawGroundLine(canvas, getWidth(), getGroundY());
    }

    private void drawDebugCircle(Canvas canvas, float x, float y, float radius, int color) {
        debugOverlayRenderer.drawCircle(canvas, x, y, radius, color);
    }

    private void drawDebugRect(Canvas canvas, RectF rect, int color) {
        debugOverlayRenderer.drawRect(canvas, rect, color);
    }

    private String debugHazardSpriteDetail(Hazard hazard) {
        if (hazard.roaring && roarSpriteForHazard(hazard.label) != null) {
            return hazard.label + " roar png";
        }
        if (sheetForHazard(hazard.label) != null) {
            return hazard.label + " sheet f" + debugHazardFrame(hazard) + " T";
        }
        return hazard.label + " drawn";
    }

    private String debugGateDetail(Gate gate) {
        if (selectedStage == 0 || selectedStage == 1) {
            return "LOG FIRE";
        }
        return obstacleHudName(selectedStage);
    }

    private String debugPowerUpDetail(PowerUp powerUp) {
        return powerUp.type;
    }

    private int debugHazardFrame(Hazard hazard) {
        int frame = Math.floorMod((int) (hazardVisualPhase(hazard) * hazardAnimationRate(hazard.label)), SPRITE_SHEET_FRAMES);
        return hazard.roaring ? 3 : frame;
    }

    private String debugBossSpriteDetail() {
        if (sheetForBoss(selectedStage) == null) {
            return STAGES[selectedStage].bossName;
        }
        float phase = bossTimer + selectedStage * 0.37f;
        float rate = selectedStage == 3 ? 1.85f : selectedStage == 1 ? 2.70f : selectedStage == 4 ? 2.45f : 2.35f;
        if (bossState == BOSS_STATE_TELL) {
            rate *= 0.45f;
        } else if (bossState == BOSS_STATE_ATTACK) {
            rate *= 1.55f;
        }
        int frame = Math.floorMod((int) (phase * rate), SPRITE_SHEET_FRAMES);
        if (bossStunTimer > 0f) {
            frame = Math.floorMod(frame + 2, SPRITE_SHEET_FRAMES);
        }
        return sheetForBoss(selectedStage) == null ? "BOSS drawn" : "BOSS sheet f" + frame + " T";
    }

    private String debugBossAttackDetail(BossAttack attack) {
        if (attack.type == ATTACK_ICE) {
            return "ICE FIRE";
        }
        if (attack.type == ATTACK_LASER) {
            return "BEAM DODGE";
        }
        if (attack.type == ATTACK_SHOCKWAVE) {
            return "WAVE DODGE";
        }
        return attack.label;
    }

    private boolean isDebugMarkerVisible(float x, float y, float pad) {
        return x + pad >= -dp(72) && x - pad <= getWidth() + dp(72)
                && y + pad >= -dp(72) && y - pad <= getHeight() + dp(72);
    }

    private void drawDebugObjectBadge(Canvas canvas, int number, String type, float x, float y, int accentColor) {
        drawDebugObjectBadge(canvas, number, type, "", x, y, accentColor);
    }

    private void drawDebugObjectBadge(Canvas canvas, int number, String type, String detail, float x, float y, int accentColor) {
        debugOverlayRenderer.drawObjectBadge(canvas, getWidth(), getHeight(), number, type, detail, x, y, accentColor);
    }

    private void initAudio() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(4)
                .build();
        soundJump = loadGeneratedSound("jump");
        soundDoubleJump = loadGeneratedSound("double_jump");
        soundThrow = loadGeneratedSound("throw");
        soundHit = loadGeneratedSound("hit");
        soundHurt = loadGeneratedSound("hurt");
        soundMedal = loadGeneratedSound("medal");
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

    private int loadGeneratedSound(String name) {
        try {
            File file = new File(getContext().getCacheDir(), "you_rush_" + name + ".wav");
            writeGameSoundWav(file, name);
            return soundPool.load(file.getAbsolutePath(), 1);
        } catch (IOException exception) {
            Log.w(TAG, "Unable to prepare sound " + name, exception);
            return 0;
        }
    }

    private void writeGameSoundWav(File file, String name) throws IOException {
        int sampleRate = 22050;
        int durationMs = soundDurationMs(name);
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
                double progress = i / (double) Math.max(1, sampleCount - 1);
                short sample = (short) (soundSample(name, i / (double) sampleRate, progress) * 13500);
                writeLittleEndianShort(out, sample);
            }
        }
    }

    private int soundDurationMs(String name) {
        if ("medal".equals(name)) return 260;
        if ("hurt".equals(name)) return 210;
        if ("hit".equals(name)) return 130;
        if ("double_jump".equals(name)) return 125;
        if ("throw".equals(name)) return 105;
        return 105;
    }

    private double soundSample(String name, double t, double progress) {
        double envelope = Math.sin(Math.PI * Math.min(1.0, progress)) * Math.pow(1.0 - progress, 0.65);
        if ("jump".equals(name)) {
            return tone(t, 420 + 410 * progress) * envelope;
        }
        if ("double_jump".equals(name)) {
            return (tone(t, 620 + 520 * progress) * 0.82 + tone(t, 1240 + 360 * progress) * 0.18) * envelope;
        }
        if ("throw".equals(name)) {
            double sweep = tone(t, 760 - 360 * progress);
            double air = noise(t, 17) * Math.pow(1.0 - progress, 1.8) * 0.18;
            return (sweep * 0.82 + air) * envelope;
        }
        if ("hit".equals(name)) {
            double thud = tone(t, 210 - 70 * progress) * 0.58;
            double crack = noise(t, 31) * Math.pow(1.0 - progress, 2.2) * 0.42;
            return (thud + crack) * envelope;
        }
        if ("hurt".equals(name)) {
            return (tone(t, 230 - 105 * progress) * 0.72 + noise(t, 43) * 0.22) * envelope;
        }
        if ("medal".equals(name)) {
            double first = progress < 0.48 ? tone(t, 880) : 0.0;
            double second = progress > 0.28 && progress < 0.78 ? tone(t, 1180) : 0.0;
            double shine = progress > 0.56 ? tone(t, 1560) * 0.45 : 0.0;
            return (first * 0.62 + second * 0.54 + shine) * envelope;
        }
        return tone(t, 440) * envelope;
    }

    private double tone(double t, double frequencyHz) {
        return Math.sin(2.0 * Math.PI * frequencyHz * t);
    }

    private double noise(double t, int seed) {
        double value = Math.sin((t * 12000.0 + seed) * 12.9898) * 43758.5453;
        return (value - Math.floor(value)) * 2.0 - 1.0;
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
        float dpadSize = dp(100);
        dpadBounds.set(dp(10), bottom - dpadSize, dp(10) + dpadSize, bottom);
        leftPadBounds.set(dpadBounds.left, dpadBounds.top + dpadSize * 0.32f, dpadBounds.left + dpadSize * 0.40f, dpadBounds.bottom - dpadSize * 0.32f);
        rightPadBounds.set(dpadBounds.right - dpadSize * 0.40f, dpadBounds.top + dpadSize * 0.32f, dpadBounds.right, dpadBounds.bottom - dpadSize * 0.32f);
        aimUpPadBounds.set(dpadBounds.left + dpadSize * 0.32f, dpadBounds.top, dpadBounds.right - dpadSize * 0.32f, dpadBounds.top + dpadSize * 0.40f);
        aimDownPadBounds.set(dpadBounds.left + dpadSize * 0.32f, dpadBounds.bottom - dpadSize * 0.40f, dpadBounds.right - dpadSize * 0.32f, dpadBounds.bottom);
        sprayPadBounds.set(getWidth() - dp(190), bottom - size, getWidth() - dp(190) + size, bottom);
        jumpPadBounds.set(getWidth() - dp(132), bottom - size, getWidth() - dp(132) + size, bottom);
        firePadBounds.set(getWidth() - dp(74), bottom - size, getWidth() - dp(74) + size, bottom);

        drawDpad(canvas);
        drawControlButton(canvas, sprayPadBounds, "SPRAY " + bearSprayCharges, sprayPressed || bearSprayCooldown > 0f);
        drawControlButton(canvas, jumpPadBounds, "JUMP", jumpPressed);
        drawControlButton(canvas, firePadBounds, "FIRE", firePressed || shotCooldown > 0f);
    }

    private void drawDpad(Canvas canvas) {
        float cx = dpadBounds.centerX();
        float cy = dpadBounds.centerY();
        float r = dpadBounds.width() * 0.5f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(46, 0, 0, 0));
        canvas.drawCircle(cx, cy + dp(3), r, paint);
        paint.setColor(Color.argb(82, 16, 25, 37));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setColor(Color.argb(24, 255, 255, 255));
        canvas.drawCircle(cx, cy - r * 0.20f, r * 0.66f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(126, 255, 255, 255));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);

        drawDpadArrow(canvas, cx, cy - r * 0.56f, 0f, -1f, aimUpPressed);
        drawDpadArrow(canvas, cx, cy + r * 0.56f, 0f, 1f, aimDownPressed);
        drawDpadArrow(canvas, cx - r * 0.56f, cy, -1f, 0f, leftPressed);
        drawDpadArrow(canvas, cx + r * 0.56f, cy, 1f, 0f, rightPressed);

        float knobY = cy + aimPadY * r * 0.46f;
        float knobX = cx + (rightPressed ? r * 0.22f : leftPressed ? -r * 0.22f : 0f);
        paint.setColor(Color.argb(170, 255, 218, 121));
        canvas.drawCircle(knobX, knobY, dp(7), paint);
        paint.setColor(Color.rgb(24, 30, 38));
        canvas.drawCircle(knobX, knobY, dp(3), paint);
    }

    private void drawDpadArrow(Canvas canvas, float x, float y, float dirX, float dirY, boolean active) {
        float buttonRadius = dp(11.5f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.argb(195, 255, 218, 121) : Color.argb(118, 255, 255, 255));
        canvas.drawCircle(x, y, buttonRadius, paint);
        paint.setColor(Color.rgb(24, 30, 38));
        Path arrow = new Path();
        float tipX = x + dirX * dp(7);
        float tipY = y + dirY * dp(7);
        float backX = x - dirX * dp(5);
        float backY = y - dirY * dp(5);
        float sideX = -dirY * dp(5);
        float sideY = dirX * dp(5);
        arrow.moveTo(tipX, tipY);
        arrow.lineTo(backX + sideX, backY + sideY);
        arrow.lineTo(backX - sideX, backY - sideY);
        arrow.close();
        canvas.drawPath(arrow, paint);
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

    private void drawPausePanel(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(44), dp(340));
        float panelHeight = dp(216);
        float left = (getWidth() - panelWidth) / 2f;
        float top = (getHeight() - panelHeight) / 2f;
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(234, 10, 18, 29));
        canvas.drawRoundRect(panel, dp(18), dp(18), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(panel, dp(18), dp(18), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(25));
        canvas.drawText("TRAIL PAUSED", getWidth() / 2f, top + dp(45), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(12));
        canvas.drawText("Goal: " + STAGES[selectedStage].name, getWidth() / 2f, top + dp(76), textPaint);
        textPaint.setColor(Color.rgb(210, 232, 238));
        drawTextFit(canvas, stageActionVerb(selectedStage) + " " + STAGES[selectedStage].obstacleName + " · tap FIRE snowballs · tap SPRAY for bears", getWidth() / 2f, top + dp(98), panelWidth - dp(36), dp(12), dp(8.5f), Paint.Align.CENTER);
        canvas.drawText("Boss weak window: RECOVER", getWidth() / 2f, top + dp(118), textPaint);

        setButton(primaryButtonBounds, top + dp(151), dp(214), dp(38));
        drawButton(canvas, primaryButtonBounds, "RESUME");

        setButton(secondaryButtonBounds, top + dp(190), dp(104), dp(32));
        secondaryButtonBounds.offset(-dp(58), 0);
        setButton(thirdButtonBounds, top + dp(190), dp(104), dp(32));
        thirdButtonBounds.offset(dp(58), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "SPRITE");
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
        drawTextFit(canvas, deathLine(), getWidth() / 2f, top + dp(90), panelWidth - dp(32), dp(15), dp(10), Paint.Align.CENTER);

        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(255, 218, 121));
        float resultWidth = panelWidth - dp(30);
        drawTextFit(canvas, "Score " + score + (runNewBest ? " · NEW BEST" : "") + " · Jumps " + gatesPassed, getWidth() / 2f, top + dp(118), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, "Missions " + missionsCompleted + "/3 · Combo " + gameState.bestCombo + " · Flow " + bestCleanVaultStreak + " · Rank " + runRank(), getWidth() / 2f, top + dp(142), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, "Tokens +" + runTokensEarned + " · Bank " + trailTokens + " · Logs " + expeditionLogs, getWidth() / 2f, top + dp(166), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, dailyResultLine(), getWidth() / 2f, top + dp(188), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, expeditionLine(false), getWidth() / 2f, top + dp(210), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, badgeSummaryLine(), getWidth() / 2f, top + dp(232), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, nextGoalLine(false), getWidth() / 2f, top + dp(254), resultWidth, dp(13), dp(9), Paint.Align.CENTER);

        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Tap anywhere to retry", getWidth() / 2f, top + dp(277), textPaint);

        setButton(secondaryButtonBounds, top + dp(316), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(316), dp(118), dp(36));
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
        float resultWidth = panelWidth - dp(30);
        drawTextFit(canvas, STAGES[selectedStage].bossName + " defeated", getWidth() / 2f, top + dp(82), resultWidth, dp(15), dp(10), Paint.Align.CENTER);
        drawTextFit(canvas, "Score " + score + " · Best " + bestScore + (runNewBest ? " · NEW BEST" : ""), getWidth() / 2f, top + dp(108), resultWidth, dp(15), dp(10), Paint.Align.CENTER);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(dp(13));
        drawTextFit(canvas, "Rank " + runRank() + " · Missions " + missionsCompleted + "/3 · Stars " + gameState.stars + " · Flow " + bestCleanVaultStreak, getWidth() / 2f, top + dp(134), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, "Tokens +" + runTokensEarned + " · Bank " + trailTokens + " · Logs " + expeditionLogs, getWidth() / 2f, top + dp(160), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, dailyResultLine(), getWidth() / 2f, top + dp(184), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, expeditionLine(true), getWidth() / 2f, top + dp(207), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, badgeSummaryLine(), getWidth() / 2f, top + dp(230), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, stageClearLine(), getWidth() / 2f, top + dp(254), resultWidth, dp(13), dp(9), Paint.Align.CENTER);
        drawTextFit(canvas, nextGoalLine(true), getWidth() / 2f, top + dp(276), resultWidth, dp(13), dp(9), Paint.Align.CENTER);

        setButton(secondaryButtonBounds, top + dp(318), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(318), dp(118), dp(36));
        thirdButtonBounds.offset(dp(64), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "NEXT");
    }

    private void drawTopBrand(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(190, 0, 0, 0));
        float height = isLandscape() ? dp(76) : dp(112);
        canvas.drawRect(0, 0, getWidth(), height, paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(isLandscape() ? dp(8.5f) : dp(9.5f));
        canvas.drawText("TRIPPERDEE LABS · " + BuildConfig.BUILD_BADGE + (debugOverlay ? " · DEBUG" : ""),
                getWidth() / 2f, isLandscape() ? dp(18) : dp(25), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(isLandscape() ? dp(20) : dp(26));
        canvas.drawText(title, getWidth() / 2f, isLandscape() ? dp(45) : dp(61), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(isLandscape() ? dp(11) : dp(13));
        canvas.drawText(subtitle, getWidth() / 2f, isLandscape() ? dp(65) : dp(88), textPaint);
    }

    private void drawButton(Canvas canvas, RectF bounds, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawRoundRect(bounds, dp(15), dp(15), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.6f));
        paint.setColor(Color.rgb(82, 57, 18));
        canvas.drawRoundRect(bounds, dp(15), dp(15), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(24, 30, 38));
        drawTextFit(canvas, label, bounds.centerX(), bounds.centerY() + dp(5), bounds.width() - dp(18), dp(13), dp(9), Paint.Align.CENTER);
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
        paint.setColor(Color.argb(248, 10, 18, 29));
        canvas.drawRoundRect(bounds, dp(12), dp(12), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(Color.argb(220, 210, 232, 238));
        canvas.drawRoundRect(bounds, dp(12), dp(12), paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.WHITE);
        drawTextFit(canvas, label, bounds.centerX(), bounds.centerY() + dp(4), bounds.width() - dp(12), dp(12), dp(8.5f), Paint.Align.CENTER);
    }

    private void drawPassportScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "TRAIL PASSPORT", "Collection of Alaska expedition badges.");

        float panelWidth = Math.min(getWidth() - dp(40), dp(460));
        float panelHeight = getHeight() - (isLandscape() ? dp(100) : dp(160));
        float left = (getWidth() - panelWidth) / 2f;
        float top = isLandscape() ? dp(86) : dp(124);
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 12, 18, 30));
        canvas.drawRoundRect(panel, dp(18), dp(18), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.argb(160, 255, 218, 121));
        canvas.drawRoundRect(panel, dp(18), dp(18), paint);
        paint.setStyle(Paint.Style.FILL);

        int cols = isLandscape() ? 5 : 2;
        int rows = (TrailBadgeCatalog.BADGE_COUNT + cols - 1) / cols;
        float slotWidth = panelWidth / cols;
        float slotHeight = panelHeight / rows;

        for (int i = 0; i < TrailBadgeCatalog.BADGE_COUNT; i++) {
            int r = i / cols;
            int c = i % cols;
            float cx = left + c * slotWidth + slotWidth / 2f;
            float cy = top + r * slotHeight + slotHeight / 2f;
            boolean has = TrailBadgeCatalog.hasBadge(trailBadgeMask, i);

            float rBase = Math.min(slotWidth, slotHeight) * 0.35f;
            if (has) {
                paint.setColor(Color.rgb(255, 218, 121));
                canvas.drawCircle(cx, cy - dp(8), rBase, paint);
                paint.setColor(Color.WHITE);
                textPaint.setTextSize(dp(11));
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("★", cx, cy - dp(8) + dp(4), textPaint);
            } else {
                paint.setColor(Color.argb(60, 255, 255, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1.5f));
                canvas.drawCircle(cx, cy - dp(8), rBase, paint);
                paint.setStyle(Paint.Style.FILL);
            }

            textPaint.setTextSize(dp(9));
            textPaint.setColor(has ? Color.WHITE : Color.argb(120, 255, 255, 255));
            canvas.drawText(TrailBadgeCatalog.badgeName(i).toUpperCase(), cx, cy + rBase + dp(8), textPaint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(12));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("Tap anywhere to return", getWidth() / 2f, top + panelHeight + dp(24), textPaint);
    }

    private void drawTextFit(Canvas canvas, String label, float x, float baseline, float maxWidth, float preferredSize, float minSize, Paint.Align align) {
        textPaint.setTextAlign(align);
        float size = preferredSize;
        textPaint.setTextSize(size);
        while (size > minSize && textPaint.measureText(label) > maxWidth) {
            size -= dp(0.5f);
            textPaint.setTextSize(size);
        }
        canvas.drawText(label, x, baseline, textPaint);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, float left, float top, float right, float bottom) {
        if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
            android.graphics.drawable.BitmapDrawable bitmapDrawable = (android.graphics.drawable.BitmapDrawable) drawable;
            bitmapDrawable.setFilterBitmap(true);
            bitmapDrawable.setAntiAlias(true);
        }
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
        demoMode = false;
        dailyRushMode = true;
        selectedStage = dailyStageIndex();
        selectedSeason = STAGES[selectedStage].season;
        backdropCacheKey = Integer.MIN_VALUE;
        saveChoices();
        logEvent("Daily Rush selected: " + STAGES[selectedStage].name + ".");
        startGame();
    }

    private void startComputerDemo() {
        demoMode = true;
        dailyRushMode = false;
        demoTimer = 0f;
        selectedStage = 0;
        selectedSeason = STAGES[selectedStage].season;
        backdropCacheKey = Integer.MIN_VALUE;
        logEvent("Computer Run demo started.");
        startGame();
    }

    private void saveChoices() {
        prefs.edit()
                .putInt(PREF_SELECTED_STAGE, selectedStage)
                .putInt(PREF_SELECTED_SEASON, selectedSeason)
                .putInt(PREF_OUTFIT, effectiveOutfitIndex())
                .putInt(PREF_BODY_STYLE, selectedBodyStyle)
                .putInt(PREF_TRAIL_TOKENS, trailTokens)
                .putInt(PREF_UNLOCKED_OUTFITS, unlockedOutfitMask)
                .putInt(PREF_TOTAL_MISSIONS, totalMissionsCompleted)
                .putInt(PREF_TRAIL_BADGES, trailBadgeMask)
                .putInt(PREF_DAILY_COMPLETED_DAY, dailyCompletedDay)
                .putInt(PREF_DAILY_STREAK, dailyStreak)
                .putInt(PREF_EXPEDITION_LOGS, expeditionLogs)
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
        return stage.name + " · " + dailyGateGoal() + " vaults · +" + reward;
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

    private String nextGoalLine(boolean stageCleared) {
        if (stageCleared && selectedStage < STAGES.length - 1) {
            return "Next: " + STAGES[selectedStage + 1].name + " · " + STAGES[selectedStage + 1].bossName;
        }
        if (stageCleared) {
            return "Next: S rank, perfect clear, passport badges.";
        }
        if (gatesPassed < STAGES[selectedStage].goalGates) {
            return "Next: " + stageActionVerb(selectedStage).toLowerCase(Locale.ROOT) + " " + STAGES[selectedStage].goalGates + " " + STAGES[selectedStage].obstacleName + ".";
        }
        return "Next: fire during RECOVER for weak-window double damage.";
    }

    private String missionBriefLine() {
        return missionStarGoal + " STARS";
    }

    private String stageRuleLine(StageConfig stage) {
        if (selectedStage == 0 || selectedStage == 1) {
            return "VAULT " + stage.obstacleName + ". Tap FIRE blasts logs; SPRAY stuns wildlife.";
        }
        return stageActionVerb(selectedStage) + " " + stage.obstacleName + ". Tap FIRE throws; SPRAY stuns.";
    }

    private String obstacleHudName(int stageIndex) {
        if (stageIndex == 0) return "LOGS";
        if (stageIndex == 1) return "LOGS";
        if (stageIndex == 2) return "ANTLERS";
        if (stageIndex == 3) return "ICEBERGS";
        if (stageIndex == 4) return "SNOWBANKS";
        return STAGES[stageIndex].obstacleName;
    }

    private String stageActionVerb(int stageIndex) {
        if (stageIndex == 0) return "CLEAR";
        if (stageIndex == 1) return "VAULT";
        if (stageIndex == 2) return "VAULT";
        if (stageIndex == 3) return "LEAP";
        if (stageIndex == 4) return "SURVIVE";
        return "CLEAR";
    }

    private String stageCounterLabel(int stageIndex) {
        if (stageIndex == 3) return "LEAPS";
        if (stageIndex == 4) return "SURVIVE";
        return "VAULTS";
    }

    private String missionProgressLine() {
        return "MISSIONS " + missionsCompleted + "/3  " + stageCounterLabel(selectedStage) + " " + Math.min(gatesPassed, STAGES[selectedStage].goalGates) + "/" + STAGES[selectedStage].goalGates
                + "  STARS " + gameState.stars + "/" + missionStarGoal
                + "  COMBO " + gameState.bestCombo + "/" + missionComboGoal;
    }

    private int expeditionGradeScore(boolean stageCleared) {
        int score = 0;
        if (stageCleared) score += 2;
        if (routeMilestoneIndex >= 3) score += 2;
        if (campReached) score += 1;
        if (runFocusPickups > 0) score += 1;
        if (runLogsBlasted >= 2) score += 1;
        if (runTrailMaps > 0) score += 1;
        if (runRescueKits > 0) score += 1;
        if (runCleanVaults >= 2) score += 1;
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
                + " · Maps " + runTrailMaps
                + " · Kits " + runRescueKits
                + " · Logs " + runLogsBlasted
                + " · Vaults " + runCleanVaults;
    }

    private String bossPatternLabel() {
        if (!bossActive) {
            return "";
        }
        if (bossState == BOSS_STATE_TELL) {
            if (bossPattern == BOSS_PATTERN_LUNGE) return "TELL: CHARGE";
            if (bossPattern == BOSS_PATTERN_SNOW_WAVE) return "TELL: ICE";
            if (bossPattern == BOSS_PATTERN_LASER) return "TELL: EYE BEAM";
            return "TELL: SUMMON";
        }
        if (bossState == BOSS_STATE_ATTACK) {
            if (bossPattern == BOSS_PATTERN_LUNGE) return "CHARGE";
            if (bossPattern == BOSS_PATTERN_SNOW_WAVE) return "ICE";
            if (bossPattern == BOSS_PATTERN_LASER) return "BEAM DODGE";
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
        if (state == STATE_PAUSED) return "paused";
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
        float fractionLine = getHeight() * GROUND_LINE_HEIGHT_FRACTION;
        float minLine = getHeight() * 0.6f;
        float maxLine = getHeight() - dp(PLAYFIELD_BOTTOM_MARGIN_DP);
        return clamp(fractionLine, minLine, maxLine);
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
        /*
         * Android screens have different densities. dp() converts a design size
         * into real pixels so a 20dp object looks about the same physical size
         * on different phones.
         */
        return value * getResources().getDisplayMetrics().density;
    }

    private float gameplayDp(float value) {
        /*
         * Gameplay uses a slightly smaller scale than UI. That gives the runner
         * and obstacles more room to move on small screens.
         */
        return dp(value * 0.82f);
    }

    /*
     * The small classes below are data containers. They do not draw or update
     * themselves; MooseRushView owns the game loop and changes their fields.
     *
     * This is a friendly first architecture for a small game:
     * - data objects store what exists
     * - update methods change the data
     * - draw methods turn the data into pictures
     */
    private static class StageConfig {
        // Human-readable title shown in menus.
        final String name;
        // Short description shown near the stage.
        final String line;
        // Which background/weather palette this stage uses.
        final int season;
        // Boss name used in UI messages and defeat text.
        final String bossName;
        // Main moving hazard label, used to pick sprites and behavior.
        final String hazardLabel;
        // Gate/obstacle name used in UI messages.
        final String obstacleName;
        // How many gates the player clears before the boss starts.
        final int goalGates;
        // Boss health is also how many good hits the stage asks for.
        final int bossHealth;
        // Base scroll speed in dp-ish tuning units.
        final int baseSpeed;
        // Starting time between gate spawns.
        final float spawnSeconds;
        // Art/behavior flavor for the boss.
        final int bossType;

        StageConfig(String name, String line, int season, String bossName, String hazardLabel, String obstacleName, int goalGates, int bossHealth, int baseSpeed, float spawnSeconds, int bossType) {
            this.name = name;
            this.line = line;
            this.season = season;
            this.bossName = bossName;
            this.hazardLabel = hazardLabel;
            this.obstacleName = obstacleName;
            this.goalGates = goalGates;
            this.bossHealth = bossHealth;
            this.baseSpeed = baseSpeed;
            this.spawnSeconds = spawnSeconds;
            this.bossType = bossType;
        }
    }

    private static class Gate {
        // x moves left every frame until the gate leaves the screen.
        float x;
        // height and width define the rectangle the player must jump over.
        final float height;
        final float width;
        // passed prevents awarding score more than once for the same gate.
        boolean passed = false;

        Gate(float x, float height, float width) {
            this.x = x;
            this.height = height;
            this.width = width;
        }
    }

    private static class RoutePlatform {
        float x;
        float y;
        final float baseY;
        final float width;
        final boolean moving;
        final boolean brittle;
        final float phase;
        float age = 0f;
        boolean broken = false;
        int hits = 0;

        RoutePlatform(float x, float y, float width, boolean moving, boolean brittle, float phase) {
            this.x = x;
            this.y = y;
            this.baseY = y;
            this.width = width;
            this.moving = moving;
            this.brittle = brittle;
            this.phase = phase;
        }
    }

    private static class Hazard {
        // Current center position of the hazard.
        float x;
        float y;
        // Original ground/lane y; animation offsets are added on top.
        final float baseY;
        // Main size used for drawing and hit checks.
        final float radius;
        // Speed starts here so temporary effects can change speedMultiplier.
        final float baseSpeedMultiplier;
        float speedMultiplier;
        // Random-ish offset so multiple hazards do not animate in sync.
        final float phase;
        // Label chooses art and special behavior.
        final String label;
        final Drawable drawable;
        // Gameplay bookkeeping flags.
        boolean passed = false;
        boolean nearMissAwarded = false;
        boolean roaring = false;
        boolean roarUsed = false;
        // Some wolves pounce; the phase makes that choice deterministic.
        final boolean pouncingWolf;
        float roarTimer = 0f;
        float age = 0f;
        int behaviorState = 0;
        float intentTimer = 0f;
        float targetY = 0f;
        boolean committed = false;
        boolean environmentHit = false;

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
            this.pouncingWolf = "WOLF".equals(label) && ((int) (phase * 1000f) & 1) == 0;
        }
    }

    private static class Shot {
        float x;
        float y;
        final float speed;
        float vy;
        final float radius;
        final boolean empowered;
        float wobble = 0f;
        float age = 0f;

        Shot(float x, float y, float speed, float vy, float radius, boolean empowered) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.vy = vy;
            this.radius = radius;
            this.empowered = empowered;
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
        float y;
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
