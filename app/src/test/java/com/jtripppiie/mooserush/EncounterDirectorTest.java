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

    @Test
    public void deckHasEnoughAuthoredVocabularyForDistinctRuns() {
        EncounterCard[] cards = EncounterDeck.cards();
        assertTrue(cards.length >= 32);
        int flowCards = 0;
        int highRoutes = 0;
        int groundRoutes = 0;
        for (EncounterCard card : cards) {
            assertTrue(card.hazards.length > 0);
            assertTrue(card.starCount > 0);
            if (card.flowVariant) flowCards++;
            if (card.route == EncounterCard.ROUTE_HIGH) highRoutes++;
            if (card.route == EncounterCard.ROUTE_GROUND) groundRoutes++;
        }
        assertTrue(flowCards >= 4);
        assertTrue(highRoutes >= 6);
        assertTrue(groundRoutes >= 6);
    }

    @Test
    public void finalQuarterUsesStageSpecificMasteryGauntlets() {
        EncounterDirector director = new EncounterDirector(400L);
        EncounterCard card = director.next(3, 14, 18, false);
        assertTrue(card.mastery);
        assertEquals(3, card.minStage);
    }
}
