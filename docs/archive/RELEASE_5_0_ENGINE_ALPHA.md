# You Rush 5.0 Engine Alpha

This release begins the controlled replacement of the monolithic Java/Canvas
gameplay implementation with Godot 4.7.1.

## Playable migration milestone

- One complete handcrafted Chugach rescue stage.
- Deterministic movement, coyote time, buffered/variable jump, sprint, dash,
  crouch, stomp, snowball combat, checkpoints, and touch controls.
- High, precision, and low routes with moving/brittle platforms, freezable
  water, launch pads, Aurora rings, supply blocks, wolves, and bears.
- Required key, two survivor rescues, boss defeat, and gated rescue beacon.
- Timed score chains and persisted best score/clear state.
- Stable on-entity debug IDs and compact local review notebook.

## Build identity

```text
engine: Godot 4.7.1
versionCode: 500
versionName: 5.0.0-engine-alpha
package: com.jtripppiie.mooserush.overhaul
APK: app/build/outputs/apk/debug/you-rush-alaska-5.0.0-engine-alpha-debug.apk
```

The separate package is intentional. Reviewers can compare the engine alpha and
Java rollback build on the same Android device before the package cutover.

## Remaining cutover gates

- Physical-device input, aspect ratio, thermals, and frame pacing acceptance.
- Audio and haptic parity.
- Android speech-to-text note parity; compact typed notes are implemented now.
- Photo customization and existing local profile import.
- Four remaining authored Alaska stages and bosses.
- Accessibility review and release signing/store configuration.
