# Release 3.26 Beta - Collision Tuning Cleanup

Focused cleanup release after the project review called out scattered collision
magic numbers in `MooseRushView`.

## Package

```text
versionCode: 326
versionName: 3.2.6-beta
build badge: ALASKA PASSPORT v3.26 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.6-beta-326-debug.apk
```

## What changed

- Added `CollisionTuning` as a pure Java tuning class for gameplay hitbox scale
  values.
- Moved player-vs-boss, player-vs-boss-attack, laser, wildlife, roaring
  wildlife, thin-ice, near-miss, pickup, snowball-vs-projectile, and
  snowball-vs-log hitbox scales out of inline view code.
- Updated `MooseRushView` collision paths to use named collision constants and
  helper methods.
- Added unit tests documenting the intended collision relationships, including
  roaring wildlife body growth, forgiving snowball impacts, and pickup leniency.
- Bumped Android package metadata and docs to `3.2.6-beta` / `326`.

## QA focus

- Confirm wildlife contact still feels fair when bears roar.
- Confirm snowballs still shatter boss ice projectiles and destroy Salmon Rush
  river logs.
- Confirm boss lasers still require jumping instead of feeling like full-body
  invisible walls.
- Confirm stars and powerups remain easy to collect without needing exact
  center overlap.
