package com.jtripppiie.mooserush;

/** Pure pacing rules that turn a flat random run into repeating encounter beats. */
final class RushDirector {
    static final int BEAT_LAUNCH = 0;
    static final int BEAT_PRECISION = 1;
    static final int BEAT_WILDLIFE = 2;
    static final int BEAT_JACKPOT = 3;

    private RushDirector() {
    }

    static int beatFor(int gatesPassed) {
        if (gatesPassed <= 0) return BEAT_LAUNCH;
        int step = (gatesPassed - 1) % 3;
        if (step == 0) return BEAT_PRECISION;
        if (step == 1) return BEAT_WILDLIFE;
        return BEAT_JACKPOT;
    }

    static String beatLabel(int gatesPassed) {
        int beat = beatFor(gatesPassed);
        if (beat == BEAT_PRECISION) return "PRECISION";
        if (beat == BEAT_WILDLIFE) return "WILDLIFE RUSH";
        if (beat == BEAT_JACKPOT) return "JACKPOT LINE";
        return "LAUNCH";
    }

    static int starTrailCount(int gatesPassed) {
        int beat = beatFor(gatesPassed);
        if (beat == BEAT_PRECISION) return 3;
        if (beat == BEAT_WILDLIFE) return 2;
        if (beat == BEAT_JACKPOT) return 4;
        return 1;
    }

    static float gateCooldownMultiplier(int gatesPassed, boolean flowActive) {
        float multiplier = beatFor(gatesPassed) == BEAT_JACKPOT ? 1.08f : 0.92f;
        return flowActive ? multiplier * 0.80f : multiplier;
    }

    static float hazardCooldownMultiplier(int gatesPassed, boolean flowActive) {
        int beat = beatFor(gatesPassed);
        float multiplier = beat == BEAT_WILDLIFE ? 0.68f : beat == BEAT_JACKPOT ? 1.18f : 0.92f;
        return flowActive ? multiplier * 0.82f : multiplier;
    }

    static float worldSpeedMultiplier(boolean flowActive) {
        return flowActive ? 1.14f : 1f;
    }

    static float horizontalSpeedMultiplier(boolean flowActive) {
        return flowActive ? 1.12f : 1f;
    }

    static int hazardWaveSize(int stage, int gatesPassed) {
        if (gatesPassed < 3) return 1;
        if (stage >= 3 && gatesPassed >= 6) return 3;
        if (beatFor(gatesPassed) == BEAT_WILDLIFE || gatesPassed >= 5) return 2;
        return 1;
    }

    static float hazardWaveSpacingDp(int waveIndex) {
        return 104f + waveIndex * 18f;
    }
}
