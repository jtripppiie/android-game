# Release 3.0.4 Beta - Sprite Cleanup

Four-item sprite-focused polish pass.

## Version

versionCode: 304
versionName: 3.0.4-beta
build badge: ALASKA PASSPORT v3.0.4 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.4-beta-304-debug.apk

## What changed

1. Tightened shared wildlife/boss sprite-sheet guard and trim values to reduce frame-edge artifacts.
2. Tightened player runner sprite-sheet trim values so the player body gets the same artifact protection.
3. Increased roaring bear source inset again to crop more original-image edge/foot residue.
4. Added debug sprite details to numbered badges, so wildlife and bosses can be reported with creature and frame, such as `H2 BEAR f3`.

## QA focus

- Turn DEBUG on and report any artifact as `H number + detail`, for example `H2 BEAR f3`.
- Check bear and polar roaring poses for leftover source-image feet or border scraps.
- Check player runner frames for stray original-frame pixels around the body.
- Check salmon, eagle, moose, wolf, bear, and polar bear boss/hazard sprites for over-cropping.
