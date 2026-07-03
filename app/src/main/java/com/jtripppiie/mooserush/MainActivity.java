package com.jtripppiie.mooserush;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = "YouRushDebug";
    private static final int REQUEST_PLAYER_PHOTO = 1001;
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
        gameView = new AlaskaNearMissMooseRushView(this);
        gameView.setPhotoRequestListener(this::openPhotoPicker);
        setContentView(createGameRoot());
        loadSavedPlayerPhoto();
        enableImmersiveMode();
        Log.d(TAG, "onCreate: Alaska near-miss game view attached, version badge added, immersive mode enabled");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode != REQUEST_PLAYER_PHOTO || resultCode != RESULT_OK || data == null || data.getData() == null) {
            Log.d(TAG, "onActivityResult: no usable photo selected");
            return;
        }

        Uri selectedImageUri = data.getData();
        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(selectedImageUri, flags);
            Log.d(TAG, "onActivityResult: persistent photo permission granted");
        } catch (SecurityException exception) {
            Log.d(TAG, "onActivityResult: persistent permission unavailable; session photo can still work", exception);
        }

        Bitmap selectedPhoto = decodeBitmap(selectedImageUri);
        if (selectedPhoto != null) {
            prefs.edit().putString(PREF_PLAYER_PHOTO_URI, selectedImageUri.toString()).apply();
            gameView.setPlayerPhoto(selectedPhoto);
            Log.d(TAG, "onActivityResult: player photo loaded " + selectedPhoto.getWidth() + "x" + selectedPhoto.getHeight());
        } else {
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
            badgeParams.setMargins(dp(12), 0, 0, dp(88));
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
        if (savedPhoto != null) {
            gameView.setPlayerPhoto(savedPhoto);
            Log.d(TAG, "loadSavedPlayerPhoto: restored saved photo " + savedPhoto.getWidth() + "x" + savedPhoto.getHeight());
        } else {
            prefs.edit().remove(PREF_PLAYER_PHOTO_URI).apply();
            Log.w(TAG, "loadSavedPlayerPhoto: saved photo failed, clearing saved URI");
        }
    }

    private Bitmap decodeBitmap(Uri imageUri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
            Log.d(TAG, "decodeBitmap: original image " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return scaleBitmapDown(bitmap, 512);
        } catch (IOException | SecurityException exception) {
            Log.w(TAG, "decodeBitmap: failed to decode image", exception);
            return null;
        }
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
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}
