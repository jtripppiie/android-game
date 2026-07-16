# You Rush 4.1.1 In-game Review Notebook

Version 4.1.1 adds a persistent field-review workflow to the 4.1 debug tuning
overlay. A tester can pause mid-run, type a note with the Android keyboard, and
resume without losing the exact gameplay context.

## Build Identity

```text
versionName: 4.1.1
versionCode: 411
channel: ALASKA RELEASE
badge: ALASKA PASSPORT v4.11
debug APK: app/build/outputs/apk/debug/you-rush-alaska-4.1.1-411-debug.apk
```

## Review Flow

1. Play in the debug APK with stable IDs visible.
2. Tap `NOTE`; the run freezes immediately without drawing the normal pause panel.
3. Type a multiline observation and optionally check `Priority fix`.
4. Tap `Save`; the entry is appended locally and the run resumes.
5. Open `NOTE` later and use `Copy all` to export the complete log.

## Captured Context

Every entry includes a timestamp, stage, encounter recipe, gate progress,
score, deterministic run seed, and the stable IDs currently visible. The note
count appears on the `NOTE` button. Empty notes are rejected instead of adding
noise to the log.

Notes are stored in the app-private `debug-review-notes.txt` file. They do not
require network access and are never uploaded by the game.

The editor is a compact right-side card capped at 42 percent of the display
width with minimal background dimming. The paused scene and item identifiers
remain visible beside it while the note is written.

## Validation

Pure JVM coverage verifies review-entry formatting and preservation of stable
item IDs. Release validation also runs Android compilation, lint, APK assembly,
Godot structural validation, and `git diff --check`.
