# Authored Encounter System

You Rush now generates runs from authored encounter cards instead of composing
every threat independently. The system follows `GAMEPLAY_REVAMP.md`: pure pacing
rules remain testable, rendering stays in `MooseRushView`, FLOW increases both
risk and opportunity, and every new rule changes a player decision.

## Java responsibilities

- `EncounterCard`: immutable route, threat, reward, and FLOW recipe.
- `EncounterDeck`: authored vocabulary and stage onboarding gates.
- `EncounterDirector`: seeded selection, threat budget, and recent-history rules.
- `MooseRushView`: turns the selected recipe into gates, stars, wildlife, and
  reaction feedback.

The director rotates ground, precision, and high routes where the available deck
allows it. Threat budgets rise with stage progress and FLOW, capped at eight.
Cards can require a stage, gate count, or active FLOW state.

## Implemented systemic reactions

- FLOW snowballs break Dark Winter icebergs and Bear Country snowbanks to open
  an alternate route.
- Snowballing a bear into nearby wildlife clears both threats and awards a
  wildlife-collision chain.
- Interrupting a Dark Winter eagle drops an icicle into the ground lane.
- Empowered shots ricochet into a nearby second target and extend FLOW.
- Reward arcs and obstacle height respond to the active route card.

## Spatial route runtime

Encounter routes now create physical one-way-style footing in the Android run:

- High cards build a three-platform traversal chain with a vertically moving
  middle step and a recoverable fall to the ground route.
- Precision cards introduce one timed middle platform rather than a mandatory
  full-height commitment.
- FLOW ground cards can expose a fast low bridge beneath aerial pressure.
- Brittle routes crack after one normal hit, shatter after two, or break at once
  from an empowered shot.
- The landing solver checks downward crossings, horizontal overlap, moving
  platform position, and ground fallback each frame.

Wildlife now has intent states. Eagles slow and draw their committed dive line
before accelerating toward a captured vertical target. Bears, polar bears, and
moose slow to telegraph a ground lane, then commit to a faster charge. Committed
heavy wildlife can break gates and brittle platforms, rewarding deliberate bait.

Boss arenas reuse these systems. Each stage receives a different arrangement of
moving, fixed, or brittle footing. FLOW shots can reflect active lasers, shattered
ice projectiles can create temporary platforms, lasers damage brittle platforms,
and enraged Moose/Polar Bear charges collapse arena footing.

## Godot parity

`TrailEncounterCard` and `TrailEncounterDirector` mirror the same route types,
budgets, history constraints, deterministic selection, and FLOW requirements.
The Chugach slice prebuilds a seeded encounter sequence and exposes its current
route in the HUD. It now includes an upper moving-platform chain, precision line
with snowball-reactive ice, lower wildlife route, fallbacks, and convergence
section. `ReactiveIce` provides a reusable two-hit destructible terrain body.

## Extension rule

Add complexity by authoring a new relationship or route choice. Do not add a card
whose only difference is more objects or less warning time. Every card must have
at least two credible responses and must fit within its declared threat budget.
