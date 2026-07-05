# Graphics 1.8.0 Architecture

This branch begins the move from patch-style Canvas drawing toward a proper
graphics architecture.

## Why this exists

The previous 1.7.5 work improved isolated visuals, but it still kept too much
inside `MooseRushView`. That made every graphics fix risky:

- Player animation was mixed into the main game loop.
- Photo uploads were rendered like pasted screenshots instead of character heads.
- Animal hazards depended on weak placeholder vectors.
- Background scenery shared the short ground scroll loop, causing visible jumps.
- Effects, HUD, gameplay, input, collision, and asset loading all lived in one
  oversized view class.

This is not a full engine migration yet. It is the first architecture pass that
creates clear ownership boundaries for sprites, assets, animation, and effects
while keeping the Android app buildable.

## Current renderer choice

The app still uses Android `Canvas` as the host renderer.

That is intentional for this phase:

- It avoids a half-migrated LibGDX/Unity-style rewrite.
- It keeps the APK, activity, input, audio, and Android photo picker stable.
- It lets us extract render systems first, then decide whether a full engine is
  worth the added dependency and migration cost.

The target architecture is engine-ready: sprite rendering, asset loading, and
effects should not remain tangled into the main view.

## Changes in this pass

### File-level change log

- `GameAssets.java`: new asset registry for backgrounds, stage hazards, and
  boss lookup.
- `SpriteRenderer.java`: player renderer for standing previews, running
  frames, default heads, uploaded face heads, limbs, boots, outfit color,
  runner sheet trimming, and readable runner animation cadence.
- `VisualEffects.java`: new effects manager for particles and score popups.
- `ArcadeScoring.java`: pure scoring helper for combo multipliers and
  stage-clear bonuses.
- `SpriteSheetMath.java`: pure sprite-sheet source-rect helper for frame-edge
  guard pixels.
- `MooseRushView.java`: reduced direct graphics ownership by delegating player
  drawing, drawable lookup, particle updates, and popup drawing to dedicated
  classes.
- `app/build.gradle`: tracks the current `1.8.x` graphics alpha package and
  visible build badge.
- `README.md`, `docs/VERSIONING.md`, and `docs/ANDROID_TEST_CHECKLIST.md`:
  updated package names, badge text, and test instructions for the current
  1.8.x graphics alpha.

### Sprite rendering

Added `SpriteRenderer`.

Responsibilities:

- Draw standing preview avatars.
- Draw running player frames.
- Compose uploaded face crops into a character head.
- Own limb/head/body drawing details instead of placing them in `MooseRushView`.

`MooseRushView` now passes a `SpriteRenderer.PlayerFrame` into the renderer.

### Asset loading

Added `GameAssets`.

Responsibilities:

- Load background drawables.
- Load hazard/boss drawables.
- Own stage-to-asset lookup.

`MooseRushView` no longer owns direct background/hazard drawable fields.

### Visual effects

Added `VisualEffects`.

Responsibilities:

- Own particle state.
- Own score popup state.
- Update and draw visual effects.
- Provide focused spawn methods for dust, spark bursts, snowball trails, and
  score text.

`MooseRushView` still owns high-level screen shake and flash timers because
those affect the whole world transform, but particle and popup lifecycles are no
longer embedded in the view.

### Photo avatar pipeline

The photo flow now rejects non-face images and crops accepted images around the
detected face before using them as the player head.

Current behavior:

- Decode selected image.
- Detect a usable face with Android `FaceDetector`.
- Reject images with no clear face.
- Crop around the best face.
- Scale the cropped face image down for sprite use.
- Render the face as an oval character head with a helmet/frame treatment.

Limit:

- This is face validation, not full content moderation.

### Animal hazard assets

Replaced the weak moose and bear placeholder drawables with side-profile
silhouettes designed for fast gameplay readability.

Moose target read:

- Long body
- Tall legs
- Long muzzle
- Large palmate antlers

Bear target read:

- Heavy body mass
- Rounded ears
- Long snout
- Broad paws and claws

### Background motion

Separated background scenery scroll from the short ground texture loop.

Before:

- `groundScroll` wrapped every 48dp.
- Trees and mountains reused that counter with larger spacing.
- Larger scenery layers visibly snapped.

Now:

- `groundScroll` handles short ground texture repeats.
- `sceneryScroll` handles long parallax motion.

