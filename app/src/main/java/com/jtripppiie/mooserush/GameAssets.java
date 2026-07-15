package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

/*
 * GameAssets is the game's art shelf.
 *
 * Android stores images in res/drawable* and app/src/main/assets. Loading them
 * once here keeps the main game code cleaner. Other classes ask GameAssets for
 * the picture they need instead of knowing file names.
 */
final class GameAssets {
    // This file lives in app/src/main/assets/branding, not res/drawable.
    private static final String BRANDING_LOGO_ASSET = "branding/tripperdeelabs-logo.png";

    private final Drawable backgroundMidnightSun;
    private final Drawable backgroundDarkWinter;
    private final Drawable treeSummer;
    private final Drawable treeWinter;
    private final Drawable obstacleRiverLog;
    private final Drawable obstacleSnowbank;
    private final Drawable obstacleIceberg;
    private final Drawable obstacleAntlerBarricade;
    private final Bitmap mooseWalkSheet;
    private final Bitmap bearWalkSheet;
    private final Bitmap polarBearWalkSheet;
    private final Bitmap wolfRunSheet;
    private final Bitmap bearRoarSprite;
    private final Bitmap polarBearRoarSprite;
    private final Bitmap salmonSwimSheet;
    private final Bitmap eagleFlySheet;
    private final Bitmap routePlatformIce;
    private final Bitmap routePlatformSnow;
    private final Bitmap routePlatformMoving;
    private final Bitmap glacialWaterSurface;
    private final Bitmap bossLaserEmitter;
    private final Bitmap laserIceImpact;
    private final Bitmap brandingLogo;

    GameAssets(Context context) {
        /*
         * getDrawable loads XML/vector or bitmap drawables from res.
         * BitmapFactory.decodeResource loads sprite sheets when we need to pick
         * individual animation frames by pixel rectangle.
         */
        backgroundMidnightSun = context.getDrawable(R.drawable.background_midnight_sun_art);
        backgroundDarkWinter = context.getDrawable(R.drawable.background_dark_winter_art);
        treeSummer = context.getDrawable(R.drawable.sprite_tree_summer);
        treeWinter = context.getDrawable(R.drawable.sprite_tree_winter);
        obstacleRiverLog = context.getDrawable(R.drawable.obstacle_river_log_raster);
        obstacleSnowbank = context.getDrawable(R.drawable.obstacle_snowbank);
        obstacleIceberg = context.getDrawable(R.drawable.obstacle_iceberg);
        obstacleAntlerBarricade = context.getDrawable(R.drawable.obstacle_antler_barricade);
        mooseWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_moose_walk);
        bearWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_bear_walk);
        polarBearWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_polar_bear_walk);
        wolfRunSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_wolf_run);
        bearRoarSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.sprite_bear_roar);
        polarBearRoarSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.sprite_polar_bear_roar);
        salmonSwimSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_salmon_swim);
        eagleFlySheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_eagle_fly);
        routePlatformIce = BitmapFactory.decodeResource(context.getResources(), R.drawable.route_platform_ice);
        routePlatformSnow = BitmapFactory.decodeResource(context.getResources(), R.drawable.route_platform_snow);
        routePlatformMoving = BitmapFactory.decodeResource(context.getResources(), R.drawable.route_platform_moving);
        glacialWaterSurface = BitmapFactory.decodeResource(context.getResources(), R.drawable.glacial_water_surface);
        bossLaserEmitter = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss_laser_emitter);
        laserIceImpact = BitmapFactory.decodeResource(context.getResources(), R.drawable.laser_ice_impact);
        brandingLogo = decodeOptionalAsset(context, BRANDING_LOGO_ASSET);
    }

    private Bitmap decodeOptionalAsset(Context context, String assetPath) {
        /*
         * Optional assets can fail safely. If the logo is missing, the splash
         * screen can still draw its fallback instead of crashing at launch.
         */
        try (InputStream input = context.getAssets().open(assetPath)) {
            return BitmapFactory.decodeStream(input);
        } catch (IOException exception) {
            return null;
        }
    }

    Drawable background(boolean dark, boolean winter) {
        return dark || winter ? backgroundDarkWinter : backgroundMidnightSun;
    }

    Drawable tree(boolean winter) {
        return winter ? treeWinter : treeSummer;
    }

    Drawable obstacleRiverLog() {
        return obstacleRiverLog;
    }

    Drawable obstacleSnowbank() {
        return obstacleSnowbank;
    }

    Drawable obstacleIceberg() {
        return obstacleIceberg;
    }

    Drawable obstacleAntlerBarricade() {
        return obstacleAntlerBarricade;
    }

    Bitmap mooseWalkSheet() {
        return mooseWalkSheet;
    }

    Bitmap bearWalkSheet() {
        return bearWalkSheet;
    }

    Bitmap polarBearWalkSheet() {
        return polarBearWalkSheet;
    }

    Bitmap wolfRunSheet() {
        return wolfRunSheet;
    }

    Bitmap bearRoarSprite() {
        return bearRoarSprite;
    }

    Bitmap polarBearRoarSprite() {
        return polarBearRoarSprite;
    }

    Bitmap salmonSwimSheet() {
        return salmonSwimSheet;
    }

    Bitmap eagleFlySheet() {
        return eagleFlySheet;
    }

    Bitmap routePlatformIce() {
        return routePlatformIce;
    }

    Bitmap routePlatformSnow() {
        return routePlatformSnow;
    }

    Bitmap routePlatformMoving() {
        return routePlatformMoving;
    }

    Bitmap glacialWaterSurface() {
        return glacialWaterSurface;
    }

    Bitmap bossLaserEmitter() {
        return bossLaserEmitter;
    }

    Bitmap laserIceImpact() {
        return laserIceImpact;
    }

    Bitmap brandingLogo() {
        return brandingLogo;
    }
}
