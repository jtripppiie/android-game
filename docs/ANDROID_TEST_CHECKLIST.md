# Android Test Checklist

Use this after GitHub Actions produces the debug APK artifact.

Current packaged beta:

```text
versionCode: 128
versionName: 1.2.8-beta
build badge: ALASKA BETA v1.2.8
```

## Install test

1. Download `you-rush-alaska-debug-apk` from the latest successful workflow run.
2. Extract the ZIP if GitHub downloaded it as a ZIP.
3. Install the `.apk` on an Android phone.
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
- Back buttons return to menu.
- Play starts the selected stage.

## Gameplay test

Test these controls:

- LEFT moves the character left.
- RIGHT moves the character right.
- JUMP bounces the character upward.
- THROW launches a snowball.
- CLIMB TREE appears only near the tree and changes the timing challenge.
- PAUSE opens help and resumes without spending a life.
- Normal screen tap still bounces the character.

## Survival system test

Confirm the beta loop behaves consistently:

- The run starts with three lives.
- Passing gates advances the progress HUD and checkpoint.
- Crashing with no shield spends one life.
- Respawn resumes from the latest checkpoint gate when lives remain.
- Losing the last life opens the run summary.
- Entering the Contra code enables unlimited lives.
- The unlimited-lives state is visible enough for tester confidence.

## Scoring test

Confirm score changes:

- Clearing antler gates increases score.
- Dodging hazards gives small score bumps.
- Near misses award score without requiring a collision.
- Combo streaks build from successful play and reset on mistakes.
- Hitting a boss with snowballs increases score.
- Defeating a boss gives a larger score bonus.
- Best score persists after restarting the app.

## Pickup and reward test

Confirm pickups are readable and useful:

- Bonus stars appear in normal play.
- Star collection increments the star count.
- Every third star grants an extra life when below the normal max.
- Aurora shield pickup appears.
- Shield pickup arms the shield state.
- Shield save prevents the next life loss.
- Shield feedback is clear enough to understand during motion.

## Mission and award test

Confirm end-of-run progression works:

- Mission objectives appear during a run.
- Mission score bonuses are applied on summary.
- Persistent mission totals survive an app restart.
- Run summary shows grade, XP, missions, records, and medals without overlap.
- Grades can range from F through S based on performance.
- Best grade persists.
- Bronze, silver, and gold medal awards trigger from the expected grade tiers.
- Persistent medal totals survive an app restart.
- Best gates and longest run records persist.

## Stage test

For each Alaska stage:

1. Start the stage from the Alaska Map.
2. Clear the required number of gates.
3. Confirm the boss phase starts.
4. Hit the boss until its HP reaches zero.
5. Confirm the Stage Clear screen appears.
6. Confirm the next stage unlocks.

Stage feel targets:

- Stage 1 is easy and readable.
- Stage 2 introduces more pickups.
- Stage 3 asks for better timing.
- Stage 4 feels darker and harder, but fair.
- Stage 5 feels like the big Alaska challenge.

## Layout and readability test

Check small and large screens:

- Top HUD elements do not overlap.
- Gate progress, timer, lives, stars, shield, missions, and warnings remain readable.
- Incoming hazard warnings do not hide the player or controls.
- Run summary content fits on small phones.
- Medals, grade, XP, missions, and records do not collide.
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
- Wrapper layers still rely on reflection in places; this is acceptable for beta, but should be reduced during 1.3.0 stabilization.

## First tuning targets

- Gate gap size
- Gravity and jump height
- D-pad button size
- Boss HP
- Hazard spawn rate
- Bear Country difficulty
- Stage-specific pickup frequency
- Summary screen vertical spacing
