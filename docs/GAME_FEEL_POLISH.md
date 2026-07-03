# Game Feel Polish Layer

This build adds a lightweight polish wrapper around the main `MooseRushView`.

## Why

The game already has a playable Alaska loop, but it needed more immediate tactile feedback. A mobile arcade game should feel responsive every time the player taps.

This feature improves perceived quality without rewriting the core gameplay file.

## New class

```text
app/src/main/java/com/jtripppiie/mooserush/JuicyMooseRushView.java
```

`JuicyMooseRushView` extends `MooseRushView` and adds:

- Touch ripples
- Tiny screen jolt on input
- Android haptic tap feedback where supported

## MainActivity change

`MainActivity` now instantiates:

```java
new JuicyMooseRushView(this)
```

instead of the base `MooseRushView`.

The variable type remains `MooseRushView`, so existing photo picker and lifecycle wiring continue to work.

## Why this is safer

This keeps the huge gameplay file stable while adding feel polish as a separate layer.

The core systems are unchanged:

- Alaska stages
- Boss health
- Gates
- Hazards
- Score
- Photo upload
- Debug overlay

## Version

This polish pass is versioned as:

```text
0.2.1-alpha
versionCode 3
badge: ALASKA DEV v0.2.1-alpha
```

## Test checklist

On device, confirm:

- Tapping creates a yellow ripple.
- Controls still work normally.
- JUMP still bounces.
- THROW still launches snowballs.
- There is a subtle screen jolt when pressing controls.
- The APK badge shows `ALASKA DEV v0.2.1-alpha`.
- Photo upload still works.
- Game over and stage clear still work.

## Future polish ideas

- Score popups when points are earned
- Boss hit flash effects
- Stage intro countdown
- Stronger victory confetti
- Better game-over impact animation
- Sound effects
- Optional vibration toggle
