# You Rush

A personalized Android arcade game by **TripperDeeLabs**.

**Current status: Alaska 0.9.1 beta.**

This build is beta-ready for APK testing, tuning, and bug fixing. It is not final 1.0 or 2.0 until it builds, installs, and plays well on a real phone.

## Current beta

```text
versionCode: 20
versionName: 0.9.1-beta
build badge: ALASKA BETA v0.9.1
```

## Game flow

1. Splash screen
2. Main menu
3. Alaska map
4. Customization / photo personalization
5. Stage intro
6. Main arcade run
7. Pause / quick help
8. Stage challenge phase
9. Stage clear or retry
10. Next stage unlock / score chase

## Controls

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: bounce upward
- **THROW**: launch a snowball
- **CLIMB TREE**: appears only when close enough to the tree
- **PAUSE**: opens quick help and stops the run until resumed

## Beta systems

- Splash and menu flow
- Alaska map
- Photo-personalized player character
- Local saved progress
- Five Alaska stages
- Stage intro overlay
- Pause and quick-help overlay
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
new AlaskaPauseHelpMooseRushView(this)
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
5. Verify movement, jump, throw, tree, pause, near-miss, combo, and incoming-callout systems.
6. Tune movement, spacing, scoring, and difficulty.
7. Confirm no crash in a 15-minute phone test.
8. Keep 2.0 as the future cleanup and polish milestone.
