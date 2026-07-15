package com.jtripppiie.mooserush;

final class ArcadeScoring {
    private static final int MAX_SCORE_MULTIPLIER = 4;
    static final float COMBO_WINDOW_SECONDS = 2.8f;

    private ArcadeScoring() {
    }

    static int scoreMultiplierForCombo(int combo) {
        if (combo >= 10) return MAX_SCORE_MULTIPLIER;
        if (combo >= 7) return 3;
        if (combo >= 4) return 2;
        return 1;
    }

    static float comboWindowSeconds(int combo) {
        return Math.max(2.15f, COMBO_WINDOW_SECONDS - Math.max(0, combo - 1) * 0.045f);
    }

    static int actionAward(int amount, int combo, boolean surge, float perkMultiplier) {
        int multiplier = scoreMultiplierForCombo(combo);
        if (surge) multiplier = Math.min(MAX_SCORE_MULTIPLIER, multiplier + 1);
        return Math.round(Math.max(0, amount) * multiplier * Math.max(1f, perkMultiplier));
    }

    static int bankedAward(int amount) {
        return Math.max(0, amount);
    }

    static int stageClearBonus(int selectedStage, int bestCombo, int stars) {
        int stageBonus = 100 + Math.max(0, selectedStage) * 40;
        int comboBonus = Math.min(120, Math.max(0, bestCombo) * 5);
        int starBonus = Math.max(0, stars) * 10;
        return stageBonus + comboBonus + starBonus;
    }
}
