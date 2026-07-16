# You Rush 5.0.1: Graphics and Jump Refinement

This pass makes the finished art the actual in-game language. Godot previously
left strong repository assets unused and drew animals/bosses as placeholder
polygons. It now uses six-frame illustrated sheets for wolf, brown bear, salmon,
eagle, moose and polar bear; existing painted Midnight Sun/Dark Winter
backgrounds and summer/winter trees; illustrated snow/ice terrain caps; and the
existing runner, moving platform, launch pad and supply-block families.

The only generated addition is `godot/assets/collectibles_atlas.png`: a shared
trail-token, rescue-key and emergency-beacon family. It was generated with the
built-in image tool on a flat magenta key and locally converted to alpha. Prompt
direction: premium hand-painted Alaska adventure sprites, crisp navy outline,
gold/glacial-teal/coral palette, consistent upper-left lighting, three isolated
equal-scale objects, no text or scenery. The keyed source remains in the local
ignored `tmp/imagegen` workspace, not the shipped game.

Jumping was underpowered relative to the authored platform steps. Primary jump
speed rises from 610 to 750 while gravity falls from 1550 to 1450. This changes
the approximate ideal jump apex from 120 to 194 pixels. Coyote time rises from
0.11 to 0.14 seconds and input buffering from 0.13 to 0.16 seconds. One 88%
strength air jump is available and resets on landing. Releasing jump early still
produces a shorter arc, so precision is preserved.

Visual scale targets are gameplay-first: salmon < wolf/eagle < brown bear <
moose/polar boss, with the human remaining readable against every silhouette.
Collision shapes remain deliberately smaller than painted fur, wings and
antlers so decorative pixels do not create unfair hits.

Validation requires every reused/generated asset, animation references, jump
constants, and all five stage smoke launches. Real-device review should focus on
sprite readability at phone size, animation pacing, apparent contact with the
ground, platform edge clarity, and whether the air jump over-trivializes any
optional high route.

The final play audit drives the real player controller through movement,
buffered jumps/air jumps, collisions, pickups, checkpoint routes, continuous
snowball combat, every boss recovery cycle, and the finish beacon. Three full
runs of every stage passed. That audit exposed and led to five real spacing
changes at raised platform approaches. It also exposed an airborne Moose Pass
goal overshoot; the beacon trigger is now taller and the corrected stage passed
three further traversals. Observed full-stage completion times were roughly
31–84 simulated seconds, with no stage depending on invulnerability or a
teleport.
