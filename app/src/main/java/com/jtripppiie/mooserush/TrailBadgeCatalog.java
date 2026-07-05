package com.jtripppiie.mooserush;

final class TrailBadgeCatalog {
    static final int BADGE_COUNT = 10;
    static final int TOKEN_REWARD_PER_BADGE = 18;

    private static final String[] BADGE_NAMES = {
            "First Trail",
            "Gate Skipper",
            "Star Scout",
            "Combo Spark",
            "Boss Buster",
            "Perfect Parka",
            "Daily Dasher",
            "Alaska Passport",
            "Aurora Chaser",
            "Mission Maker"
    };

    private TrailBadgeCatalog() {
    }

    static String badgeName(int index) {
        if (index < 0 || index >= BADGE_NAMES.length) {
            return "Trail Badge";
        }
        return BADGE_NAMES[index];
    }

    static boolean hasBadge(int badgeMask, int index) {
        if (index < 0 || index >= BADGE_COUNT) {
            return false;
        }
        return (badgeMask & (1 << index)) != 0;
    }

    static int badgeCount(int badgeMask) {
        int playableMask = badgeMask & ((1 << BADGE_COUNT) - 1);
        return Integer.bitCount(playableMask);
    }

    static int tokensForNewBadges(int newBadgeCount) {
        return Math.max(0, newBadgeCount) * TOKEN_REWARD_PER_BADGE;
    }

    static int newlyEarnedMask(
            int badgeMask,
            int score,
            int selectedStage,
            int finalUnlockedStage,
            int gatesPassed,
            int stars,
            int bestCombo,
            int missionsCompleted,
            boolean stageCleared,
            boolean perfectRun,
            boolean dailyCompleteToday,
            int dailyStreak,
            int auroraRushes) {
        int earned = 0;
        earned = maybeEarn(earned, badgeMask, 0, gatesPassed > 0 || score > 0);
        earned = maybeEarn(earned, badgeMask, 1, gatesPassed >= 5);
        earned = maybeEarn(earned, badgeMask, 2, stars >= 3);
        earned = maybeEarn(earned, badgeMask, 3, bestCombo >= 6);
        earned = maybeEarn(earned, badgeMask, 4, stageCleared);
        earned = maybeEarn(earned, badgeMask, 5, stageCleared && perfectRun);
        earned = maybeEarn(earned, badgeMask, 6, dailyCompleteToday && dailyStreak >= 1);
        earned = maybeEarn(earned, badgeMask, 7, finalUnlockedStage >= 4 || (stageCleared && selectedStage >= 4));
        earned = maybeEarn(earned, badgeMask, 8, auroraRushes > 0);
        earned = maybeEarn(earned, badgeMask, 9, missionsCompleted >= 3);
        return earned;
    }

    private static int maybeEarn(int earnedMask, int existingMask, int index, boolean condition) {
        if (!condition || hasBadge(existingMask, index)) {
            return earnedMask;
        }
        return earnedMask | (1 << index);
    }
}
