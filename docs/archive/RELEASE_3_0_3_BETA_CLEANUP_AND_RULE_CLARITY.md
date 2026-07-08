# Release 3.0.3 Beta - Cleanup and Rule Clarity

Focused three-item follow-up after the obstacle clarity pass.

## Version

versionCode: 303
versionName: 3.0.3-beta
build badge: ALASKA PASSPORT v3.0.3 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.3-beta-303-debug.apk

## What changed

1. Removed leftover active-weather plumbing after weather fronts were disabled.
2. Salmon Rush rule text now explicitly says `FIRE blasts logs`, so destructible logs are taught before play.
3. Expedition scoring/results now reward blasted logs instead of the removed weather-front system.

## QA focus

- Confirm no weather front callouts, HUD labels, score credit, or overlays appear.
- Start Salmon Rush and confirm the ready/rule text teaches snowball log blasting.
- Blast at least two river logs and confirm the result screen includes `Logs`.
- Start Dark Winter and confirm the live HUD says `ICEBERGS`.
