# You Rush

A personalized, retro-funny Android arcade game by **TripperDeeLabs**.

Upload your photo, become the tiny chaos character, pick a region, and survive local nonsense.

## Prototype status

This is now an Alaska-first gameplay prototype. The goal is still to prove the addictive core before expanding to Florida, Michigan, City, or other regions.

> Add your face. Pick a place. Get bonked. Retry. Beat the boss. Unlock the next stage.

## Current direction

The first region is **Alaska**. Alaska is also the template for how future regions should work.

Each region should eventually include:

- A regional map
- Seasonal or environment variants
- Local obstacle placeholders
- Local boss placeholders
- Regional death jokes
- A short stage progression
- Region-specific scoring and boss interactions

## Current game flow

The app currently includes:

1. **Splash screen**
   - TripperDeeLabs retro boot-up vibe
   - You Rush: Alaska identity

2. **Main menu**
   - Play selected Alaska stage
   - Open Alaska map
   - Open customization
   - Toggle debug overlay

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
   - Virtual directional controls
   - Jump control
   - Fire/snowball control
   - Uploaded photo as the player head
   - Generated walking-sprite body with animated arms and legs
   - Alaska stage label in the HUD
   - Antler gates
   - Stage hazards
   - Boss phases with health
   - Scoring
   - Stage clear screen
   - Game-over retry loop

## Controls

The visible control pad includes:

- **LEFT**: move left
- **RIGHT**: move right
- **JUMP**: bounce upward
- **FIRE**: shoot a snowball at the boss

A normal screen tap during gameplay still bounces the character, but the control pad is now the preferred test interface.

## Alaska stage structure

| Stage | Season | Hazard | Boss | Goal before boss |
|---|---|---|---|---|
| Midnight Sun Run | Midnight Sun | SUN placeholder | Sunburn Sprite | 6 gates |
| Salmon Rush | Summer | SALMON | Salmon Boss | 8 gates |
| Moose Pass | Summer | MOOSE | Moose Boss | 10 gates |
| Dark Winter | Darkness | DARK | Darkness Boss | 12 gates |
| Bear Country | Winter | BEAR | Bear Boss | 14 gates |

Each stage transitions into a boss phase after the gate goal is reached. Bosses are defeated by landing snowball hits with the **FIRE** control.

## Scoring

Current scoring values:

- Clear a gate: **+10**
- Dodge a hazard: **+4**
- Hit a boss: **+25**
- Defeat a boss: **+100 + stage bonus**

Best score persists locally.

## Asset packs

The first visible asset pack lives here:

`app/src/main/assets/regions/alaska/`

Current Alaska placeholder files:

- `region.json`
- `background_midnight_sun.svg`
- `background_dark_winter.svg`
- `hazard_salmon.svg`
- `hazard_moose.svg`
- `hazard_bear.svg`
- `gate_antlers.svg`

The runtime uses Android vector drawables for now:

`app/src/main/res/drawable/`

This lets the APK render placeholders immediately while the region asset-pack folder remains the future source of truth for real art.

## Android compatibility

The debug build is currently pinned conservatively for CI stability:

- `minSdk 23`
- `targetSdk 35`
- `compileSdk 35`
- Android Gradle Plugin `8.7.3`
- Gradle `8.10.2` in the GitHub Actions workflow

Android 14 is API level 34, so Android 14 devices remain supported.

## Build

Open the repo in Android Studio and run the `app` configuration on an Android device or emulator.

## Debug APK workflow

GitHub Actions includes an Android Debug APK workflow at:

`.github/workflows/android-debug-apk.yml`

It builds `assembleDebug` and uploads:

- `you-rush-alaska-debug-apk`
- `you-rush-alaska-build-logs`

The logs artifact is uploaded even when the APK fails, so build errors can be diagnosed quickly.

## Debugging

There are two debug layers:

- Android logcat lifecycle/photo tag: `YouRushDebug`
- Gameplay event tag: `YouRushGame`

The in-game debug overlay shows:

- State
- Score
- Boss status and HP
- Player position
- Active shots and hazards
- Recent game events

## More documentation

Detailed Alaska gameplay notes live here:

`docs/ALASKA_GAMEPLAY_BUILD.md`

## Why this direction

The stronger product is not just a runner. It is a personal chaos game.

The $1.99 pitch:

> A tiny arcade game starring you. Upload your face, dodge ridiculous local chaos, and chase one more point. No ads. No in-app purchases.

A viral mobile game needs an instantly understandable loop:

1. One input: tap/control.
2. One goal: survive the next obstacle.
3. One emotional hook: that is my face getting bonked.
4. One escalation: boss phase.
5. One restart action: retry.

## Future regions

Future regions can copy the Alaska pattern:

- Florida: gators, golf carts, storms, flamingos
- Michigan: potholes, deer, snowplows, lake waves
- City: manhole covers, pigeons, taxis, traffic cones

Do not start these until Alaska feels fun.

## Next development steps

1. Confirm the GitHub Actions debug APK build succeeds.
2. Install the debug APK on a real Android phone.
3. Tune the LEFT / RIGHT / JUMP / FIRE touch targets.
4. Tune gravity, gate spacing, hazard speed, and boss HP.
5. Add crop and position controls for the uploaded photo.
6. Improve individual boss attack patterns.
7. Add one-tap share card after game over.
8. Create the second region only after Alaska feels fun.
