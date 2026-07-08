# Release 3.0.2 Beta - Obstacle Clarity

Focused phone-test fix for the `1G 2G 3G` obstacle feedback.

## Version

versionCode: 302
versionName: 3.0.2-beta
build badge: ALASKA PASSPORT v3.0.2 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.2-beta-302-debug.apk

## What changed

- Salmon Rush `G` obstacles now render as floating river logs instead of generic gate frames.
- Snowballs can now blast Salmon Rush river logs apart, advancing route progress with score/combo feedback.
- Dark Winter now uses `ICEBERGS` language instead of `ICE MARKERS`.
- Dark Winter obstacle art now draws chunky iceberg shapes instead of thin marker lines.
- Active weather fronts were disabled so rain/snow/aurora overlays no longer reduce gameplay readability.
- The debug info panel is smaller, lighter, and shows one recent event so it does not cover the playfield while numbered object badges are on.

## QA focus

- Turn DEBUG on and start Salmon Rush.
- Confirm `1G`, `2G`, and `3G` look like river logs.
- Fire snowballs into river logs and confirm they break, score, and advance route progress.
- Start Dark Winter and confirm the obstacle label says `ICEBERGS`.
- Confirm no active weather overlay appears during normal gameplay.
- Confirm the debug panel is compact enough for phone testing.
