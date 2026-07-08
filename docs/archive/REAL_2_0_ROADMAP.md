# Real 2.0 Roadmap

The current Alaska build is not final 2.0 yet.

It is a stronger alpha with many 2.0-style systems, but a real 2.0 needs more time, build validation, playtesting, balancing, and cleanup.

## Current honest version

```text
versionCode 11
versionName 0.4.0-alpha
badge ALASKA ALPHA v0.4.0
```

## What already exists

- Alaska-first game direction
- Five Alaska stages
- Photo personalization
- Touch controls
- Stage intro overlay
- Tree timing mechanic
- Snowball interactions
- Near-miss rewards
- Combo streaks
- Incoming callouts
- Debug overlay
- Local saved progress
- Android debug APK workflow

## What real 2.0 still needs

### 1. Build validation

- Confirm GitHub Actions builds the latest APK.
- Download and install the APK.
- Fix all compile errors and runtime crashes.

### 2. Device playtesting

- Test on a real Android phone.
- Test portrait layout on small and large screens.
- Test repeated app pause/resume.
- Test photo picker permissions.
- Test saved photo restore.

### 3. Gameplay balance

- Tune movement speed.
- Tune jump height.
- Tune gravity.
- Tune gate spacing.
- Tune obstacle spacing.
- Tune tree timing.
- Tune snowball cooldown.
- Tune combo scoring.
- Tune stage difficulty curve.

### 4. Architecture cleanup

The current wrapper layers let features be added quickly, but final 2.0 should fold proven systems into cleaner core gameplay classes.

Needed cleanup:

- Reduce reflection usage.
- Move stable mechanics into typed game state.
- Separate rendering from update logic.
- Add clear model classes for stages, hazards, shots, and player state.
- Keep debug tools isolated from player-facing systems.

### 5. UI polish

- Better title screen layout.
- Better stage select cards.
- Better stage clear screen.
- Better retry flow.
- Better pause/settings flow.
- Better accessibility labels.
- Optional vibration toggle.

### 6. Art and sound

- Replace placeholder vector art where needed.
- Add simple sound effects.
- Add stage clear sound.
- Add combo feedback sound.
- Add optional mute toggle.

### 7. Release packaging

- Disable visible alpha badge.
- Add final privacy policy URL.
- Add final screenshots.
- Add final store listing.
- Use release signing.
- Tag the release.

## Minimum bar for calling it 2.0

Do not call the game 2.0 until:

1. The latest APK builds.
2. The APK installs on a real device.
3. All five stages are playable.
4. No obvious crash exists in a 15-minute test session.
5. Controls feel responsive.
6. Difficulty feels fair.
7. The game has a clean start, retry, and stage-clear loop.
8. The player can understand what to do without developer explanation.
