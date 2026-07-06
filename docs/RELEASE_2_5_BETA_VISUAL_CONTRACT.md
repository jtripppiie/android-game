# You Rush 2.5.0 Beta Visual Contract

Pass focused on the diagnosis that the code had answers the player could not
read clearly enough: what to jump, what to avoid, what to attack, and why some
animation felt too frantic.

```text
versionCode: 250
versionName: 2.5.0-beta
build badge: ALASKA PASSPORT v2.5 BETA
APK: app/build/outputs/apk/debug/you-rush-alaska-2.5.0-beta-250-debug.apk
```

## Changes

1. Stage-specific obstacle identity
   - Replaced generic hurdle/gate language with named obstacle types.
   - Stages now use driftwood rails, fish racks, antler barricades, ice
     markers, and snowbanks.

2. Themed obstacle rendering
   - The obstacle renderer now changes materials and details per stage while
     keeping the existing collision box stable.
   - Obstacles get in-world nameplates so the player can read what they are
     jumping over.

3. Clearer briefing and HUD language
   - Ready-screen chips now name the obstacle instead of saying only `HURDLES`.
   - HUD and result guidance now say `JUMP <obstacle>` instead of generic
     gates/hurdles.
   - Removed stale `2.0 BETA` briefing text in favor of the current build badge.

4. Calmer creature animation
   - Reduced eagle, salmon, wolf, moose, bear, and polar bear animation rates.
   - Boss sprite sheets use calmer rates too, especially eagle wings and salmon
     swimming.

5. Documentation and QA
   - README explains the visual/gameplay contract in player-facing terms.
   - Android QA checklist now tests obstacle identity and calmer animal motion.

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
ff60dac9af717f48313a1cfaaf454f5808c3c3c62b1096dbdce80fae5fe30282
```

Then install the APK on a real Android phone and complete:

```text
docs/ANDROID_TEST_CHECKLIST.md
```
