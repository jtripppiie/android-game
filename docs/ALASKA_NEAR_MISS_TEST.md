# Alaska Near-Miss Test

Version:

```text
ALASKA DEV v0.2.5-alpha
versionCode 7
```

## What changed

This build adds near-miss rewards.

When the player passes very close to a hazard without colliding, the game now awards a small bonus and shows a popup.

Current behavior:

- Near miss range: about 44dp beyond the normal collision edge.
- Bonus: `+3 NEAR MISS`.
- Haptic tick feedback fires when supported.
- Each hazard can only award the near-miss bonus once.

## What to test

1. Start an Alaska stage.
2. Dodge close to salmon, moose, bear, or dark hazards.
3. Confirm `+3 NEAR MISS` appears.
4. Confirm score increases by 3.
5. Confirm the bonus does not spam repeatedly on the same hazard.
6. Confirm normal scoring, snowballs, tree climb, and boss behavior still work.
7. Confirm the badge says `ALASKA DEV v0.2.5-alpha`.

## Tuning notes

If near misses feel too rare, increase `NEAR_MISS_RANGE_DP`.

If near misses feel spammy, reduce the scoring window or reduce the range.
