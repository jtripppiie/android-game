# Region Replication Playbook

Use this playbook when turning **You Rush: Alaska** into another region, another
branded runner, or a clean clone that keeps the same game engine and replaces
the local theme.

The goal is not to copy random files and hope the game still works. The goal is
to preserve the engine-like systems, replace the region-specific content, test
the new package, and leave enough documentation that the next region is easier.

## Core Strategy

The current app is still mostly a single-view Android game. That means the
proper replication path is:

1. Keep the stable engine systems.
2. Replace the region content deliberately.
3. Add or swap art assets through documented names.
4. Tune stage pacing for the new hazards.
5. Run unit tests and build the APK.
6. Update docs before pushing.

Do not treat the old Alaska branch name as the product version. Branch names are
work streams. Android package versions live in `app/build.gradle`.

## What Counts As Engine

Keep these files and classes unless the new product has a truly different game
model:

```text
app/src/main/java/com/jtripppiie/mooserush/MainActivity.java
app/src/main/java/com/jtripppiie/mooserush/MooseRushView.java
app/src/main/java/com/jtripppiie/mooserush/GameState.java
app/src/main/java/com/jtripppiie/mooserush/RunnerTuning.java
app/src/main/java/com/jtripppiie/mooserush/DifficultyCurve.java
app/src/main/java/com/jtripppiie/mooserush/ArcadeScoring.java
app/src/main/java/com/jtripppiie/mooserush/RunRewardEconomy.java
app/src/main/java/com/jtripppiie/mooserush/TrailBadgeCatalog.java
app/src/main/java/com/jtripppiie/mooserush/GameAssets.java
app/src/main/java/com/jtripppiie/mooserush/SpriteRenderer.java
app/src/main/java/com/jtripppiie/mooserush/SpriteSheetMath.java
app/src/main/java/com/jtripppiie/mooserush/VisualEffects.java
```

Engine responsibility by class:

| Class | Keep because |
|---|---|
| `MainActivity` | Owns fullscreen Android setup, photo picker bridge, version badge, saved photo restore, and photo reset requests. |
| `MooseRushView` | Owns the run loop, screens, input, drawing, collision, stage state, and current region constants. |
| `GameState` | Owns score-adjacent run state: lives, combo, stars, XP, level, shield, mute, and per-run reset behavior. |
| `RunnerTuning` | Keeps jump feel, coyote time, jump buffering, gravity, and spawn-spacing fairness in one testable place. |
| `DifficultyCurve` | Provides capped progressive difficulty so runs ramp up without sudden unfair spikes. |
| `ArcadeScoring` | Keeps score multipliers and stage-clear bonuses consistent. |
| `RunRewardEconomy` | Keeps Trail Token payouts, outfit unlock rules, Daily Rush rotation, daily reward, and streak math. |
| `TrailBadgeCatalog` | Keeps Trail Passport badge names, masks, thresholds, and one-time token rewards. |
| `GameAssets` | Loads generated raster resources and exposes them to the renderer. |
| `SpriteRenderer` | Composes the uploaded face with the headless runner body and trims player frames. |
| `SpriteSheetMath` | Guards frame source rectangles against atlas bleed artifacts. |
| `VisualEffects` | Draws score popups, particles, spark bursts, trails, and impact feedback. |

## What Counts As Region Content

Replace these for a new region:

```text
Stage names
Stage one-line descriptions
Season names and season mapping
Hazard labels
Boss names
Goal gate counts
Boss health
Base scroll speeds
Spawn timings
Background plates
Foreground trees/props
Animal/enemy sprite sheets
Special pose sprites
Region metadata JSON
Visible copy in menus, badges, docs, and test checklists
```

The biggest current region-specific code lives in `MooseRushView`:

```java
private static final String[] SEASONS = { ... };

private static final StageConfig[] STAGES = {
        new StageConfig(...),
        ...
};
```

