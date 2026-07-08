# Alaska Combo Test

Version:

```text
ALASKA DEV v0.2.6-alpha
versionCode 8
```

## What changed

This build adds combo streaks.

Any quick score gains can extend the combo:

- Gate clears
- Near misses
- Snowball hazard interactions
- Boss hits

## Current behavior

- Combo window: about 2.15 seconds.
- Combo HUD appears at combo x2 or higher.
- Every third combo awards a small +5 bonus.
- The combo resets when the score does not keep moving quickly enough.
- The combo resets when the game is no longer actively running.

## What to test

1. Start an Alaska stage.
2. Score two events quickly and confirm `COMBO x2` appears.
3. Score three events quickly and confirm the +5 bonus appears.
4. Confirm the combo bar drains after a short delay.
5. Confirm near-miss, tree, snowball, and boss mechanics still work.
6. Confirm the badge says `ALASKA DEV v0.2.6-alpha`.

## Tuning notes

If combos feel too hard, increase `COMBO_WINDOW_SECONDS`.

If combos feel too generous, lower `COMBO_WINDOW_SECONDS` or raise `COMBO_BONUS_EVERY`.
