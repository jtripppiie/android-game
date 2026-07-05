# App Duplication Guide

Use this when turning You Rush into a new region, new branded runner, or a
separate personalized arcade app.

The goal is to copy the structure without copying confusion.

For the full step-by-step replication manual, including stage planning sheets,
asset naming, sprite rules, Daily Rush transfer, QA, release, and handoff
templates, use:

```text
docs/REGION_REPLICATION_PLAYBOOK.md
```

## What To Keep

Keep these systems:

- `MainActivity`: Android lifecycle, fullscreen setup, photo picker bridge, and version badge.
- `MooseRushView`: current single-view game loop, drawing, input, HUD, and screens.
- `GameState`: run state, lives, score, combo, XP, level, and shield flag.
- `RunnerTuning`: platformer feel constants and spawn fairness floors.
- `DifficultyCurve`: capped progressive difficulty.
- `ArcadeScoring`: combo multipliers and stage-clear bonus math.
- `RunRewardEconomy`: Trail Token rewards, cosmetic unlock rules, and Daily
  Rush rotation/streak math.
- `GameAssets`: runtime bitmap/drawable loading.
- `SpriteRenderer`: player/photo runner composition.
- `SpriteSheetMath`: runtime frame-edge guards.
- `VisualEffects`: particles and score popups.

These are the engine-like pieces of the app.

## What To Replace For A New Region

For a new region, replace or add:

- Stage names
- Season mapping
- Hazard labels
- Boss names
- Goal gate counts
- Boss health
- Background plates
- Tree/foreground sprites
- Animal/enemy sprite sheets
- Roar/special pose sprites
- Store listing copy
- Region metadata
- Test checklist expectations

Primary files:

```text
app/src/main/java/com/jtripppiie/mooserush/MooseRushView.java
app/src/main/java/com/jtripppiie/mooserush/GameAssets.java
app/src/main/assets/regions/alaska/region.json
app/src/main/res/drawable-nodpi/
docs/ALASKA_GAMEPLAY_BUILD.md
docs/SPRITE_SHEET_ASSET_PIPELINE.md
docs/ANDROID_TEST_CHECKLIST.md
```

## Stage Template

Each stage needs:

```text
name
one-line player-facing description
season
primary hazard label
boss name
gate goal
boss health
base scroll speed
spawn seconds
boss type index
```

Current Java shape:

```java
new StageConfig(
        "Stage Name",
        "Short instruction line.",
        SEASON_WINTER,
        "Boss Name",
        "HAZARD",
        10,
        6,
        198,
        1.85f,
        4
)
```

Keep early stages easy and readable. Make later stages harder through controlled
speed, spawn spacing, hazard variety, and boss pressure.

## Asset Template

Use `drawable-nodpi` for generated raster art:

```text
app/src/main/res/drawable-nodpi/
```

Sprite-sheet rules:

- 6 frames
- One horizontal row
- Transparent PNG after chroma-key conversion
- Consistent baseline
- Consistent subject scale
- Generous padding
- No text
- No frame numbers
- No watermark
- No shadows baked into transparent sprites

The app expects runtime source-rect guards. Still, do not rely on the renderer
to fix messy source art. Clean source sheets produce cleaner animation.

## Photo Player Template

The player body is drawn from:

```text
sheet_player_run_headless.png
```

The uploaded face is composited separately by `SpriteRenderer`.

For a new app, keep the headless-body strategy unless the entire identity of
the game changes. It is the reason the personalized photo reads as a game
character instead of a pasted screenshot.

## Reward Loop Template

The current replay loop is:

1. Clear gates.
2. Collect stars.
3. Dodge hazards.
4. Build combo.
5. Fill Aurora Rush.
6. Trigger score-burst mode.
7. Complete run missions.
8. Defeat boss or survive as long as possible.
9. Earn Trail Tokens.
10. Unlock cosmetic outfit colors.
11. Return for the rotating Daily Rush challenge and streak bonus.
12. Retry for a better rank, more tokens, and more stage progress.

Keep monetization fair:

- Cosmetics can be paid or earned.
- Gameplay power should not require payment.
- No timers that block play.
- No random paid loot.
- No hidden odds.

## Persistence Template

Current persistent values live in shared preferences:

```text
best_score
selected_stage
selected_season
unlocked_stage
debug_overlay
muted
xp
outfit
trail_tokens
unlocked_outfits
total_missions
daily_completed_day
daily_streak
```

When adding a new persistent value:

1. Add a `PREF_` key.
2. Load it in the constructor.
3. Save it in the relevant state transition.
4. Add a line to this guide.
5. Add a test if the value affects game math.

## Build Template

Use these commands before every handoff:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Expected debug APK path:

```text
app/build/outputs/apk/debug/
```

The filename is controlled by `app/build.gradle`:

```gradle
outputFileName = "you-rush-alaska-${variant.versionName}-${variant.versionCode}-${variant.name}.apk"
```

## Documentation Checklist

For every meaningful release, update:

- `README.md`
- `docs/VERSIONING.md`
- `docs/ANDROID_TEST_CHECKLIST.md`
- `docs/ALASKA_GAMEPLAY_BUILD.md`
- `docs/REGION_REPLICATION_PLAYBOOK.md`
- `docs/SPRITE_SHEET_ASSET_PIPELINE.md` when assets change
- `app/src/main/assets/regions/alaska/region.json` when stage metadata changes
- `.github/workflows/android-debug-apk.yml` when release branches change

## Duplication Steps

1. Create a branch from `main`.
2. Decide whether this is a new region inside You Rush or a separate app.
3. Copy the Alaska stage template.
4. Rename stages, hazards, bosses, and region metadata.
5. Generate new background plates and sprite sheets.
6. Add new assets to `drawable-nodpi`.
7. Wire assets in `GameAssets`.
8. Update stage configs in `MooseRushView`.
9. Tune `RunnerTuning` and `DifficultyCurve` only if the new region needs a different feel.
10. Keep `RunRewardEconomy` unless the monetization model changes.
11. Keep Daily Rush local and fair unless the product intentionally adds accounts.
12. Update docs.
13. Run tests.
14. Build APK.
15. Merge to `main`.
16. Push `main`.

## Quality Bar

Do not call a duplicate app ready until:

- The first screen is playable, not a placeholder landing page.
- The player reads clearly at phone size.
- Every hazard reads clearly at phone size.
- Background motion is smooth.
- Jump arcs feel fair.
- Difficulty ramps without sudden spikes.
- Score/reward feedback is understandable.
- The APK installs on a real phone.
- A 15-minute manual test does not crash.
