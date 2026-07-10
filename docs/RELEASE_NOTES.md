# Release Notes

Current release details are tracked in `docs/VERSIONING.md` and the root
`README.md`.

## Latest Beta

```text
3.2.34-beta
versionCode 354
ALASKA PASSPORT v3.54 BETA
```

Highlights:

- Reduced the visible runner scale, accelerated its six-frame running cadence,
  increased lateral response, and expanded coyote/jump-buffer forgiveness.
- Raised the opening Midnight Sun stage pace so the first run reaches its
  intended arcade rhythm sooner.
- Rebuilt the Midnight Sun attack as real twin-eye beams with layered additive
  Android glow, brighter cores, accurate eye emitters, and eclipse-aware draw
  ordering so the attack stays luminous in the game rather than only in HTML.
- Added a dedicated numbered 15 px Midnight Sun laser calibration preview with
  phase, thickness, speed, hitbox, and pause controls.
- Added bounded object pooling for particles and score popups to reduce
  high-frequency allocations and garbage-collection pressure during effects.
- Extracted pure boss transitions into `BossStateMachine` and stage-specific
  attack routing into `StageBossRules`, with focused regression tests.
- Corrected boss HUD labels so laser tells and attacks read `EYE BEAM` and
  `BEAM DODGE` instead of being mislabeled as summons.
- Removed the detached neighboring-frame boot from the default runner by using
  component-aware, seam-guarded runtime crops.
- Rebuilt eye-beam gameplay around one shared diagonal segment for rendering,
  collision, and DEBUG visualization; the attack now locks to its telegraphed
  lane and the Polar Bear emitter follows the rendered laser pose.
- Removed the fake offset duplicate Polar Bear beam so every visible beam comes
  from a real emitter.
- Kept boss-summoned wildlife moving and interactive during boss encounters
  without re-enabling ordinary stage hazard spawning.
- Replaced the obsolete runner-trim test with diagonal laser collision coverage.
- Required runs to be explicitly launched as a Daily Rush before they can
  grant the daily reward or advance the daily streak.
- Set remaining lives to zero on the fatal hit so the game-over HUD and state
  agree.
- Added custom-view click accessibility signaling and regression tests for
  Daily Rush eligibility and terminal-life state.
- Made all four D-pad arrow buttons the same drawn size, replacing uneven font
  arrows with matching circular arrow controls.
- Replaced the Android laser tell rectangle with the HTML preview's thin
  eye-origin beam tell, including the polar-bear twin-beam preview pass.
- Raised Midnight Sun boss health from 9 to 14 so the sun fight takes a more
  deliberate number of snowball hits.
- Ported the `tools/laser-eyes-preview.html` beam treatment into Android:
  active boss lasers now use a thin gradient eye beam, eased visual sweep toward
  the target lane, and a twin-beam polar-bear pass instead of a red bar.
- Lowered Bear Country snow piles again: collision now uses a 14-27dp pile band
  and the visual sprite is capped shorter/wider so it sits closer to runner
  leg height instead of still reading as a mound wall.
- Replaced separate aim buttons with a left-thumb D-pad that handles movement
  and snowball aim together, including diagonal thumb positions.
- Raised Polar Bear Boss health from 6 to 12 so the fight has time to reach and
  showcase its later laser pattern.
- Changed active laser drawing so the beam uses the boss eye as the origin and
  the runner lane only as the aim/collision target, removing the red
  runner-height bar look.
- Raised Midnight Sun boss health from 4 to 9 so it takes more than a few
  normal snowballs to defeat.
- Added a dedicated on-screen SPRAY button and removed the old hold-FIRE spray
  instruction path.
- Added left-thumb D-pad aiming so players can bias snowball throws high, low,
  or diagonally instead of relying only on auto-targeting.
- Increased moose hazard and boss scale so Moose Pass reads as a bigger threat.
- Lowered Dark Winter iceberg gate generation and visual height so icebergs sit
  closer to runner height instead of spike-wall height.
