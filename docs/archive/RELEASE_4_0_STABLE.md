# You Rush 4.0 Stable Release

You Rush 4.0 is the first stable gameplay release of the local-first Alaska
arcade platformer. This is not a beta rename: it expands the campaign structure
and adds authored regional finales that test the mechanics learned in each run.

## Build Identity

```text
versionName: 4.0.0
versionCode: 400
channel: ALASKA RELEASE
badge: ALASKA PASSPORT v4.00
debug APK: app/build/outputs/apk/debug/you-rush-alaska-4.0.0-400-debug.apk
```

## New 4.0 Run Structure

- Stage lengths increase from 8–16 gates to 10–22 gates.
- The final quarter switches to a region-specific Mastery Gauntlet.
- Each gauntlet offers simultaneous upper ring/launch and lower brittle routes.
- The seeded director keeps finales regional, reproducible, and testable.

## Complete Stable Scope

The release includes FLOW risk/reward, authored multi-path encounters, variable
jumping and aiming, snowballs and spray, launch pads, aerial rings, stomp
chains, supply blocks, reactive freeze-water bridges, scaled wildlife,
environmental interactions, and multi-phase telegraphed bosses.

## Release Gates

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
python3 godot/validate_project.py
git diff --check
```

Before public distribution, complete all stages on a phone and emulator,
exercise both mastery routes, verify every boss/laser, restore local progress,
and run for 15 minutes without a crash or input lock. The debug APK is for
direct testing; store publication still requires the owner's release key.
