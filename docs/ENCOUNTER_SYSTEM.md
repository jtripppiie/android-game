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

Salmon Rush precision and ground cards can now cut holes in the normal ground
line with moving water. The player can clear the gap, take an upper platform, or
shoot the water to create a brittle ice bridge. The bridge remains interactive:
it can be cracked by shots or deliberately smashed by holding aim-down during a
hard fall.

Aim-down now doubles as fast-fall control while airborne. It tightens jump arcs,
enables stomp-through shortcuts, and creates a faster but riskier response to
wind. Dark Winter applies oscillating lateral wind only while airborne, keeping
ground movement predictable while making its high routes stage-specific.

First-time landings on authored route platforms award active execution. Moving
and high-line landings score more, increase combo, and extend an already-active
FLOW state. Falling from a missed platform returns to the ground route.

The Java deck contains at least 24 authored recipes spanning stage onboarding,
ground gambits, precision routes, high lines, multi-wildlife interactions, and
four FLOW-only cards. Tests enforce minimum route and FLOW vocabulary.

## Godot parity

`TrailEncounterCard` and `TrailEncounterDirector` mirror the same route types,
budgets, history constraints, deterministic selection, and FLOW requirements.
The Chugach slice prebuilds a seeded encounter sequence and exposes its current
route in the HUD. It now includes an upper moving-platform chain, precision line
with snowball-reactive ice, lower wildlife route, fallbacks, and convergence
section. `ReactiveIce` provides a reusable two-hit destructible terrain body.
`FreezableWater` is a lethal route volume that converts into `ReactiveIce` when
hit by a snowball, matching the Salmon Rush decision in the Android game.

## Raster presentation

Physical route platforms now use `route_platform_ice.png`, a transparent,
hand-painted blue-ice sprite rather than procedural rounded rectangles. Android
scales the sprite to the collision width while retaining explicit crack overlays
for brittle state. The Godot route builder uses the same source for thin route
platforms.

Laser attacks use `boss_laser_emitter.png`, a compact mechanical aperture with a
contained red iris and white pupil. The emitter center, beam start, collision
segment, tell line, reflection target, and debug origin all call the same
`bossLaserEyeX/Y` functions. Polar Bear coordinates were measured against the
actual standing sprite: `-0.48 radius` horizontally and `0.895 sprite height`
above its bottom anchor. The emitter is repainted over the boss during the tell;
previously the tell was drawn first and then hidden underneath the boss sprite.

## Extension rule

Add complexity by authoring a new relationship or route choice. Do not add a card
whose only difference is more objects or less warning time. Every card must have
at least two credible responses and must fit within its declared threat budget.
