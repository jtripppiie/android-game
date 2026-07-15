# You Rush 3.19 Beta — Aerial Ring Routes

## Build

```text
versionName: 3.19.0-beta
versionCode: 387
build badge: ALASKA PASSPORT v3.87 BETA
```

## Purposeful Launches

Launch pads now lead into authored four-ring arcs instead of empty air. Ground,
precision and high routes use different launch heights but share a readable
rise-crest-fall rhythm. Salmon ground-water routes do not receive unsupported
rings unless FLOW has exposed their bridge route.

## Ring Chain

Collecting a ring:

- preserves a controlled amount of upward velocity;
- increases combo, score and Aurora energy;
- starts or extends a short Ring Rush timer;
- advances a 1.65-second trick-chain window.

Three rings ignite FLOW. Additional rings extend active FLOW. Missing the next
ring ends only the chain; it does not take control away from the player.

## Ring Rush

Ring Rush temporarily increases world pace by 12 percent and horizontal runner
control by 14 percent. The game becomes faster, but the player receives matching
steering authority. This keeps the mechanic consistent with FLOW's explicit
high-risk/high-reward contract.

## Godot Parity

`AuroraTrickRing` detects the runner and calls `collect_aurora_ring`. The Godot
runner tracks chain and surge timers, preserves lift, raises combo and increases
target movement speed during the surge. Directed encounters construct matching
four-ring arcs after each launch pad.

## Wildlife Scale Normalization

The same release centralizes Android wildlife dimensions in `WildlifeScale` and
renders trimmed visible frame content rather than full transparent atlas cells.
Moose is tallest/longest, polar bear is the heaviest bear, brown bear remains
larger than the runner's low threats, wolf is compact, eagle keeps a readable
wingspan without becoming plane-sized, and salmon remains the smallest mass.
The exact table is in [WILDLIFE_SCALE_CONTRACT.md](WILDLIFE_SCALE_CONTRACT.md).

Godot now gives bears a larger collision and drawn scale than wolves instead of
using one shared collision radius.
