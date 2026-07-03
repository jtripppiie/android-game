# You Rush

A personalized, retro-funny Android arcade game by **TripperDeeLabs**.

Upload your photo, become the tiny chaos character, pick a region, and survive local nonsense.

## Prototype status

This is the first playable seed of the viral loop. The goal is not complexity yet. The goal is to prove the addictive core:

> Add your face. Pick a place. Get bonked. Retry.

## Current direction

The first region is **Alaska**. Alaska is also the template for how future regions should work.

Each region should eventually include:

- A regional map
- Seasonal or environment variants
- Local obstacle placeholders
- Local boss placeholders
- Regional death jokes
- A short stage progression

## Current game flow

The app now has a full early game shell:

1. **Splash screen**
   - TripperDeeLabs retro boot-up vibe
   - You Rush: Alaska identity

2. **Main menu**
   - Play selected Alaska stage
   - Open Alaska map
   - Open customization

3. **Alaska map**
   - Midnight Sun Run
   - Salmon Rush
   - Moose Pass
   - Dark Winter
   - Bear Country

4. **Customization screen**
   - Add or change player photo
   - Preview the generated walking-sprite character
   - Cycle season style

5. **Gameplay**
   - One-tap bounce controls
   - Uploaded photo as the player head
   - Generated walking-sprite body with animated arms and legs
   - Alaska stage label in the HUD
   - Gates and collision
   - Placeholder boss phase
   - Game-over retry loop

## Asset packs

The first visible asset pack now lives here:

`app/src/main/assets/regions/alaska/`

Current Alaska placeholder files:

- `region.json`
- `background_midnight_sun.svg`
- `background_dark_winter.svg`
- `hazard_salmon.svg`
- `hazard_moose.svg`
- `hazard_bear.svg`
- `gate_antlers.svg`

Important: the game currently still draws most visuals directly with Canvas. These files are the visible regional asset structure and placeholder art target. The next step is wiring the runtime to load these assets instead of drawing every placeholder directly in Java.

## Android compatibility

The app is intended to support Android 14 and newer.

Current SDK settings:

- `minSdk 23`
- `targetSdk 36`
- `compileSdk 36`

Android 14 is API level 34. Because the app minimum SDK is below 34 and the target SDK is above 34, Android 14 devices are included while older supported devices can still install the game.

If the product decision changes to Android 14 only, set `minSdk 34` in `app/build.gradle`.

## Build

Open the repo in Android Studio and run the `app` configuration on an Android device or emulator.

The project uses Android Gradle Plugin `9.2.0`, `compileSdk 36`, and `targetSdk 36`.

## Debug APK workflow

GitHub Actions includes an Android Debug APK workflow at:

`.github/workflows/android-debug-apk.yml`

It builds `assembleDebug` and uploads the APK artifact as:

`you-rush-alaska-debug-apk`

## Why this direction

The stronger product is not just a runner. It is a personal chaos game.

The $1.99 pitch:

> A tiny arcade game starring you. Upload your face, dodge ridiculous local chaos, and chase one more point. No ads. No in-app purchases.

A viral mobile game needs an instantly understandable loop:

1. One input: tap.
2. One goal: survive the next obstacle.
3. One emotional hook: that is my face getting bonked.
4. One restart action: tap again.

## Alaska template

Alaska should prove the structure before other regions are added.

Stage ideas:

1. **Midnight Sun Run** - bright intro stage
2. **Salmon Rush** - salmon chaos and flying-fish jokes
3. **Moose Pass** - moose/antler hazard style
4. **Dark Winter** - darker visual mode
5. **Bear Country** - bear boss moment

Future regions can copy this pattern:

- Florida: gators, golf carts, storms, flamingos
- Michigan: potholes, deer, snowplows, lake waves
- City: manhole covers, pigeons, taxis, traffic cones

## Next development steps

1. Test the new shell on a real Android 14 device or emulator.
2. Confirm the GitHub Actions debug APK build succeeds.
3. Tune the Alaska map flow and touch targets.
4. Wire runtime rendering to load the Alaska asset pack files.
5. Replace placeholder bosses with better Alaska attack patterns.
6. Add crop and position controls for the uploaded photo.
7. Add one-tap share card after game over.
8. Create the second region only after Alaska feels fun.
