# Release 2.7.3 Beta - Adaptive Splash Title

Package metadata:

```text
versionCode: 273
versionName: 2.7.3-beta
build badge: ALASKA PASSPORT v2.7.3 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-2.7.3-beta-273-debug.apk
```

## What changed

- Narrow splash screens now stack `YOU` above `RUSH`, eliminating horizontal crowding entirely.
- Wide splash screens now use an even larger horizontal word gap.
- The title panel adapts between stacked and wide layouts while preserving divider styling.
- The subtitle moves down only when needed so it does not collide with the stacked title.

## QA focus

- Confirm portrait phones show `YOU` and `RUSH` as clearly separated stacked title lines.
- Confirm landscape/wide screens keep a large horizontal gap.
- Confirm subtitle and tap text remain readable below the title.
