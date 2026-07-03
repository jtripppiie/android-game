package com.jtripppiie.mooserush;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final int REQUEST_PLAYER_PHOTO = 1001;
    private static final String PREFS_NAME = "moose_rush";
    private static final String PREF_PLAYER_PHOTO_URI = "player_photo_uri";

    private MooseRushView gameView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gameView = new MooseRushView(this);
        gameView.setPhotoRequestListener(this::openPhotoPicker);
        setContentView(gameView);
        loadSavedPlayerPhoto();
        enableImmersiveMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveMode();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (gameView != null) {
            gameView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PLAYER_PHOTO || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        Uri selectedImageUri = data.getData();
        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(selectedImageUri, flags);
        } catch (SecurityException ignored) {
            // Some providers do not offer persistable grants. The photo can still be used during this session.
        }

        Bitmap selectedPhoto = decodeBitmap(selectedImageUri);
        if (selectedPhoto != null) {
            prefs.edit().putString(PREF_PLAYER_PHOTO_URI, selectedImageUri.toString()).apply();
            gameView.setPlayerPhoto(selectedPhoto);
        }
        enableImmersiveMode();
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PLAYER_PHOTO);
    }

    private void loadSavedPlayerPhoto() {
        String savedUri = prefs.getString(PREF_PLAYER_PHOTO_URI, null);
        if (savedUri == null) {
            return;
        }

        Bitmap savedPhoto = decodeBitmap(Uri.parse(savedUri));
        if (savedPhoto != null) {
            gameView.setPlayerPhoto(savedPhoto);
        } else {
            prefs.edit().remove(PREF_PLAYER_PHOTO_URI).apply();
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
            return scaleBitmapDown(bitmap, 512);
        } catch (IOException | SecurityException exception) {
            return null;
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= maxSize) {
            return bitmap;
        }

        float scale = maxSize / (float) longestSide;
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
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
