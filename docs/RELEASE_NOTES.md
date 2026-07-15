# Release Notes

Current release details are tracked in `docs/VERSIONING.md` and the root
`README.md`.

## Latest Beta

```text
3.16.0-beta
versionCode 384
ALASKA PASSPORT v3.84 BETA
```

Reactive-river highlights:

- Frozen-water bridges retain ownership of their source river.
- Stomps, snowballs, committed wildlife and boss lasers can reopen the current
  when they destroy that bridge.
- Reopened water immediately restores its visible current and lethal collision,
  with explicit `RIVER REOPENED` and route-clear feedback.
- The player can freeze the same water again, creating a repeatable tactical
  terrain loop instead of a permanent one-time safety conversion.
- Godot `FreezableWater` now disables itself while its `ReactiveIce` bridge is
  active and restores itself from the bridge's `shattered` signal.

See [the complete 3.16 release note](RELEASE_3_16_BETA_REACTIVE_RIVER.md).

## Previous Beta

```text
3.15.0-beta
versionCode 383
ALASKA PASSPORT v3.83 BETA
```

System-integrity highlights:

- Unified platform landing and standing contact geometry, eliminating unstable
  grounded state on fixed and moving route platforms.
- Made FLOW snowballs reflect the actual visible laser segment, including both
  Midnight Sun beams, rather than an invisible mixed-coordinate target.
- Limited laser terrain damage to brittle platforms intersected by the beam.
- Bound gates, route geometry, rewards, and wildlife to the same encounter card
  until its hazard wave is consumed; route geometry spawns once per encounter.
- Made the Godot encounter sequence construct physical routes, rewards, and
  enemy pressure instead of changing only a HUD label.
- Expanded Godot validation to require all new scripts and assets, inspect RGBA
  output and integration markers, and run a headless engine parse when Godot is
  available.

See [the complete 3.15 release note](RELEASE_3_15_BETA_SYSTEM_INTEGRITY.md).

## Previous Beta

```text
3.14.0-beta
versionCode 382
ALASKA PASSPORT v3.82 BETA
```

Visual gameplay highlights:

- Added distinct production sprites for snow routes and mechanical moving
  platforms, eliminating the generic shared route appearance.
- Replaced procedural water shapes with a readable glacial-current surface that
  retains the existing freeze-or-clear interaction.
- Added a rotating red laser/ice impact effect with a precise stable collision
  core at the beam endpoint.
- Added the mechanical moving-platform art to the Godot slice without changing
  its collision or movement contract.
- Preserved generated-asset prompts and implementation details in
  [the complete 3.14 release note](RELEASE_3_14_BETA_VISUAL_GAMEPLAY_PASS.md).

## Previous Beta

```text
3.6.0-beta
versionCode 374
ALASKA PASSPORT v3.74 BETA
```

Game-feel + Climax highlights:

- **The "Juice" Pass**: Added several classic game-feel techniques to increase
  responsiveness and impact.
  - **Hit Freeze**: The world now briefly freezes for a few frames whenever the
    player is hit or shatters a boss projectile, adding weight to collisions.
  - **Procedural Squash and Stretch**: The runner's body now dynamically
    stretches when jumping/falling and squashes upon landing, making movement feel
    more springy and grounded.
  - **Visual Near-Miss Feedback**: Narrowly dodging a hazard now triggers a
    subtle teal energy flash around the screen border for instant skill
    confirmation.
  - **Camera Juice**: Added subtle camera leaning that tilts the view into the
    direction of horizontal movement, grounding the character in the world.
  - **Flow Mode Motion Blur**: FLOW now features horizontal speed streaks trailing
    the runner to intensify the sense of momentum.
- **Finished the Runner Roster**: Fully integrated the preset hero body styles.
  Players can now cycle between the Photo Runner, Female Runner (Mom), Male
  Runner (Dad), and Trail Runner 2.0 in the Customize menu.
- **Final Boss Climax (Bear Country)**: Added a multi-phase "Final Stand" to the
  Polar Bear boss.
  - **Blizzard Phase**: Heavy white-out blizzard effect activates at low health,
    dramatically reducing visibility for a final challenge.
  - **Ice Spike Summon**: The boss now calls down falling icicles that track the
    player, creating a bullet-hell style overhead threat.
- **Refined Energy Lasers**: Replaced the "blocky" Midnight Sun lasers with
  concentrated, multi-layered energy beams. They now feature an additive glow, a
  hot core, and twin-eye emitters with energy flickering.
- **Alaska Passport Viewer**: Added a dedicated screen to view earned Trail
  Passport badges, accessible via the map. This completes the local collection
  loop for the Alaska region.
