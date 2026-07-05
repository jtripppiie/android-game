# You Rush

A personalized Android arcade game by **TripperDeeLabs**.

**Current status: Alaska 1.8.5 asset cleanup alpha.**

This build is beta-ready for APK testing, tuning, and bug fixing. It is not final 1.0 or a larger milestone until it installs and plays well across all Alaska stages on a real phone.

## Current beta

```text
versionCode: 185
versionName: 1.8.5-alpha
build badge: ALASKA ART v1.8.5
```

## Game flow

1. Splash screen
2. Main menu
3. Alaska map
4. Customization / photo personalization
5. Main arcade run with a single unified HUD
6. Boss phase
7. Stage clear or retry
8. Next stage unlock / score chase

## Controls

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: jump from the ground; tap again in the air for a double jump
- **FIRE**: launch a snowball

## Gameplay style

You Rush is a side-scrolling platform runner (Mario-style), not a flap-to-fly
game. Your character runs along the ground under gravity. Antler hurdles rise
from the ground and hazards fly in from the right — jump (or double jump) to
clear them. The ground is safe to land on; only hitting a hurdle, a hazard, or
the boss ends the run.


## Contra code

```text
UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT B A START
```

Mobile mapping:

- `UP` = tap upper screen
- `DOWN` = tap lower middle screen
- `LEFT` = left control
- `RIGHT` = right control
- `B` = throw
- `A` = jump
- `START` = pause

## Beta systems

- Splash and menu flow
- Alaska map
- Photo-personalized player sprite with outfit color selection
- Local saved progress
- Five Alaska stages
- Unified run HUD
- Hurdle progress bar
- Collectible star paths
- Parallax Alaska scenery
- Three lives per run
- Checkpoint respawn after passed hurdles
- Snowball interactions
- Combo counter
- Persistent XP and local levels
- SoundPool generated SFX with persisted mute toggle
- Debug overlay (off by default; toggle from the menu)
- Version badge
- Build-log artifact workflow
- Safer launcher vector paths
- Hardened app manifest

## Runtime architecture

`MainActivity` loads one game view:

```java
new MooseRushView(this)
```

The previous subclass stack was removed in 1.3.2. Gameplay state now flows
through `GameState`, platformer constants live in `RunnerTuning`, and the HUD is
drawn once by `MooseRushView`.

## Build

Open the repo in Android Studio and run the `app` configuration on a device or emulator.

From the command line, use the committed Gradle wrapper:

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run JVM unit tests (GameMath, LevelCurve)
./gradlew lintDebug            # run Android lint
```

The debug APK lands in `app/build/outputs/apk/debug/` with the version in the
filename, for example:

```text
you-rush-alaska-1.8.5-alpha-185-debug.apk
```

GitHub Actions also builds a debug APK using:

```text
.github/workflows/android-debug-apk.yml
```

Artifacts:

- `you-rush-alaska-debug-apk`
- `you-rush-alaska-build-logs`

## Tests

Pure game math lives in dependency-free classes so it can be covered by fast JVM
unit tests:

- `GameMath` — clamp and circle/rect collision helpers used by the game loop.
- `LevelCurve` — XP thresholds and level/progress math used by the level HUD.
- `RunnerTuning` — coyote time, jump buffer, and spawn-spacing fairness floors.

Run them with `./gradlew testDebugUnitTest`.

## App icon

The launcher uses an adaptive icon (`res/mipmap-anydpi-v26/ic_launcher.xml`) with
separate background, foreground, and monochrome (themed-icon) layers, plus a
full-bleed vector fallback in `res/mipmap-anydpi/` for API 23–25.


## Beta test checklist

1. Confirm GitHub Actions builds the APK.
2. Install the APK on a real Android device.
3. Verify photo picker and photo restore.
4. Verify all five Alaska stages.
5. Verify movement, jump, double jump, fire, lives, checkpoint respawn, unified HUD, combo, XP, all bosses, and mute toggle.
6. Tune movement, spacing, scoring, spawn rates, stage difficulty, and HUD overlap.
7. Confirm no crash in a 15-minute phone test.
8. Keep 1.3.x focused on stability, testability, balancing, audio assets, and cleaner game-state integration.
