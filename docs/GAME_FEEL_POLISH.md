# Game Feel Polish

This note is historical. As of Alaska 1.3.2, feel polish belongs in the single
`MooseRushView` runtime instead of a wrapper subclass.

## Why

The game already has a playable Alaska loop, but it needed more immediate tactile feedback. A mobile arcade game should feel responsive every time the player taps.

This feature improves perceived quality while keeping gameplay state in one
place.

## Current classes

```text
app/src/main/java/com/jtripppiie/mooserush/MooseRushView.java
app/src/main/java/com/jtripppiie/mooserush/GameState.java
app/src/main/java/com/jtripppiie/mooserush/RunnerTuning.java
```

`MooseRushView` owns rendering, input, and the game loop. `GameState` holds run
state such as lives, combo, XP, and mute. `RunnerTuning` holds platformer feel
constants such as coyote time, jump buffer, and spawn-spacing floors.

- Coyote time
- Jump buffer
- Double jump
- Fair spawn cooldown floors
- SoundPool generated SFX with a persisted mute toggle

## MainActivity change

`MainActivity` instantiates:

```java
new MooseRushView(this)
```

Photo picker and lifecycle wiring stay attached directly to the core view.

## Version

This polish pass is versioned as:

```text
1.3.4-beta
versionCode 134
badge: ALASKA BETA v1.3.4
```

## Test checklist

On device, confirm:

- Controls still work normally.
- JUMP works from the ground with coyote time.
- A buffered jump fires when landing shortly after a tap.
- Double jump works once while airborne.
- FIRE launches snowballs.
- The APK badge shows `ALASKA BETA v1.3.4`.
- Photo upload still works.
- Game over and stage clear still work.

## Future polish ideas

- Richer bundled SoundPool samples for each audio event
- Score popups when points are earned
- Better game-over impact animation
- Optional vibration toggle
