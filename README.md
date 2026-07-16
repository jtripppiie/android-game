# You Rush: Alaska

You Rush is a local-first Android platform runner built with Godot 4.7.1.
Across five Alaska stages, the player runs, jumps, uses one air jump, dashes,
throws snowballs, finds a key, rescues two people, defeats a distinct boss, and
reaches the finish beacon.

## Current source

```text
engine: Godot 4.7.1
versionCode: 522
versionName: 5.2.2
package: com.jtripppiie.mooserush
APK status: build pending
last compiled APK: app/build/outputs/apk/debug/you-rush-alaska-5.2.1-debug.apk
```

Version 5.2.2 replaces the broken fixed-coordinate mobile overlay with large,
high-contrast, viewport-relative touch controls. Source validation, the
multi-touch regression test, and a five-stage traversal passed. Its APK export
is pending because the build environment could not start Gradle after two
approval timeouts. Do not distribute a file labeled 5.2.2 until it has been
compiled and inspected.

## Controls

- Left/right: move
- Run: sprint
- Jump: jump, then jump once more in the air
- Dash: short horizontal burst
- Crouch while airborne: stomp
- Fire: throw a snowball
- Map: safely leave the stage
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

## Last accepted APK

The most recent compiled and inspected package is
`app/build/outputs/apk/debug/you-rush-alaska-5.2.1-debug.apk`.

SHA-256:

```text
5dc4884d683eafe192b8170474034f4a6fb02f93a4f08a2f3226b864677c66cf
```

Keep it as the rollback build until 5.2.2 is compiled and passes the physical
device checklist.
