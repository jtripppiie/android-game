package com.jtripppiie.mooserush;

/** Immutable authored encounter recipe selected by the deterministic director. */
final class EncounterCard {
    static final int ROUTE_GROUND = 0;
    static final int ROUTE_PRECISION = 1;
    static final int ROUTE_HIGH = 2;

    final String id;
    final int minStage;
    final int minGates;
    final int route;
    final int threatBudget;
    final String[] hazards;
    final int starCount;
    final boolean flowVariant;

    EncounterCard(String id, int minStage, int minGates, int route, int threatBudget,
                  int starCount, boolean flowVariant, String... hazards) {
        this.id = id;
        this.minStage = minStage;
        this.minGates = minGates;
        this.route = route;
        this.threatBudget = threatBudget;
        this.starCount = starCount;
        this.flowVariant = flowVariant;
        this.hazards = hazards;
    }

    boolean supports(int stage, int gates, boolean flow, int budget) {
        return stage >= minStage && gates >= minGates && threatBudget <= budget
                && (!flowVariant || flow);
    }

    String routeLabel() {
        if (route == ROUTE_HIGH) return "HIGH ROUTE";
        if (route == ROUTE_GROUND) return "GROUND GAMBIT";
        return "PRECISION LINE";
    }
}
