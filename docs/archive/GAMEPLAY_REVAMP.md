# Gameplay Revamp Contract

Version 3.3 changes the run from a flat random obstacle stream into a readable
arcade rhythm. New mechanics should strengthen player decisions, not add another
passive meter.

## Core loop

1. Read the encounter beat.
2. Choose a jump line, ground line, or attack response.
3. Earn combo and Aurora through clean execution.
4. Trigger FLOW by chaining clean vaults.
5. Accept a faster, denser run in exchange for empowered snowballs, pickup pull,
   and a higher score multiplier.
6. Extend FLOW with near misses, or lose momentum when execution breaks.
7. Enter the boss with the run's accumulated resources and rhythm.

## Encounter beats

- `LAUNCH`: one readable reward and a clean start.
- `PRECISION`: three-star arc emphasizing jump shape.
- `WILDLIFE RUSH`: tighter hazard cadence and a two-star action line.
- `JACKPOT LINE`: four-star payoff with a short pacing breath.

`RushDirector` owns these pure pacing decisions. Rendering and entity creation
remain in `MooseRushView`.

## FLOW contract

FLOW must feel more dangerous and more powerful than ordinary running:

- world speed: `1.14x`
- lateral response: `1.12x`
- gate and hazard cooldowns: compressed by the encounter director
- snowballs: empowered and repeated faster while FIRE is held
- near miss: extends FLOW by `0.75s`, capped above the base duration
- score: retains the additional FLOW multiplier

FLOW must never slow the game. Focus remains the deliberate slow-motion state.

## Design rules

- Every preview keeps its numbered 15 px calibration grid enabled by default.
- Telegraph danger before increasing speed.
- Reward a line of execution, not a random pickup cloud.
- New systems must change a player decision or replace an older system.
- Pure pacing rules require JVM tests before device tuning.
- A gate, its route geometry, rewards, and wildlife wave form one owned beat;
  another gate cannot reuse a partially emitted encounter.
- Wildlife begins after the route footprint plus recovery clearance, never
  inside a platform sequence.
- A route may present one utility pickup and one primary reward language:
  precision uses stars/blocks, while high routes use rings.
- Combined speed effects are capped relative to the stage's authored base speed.
- Open-playfield and labeled-button jumps share identical hold/release physics.
