# Alaska 1.3.0 beta — game-state integration pass

Focus of this build: cleaner game-state integration and player-facing polish, per the 1.3.0 goals in the README.

## Changes

### XP and local levels are now live
- `AlaskaLevelMooseRushView` was fully implemented but orphaned: it extended
  `AlaskaRunSummaryMooseRushView` as a sibling of the active
  `AlaskaAwardMooseRushView`, so it was never part of the rendered view chain.
- It is now inserted into the active stack:
  `AlaskaRunSummaryMooseRushView -> AlaskaLevelMooseRushView -> AlaskaAwardMooseRushView`.
- Result: XP accrues from run score, levels persist, the level HUD renders
  top-right during runs, and level-up popups fire again.
- The level HUD was moved down to `dp(100)` so it no longer collides with the
  boss health bar at the top of the screen during boss phases.

### Cleaner default player experience
- The debug overlay now defaults to **off**. It previously covered a large part
  of the play area on first launch. Testers can still toggle it from the menu
  `DEBUG` button, and the choice is persisted.

### Snowball fix
- The snowball projectile radius was a fixed `7f` raw pixels, which is nearly
  invisible on high-density phones and hurt the throw/aim mechanic. It is now
  density-aware (`dp(7)`) so it scales correctly across devices.

## Version

```text
versionCode: 130
versionName: 1.3.0-beta
build badge: ALASKA BETA v1.3.0
```

## Test focus for this build
1. Confirm the level HUD appears top-right during a run and does not overlap the
   boss health bar.
2. Earn score and confirm XP increases and a level-up popup fires.
3. Confirm XP/level persist across app restarts.
4. Confirm the debug overlay is hidden by default and still toggles from the menu.
5. Confirm snowballs are clearly visible and still hit the boss.
</content>
</invoke>
