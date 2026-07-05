# Android Test Checklist

Use this after GitHub Actions produces the debug APK artifact.

Current packaged beta:

```text
versionCode: 184
versionName: 1.8.4-alpha
build badge: ALASKA ART v1.8.4
```

## Install test

1. Download `you-rush-alaska-debug-apk` from the latest successful workflow run.
2. Extract the ZIP if GitHub downloaded it as a ZIP.
3. Install `you-rush-alaska-1.8.4-alpha-184-debug.apk` on an Android phone.
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
- Combo streaks build from successful play and reset on mistakes.
- Hitting a boss with snowballs increases score.
- Defeating a boss gives a larger score bonus.
- Best score persists after restarting the app.
- XP and level persist after restarting the app.

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
- Stage 5 feels like the big Alaska challenge.

## Layout and readability test

Check small and large screens:

- Top HUD elements do not overlap.
- Hurdle progress, lives, combo, XP level, score, and best score remain readable.
- Boss health bar never overlaps the top HUD.
- Game-over and stage-clear content fits on small phones.
- Button labels and touch targets remain usable in portrait orientation.

## Debug test

On the main menu:

1. Toggle DEBUG on.
2. Start a stage.
3. Confirm the overlay shows state, score, boss status, player position, shots, hazards, and recent events.
4. Toggle DEBUG off and confirm the overlay hides.

## Current known rough edges

- Boss attack patterns are placeholder-simple.
- Hazards are placeholder assets and labels.
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
