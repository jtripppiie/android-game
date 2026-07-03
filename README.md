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
- Generated walking-sprite body with animated arms and legs
- Saved player photo URI when Android grants persistent access
- Procedural gap and gate spawning
- Collision detection
- Score counter
- Saved best score
- Fast tap-to-restart loop
- No ads, no account system, no network dependency

## Build

Open the repo in Android Studio and run the `app` configuration on an Android device or emulator.

The project uses Android Gradle Plugin `9.2.0`, `compileSdk 36`, and `targetSdk 36`.

## Android compatibility

The app is intended to support Android 14 and newer.

Current SDK settings:

- `minSdk 23`
- `targetSdk 36`
- `compileSdk 36`

Android 14 is API level 34. Because the app minimum SDK is below 34 and the target SDK is above 34, Android 14 devices are included while older supported devices can still install the game.

If the product decision changes to Android 14 only, set `minSdk 34` in `app/build.gradle`.

## Game direction

The stronger product is not just an Alaska runner. It is a personal chaos game.

The $1.99 pitch:

> A tiny arcade game starring you. Upload your face, dodge ridiculous chaos, and chase one more point. No ads. No in-app purchases.

## Why this direction

A viral mobile game needs an instantly understandable loop:

1. One input: tap.
2. One goal: pass the next gap.
3. One emotional hook: that is my face getting bonked.
4. One restart action: tap again.

## Personalization direction

The current MVP uses a practical sprite illusion: the uploaded photo becomes the character face, and the game generates a tiny animated body underneath it. That avoids heavy AI processing while still making the character feel personal.

Later versions can add real photo cutout, crop and position controls, outfit selection, and shareable animated death cards.

## Next development steps

1. Add crop and position controls for the uploaded photo.
2. Add a one-tap share card after game over.
3. Add themed chaos packs: Alaska, Office, School, Family, Pets.
4. Let users rename hazards, such as Boss, Homework, or Monday.
5. Add sound effects and haptics.
6. Add daily challenge seeds.
7. Tune gravity, flap strength, gap size, and spawn pacing on a real device.
