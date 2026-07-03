# Moose Rush

A tiny premium-style Android arcade prototype: drag your character left and right to dodge Alaska chaos.

## Prototype status

This is the first playable seed of the game. It is intentionally simple so the core loop can be tested quickly.

### Currently included

- Native Android Java app
- Full-screen portrait gameplay
- One-thumb drag movement
- Random obstacle spawning
- Moose, bear, fish, ice, and tourist van hazards
- Collision detection
- Score counter
- Saved best score
- Fast tap-to-restart loop

## Build

Open the repo in Android Studio and run the `app` configuration on an Android device or emulator.

The project uses Android Gradle Plugin `9.2.0`, `compileSdk 36`, and `targetSdk 36`.

## Gameplay direction

The monetization target is a clean $1.99 premium game:

- No ads
- No in-app purchases
- No account system
- Offline by default
- Skill-based replayability

## Next development steps

1. Add sprite assets for the player, moose, bear, fish, ice, and van.
2. Add sound effects and haptics.
3. Add daily challenge seeds.
4. Add unlockable trails and cosmetics.
5. Add polish passes for spawn fairness and difficulty pacing.
6. Prepare store listing copy and screenshots once the game feels fun.
