# 1.25-Worthy Milestone

This milestone is about substance, not only a number in `build.gradle`.

The game now has a deeper Alaska arcade loop with:

- stage selection
- photo personalization
- five Alaska stages
- lives
- checkpoint respawn
- unlimited-lives secret code
- bonus stars
- extra-life rewards
- shield pickup
- progress HUD
- run timer
- gate progress
- challenge-phase messaging
- run missions
- mission score bonuses
- persistent total mission completions
- post-run summary
- best gates tracking
- longest run tracking

## What changed for this milestone

### Run missions

Each run now has four goals:

1. Clear 4 gates.
2. Collect 3 stars.
3. Survive 45 seconds.
4. Reach challenge phase.

Each completed goal awards score and adds to a persistent total mission count.

### Run summary

Game over and stage clear now show a polished summary panel with:

- stage name
- score
- gates
- best gates
- stars collected this run
- run time
- longest run
- missions completed
- total missions completed

## Still required before final public release

This milestone still needs device verification:

1. GitHub Actions must build a fresh APK.
2. APK must install on a real Android phone.
3. All stages must be played.
4. The 15-minute no-crash test must pass.
5. Controls and difficulty need real-device tuning.
6. Reflection-heavy systems should be folded into cleaner typed game state before a final polished major release.