When duplicating the app, start here first. A future cleanup should extract this
to a data-driven region loader, but today the controlled path is to edit this
constant block and then document the new region metadata.

## New Region Planning Sheet

Fill this out before editing code. It prevents the new region from becoming a
theme swap with unclear gameplay.

```text
Region name:
Product name:
Package version:
Build badge:
Primary vibe:
Main color notes:
Safe-for-kids tone:
Stage 1:
Stage 2:
Stage 3:
Stage 4:
Stage 5:
Primary collectible:
Common obstacle:
Fast hazard:
Heavy hazard:
Flying hazard:
Water/ground hazard:
Final boss:
Cosmetic outfit theme:
Daily Rush reward name:
Trail Passport badge theme:
```

Design targets:

- Stage 1 teaches the jump and should be forgiving.
- Stage 2 introduces the first region-specific hazard.
- Stage 3 asks for better timing.
- Stage 4 changes the visual mood or hazard height.
- Stage 5 should feel like the big local challenge.
- Every hazard must be readable at phone size.
- Every stage should have one clear reason to exist.

## Stage Template

Each stage maps to this constructor:

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

Field meaning:

| Field | Meaning | Replication guidance |
|---|---|---|
| `name` | Player-facing stage name | Keep it short enough for HUD text. |
| `line` | Ready-screen instruction | One sentence. Tell the player what changes. |
| `season` | Visual environment index | Use this to select background, lighting, trees, and stage mood. |
| `bossName` | Result/death/boss label | Should identify the local threat clearly. |
| `hazardLabel` | Runtime hazard category | Keep uppercase and short: `MOOSE`, `EAGLE`, `BEAR`, etc. |
| `goalGates` | Hurdles before boss | Start around 5, increase toward 10. |
| `bossHealth` | Snowball hits needed | Start around 2, increase toward 6. |
| `baseSpeed` | Stage scroll speed | Tune after testing on a real phone. |
| `spawnSeconds` | Base spawn pacing | Lower is harder. Respect fairness floors in `RunnerTuning`. |
| `bossType` | Boss movement/style index | Reuse an existing pattern first, then customize only if needed. |

Suggested starting values:

| Stage | Gates | Boss HP | Speed | Spawn seconds |
|---|---:|---:|---:|---:|
| Tutorial | 5 | 2 | 150 | 2.35 |
| Easy hazard | 7 | 3 | 165 | 2.15 |
| Timing stage | 8 | 4 | 178 | 2.05 |
| Advanced stage | 9 | 4 | 188 | 1.95 |
| Final stage | 10 | 6 | 198 | 1.85 |

Avoid making difficulty harder only by making everything faster. Mix difficulty
through speed, spacing, hazard height, collectible placement, and boss pressure.

## Season Template

Seasons are visual modes, not just weather labels. Each season should answer:

```text
What color is the sky?
What is the distant background?
What foreground prop repeats?
How much contrast does gameplay need?
Does the stage need snow, water, desert, city lights, forest, etc.?
Does the player/hazard silhouette stay readable?
```

Current Alaska season roles:

| Season | Role |
|---|---|
| Summer | Brighter standard outdoor look. |
| Winter | Snow-heavy challenge look. |
| Midnight Sun | Warm bright Alaska look. |
| Darkness | Dark winter/night mood with stronger contrast needs. |

For a new region, rename or replace the season list in `MooseRushView`, then
make sure `drawAlaskaBackdrop` or its replacement chooses the correct art.

## Art Asset Naming

Use consistent names so `GameAssets` and docs stay easy to scan.

Recommended pattern:

```text
background_<region>_<season>_art.png
sprite_<region>_<foreground>.png
sheet_<creature>_<motion>.png
sprite_<creature>_<special_pose>.png
sheet_player_run_headless.png
sheet_mom_run.png
sheet_dad_run.png
```

Current Alaska examples:

