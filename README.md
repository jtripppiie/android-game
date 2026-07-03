# You Rush

A personalized, Flappy Bird-style Android arcade prototype: upload a photo, become the player, tap to bounce, dodge chaos gates, fail fast, and retry immediately.

## Prototype status

This is the first playable seed of the viral loop. The goal is not complexity yet. The goal is to prove the addictive core:

> Add your face. Tap. Bonk. Retry.

### Currently included

- Native Android Java app
- Full-screen portrait gameplay
- One-tap bounce controls
- Uploaded photo as the player head
- Saved player photo URI when Android grants persistent access
- Procedural gap/gate spawning
- Collision detection
- Score counter
- Saved best score
- Fast tap-to-restart loop
- No ads, no account system, no network dependency

## Build

Open the repo in Android Studio and run the `app` configuration on an Android device or emulator.

The project uses Android Gradle Plugin `9.2.0`, `compileSdk 36`, and `targetSdk 36`.

## Game direction

The stronger product is not just an Alaska runner. It is a personal chaos game.

The $1.99 pitch:

> A tiny arcade game starring you. Upload your face, dodge ridiculous chaos, and chase one more point. No ads. No in-app purchases.

## Why this direction

A viral mobile game needs an instantly understandable loop:

1. One input: tap.
2. One goal: pass the next gap.
3. One emotional hook: that is *my* face getting bonked.
4. One restart action: tap again.

## Next development steps

1. Add crop/position controls for the uploaded photo.
2. Add a one-tap share card after game over.
3. Add themed chaos packs: Alaska, Office, School, Family, Pets.
4. Let users rename hazards, such as “Boss,” “Homework,” or “Monday.”
5. Add sound effects and haptics.
6. Add daily challenge seeds.
7. Tune gravity, flap strength, gap size, and spawn pacing on a real device.
