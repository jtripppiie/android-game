# You Rush 4.1 Debug Tuning Overhaul

Version 4.1 turns the Android debug build into a screenshot-driven tuning tool.
The goal is to let a tester identify one bad obstacle, animal, platform, or
reward without describing its location ambiguously.

## Build Identity

```text
versionName: 4.1.0
versionCode: 410
channel: ALASKA RELEASE
badge: ALASKA PASSPORT v4.10
debug APK: app/build/outputs/apk/debug/you-rush-alaska-4.1.0-410-debug.apk
```

## Stable Object Language

- IDs combine a stage prefix, item category, and run-local sequence.
- IDs are stored on the runtime object and never depend on current draw order.
- New runs reset sequences, making `SUN-OB01` easy to reproduce from a fresh run.
- Player and boss use fixed IDs; tweakable spawned objects use numbered IDs.
- Logcat records the ID-to-type mapping for later diagnosis.

## Compact Overlay

The overlay labels gates, wildlife, route platforms, launch pads, supply
blocks, rings, water, utility pickups, and boss attacks. It deliberately omits
stars, snowballs, particles, and score popups. The debug panel displays the
active encounter recipe so feedback can include both object and layout context.

Debug builds start with the overlay enabled. Release builds remain clean, and
the existing five-tap menu gesture toggles the overlay when a clean screenshot
is needed.

## Feedback Contract

Use the exact badge and, when possible, attach a screenshot:

```text
RIV-OB02 is too tall.
MOO-AN01 arrives before MOO-PF02 is clear.
BER-PU01 should sit lower than BER-RG03.
```

See [Debug Item Identifiers](DEBUG_ITEM_IDENTIFIERS.md) for the complete legend.

## Release Gates

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
python3 godot/validate_project.py
git diff --check
```