```text
background_midnight_sun_art.png
background_dark_winter_art.png
sprite_tree_summer.png
sprite_tree_winter.png
sheet_moose_walk.png
sheet_bear_walk.png
sheet_polar_bear_walk.png
sheet_wolf_run.png
sheet_salmon_swim.png
sheet_eagle_fly.png
sheet_player_run_headless.png
sprite_bear_roar.png
sprite_polar_bear_roar.png
```

Put runtime raster art here:

```text
app/src/main/res/drawable-nodpi/
```

Do not put large runtime SVG placeholders in the region asset folder. The
current runtime uses generated raster drawables and Canvas for simple geometry.

## Sprite Sheet Rules

Use this format unless there is a strong reason to change it:

```text
frames: 6
layout: one horizontal row
background: transparent PNG
padding: generous
baseline: stable across frames
subject scale: stable across frames
style: same game world as the player
```

Critical quality rules:

- No frame numbers.
- No watermark.
- No baked text.
- No opaque background.
- No colored fringe from chroma-key cleanup.
- No visible leftovers from neighboring frames.
- No huge pose jumps between frames.
- No animation that reads as 100x speed.
- Keep feet, paws, fins, and wing roots stable enough to avoid jitter.

When a generated sheet has edge artifacts, fix the source image first when
possible. Runtime trim guards help, but they should not be the primary cleanup
tool.

## Sprite Sheet Integration Steps

1. Add the PNG to `app/src/main/res/drawable-nodpi/`.
2. Add a field in `GameAssets`.
3. Load the drawable by resource name.
4. Add frame trim data in `MooseRushView` when needed.
5. Route the hazard or boss renderer to the new sheet.
6. Verify animation speed on device.
7. Verify there are no halo/circle effects unless the design intentionally uses one.
8. Update `docs/SPRITE_SHEET_ASSET_PIPELINE.md`.
9. Update the region JSON runtime asset list.

Example trim block shape:

```java
private static final int[][] WOLF_FRAME_TRIMS = {
        {28, 266, 362, 469},
        {0, 275, 362, 461},
        {0, 266, 362, 467},
        {0, 274, 362, 469},
        {0, 272, 362, 468},
        {0, 271, 342, 468}
};
```

Trim values are:

```text
left, top, right, bottom
```

They are measured inside each frame, not across the whole sheet.

## Player Sprite Strategy

Keep the headless runner strategy for personalized versions:

```text
sheet_player_run_headless.png
```

The uploaded face is drawn separately by `SpriteRenderer`. This matters because
it keeps the photo from looking like a pasted rectangle. The body should:

- Have a clear run cycle.
- Have stable legs and feet.
- Use a simple readable outfit.
- Leave space for a circular/cropped head.
- Match the scale of hazards and UI.
- Use strong outlines at phone size.

For region copies, usually keep the same player sheet first. Replace it only
after the hazards, backgrounds, and pacing are stable.

## Background And Foreground Rules

The background should support gameplay, not fight it.

Rules:

- Use raster background plates for mountains, skyline, water, or distant land.
- Use foreground prop sprites for trees, signs, rocks, grass, snowbanks, etc.
- Keep the ground lane stable.
- Anchor trees and props to the gameplay ground line, not arbitrary screen
  percentages.
- Sink transparent-bottom sprites slightly into the ground layer so roots,
  trunks, rocks, or signs do not appear to float.
- Avoid scenery that snaps or jumps when it loops.
- Avoid highly detailed props directly behind hazards.
- Keep contrast high around the player and obstacles.
- Do not reintroduce old vector triangle mountain overlays.

Layering target:

```text
Sky / far plate
Distant land or mountains
Midground trees/props
Gameplay lane
Ground texture
HUD and overlays
```

If a foreground prop jitters, check whether it is tied to `groundScroll` instead
of a longer scenery/parallax scroll value.

## Boss Grounding And Attack Lanes

Bosses should use explicit gameplay lanes:

- Ground bosses should compute their center from the ground line and drawn
  sprite height.
