package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunRewardEconomyTest {

    @Test
    public void baseOutfitsStartUnlocked() {
        assertTrue(RunRewardEconomy.isOutfitUnlocked(RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 0));
        assertTrue(RunRewardEconomy.isOutfitUnlocked(RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 3));
        assertFalse(RunRewardEconomy.isOutfitUnlocked(RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 4));
    }

    @Test
    public void unlockOutfitAddsBitWithoutClearingProgress() {
        int mask = RunRewardEconomy.unlockOutfit(RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 5);

        assertTrue(RunRewardEconomy.isOutfitUnlocked(mask, 0));
        assertTrue(RunRewardEconomy.isOutfitUnlocked(mask, 5));
        assertFalse(RunRewardEconomy.isOutfitUnlocked(mask, 6));
    }

    @Test
    public void tokenRewardValuesSkillAndCompletion() {
        int survivalReward = RunRewardEconomy.tokensForRun(1, 4, 2, false, false);
        int clearReward = RunRewardEconomy.tokensForRun(3, 12, 5, true, true);

        assertEquals(10, survivalReward);
        assertEquals(52, clearReward);
    }

    @Test
    public void outfitUnlockRequiresEnoughTokensAndLockedState() {
        int[] costs = {0, 0, 0, 0, 35};

        assertFalse(RunRewardEconomy.canUnlockOutfit(34, RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 4, costs));
        assertTrue(RunRewardEconomy.canUnlockOutfit(35, RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 4, costs));
        assertFalse(RunRewardEconomy.canUnlockOutfit(99, RunRewardEconomy.unlockOutfit(RunRewardEconomy.BASE_OUTFIT_UNLOCK_MASK, 4), 4, costs));
    }
}