- **Global Bridge**: Added the "ENTER GLOBAL EXPEDITION" gateway on the map.
  Clearing Alaska now unlocks a persistent transition flag, signaling readiness
  for the upcoming Godot-powered Global Overhaul.
- **Refined Asset Rendering**: Enabled bilinear filtering and stabilized frame-
  scaling in the SpriteRenderer to remove horizontal jitter and pixelation
  artifacts.

## Previous Beta

```text
3.4.2-beta
versionCode 372
ALASKA PASSPORT v3.72 BETA
```

Gameplay fairness highlights:

- Fixed the river-log hitbox on the first two stages. The log is drawn as a low
  obstacle, but its collision box previously used the full spawn height, so the
  runner bonked an invisible wall well above the visible log. The collider (and
  the clean-vault reward window) now follow the drawn log, so what you see is
  what you jump.
- Aligned obstacles to a single ground level. River logs now sit their base
  exactly on the ground line, matching the icebergs, snowbanks, antler
  barricades, and grounded wildlife instead of floating slightly above it.

## Previous Beta

```text
3.4.1-beta
versionCode 371
ALASKA PASSPORT v3.71 BETA
```

Launch-polish highlights:

- Fixed full-body runner clipping. The female, male, and overhaul runner sheets
  now use cell-content crops that keep detached limbs (a lifted foot, an
  outstretched hand) instead of connected-component crops that shaved them off.
- Grounded all charging wildlife. Bears, polar bears, moose, wolves, and the
  default ground creature are now anchored by their feet to the ground line, so
  no size/tuning mismatch can leave them hovering. Wolf pounces and flying
  eagles/salmon keep their intended airborne motion; collision is unchanged.
- Made the touch D-pad responsive. Reduced its dead zones, enlarged the pad, and
  added a forgiving drag-capture margin so a finger that drifts just outside the
  pad keeps steering. Horizontal runner speed was raised so lane changes feel
  immediate.
- Reworked the Midnight Sun eye beam. Replaced the noisy red-plus-cyan gradient
  with a cohesive warm energy ray (additive halo, fading red-orange body, and a
  flickering white-hot core) plus a charging orb emitter at the eye.

## Previous Beta

```text
3.4.0-beta
versionCode 370
ALASKA PASSPORT v3.70 BETA
```

Highlights:

- Added a separate Godot action-platformer vertical slice with a handcrafted
  Chugach rescue level, momentum movement, slopes, platforms, enemies,
  snowballs, collectibles, a required key, checkpoint, camera, and goal beacon.
- Deepened the slice with trail dash, aerial stomp, landing feedback, contact
  damage, knockback, invulnerability, combat combos, moving platforms,
  aggressive wolf/bear behaviors, and two required survivor rescues.
- Added explicit idle, run, sprint, crouch, jump, and fall animation states for
  the next-generation runner controller.
- Generated and integrated a versioned transparent six-frame `TRAIL RUNNER 2.0`
  candidate without removing the existing photo, female, or male runners.
- Reduced the shipping Android D-pad from 112dp to 88dp and substantially
  lowered its background, outline, arrow, and knob opacity so chase threats stay
  visible behind the control surface.
- Extended bear spray from 142dp to 210dp, widened its far cone, and expanded
  rear chase-bear interruption distance while keeping visuals and collision in
  the same tuning contract.
- Added a smaller translucent six-action touch layout to the Godot slice.
- Added a default-on numbered 15 px platformer-overhaul preview covering camera,
  terrain, runner scale, control footprint, and chase-bear visibility.
- Documented the engine-migration boundary and added deterministic project and
  sprite validation for the Godot prototype.
- Replaced the flat obstacle stream with a repeating encounter director:
  Launch, Precision, Wildlife Rush, and Jackpot Line beats now alter reward
  trails and obstacle/hazard cadence.
- Turned FLOW into a real high-risk momentum state. It now speeds up the world,
  movement, firing, and encounter density while empowering snowballs and
  preserving its score/pickup bonuses.
- Added skill-based FLOW extensions for close wildlife near misses.
- Added curved multi-star reward trails with beat-specific counts, making jump
  height and path choice more readable and satisfying.
- Added hold-to-fire snowballs, with a faster repeat rate during FLOW, so combat
  feels continuous instead of requiring repetitive taps.
- Brought wildlife into the run after the first gate instead of delaying stage
  identity behind a long warm-up.
- Updated the gameplay calibration preview and HUD to expose encounter names
  and FLOW state while preserving its default-on numbered 15 px grid.
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
