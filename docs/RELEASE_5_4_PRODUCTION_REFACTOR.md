# Release 5.4 — Production Refactor

Version 5.4.0 is a structural and presentation release, not a content reset.
The five-stage objective contract remains: key, two rescues, one boss, goal.

## Player and game flow

- Runner collision/art/camera are now an editable `player.tscn`.
- Six animation foot lines are measured and aligned within 0.21 px of the
  capsule ground line; landing shadow and snow bursts clarify contact.
- Camera look-ahead preserves a level playfield. Deterministic trauma is capped
  and fully disabled by Reduced Motion.
- Health reaching zero opens a real game-over overlay. Restart and map exit
  cleanly dispose the previous world.
- Completion opens a score/objective result screen with next/replay/map.
- Dash preserves bounded vertical momentum instead of freezing the player.

## Interface and review

- HUD cards are taller with larger text, shadows, stronger borders, explicit
  objective states, an 8 px progress meter, and a route marker.
- Main menu has a responsive centered frame, 88 px primary actions, and clear
  title/subtitle/status hierarchy.
- Touch controls remain visible, high contrast, and multi-touch owned.
- Review identifiers are compact (`S3-PF04`), limited to four nearby items.
- Notes remain a 480×292 top-right panel instead of covering gameplay.

## World and presentation

- Terrain uses the full snow/ice/rock art, mipmapped filtering, and overlapping
  translucent crests to soften independent platform joins.
- Mountain and tree layers have separate parallax depth.
- Wildlife duplicate protection refuses spawns within 420 px.
- Bosses preserve distinct tells/attacks/recovery, face the player, and leave no
  hazards after defeat.

## Architecture, data, and Android

- Gameplay audits, review registration, result overlays, camera, effects, and
  presentation moved out of oversized controllers.
- Profile schema 3 adds backups, completion counts, best time/stars, and saved
  touch scale; local photos are bounded, cropped, resized, and cached.
- The debug-only Android harness exposes reproducible real-game scenarios.
- ARM64 device and x86_64 emulator exports are separate; version is 540/5.4.0.
- CI now validates and exports `godot/`, not the legacy Java application.

## Verification status

Headless validation and all five stage audits are required for the release.
The available Android 11 emulator installs and launches the APK, and preserves
the migration fixture across `adb install -r`. Its SwiftShader GLES 3 driver
cannot link Godot 4.7.1’s 261-vector canvas/scene shader against a 256-vector
limit, so rendered Android gameplay evidence on this host is a recorded FAIL,
not a fabricated PASS. Physical-device acceptance remains required.

See `test-results/android-gameplay/GAMEPLAY-VERIFICATION.md` for exact commands,
checksums, statuses, logs, and limitations.
