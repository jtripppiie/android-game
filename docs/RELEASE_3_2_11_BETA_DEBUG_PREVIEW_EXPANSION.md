# Release 3.31 Beta - Debug Preview Expansion

This pass makes the existing game systems easier to inspect, tune, and report
against before rebuilding the Android app.

## Package

```text
versionCode: 331
versionName: 3.2.11-beta
build badge: ALASKA PASSPORT v3.31 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.11-beta-331-debug.apk
```

## What changed

1. Added `tools/gear-obstacle-preview.html` for logs, snow piles, icebergs,
   snowballs, bear spray, hitboxes, numbered badges, and grid coordinates.
2. Added `tools/sprite-sheet-audit.html` for inspecting actual PNG sprite
   sheets frame by frame with checkerboard transparency and trim boxes.
3. Updated `tools/debug-tuning-dashboard.html` with links to every preview
   screen and the current 3.31 report language.
4. Updated README preview documentation so the debug workflow is discoverable.
5. Bumped Android metadata to `3.2.11-beta` / `versionCode 331`.

## Confirmed existing systems

- Bear spray is already in-game as `SPRAY` pickups, charges, hold-FIRE cone,
  wildlife stun, and close boss-lunge interrupt.
- Salmon Rush river logs are already sprite-backed and destructible with
  snowballs.
- Dark Winter icebergs and Bear Country snow piles are already wired through
  vector obstacle sprites.

## QA focus

- Open `tools/gear-obstacle-preview.html` and confirm reports can reference
  numbered objects such as `1G`, `3T`, or `8U`.
- Open `tools/sprite-sheet-audit.html` and confirm sprite artifact reports can
  reference frame numbers such as `F3` or `F6`.
- Install the APK and confirm the visible version badge reads
  `ALASKA PASSPORT v3.31 BETA`.
