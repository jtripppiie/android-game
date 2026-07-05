package com.jtripppiie.mooserush;

final class RunRewardEconomy {
    static final int BASE_OUTFIT_UNLOCK_MASK = 0b0000_1111;

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
}
