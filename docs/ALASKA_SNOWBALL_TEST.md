# Alaska Snowball Test

This build makes snowballs interact with normal Alaska hazards, not only bosses.

## Version

```text
ALASKA DEV v0.2.3-alpha
versionCode 5
```

## What changed

`AlaskaSurvivalMooseRushView` now checks overlap between active snowballs and active hazards.

Current behavior:

- Salmon-type hazards can be cleared.
- Moose, bear, and dark hazards are pushed farther away.
- The player receives a small score bonus.
- A popup appears, such as `+8 CLEARED` or `+6 SLOWED`.
- The existing boss snowball behavior remains in the core gameplay loop.

## What to test

1. Install the latest APK.
2. Start an Alaska stage.
3. Use the throw control when normal hazards are visible.
4. Confirm snowballs visibly affect hazards.
5. Confirm score increases.
6. Confirm the popup text appears.
7. Confirm boss snowball behavior still works.
8. Confirm the badge says `ALASKA DEV v0.2.3-alpha`.

## Notes

This is still a prototype wrapper around the main gameplay file. Once the mechanic feels right, move the hazard interaction into the core game loop directly.
