package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BossStateMachineTest {
    @Test
    public void followsEnterTellAttackRecoverCycle() {
        assertEquals(BossStateMachine.ENTER_TELL,
                BossStateMachine.transition(0, true, 0f, 1f, 1f, 1f, 1f));
        assertEquals(BossStateMachine.BEGIN_ATTACK,
                BossStateMachine.transition(1, false, 1f, 1f, 1f, 1f, 1f));
        assertEquals(BossStateMachine.ENTER_RECOVER,
                BossStateMachine.transition(2, false, 1f, 1f, 1f, 1f, 1f));
        assertEquals(BossStateMachine.NEXT_TELL,
                BossStateMachine.transition(3, false, 1f, 1f, 1f, 1f, 1f));
    }

    @Test
    public void fasterStateSpeedShortensTransitionTime() {
        assertEquals(BossStateMachine.BEGIN_ATTACK,
                BossStateMachine.transition(1, false, 0.5f, 1f, 1f, 1f, 2f));
    }
}