- Flying or swimming bosses can use an air lane, but the lane should be clamped
  between the HUD and the ground.
- Shadows should be drawn on the ground lane, not under an arbitrary floating
  center point.
- Boss X movement should visibly advance toward the player and retreat. A boss
  that parks at the far-right edge feels unfinished.

Avoid this pattern for boss placement:

```java
bossY = getHeight() * 0.42f;
```

Prefer this pattern:

```java
bossY = bossLaneCenterY(stage);
bossX += (bossAttackX(stage) - bossX) * smoothing;
```

## Region Metadata JSON

Every region should have metadata similar to:

```text
app/src/main/assets/regions/<region_id>/region.json
```

Minimum required sections:

```json
{
  "id": "region_id",
  "name": "Region Name",
  "style": "retro-funny",
  "studio": "TripperDeeLabs",
  "status": "runtime-metadata",
  "runtimeAssets": {
    "backgrounds": [],
    "foreground": [],
    "spriteSheets": []
  },
  "gameplayTemplate": {
    "loop": [],
    "controls": [],
    "scoring": {},
    "retention": {},
    "debug": {}
  },
  "stages": [],
  "deathLines": []
}
```

For a new region:

1. Copy the Alaska JSON to a new folder.
2. Rename `id`, `name`, and asset names.
3. Update every stage entry.
4. Update runtime background and hazard sheet names.
5. Keep the `retention` section if Daily Rush exists.
6. Keep the Trail Passport retention values if badge collection exists.
7. Keep debug tags unless code tags are intentionally renamed.

## Daily Rush Replication

Daily Rush is local-first and fair. It does not require accounts, servers, or
paid timers.

Current behavior:

- Rotates one unlocked stage per local day.
- Sets the stage season automatically.
- Shows a main-menu Daily Rush button.
- Pays one bonus per local day.
- Awards the bonus when the player clears the daily gate target or clears the stage.
- Increases local streak rewards.
- Persists completion day and streak in shared preferences.

Current preference keys:

```text
daily_completed_day
daily_streak
```

Reusable math lives in:

```text
app/src/main/java/com/jtripppiie/mooserush/RunRewardEconomy.java
```

Daily methods to preserve:

```java
dailyStageIndex(int dayKey, int stageCount)
dailyGateGoal(int dayKey, int stageGoalGates)
dailyReward(int streakBeforeClaim)
nextDailyStreak(int lastCompletedDay, int todayKey, int currentStreak)
```

When copying to a new region:

1. Keep Daily Rush rotating only through unlocked stages unless the product
   intentionally wants all stages available from day one.
2. Keep the daily reward cosmetic/progression-only.
3. Do not block normal play after the daily reward is claimed.
4. Update docs and QA checklist with the daily challenge name if it changes.
5. Add tests if reward math changes.

## Trail Passport Replication

Trail Passport is the local badge collection layer. It gives kids and families
clear goals beyond a single score chase while staying fair and cosmetic.

Current behavior:

- Tracks earned badges in a single integer bit mask.
- Awards badges only at run resolution so feedback is clean.
- Pays a one-time Trail Token reward for each new badge.
- Shows badge progress on the main menu.
- Shows newly earned badge summaries on game-over and stage-clear panels.
- Keeps badge rules in a testable catalog class.

Current preference key:

```text
trail_badges
```

Reusable rules live in:

```text
app/src/main/java/com/jtripppiie/mooserush/TrailBadgeCatalog.java
```

Current Alaska badges:

| Badge | Unlock idea |
|---|---|
| First Trail | Make any run progress. |
| Gate Skipper | Pass at least five gates. |
| Star Scout | Collect at least three stars. |
| Combo Spark | Reach a six-combo run. |
| Boss Buster | Clear a stage. |
| Perfect Parka | Clear a stage without losing lives. |
| Daily Dasher | Complete Daily Rush. |
| Alaska Passport | Unlock or clear the final stage. |
| Aurora Chaser | Trigger Aurora Rush. |
| Mission Maker | Complete all three run missions. |

