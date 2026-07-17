# You Rush: Alaska

You Rush is a local-first Android platform runner built with Godot 4.7.1. Five
authored Alaska stages ask the player to find a key, rescue two people, defeat
one distinct wildlife boss, and reach the trail beacon.

## Current production source

```text
source: godot/
engine: Godot 4.7.1
versionCode: 541
versionName: 5.4.1
package: com.jtripppiie.mooserush
device APK: app/build/outputs/apk/debug/you-rush-alaska-5.4.1-debug.apk
```

`app/src/main/` is the legacy Java game retained for rollback and save
migration. It is not the current game and must not be used for production
gameplay changes.

Version 5.4.1 adds more responsive reversal and air momentum, a much stronger
short/full jump distinction, touch drift protection, exact enemy contact,
stage-specific boss tells and fair recovery windows, grounded full-height
checkpoints, safer objective/wildlife/boss spacing, a clearer HUD and exit
beacon, and deeper fairness telemetry across all five stages. It builds on
5.4.0's separated player scene, camera, effects, verification, review IDs,
result flows, measured runner grounding, parallax, snow contact effects, soft
terrain joins, profile backups, and score/time/no-damage stars. Android export
now produces one ARM64 device APK only.

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
```

`Android Debug` is the only APK preset and targets ARM64 phones/tablets. It is
debug-signed; a public release needs the owner’s private release key.

## Documentation and evidence

- [Gameplay verification](test-results/android-gameplay/GAMEPLAY-VERIFICATION.md)
- [Kid owner handbook](docs/KID_OWNER_HANDBOOK.md)
- [Device acceptance checklist](docs/DEVICE_ACCEPTANCE_CHECKLIST.md)
- [Technical maintenance](docs/TECHNICAL_MAINTENANCE.md)
- [Privacy](docs/PRIVACY.md)
- [5.4 release notes](docs/RELEASE_5_4_PRODUCTION_REFACTOR.md)
- [5.4.1 gameplay refinement](docs/RELEASE_5_4_1_GAMEPLAY_REFINEMENT.md)

Emulator/headless evidence does not replace testing touch ergonomics, haptics,
speech recognition, interruptions, thermals, or performance on a physical
Android device.
