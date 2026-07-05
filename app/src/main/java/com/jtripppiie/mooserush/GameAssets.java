package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

final class GameAssets {
    private final Drawable backgroundMidnightSun;
    private final Drawable backgroundDarkWinter;
    private final Drawable treeSummer;
    private final Drawable treeWinter;
    private final Bitmap mooseWalkSheet;
    private final Bitmap bearWalkSheet;
    private final Bitmap polarBearWalkSheet;
    private final Bitmap wolfRunSheet;
    private final Bitmap bearRoarSprite;
    private final Bitmap polarBearRoarSprite;
    private final Bitmap salmonSwimSheet;
    private final Bitmap eagleFlySheet;

    GameAssets(Context context) {
        backgroundMidnightSun = context.getDrawable(R.drawable.background_midnight_sun_art);
        backgroundDarkWinter = context.getDrawable(R.drawable.background_dark_winter_art);
        treeSummer = context.getDrawable(R.drawable.sprite_tree_summer);
        treeWinter = context.getDrawable(R.drawable.sprite_tree_winter);
        mooseWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_moose_walk);
        bearWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_bear_walk);
        polarBearWalkSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_polar_bear_walk);
        wolfRunSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_wolf_run);
        bearRoarSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.sprite_bear_roar);
        polarBearRoarSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.sprite_polar_bear_roar);
        salmonSwimSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_salmon_swim);
        eagleFlySheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheet_eagle_fly);
    }

    Drawable background(boolean dark, boolean winter) {
        return dark || winter ? backgroundDarkWinter : backgroundMidnightSun;
    }

    Drawable tree(boolean winter) {
        return winter ? treeWinter : treeSummer;
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
}
