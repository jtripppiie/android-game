package com.jtripppiie.mooserush;

/** Authored vocabulary. Randomness chooses between readable recipes, never raw spam. */
final class EncounterDeck {
    private static final EncounterCard[] CARDS = {
            new EncounterCard("clean_launch", 0, 0, EncounterCard.ROUTE_PRECISION, 2, 3, false, "STAGE"),
            new EncounterCard("ground_wildlife", 0, 2, EncounterCard.ROUTE_GROUND, 3, 2, false, "STAGE"),
            new EncounterCard("jump_then_hunt", 0, 3, EncounterCard.ROUTE_PRECISION, 4, 3, false, "STAGE", "WOLF"),
            new EncounterCard("log_wolf_sandwich", 0, 4, EncounterCard.ROUTE_GROUND, 5, 2, false, "WOLF", "STAGE"),
            new EncounterCard("midnight_highline", 0, 5, EncounterCard.ROUTE_HIGH, 6, 5, false, "WOLF", "WOLF"),
            new EncounterCard("salmon_arc", 1, 2, EncounterCard.ROUTE_HIGH, 4, 4, false, "SALMON", "STAGE"),
            new EncounterCard("freeze_or_fly", 1, 3, EncounterCard.ROUTE_PRECISION, 5, 3, false, "SALMON", "SALMON"),
            new EncounterCard("river_low_gamble", 1, 5, EncounterCard.ROUTE_GROUND, 6, 2, false, "SALMON", "WOLF"),
            new EncounterCard("salmon_staircase", 1, 7, EncounterCard.ROUTE_HIGH, 7, 5, false, "SALMON", "SALMON", "WOLF"),
            new EncounterCard("antler_squeeze", 2, 3, EncounterCard.ROUTE_GROUND, 5, 2, false, "MOOSE", "WOLF"),
            new EncounterCard("moose_breakout", 2, 5, EncounterCard.ROUTE_PRECISION, 6, 3, false, "MOOSE", "STAGE"),
            new EncounterCard("antler_skybridge", 2, 6, EncounterCard.ROUTE_HIGH, 7, 5, false, "WOLF", "MOOSE"),
            new EncounterCard("charge_bait_line", 2, 8, EncounterCard.ROUTE_GROUND, 7, 2, false, "MOOSE", "MOOSE"),
            new EncounterCard("eagle_crossfire", 3, 3, EncounterCard.ROUTE_PRECISION, 5, 3, false, "EAGLE", "WOLF"),
            new EncounterCard("dark_sky_chain", 3, 6, EncounterCard.ROUTE_HIGH, 7, 5, false, "EAGLE", "WOLF", "EAGLE"),
            new EncounterCard("wind_drop", 3, 5, EncounterCard.ROUTE_GROUND, 6, 2, false, "EAGLE", "EAGLE"),
            new EncounterCard("icicle_ladder", 3, 7, EncounterCard.ROUTE_HIGH, 8, 5, false, "EAGLE", "WOLF", "EAGLE"),
            new EncounterCard("stomp_escape", 3, 9, EncounterCard.ROUTE_PRECISION, 8, 4, false, "WOLF", "EAGLE", "WOLF"),
            new EncounterCard("bear_commitment", 4, 3, EncounterCard.ROUTE_GROUND, 6, 2, false, "BEAR", "WOLF"),
            new EncounterCard("bear_country_chain", 4, 6, EncounterCard.ROUTE_PRECISION, 8, 4, false, "POLAR", "WOLF", "BEAR"),
            new EncounterCard("polar_highground", 4, 5, EncounterCard.ROUTE_HIGH, 7, 5, false, "POLAR", "WOLF"),
            new EncounterCard("blizzard_split", 4, 8, EncounterCard.ROUTE_PRECISION, 8, 4, false, "BEAR", "WOLF", "POLAR"),
            new EncounterCard("stampede_breaker", 4, 10, EncounterCard.ROUTE_GROUND, 8, 2, false, "BEAR", "POLAR", "WOLF"),
            new EncounterCard("aurora_overdrive", 1, 3, EncounterCard.ROUTE_HIGH, 8, 6, true, "STAGE", "WOLF", "STAGE"),
            new EncounterCard("flow_ground_thread", 2, 5, EncounterCard.ROUTE_GROUND, 8, 5, true, "MOOSE", "WOLF", "STAGE"),
            new EncounterCard("flow_dark_dare", 3, 6, EncounterCard.ROUTE_HIGH, 8, 6, true, "EAGLE", "WOLF", "EAGLE"),
            new EncounterCard("flow_bear_gauntlet", 4, 7, EncounterCard.ROUTE_PRECISION, 8, 6, true, "BEAR", "WOLF", "POLAR"),
            new EncounterCard("sunset_mastery", 0, 6, EncounterCard.ROUTE_HIGH, 5, 6, false, true, "WOLF", "STAGE", "WOLF"),
            new EncounterCard("river_mastery", 1, 9, EncounterCard.ROUTE_PRECISION, 6, 6, false, true, "SALMON", "WOLF", "SALMON"),
            new EncounterCard("antler_mastery", 2, 11, EncounterCard.ROUTE_GROUND, 6, 6, false, true, "MOOSE", "WOLF", "MOOSE"),
            new EncounterCard("aurora_mastery", 3, 13, EncounterCard.ROUTE_HIGH, 6, 7, false, true, "EAGLE", "WOLF", "EAGLE"),
            new EncounterCard("whiteout_mastery", 4, 16, EncounterCard.ROUTE_PRECISION, 7, 8, false, true, "BEAR", "WOLF", "POLAR")
    };

    private EncounterDeck() {
    }

    static EncounterCard[] cards() {
        return CARDS.clone();
    }
}
