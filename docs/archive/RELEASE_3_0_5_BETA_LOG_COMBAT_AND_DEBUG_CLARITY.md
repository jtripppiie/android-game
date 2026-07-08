# Release 3.0.5 Beta - Log Combat and Debug Clarity

Five-item polish pass for Salmon Rush combat feel and phone-test reporting.

## Version

versionCode: 305
versionName: 3.0.5-beta
build badge: ALASKA PASSPORT v3.0.5 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.5-beta-305-debug.apk

## What changed

1. Snowballs now consider nearby river logs as throw targets, so shots naturally arc toward destructible logs.
2. River log snowball collision is more forgiving.
3. River logs now draw a small FIRE target mark so the blast mechanic is visible in the world.
4. River log destruction has stronger score, particles, shake, flash, and run-callout feedback.
5. Debug badges now label Salmon Rush gates as `LOG FIRE` and thrown snowballs as `SNOW` or `POWER`.

## QA focus

- Start Salmon Rush with DEBUG on.
- Confirm log gates show `G# LOG FIRE`.
- Fire at logs and confirm shots arc toward them and destroy them without pixel-perfect aim.
- Confirm normal snowballs show `T# SNOW` and powered snowballs show `T# POWER`.
- Confirm boss projectile tells still fit in the top callout.
