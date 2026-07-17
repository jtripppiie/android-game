# You Rush: Alaska

You Rush is a local-first Android platform runner built with Godot 4.7.1. Five
authored Alaska stages ask the player to find a key, rescue two people, defeat
one distinct wildlife boss, and reach the trail beacon.

## Current production source

```text
source: godot/
engine: Godot 4.7.1
versionCode: 540
versionName: 5.4.0
package: com.jtripppiie.mooserush
device APK: app/build/outputs/apk/debug/you-rush-alaska-5.4.0-debug.apk
emulator APK: app/build/outputs/apk/debug/you-rush-alaska-5.4.0-x86_64-debug.apk
```

`app/src/main/` is the legacy Java game retained for rollback and save
migration. It is not the current game and must not be used for production
gameplay changes.

Version 5.4.0 separates the player scene, presentation, camera, effects,
verification, review IDs, and completion/game-over flows; fixes runner
grounding to a measured subpixel contract; adds parallax, snow contact effects,
soft terrain joins, a larger premium HUD and menu; strengthens profile backups;
adds score/time/no-damage stars, saved touch sizing, and deterministic Android
verification scenarios. The Android export is
split by architecture so the ARM64 device APK no longer carries an unused
x86_64 engine.

## Controls

- Left/right: move; touch movement automatically sprints.
- Jump: release early for a short jump; tap once more in air for one air jump.
- Dash: short horizontal burst.
- Crouch while airborne: stomp.
- Snow: throw a snowball.
- Pause: freeze the run; restart and Exit to Map are explicit choices.
- Review Mode: optional compact IDs and field notes, hidden in normal play.

## Validate

Use the exact supported audit suite:

```bash
python3 godot/validate_project.py
GODOT_BIN=/path/to/godot godot/tools/run_gameplay_audits.sh
```

The script runs touch, system, lifecycle, pause, mechanics, geometry,
debug-overlay, and autoplay audits for stages 0–4. A successful process exit
alone is not enough: the validator also rejects script failures found in
Godot’s output.

## Build Android

Godot 4.7.1 with matching export templates, Java 17, Android SDK/target 36,
Build Tools 36.1.0, and NDK 29 are required.

```bash
godot --headless --path godot --export-debug "Android Debug"
godot --headless --path godot --export-debug "Android Emulator Debug"
```

`Android Debug` is ARM64 for phones/tablets. `Android Emulator Debug` is
x86_64 and is test-only. Both are debug-signed; a public release needs the
owner’s private release key.

## Documentation and evidence

- [Gameplay verification](test-results/android-gameplay/GAMEPLAY-VERIFICATION.md)
- [Kid owner handbook](docs/KID_OWNER_HANDBOOK.md)
- [Device acceptance checklist](docs/DEVICE_ACCEPTANCE_CHECKLIST.md)
- [Technical maintenance](docs/TECHNICAL_MAINTENANCE.md)
- [Privacy](docs/PRIVACY.md)
- [5.4 release notes](docs/RELEASE_5_4_PRODUCTION_REFACTOR.md)

Emulator/headless evidence does not replace testing touch ergonomics, haptics,
speech recognition, interruptions, thermals, or performance on a physical
Android device.
