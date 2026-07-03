# Android Test Checklist

Use this after GitHub Actions produces the debug APK artifact.

## Install test

1. Download `you-rush-alaska-debug-apk` from the latest successful workflow run.
2. Extract the ZIP if GitHub downloaded it as a ZIP.
3. Install the `.apk` on an Android phone.
4. Allow installation from the browser/files app if Android prompts you.
5. Open **You Rush**.

## Smoke test

Confirm these screens work:

- Splash screen appears.
- Main menu appears after splash or tap.
- Alaska Map opens.
- Customize opens.
- Photo picker opens from Customize.
- Back buttons return to menu.
- Play starts the selected stage.

## Gameplay test

Test these controls:

- LEFT moves the character left.
- RIGHT moves the character right.
- JUMP bounces the character upward.
- THROW launches a snowball.
- Normal screen tap still bounces the character.

## Scoring test

Confirm score changes:

- Clearing antler gates increases score.
- Dodging hazards gives small score bumps.
- Hitting a boss with snowballs increases score.
- Defeating a boss gives a larger score bonus.
- Best score persists after restarting the app.

## Stage test

For each Alaska stage:

1. Start the stage from the Alaska Map.
2. Clear the required number of gates.
3. Confirm the boss phase starts.
4. Hit the boss until its HP reaches zero.
5. Confirm the Stage Clear screen appears.
6. Confirm the next stage unlocks.

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

## First tuning targets

- Gate gap size
- Gravity and jump height
- D-pad button size
- Boss HP
- Hazard spawn rate
- Bear Country difficulty