- Removed projectile-style attacks from the Eagle Boss rotation so the eagle no
  longer reads like it is using bad laser attacks.
- Changed left-side bear chases so the bear slows and falls farther behind over
  time, leaving a larger dust trail as the runner escapes.
- Lowered Bear Country snowbank gate generation and visual height so snow piles
  sit closer to runner height instead of reading like tall walls.
- Fixed Midnight Sun laser rendering so the beam draws over the boss face from
  the visible eye origin instead of appearing hidden behind the sun art.
- Added eye charge glows to both Midnight Sun eyes during laser tells/attacks
  so the attack reads like the polar-bear-style eye beam.
- Improved jump feel for log-plus-wolf sequences: the second jump now has more
  lift, normal gravity is slightly softer, and hazards keep extra spawn spacing
  after gates so double-jump clears are fairer.
- Reduced wolf pounce height while the runner is already airborne so a pouncing
  wolf can threaten bad timing without deleting a good double jump.
- Tightened visible runner proportions by slightly reducing the scaled runner
  body and fallback/photo head size.
- Made Computer Run invincible and dry-run only: it ignores damage, retries
  safely, advances stages for demos, and does not persist progress, rewards,
  best scores, unlocks, daily streaks, badges, mission totals, or selected stage.
- Increased the visible runner scale while keeping gameplay collision behavior
  anchored to the existing player radius.
- Added occasional wolf pounces, letting some wolves jump into the runner's
  airborne path instead of always staying as ground-only threats.
- Made the Midnight Sun fight explicit: the first boss is now the named
  Midnight Sun, has face/ray boss art, and uses polar-bear-style laser patterns
  once the fight escalates.
- Added a short eclipse-darkness payoff after defeating the Midnight Sun.
- Kept Midnight Sun running hazards wildlife-based with WOLF instead of a
  second sun/glare object in the path.
- Added Computer Run demo mode from the menu, with autopilot jumping, firing,
  retrying, and advancing through stages for hands-free show-and-tell.
- Reframed the main-menu runner preview so the standing legs stay inside the
  visible landscape preview area instead of clipping off the bottom.
- Reduced and refined the default fallback head proportions so the menu sprite
  reads cleaner and less blocky.
- Updated the menu layout preview to match the Android runner framing and head
  proportions.
- Tightened the default red runner-body atlas crop with the shared seam guard
  and disabled smoothing on that draw path to stop running-frame artifacts from
  neighboring sprite frames.
- Updated the offline sprite audit and debug workbench runtime crop overlays to
  match the Android guarded crop.
- Anchored driftwood and river logs to the ground/waterline so they no longer
  appear to float in runs or the obstacle preview.
- Added the straighter `1.5` degree vector river log to the Android asset and
  mirrored it in the obstacle HTML preview.
- Added a temporary left-side bear chase that speeds up the run, can be escaped
  by clean vaults or survival time, and can be interrupted with rear bear spray.
- Reworked driftwood and river-log obstacle art with organic silhouettes,
  stage-specific coloring, and no baked-in target marker on driftwood.
- Added HUD and result-panel text fitting so dense labels stay inside their
  available space across preview/device sizes.
- Added FLOW gameplay for clean vault streaks, with bonus scoring, pickup pull,
  HUD timer feedback, and run-summary stats.
- Improved runner reset behavior and proportional ground-line placement.
- Hardened runner and wildlife sprite-sheet sampling to reduce adjacent-frame
  bleed and late-frame clipping.
- Added an offline debug workbench and local tools index for sprite crops,
  contact-line checks, gameplay composition, and PNG visual snapshots.
- Refreshed launcher branding across adaptive, monochrome, and legacy icons.
- Expanded the sprite-sheet audit tool with Android runtime crop overlays.
- Preserved detailed historical release notes under `docs/archive/`.

## Historical Releases

Detailed older release notes are archived in `docs/archive/`.
