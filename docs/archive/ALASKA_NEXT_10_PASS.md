# Alaska Next 10 Pass

Version:

```text
ALASKA DEV v0.3.0-alpha
versionCode 10
```

## Implemented in this pass

1. Wired the incoming-warning layer into the active app view.
2. Added right-edge incoming callouts for active Alaska obstacles.
3. Added a pulsing right-edge alert rail.
4. Added an `INCOMING xN` count pill.
5. Limited stacked callouts so the UI does not flood.
6. Added animated pulse feedback to warning pills.
7. Kept the stage intro, combo, near-miss, tree, and snowball layers underneath the warning layer.
8. Bumped the visible dev badge to `ALASKA DEV v0.3.0-alpha`.
9. Added a test checklist for the readability pass.
10. Preserved the privacy/local-first structure: no accounts, no uploads, no cloud storage.

## What to test

1. Start each Alaska stage.
2. Confirm the intro overlay still appears.
3. Confirm incoming callouts appear on the right side before obstacles reach the player.
4. Confirm the right-edge alert rail pulses.
5. Confirm `INCOMING xN` appears when one or more incoming objects are visible.
6. Confirm combo, near-miss, tree, snowball, boss, and stage clear behavior still work.
7. Confirm the badge says `ALASKA DEV v0.3.0-alpha`.

## Next suggested pass

The next useful pass should be balancing:

- Gate spacing
- Gravity and jump height
- Obstacle speed
- Tree cooldown
- Snowball cooldown
- Boss health
