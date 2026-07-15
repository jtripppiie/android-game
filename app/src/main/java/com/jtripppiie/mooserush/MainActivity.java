package com.jtripppiie.mooserush;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private static final String TAG = "YouRushDebug";
    private static final int REQUEST_PLAYER_PHOTO = 1001;
    private static final int MAX_FACE_COUNT = 3;
    private static final float MIN_FACE_CONFIDENCE = 0.38f;
    private static final float MIN_EYE_DISTANCE_RATIO = 0.035f;
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_PLAYER_PHOTO_URI = "player_photo_uri";

    private MooseRushView gameView;
    private TextView versionBadge;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starting You Rush " + BuildConfig.BUILD_BADGE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gameView = new MooseRushView(this);
        gameView.setPhotoRequestListener(new MooseRushView.PhotoRequestListener() {
            @Override
            public void onPhotoRequested() {
                openPhotoPicker();
            }

            @Override
            public void onPhotoResetRequested() {
                resetPlayerPhoto();
            }
        });
        setContentView(createGameRoot());
        loadSavedPlayerPhoto();
        enableImmersiveMode();
        Log.d(TAG, "onCreate: award game view attached, version badge added, immersive mode enabled");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resuming game view " + BuildConfig.VERSION_NAME + " code " + BuildConfig.VERSION_CODE);
        enableImmersiveMode();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: pausing game view");
        if (gameView != null) {
            gameView.pause();
        }
        super.onPause();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode != REQUEST_PLAYER_PHOTO || resultCode != RESULT_OK || data == null || data.getData() == null) {
            Log.d(TAG, "onActivityResult: no usable photo selected");
            return;
        }

        Uri selectedImageUri = data.getData();
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(selectedImageUri, flags);
            Log.d(TAG, "onActivityResult: persistent photo permission granted");
        } catch (SecurityException exception) {
            Log.d(TAG, "onActivityResult: persistent permission unavailable; session photo can still work", exception);
        }

        Bitmap selectedPhoto = decodeBitmap(selectedImageUri);
        if (selectedPhoto != null) {
            Bitmap facePhoto = extractUsableFaceBitmap(selectedPhoto);
            if (facePhoto != null) {
                prefs.edit().putString(PREF_PLAYER_PHOTO_URI, selectedImageUri.toString()).apply();
                gameView.setPlayerPhoto(facePhoto);
                Log.d(TAG, "onActivityResult: player face photo loaded " + facePhoto.getWidth() + "x" + facePhoto.getHeight());
            } else {
                rejectPlayerPhoto("Choose a clear face photo.");
                Log.w(TAG, "onActivityResult: rejected selected image because no usable face was detected");
            }
        } else {
            rejectPlayerPhoto("That photo could not be used.");
            Log.w(TAG, "onActivityResult: failed to decode selected photo");
        }
        enableImmersiveMode();
    }

    private FrameLayout createGameRoot() {
        FrameLayout root = new FrameLayout(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        if (BuildConfig.SHOW_VERSION_BADGE) {
            versionBadge = new TextView(this);
            versionBadge.setText(BuildConfig.BUILD_BADGE);
            versionBadge.setTextColor(Color.rgb(7, 22, 41));
            versionBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            versionBadge.setTypeface(Typeface.DEFAULT_BOLD);
            versionBadge.setGravity(Gravity.CENTER);
            versionBadge.setPadding(dp(9), dp(4), dp(9), dp(4));

            GradientDrawable badgeBackground = new GradientDrawable();
            badgeBackground.setColor(Color.rgb(255, 218, 121));
            badgeBackground.setCornerRadius(dp(14));
            badgeBackground.setStroke(dp(1), Color.WHITE);
            versionBadge.setBackground(badgeBackground);
            versionBadge.setAlpha(0.92f);
            versionBadge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.START
            );
            // Keep the build badge above the on-screen directional controls.
            badgeParams.setMargins(dp(12), 0, 0, dp(156));
            root.addView(versionBadge, badgeParams);
        }

        return root;
    }

    private void openPhotoPicker() {
        Log.d(TAG, "openPhotoPicker: launching Android document picker");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launchPhotoPicker(intent);
    }

    private void resetPlayerPhoto() {
        prefs.edit().remove(PREF_PLAYER_PHOTO_URI).apply();
        if (gameView != null) {
            gameView.clearPlayerPhoto();
        }
        Log.d(TAG, "resetPlayerPhoto: cleared saved player photo");
    }

    @SuppressWarnings("deprecation")
    private void launchPhotoPicker(Intent intent) {
        startActivityForResult(intent, REQUEST_PLAYER_PHOTO);
    }

    private void loadSavedPlayerPhoto() {
        String savedUri = prefs.getString(PREF_PLAYER_PHOTO_URI, null);
        if (savedUri == null) {
            Log.d(TAG, "loadSavedPlayerPhoto: no saved photo URI");
            return;
        }

        Log.d(TAG, "loadSavedPlayerPhoto: attempting saved photo restore");
        Bitmap savedPhoto = decodeBitmap(Uri.parse(savedUri));
        Bitmap facePhoto = savedPhoto == null ? null : extractUsableFaceBitmap(savedPhoto);
        if (facePhoto != null) {
            gameView.setPlayerPhoto(facePhoto);
            Log.d(TAG, "loadSavedPlayerPhoto: restored saved face photo " + facePhoto.getWidth() + "x" + facePhoto.getHeight());
        } else {
            prefs.edit().remove(PREF_PLAYER_PHOTO_URI).apply();
            gameView.clearPlayerPhoto();
            Log.w(TAG, "loadSavedPlayerPhoto: saved photo failed, clearing saved URI");
        }
    }

    private void rejectPlayerPhoto(String message) {
        prefs.edit().remove(PREF_PLAYER_PHOTO_URI).apply();
        if (gameView != null) {
            gameView.clearPlayerPhoto();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Bitmap extractUsableFaceBitmap(Bitmap bitmap) {
        Bitmap faceBitmap = createFaceDetectorBitmap(bitmap);
        if (faceBitmap == null) {
            return null;
        }

        FaceDetector detector = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), MAX_FACE_COUNT);
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE_COUNT];
        int faceCount = detector.findFaces(faceBitmap, faces);
        float minEyeDistance = faceBitmap.getWidth() * MIN_EYE_DISTANCE_RATIO;
        PointF midpoint = new PointF();
        FaceDetector.Face bestFace = null;
        PointF bestMidpoint = new PointF();
        float bestScore = 0f;

        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];
            if (face == null) {
                continue;
            }
            face.getMidPoint(midpoint);
            boolean centeredEnough = midpoint.x > faceBitmap.getWidth() * 0.08f
                    && midpoint.x < faceBitmap.getWidth() * 0.92f
                    && midpoint.y > faceBitmap.getHeight() * 0.08f
                    && midpoint.y < faceBitmap.getHeight() * 0.92f;
            if (face.confidence() >= MIN_FACE_CONFIDENCE
                    && face.eyesDistance() >= minEyeDistance
                    && centeredEnough) {
                float score = face.confidence() * face.eyesDistance();
                if (score > bestScore) {
                    bestScore = score;
                    bestFace = face;
                    bestMidpoint.set(midpoint);
                }
            }
        }

        if (bestFace == null) {
            Log.d(TAG, "extractUsableFaceBitmap: no usable face found in " + faceBitmap.getWidth() + "x" + faceBitmap.getHeight());
            return null;
        }

        float eyeDistance = bestFace.eyesDistance();
        int cropSize = Math.round(eyeDistance * 4.8f);
        cropSize = Math.max(cropSize, Math.round(Math.min(bitmap.getWidth(), bitmap.getHeight()) * 0.28f));
        cropSize = Math.min(cropSize, Math.min(bitmap.getWidth(), bitmap.getHeight()));
        int centerX = Math.round(bestMidpoint.x);
        int centerY = Math.round(bestMidpoint.y + eyeDistance * 0.62f);
        int left = clampInt(centerX - cropSize / 2, 0, bitmap.getWidth() - cropSize);
        int top = clampInt(centerY - cropSize / 2, 0, bitmap.getHeight() - cropSize);
        Bitmap cropped = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize);
        Bitmap facePhoto = scaleBitmapDown(cropped, 256);
        Log.d(TAG, "extractUsableFaceBitmap: accepted face crop " + facePhoto.getWidth() + "x" + facePhoto.getHeight());
        return facePhoto;
    }

    private Bitmap createFaceDetectorBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width % 2 != 0) {
            width -= 1;
        }
        if (width < 2 || height < 2) {
            return null;
        }

        Bitmap source = bitmap;
        if (width != bitmap.getWidth()) {
            source = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        if (source.getConfig() == Bitmap.Config.RGB_565) {
            return source;
        }
        return source.copy(Bitmap.Config.RGB_565, false);
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private Bitmap decodeBitmap(Uri imageUri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    int sourceWidth = info.getSize().getWidth();
                    int sourceHeight = info.getSize().getHeight();
                    int longestSide = Math.max(sourceWidth, sourceHeight);
                    if (longestSide > 1024) {
                        float scale = 1024f / longestSide;
                        decoder.setTargetSize(Math.max(1, Math.round(sourceWidth * scale)), Math.max(1, Math.round(sourceHeight * scale)));
                    }
                });
            } else {
                bitmap = decodeSampledBitmap(imageUri, 1024);
            }
            if (bitmap == null) {
                Log.w(TAG, "decodeBitmap: decoder returned no bitmap");
                return null;
            }
            Log.d(TAG, "decodeBitmap: original image " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return scaleBitmapDown(bitmap, 512);
        } catch (IOException | SecurityException | OutOfMemoryError exception) {
            Log.w(TAG, "decodeBitmap: failed to decode image", exception);
            return null;
        }
    }

    private Bitmap decodeSampledBitmap(Uri imageUri, int maxSize) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSize);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try (InputStream input = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            if (bitmap == null) {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
            return bitmap;
        }
    }

    private int sampleSizeFor(int width, int height, int maxSize) {
        int sampleSize = 1;
        int longestSide = Math.max(width, height);
        while (longestSide / (sampleSize * 2) >= maxSize) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= maxSize) {
            Log.d(TAG, "scaleBitmapDown: no scaling needed");
            return bitmap;
        }

        float scale = maxSize / (float) longestSide;
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);
        Log.d(TAG, "scaleBitmapDown: scaled to " + scaledWidth + "x" + scaledHeight);
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            enableLegacyImmersiveMode();
        }
    }

    @SuppressWarnings("deprecation")
    private void enableLegacyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}
