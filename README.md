# DIRT RUSH 🏍️

**Original retro motocross time-trial game for Android, made in Godot 4.**

Dirt Rush is a compact, entirely original, 8-bit-inspired arcade racer with smooth analog thumbstick movement based on the *control feel* of Captain Quack Cosmic Adventures. The artwork, course names, code, gameplay logic, and procedurally generated courses are original; no Nintendo or Excitebike assets, tracks, names, or music are used.

## Play

- **Left virtual joystick:** up/down changes between four track lanes. Left brakes on the ground; right speeds up. While airborne, left/right leans the bike to stabilize your landing.
- **Right BOOST button:** hold for turbo; release to cool your engine. An overheated engine must cool before boost reactivates.
- Hit ramps for air, avoid rocks, recover from crashes, and finish the time trial.
- 6 increasingly challenging courses with seeded layouts, local best times, unlock progression, and medals.
- Pause/resume. No login, ads, analytics, network requests, or purchases in the game.

**Desktop/testing:** WASD or arrow keys to steer, Space or Shift for boost, P/Esc for pause, Enter to start/advance, R to restart.

## Development status

Playable first-pass Godot source. It needs physical-device QA, final sound/music, store art, accessibility review, and signed Android release testing before commercial launch. The intention is a **$0.99, one-time paid download**, not an in-app purchase.

## Run locally

1. Open this folder using **Godot 4.3+** (Godot 4.4/4.5/4.6 recommended).
2. Press **F6** on `scenes/game.tscn`, or **F5** for the project.
3. Android export requires Android SDK, Java SDK, and Godot's Android export templates; see [Android notes](docs/ANDROID.md).

The whole first version is drawn programmatically with pixel-sized shapes in a 480×270 canvas. There are no external graphics or fonts to install.

## Project layout

- `scripts/game.gd` — main game loop, retro drawing, controls, HUD, persistence.
- `scripts/race_rules.gd` — repeatable track generation, heat and speed logic.
- `scenes/game.tscn` — tiny scene entry point.
- `tests/race_rules_test.gd` — headless logic test; see command below.

```bash
godot --headless --path . --script res://tests/race_rules_test.gd
```

## Commercial note

Keep the game visually and mechanically distinct from Nintendo's Excitebike; don't use third-party sprites, audio, copied tracks or Nintendo branding. Check the final game's store name and potential trademarks before release. The payment price is configured in the store, not in Godot.

## Install on Android (testing)

The `Android test APK` GitHub Actions workflow creates a debug build on each push to the `dirt-rush` branch. Open the workflow run, download the `dirt-rush-android-debug-apk` artifact, extract the ZIP, and install the APK on an Android phone. This uses disposable debug signing, **not** a Play Store release signature.
