package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EncounterDirectorTest {
    @Test
    public void sameSeedProducesSameAuthoredRun() {
        EncounterDirector a = new EncounterDirector(1776L);
        EncounterDirector b = new EncounterDirector(1776L);
        for (int gates = 0; gates < 12; gates++) {
            assertEquals(a.next(4, gates, gates > 5).id, b.next(4, gates, gates > 5).id);
        }
    }

    @Test
    public void directorAvoidsImmediateCardAndRouteRepeats() {
        EncounterDirector director = new EncounterDirector(42L);
        EncounterCard previous = director.next(4, 8, true);
        for (int i = 0; i < 8; i++) {
            EncounterCard next = director.next(4, 8, true);
            assertFalse(previous.id.equals(next.id));
            assertTrue(previous.route != next.route);
            previous = next;
        }
    }

    @Test
    public void flowRaisesThreatBudgetWithoutBreakingCap() {
        assertTrue(EncounterDirector.threatBudget(3, 8, true)
                > EncounterDirector.threatBudget(3, 8, false));
        assertTrue(EncounterDirector.threatBudget(99, 99, true) <= 8);
    }
}
