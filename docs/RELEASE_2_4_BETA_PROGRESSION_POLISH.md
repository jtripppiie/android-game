# You Rush 2.4.0 Beta Progression Polish

Next improvement pass focused on making the game feel more like a real journey
and less like a free-form test harness.

```text
versionCode: 240
versionName: 2.4.0-beta
build badge: ALASKA PASSPORT v2.4 BETA
APK: app/build/outputs/apk/debug/you-rush-alaska-2.4.0-beta-240-debug.apk
```

## The five changes

1. Real stage progression
   - Locked Alaska map stages can no longer be started early.
   - Locked stages remain visible, but tapping them now shows the stage that
     must be cleared first.

2. Safer respawn feel
   - Losing a life now grants a short visible respawn-grace window.
   - The player gets a gold aura during grace and cannot be immediately hit by
     the same obstacle pileup.

3. Boss pressure clarity
   - Boss health now shows the remaining escape countdown.
   - The hidden boss fail timer is now readable during the fight.

4. Combat-rule clarity
   - Ready screens now state that wildlife should be avoided, FIRE stuns
     animals, and boss recovery is the weak window.

5. Result motivation
   - Game-over and stage-clear panels now call out new best scores.
   - Result screens keep the next-goal guidance from 2.3 and make score-chasing
     more obvious.

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
35a21ba049b44951e970f732236e6975fdd74d511eb06eeda93413fc960102ab
```

Then install the APK on a real Android phone and complete:

```text
docs/ANDROID_TEST_CHECKLIST.md
```
