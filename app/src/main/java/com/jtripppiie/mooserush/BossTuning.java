package com.jtripppiie.mooserush;

/*
 * BossTuning controls the boss rhythm.
 *
 * Boss fights are easier to understand when split into timing and patterns:
 * - timing says how long each state lasts
 * - patterns say which attack comes next
 *
 * MooseRushView owns the state machine; this file only chooses numbers.
 */
final class BossTuning {
    // Normal play must defeat the boss before this timer runs out.
    static final float BOSS_SURVIVAL_SECONDS = 65f;

    private BossTuning() {
    }

    static float stateSpeed(boolean finalStage, int health, int maxHealth, boolean enraged) {
        /*
         * phasePressure rises as health falls. This makes the boss naturally
         * speed up near defeat, which feels like a phase change.
         */
        float phasePressure = maxHealth <= 0 ? 0f : 1f - health / (float) maxHealth;
        return 1f + phasePressure * (finalStage ? 0.30f : 0.22f) + (enraged ? 0.12f : 0f);
    }

    static int nextPattern(boolean finalStage, int health, int maxHealth, int patternCount, int lunge, int snowWave, int summon, int laser) {
        /*
         * patternCount is a simple playlist index. Using modulo (%) lets the
         * boss cycle attacks without needing a big list object.
         */
        if (finalStage) {
            if (health <= maxHealth / 2) {
                int phaseTwoStep = patternCount % 5;
                if (phaseTwoStep == 2) return laser;
                if (phaseTwoStep == 3) return summon;
                return phaseTwoStep == 0 ? snowWave : lunge;
            }
            int openingStep = patternCount % 4;
            if (openingStep == 2) return summon;
            return openingStep == 0 ? lunge : snowWave;
        }
        if (health <= maxHealth / 2 && patternCount % 3 == 2) {
            return summon;
        }
        return patternCount % 2 == 0 ? lunge : snowWave;
    }

    static float tellDuration(boolean finalStage, boolean enraged) {
        // Tell is the warning window before the attack.
        return (finalStage ? 1.30f : 1.12f) * (enraged ? 0.94f : 1f);
    }

    static float attackDuration(boolean finalStage, boolean enraged, boolean lunge, boolean laser) {
        // Different attacks stay active for different amounts of time.
        if (lunge) {
            return (finalStage ? 0.74f : 0.64f) * (enraged ? 0.94f : 1f);
        }
        if (laser) {
            return 1.08f * (enraged ? 0.92f : 1f);
        }
        return (finalStage ? 0.50f : 0.44f) * (enraged ? 0.92f : 1f);
    }

    static float recoverDuration(boolean finalStage, boolean enraged) {
        // Recovery gives the player a breather after an attack.
        return (finalStage ? 1.28f : 1.12f) * (enraged ? 0.92f : 1f);
    }

    static int shotDamage(boolean weakWindow, boolean empowered) {
        if (!weakWindow) return 0;
        return empowered ? 2 : 1;
    }

    static int logShotDamage(boolean empowered) {
        return empowered ? 2 : 1;
    }

    static int logShotsRequired() {
        return 2;
    }
}
