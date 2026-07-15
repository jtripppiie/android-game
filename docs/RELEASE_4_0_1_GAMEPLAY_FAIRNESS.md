# You Rush 4.0.1 Gameplay Fairness Update

Version 4.0.1 rebuilds the Alaska runner around readable, stage-coherent
encounters. It follows the 4.0 stable campaign release without replacing its
progression or save format.

## Build Identity

```text
versionName: 4.0.1
versionCode: 401
channel: ALASKA RELEASE
badge: ALASKA PASSPORT v4.01
debug APK: app/build/outputs/apk/debug/you-rush-alaska-4.0.1-401-debug.apk
```

## Encounter And Obstacle Corrections

- Final cooldown clamps preserve landing and decision recovery after all
  difficulty, FLOW, and encounter multipliers are applied.
- Gates wait until the current encounter has emitted its route and wildlife;
  timer races can no longer create an orphan gate without matching geometry.
- Biome-specific cards stay in their authored stage instead of leaking salmon,
  moose, eagle, or bear arrangements into unrelated regions.
- Wildlife waves begin beyond the complete route footprint and have at least
  188 dp center spacing.
- Each gate rolls at most one utility reward. High routes use rings instead of
  duplicating the line with stars, and bonus blocks belong to precision routes.

## Scale And Collision

- Wildlife is sized against the roughly 100 dp rendered runner: moose is 132
  dp tall, polar bear 116, brown bear 104, wolf 56, eagle 50, and salmon 30.
- Ground-animal collision radii were increased with the art so the runner
  cannot visibly pass through a moose or bear while retaining forgiving edges.
- Random untelegraphed wolf pounces were removed; wolves remain honest ground
  threats while eagles and charging heavy wildlife keep visible intent tells.

## Speed And Input

- Stage-start pressure and maximum difficulty acceleration were reduced.
- The hidden progress ramp now adds at most 36 dp/s instead of 64 dp/s.
- All combined world-speed effects are capped at 1.72 times stage base speed.
- Bear chases are exclusive to Bear Country.
- Touching the open playfield and pressing JUMP now share the same variable
  jump: hold for height and release for a short hop.

## Automated Verification

JVM coverage protects cooldown floors, wave spacing, biome ownership, opening
and maximum speed, scroll caps, jump reach, wildlife hierarchy, and the maximum
visual-to-collider relationship for large ground animals.

Release validation:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
python3 godot/validate_project.py
git diff --check
```

Device acceptance still requires playing every route family, confirming short
and full jumps from both touch surfaces, and completing Bear Country with FLOW,
Ring Rush, and the bear chase overlapping without exceeding the speed cap.
