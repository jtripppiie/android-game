# 5.3.2 Deep Source Audit

Status: source validated; compilation and device acceptance pending.

This revision exists because reaching the finish line was not enough evidence
that the game was correct. The audit reviewed startup, screen transitions,
touch input, pause and notes, respawning, wildlife, boss combat, scoring,
objectives, stage geometry, persistence, and Android-facing flow.

## Corrected defects

- A completed stage called `GameSession.complete_stage` inside the world and
  again in the main scene, doubling lifetime score. The main scene is now the
  single completion owner.
- UI screens were only queued for deletion, allowing an old screen to remain
  visible and interactive for the rest of the frame. Old UI is detached before
  the next screen is built.
- Respawning left dash, fire, jump-buffer, combo-timer, and air-jump state
  partially active. Recovery now resets the complete transient movement state.
- Every boss used the same 58-pixel circle regardless of its visible animal.
  Variant hitboxes now follow the salmon, moose, eagle, polar bear, and sun
  silhouettes while remaining aligned with the runner's snowball lane.
- Boss damage depended on a one-time body-entered event. Overlap during the
  attack state is now checked continuously, respecting player invulnerability.
- Boss projectiles could remain lethal after victory. They are removed when
  the boss is defeated.
- Boss health was unnecessarily long at 7/8/9/10/12; it is now 6/6/7/7/9.
- Ground wildlife could patrol off platform edges and disappear. Bears and
  wolves now use forward ledge detection.
- Pickup callbacks had no transaction guard. A pickup can now be awarded only
  once even if multiple physics notifications arrive before deletion.
- Every level-clear message incorrectly said `CHUGACH`. It now says
  `EXPEDITION COMPLETE`.
- High Contrast changed a color hidden behind opaque artwork. It now changes
  the visible painted backdrop.
- Android Back/Escape now controls the compact pause state. Review notes retain
  the pause state that existed before opening, and cannot be opened on top of
  the pause menu.
- The unavailable microphone action is disabled instead of appearing broken.
- Touch controls use smaller footprints, readable directional words, and lower
  idle opacity so they obscure less of the playfield.
- Review identifiers now cover slopes, checkpoints, the finish beacon,
  breakable ice, and previously unregistered route water. Long platforms use
  nearest-surface distance when selecting a note target.
- Aurora rings now increment the visible Aurora count in addition to their
  score and temporary movement surge.
- Midnight Sun and Bear Country now contain reachable supply caches instead of
  carrying an unused supply-block system.
- Profile loading clamps corrupt negative totals/scores, invalid completion
  indexes are rejected, and save failures are reported.
- The Android bridge checks signals and methods before calling them, preventing
  a stale native plugin from crashing startup.
- Android backgrounding leaves an active run safely paused. Back/Escape now
  returns submenus to the main menu and exits normally from the main menu.
- Reduced Motion no longer silently disables vibration while the independent
  Haptics setting remains enabled.
- Wildlife chase logic is now bounded by its authored patrol distance; salmon,
  wolves, and bears can no longer keep reacquiring the runner and drift through
  the rest of a level. Ground patrols are clamped at their route edge.
- Boss lunges are reach-limited on both sides, and spawned hazards travel
  toward the runner even after the runner crosses behind the boss.
- Moving platforms preserve their authored origin-to-destination paths but
  initialize at the midpoint before the first rendered frame, removing the
  visible frame-one teleport without relocating the route.
- Supply caches were moved onto the runner's actual snowball lane and their
  reachability is now covered by the geometry audit.
- The unused random encounter card/director implementation was removed from
  the active game scripts. Authored stages remain the sole geometry and enemy
  source, preventing accidental reintroduction of stacked encounters.
- The source version is 5.3.2/code 532 so a later corrected build is not
  confused with the already-compiled 5.3.1 candidate.

## Verification

- Godot 4.7.1 parser/import: passed.
- Touch multi-input audit: passed.
- Single-world lifecycle audit: passed.
- Score ownership/immediate UI replacement/screen-flow audit: passed.
- Five geometry audits: passed.
- Five automated objective/boss/finish traversals: passed.

Measured main-route geometry:

| Stage | Surfaces | Maximum gap | Maximum rise |
|---|---:|---:|---:|
| Midnight Sun | 7 | 120 px | 90 px |
| Salmon Rush | 7 | 180 px | 80 px |
| Moose Pass | 9 | 110 px | 120 px |
| Dark Winter | 8 | 170 px | 140 px |
| Bear Country | 7 | 180 px | 180 px |

Final automated traversal times were 30.13, 17.05, 16.75, 43.29, and 33.48
seconds. Dark Winter remains the slowest pacing target. Automated traversal is
regression evidence, not a substitute for physical-device visual playtesting.

No 5.3.2 APK was compiled during this pass.
