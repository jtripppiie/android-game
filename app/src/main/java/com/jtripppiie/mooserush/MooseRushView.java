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

    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_GAME_OVER = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Gate> gates = new ArrayList<>();
    private final SharedPreferences prefs;
    private final RectF customizeButtonBounds = new RectF();
    private final RectF topHitBox = new RectF();
    private final RectF bottomHitBox = new RectF();
    private final RectF bodyBounds = new RectF();
    private final Matrix photoMatrix = new Matrix();

    private PhotoRequestListener photoRequestListener;
    private Bitmap playerPhoto;
    private int state = STATE_READY;
    private boolean paused = false;

    private long lastFrameNanos = 0L;
    private float spawnCooldown = 0f;
    private float groundScroll = 0f;
    private float spriteClock = 0f;
    private int score = 0;
    private int bestScore = 0;

    private float playerX;
    private float playerY;
    private float playerVelocityY;
    private float playerRadius;

    public MooseRushView(Context context) {
        super(context);
        setFocusable(true);
        prefs = context.getSharedPreferences("moose_rush", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("best_score", 0);

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
        }

        drawWorld(canvas);
        drawHud(canvas);

        if (state == STATE_READY) {
            drawCenterPanel(canvas, "YOU RUSH", "Put your face on a tiny sprite.", "Tap to bounce");
        } else if (state == STATE_GAME_OVER) {
            drawCenterPanel(canvas, "BONKED", deathLine(), "Tap to retry");
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

        if (state != STATE_RUNNING && isCustomizeButtonHit(event.getX(), event.getY())) {
            if (photoRequestListener != null) {
                photoRequestListener.onPhotoRequested();
            }
            return true;
        }

        if (state == STATE_READY || state == STATE_GAME_OVER) {
            startGame();
        }

        flap();
        return true;
    }

    private void startGame() {
        gates.clear();
        score = 0;
        spawnCooldown = 0.65f;
        groundScroll = 0f;
        spriteClock = 0f;
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

        if (playerY - playerRadius < dp(44) || playerY + playerRadius > getGroundY()) {
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

    private void endGame() {
        state = STATE_GAME_OVER;
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt("best_score", bestScore).apply();
        }
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

    private void drawWorld(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(25, 43, 68));
        canvas.drawRect(0, 0, width, height, paint);

        drawBackground(canvas, width, height);

        for (Gate gate : gates) {
            drawGate(canvas, gate);
        }

        drawPlayer(canvas);
        drawGround(canvas, width);
    }

    private void drawBackground(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(38, 82, 92));
        canvas.drawCircle(width * 0.12f, height * 0.18f, dp(96), paint);
        canvas.drawCircle(width * 0.88f, height * 0.20f, dp(104), paint);

        paint.setColor(Color.rgb(218, 235, 238));
        canvas.drawCircle(width * 0.12f, height * 0.14f, dp(35), paint);
        canvas.drawCircle(width * 0.88f, height * 0.15f, dp(40), paint);

        paint.setColor(Color.rgb(240, 210, 108));
        canvas.drawCircle(width - dp(58), dp(70), dp(24), paint);
    }

    private void drawGround(Canvas canvas, int width) {
        float groundY = getGroundY();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(46, 119, 82));
        canvas.drawRect(0, groundY, width, getHeight(), paint);

        paint.setColor(Color.rgb(35, 86, 63));
        for (float x = -groundScroll; x < width + dp(60); x += dp(48)) {
            canvas.drawRoundRect(x, groundY + dp(12), x + dp(28), groundY + dp(20), dp(4), dp(4), paint);
        }
    }

    private void drawGate(Canvas canvas, Gate gate) {
        float topBottom = gate.gapCenter - gate.gapHeight / 2f;
        float bottomTop = gate.gapCenter + gate.gapHeight / 2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(104, 62, 36));
        canvas.drawRoundRect(gate.x, -dp(20), gate.x + gate.width, topBottom, dp(14), dp(14), paint);
        canvas.drawRoundRect(gate.x, bottomTop, gate.x + gate.width, getGroundY(), dp(14), dp(14), paint);

        paint.setColor(Color.rgb(210, 173, 103));
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

    private void drawPlayer(Canvas canvas) {
        float bob = (float) Math.sin(spriteClock * Math.PI * 2f) * dp(2.5f);
        float cycle = (float) Math.sin(spriteClock * Math.PI * 2f);
        float headY = playerY + bob;

        drawWalkingSpriteBody(canvas, headY, cycle);

        if (playerPhoto != null) {
            drawPlayerPhoto(canvas, headY);
        } else {
            drawDefaultPlayerHead(canvas, headY);
        }
    }

    private void drawWalkingSpriteBody(Canvas canvas, float headY, float cycle) {
        float bodyTop = headY + playerRadius * 0.72f;
        float bodyBottom = bodyTop + dp(36);
        float bodyHalfWidth = dp(15);
        float step = cycle * dp(11);
        float oppositeStep = -step;

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 218, 121));
        bodyBounds.set(playerX - bodyHalfWidth, bodyTop, playerX + bodyHalfWidth, bodyBottom);
        canvas.drawRoundRect(bodyBounds, dp(7), dp(7), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(5));
        paint.setColor(Color.rgb(255, 177, 70));
        canvas.drawLine(playerX - dp(12), bodyTop + dp(10), playerX - dp(27) - step * 0.25f, bodyTop + dp(23) + step * 0.12f, paint);
        canvas.drawLine(playerX + dp(12), bodyTop + dp(10), playerX + dp(27) + step * 0.25f, bodyTop + dp(23) - step * 0.12f, paint);

        paint.setStrokeWidth(dp(6));
        paint.setColor(Color.rgb(52, 134, 196));
        canvas.drawLine(playerX - dp(7), bodyBottom - dp(3), playerX - dp(15) + step, bodyBottom + dp(25), paint);
        canvas.drawLine(playerX + dp(7), bodyBottom - dp(3), playerX + dp(15) + oppositeStep, bodyBottom + dp(25), paint);

        paint.setStrokeWidth(dp(8));
        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawPoint(playerX - dp(15) + step, bodyBottom + dp(25), paint);
        canvas.drawPoint(playerX + dp(15) + oppositeStep, bodyBottom + dp(25), paint);

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawDefaultPlayerHead(Canvas canvas, float headY) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 192, 81));
        canvas.drawRoundRect(playerX - playerRadius, headY - playerRadius, playerX + playerRadius, headY + playerRadius, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawCircle(playerX - playerRadius * 0.35f, headY - playerRadius * 0.12f, dp(3), paint);
        canvas.drawCircle(playerX + playerRadius * 0.35f, headY - playerRadius * 0.12f, dp(3), paint);
        canvas.drawRoundRect(
                playerX - playerRadius * 0.35f,
                headY + playerRadius * 0.25f,
                playerX + playerRadius * 0.35f,
                headY + playerRadius * 0.38f,
                dp(5),
                dp(5),
                paint
        );
    }

    private void drawPlayerPhoto(Canvas canvas, float headY) {
        float diameter = playerRadius * 2f;
        BitmapShader shader = new BitmapShader(playerPhoto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = Math.max(diameter / playerPhoto.getWidth(), diameter / playerPhoto.getHeight());
        float dx = playerX - playerPhoto.getWidth() * scale / 2f;
        float dy = headY - playerPhoto.getHeight() * scale / 2f;
        photoMatrix.reset();
        photoMatrix.setScale(scale, scale);
        photoMatrix.postTranslate(dx, dy);
        shader.setLocalMatrix(photoMatrix);

        paint.setShader(shader);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(playerX - playerRadius, headY - playerRadius, playerX + playerRadius, headY + playerRadius, dp(8), dp(8), paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(playerX - playerRadius, headY - playerRadius, playerX + playerRadius, headY + playerRadius, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHud(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(42));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(score), getWidth() / 2f, dp(66), textPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(16));
        canvas.drawText("Best " + bestScore, getWidth() - dp(18), dp(32), textPaint);

        if (state != STATE_RUNNING) {
            drawCustomizeButton(canvas);
        }
    }

    private void drawCustomizeButton(Canvas canvas) {
        calculateCustomizeButtonBounds();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(225, 255, 218, 121));
        canvas.drawRoundRect(customizeButtonBounds, dp(15), dp(15), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setColor(Color.rgb(24, 30, 38));
        String label = playerPhoto == null ? "ADD YOUR PHOTO" : "CHANGE PHOTO";
        canvas.drawText(label, customizeButtonBounds.centerX(), customizeButtonBounds.centerY() + dp(5), textPaint);
    }

    private boolean isCustomizeButtonHit(float x, float y) {
        calculateCustomizeButtonBounds();
        return customizeButtonBounds.contains(x, y);
    }

    private void calculateCustomizeButtonBounds() {
        float buttonWidth = playerPhoto == null ? dp(142) : dp(134);
        float buttonHeight = dp(36);
        float left = (getWidth() - buttonWidth) / 2f;
        float top = dp(82);
        customizeButtonBounds.set(left, top, left + buttonWidth, top + buttonHeight);
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
        textPaint.setTextSize(dp(34));
        canvas.drawText(title, getWidth() / 2f, top + dp(54), textPaint);

        textPaint.setTextSize(dp(16));
        canvas.drawText(lineOne, getWidth() / 2f, top + dp(98), textPaint);

        textPaint.setTextSize(dp(15));
        textPaint.setColor(Color.rgb(255, 218, 121));
        canvas.drawText(lineTwo, getWidth() / 2f, top + dp(132), textPaint);
    }

    private String deathLine() {
        if (score == 0) {
            return "Score 0. Personally embarrassing.";
        }
        if (score < 5) {
            return "Score " + score + ". One more try.";
        }
        if (score < 10) {
            return "Score " + score + ". That was almost viral.";
        }
        return "Score " + score + ". New chaos legend.";
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
