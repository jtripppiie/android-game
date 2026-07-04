package com.jtripppiie.mooserush;

final class GameState {
    int score;
    int runStageScore;
    int lives = 3;
    int gatesPassed;
    int combo;
    int bestCombo;
    int xp;
    int level;
    boolean shieldActive;
    boolean muted;

    void resetRun() {
        score = 0;
        runStageScore = 0;
        lives = 3;
        gatesPassed = 0;
        combo = 0;
        bestCombo = 0;
        shieldActive = false;
        updateLevel();
    }

    void addScore(int amount) {
        score += amount;
        runStageScore += amount;
        xp += Math.max(0, amount);
        updateLevel();
    }

    void addCombo() {
        combo++;
        bestCombo = Math.max(bestCombo, combo);
    }

    void breakCombo() {
        combo = 0;
    }

    void updateLevel() {
        level = LevelCurve.levelIndex(xp) + 1;
    }
}
