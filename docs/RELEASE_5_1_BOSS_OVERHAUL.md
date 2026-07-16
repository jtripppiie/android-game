# You Rush 5.1: Distinct Boss Encounters

The five bosses no longer share one charge with different art. Every encounter
keeps the readable `tell → attack → recovery` contract but adds a stage-specific
hazard family:

- Midnight Sun launches staggered flare lanes.
- Salmon Rush throws gravity-driven splash arcs.
- Moose Pass sends a grounded antler shockwave.
- Dark Winter fires a three-height feather spread.
- Bear Country launches a three-snowball barrage.

Hostile hazards use their own collision layer: they hit the runner but cannot
erase player snowballs or accidentally armor a boss. The autonomous player now
detects incoming hazards and demonstrates a jump response during release QA.

Every boss arena gains an illustrated checkpoint. A failed pattern therefore
restarts at the encounter rather than replaying half a stage. Health is
rebalanced to `[7, 8, 9, 10, 12]`; difficulty comes from reading attacks and
using recovery windows, not waiting through 16 repetitive hits. Tells, recovery
timing, contact damage, health bars, names and pattern feedback remain visible.

The active documentation set was also reduced to current owner, QA, privacy,
maintenance, versioning, identifier, provenance, scale, and 5.x release files.
Older Java/beta notes moved to `docs/archive/` with no history deleted. Version,
debug-ID and privacy documents were rewritten to remove obsolete Java guidance.

Release gate: three complete autonomous traversals of every stage, structural
validation, matching-certificate Android export, native bridge inspection, and
real-device acceptance before public distribution.
