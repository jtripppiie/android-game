package com.jtripppiie.mooserush;

/** Stage-specific boss strategy selection without any Android dependencies. */
final class StageBossRules {
    private StageBossRules() {
    }

    static boolean usesLaserTuning(int stage) {
        return stage == 0 || stage == 4;
    }

    static int nextPattern(int stage, int health, int maxHealth, int patternCount,
                           int lunge, int snowWave, int summon, int laser) {
        if (stage == 0) {
            // The Midnight Sun is primarily a laser-based boss.
            int step = patternCount % 4;
            if (step == 0) return laser;
            if (step == 2) return laser;
            return step == 1 ? lunge : snowWave;
        }
        if (stage == 3) {
            return patternCount % 3 == 2 ? summon : lunge;
        }
        return BossTuning.nextPattern(usesLaserTuning(stage), health, maxHealth, patternCount,
                lunge, snowWave, summon, laser);
    }
}
