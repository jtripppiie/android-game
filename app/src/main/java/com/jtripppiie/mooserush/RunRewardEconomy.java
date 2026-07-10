package com.jtripppiie.mooserush;

final class RunRewardEconomy {
    static final int BASE_OUTFIT_UNLOCK_MASK = 0b0000_1111;
    static final int DAILY_BASE_REWARD = 24;

    private RunRewardEconomy() {
    }

    static boolean isOutfitUnlocked(int unlockMask, int outfitIndex) {
        if (outfitIndex < 0 || outfitIndex >= Integer.SIZE - 1) {
            return false;
        }
        return (unlockMask & (1 << outfitIndex)) != 0;
    }

    static int unlockOutfit(int unlockMask, int outfitIndex) {
        if (outfitIndex < 0 || outfitIndex >= Integer.SIZE - 1) {
            return unlockMask;
        }
        return unlockMask | (1 << outfitIndex);
    }

    static int tokensForRun(int missionsCompleted, int bestCombo, int stars, boolean stageCleared, boolean perfectRun) {
        int missionTokens = Math.max(0, missionsCompleted) * 4;
        int comboTokens = bestCombo >= 12 ? 8 : bestCombo >= 7 ? 5 : bestCombo >= 3 ? 2 : 0;
        int starTokens = Math.min(10, Math.max(0, stars) * 2);
        int clearTokens = stageCleared ? 12 : 0;
        int perfectTokens = stageCleared && perfectRun ? 10 : 0;
        return missionTokens + comboTokens + starTokens + clearTokens + perfectTokens;
    }

    static boolean canUnlockOutfit(int trailTokens, int unlockMask, int outfitIndex, int[] outfitCosts) {
        if (outfitIndex < 0 || outfitIndex >= outfitCosts.length) {
            return false;
        }
        return !isOutfitUnlocked(unlockMask, outfitIndex) && trailTokens >= outfitCosts[outfitIndex];
    }

    static int dailyStageIndex(int dayKey, int stageCount) {
        if (stageCount <= 0) {
            return 0;
        }
        return Math.floorMod(dayKey * 3 + 1, stageCount);
    }

    static int dailyGateGoal(int dayKey, int stageGoalGates) {
        if (stageGoalGates <= 3) {
            return Math.max(0, stageGoalGates);
        }
        int pressure = Math.floorMod(dayKey, 3);
        return Math.max(3, Math.min(stageGoalGates, stageGoalGates - 1 + pressure));
    }

    static int dailyReward(int streakBeforeClaim) {
        return DAILY_BASE_REWARD + Math.min(7, Math.max(0, streakBeforeClaim)) * 4;
    }

    static boolean canClaimDailyReward(boolean dailyRushMode, boolean alreadyAwarded,
                                       int lastCompletedDay, int todayKey,
                                       int selectedStage, int dailyStage) {
        return dailyRushMode
                && !alreadyAwarded
                && lastCompletedDay != todayKey
                && selectedStage == dailyStage;
    }

    static int nextDailyStreak(int lastCompletedDay, int todayKey, int currentStreak) {
        if (lastCompletedDay == todayKey) {
            return Math.max(1, currentStreak);
        }
        if (lastCompletedDay == todayKey - 1) {
            return Math.max(1, currentStreak + 1);
        }
        return 1;
    }
}
