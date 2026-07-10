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

/*
 * SpriteRenderer owns runner drawing.
 *
 * The game has three runner styles:
 * - photo head plus a running body sheet
 * - full female runner sheet
 * - full male runner sheet
 *
 * A sprite sheet is one image containing several animation frames. We crop one
 * frame at a time, draw it, then switch frames as runnerClock advances.
 */
final class SpriteRenderer {
    static final int BODY_STYLE_PHOTO = 0;
    static final int BODY_STYLE_FEMALE = 1;
    static final int BODY_STYLE_MALE = 2;
    static final int BODY_STYLE_OVERHAUL = 3;

    private static final int RUNNER_FRAMES = 6;
    private static final int FULL_RUNNER_FRAME_GUARD_PX = 14;
    private static final int RUNNER_CELL_CROP_PAD_PX = 2;
    private static final float RUNNER_BODY_HEIGHT_RUNNING = 2.68f;
    private static final float RUNNER_BODY_HEIGHT_STANDING = 2.62f;
    private static final float RUNNER_BODY_WIDTH_SCALE = 1.18f;
    private static final float RUNNER_BODY_TOP_FROM_HEAD = 0.60f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final RectF bodyBounds = new RectF();
    private final RectF tempRect = new RectF();
    private final Rect sourceRect = new Rect();
    private final Matrix photoMatrix = new Matrix();
    private final Bitmap runnerBodySheet;
    private final Bitmap femaleRunnerSheet;
    private final Bitmap maleRunnerSheet;
    private final Bitmap overhaulRunnerSheet;
    private final Rect[] runnerBodyCrops;
    private final Rect[] femaleRunnerCrops;
    private final Rect[] maleRunnerCrops;
    private final Rect[] overhaulRunnerCrops;
    private final float density;

