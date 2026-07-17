# 5.3.1 Duplicate Entity Correction

Physical-device feedback exposed two authored/lifecycle defects that traversal
completion did not detect:

- Returning to the map left the previous `AlaskaStage` attached. Starting
  another stage added a second complete world and a second runner.
- Moose Pass combined its stage wildlife with an extra branch-route bear,
  crowding two large bear encounters into the same convergence section.

The main scene now owns exactly one stage. Menu/map transitions immediately
detach and free the previous world, stage startup refuses duplicate activation,
and a transition lock prevents rapid repeated button activation. The shared
route bear was removed because Moose Pass already authors wildlife around that
route.

The `--lifecycle-audit` regression starts stage 0, returns to the map, starts
stage 1, and asserts exactly one `active_stage` and one player after both
transitions. It passed, followed by a 5/5 traversal run.

The same source revision replaces the two-button movement cluster with a visible
four-way D-pad. Left/right auto-run, Up jumps, and Down crouches/stomps. The
right side now has deliberately distinct controls instead of matching generic
rectangles: a 150-by-150 gold Jump button, circular cyan Snow button, and
separate purple Dash button. The touch regression passed D-pad Up, simultaneous
movement/jump, auto-run, and independent release.

Status: validated source only. Do not claim or distribute a 5.3.1 APK until the
owner explicitly requests compilation.

Menu follow-up: primary menu buttons are now 82 pixels tall with 26-pixel
labels and 16-pixel corner radii. Main/customization screens use 20 pixels of
vertical separation, the six-button stage map uses 12 pixels so every option
and Back remain inside the viewport, and accessibility rows increase to 66
pixels with explicit spacing.

Readability follow-up: every gameplay action retains an internal label (`JUMP`,
`SNOW`, `DASH`) and the D-pad uses four directional symbols. Jump text is dark
on both its orange and bright pressed states; other actions use white on dark
cyan/purple. Menu pressed, focused, and disabled states now have explicit text,
fill, and border colors instead of inheriting ambiguous theme defaults.

Jump-contact follow-up: the previous stomp rule required downward speed above
520 pixels/second, so an ordinary jump landing near its apex could damage the
runner. An airborne runner now stomps when approaching an animal from at least
18 pixels above and moving downward or within 180 pixels/second of the apex.
Side and underside collisions still cause damage. Respawning now reports
`TRAIL RECOVERY · TRY AGAIN` rather than silently appearing to die from a jump.

Boss-facing follow-up: every wildlife sheet is authored facing left, but the
boss loop previously forced `flip_h = true` on every frame. This visibly turned
Salmon away from the player. Boss artwork now faces the player's live position;
with the normal player-left/boss-right arena layout, Salmon, Moose, Eagle, and
Polar Bear retain their authored left-facing orientation.

Visual composition review measured the Salmon boss at roughly 197-by-81 pixels
against a 105-by-180 runner, making it read like an ordinary fish. Its boss-only
scale increases from 0.58 to 1.05 (roughly 357-by-147 in the audit frame), with
its waterline offset recalibrated. Ordinary salmon enemies remain small.

Future enemy spawns are rejected when authored within 420 pixels of an existing
wildlife spawn in the same stage. This prevents another shared route layer from
silently stacking patrols into one encounter.
