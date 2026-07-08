# Alaska Stage Intro Test

Version:

```text
ALASKA DEV v0.2.7-alpha
versionCode 9
```

## What changed

This build adds a short stage briefing overlay when a run starts.

The overlay shows:

- Alaska stage label
- Stage name
- Gate goal
- Boss name
- Basic control reminder

## What to test

1. Open the app.
2. Start an Alaska stage.
3. Confirm the intro panel appears briefly.
4. Confirm it shows the selected stage name.
5. Confirm it mentions the gate goal and boss.
6. Confirm gameplay continues normally behind/after the overlay.
7. Confirm combo, near-miss, tree, snowball, and boss mechanics still work.
8. Confirm the badge says `ALASKA DEV v0.2.7-alpha`.

## Tuning notes

If the overlay feels too long, reduce `INTRO_SECONDS`.

If players miss the information, increase `INTRO_SECONDS` or add a real countdown later.