When copying to a new region:

1. Rename badge names to match the region tone.
2. Keep early badges easy so the first session has a win.
3. Keep at least one badge for stage clears, one for collection, one for combo,
   one for daily play, and one for completing the region.
4. Keep token rewards one-time only.
5. Add tests when changing thresholds or badge count.
6. Update region JSON with `trailPassport`, badge count, and reward amount.

## Reward And Monetization Rules

The current replay economy is intentionally kid-friendly and fair:

- Trail Tokens are earned through play.
- Outfit unlocks are cosmetic.
- Daily Rush rewards are once per day but do not block play.
- Trail Passport badges are earned by skill/progression and pay one-time token
  bonuses.
- Aurora Rush is earned through skill events inside the run.
- No paid random loot.
- No hidden odds.
- No pay-to-win gameplay power.

If a future app adds paid purchases, keep them cosmetic or convenience-focused.
Do not sell mandatory progress.

## UI Copy Replacement Checklist

Search and replace carefully:

```bash
rg -n "Alaska|ALASKA|Moose|Bear|Salmon|Eagle|Winter|Midnight|You Rush"
```

Places that often need text changes:

```text
README.md
docs/*.md
app/build.gradle
app/src/main/AndroidManifest.xml
app/src/main/res/values/strings.xml
app/src/main/assets/regions/<region_id>/region.json
MooseRushView stage constants
Death/result lines
Menu title/subtitle
Version badge
GitHub Actions artifact names if product name changes
```

Do not blindly replace Java package names unless the product truly needs a new
application id. If changing the package, update Android manifest, namespace,
application id, imports, and any build/CI scripts together.

## Build Version Steps

Before making a package build, update:

```gradle
versionCode 196
versionName "1.9.6-alpha"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA BETA"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA PASSPORT v1.9.6"'
```

For a new region, use a badge like:

```text
REGION BETA v0.1.0
REGION DAILY v0.2.0
REGION LAUNCH v1.0.0
```

Package rule:

- Increase `versionCode` for every APK package.
- Use `versionName` for the human release label.
- Keep the badge visible in debug/test builds.
- Turn off or simplify debug badges for public store builds.

## QA Checklist For A New Region

Run this on a real phone before calling the region ready:

### Install

- APK installs cleanly.
- App launches without crash.
- Version badge matches `app/build.gradle`.
- Existing saved state does not crash the new build.

### Menu

- Splash screen appears.
- Main menu appears.
- Daily Rush button fits and starts a valid unlocked stage.
- Trail Passport badge count is visible on the main menu.
- Map opens.
- Customize opens.
- Debug and mute buttons work.
- Bottom stats line does not overlap.

### Photo

- Photo picker opens.
- A valid face upload appears in preview.
- Reset photo restores the default sprite.
- Photo persists after app restart.
- Non-face or low-quality uploads are rejected when face validation is active.

### Gameplay

- Player scale is readable.
- Player legs animate naturally.
- Jump and double jump feel fair.
- Player does not hit the top of the screen too easily.
- Ground lane is stable.
- Hurdles can be jumped.
- Hazards have readable silhouettes.
- Hazards do not have unwanted halos.
- Background does not stutter.
- Foreground props do not snap.
- HUD does not cover the player or hazards.

### Stage Progression

- Stage 1 reaches boss.
- Stage 2 reaches boss.
- Stage 3 reaches boss.
- Stage 4 reaches boss.
- Stage 5 reaches boss.
- Boss health decreases when hit.
- Stage clear unlocks the next stage.
- Game over allows retry, map, and customize.

### Rewards

