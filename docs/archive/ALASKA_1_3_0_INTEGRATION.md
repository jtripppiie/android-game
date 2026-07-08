# Alaska 1.3.0 beta — game-state integration pass

Historical note: Alaska 1.3.2 removed the inherited Alaska view stack. Runtime
gameplay now uses one `MooseRushView`, a `GameState` object, and `RunnerTuning`
for platformer constants.

Focus of this build: cleaner game-state integration and player-facing polish, per the 1.3.0 goals in the README.

## Changes

### XP and local levels are now live
- XP accrues from run score through `GameState`.
- Levels persist via the existing shared preferences file.
- Level status renders in the unified top HUD so it does not collide with the
  boss health bar.

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
1. Confirm the level HUD appears in the unified top HUD and does not overlap the
   boss health bar during boss phases.
2. Earn score and confirm XP increases and a level-up popup fires.
3. Confirm XP/level persist across app restarts.
4. Confirm the debug overlay is hidden by default and still toggles from the menu.
5. Confirm snowballs are clearly visible and still hit the boss.
</content>
</invoke>
