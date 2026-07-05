package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.drawable.Drawable;

final class GameAssets {
    private final Drawable backgroundMidnightSun;
    private final Drawable backgroundDarkWinter;
    private final Drawable salmonHazard;
    private final Drawable mooseHazard;
    private final Drawable bearHazard;

    GameAssets(Context context) {
        backgroundMidnightSun = context.getDrawable(R.drawable.placeholder_background_midnight_sun);
        backgroundDarkWinter = context.getDrawable(R.drawable.placeholder_background_dark_winter);
        salmonHazard = context.getDrawable(R.drawable.placeholder_hazard_salmon);
        mooseHazard = context.getDrawable(R.drawable.placeholder_hazard_moose);
        bearHazard = context.getDrawable(R.drawable.placeholder_hazard_bear);
    }

    Drawable background(boolean dark, boolean winter) {
        return dark || winter ? backgroundDarkWinter : backgroundMidnightSun;
    }

    Drawable hazardForStage(int stage) {
        if (stage == 1) {
            return salmonHazard;
        }
        if (stage == 2) {
            return mooseHazard;
        }
        if (stage == 3 || stage == 4) {
            return bearHazard;
        }
        return salmonHazard;
    }

    Drawable bossForStage(int stage) {
        return hazardForStage(stage);
    }
}
