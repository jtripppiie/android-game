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

    private static final int STATE_SPLASH = 0;
    private static final int STATE_MENU = 1;
    private static final int STATE_MAP = 2;
    private static final int STATE_CUSTOMIZE = 3;
    private static final int STATE_RUNNING = 4;
    private static final int STATE_GAME_OVER = 5;

    private static final String[] ALASKA_STAGES = {
            "Midnight Sun Run",
            "Salmon Rush",
            "Moose Pass",
            "Dark Winter",
            "Bear Country"
    };

    private static final String[] ALASKA_STAGE_LINES = {
            "Warm up under the endless sun.",
            "Flying fish. Questionable physics.",
            "Antlers everywhere. Stay humble.",
            "Low light, high panic.",
            "The bear is not impressed."
    };

    private static final String[] ALASKA_BOSSES = {
            "Sunburn Sprite",
            "Salmon Boss",
            "Moose Boss",
            "Darkness Boss",
            "Bear Boss"
    };

    private static final String[] SEASONS = {
            "Summer",
            "Winter",
            "Midnight Sun",
            "Darkness"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Gate> gates = new ArrayList<>();
    private final SharedPreferences prefs;

    private final RectF primaryButtonBounds = new RectF();
    private final RectF secondaryButtonBounds = new RectF();
    private final RectF thirdButtonBounds = new RectF();
    private final RectF photoButtonBounds = new RectF();
    private final RectF backButtonBounds = new RectF();
    private final RectF seasonButtonBounds = new RectF();
    private final RectF topHitBox = new RectF();
    private final RectF bottomHitBox = new RectF();
    private final RectF bodyBounds = new RectF();
    private final Matrix photoMatrix = new Matrix();

    private PhotoRequestListener photoRequestListener;
    private Bitmap playerPhoto;
    private int state = STATE_SPLASH;
    private boolean paused = false;

    private long lastFrameNanos = 0L;
    private float splashTimer = 0f;
    private float spawnCooldown = 0f;
    private float groundScroll = 0f;
    private float spriteClock = 0f;
    private float bossTimer = 0f;
    private boolean bossActive = false;
    private boolean bossDefeated = false;
    private float bossX = 0f;
    private float bossY = 0f;
    private int score = 0;
    private int bestScore = 0;
    private int selectedStage = 0;
    private int selectedSeason = 2;

    private float playerX;
    private float playerY;
    private float playerVelocityY;
    private float playerRadius;

    public MooseRushView(Context context) {
        super(context);
        setFocusable(true);
        prefs = context.getSharedPreferences("moose_rush", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("best_score", 0);
        selectedStage = prefs.getInt("selected_stage", 0);
        selectedSeason = prefs.getInt("selected_season", 2);

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setPhotoRequestListener(PhotoRequestListener listener) {
        this.photoRequestListener = listener;
    }

    public void setPlayerPhoto(Bitmap photo) {
        this.playerPhoto = photo;
        invalidate();
    }

    public void resume() {
        paused = false;
        lastFrameNanos = 0L;
        postInvalidateOnAnimation();
    }

    public void pause() {
        paused = true;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        playerRadius = dp(24);
        playerX = width * 0.32f;
        playerY = height * 0.45f;
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

        if (!paused && state == STATE_RUNNING) {
            updateGame(dt);
        } else if (!paused) {
            spriteClock += dt * 2.5f;
            if (state == STATE_SPLASH) {
                splashTimer += dt;
                if (splashTimer > 1.8f) {
                    state = STATE_MENU;
                }
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
            if (state == STATE_GAME_OVER) {
                drawGameOverPanel(canvas);
            }
        }

        if (!paused) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        if (state == STATE_SPLASH) {
            state = STATE_MENU;
            return true;
        }

        if (state == STATE_MENU) {
            if (primaryButtonBounds.contains(x, y)) {
                startGame();
            } else if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_MAP;
            } else if (thirdButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
            }
            return true;
        }

        if (state == STATE_MAP) {
            if (backButtonBounds.contains(x, y)) {
                state = STATE_MENU;
                return true;
            }
            int tappedStage = findTappedStage(x, y);
            if (tappedStage >= 0) {
                selectedStage = tappedStage;
                selectedSeason = seasonForStage(tappedStage);
                saveChoices();
                startGame();
            }
            return true;
        }

        if (state == STATE_CUSTOMIZE) {
            if (backButtonBounds.contains(x, y)) {
                saveChoices();
                state = STATE_MENU;
            } else if (photoButtonBounds.contains(x, y)) {
                if (photoRequestListener != null) {
                    photoRequestListener.onPhotoRequested();
                }
            } else if (seasonButtonBounds.contains(x, y)) {
                selectedSeason = (selectedSeason + 1) % SEASONS.length;
                saveChoices();
            }
            return true;
        }

        if (state == STATE_GAME_OVER) {
            if (secondaryButtonBounds.contains(x, y)) {
                state = STATE_MAP;
                return true;
            }
            if (thirdButtonBounds.contains(x, y)) {
                state = STATE_CUSTOMIZE;
                return true;
            }
            startGame();
            flap();
            return true;
        }

        if (state == STATE_RUNNING) {
            flap();
        }
        return true;
    }

    private void startGame() {
        gates.clear();
        score = 0;
        spawnCooldown = 0.65f;
        groundScroll = 0f;
        spriteClock = 0f;
        bossTimer = 0f;
        bossActive = false;
        bossDefeated = false;
        state = STATE_RUNNING;
        playerX = getWidth() * 0.32f;
        playerY = getHeight() * 0.45f;
        playerVelocityY = 0f;
    }

    private void flap() {
        playerVelocityY = -dp(430);
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        float gateSpeed = dp(190) + Math.min(dp(90), score * dp(4));
        float gravity = dp(1120);
        spriteClock += dt * (5.5f + Math.min(5f, score * 0.22f));
        playerVelocityY += gravity * dt;
        playerY += playerVelocityY * dt;
        groundScroll = (groundScroll + gateSpeed * dt) % dp(48);

        if (!bossDefeated && !bossActive && score >= bossTriggerScore()) {
            startBossPhase();
        }

        if (bossActive) {
            updateBoss(dt);
        } else {
            updateGates(dt, gateSpeed);
        }

        if (playerY - playerRadius < dp(44) || playerY + playerRadius > getGroundY()) {
            endGame();
            return;
        }

        if (bossActive && circleHitsCircle(playerX, playerY, playerRadius * 0.9f, bossX, bossY, bossRadius())) {
            endGame();
            return;
        }

        for (Gate gate : gates) {
            if (hitsGate(gate)) {
                endGame();
                return;
            }
        }
    }

    private void updateGates(float dt, float gateSpeed) {
        spawnCooldown -= dt;
        if (spawnCooldown <= 0f) {
            spawnGate();
            spawnCooldown = Math.max(0.95f, 1.35f - score * 0.025f);
        }

        Iterator<Gate> iterator = gates.iterator();
        while (iterator.hasNext()) {
            Gate gate = iterator.next();
            gate.x -= gateSpeed * dt;

            if (!gate.passed && gate.x + gate.width < playerX) {
                gate.passed = true;
                score++;
            }

            if (gate.x + gate.width < -dp(24)) {
                iterator.remove();
            }
        }
    }

    private void startBossPhase() {
        gates.clear();
        bossActive = true;
        bossTimer = 0f;
        bossX = getWidth() + dp(92);
        bossY = getHeight() * 0.42f;
    }

    private void updateBoss(float dt) {
        bossTimer += dt;
        bossX -= dp(128) * dt;
        bossY = getHeight() * 0.42f + (float) Math.sin(bossTimer * 4.2f) * dp(80);

        if (bossTimer > 8.5f || bossX < -dp(120)) {
            bossActive = false;
            bossDefeated = true;
            score += 5;
        }
    }

    private void endGame() {
        state = STATE_GAME_OVER;
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt("best_score", bestScore).apply();
        }
    }

    private int bossTriggerScore() {
        return 4 + selectedStage;
    }

    private float bossRadius() {
        return selectedStage == 4 ? dp(42) : dp(34);
    }

    private void spawnGate() {
        float gateWidth = dp(72);
        float gapHeight = Math.max(dp(148), dp(196) - score * dp(1.2f));
        float safeTop = dp(110);
        float safeBottom = getGroundY() - dp(100);
        float minGapCenter = safeTop + gapHeight / 2f;
        float maxGapCenter = safeBottom - gapHeight / 2f;
        float gapCenter = minGapCenter + random.nextFloat() * Math.max(dp(1), maxGapCenter - minGapCenter);
        gates.add(new Gate(getWidth() + gateWidth, gapCenter, gapHeight, gateWidth));
    }

    private boolean hitsGate(Gate gate) {
        topHitBox.set(gate.x, 0, gate.x + gate.width, gate.gapCenter - gate.gapHeight / 2f);
        bottomHitBox.set(gate.x, gate.gapCenter + gate.gapHeight / 2f, gate.x + gate.width, getGroundY());
        return circleHitsRect(playerX, playerY, playerRadius * 0.86f, topHitBox)
                || circleHitsRect(playerX, playerY, playerRadius * 0.86f, bottomHitBox);
    }

    private void drawSplashScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        paint.setColor(Color.argb(180, 0, 0, 0));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(20));
        canvas.drawText("TRIPPERDEELABS", getWidth() / 2f, getHeight() * 0.42f, textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(42));
        canvas.drawText("YOU RUSH", getWidth() / 2f, getHeight() * 0.50f, textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(210, 232, 238));
        canvas.drawText("Alaska prototype booting...", getWidth() / 2f, getHeight() * 0.57f, textPaint);
    }

    private void drawMenuScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "YOU RUSH: ALASKA", "Upload your face. Survive local chaos.");

        drawCharacter(canvas, getWidth() / 2f, getHeight() * 0.36f, dp(28));

        float y = getHeight() * 0.55f;
        setButton(primaryButtonBounds, y, dp(210), dp(48));
        setButton(secondaryButtonBounds, y + dp(62), dp(210), dp(48));
        setButton(thirdButtonBounds, y + dp(124), dp(210), dp(48));

        drawButton(canvas, primaryButtonBounds, "PLAY " + ALASKA_STAGES[selectedStage]);
        drawButton(canvas, secondaryButtonBounds, "ALASKA MAP");
        drawButton(canvas, thirdButtonBounds, "CUSTOMIZE");

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Best Score " + bestScore, getWidth() / 2f, getHeight() - dp(35), textPaint);
    }

    private void drawMapScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "ALASKA MAP", "Pick a stage. Other regions come later.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        float startY = dp(138);
        float gap = dp(76);
        for (int i = 0; i < ALASKA_STAGES.length; i++) {
            RectF node = stageBounds(i, startY, gap);
            boolean selected = i == selectedStage;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selected ? Color.rgb(255, 218, 121) : Color.argb(220, 16, 25, 37));
            canvas.drawRoundRect(node, dp(16), dp(16), paint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(selected ? Color.rgb(23, 29, 38) : Color.WHITE);
            textPaint.setTextSize(dp(18));
            canvas.drawText((i + 1) + ". " + ALASKA_STAGES[i], node.left + dp(16), node.top + dp(28), textPaint);

            textPaint.setTextSize(dp(12));
            textPaint.setColor(selected ? Color.rgb(58, 65, 78) : Color.rgb(204, 223, 230));
            canvas.drawText(ALASKA_STAGE_LINES[i], node.left + dp(16), node.top + dp(50), textPaint);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(dp(11));
            canvas.drawText(ALASKA_BOSSES[i], node.right - dp(14), node.top + dp(50), textPaint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawCustomizeScreen(Canvas canvas) {
        drawAlaskaBackdrop(canvas);
        drawTopBrand(canvas, "CUSTOMIZE", "Make the tiny chaos person yours.");
        setBackButton();
        drawSmallButton(canvas, backButtonBounds, "BACK");

        drawCharacter(canvas, getWidth() / 2f, getHeight() * 0.34f, dp(34));

        float y = getHeight() * 0.53f;
        setButton(photoButtonBounds, y, dp(220), dp(48));
        setButton(seasonButtonBounds, y + dp(64), dp(220), dp(48));

        drawButton(canvas, photoButtonBounds, playerPhoto == null ? "ADD YOUR PHOTO" : "CHANGE PHOTO");
        drawButton(canvas, seasonButtonBounds, "SEASON: " + SEASONS[selectedSeason]);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Stage: " + ALASKA_STAGES[selectedStage], getWidth() / 2f, y + dp(140), textPaint);
    }

    private void drawWorld(Canvas canvas) {
        drawAlaskaBackdrop(canvas);

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }

        if (bossActive) {
            drawBoss(canvas);
        }

        drawCharacter(canvas, playerX, playerY, playerRadius);
        drawGround(canvas, getWidth());
    }

    private void drawAlaskaBackdrop(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        boolean dark = selectedSeason == 3 || selectedStage == 3;
        boolean winter = selectedSeason == 1 || selectedStage == 3 || selectedStage == 4;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? Color.rgb(8, 18, 36) : Color.rgb(25, 43, 68));
        canvas.drawRect(0, 0, width, height, paint);

        if (dark) {
            paint.setColor(Color.rgb(94, 220, 169));
            canvas.drawOval(width * 0.10f, height * 0.10f, width * 0.92f, height * 0.24f, paint);
            paint.setColor(Color.rgb(8, 18, 36));
            canvas.drawOval(width * 0.10f, height * 0.13f, width * 0.92f, height * 0.28f, paint);
        } else {
            paint.setColor(selectedSeason == 2 ? Color.rgb(245, 205, 92) : Color.rgb(240, 210, 108));
            canvas.drawCircle(width - dp(58), dp(70), selectedSeason == 2 ? dp(34) : dp(24), paint);
        }

        paint.setColor(winter ? Color.rgb(218, 235, 238) : Color.rgb(38, 82, 92));
        drawMountain(canvas, -dp(30), height * 0.50f, width * 0.38f, height * 0.22f);
        drawMountain(canvas, width * 0.18f, height * 0.53f, width * 0.70f, height * 0.20f);
        drawMountain(canvas, width * 0.54f, height * 0.51f, width + dp(40), height * 0.23f);

        paint.setColor(Color.rgb(232, 242, 245));
        drawMountain(canvas, width * 0.22f, height * 0.52f, width * 0.48f, height * 0.35f);

        paint.setColor(winter ? Color.rgb(230, 239, 240) : Color.rgb(33, 92, 70));
        canvas.drawRect(0, getGroundY() - dp(24), width, getGroundY(), paint);
    }

    private void drawMountain(Canvas canvas, float left, float baseY, float right, float peakY) {
        float mid = (left + right) / 2f;
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(left, baseY);
        path.lineTo(mid, peakY);
        path.lineTo(right, baseY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawGround(Canvas canvas, int width) {
        float groundY = getGroundY();
        boolean winter = selectedSeason == 1 || selectedStage == 3 || selectedStage == 4;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(winter ? Color.rgb(225, 235, 238) : Color.rgb(46, 119, 82));
        canvas.drawRect(0, groundY, width, getHeight(), paint);

        paint.setColor(winter ? Color.rgb(190, 207, 216) : Color.rgb(35, 86, 63));
        for (float x = -groundScroll; x < width + dp(60); x += dp(48)) {
            canvas.drawRoundRect(x, groundY + dp(12), x + dp(28), groundY + dp(20), dp(4), dp(4), paint);
        }
    }

    private void drawGate(Canvas canvas, Gate gate) {
        float topBottom = gate.gapCenter - gate.gapHeight / 2f;
        float bottomTop = gate.gapCenter + gate.gapHeight / 2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selectedStage == 2 ? Color.rgb(92, 65, 48) : Color.rgb(104, 62, 36));
        canvas.drawRoundRect(gate.x, -dp(20), gate.x + gate.width, topBottom, dp(14), dp(14), paint);
        canvas.drawRoundRect(gate.x, bottomTop, gate.x + gate.width, getGroundY(), dp(14), dp(14), paint);

        paint.setColor(selectedStage == 2 ? Color.rgb(224, 217, 188) : Color.rgb(210, 173, 103));
        canvas.drawRoundRect(gate.x - dp(8), topBottom - dp(18), gate.x + gate.width + dp(8), topBottom + dp(7), dp(8), dp(8), paint);
        canvas.drawRoundRect(gate.x - dp(8), bottomTop - dp(7), gate.x + gate.width + dp(8), bottomTop + dp(18), dp(8), dp(8), paint);

        paint.setStrokeWidth(dp(3));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(232, 196, 123));
        canvas.drawLine(gate.x + dp(12), topBottom - dp(18), gate.x - dp(4), topBottom - dp(36), paint);
        canvas.drawLine(gate.x + gate.width - dp(12), topBottom - dp(18), gate.x + gate.width + dp(4), topBottom - dp(36), paint);
        canvas.drawLine(gate.x + dp(12), bottomTop + dp(18), gate.x - dp(4), bottomTop + dp(36), paint);
        canvas.drawLine(gate.x + gate.width - dp(12), bottomTop + dp(18), gate.x + gate.width + dp(4), bottomTop + dp(36), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBoss(Canvas canvas) {
        float radius = bossRadius();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawCircle(bossX, bossY, radius + dp(6), paint);

        if (selectedStage == 1) {
            paint.setColor(Color.rgb(228, 96, 76));
            canvas.drawOval(bossX - radius * 1.2f, bossY - radius * 0.55f, bossX + radius * 1.2f, bossY + radius * 0.55f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(bossX + radius * 0.55f, bossY - radius * 0.12f, dp(4), paint);
        } else if (selectedStage == 2) {
            paint.setColor(Color.rgb(116, 75, 46));
            canvas.drawCircle(bossX, bossY, radius, paint);
            paint.setStrokeWidth(dp(5));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(bossX - radius * 0.55f, bossY - radius * 0.8f, bossX - radius * 1.25f, bossY - radius * 1.35f, paint);
            canvas.drawLine(bossX + radius * 0.55f, bossY - radius * 0.8f, bossX + radius * 1.25f, bossY - radius * 1.35f, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (selectedStage == 4) {
            paint.setColor(Color.rgb(73, 48, 35));
            canvas.drawCircle(bossX, bossY, radius, paint);
            canvas.drawCircle(bossX - radius * 0.58f, bossY - radius * 0.65f, radius * 0.34f, paint);
            canvas.drawCircle(bossX + radius * 0.58f, bossY - radius * 0.65f, radius * 0.34f, paint);
        } else {
            paint.setColor(Color.rgb(94, 220, 169));
            canvas.drawCircle(bossX, bossY, radius, paint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(ALASKA_BOSSES[selectedStage], getWidth() / 2f, dp(104), textPaint);
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
        canvas.drawRoundRect(
                x - radius * 0.35f,
                headY + radius * 0.25f,
                x + radius * 0.35f,
                headY + radius * 0.38f,
                dp(5),
                dp(5),
                paint
        );
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
        textPaint.setTextSize(dp(42));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(score), getWidth() / 2f, dp(66), textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(14));
        canvas.drawText("Best " + bestScore, getWidth() - dp(18), dp(32), textPaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(13));
        canvas.drawText(ALASKA_STAGES[selectedStage], dp(18), dp(32), textPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawGameOverPanel(Canvas canvas) {
        float panelWidth = Math.min(getWidth() - dp(40), dp(340));
        float panelHeight = dp(218);
        float left = (getWidth() - panelWidth) / 2f;
        float top = (getHeight() - panelHeight) / 2f;
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(222, 14, 21, 31));
        canvas.drawRoundRect(panel, dp(22), dp(22), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(34));
        canvas.drawText("BONKED", getWidth() / 2f, top + dp(52), textPaint);

        textPaint.setTextSize(dp(15));
        canvas.drawText(deathLine(), getWidth() / 2f, top + dp(92), textPaint);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText("Tap anywhere to retry", getWidth() / 2f, top + dp(122), textPaint);

        setButton(secondaryButtonBounds, top + dp(146), dp(118), dp(36));
        secondaryButtonBounds.offset(-dp(64), 0);
        setButton(thirdButtonBounds, top + dp(146), dp(118), dp(36));
        thirdButtonBounds.offset(dp(64), 0);
        drawSmallButton(canvas, secondaryButtonBounds, "MAP");
        drawSmallButton(canvas, thirdButtonBounds, "CUSTOMIZE");
    }

    private void drawTopBrand(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(140, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), dp(112), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 218, 121));
        textPaint.setTextSize(dp(12));
        canvas.drawText("TRIPPERDEELABS", getWidth() / 2f, dp(26), textPaint);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(29));
        canvas.drawText(title, getWidth() / 2f, dp(62), textPaint);

        textPaint.setColor(Color.rgb(210, 232, 238));
        textPaint.setTextSize(dp(14));
        canvas.drawText(subtitle, getWidth() / 2f, dp(88), textPaint);
    }

    private void drawButton(Canvas canvas, RectF bounds, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(232, 255, 218, 121));
        canvas.drawRoundRect(bounds, dp(15), dp(15), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(14));
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

    private void setButton(RectF bounds, float centerY, float width, float height) {
        float left = (getWidth() - width) / 2f;
        bounds.set(left, centerY - height / 2f, left + width, centerY + height / 2f);
    }

    private void setBackButton() {
        backButtonBounds.set(dp(16), dp(16), dp(88), dp(52));
    }

    private RectF stageBounds(int index, float startY, float gap) {
        float left = dp(24);
        float right = getWidth() - dp(24);
        float top = startY + index * gap;
        return new RectF(left, top, right, top + dp(62));
    }

    private int findTappedStage(float x, float y) {
        float startY = dp(138);
        float gap = dp(76);
        for (int i = 0; i < ALASKA_STAGES.length; i++) {
            if (stageBounds(i, startY, gap).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private int seasonForStage(int stage) {
        if (stage == 0) {
            return 2;
        }
        if (stage == 3 || stage == 4) {
            return 3;
        }
        return 0;
    }

    private void saveChoices() {
        prefs.edit()
                .putInt("selected_stage", selectedStage)
                .putInt("selected_season", selectedSeason)
                .apply();
    }

    private String deathLine() {
        if (bossActive) {
            return ALASKA_BOSSES[selectedStage] + " got you.";
        }
        if (score == 0) {
            return "Score 0. Alaska remains undefeated.";
        }
        if (score < 5) {
            return "Score " + score + ". Blame the moose.";
        }
        if (score < 10) {
            return "Score " + score + ". Almost tourist-proof.";
        }
        return "Score " + score + ". Certified chaos legend.";
    }

    private float getGroundY() {
        return getHeight() - dp(72);
    }

    private boolean circleHitsRect(float cx, float cy, float radius, RectF rect) {
        float closestX = clamp(cx, rect.left, rect.right);
        float closestY = clamp(cy, rect.top, rect.bottom);
        float dx = cx - closestX;
        float dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }

    private boolean circleHitsCircle(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx;
        float dy = ay - by;
        float radius = ar + br;
        return dx * dx + dy * dy < radius * radius;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class Gate {
        float x;
        final float gapCenter;
        final float gapHeight;
        final float width;
        boolean passed = false;

        Gate(float x, float gapCenter, float gapHeight, float width) {
            this.x = x;
            this.gapCenter = gapCenter;
            this.gapHeight = gapHeight;
            this.width = width;
        }
    }
}
