# You Rush

A personalized Android arcade game by **TripperDeeLabs**.

**Current status: Alaska 0.9.0 beta.**

This build is now treated as beta-ready: the main Alaska loop is feature-complete enough for serious APK testing, balancing, and bug fixing. It is still not final 1.0 or 2.0 until the APK is build-verified, installed, playtested, and tuned.

> Add your face. Pick Alaska. Survive local chaos. Build combos. Clear stages. Chase a better score.

## Current beta

```text
versionCode: 19
versionName: 0.9.0-beta
build badge: ALASKA BETA v0.9.0
```

## Current game flow

1. Splash screen
2. Main menu
3. Alaska map
4. Customization / photo personalization
5. Stage intro
6. Main arcade run
7. Stage challenge phase
8. Stage clear or retry
9. Next stage unlock / score chase

## Alaska stages

| Stage | Style | Goal |
|---|---|---|
| Midnight Sun Run | warm-up | clear 6 gates |
| Salmon Rush | summer | clear 8 gates |
| Moose Pass | summer | clear 10 gates |
| Dark Winter | darkness | clear 12 gates |
| Bear Country | winter | clear 14 gates |

## Controls

The visible control pad includes:

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: bounce upward
- **THROW**: launch a snowball
- **CLIMB TREE**: appears only when close enough to the tree

A normal screen tap during gameplay still bounces the character.

## Beta gameplay systems

The active Alaska build includes:

- Splash and menu flow
- Alaska map
- Photo-personalized player character
- Animated generated body
- Local saved progress
- Five Alaska stages
- Seasonal backdrops
- Antler gate challenge
- Regional moving obstacles
- Stage challenge phase
- Snowball interactions
- Tree timing mechanic
- Large-obstacle tree disruption
- Directional controls
- Jump and throw controls
- Stage intro overlay
- Near-miss rewards
- Combo streaks
- Incoming callouts
- Touch ripples
- Haptic feedback where supported
- Version badge
- Debug overlay
- Build-log artifact workflow
- Safer launcher vector paths
- Hardened app manifest

## Active view stack

`MainActivity` currently loads:

```java
new AlaskaHazardWarningMooseRushView(this)
```

That layer inherits the full gameplay stack:

```text
MooseRushView
JuicyMooseRushView
AlaskaSurvivalMooseRushView
AlaskaNearMissMooseRushView
AlaskaComboMooseRushView
AlaskaStageIntroMooseRushView
AlaskaHazardWarningMooseRushView
```

## Scoring

Current scoring includes:

- Gate clears
- Hazard dodges
- Snowball interactions
- Stage challenge hits
- Stage completion bonuses
- Near-miss bonuses
- Combo bonuses

Best score persists locally.

## Privacy posture

The app is local-first.

- No account required
- No server profile
- No network requirement for normal play
- Photo personalization uses Android's system picker
- Selected photo reference and scores are saved locally

See `docs/PRIVACY.md`.

## Android compatibility

- `minSdk 23`
- `targetSdk 35`
- `compileSdk 35`
- Android Gradle Plugin `8.7.3`
- Gradle `8.10.2` in GitHub Actions

## Build

Open the repo in Android Studio and run the `app` configuration on a device or emulator.

GitHub Actions also builds a debug APK using:

```text
.github/workflows/android-debug-apk.yml
```

Artifacts:

- `you-rush-alaska-debug-apk`
- `you-rush-alaska-build-logs`

## Release docs

- `docs/BETA_0_9_READINESS.md`
- `docs/REAL_2_0_ROADMAP.md`
- `docs/PRIVACY.md`
- `docs/STORE_LISTING_DRAFT.md`
- `docs/ANDROID_TEST_CHECKLIST.md`

## Beta readiness checklist

Before calling this beta stable:

1. Confirm GitHub Actions builds the APK.
2. Install the APK on a real Android device.
3. Verify photo picker and photo restore.
4. Verify all five Alaska stages.
5. Verify stage challenge phase.
6. Verify snowball, tree, near-miss, combo, and incoming-callout systems.
7. Tune movement, spacing, scoring, and difficulty.
8. Confirm no crash in a 15-minute phone test.
9. Confirm the game is understandable without developer notes.
10. Keep 2.0 as the future architectural cleanup and polish milestone.
