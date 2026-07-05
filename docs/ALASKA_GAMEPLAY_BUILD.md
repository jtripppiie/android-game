# Alaska Gameplay Build Notes

This document describes the Alaska-first gameplay pass for **You Rush** by **TripperDeeLabs**.

## Product focus

The current build focuses on Alaska as the template region. Other regions should eventually copy this structure, but Alaska needs to feel fun first.

Core idea:

> Upload your face, pick an Alaska stage, dodge local chaos, defeat the boss, unlock the next stage.

## Current game loop

1. Splash screen
2. Main menu
3. Alaska map
4. Optional customization
5. Stage run
6. Boss phase
7. Stage clear or game over
8. Retry / map / next stage

## Alaska stages

The current Alaska map has five stages:

| Stage | Season style | Main hazard | Boss | Goal before boss |
|---|---|---|---|---|
| Midnight Sun Run | Midnight Sun | SUN placeholder | Sunburn Sprite | 6 gates |
| Salmon Rush | Summer | SALMON | Salmon Boss | 8 gates |
| Moose Pass | Summer | MOOSE | Moose Boss | 10 gates |
| Dark Winter | Darkness | DARK | Darkness Boss | 12 gates |
| Bear Country | Winter | BEAR | Bear Boss | 14 gates |

## Controls

The game now supports a visible virtual control pad:

- **LEFT**: move player left
- **RIGHT**: move player right
- **JUMP**: bounce upward
- **FIRE**: shoot a snowball

A normal screen tap during gameplay still works as a quick bounce, but the virtual pad is the intended debugging/test interface now.

## Scoring

Current scoring values:

- Clear a gate: **+10**
- Dodge a hazard: **+4**
- Hit a boss with a snowball: **+25**
- Defeat a boss: **+100 + stage bonus**

Best score persists in shared preferences.

## Stage progression

Each stage has a gate goal. Once the player clears the required number of gates, normal gate/hazard spawning stops and the boss phase begins.

When the boss is defeated:

- The stage clear screen appears.
- The next stage unlocks.
- Best score is saved if beaten.
- The player can go to the map or start the next stage.

The map still shows every stage so the Alaska template can be inspected during development.

## Boss design

Bosses currently use placeholder vector assets and simple movement patterns.

The player defeats a boss by pressing **FIRE** and landing snowball hits.

Current boss health:

- Sunburn Sprite: 2
- Salmon Boss: 3
- Moose Boss: 4
- Darkness Boss: 4
- Bear Boss: 6

The boss phase has a fail timer. If the player does not defeat the boss quickly enough, the boss wins.

## Debugging and logging

There are two layers of debug support:

### Android logcat

`MooseRushView` logs game events with the tag:

```text
YouRushGame
```

`MainActivity` logs lifecycle and photo-picker events with the tag:

```text
YouRushDebug
```

### In-game debug overlay

The main menu has a **DEBUG** toggle.

When enabled, gameplay shows:

- Current state
- Score
- Boss status and HP
- Player position
- Active shots and hazards
- Recent game events

This is meant to make mobile testing easier without needing logcat open constantly.

## Runtime art assets

The current runtime art lives in two asset groups.

### Android raster art

These are generated raster PNG assets used by the Android renderer:

```text
app/src/main/res/drawable-nodpi/background_midnight_sun_art.png
app/src/main/res/drawable-nodpi/background_dark_winter_art.png
app/src/main/res/drawable-nodpi/sprite_tree_summer.png
app/src/main/res/drawable-nodpi/sprite_tree_winter.png
app/src/main/res/drawable-nodpi/sheet_moose_walk.png
app/src/main/res/drawable-nodpi/sheet_bear_walk.png
app/src/main/res/drawable-nodpi/sheet_salmon_swim.png
app/src/main/res/drawable-nodpi/sheet_eagle_fly.png
app/src/main/res/drawable-nodpi/sheet_player_run_headless.png
app/src/main/res/drawable-nodpi/sheet_mom_run.png
app/src/main/res/drawable-nodpi/sheet_dad_run.png
```

See `docs/SPRITE_SHEET_ASSET_PIPELINE.md` for the repeatable generation and
transparent-background conversion process.

### Region asset pack folder

This folder now stores Alaska region metadata only:

```text
app/src/main/assets/regions/alaska/
```

The old SVG placeholder files were removed in `1.8.5-alpha`. The runtime uses
generated raster drawables and sprite sheets from `app/src/main/res/drawable-nodpi/`,
while `region.json` names those runtime resources for future region tooling.

## Current development priorities

1. Confirm APK build succeeds.
2. Install debug APK on Android phone.
3. Test touch targets for LEFT / RIGHT / JUMP / FIRE.
4. Tune gravity, gate spacing, and boss HP.
5. Improve boss attack patterns.
6. Add photo crop and face-position controls.
7. Add share-card screen after game over.
8. Only after Alaska is fun, start Florida or Michigan.

## Important note

This game intentionally avoids user accounts for now. Personalization should happen locally through photo upload and saved preferences. The logging mentioned here is debug logging, not user login/authentication.
