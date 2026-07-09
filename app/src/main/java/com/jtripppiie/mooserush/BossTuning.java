package com.jtripppiie.mooserush;

final class BossTuning {
    static final float BOSS_SURVIVAL_SECONDS = 32f;

    private BossTuning() {
    }

    static float stateSpeed(boolean finalStage, int health, int maxHealth, boolean enraged) {
        float phasePressure = maxHealth <= 0 ? 0f : 1f - health / (float) maxHealth;
        return 1f + phasePressure * (finalStage ? 0.55f : 0.35f) + (enraged ? 0.22f : 0f);
    }

    static int nextPattern(boolean finalStage, int health, int maxHealth, int patternCount, int lunge, int snowWave, int summon, int laser) {
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
        return (finalStage ? 0.98f : 0.78f) * (enraged ? 0.90f : 1f);
    }

    static float attackDuration(boolean finalStage, boolean enraged, boolean lunge, boolean laser) {
        if (lunge) {
            return (finalStage ? 0.74f : 0.64f) * (enraged ? 0.94f : 1f);
        }
        if (laser) {
            return 1.08f * (enraged ? 0.92f : 1f);
        }
        return (finalStage ? 0.50f : 0.44f) * (enraged ? 0.92f : 1f);
    }

    static float recoverDuration(boolean finalStage, boolean enraged) {
        return (finalStage ? 0.92f : 0.72f) * (enraged ? 0.82f : 1f);
    }
}
