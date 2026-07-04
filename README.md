# You Rush

A personalized Android arcade game by **TripperDeeLabs**.

**Current status: Alaska 0.9.5 beta.**

This build is beta-ready for APK testing, tuning, and bug fixing. It is not final 1.0 or 2.0 until it builds, installs, and plays well on a real phone.

## Current beta

```text
versionCode: 24
versionName: 0.9.5-beta
build badge: ALASKA BETA v0.9.5
```

## Game flow

1. Splash screen
2. Main menu
3. Alaska map
4. Customization / photo personalization
5. Stage intro
6. Main arcade run with progress HUD
7. Pause / quick help
8. Collect bonus stars and shields
9. Checkpoint respawn if lives remain
10. Stage challenge phase
11. Stage clear or retry
12. Next stage unlock / score chase

## Controls

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: bounce upward
- **THROW**: launch a snowball
- **CLIMB TREE**: appears only when close enough to the tree
- **PAUSE**: opens quick help and stops the run until resumed

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
- Photo-personalized player character
- Local saved progress
- Five Alaska stages
- Stage intro overlay
- Stage progress HUD
- Gate progress bar
- Run timer
- Stage challenge phase messaging
- Pause and quick-help overlay
- Three lives per run
- Checkpoint respawn after passed gates
- Contra-code unlimited lives
- Bonus star pickups
- Life restore after every third collected star when below normal max
- Aurora shield pickup
- Shield save before a life is spent
- Tree timing mechanic
- Snowball interactions
- Near-miss rewards
- Combo streaks
- Incoming callouts
- Touch ripples
- Haptic feedback where supported
- Debug overlay
- Version badge
- Build-log artifact workflow
- Safer launcher vector paths
- Hardened app manifest

## Active view stack

`MainActivity` currently loads:

```java
new AlaskaProgressMooseRushView(this)
```

That layer inherits:

```text
MooseRushView
JuicyMooseRushView
AlaskaSurvivalMooseRushView
AlaskaNearMissMooseRushView
AlaskaComboMooseRushView
AlaskaStageIntroMooseRushView
AlaskaHazardWarningMooseRushView
AlaskaPauseHelpMooseRushView
AlaskaLivesMooseRushView
AlaskaContraCodeMooseRushView
AlaskaCollectibleMooseRushView
AlaskaShieldMooseRushView
AlaskaProgressMooseRushView
```

## Build

Open the repo in Android Studio and run the `app` configuration on a device or emulator.

GitHub Actions also builds a debug APK using:

```text
.github/workflows/android-debug-apk.yml
```

Artifacts:

- `you-rush-alaska-debug-apk`
- `you-rush-alaska-build-logs`

## Beta test checklist

1. Confirm GitHub Actions builds the APK.
2. Install the APK on a real Android device.
3. Verify photo picker and photo restore.
4. Verify all five Alaska stages.
5. Verify movement, jump, throw, tree, pause, lives, respawn, cheat code, stars, shield, progress HUD, near-miss, combo, and incoming-callout systems.
6. Tune movement, spacing, scoring, and difficulty.
7. Confirm no crash in a 15-minute phone test.
8. Keep 2.0 as the future cleanup and polish milestone.
