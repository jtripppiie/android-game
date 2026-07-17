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

Status: compiled and package-inspected on July 16, 2026 after the owner
explicitly authorized compilation. Physical-device visual/gameplay acceptance
is still pending; do not describe this candidate as accepted.

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

Launch presentation follow-up: the project previously configured no Godot boot
splash, so Android displayed a small generated icon based on the crude
stick-figure mountain SVG. The source now has a full-size 1280-by-720 boot image
built from the real Dark Winter painting and runner sheet, plus a coordinated
aurora/mountain compass app icon. The splash remains source-only until the owner
requests compilation.

Scenery consistency follow-up: the stage builder previously placed painted PNG
mountains at z=-20 and then drew twelve flat `Polygon2D` triangle mountains at
z=-8. The incompatible layers visibly mixed painted and SVG-like scenery.
Procedural triangle mountains are removed. All five stages now reuse one of the
two painted Alaska panoramas with stage-specific winter tinting.

Dark Winter previously spawned an ordinary eagle and later an Eagle boss from
the same visually ambiguous wing sheet. The ordinary eagle is removed so the
boss is the level's only eagle. Combined with single-world ownership, the source
can no longer display two eagle actors in that stage.

Run-flow follow-up: the HUD previously exposed a `MAP` button that immediately
abandoned the run. It is now `PAUSE`. A compact 336-by-150 panel offers `RESUME`
and the deliberate `EXIT TO MAP` action; gameplay remains visible underneath
and the world cannot change from an accidental single tap.

HUD follow-up: three labels previously occupied x=24..464, x=470..1040, and a
button at x=1064 with clipping disabled, so long score/pickup/boss strings drew
over one another. The HUD now has fixed non-overlapping columns (20..520,
530..1050, 1070..1260), clipping enabled, smaller bounded typography, a
high-contrast top bar, shorter event copy, and a visible Aurora count.

## Build and verification record

- Godot engine/template: 4.7.1 stable
- Android package: `com.jtripppiie.mooserush`
- Version: code 531, name 5.3.1
- Minimum/target SDK: 24/36
- Architecture: ARM64
- Touch audit: passed
- Stage lifecycle audit: passed
- Autoplay traversal: all five stages passed
- Traversal times: 23.38, 16.55, 17.58, 43.18, and 20.08 seconds
- APK signature: v2 and v3 verified
- Update certificate SHA-256:
  `2ced30b68157ef4da0f723a36f2024fd281f34b935d0fb1b5f6bf2ddaf4d3615`
- APK SHA-256:
  `560048bde263fd247df59a25d9c9e24f046e2bf070609d31c64306d9e56dfd22`

Dark Winter’s 43.18-second automated traversal is substantially slower than
the other stages and remains a pacing-review target even though it completed.

An Android Studio Pixel 5 AVD was found, but this WSL session has no `/dev/kvm`.
The software-emulation fallback did not finish booting, so no claim of emulator
or physical-device visual playtesting is made in this record.

Post-build splash correction: the first 5.3.1 candidate exposed that Godot's
engine boot splash disappears as soon as loading completes—about one second on
the test phone. The source now hands that image directly to a real in-game
launch state. It remains for four seconds total, becomes skippable after 1.25
seconds, displays `TAP TO BEGIN`, and fades into the menu over 0.35 seconds.
Command-line audits bypass the launch state. This correction is source-only and
must not be described as compiled until a later build is explicitly requested.
