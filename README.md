# You Rush: Alaska

You Rush is a local-first Android platform runner built with Godot 4.7.1.
Across five Alaska stages, the player runs, jumps, uses one air jump, dashes,
throws snowballs, finds a key, rescues two people, defeats a distinct boss, and
reaches the finish beacon.

## Current source

```text
engine: Godot 4.7.1
versionCode: 531
versionName: 5.3.1
package: com.jtripppiie.mooserush
APK status: compiled and package-inspected; device acceptance pending
APK: app/build/outputs/apk/debug/you-rush-alaska-5.3.1-debug.apk
```

Version 5.3.1 contains the 5.3 control/movement overhaul and fixes duplicated
stage worlds/runners after map transitions plus an extra stacked Moose Pass
bear. It includes high-contrast viewport-relative touch controls, automatic
mobile running, higher jumping, and full-size menus. Source validation, touch
and lifecycle regression tests, and a five-stage traversal passed. The APK was
exported with Godot 4.7.1 and signed with the same debug certificate as 5.3.0.
It has not yet passed the physical-device checklist.

## Controls

- Left/right: move
- Run: sprint
- Jump: jump, then jump once more in the air
- Dash: short horizontal burst
- Crouch while airborne: stomp
- Fire: throw a snowball
- Pause: pause the run; Exit to Map is a separate deliberate action
- Note/IDs: available only when Review Mode is enabled

Touch controls support simultaneous movement and jumping.

## Review Mode

Enable `Accessibility > Review Mode · IDs + Notes` to display nearby authored
identifiers and the compact review notebook. Normal play hides these tools.

## Project layout

- `godot/`: primary game, Android export preset, scripts, scenes, and assets
- `docs/`: current owner, tester, privacy, release, and maintenance guidance
- `docs/archive/`: superseded Java-era and old release/work logs
- `app/`: legacy Android project and output location used by Godot exports
- `verification/`: retained legacy sprite snapshots, not current acceptance
- `tools/`: offline legacy inspection pages

## Validate and build

```bash
python3 godot/validate_project.py
godot --headless --path godot -- --touch-audit
godot --headless --path godot -- --autoplay-audit=0
godot --headless --path godot --export-debug "Android Debug"
```

Repeat the traversal audit for stages `0` through `4`. Android export requires
Godot 4.7.1, matching export templates, Java 17, Android SDK 36, and normal
local socket access for Gradle.

Before handing over a build, complete
[the device acceptance checklist](docs/DEVICE_ACCEPTANCE_CHECKLIST.md).

## Current APK

The current compiled and package-inspected candidate is
`app/build/outputs/apk/debug/you-rush-alaska-5.3.1-debug.apk`.

SHA-256:

```text
560048bde263fd247df59a25d9c9e24f046e2bf070609d31c64306d9e56dfd22
```

Version 5.3.0 remains the local rollback while 5.3.1 completes the physical
device checklist. “Package-inspected” does not mean visually accepted.
