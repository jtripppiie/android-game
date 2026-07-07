# You Rush

A personalized Android arcade game by **TripperDeeLabs**.

**Current status: Alaska 2.7.3 adaptive splash-title beta.**

This build is beta-ready for APK testing, tuning, and bug fixing. It is not final 1.0 or a larger milestone until it installs and plays well across all Alaska stages on a real phone.

## Current beta

```text
versionCode: 273
versionName: 2.7.3-beta
build badge: ALASKA PASSPORT v2.7.3 BETA
```

## Game flow

1. Splash screen
2. Main menu
3. Daily Rush or Alaska map
4. Selected-stage start
5. Customization / photo personalization
6. Main arcade run with a single unified HUD
7. Boss phase
8. Stage clear or retry
9. Next stage unlock / score chase

## Controls

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: jump from the ground; tap again in the air for a double jump
- **FIRE**: launch a snowball
- **PAUSE**: freeze the current run, resume, return to map, or edit sprite

## Gameplay style

You Rush is a side-scrolling platform runner (Mario-style), not a flap-to-fly
game. Your character runs along the ground under gravity. Stage-specific trail
obstacles rise from the ground — driftwood rails, fish racks, antler
barricades, ice markers, and snowbanks — while wildlife hazards enter from the
right. Jump obstacles, avoid or stun wildlife, and defeat bosses with snowballs.
The ground is safe to land on; only hitting an obstacle, a hazard, or the boss
ends the run.


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
- Launch-style boss and combo callout overlays
- Combo-based score multipliers and stage-clear bonus scoring
- Near-miss rewards for tight dodges
- Run missions and shield powerup pickups
- Aurora Rush meter and score burst mode
- Trail Tokens with cosmetic outfit unlocks
- Daily Rush rotating unlocked-stage challenge with local streak rewards
- Trail Passport collectible badges with token rewards
- Expedition Logs for higher-grade clears
- Trail Map and Rescue Kit pickups
- Real in-run pause/resume overlay with map and sprite exits
- Map route intel that calls out each selected stage's obstacles, hazard, and boss
- Stage-specific tactical briefing and launch callout
- Short live-HUD objective labels for better top-bar readability on phones
- Trail Scout mode from map pickups with upcoming danger markers
- Clean Vault skill bonuses for tight obstacle clears
- Boss weak-window reticle and FIRE NOW prompt
- Adaptive splash title: stacked words on narrow screens, huge spacing on wide screens
- Hazard forecast warnings
- Boss phase-two escalation callouts
- Expedition grading, route milestones, trail camp restocks, and beta run summaries
- Progressive difficulty pacing for speed and spawn pressure
- Hurdle progress bar
- Collectible star paths
- Parallax Alaska scenery with generated background plates and snow-tree layers
- Winter-mode polar bear, wolf, and roaring bear moments
- Three lives per run
- Checkpoint respawn after passed obstacles
- Snowball interactions
- Combo counter
- Persistent XP and local levels
- SoundPool generated SFX with persisted mute toggle
- Debug overlay (off by default; toggle from the menu)
- Version badge
- Sampled photo decoding to reduce memory risk from large gallery images
- Splash screen title spacing and boss-fight readability polish
- Stronger sprite edge guards for animal sheets and roar sprites
- Result panels with next-goal guidance after wins and losses
- Real map progression locks, respawn grace, boss escape timer, and clearer combat rules
- Stage-specific obstacle identities and calmer creature animation pacing
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
you-rush-alaska-2.7.3-beta-273-debug.apk
```

GitHub Actions also builds a debug APK using:

```text
.github/workflows/android-debug-apk.yml
```

The workflow runs on manual dispatch, `main`, `jtripp`, and the historical
`graphics-1.8.0` graphics branch. APK files are ignored by git; GitHub stores
the package as a workflow artifact instead of committing it to the repository.

Artifacts:

- `you-rush-alaska-debug-apk`
- `you-rush-alaska-build-logs`

## Tests

Pure game math lives in dependency-free classes so it can be covered by fast JVM
unit tests:

- `GameMath` — clamp and circle/rect collision helpers used by the game loop.
- `LevelCurve` — XP thresholds and level/progress math used by the level HUD.
- `RunnerTuning` — coyote time, jump buffer, and spawn-spacing fairness floors.
- `SpriteRenderer` / sprite trim tests — frame-edge guards that prevent atlas
  bleed artifacts during walking, swimming, and flying animation.
- `RunRewardEconomy` — Trail Token payouts, outfit unlocks, and Daily Rush
  rotation/streak math.
- `TrailBadgeCatalog` — local Trail Passport badge unlock rules and badge
  token rewards.

Run them with `./gradlew testDebugUnitTest`.

## Documentation Map

- `docs/APP_DUPLICATION_GUIDE.md` explains how to clone this app structure for a new region or new branded runner.
- `docs/REGION_REPLICATION_PLAYBOOK.md` is the detailed step-by-step manual for replicating the game for other regions.
- `docs/BRANCHING_AND_RELEASES.md` explains why Git branch names and Android app versions are separate, and how `main` should be used.
- `docs/ALASKA_GAMEPLAY_BUILD.md` documents the current gameplay loop, stages, scoring, assets, and debug hooks.
- `docs/SPRITE_SHEET_ASSET_PIPELINE.md` preserves the repeatable art-generation prompts and sprite-sheet rules.
- `docs/ANDROID_TEST_CHECKLIST.md` is the phone QA checklist for each APK.
- `docs/RELEASE_2_2_BETA_LAUNCH_READINESS.md` documents the 2.2.0-beta release-owner hardening pass.
- `docs/RELEASE_2_3_BETA_GAMEPLAY_POLISH.md` documents the next-five gameplay polish pass.
- `docs/RELEASE_2_4_BETA_PROGRESSION_POLISH.md` documents the progression and fairness polish pass.
- `docs/RELEASE_2_5_BETA_VISUAL_CONTRACT.md` documents the obstacle-identity and animation-readability pass.

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
8. Keep 2.5.x focused on visual/gameplay clarity, phone QA, and crash-free beta testing.
