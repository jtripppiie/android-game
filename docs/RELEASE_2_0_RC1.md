# You Rush Alaska 2.0 RC1

Version:

```text
versionCode 20
versionName 2.0.0-rc1
badge ALASKA RC v2.0.0-rc1
```

## Release identity

**You Rush** is a local-first Android arcade game by **TripperDeeLabs**.

The 2.0 Alaska experience focuses on one complete region with a polished arcade loop.

## Player loop

1. Open the game.
2. Pick or continue an Alaska stage.
3. Add a face photo if desired.
4. Move, jump, throw snowballs, and use trees carefully.
5. Clear stage goals.
6. Complete the stage challenge.
7. Unlock the next stage.
8. Chase a better score.

## Included systems

- Splash screen
- Main menu
- Alaska map
- Photo personalization
- Local saved preferences
- Five Alaska stages
- Seasonal backdrops
- Gate challenge
- Regional moving obstacles
- Stage challenge phase
- Snowball interactions
- Tree timing mechanic
- Directional controls
- Jump control
- Throw control
- Stage intro overlay
- Near-miss rewards
- Combo streaks
- Incoming callouts
- Debug overlay
- Version badge
- Build-log artifact workflow

## Current active view stack

`MainActivity` loads:

```java
new AlaskaHazardWarningMooseRushView(this)
```

That class extends the previous layers, so the active gameplay stack includes:

```text
MooseRushView
JuicyMooseRushView
AlaskaSurvivalMooseRushView
AlaskaNearMissMooseRushView
AlaskaComboMooseRushView
AlaskaStageIntroMooseRushView
AlaskaHazardWarningMooseRushView
```

## Release candidate status

This is marked as a release candidate, not a final store release, until a fresh GitHub Actions APK build is confirmed and installed on a real Android device.

## Final release checks

Before calling this final 2.0:

1. GitHub Actions must build a fresh APK successfully.
2. APK must install on a real Android phone.
3. Photo picker must work.
4. All five Alaska stages must start.
5. Stage challenge phase must trigger.
6. Snowball interactions must work.
7. Tree timing must not soft-lock the player.
8. Combo and near-miss scoring must feel controlled.
9. Debug overlay must be toggleable.
10. Version badge should be disabled for a public store build.
