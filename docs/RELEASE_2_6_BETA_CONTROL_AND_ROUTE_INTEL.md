# Release 2.6 Beta - Control and Route Intel

Package metadata:

```text
versionCode: 260
versionName: 2.6.0-beta
build badge: ALASKA PASSPORT v2.6 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-2.6.0-beta-260-debug.apk
```

## What changed

1. Added a real in-run pause state.
   - The run can now be paused from the HUD.
   - Pause freezes gameplay, clears held controls, and resumes without inheriting stale input.
   - The pause panel includes Resume, Map, and Sprite actions.

2. Added map route intel.
   - The Alaska Map now summarizes the selected stage's obstacle, wildlife hazard, and boss.
   - This makes each route feel more intentional before launch.

3. Improved stage tactical briefing.
   - Ready screens now state the current stage rule directly: jump the stage obstacle and use FIRE to stun the listed hazard.
   - Launching a stage shows a short stage-specific jump objective callout.

4. Improved live HUD readability.
   - The live objective now uses compact stage labels such as RAILS, RACKS, ANTLERS, ICE, and SNOWBANKS.
   - This reduces top-bar crowding on common phone widths while preserving full names in map/briefing screens.

## QA focus

- Pause during normal running, while charging a jump, and during boss pressure.
- Resume from pause and confirm no stuck left/right/jump/fire input.
- Use Map from pause and start another route.
- Use Sprite from pause and return to gameplay.
- Confirm route intel remains readable in portrait and landscape.
- Confirm HUD objective text does not collide with score, progress, or stage labels.

## Remaining launch risks

- Needs physical-device install testing across several Android screen sizes.
- Debug APK remains signed as a debug build and is not a Play Store release artifact.
