# You Rush

**You Rush** is a local-first Android arcade runner by **TripperDeeLabs**.

Players choose an Alaska stage, optionally personalize the runner with a local
photo, then move, jump, throw snowballs, collect rewards, and survive regional
wildlife encounters through stage runs and boss fights.

## Current Build

```text
versionCode: 377
versionName: 3.9.0-beta
build badge: ALASKA PASSPORT v3.77 BETA
APK: app/build/outputs/apk/debug/you-rush-alaska-3.9.0-beta-377-debug.apk
```

This is a beta package for device testing and gameplay tuning. It is not a
signed public-store release.

## Gameplay

- Five Alaska stages with distinct obstacles, hazards, bosses, and seasonal
  presentation.
- Platform-runner controls: left-thumb D-pad movement/aim, jump, double jump,
  fire, and spray.
- Snowballs can damage bosses, clear specific threats, and destroy Salmon Rush
  river logs.
- Bear spray is a scarce pickup with its own SPRAY button to stun close wildlife
  and interrupt close boss lunges.
- Clean vault streaks trigger FLOW, a short momentum state with bonus scoring,
  faster world movement, faster controls and snowballs, denser encounters,
  pickup pull, HUD feedback, and a runner aura. Near misses extend FLOW.
- Runs cycle through authored encounter beats—Launch, Precision, Wildlife Rush,
  and Jackpot Line—with distinct pacing and curved multi-star trails.
- Customize lets players choose a photo/default body style, including female
  and male runner bodies.
- Progression includes stage unlocks, XP, Trail Tokens, Daily Rush, Trail
  Passport badges, Expedition Logs, cosmetics, combo scoring, near-miss rewards,
  and local best scores.

## Controls

- `D-PAD`: move left/right and aim snowballs high, low, or diagonally
- `JUMP`: jump, with one air jump available
- `FIRE`: tap for snowball
- `SPRAY`: use bear spray charges
- `PAUSE`: pause, resume, return to map, or edit the runner sprite

## Build And Test

Open the project in Android Studio, or use the Gradle wrapper:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

For the simplest local emulator install and launch:

```bash
tools/install-local-debug.sh
```

This reads `local.properties`, adds the Android SDK tools for that command, runs
`app:installDebug`, and opens You Rush on the connected emulator/device.

Debug APKs are generated under:

```text
app/build/outputs/apk/debug/
```

GitHub Actions also publishes the debug APK as the
`you-rush-alaska-debug-apk` artifact.

## Debug Tools

Local browser previews live in `tools/` and are not packaged into the app.

- `index.html`: local launcher for every offline debug preview
- `offline-debug-workbench.html`: sprite crops, alpha bounds, connected
  components, runner foot/contact lines, runtime strips, gameplay composition,
  and PNG snapshots
- `menu-preview.html`: main menu layout review with 15 px grid overlay
- `gameplay-preview.html`: HUD, overlays, controls, hitboxes, contrast, and 15
  px grid overlay
- `platformer-overhaul-preview.html`: camera, terrain, new runner, chase-bear
  visibility, translucent controls, and numbered 15 px grid overlay
- `laser-eyes-preview.html`: polar bear boss beam origin and sweep tuning with
  15 px grid overlay
- `sun-lasers-preview.html`: Midnight Sun twin-eye beam, glow, hitbox, sweep,
  and runner-scale tuning with a default-on 15 px grid overlay
- `gear-obstacle-preview.html`: logs, snow piles, icebergs, snowballs, bear
  spray, hitboxes, numbered debug badges, and 15 px grid overlay
- `sprite-sheet-audit.html`: actual PNG sprite sheets with frame numbers,
  checkerboard transparency, trim boxes, runtime crop boxes, and 15 px grid
  overlay
- `debug-tuning-dashboard.html`: reporting guide for numbered DEBUG screenshots

## Documentation

Start with [docs/README.md](docs/README.md).

Key active docs:

- [Versioning](docs/VERSIONING.md)
- [Android test checklist](docs/ANDROID_TEST_CHECKLIST.md)
- [Gameplay revamp contract](docs/GAMEPLAY_REVAMP.md)
- [Godot overhaul vertical slice](docs/GODOT_OVERHAUL.md)
- [Offline debug previews](docs/OFFLINE_DEBUG_PREVIEWS.md)
- [Privacy notes](docs/PRIVACY.md)
- [Store listing draft](docs/STORE_LISTING_DRAFT.md)
- [Sprite sheet asset pipeline](docs/SPRITE_SHEET_ASSET_PIPELINE.md)
- [Region replication playbook](docs/REGION_REPLICATION_PLAYBOOK.md)

Historical release notes and rapid-iteration logs are preserved in
[docs/archive](docs/archive/).

## Architecture

`MainActivity` hosts a single custom Android view:

```java
new MooseRushView(this)
```

Pure gameplay math and tuning helpers live in small Java classes with fast JVM
unit coverage. `BossStateMachine` owns pure boss transition decisions,
`StageBossRules` owns stage-specific boss strategy routing, and
`ReusableObjectPool` recycles high-frequency particles and score popups.
Rendering, input, persistence, and moment-to-moment gameplay remain coordinated
by `MooseRushView`.

## Privacy

The current beta is local-first: no account, no normal-play network dependency,
and no image upload. Optional photo personalization uses Android's system picker
and decodes the selected image locally.
