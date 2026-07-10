package com.jtripppiie.mooserush;

/** Pure boss-state transition rules, kept independent from rendering and input. */
final class BossStateMachine {
    static final int NONE = 0;
    static final int ENTER_TELL = 1;
    static final int BEGIN_ATTACK = 2;
    static final int ENTER_RECOVER = 3;
    static final int NEXT_TELL = 4;

    private BossStateMachine() {
    }

    static int transition(int state, boolean entranceComplete, float stateTimer,
                          float tellDuration, float attackDuration, float recoverDuration,
                          float stateSpeed) {
        if (state == 0 && entranceComplete) {
            return ENTER_TELL;
        }
        float safeSpeed = Math.max(0.01f, stateSpeed);
        if (state == 1 && stateTimer >= tellDuration / safeSpeed) {
            return BEGIN_ATTACK;
        }
        if (state == 2 && stateTimer >= attackDuration / safeSpeed) {
            return ENTER_RECOVER;
        }
        if (state == 3 && stateTimer >= recoverDuration / safeSpeed) {
            return NEXT_TELL;
        }
        return NONE;
    }
}
