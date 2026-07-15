# You Rush 3.16 Beta — Reactive River

## Build

```text
versionName: 3.16.0-beta
versionCode: 384
build badge: ALASKA PASSPORT v3.84 BETA
```

## Gameplay Loop

Water and its ice bridge now remain one systemic object across state changes:

```text
LETHAL WATER → SNOWBALL → BRITTLE ICE → DESTRUCTION → LETHAL WATER
```

The reopened current can be frozen again as long as it remains on screen. This
creates resource decisions and recovery opportunities instead of turning water
permanently safe after one shot.

## Bridge Destruction Sources

- A normal snowball cracks and then breaks the bridge.
- An empowered snowball breaks it immediately.
- An aim-down hard landing stomps through it.
- Committed bear, polar bear, or moose movement can destroy it.
- A boss laser intersecting the bridge can crack or shatter it.
- Boss arena collapse uses the same break path.

Every path calls the shared Android `breakRoutePlatform` transition. Water-owned
platforms restore their source `WaterPatch`, redraw the river and reactivate its
collision. Other brittle platforms retain their existing behavior.

## Godot Parity

`FreezableWater` remains in the scene while frozen, disables its lethal collision
and creates `ReactiveIce`. It listens to that bridge's `shattered` signal, then
reenables collision and redraws the water. It no longer deletes itself during the
first freeze.

