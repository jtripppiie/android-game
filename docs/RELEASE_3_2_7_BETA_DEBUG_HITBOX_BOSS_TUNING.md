# Release 3.27 Beta - Debug Hitboxes and Boss Tuning

Four-item polish pass focused on making the next gameplay QA round easier and
less guessy.

## Package

```text
versionCode: 327
versionName: 3.2.7-beta
build badge: ALASKA PASSPORT v3.27 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.7-beta-327-debug.apk
```

## What changed

- Added translucent debug hitboxes for the player, gates, wildlife, pickups,
  throws, boss attacks, and boss contact zones.
- Added `BossTuning` as a pure Java home for boss survival timing, phase speed,
  tell/attack/recover windows, and phase-two pattern selection.
- Moved gate collision radius/insets into `CollisionTuning` and reused the same
  gate hit rectangle for runtime collision and debug drawing.
- Improved DEBUG sprite labels so reports can distinguish sprite-sheet frames,
  roar PNG sprites, and fallback drawn hazards. Trimmed sprite-sheet frames show
  a `T` marker.

## QA focus

- Turn DEBUG on, screenshot a weird object, and use its number plus hitbox shape
  to report the issue.
- Confirm translucent hitboxes are useful but not visually overwhelming.
- Confirm boss tells, attacks, recovery, phase two, and enrage still feel
  readable after the tuning extraction.
- Confirm sprite labels make artifact reports specific enough to identify the
  render path.