- Score increases for gates, stars, near misses, dodges, boss hits, and boss defeat.
- Combo multiplier grows and resets properly.
- Aurora Rush fills and triggers.
- Trail Tokens are awarded once per run result.
- Outfit unlocks spend Trail Tokens.
- Daily Rush pays once per local day.
- Daily streak appears on result screens.
- Trail Passport badges unlock only once and show on result screens.
- Badge token rewards are included in the run token summary.

### Persistence

- Best score persists.
- Selected stage persists.
- Selected season persists.
- Unlocked stage persists.
- XP and level persist.
- Trail Tokens persist.
- Outfit unlocks persist.
- Trail Passport badge progress persists.
- Daily completion and streak persist.
- Mute persists.

### Performance

- No obvious frame skipping during background scroll.
- Sprite animation speed feels natural.
- Particles do not obscure hazards.
- Long run does not leak memory or crash.
- A 15-minute phone test is stable.

## Unit Test Expectations

Keep fast JVM tests around math and fairness:

```text
GameMathTest
LevelCurveTest
RunnerTuningTest
RunRewardEconomyTest
TrailBadgeCatalogTest
SpriteSheetMathTest
SpriteRendererTest
```

Add or update tests when changing:

- Daily reward amounts.
- Daily streak rules.
- Badge names, thresholds, or reward amounts.
- Outfit unlock costs.
- Spawn fairness floors.
- Jump physics constants.
- Sprite source-rect guards.
- Score multipliers.

Run:

```bash
./gradlew testDebugUnitTest
```

## APK Build Expectations

Build with:

```bash
./gradlew assembleDebug
```

Expected output folder:

```text
app/build/outputs/apk/debug/
```

Expected filename format:

```text
you-rush-alaska-${variant.versionName}-${variant.versionCode}-${variant.name}.apk
```

If the product name changes, update the output filename pattern in
`app/build.gradle` and update every doc that references the APK filename.

## Git Workflow

Recommended flow:

```bash
git switch main
git pull
git switch -c region-name-polish
```

After work:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
git status --short
git add <files>
git commit -m "Add <region> region foundation"
git switch main
git merge region-name-polish
git push origin main
```

If working directly on `main`, still run tests and build before pushing.

Do not commit APKs unless the project policy changes. APK files are build
artifacts and should come from local Gradle output or GitHub Actions artifacts.

## Documentation To Update Every Time

Minimum docs for a meaningful region release:

```text
README.md
docs/VERSIONING.md
docs/ANDROID_TEST_CHECKLIST.md
docs/APP_DUPLICATION_GUIDE.md
docs/REGION_REPLICATION_PLAYBOOK.md
docs/SPRITE_SHEET_ASSET_PIPELINE.md
docs/ALASKA_GAMEPLAY_BUILD.md or the new region gameplay build doc
app/src/main/assets/regions/<region_id>/region.json
```

For a new region, create:

```text
docs/<REGION>_GAMEPLAY_BUILD.md
docs/<REGION>_TEST_CHECKLIST.md
app/src/main/assets/regions/<region_id>/region.json
```

Keep history in old docs. Do not erase Alaska notes just because another region
exists.

## Recommended Future Refactor

The proper long-term architecture is to move region data out of
`MooseRushView`:

```text
RegionConfig
StageConfig data loader
SeasonConfig
HazardConfig
BossConfig
RegionAssetManifest
RegionPalette
```

Target shape:

```text
MooseRushView
  uses GameEngine
  uses RegionConfig
  uses GameAssets
  draws current region through renderer helpers
```

Do this refactor only when the current gameplay is stable enough to protect
with tests. Until then, duplicate carefully and document each region-specific
change.

## Final Handoff Template

When finishing a region pass, report:

```text
Summary:
- What changed
- What systems were reused
- What region content was replaced

Version:
- versionCode
- versionName
- badge

Validation:
- Commands run
- APK path
- Phone test status

Git:
- Branch
- Commit hash
- Push status

Known issues:
- Remaining art issues
- Remaining tuning issues
- Remaining device-test risks
```

This handoff format keeps future regions from losing context.
