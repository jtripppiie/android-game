# You Rush 3.17 Beta — Arcade Joy

## Build

```text
versionName: 3.17.0-beta
versionCode: 385
build badge: ALASKA PASSPORT v3.85 BETA
```

## Design Goal

This pass adds the playful action-and-rebound rhythm associated with excellent
classic platformers while keeping You Rush's original Alaska identity, FLOW
economy, aiming, snowballs and wildlife behavior.

## Wildlife Stomps

Falling onto ordinary wildlife defeats it and rebounds the runner upward. The
stomp increases combo, awards score and Aurora energy, produces impact particles
and haptics, and preserves one air jump so the player can steer into a follow-up.

Bears, polar bears and moose are heavy threats. They can only be stomped while
FLOW is active or while the player deliberately holds aim-down. Side contact and
poorly timed landings remain dangerous. Ice spikes and avalanches cannot be
stomped.

## Aurora Launch Pads

Every route family can now incorporate an original mechanical arctic launch pad:

- Ground routes launch into optional air rewards.
- Precision routes turn a narrow landing into a committed rebound.
- High routes accelerate traversal between elevated platforms.
- Boss arenas provide a repeatable escape and attack setup.
- FLOW increases launch velocity and score.

The pad compresses when activated and emits a visible energy ring, score callout,
spark burst, screen shake and haptic response. Godot uses the same artwork and
launch contract through `AuroraLaunchPad`.

## Generated Asset

`arctic_launch_pad.png` was generated with the built-in image tool on a flat
magenta chroma background, converted to alpha with the image-generation skill's
cleanup helper, resized with Lanczos filtering, visually inspected, and copied
to both Android and Godot asset folders.