    SpriteRenderer(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        runnerBodySheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_player_run_headless);
        femaleRunnerSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_mom_run);
        maleRunnerSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_dad_run);
        overhaulRunnerSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_runner_overhaul_candidate);
        /*
         * Crops remove empty transparent pixels around each frame. Without crop
         * rectangles, sprites can look offset, tiny, or like they have artifacts.
         *
         * Full-body runner sheets use cell-content crops (every visible pixel in
         * the frame cell) instead of a single connected component. A running pose
         * lifts a foot or throws out a hand that is separated from the torso by a
         * transparent gap; the connected-component crop would treat those detached
         * limbs as noise and shave them off, which is what made the runner look
         * clipped. Cell-content crops keep the whole silhouette and still never
         * bleed across a cell seam.
         */
        runnerBodyCrops = SpriteFrameCropper.computeFrameCrops(runnerBodySheet, RUNNER_FRAMES, FULL_RUNNER_FRAME_GUARD_PX);
        femaleRunnerCrops = SpriteFrameCropper.computeCellContentCrops(femaleRunnerSheet, RUNNER_FRAMES, RUNNER_CELL_CROP_PAD_PX);
        maleRunnerCrops = SpriteFrameCropper.computeCellContentCrops(maleRunnerSheet, RUNNER_FRAMES, RUNNER_CELL_CROP_PAD_PX);
        overhaulRunnerCrops = SpriteFrameCropper.computeCellContentCrops(overhaulRunnerSheet, RUNNER_FRAMES, RUNNER_CELL_CROP_PAD_PX);
        bitmapPaint.setFilterBitmap(false);
    }

    void drawRunner(Canvas canvas, PlayerFrame frame) {
        // bob is a sine wave that makes running feel springy.
        float bob = (float) Math.sin(frame.spriteClock * Math.PI * 2f) * dp(4.0f);
        float headY = frame.y + bob;
        boolean fullBody = drawFullRunnerSheet(canvas, frame, headY, true);
        if (!fullBody && !drawRunnerSheetBody(canvas, frame, headY, true)) {
            drawRunningBody(canvas, frame, headY);
        }
        if (!fullBody || frame.playerPhoto != null) {
            drawHead(canvas, frame.x, headY, frame.radius, frame.playerPhoto);
        }
    }

    void drawStanding(Canvas canvas, PlayerFrame frame) {
        boolean fullBody = drawFullRunnerSheet(canvas, frame, frame.y, false);
        if (!fullBody && !drawRunnerSheetBody(canvas, frame, frame.y, false)) {
            drawStandingBody(canvas, frame);
        }
        if (!fullBody || frame.playerPhoto != null) {
            drawHead(canvas, frame.x, frame.y, frame.radius, frame.playerPhoto);
        }
    }

    private void drawHead(Canvas canvas, float x, float headY, float radius, Bitmap playerPhoto) {
        if (playerPhoto != null) {
            drawPlayerPhoto(canvas, x, headY, radius, playerPhoto);
        } else {
            drawDefaultPlayerHead(canvas, x, headY, radius);
        }
    }

    private boolean drawRunnerSheetBody(Canvas canvas, PlayerFrame frame, float headY, boolean animated) {
        // Return false if the sprite sheet is unavailable so fallback art draws.
        if (runnerBodySheet == null || runnerBodySheet.getWidth() <= 0 || runnerBodySheet.getHeight() <= 0) {
            return false;
        }

        int frameIndex = animated ? runnerSheetFrame(frame.spriteClock) : 0;
        Rect crop = runnerBodyCrop(frameIndex);
        if (crop == null || crop.width() <= 0 || crop.height() <= 0) {
            return false;
        }
        sourceRect.set(crop.left, crop.top, crop.right, crop.bottom);

        float sourceWidth = sourceRect.width();
        float sourceHeight = sourceRect.height();
        float bodyHeight = runnerSheetBodyHeight(frame.radius, animated);
        float bodyWidth = bodyHeight * (sourceWidth / sourceHeight) * RUNNER_BODY_WIDTH_SCALE;
        float top = headY + frame.radius * RUNNER_BODY_TOP_FROM_HEAD;
        float centerX = frame.x + frame.radius * 0.10f;

        tempRect.set(centerX - bodyWidth * 0.50f, top, centerX + bodyWidth * 0.50f, top + bodyHeight);
        boolean previousFilter = bitmapPaint.isFilterBitmap();
        bitmapPaint.setFilterBitmap(false);
        canvas.drawBitmap(runnerBodySheet, sourceRect, tempRect, bitmapPaint);
        bitmapPaint.setFilterBitmap(previousFilter);
        return true;
    }

    private Rect runnerBodyCrop(int frameIndex) {
        if (runnerBodyCrops == null || runnerBodyCrops.length == 0) {
            return null;
        }
        return runnerBodyCrops[Math.floorMod(frameIndex, runnerBodyCrops.length)];
    }

    private boolean drawFullRunnerSheet(Canvas canvas, PlayerFrame frame, float headY, boolean animated) {
        Bitmap sheet = fullRunnerSheet(frame.bodyStyle);
        if (sheet == null || sheet.getWidth() <= 0 || sheet.getHeight() <= 0) {
            return false;
        }

        int frameIndex = animated ? runnerSheetFrame(frame.spriteClock) : 0;
        Rect[] crops = fullRunnerCrops(frame.bodyStyle);
        int[] source = fullRunnerSourceValues(sheet.getWidth(), sheet.getHeight(), frameIndex, crops);
        sourceRect.set(source[0], source[1], source[2], source[3]);
        if (sourceRect.width() <= 0 || sourceRect.height() <= 0) {
            return false;
        }

        float bodyHeight = frame.radius * (animated ? 4.08f : 4.02f);
        float bodyWidth = bodyHeight * (sourceRect.width() / (float) sourceRect.height());
        float centerX = frame.x + frame.radius * 0.04f;
        float top = headY - frame.radius * 1.12f;
        tempRect.set(centerX - bodyWidth * 0.50f, top, centerX + bodyWidth * 0.50f, top + bodyHeight);
        boolean previousFilter = bitmapPaint.isFilterBitmap();
        bitmapPaint.setFilterBitmap(false);
        canvas.drawBitmap(sheet, sourceRect, tempRect, bitmapPaint);
        bitmapPaint.setFilterBitmap(previousFilter);
        return true;
    }

    private Bitmap fullRunnerSheet(int bodyStyle) {
        if (bodyStyle == BODY_STYLE_FEMALE) {
            return femaleRunnerSheet;
        }
        if (bodyStyle == BODY_STYLE_MALE) {
            return maleRunnerSheet;
        }
        if (bodyStyle == BODY_STYLE_OVERHAUL) {
            return overhaulRunnerSheet;
        }
        return null;
    }

    private Rect[] fullRunnerCrops(int bodyStyle) {
        if (bodyStyle == BODY_STYLE_FEMALE) {
            return femaleRunnerCrops;
        }
        if (bodyStyle == BODY_STYLE_MALE) {
            return maleRunnerCrops;
        }
        if (bodyStyle == BODY_STYLE_OVERHAUL) {
            return overhaulRunnerCrops;
        }
        return null;
    }

    static int runnerSheetFrame(float runnerClock) {
        // Convert time into frame number: 0, 1, 2, 3, 4, 5, then repeat.
        return Math.floorMod((int) (runnerClock * RUNNER_FRAMES), RUNNER_FRAMES);
    }

    static int fullRunnerFrameLeft(int sheetWidth, int frameIndex) {
        return Math.round(sheetWidth * (frameIndex / (float) RUNNER_FRAMES));
    }

    static int fullRunnerFrameRight(int sheetWidth, int frameIndex) {
        return Math.round(sheetWidth * ((frameIndex + 1) / (float) RUNNER_FRAMES));
    }

    static int[] fullRunnerSourceValues(int sheetWidth, int sheetHeight, int frameIndex) {
        return fullRunnerSourceValues(sheetWidth, sheetHeight, frameIndex, null);
    }

    static int[] fullRunnerSourceValues(int sheetWidth, int sheetHeight, int frameIndex, Rect[] crops) {
        int safeFrame = Math.floorMod(frameIndex, RUNNER_FRAMES);
        if (crops != null && safeFrame < crops.length && crops[safeFrame] != null && !crops[safeFrame].isEmpty()) {
            Rect crop = crops[safeFrame];
            return new int[]{crop.left, crop.top, crop.right, crop.bottom};
        }
        int left = fullRunnerFrameLeft(sheetWidth, safeFrame);
        int right = fullRunnerFrameRight(sheetWidth, safeFrame);
        if (safeFrame > 0) {
            left += FULL_RUNNER_FRAME_GUARD_PX;
        }
        if (safeFrame < RUNNER_FRAMES - 1) {
            right -= FULL_RUNNER_FRAME_GUARD_PX;
        }
        if (right <= left) {
            left = fullRunnerFrameLeft(sheetWidth, safeFrame);
            right = fullRunnerFrameRight(sheetWidth, safeFrame);
        }
        return new int[]{left, 0, right, sheetHeight};
    }

    static float runnerSheetBodyHeight(float radius, boolean animated) {
        return radius * (animated ? RUNNER_BODY_HEIGHT_RUNNING : RUNNER_BODY_HEIGHT_STANDING);
    }

    /**
     * Vertical distance from the head anchor (the {@code y} passed to
     * {@link #drawRunner}/{@link #drawStanding}) down to the sole of the runner's
     * foot. Callers use this to keep the feet grounded when the drawn radius is
     * scaled independently of the collision radius.
     */
    static float runnerFeetDropFromHead(float radius, boolean animated) {
        return radius * RUNNER_BODY_TOP_FROM_HEAD + runnerSheetBodyHeight(radius, animated);
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
        float headRadius = radius * 0.74f;
        float outlineRadius = radius * 0.82f;
        float eyeRadius = Math.max(dp(1.55f), radius * 0.078f);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(31, 34, 38));
        canvas.drawRoundRect(x - outlineRadius, headY - outlineRadius, x + outlineRadius, headY + outlineRadius, dp(9), dp(9), paint);

        paint.setColor(Color.rgb(255, 192, 81));
        canvas.drawRoundRect(x - headRadius, headY - headRadius, x + headRadius, headY + headRadius, dp(8), dp(8), paint);

        paint.setColor(Color.argb(230, 87, 54, 38));
        canvas.drawRoundRect(x - headRadius * 0.95f, headY - headRadius * 0.97f, x + headRadius * 0.95f, headY - headRadius * 0.43f, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(43, 32, 31));
        canvas.drawCircle(x - headRadius * 0.34f, headY - headRadius * 0.09f, eyeRadius, paint);
        canvas.drawCircle(x + headRadius * 0.34f, headY - headRadius * 0.09f, eyeRadius, paint);
        canvas.drawRoundRect(x - headRadius * 0.32f, headY + headRadius * 0.28f, x + headRadius * 0.32f, headY + headRadius * 0.39f, dp(4), dp(4), paint);
    }

    private void drawPlayerPhoto(Canvas canvas, float x, float headY, float radius, Bitmap playerPhoto) {
        float faceRadius = radius * 0.84f;
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
        canvas.drawCircle(x, headY, radius * 1.00f, paint);

        paint.setColor(Color.rgb(226, 64, 72));
        tempRect.set(x - radius * 0.93f, headY - radius * 0.96f, x + radius * 0.93f, headY - radius * 0.39f);
        canvas.drawRoundRect(tempRect, dp(9), dp(9), paint);

        paint.setColor(Color.rgb(255, 218, 121));
        canvas.drawCircle(x + radius * 0.65f, headY - radius * 0.45f, radius * 0.16f, paint);

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
        canvas.drawCircle(x - radius * 0.25f, headY - radius * 0.29f, radius * 0.20f, paint);
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
        final int bodyStyle;

        PlayerFrame(float x, float y, float radius, float spriteClock, boolean grounded, float velocityY, Bitmap playerPhoto, int outfitColor, int bodyStyle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.spriteClock = spriteClock;
            this.grounded = grounded;
            this.velocityY = velocityY;
            this.playerPhoto = playerPhoto;
            this.outfitColor = outfitColor;
            this.bodyStyle = bodyStyle;
        }
    }
}
