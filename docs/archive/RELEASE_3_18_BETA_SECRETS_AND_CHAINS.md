# You Rush 3.18 Beta — Secrets and Chains

## Build

```text
versionName: 3.18.0-beta
versionCode: 386
build badge: ALASKA PASSPORT v3.86 BETA
```

## Airborne Stomp Mastery

Stomping multiple wildlife threats without touching stable ground builds an air
chain. Each link raises rebound velocity, score value, Aurora gain and the size
of the collectible arc released by the defeated threat. Landing resets the chain.
A third consecutive stomp activates FLOW, rewarding players who read enemy
spacing as a route instead of treating every animal as a wall.

## Aurora Supply Blocks

Every authored encounter can place one floating Alaska supply block. Players can:

- jump into its underside;
- spend a normal snowball to open it safely;
- spend an empowered snowball for a larger star burst.

The block bumps, flashes, releases a curved collectible formation and remains as
a visibly exhausted landmark. Every third cache also releases bear spray when
capacity is available, otherwise a normal supply cache.

## Godot Parity

`AuroraSupplyBlock` reacts to upward runner contact and snowballs, changes to a
spent appearance, and signals the world to create an aerial reward arc. Directed
encounters place blocks alongside their generated route, enemy and reward setup.

## Generated Asset

`aurora_supply_block.png` was created using the built-in image generator on a
flat magenta chroma background, converted to alpha with the image-generation
skill helper, resized with Lanczos filtering, inspected, and copied to Android
and Godot.

