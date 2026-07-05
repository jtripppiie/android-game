package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

final class SpriteRenderer {
    private static final int RUNNER_FRAMES = 6;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bodyBounds = new RectF();
    private final RectF tempRect = new RectF();
    private final Rect sourceRect = new Rect();
    private final Matrix photoMatrix = new Matrix();
    private final Bitmap runnerBodySheet;
    private final float density;

    SpriteRenderer(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        runnerBodySheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_player_run_headless);
    }

    void drawRunner(Canvas canvas, PlayerFrame frame) {
        float bob = (float) Math.sin(frame.spriteClock * Math.PI * 2f) * dp(4.0f);
        float headY = frame.y + bob;
        if (!drawRunnerSheetBody(canvas, frame, headY, true)) {
            drawRunningBody(canvas, frame, headY);
        }
        drawHead(canvas, frame.x, headY, frame.radius, frame.playerPhoto);
    }

    void drawStanding(Canvas canvas, PlayerFrame frame) {
        if (!drawRunnerSheetBody(canvas, frame, frame.y, false)) {
            drawStandingBody(canvas, frame);
        }
        drawHead(canvas, frame.x, frame.y, frame.radius, frame.playerPhoto);
    }

    private void drawHead(Canvas canvas, float x, float headY, float radius, Bitmap playerPhoto) {
        if (playerPhoto != null) {
            drawPlayerPhoto(canvas, x, headY, radius, playerPhoto);
        } else {
            drawDefaultPlayerHead(canvas, x, headY, radius);
        }
    }

    private boolean drawRunnerSheetBody(Canvas canvas, PlayerFrame frame, float headY, boolean animated) {
        if (runnerBodySheet == null || runnerBodySheet.getWidth() <= 0 || runnerBodySheet.getHeight() <= 0) {
            return false;
        }

        int frameWidth = runnerBodySheet.getWidth() / RUNNER_FRAMES;
        if (frameWidth <= 0) {
            return false;
        }

        int frameIndex = animated
                ? Math.floorMod((int) (frame.spriteClock * 12.0f), RUNNER_FRAMES)
                : 0;
        sourceRect.set(frameIndex * frameWidth, 0, (frameIndex + 1) * frameWidth, runnerBodySheet.getHeight());

        float bodyHeight = frame.radius * (animated ? 3.52f : 3.42f);
        float bodyWidth = bodyHeight * (frameWidth / (float) runnerBodySheet.getHeight());
        float top = headY + frame.radius * 0.42f;
        float centerX = frame.x + frame.radius * 0.10f;

        tempRect.set(centerX - bodyWidth * 0.50f, top, centerX + bodyWidth * 0.50f, top + bodyHeight);
        canvas.drawBitmap(runnerBodySheet, sourceRect, tempRect, null);
        return true;
    }

    private void drawStandingBody(Canvas canvas, PlayerFrame frame) {
        float x = frame.x;
        float headY = frame.y;
        float radius = frame.radius;
        float shoulderY = headY + radius * 1.02f;
        float hipY = headY + radius * 2.14f;
        float footY = headY + radius * 3.22f;
        int outfitColor = frame.outfitColor;
        int pantsColor = frame.playerPhoto == null ? Color.rgb(52, 134, 196) : darkerColor(frame.outfitColor);
        int outlineColor = Color.rgb(31, 34, 38);
        int bootColor = Color.rgb(42, 31, 30);
        int gloveColor = Color.rgb(255, 177, 70);

        paint.setShader(null);

        drawBentLimb(canvas, x - radius * 0.28f, hipY, x - radius * 0.36f, hipY + radius * 0.56f, x - radius * 0.52f, footY, radius * 0.42f, outlineColor);
        drawBentLimb(canvas, x + radius * 0.28f, hipY, x + radius * 0.36f, hipY + radius * 0.56f, x + radius * 0.52f, footY, radius * 0.42f, outlineColor);
        drawBentLimb(canvas, x - radius * 0.28f, hipY, x - radius * 0.36f, hipY + radius * 0.56f, x - radius * 0.52f, footY, radius * 0.27f, pantsColor);
        drawBentLimb(canvas, x + radius * 0.28f, hipY, x + radius * 0.36f, hipY + radius * 0.56f, x + radius * 0.52f, footY, radius * 0.27f, pantsColor);
        drawRunnerFoot(canvas, x - radius * 0.52f, footY, -1f, radius, outlineColor, bootColor);
        drawRunnerFoot(canvas, x + radius * 0.52f, footY, 1f, radius, outlineColor, bootColor);

        drawBentLimb(canvas, x - radius * 0.58f, shoulderY + radius * 0.16f, x - radius * 0.86f, shoulderY + radius * 0.74f, x - radius * 0.58f, hipY - radius * 0.08f, radius * 0.35f, outlineColor);
        drawBentLimb(canvas, x + radius * 0.58f, shoulderY + radius * 0.16f, x + radius * 0.86f, shoulderY + radius * 0.74f, x + radius * 0.58f, hipY - radius * 0.08f, radius * 0.35f, outlineColor);
        drawBentLimb(canvas, x - radius * 0.58f, shoulderY + radius * 0.16f, x - radius * 0.86f, shoulderY + radius * 0.74f, x - radius * 0.58f, hipY - radius * 0.08f, radius * 0.21f, gloveColor);
        drawBentLimb(canvas, x + radius * 0.58f, shoulderY + radius * 0.16f, x + radius * 0.86f, shoulderY + radius * 0.74f, x + radius * 0.58f, hipY - radius * 0.08f, radius * 0.21f, gloveColor);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outlineColor);
        bodyBounds.set(x - radius * 0.70f, shoulderY - radius * 0.18f, x + radius * 0.70f, hipY + radius * 0.16f);
        canvas.drawRoundRect(bodyBounds, dp(9), dp(9), paint);

        paint.setColor(outfitColor);
        bodyBounds.set(x - radius * 0.58f, shoulderY - radius * 0.08f, x + radius * 0.58f, hipY + radius * 0.08f);
        canvas.drawRoundRect(bodyBounds, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(226, 64, 72));
        tempRect.set(x - radius * 0.48f, shoulderY - radius * 0.16f, x + radius * 0.48f, shoulderY + radius * 0.15f);
        canvas.drawRoundRect(tempRect, dp(7), dp(7), paint);

        paint.setColor(Color.argb(185, 255, 246, 207));
        canvas.drawRoundRect(x - radius * 0.36f, shoulderY + radius * 0.32f, x + radius * 0.36f, shoulderY + radius * 0.48f, dp(4), dp(4), paint);
    }

    private void drawRunningBody(Canvas canvas, PlayerFrame frame, float headY) {
        float x = frame.x;
        float radius = frame.radius;
        float bodyTop = headY + radius * 0.80f;
        float shoulderY = bodyTop + radius * 0.32f;
        float hipY = bodyTop + radius * 1.32f;
        float shoulderX = x - radius * 0.08f;
        float hipX = x + radius * 0.12f;
        float footY = headY + radius * 3.18f;
        float phase = frame.spriteClock * (float) Math.PI * 2f;
        float stride = frame.grounded ? (float) Math.sin(phase) : 0.28f;
        float riseA = frame.grounded ? Math.max(0f, -(float) Math.cos(phase)) : 0.60f;
        float riseB = frame.grounded ? Math.max(0f, (float) Math.cos(phase)) : 0.38f;
        float outline = radius * 0.15f;
        int outfitColor = frame.outfitColor;
        int pantsColor = frame.playerPhoto == null ? Color.rgb(52, 134, 196) : darkerColor(frame.outfitColor);
        int farPantsColor = Color.rgb(
                Math.max(0, Math.round(Color.red(pantsColor) * 0.72f)),
                Math.max(0, Math.round(Color.green(pantsColor) * 0.72f)),
                Math.max(0, Math.round(Color.blue(pantsColor) * 0.72f))
        );
        int trimColor = Color.rgb(255, 246, 207);
        int outlineColor = Color.rgb(31, 34, 38);
        int bootColor = Color.rgb(42, 31, 30);
        int gloveColor = Color.rgb(255, 177, 70);
        int farGloveColor = Color.rgb(198, 116, 48);

        float nearHipX = hipX + radius * 0.10f;
        float farHipX = hipX - radius * 0.10f;
        float farKneeX;
        float farKneeY;
        float farFootX;
        float farFootY;
        float nearKneeX;
        float nearKneeY;
        float nearFootX;
        float nearFootY;

        if (frame.grounded) {
            float bend = (float) Math.cos(phase) * radius * 0.10f;
            nearFootX = nearHipX + stride * radius * 0.72f;
            nearFootY = footY - riseA * radius * 0.22f;
            nearKneeX = nearHipX + stride * radius * 0.34f + bend;
            nearKneeY = hipY + radius * 0.60f - riseA * radius * 0.10f;

            farFootX = farHipX - stride * radius * 0.64f;
            farFootY = footY - riseB * radius * 0.20f;
            farKneeX = farHipX - stride * radius * 0.30f - bend;
            farKneeY = hipY + radius * 0.62f - riseB * radius * 0.08f;
        } else {
            nearKneeX = nearHipX + radius * 0.38f;
            nearKneeY = hipY + radius * 0.46f;
            nearFootX = nearHipX + radius * 0.16f;
            nearFootY = hipY + radius * 1.05f;

            farKneeX = farHipX - radius * 0.40f;
            farKneeY = hipY + radius * 0.56f;
            farFootX = farHipX - radius * 0.05f;
            farFootY = hipY + radius * 1.12f;
        }

        float nearArmSwing = -stride * 0.54f;
        float farArmSwing = -nearArmSwing;
        float nearShoulderX = shoulderX + radius * 0.42f;
        float farShoulderX = shoulderX - radius * 0.36f;
        float nearElbowX = nearShoulderX + nearArmSwing * radius * 0.52f;
        float nearElbowY = shoulderY + radius * 0.46f;
        float nearHandX = nearElbowX + nearArmSwing * radius * 0.42f;
        float nearHandY = nearElbowY + radius * 0.42f;
        float farElbowX = farShoulderX + farArmSwing * radius * 0.46f;
        float farElbowY = shoulderY + radius * 0.52f;
        float farHandX = farElbowX + farArmSwing * radius * 0.34f;
        float farHandY = farElbowY + radius * 0.40f;

        paint.setShader(null);
        drawBentLimb(canvas, farHipX, hipY, farKneeX, farKneeY, farFootX, farFootY, radius * 0.24f + outline, outlineColor);
        drawBentLimb(canvas, farHipX, hipY, farKneeX, farKneeY, farFootX, farFootY, radius * 0.24f, farPantsColor);
        drawRunnerFoot(canvas, farFootX, farFootY, -stride, radius, outlineColor, bootColor);

        drawBentLimb(canvas, farShoulderX, shoulderY, farElbowX, farElbowY, farHandX, farHandY, radius * 0.19f + outline, outlineColor);
        drawBentLimb(canvas, farShoulderX, shoulderY, farElbowX, farElbowY, farHandX, farHandY, radius * 0.19f, farGloveColor);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(radius * 0.38f + outline);
        paint.setColor(outlineColor);
        canvas.drawLine(shoulderX, shoulderY, hipX, hipY, paint);

        paint.setStrokeWidth(radius * 0.38f);
        paint.setColor(outfitColor);
        canvas.drawLine(shoulderX, shoulderY, hipX, hipY, paint);

        paint.setStrokeWidth(radius * 0.2f);
        paint.setColor(trimColor);
        canvas.drawLine(shoulderX - radius * 0.10f, shoulderY + radius * 0.30f, shoulderX + radius * 0.54f, shoulderY + radius * 0.43f, paint);

        drawBentLimb(canvas, nearHipX, hipY, nearKneeX, nearKneeY, nearFootX, nearFootY, radius * 0.27f + outline, outlineColor);
        drawBentLimb(canvas, nearHipX, hipY, nearKneeX, nearKneeY, nearFootX, nearFootY, radius * 0.27f, pantsColor);
        drawRunnerFoot(canvas, nearFootX, nearFootY, stride, radius, outlineColor, bootColor);

        drawBentLimb(canvas, nearShoulderX, shoulderY, nearElbowX, nearElbowY, nearHandX, nearHandY, radius * 0.21f + outline, outlineColor);
        drawBentLimb(canvas, nearShoulderX, shoulderY, nearElbowX, nearElbowY, nearHandX, nearHandY, radius * 0.21f, gloveColor);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outlineColor);
        bodyBounds.set(x - radius * 0.58f, bodyTop - radius * 0.34f, x + radius * 0.56f, bodyTop + radius * 0.12f);
        canvas.drawRoundRect(bodyBounds, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(226, 64, 72));
        tempRect.set(x - radius * 0.5f, bodyTop - radius * 0.27f, x + radius * 0.5f, bodyTop + radius * 0.03f);
        canvas.drawRoundRect(tempRect, dp(7), dp(7), paint);

        paint.setColor(Color.argb(190, 255, 246, 207));
        canvas.drawCircle(x + radius * 0.64f, bodyTop - radius * 0.08f, radius * 0.13f, paint);

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBentLimb(Canvas canvas, float ax, float ay, float bx, float by, float cx, float cy, float strokeWidth, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        canvas.drawLine(ax, ay, bx, by, paint);
        canvas.drawLine(bx, by, cx, cy, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawRunnerFoot(Canvas canvas, float x, float y, float stride, float radius, int outlineColor, int bootColor) {
        float direction = stride >= 0f ? 1f : -1f;
        float toeX = x + direction * radius * 0.38f;
        float heelX = x - direction * radius * 0.18f;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(radius * 0.24f);
        paint.setColor(outlineColor);
        canvas.drawLine(heelX, y, toeX, y + radius * 0.03f, paint);
        paint.setStrokeWidth(radius * 0.16f);
        paint.setColor(bootColor);
        canvas.drawLine(heelX, y, toeX, y + radius * 0.03f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawDefaultPlayerHead(Canvas canvas, float x, float headY, float radius) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(31, 34, 38));
        canvas.drawRoundRect(x - radius * 1.06f, headY - radius * 1.06f, x + radius * 1.06f, headY + radius * 1.06f, dp(10), dp(10), paint);

        paint.setColor(Color.rgb(255, 192, 81));
        canvas.drawRoundRect(x - radius, headY - radius, x + radius, headY + radius, dp(8), dp(8), paint);

        paint.setColor(Color.argb(230, 87, 54, 38));
        canvas.drawRoundRect(x - radius * 0.95f, headY - radius * 0.98f, x + radius * 0.95f, headY - radius * 0.42f, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawCircle(x - radius * 0.35f, headY - radius * 0.12f, dp(3), paint);
        canvas.drawCircle(x + radius * 0.35f, headY - radius * 0.12f, dp(3), paint);
        canvas.drawRoundRect(x - radius * 0.35f, headY + radius * 0.25f, x + radius * 0.35f, headY + radius * 0.38f, dp(5), dp(5), paint);
    }

    private void drawPlayerPhoto(Canvas canvas, float x, float headY, float radius, Bitmap playerPhoto) {
        float faceRadius = radius * 0.92f;
        float diameter = faceRadius * 2f;
        BitmapShader shader = new BitmapShader(playerPhoto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = Math.max(diameter / playerPhoto.getWidth(), diameter / playerPhoto.getHeight());
        float dx = x - playerPhoto.getWidth() * scale / 2f;
        float dy = headY - playerPhoto.getHeight() * scale / 2f;
        photoMatrix.reset();
        photoMatrix.setScale(scale, scale);
        photoMatrix.postTranslate(dx, dy);
        shader.setLocalMatrix(photoMatrix);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setColor(Color.rgb(31, 34, 38));
        canvas.drawCircle(x, headY, radius * 1.11f, paint);

        paint.setColor(Color.rgb(226, 64, 72));
        tempRect.set(x - radius * 1.03f, headY - radius * 1.06f, x + radius * 1.03f, headY - radius * 0.43f);
        canvas.drawRoundRect(tempRect, dp(9), dp(9), paint);

        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawCircle(x + radius * 0.72f, headY - radius * 0.50f, radius * 0.18f, paint);

        paint.setShader(shader);
        tempRect.set(x - faceRadius, headY - faceRadius, x + faceRadius, headY + faceRadius);
        canvas.drawOval(tempRect, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.4f));
        paint.setColor(Color.rgb(255, 246, 207));
        canvas.drawOval(tempRect, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(78, 255, 255, 255));
        canvas.drawCircle(x - radius * 0.28f, headY - radius * 0.32f, radius * 0.22f, paint);
    }

    private int darkerColor(int color) {
        return Color.rgb(
                Math.max(0, Math.round(Color.red(color) * 0.52f)),
                Math.max(0, Math.round(Color.green(color) * 0.52f)),
                Math.max(0, Math.round(Color.blue(color) * 0.52f))
        );
    }

    private float dp(float value) {
        return value * density;
    }

    static final class PlayerFrame {
        final float x;
        final float y;
        final float radius;
        final float spriteClock;
        final boolean grounded;
        final float velocityY;
        final Bitmap playerPhoto;
        final int outfitColor;

        PlayerFrame(float x, float y, float radius, float spriteClock, boolean grounded, float velocityY, Bitmap playerPhoto, int outfitColor) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.spriteClock = spriteClock;
            this.grounded = grounded;
            this.velocityY = velocityY;
            this.playerPhoto = playerPhoto;
            this.outfitColor = outfitColor;
        }
    }
}
