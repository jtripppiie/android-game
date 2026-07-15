# You Rush 3.14 Beta — Visual Gameplay Pass

## Build

```text
versionName: 3.14.0-beta
versionCode: 382
build badge: ALASKA PASSPORT v3.82 BETA
commit: f14e328
```

## Purpose

This release makes route choices and boss attacks visually distinct. It replaces
several temporary procedural shapes with production raster art while preserving
the existing collision geometry, encounter timing, and FLOW risk/reward rules.

## Player-Facing Changes

- Static ice routes use a blue crystalline platform sprite.
- snowy Stage 5 routes use a dedicated packed-snow platform with a solid icy
  underside, rather than visually reusing grass or iceberg obstacles.
- Moving routes use an arctic mechanical sled with cyan motion lights. This
  gives players an immediate visual cue that the landing surface will move.
- Open water uses a glacial-current surface with whitecaps and floating ice.
  The surface subtly bobs, while the `FREEZE OR CLEAR` instruction remains.
- Boss eye lasers terminate in a rotating red energy-and-ice impact sprite. A
  stable white core still marks the exact dangerous endpoint.
- The boss laser emitter remains aligned to the visible eye and is redrawn above
  the boss sprite during the telegraph so it cannot be hidden by sprite layering.

## Android Implementation

`GameAssets` loads the new bitmaps once:

- `route_platform_snow.png`
- `route_platform_moving.png`
- `glacial_water_surface.png`
- `laser_ice_impact.png`

`MooseRushView.drawRoutePlatform` selects the moving sled first, the snowbank for
Stage 5, and the ice platform elsewhere. Brittle-platform crack overlays remain
procedural so their gameplay state stays visible on top of every base sprite.

`MooseRushView.drawWaterPatch` crops the transparent source to the painted water
region, maps it to the active hazard rectangle, and applies a small phase-based
vertical bob. The gameplay collision and freezing state are unchanged.

`MooseRushView.drawBeamImpact` scales and rotates the impact art around the beam
endpoint. The small white collision core is drawn afterward for precision.

## Godot Implementation

The same PNGs live under `godot/assets/`. `MovingTrailPlatform` now creates a
`Sprite2D` using `route_platform_moving.png` while retaining its existing
`AnimatableBody2D`, 150 x 24 collision shape, travel vector, and cycle timing.

## Asset Production

The four sprites were generated as isolated raster assets on flat chroma-key
backgrounds. The image-generation pipeline removed the backgrounds, cleaned key
spill, resized the results with Lanczos filtering, and validated RGBA output.
Prompt summaries and the earlier ice-platform and laser-emitter provenance are
recorded in [GENERATED_ASSETS.md](GENERATED_ASSETS.md).

## Verification Completed

```bash
./gradlew testDebugUnitTest assembleDebug
python3 godot/validate_project.py
git diff --check
```

All Android unit tests passed, the debug APK assembled successfully, the Godot
overhaul validation passed, and the committed patch had no whitespace errors.

## Device QA Checklist

1. Install `you-rush-alaska-3.14.0-beta-382-debug.apk`.
2. Confirm moving platforms show the mechanical sled and continue moving.
3. Confirm snowy-stage platforms show packed snow rather than green terrain.
4. Enter water and confirm artwork, collision, damage, and freezing still agree.
5. Reach a laser boss and confirm the emitter sits on the eye throughout its
   warning, the beam begins at that emitter, and the impact marks its endpoint.
6. Reflect a laser in FLOW and confirm the reflected attack still resolves.

