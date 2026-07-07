# Release 2.8 Beta - Movement and Obstacle Contract

Package metadata:

```text
versionCode: 280
versionName: 2.8.0-beta
build badge: ALASKA PASSPORT v2.8 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-2.8.0-beta-280-debug.apk
```

## What changed

- Replaced Salmon Rush `FISH RACKS` with `RIVER LOGS`.
  - Salmon remain the wildlife hazard and boss theme.
  - The actual jump obstacle is now a trail/river object that makes physical sense to vault.
- Retuned jumping to feel more grounded.
  - Lowered ground and double-jump launch velocity.
  - Increased gravity for a shorter, snappier arc.
- Tightened animal sprite edge protection.
  - Increased sprite-sheet trim guard/inset values.
  - Increased roaring bear sprite source inset.

## QA focus

- Salmon Rush should read as “jump logs, avoid/stun salmon,” not “jump fish racks.”
- Ground jumps and double-jumps should feel quicker and less floaty.
- Bear, wolf, salmon, moose, eagle, and roaring bear sprites should show fewer edge artifacts.
