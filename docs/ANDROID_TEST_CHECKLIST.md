# Android Test Checklist

Use this after GitHub Actions produces the debug APK artifact.

Current packaged beta:

```text
versionCode: 196
versionName: 1.9.6-alpha
build badge: ALASKA PASSPORT v1.9.6
```

## Install test

1. Download `you-rush-alaska-debug-apk` from the latest successful workflow run.
2. Extract the ZIP if GitHub downloaded it as a ZIP.
3. Install `you-rush-alaska-1.9.6-alpha-196-debug.apk` on an Android phone.
4. Allow installation from the browser/files app if Android prompts you.
5. Open **You Rush**.
6. Confirm the debug/version badge is visible and matches the packaged beta.

## Smoke test

Confirm these screens work:

- Splash screen appears.
- Main menu appears after splash or tap.
- Alaska Map opens.
- Customize opens.
- Photo picker opens from Customize.
- A picked player photo appears in-game.
- The selected player photo restores after closing and reopening the app.
- Outfit color selection persists after closing and reopening the app.
- Back buttons return to menu.
- Play starts the selected stage.
- Daily Rush starts the rotating daily stage.
- Main menu shows Trail Passport badge progress.

## Gameplay test

Test these controls:

- LEFT moves the character left.
- RIGHT moves the character right.
- JUMP bounces the character upward.
- A second JUMP works once while airborne.
- FIRE launches a snowball.
- Normal screen tap also requests a jump.

## Survival system test

Confirm the beta loop behaves consistently:

- The run starts with three lives.
- Passing hurdles advances the progress HUD and checkpoint.
- Crashing with no shield spends one life.
- Respawn resumes safely when lives remain.
- Losing the last life opens game over.

## Scoring test

Confirm score changes:

- Clearing antler hurdles increases score.
- Collecting stars increases score and the HUD star count.
- Dodging hazards gives small score bumps.
- Combo streaks build from successful play, unlock score multipliers, and reset on mistakes.
- Tight dodges can trigger a near-miss score popup without feeling unfair.
- Run missions appear before/during runs and report progress on result screens.
- Shield pickups appear, activate an obvious aura, and absorb one hit.
- Aurora Rush meter fills from skilled play and triggers a readable score-burst mode.
- Trail Tokens are awarded on game over or stage clear and persist after restarting the app.
- Daily Rush awards its bonus once per local day, shows the streak on result screens, and persists after restarting the app.
- Trail Passport badges unlock from skill/progression moments and add token rewards only once.
- Hitting a boss with snowballs increases score.
- Defeating a boss gives a larger stage-clear bonus with combo and star rewards.
- Best score persists after restarting the app.
- XP and level persist after restarting the app.
- Cosmetic outfit unlocks spend Trail Tokens only and never change gameplay power.
- Trail Passport badge count and badge token rewards persist after restarting the app.

## Stage test

For each Alaska stage:

1. Start the stage from the Alaska Map.
2. Clear the required number of hurdles.
3. Confirm the boss phase starts.
4. Hit the boss until its HP reaches zero.
5. Confirm the Stage Clear screen appears.
6. Confirm the next stage unlocks.

Stage feel targets:

- Stage 1 is easy and readable.
- Stage 2 gets the player to the boss quickly.
- Stage 3 asks for better timing.
- Stage 4 feels darker and harder, but fair.
- Stage 5 feels like the big Alaska challenge with bear, polar bear, and wolf variety.
- Brown bears and polar bears occasionally stand on hind legs and roar as a readable warning.

## Layout and readability test

Check small and large screens:

- Top HUD elements do not overlap.
- Hurdle/boss progress, lives, combo multiplier, XP level, score, run score, and best score remain readable.
- Boss incoming and combo callout overlays appear without covering the player.
- The ready screen shows goal, boss, and bonus briefing chips.
- Boss health bar never overlaps the top HUD.
- Game-over and stage-clear content fits on small phones.
- Button labels and touch targets remain usable in portrait orientation.
- Customize shows locked outfit prices, unlocked outfits, and current token count.
- Generated background plates carry the mountains; old vector-style mountain layers should not appear.
- Winter/dark stages show denser snow-covered tree layers without jumpy resets.
- Trees should visually connect to the ground and not float above the lane.
- Polar bear and wolf sprites animate cleanly without magenta background artifacts.
- Moose/polar/default bosses should be grounded; eagle/salmon bosses may fly or swim but should have grounded shadows and constrained lanes.
- Bosses should visibly advance toward the player and retreat instead of parking at the far right.
- Eagle wings flap at a natural pace instead of strobing.
- Bear and polar bear movement stays planted instead of jumping vertically.
- Animal sprites do not have decorative halo/circle glows around them.
- Double-jumps have enough headroom after the ground/play area shift.

## Debug test

On the main menu:

1. Toggle DEBUG on.
2. Start a stage.
3. Confirm the overlay shows state, score, boss status, player position, shots, hazards, and recent events.
4. Toggle DEBUG off and confirm the overlay hides.

## Current known rough edges

- Boss attack patterns are placeholder-simple.
- Some encounter labels and boss behavior are still simple; runtime hazards now use generated raster/sprite-sheet art with frame trim guards.
- Photo placement is automatic; crop/position controls are not built yet.
- Map unlock logic exists, but the map still displays every Alaska stage for easy inspection.
- Difficulty is not balanced yet.
- Generated tone SFX are functional placeholders; richer audio assets are still needed.

## First tuning targets

- Hurdle spacing
- Gravity and jump height
- D-pad button size
- Boss HP
- Hazard spawn rate
- Bear Country difficulty
- Stage-specific reward frequency
- Summary screen vertical spacing
