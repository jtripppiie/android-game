package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameStateTest {

    @Test
    public void exhaustingLivesMakesGameOverStateConsistent() {
        GameState state = new GameState();
        state.lives = 1;

        state.exhaustLives();

        assertEquals(0, state.lives);
    }
}
