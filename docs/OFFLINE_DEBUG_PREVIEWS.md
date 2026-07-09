# Offline Debug Previews

Use the local HTML tools when you need to debug visuals without internet access,
without Android Studio, and without reinstalling the APK after every tiny change.

## Start Here

Open this file in a browser:

```text
tools/index.html
```

The tools load images directly from `app/src/main/res/`, so they reflect the
local checkout. They are not packaged into the Android app.

## Best Tools By Job

- `tools/offline-debug-workbench.html`: sprite crops, alpha bounds, connected
  components, runner foot/contact lines, runtime strips, and gameplay
  composition snapshots.
- `tools/sprite-sheet-audit.html`: detailed frame-by-frame sheet inspection.
- `tools/gameplay-preview.html`: HUD, overlays, controls, hitboxes, contrast,
  device presets, and layout grid.
- `tools/gear-obstacle-preview.html`: obstacle readability, log identity,
  snowball/bear-spray readability, and hitbox checks.
- `tools/laser-eyes-preview.html`: polar bear beam origin and warning reticle.
- `tools/menu-preview.html`: menu layout and first-screen spacing.

## Sprite Debug Flow

1. Open `tools/offline-debug-workbench.html`.
2. Set **Preview mode** to `Sheet audit`.
3. Choose the sprite sheet in **Asset**.
4. Turn on:
   - Checkerboard
   - Raw frame boxes
   - App source crop
   - Alpha visible bounds
   - Connected components
5. Check the report for `appCut L/R/B`.
6. Switch to `Runner ground/contact test` for player feet or boss grounding.
7. Use **Download PNG Snapshot** when a visual needs to be reported.

## What The Overlay Colors Mean

- Yellow: raw source frame cell.
- Green: source rectangle the app should draw.
- Blue: visible alpha bounds.
- Red: connected alpha components.
- Cream: ground/contact line.

## Current Verification Images

Generated proof images live in `verification/`:

- `sprite-running-default-body.png`
- `sprite-running-mom.png`
- `sprite-running-dad.png`
- `sprite-running-eagle.png`
- `sprite-running-polar-bear.png`

These are snapshots, not source assets. Regenerate or replace them when sprite
source images change.

## Before Compiling

Use these checks before building a new APK:

- The default red runner feet are fully visible in every running frame.
- Mom/dad runner sheets do not show stray source-frame slivers.
- Eagle frames do not show beak or wing pieces from adjacent frames.
- Polar bear frames do not show adjacent-frame body fragments.
- Logs read as logs in `gear-obstacle-preview.html`.
- Ground/contact lines match the intended gameplay lane.

Then run:

```bash
./gradlew testDebugUnitTest assembleDebug
```
