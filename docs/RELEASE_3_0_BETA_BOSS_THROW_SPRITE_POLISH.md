# Release 3.0 Beta - Boss, Throwing, and Sprite Polish

Release owner pass for boss clarity, snowball feel, and sprite edge cleanup.

## Version

versionCode: 300
versionName: 3.0.0-beta
build badge: ALASKA PASSPORT v3.0 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.0-beta-300-debug.apk

## What changed

1. Snowballs now aim their vertical arc toward the nearest visible threat instead of flying as flat pellets.
2. Snowballs now carry vertical velocity, gravity, age, and richer trails so throwing reads as a real action.
3. Aurora focus and boss weak windows now create empowered snowballs with bigger visuals and faster travel.
4. Boss weak-window hits now deal increased damage, with powered weak hits forcing stronger boss recovery.
5. Boss hit feedback now includes stronger flash, shake, score text, and aurora meter reward scaling.
6. All bosses can now escalate into summon patterns after phase two, not only the bear.
7. Bosses now enter an enrage state at low health with a visible callout, faster tracking, shorter recovery, and stronger pressure.
8. Boss snow-wave attacks now add a third lane after phase two, making late fights feel more deliberate.
9. Boss health text now exposes `PHASE 2`, `ENRAGED`, and `WEAK WINDOW` status so combat rules are visible during play.
10. Sprite edge cleanup was tightened again with larger atlas guards, larger trim insets, and a deeper roaring-bear crop.

## QA focus

- Start each Alaska stage and confirm the boss phase can be reached without a crash.
- During boss fights, confirm FIRE is clearly the attack, RECOVER is the weak window, and phase/enrage labels appear.
- Confirm snowballs arc toward hazards or bosses and powered shots look distinct.
- Confirm bear and polar bear roar sprites show less source-image border/foot artifacting.
- Confirm normal wildlife can still be stunned with snowballs.
- Confirm the version badge reads `ALASKA PASSPORT v3.0 BETA`.
