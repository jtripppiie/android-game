package com.jtripppiie.mooserush;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MooseRushView extends View {
    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_GAME_OVER = 2;

    private static final int TYPE_MOOSE = 0;
    private static final int TYPE_BEAR = 1;
    private static final int TYPE_FISH = 2;
    private static final int TYPE_ICE = 3;
    private static final int TYPE_VAN = 4;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final SharedPreferences prefs;

    private int state = STATE_READY;
    private boolean paused = false;

    private long lastFrameNanos = 0L;
    private float spawnCooldown = 0f;
    private float score = 0f;
    private int bestScore = 0;

    private float playerX;
    private float playerY;
    private float targetX;
    private float playerRadius;
    private float roadLeft;
    private float roadRight;

    public MooseRushView(Context context) {
        super(context);
        setFocusable(true);
        prefs = context.getSharedPreferences("moose_rush", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("best_score", 0);

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
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
        roadLeft = dp(24);
        roadRight = width - dp(24);
        playerRadius = dp(22);
        playerX = width / 2f;
        targetX = playerX;
        playerY = height - dp(96);
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
        }

        drawWorld(canvas);
        drawHud(canvas);

        if (state == STATE_READY) {
            drawCenterPanel(canvas, "MOOSE RUSH", "Drag to dodge Alaska chaos", "Tap to start");
        } else if (state == STATE_GAME_OVER) {
            drawCenterPanel(canvas, "WIPED OUT", "Score: " + (int) score + "   Best: " + bestScore, "Tap to retry");
        }

        if (!paused) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (state == STATE_READY || state == STATE_GAME_OVER) {
                startGame();
            }
            targetX = clamp(event.getX(), roadLeft + playerRadius, roadRight - playerRadius);
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            targetX = clamp(event.getX(), roadLeft + playerRadius, roadRight - playerRadius);
            return true;
        }

        return true;
    }

    private void startGame() {
        obstacles.clear();
        score = 0f;
        spawnCooldown = 0.2f;
        state = STATE_RUNNING;
        playerX = getWidth() / 2f;
        targetX = playerX;
    }

    private void updateGame(float dt) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        score += dt * 12f;
        playerX += (targetX - playerX) * Math.min(1f, dt * 12f);
        playerX = clamp(playerX, roadLeft + playerRadius, roadRight - playerRadius);

        float worldSpeed = dp(250) + Math.min(dp(430), score * dp(1.2f));
        spawnCooldown -= dt;
        if (spawnCooldown <= 0f) {
            spawnObstacle();
            float minCooldown = Math.max(0.34f, 0.95f - score * 0.006f);
            spawnCooldown = minCooldown + random.nextFloat() * 0.32f;
        }

        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obstacle = iterator.next();
            obstacle.y += worldSpeed * obstacle.speedScale * dt;
            obstacle.updateBounds();

            if (circleHitsRect(playerX, playerY, playerRadius * 0.78f, obstacle.bounds)) {
                endGame();
                return;
            }

            if (obstacle.y > getHeight() + obstacle.height) {
                iterator.remove();
            }
        }
    }

    private void endGame() {
        state = STATE_GAME_OVER;
        int finalScore = (int) score;
        if (finalScore > bestScore) {
            bestScore = finalScore;
            prefs.edit().putInt("best_score", bestScore).apply();
        }
    }

    private void spawnObstacle() {
        int type;
        float roll = random.nextFloat();
        if (score < 50f) {
            type = roll < 0.58f ? TYPE_MOOSE : roll < 0.78f ? TYPE_FISH : TYPE_ICE;
        } else if (score < 120f) {
            type = roll < 0.38f ? TYPE_MOOSE : roll < 0.60f ? TYPE_BEAR : roll < 0.82f ? TYPE_FISH : TYPE_ICE;
        } else {
            type = roll < 0.30f ? TYPE_MOOSE : roll < 0.52f ? TYPE_BEAR : roll < 0.72f ? TYPE_VAN : roll < 0.88f ? TYPE_FISH : TYPE_ICE;
        }

        float width;
        float height;
        float speedScale = 1f;
        switch (type) {
            case TYPE_BEAR:
                width = dp(92);
                height = dp(58);
                speedScale = 0.92f;
                break;
            case TYPE_FISH:
                width = dp(54);
                height = dp(30);
                speedScale = 1.34f;
                break;
            case TYPE_ICE:
                width = dp(74);
                height = dp(38);
                speedScale = 1.04f;
                break;
            case TYPE_VAN:
                width = dp(104);
                height = dp(60);
                speedScale = 1.10f;
                break;
            case TYPE_MOOSE:
            default:
                width = dp(116);
                height = dp(70);
                speedScale = 1f;
                break;
        }

        float minX = roadLeft + width / 2f + dp(4);
        float maxX = roadRight - width / 2f - dp(4);
        float x = minX + random.nextFloat() * Math.max(1f, maxX - minX);
        obstacles.add(new Obstacle(type, x, -height, width, height, speedScale));
    }

    private void drawWorld(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(22, 37, 57));
        canvas.drawRect(0, 0, width, height, paint);

        drawMountains(canvas, width, height);

        paint.setColor(Color.rgb(42, 48, 56));
        canvas.drawRoundRect(roadLeft, 0, roadRight, height + dp(32), dp(18), dp(18), paint);

        paint.setColor(Color.rgb(92, 97, 103));
        canvas.drawRect(roadLeft, 0, roadLeft + dp(5), height, paint);
        canvas.drawRect(roadRight - dp(5), 0, roadRight, height, paint);

        paint.setColor(Color.rgb(240, 214, 116));
        float dashHeight = dp(42);
        float gap = dp(36);
        float centerX = width / 2f;
        float offset = (score * dp(5)) % (dashHeight + gap);
        for (float y = -dashHeight + offset; y < height + dashHeight; y += dashHeight + gap) {
            canvas.drawRoundRect(centerX - dp(3), y, centerX + dp(3), y + dashHeight, dp(3), dp(3), paint);
        }

        for (Obstacle obstacle : obstacles) {
            drawObstacle(canvas, obstacle);
        }

        drawPlayer(canvas);
    }

    private void drawMountains(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(35, 68, 77));
        canvas.drawCircle(width * 0.12f, height * 0.13f, dp(96), paint);
        canvas.drawCircle(width * 0.88f, height * 0.16f, dp(104), paint);

        paint.setColor(Color.rgb(210, 228, 232));
        canvas.drawCircle(width * 0.12f, height * 0.10f, dp(38), paint);
        canvas.drawCircle(width * 0.88f, height * 0.12f, dp(42), paint);
    }

    private void drawPlayer(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 192, 81));
        canvas.drawCircle(playerX, playerY, playerRadius, paint);

        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawCircle(playerX - playerRadius * 0.35f, playerY - playerRadius * 0.12f, dp(3), paint);
        canvas.drawCircle(playerX + playerRadius * 0.35f, playerY - playerRadius * 0.12f, dp(3), paint);
        canvas.drawRoundRect(
                playerX - playerRadius * 0.35f,
                playerY + playerRadius * 0.25f,
                playerX + playerRadius * 0.35f,
                playerY + playerRadius * 0.38f,
                dp(5),
                dp(5),
                paint
        );

        paint.setColor(Color.rgb(52, 134, 196));
        canvas.drawRoundRect(
                playerX - playerRadius * 0.54f,
                playerY + playerRadius * 0.70f,
                playerX + playerRadius * 0.54f,
                playerY + playerRadius * 1.28f,
                dp(9),
                dp(9),
                paint
        );
    }

    private void drawObstacle(Canvas canvas, Obstacle obstacle) {
        switch (obstacle.type) {
            case TYPE_BEAR:
                drawBear(canvas, obstacle);
                break;
            case TYPE_FISH:
                drawFish(canvas, obstacle);
                break;
            case TYPE_ICE:
                drawIce(canvas, obstacle);
                break;
            case TYPE_VAN:
                drawVan(canvas, obstacle);
                break;
            case TYPE_MOOSE:
            default:
                drawMoose(canvas, obstacle);
                break;
        }
    }

    private void drawMoose(Canvas canvas, Obstacle obstacle) {
        RectF b = obstacle.bounds;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(104, 62, 36));
        canvas.drawRoundRect(b.left + dp(14), b.top + dp(22), b.right - dp(18), b.bottom - dp(8), dp(16), dp(16), paint);
        canvas.drawCircle(b.right - dp(18), b.top + dp(25), dp(18), paint);

        paint.setStrokeWidth(dp(4));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(210, 173, 103));
        float antlerY = b.top + dp(12);
        canvas.drawLine(b.right - dp(28), antlerY, b.right - dp(52), b.top + dp(2), paint);
        canvas.drawLine(b.right - dp(22), antlerY, b.right - dp(2), b.top + dp(2), paint);
        canvas.drawLine(b.right - dp(45), b.top + dp(6), b.right - dp(50), b.top + dp(18), paint);
        canvas.drawLine(b.right - dp(9), b.top + dp(6), b.right - dp(4), b.top + dp(18), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(51, 30, 22));
        canvas.drawRect(b.left + dp(24), b.bottom - dp(10), b.left + dp(31), b.bottom, paint);
        canvas.drawRect(b.right - dp(42), b.bottom - dp(10), b.right - dp(35), b.bottom, paint);
        canvas.drawCircle(b.right - dp(12), b.top + dp(22), dp(3), paint);
    }

    private void drawBear(Canvas canvas, Obstacle obstacle) {
        RectF b = obstacle.bounds;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(80, 50, 39));
        canvas.drawRoundRect(b.left + dp(10), b.top + dp(18), b.right - dp(8), b.bottom - dp(6), dp(18), dp(18), paint);
        canvas.drawCircle(b.left + dp(23), b.top + dp(18), dp(14), paint);
        canvas.drawCircle(b.left + dp(13), b.top + dp(9), dp(6), paint);
        canvas.drawCircle(b.left + dp(30), b.top + dp(8), dp(6), paint);

        paint.setColor(Color.rgb(32, 22, 18));
        canvas.drawCircle(b.left + dp(28), b.top + dp(19), dp(3), paint);
        canvas.drawRect(b.left + dp(22), b.bottom - dp(10), b.left + dp(29), b.bottom, paint);
        canvas.drawRect(b.right - dp(30), b.bottom - dp(10), b.right - dp(23), b.bottom, paint);
    }

    private void drawFish(Canvas canvas, Obstacle obstacle) {
        RectF b = obstacle.bounds;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(236, 126, 72));
        canvas.drawOval(b.left + dp(10), b.top + dp(4), b.right, b.bottom - dp(4), paint);

        Path tail = new Path();
        tail.moveTo(b.left + dp(11), b.centerY());
        tail.lineTo(b.left, b.top);
        tail.lineTo(b.left, b.bottom);
        tail.close();

        paint.setColor(Color.rgb(255, 178, 99));
        canvas.drawPath(tail, paint);

        paint.setColor(Color.rgb(28, 34, 40));
        canvas.drawCircle(b.right - dp(10), b.centerY() - dp(4), dp(3), paint);
    }

    private void drawIce(Canvas canvas, Obstacle obstacle) {
        RectF b = obstacle.bounds;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(144, 214, 232));
        canvas.drawRoundRect(b, dp(12), dp(12), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.rgb(230, 250, 255));
        canvas.drawLine(b.left + dp(12), b.centerY(), b.right - dp(12), b.centerY() - dp(8), paint);
        canvas.drawLine(b.left + dp(28), b.bottom - dp(8), b.right - dp(24), b.top + dp(8), paint);
    }

    private void drawVan(Canvas canvas, Obstacle obstacle) {
        RectF b = obstacle.bounds;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(214, 68, 57));
        canvas.drawRoundRect(b.left, b.top + dp(13), b.right, b.bottom - dp(8), dp(10), dp(10), paint);

        paint.setColor(Color.rgb(184, 231, 247));
        canvas.drawRoundRect(b.left + dp(14), b.top + dp(19), b.right - dp(46), b.top + dp(38), dp(5), dp(5), paint);
        canvas.drawRoundRect(b.right - dp(40), b.top + dp(19), b.right - dp(12), b.top + dp(38), dp(5), dp(5), paint);

        paint.setColor(Color.rgb(28, 28, 28));
        canvas.drawCircle(b.left + dp(22), b.bottom - dp(8), dp(8), paint);
        canvas.drawCircle(b.right - dp(22), b.bottom - dp(8), dp(8), paint);
    }

    private void drawHud(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(dp(18));
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Score " + (int) score, dp(18), dp(32), textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Best " + bestScore, getWidth() - dp(18), dp(32), textPaint);
    }

    private void drawCenterPanel(Canvas canvas, String title, String lineOne, String lineTwo) {
        float panelWidth = Math.min(getWidth() - dp(40), dp(340));
        float panelHeight = dp(178);
        float left = (getWidth() - panelWidth) / 2f;
        float top = (getHeight() - panelHeight) / 2f;
        RectF panel = new RectF(left, top, left + panelWidth, top + panelHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(218, 14, 21, 31));
        canvas.drawRoundRect(panel, dp(22), dp(22), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(32));
        canvas.drawText(title, getWidth() / 2f, top + dp(54), textPaint);

        textPaint.setTextSize(dp(16));
        canvas.drawText(lineOne, getWidth() / 2f, top + dp(98), textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText(lineTwo, getWidth() / 2f, top + dp(132), textPaint);
    }

    private boolean circleHitsRect(float cx, float cy, float radius, RectF rect) {
        float closestX = clamp(cx, rect.left, rect.right);
        float closestY = clamp(cy, rect.top, rect.bottom);
        float dx = cx - closestX;
        float dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class Obstacle {
        final int type;
        final float x;
        float y;
        final float width;
        final float height;
        final float speedScale;
        final RectF bounds = new RectF();

        Obstacle(int type, float x, float y, float width, float height, float speedScale) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speedScale = speedScale;
            updateBounds();
        }

        void updateBounds() {
            bounds.set(x - width / 2f, y, x + width / 2f, y + height);
        }
    }
}
