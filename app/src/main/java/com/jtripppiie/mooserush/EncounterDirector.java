package com.jtripppiie.mooserush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Seeded selector with history and cognitive-load constraints. */
final class EncounterDirector {
    private final Random random;
    private String previousId = "";
    private int previousRoute = -1;

    EncounterDirector(long seed) {
        random = new Random(seed);
    }

    EncounterCard next(int stage, int gates, boolean flow) {
        int budget = threatBudget(stage, gates, flow);
        List<EncounterCard> candidates = new ArrayList<>();
        for (EncounterCard card : EncounterDeck.cards()) {
            if (card.supports(stage, gates, flow, budget) && !card.id.equals(previousId)) {
                candidates.add(card);
            }
        }
        if (candidates.isEmpty()) {
            candidates.add(EncounterDeck.cards()[0]);
        }

        // Prefer a different route so consecutive encounters ask new questions.
        List<EncounterCard> routeChanges = new ArrayList<>();
        for (EncounterCard card : candidates) {
            if (card.route != previousRoute) routeChanges.add(card);
        }
        List<EncounterCard> pool = routeChanges.isEmpty() ? candidates : routeChanges;
        EncounterCard selected = pool.get(random.nextInt(pool.size()));
        previousId = selected.id;
        previousRoute = selected.route;
        return selected;
    }

    static int threatBudget(int stage, int gates, boolean flow) {
        int budget = 3 + Math.min(2, stage / 2) + Math.min(2, gates / 4);
        return Math.min(8, budget + (flow ? 2 : 0));
    }
}
