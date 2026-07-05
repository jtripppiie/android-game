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

    @Test
    public void dailyChallengeRotatesWithinStageCount() {
        assertEquals(1, RunRewardEconomy.dailyStageIndex(0, 5));
        assertEquals(4, RunRewardEconomy.dailyStageIndex(1, 5));
        assertEquals(2, RunRewardEconomy.dailyStageIndex(2, 5));
    }

    @Test
    public void dailyGateGoalStaysFair() {
        assertEquals(4, RunRewardEconomy.dailyGateGoal(0, 5));
        assertEquals(5, RunRewardEconomy.dailyGateGoal(1, 5));
        assertEquals(5, RunRewardEconomy.dailyGateGoal(2, 5));
        assertEquals(3, RunRewardEconomy.dailyGateGoal(0, 3));
        assertEquals(2, RunRewardEconomy.dailyGateGoal(1, 2));
        assertEquals(0, RunRewardEconomy.dailyGateGoal(1, 0));
    }

    @Test
    public void dailyRewardAndStreakScaleWithoutExploding() {
        assertEquals(24, RunRewardEconomy.dailyReward(0));
        assertEquals(32, RunRewardEconomy.dailyReward(2));
        assertEquals(52, RunRewardEconomy.dailyReward(99));

        assertEquals(1, RunRewardEconomy.nextDailyStreak(99, 100, 0));
        assertEquals(4, RunRewardEconomy.nextDailyStreak(99, 100, 3));
        assertEquals(1, RunRewardEconomy.nextDailyStreak(50, 100, 9));
        assertEquals(5, RunRewardEconomy.nextDailyStreak(100, 100, 5));
    }
}
