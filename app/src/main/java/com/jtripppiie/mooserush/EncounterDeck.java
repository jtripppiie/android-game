package com.jtripppiie.mooserush;

/** Authored vocabulary. Randomness chooses between readable recipes, never raw spam. */
final class EncounterDeck {
    private static final EncounterCard[] CARDS = {
            new EncounterCard("clean_launch", 0, 0, EncounterCard.ROUTE_PRECISION, 2, 3, false, "STAGE"),
            new EncounterCard("ground_wildlife", 0, 2, EncounterCard.ROUTE_GROUND, 3, 2, false, "STAGE"),
            new EncounterCard("jump_then_hunt", 0, 3, EncounterCard.ROUTE_PRECISION, 4, 3, false, "STAGE", "WOLF"),
            new EncounterCard("salmon_arc", 1, 2, EncounterCard.ROUTE_HIGH, 4, 4, false, "SALMON", "STAGE"),
            new EncounterCard("antler_squeeze", 2, 3, EncounterCard.ROUTE_GROUND, 5, 2, false, "MOOSE", "WOLF"),
            new EncounterCard("eagle_crossfire", 3, 3, EncounterCard.ROUTE_PRECISION, 5, 3, false, "EAGLE", "WOLF"),
            new EncounterCard("dark_sky_chain", 3, 6, EncounterCard.ROUTE_HIGH, 7, 5, false, "EAGLE", "WOLF", "EAGLE"),
            new EncounterCard("bear_commitment", 4, 3, EncounterCard.ROUTE_GROUND, 6, 2, false, "BEAR", "WOLF"),
            new EncounterCard("bear_country_chain", 4, 6, EncounterCard.ROUTE_PRECISION, 8, 4, false, "POLAR", "WOLF", "BEAR"),
            new EncounterCard("aurora_overdrive", 1, 3, EncounterCard.ROUTE_HIGH, 8, 6, true, "STAGE", "WOLF", "STAGE")
    };

    private EncounterDeck() {
    }

    static EncounterCard[] cards() {
        return CARDS.clone();
    }
}
