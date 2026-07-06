# You Rush 2.3.0 Beta Gameplay Polish

Next-five improvement pass focused on making the beta feel clearer and more
intentional without expanding into random new features.

```text
versionCode: 230
versionName: 2.3.0-beta
build badge: ALASKA PASSPORT v2.3 BETA
APK: app/build/outputs/apk/debug/you-rush-alaska-2.3.0-beta-230-debug.apk
```

## The five changes

1. Splash readability
   - Reworked the splash title so `YOU` and `RUSH` are drawn as separate words
     with a stable gap.
   - Split the lab name and beta badge into separate lines for cleaner spacing.

2. Sprite edge cleanup
   - Increased sprite-sheet edge guards and trim insets to reduce neighboring
     frame bleed.
   - Cropped single-frame roaring bear sprites at render time so old border
     pixels are less likely to appear around the animal.

3. Boss movement feel
   - Replaced the boss lunge target with the staged boss attack lane.
   - Added a stronger wind-up offset, smoother return tracking, and longer
     readable tells/recovery windows.

4. Boss fight clarity
   - Added visible boss tell overlays with action hints such as backing up from
     the red zone, jumping ice arcs, and stunning summoned wildlife.
   - Preserved weak-window double damage during recovery and made the timing
     easier to understand.

5. Adventure/reward feedback
   - Added next-goal text to game-over and stage-clear panels.
   - Result screens now point players toward the boss, next stage, S rank,
     perfect clears, badges, or weak-window boss strategy.

## Verification checklist

Completed on July 6, 2026:

```bash
./gradlew testDebugUnitTest  # PASS
./gradlew lintDebug          # PASS
./gradlew assembleDebug      # PASS
```

Debug APK size: 21 MB

Debug APK SHA-256:

```text
14edf04b8f4ee9f9918ddb7aeaee3ec899b3b252aef9236e83c01f9380f84db6
```

Then install the APK on a real Android phone and complete:

```text
docs/ANDROID_TEST_CHECKLIST.md
```
