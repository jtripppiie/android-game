# You Rush Alaska 2.1.0 Beta

## Version

```text
versionCode 210
versionName 2.1.0-beta
badge ALASKA PASSPORT v2.1 BETA
apk you-rush-alaska-2.1.0-beta-210-debug.apk
```

## Direction

This release treats feature drops as minor beta releases, not patch releases.
Patch numbers should be reserved for fixes. The 2.1 line is the Expedition
Systems beta: more readable adventure structure, stronger mid-run recovery,
more meaningful progression, and clearer danger communication.

## Six Implemented Improvements

1. Expedition Logs
   - Added persistent Expedition Logs.
   - Strong expedition grades award logs and bonus Trail Tokens.
   - Logs are shown on the main menu and run result panels.

2. Trail Map Pickups
   - Added a new MAP pickup.
   - Maps reward score, combo, Aurora meter, and route awareness.
   - Maps contribute to expedition grade.

3. Rescue Kit Pickups
   - Added a new KIT pickup.
   - Kits restore a lost life or provide a shield if already healthy.
   - Kits contribute to expedition grade.

4. Hazard Forecast Warnings
   - Dangerous hazards now show short forecast callouts.
   - Forecasts cover bears, polar bears, eagles, avalanche, and thin ice.
   - Warnings make hard hazards feel intentional instead of unfair.

5. Boss Phase-Two Escalation
   - Bosses now announce phase two at half health.
   - Phase two adds screen shake, particles, and a clear callout.
   - This makes boss fights easier to read and more dramatic.

6. Incremental Release Policy
   - Bumped to 2.1.0-beta instead of 2.0.1-beta for a feature release.
   - Updated README and VERSIONING docs to make this the current build.
   - Future feature drops should use 2.2.0, 2.3.0, etc.; patch versions should
     be reserved for bug fixes.

## Verification

Run before shipping:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Expected APK path:

```text
app/build/outputs/apk/debug/you-rush-alaska-2.1.0-beta-210-debug.apk
```
