package com.jtripppiie.mooserush;

final class ArcadeScoring {
    private static final int MAX_SCORE_MULTIPLIER = 4;

    private ArcadeScoring() {
    }

    static int scoreMultiplierForCombo(int combo) {
        if (combo >= 12) return MAX_SCORE_MULTIPLIER;
        if (combo >= 7) return 3;
        if (combo >= 3) return 2;
        return 1;
    }

    static int stageClearBonus(int selectedStage, int bestCombo, int stars) {
        int stageBonus = 100 + Math.max(0, selectedStage) * 40;
        int comboBonus = Math.min(120, Math.max(0, bestCombo) * 5);
        int starBonus = Math.max(0, stars) * 10;
        return stageBonus + comboBonus + starBonus;
    }
}
