# Alaska Tree Context Test

Version:

```text
ALASKA DEV v0.2.4-alpha
versionCode 6
```

## What changed

The tree button is now contextual.

- The tree still appears in the play area.
- The climb button only appears while the player is close enough, currently about 108dp.
- The button is positioned near the tree instead of permanently sitting in the lower center.
- Large hazards near the tree shorten the escape window and shake the tree.
- A `TREE HIT` popup appears when the tree escape gets disrupted.

## Test steps

1. Start an Alaska stage.
2. Move away from the tree and confirm the climb button is hidden.
3. Move close to the tree and confirm the climb button appears.
4. Tap the climb button and confirm the player moves to the branch.
5. Let large hazards approach while climbing and confirm the timer gets shorter.
6. Confirm the tree visually shakes.
7. Confirm the badge says `ALASKA DEV v0.2.4-alpha`.