### Versioning

The active beta package is:

```text
versionCode: 195
versionName: 1.9.5-alpha
badge: ALASKA DAILY v1.9.5
```

This branch is an architecture alpha. It is intended for testing the new
graphics structure before calling the graphics work beta-ready.

`1.8.1-alpha` also includes the follow-up moose and hurdle readability fix:
the moose now uses a wider side-profile silhouette, and the jump gate is a
clean rail obstacle instead of an antler-stump shape.

`1.8.2-alpha` moves the in-game moose hazard and Moose Boss off the rectangular
drawable path and draws the moose directly in Canvas. This prevents the animal
from appearing as a square asset tile.

`1.8.3-alpha` adds generated raster sprite sheets and background/tree art.
Moose, bear, salmon, eagle, and the headless player body now have six-frame
sprite sheets available in Android resources, with hazards and bosses wired to
animate from those sheets.

`1.8.4-alpha` fixes runtime runner scale and cadence so the headless body reads
as a runner under the uploaded/default head.

`1.8.5-alpha` removes the obsolete Alaska SVG placeholders from
`app/src/main/assets/regions/alaska/`.

`1.8.6-alpha` is the launch-polish pass:

- Adds per-frame guard-pixel source rects for wildlife and player sprite sheets
  to prevent adjacent-frame bleed during animation.
- Adds polar bear and wolf sprite sheets for winter-mode variety, plus
  single-frame brown bear and polar bear roar sprites used when bears rear up.
- Removes the vector-looking triangle mountain overlay; the generated
  background plates now own mountain art.
- Adds multiple stable parallax spruce layers, with extra snow-covered trees
  and snow drift mist for winter/dark stages.
- Enlarges and repositions the player, adds a lane guide, and removes tree sway
  that could read as scenery jitter.
- Upgrades the run HUD with score, run score, objective progress, stars, lives,
  combo, multiplier, level, boss warning, and combo callouts.
- Adds combo-based score multipliers, star/combo rewards, and stage-clear bonus
  scoring.
- Adds near-miss scoring and capped progressive difficulty pressure inspired by
  endless-runner pacing: more speed and tighter spawns as the run advances,
  without sudden early spikes.
- Expands ready/game-over/stage-clear screens into briefing and run-report
  panels.
- Makes the debug APK workflow run on `graphics-1.8.0` so pushed builds publish
  APK artifacts for this active graphics branch.

`1.8.7-alpha` is the smooth-retention pass:

- Slows eagle animation on a per-hazard clock so wing flaps read naturally
  instead of speeding up with the world scroll clock.
- Smooths bear/polar bear movement by anchoring hazards to a stable base lane.
- Removes decorative animal halo/circle glows while keeping grounding shadows.
- Tightens runner and wildlife sprite source trims to reduce edge artifacts.
- Moves the playfield ground slightly lower and nudges the top clamp for more
  double-jump headroom without clipping the runner.
- Adds run missions and shield pickups for a clearer one-more-run loop.
- Nudges the progressive difficulty curve upward while preserving fair minimum
  spawn spacing.

`1.9.4-alpha` is the replay-value pass:

- Adds an Aurora Rush meter that fills through skill events and creates a short
  score-burst mode with stronger world feedback.
- Adds persistent Trail Tokens, run reward summaries, and cosmetic-only outfit
  unlocks.
- Shows long-term progress on the main menu and run result panels.
- Keeps monetization hooks cosmetic and progress-based rather than pay-to-win.

`1.9.5-alpha` is the Daily Rush retention pass:

- Adds a main-menu daily challenge button that chooses the rotating unlocked
  daily stage and season automatically.
- Adds once-per-day Daily Rush token rewards, local streak persistence, and
  result-screen status lines.
- Moves daily rotation, gate target, reward, and streak math into
  `RunRewardEconomy` so future region copies can reuse it.

## What still needs extraction

The following still need to move out of `MooseRushView`:

- World/background drawing
- HUD drawing
- Collision-independent render models

## Test focus

On device, verify:

- Customize preview looks like a character, not a pasted screenshot.
- Non-face uploads are rejected.
- Accepted face uploads are cropped to a head-like avatar.
- Moose reads as moose at gameplay size.
- Bear reads as bear at gameplay size.
- Trees no longer snap or jump.
- Hurdles remain readable and jumpable.
- Effects do not obscure hazards.
