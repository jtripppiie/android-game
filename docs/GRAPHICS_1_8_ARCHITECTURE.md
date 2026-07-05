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
- `SpriteRenderer.java`: new player renderer for standing previews, running
  frames, default heads, uploaded face heads, limbs, boots, and outfit color.
- `VisualEffects.java`: new effects manager for particles and score popups.
- `MooseRushView.java`: reduced direct graphics ownership by delegating player
  drawing, drawable lookup, particle updates, and popup drawing to dedicated
  classes.
- `app/build.gradle`: bumped the debug package to `1.8.0-alpha` /
  `versionCode 180` and updated the visible build badge.
- `README.md`, `docs/VERSIONING.md`, and `docs/ANDROID_TEST_CHECKLIST.md`:
  updated package names, badge text, and test instructions for the 1.8.0
  architecture alpha.

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
versionCode: 180
versionName: 1.8.0-alpha
badge: ALASKA ARCH v1.8.0
```

This branch is an architecture alpha. It is intended for testing the new
graphics structure before calling the graphics work beta-ready.

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
