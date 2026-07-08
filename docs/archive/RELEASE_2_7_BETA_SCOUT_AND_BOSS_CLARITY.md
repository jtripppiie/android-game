# Release 2.7 Beta - Scout and Boss Clarity

Package metadata:

```text
versionCode: 270
versionName: 2.7.0-beta
build badge: ALASKA PASSPORT v2.7 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-2.7.0-beta-270-debug.apk
```

## Next 3 changes

1. Trail Scout mode.
   - Trail Map pickups now activate a timed SCOUT state.
   - While active, hazards are forecasted more aggressively and an in-world marker points to the next upcoming obstacle or wildlife threat.

2. Clean Vault skill rewards.
   - Tight, clean obstacle clears now award a CLEAN VAULT popup, extra score, and aurora meter.
   - This gives practiced jumping a visible reward without changing the core survival rules.

3. Boss weak-window clarity.
   - Boss RECOVER windows now draw a crosshair and FIRE NOW prompt directly on the boss.
   - This makes the combat rule visible at the exact moment the player can punish the boss.

## Also preserved

- 2.6 pause/resume controls, route intel, ready briefing, and compact HUD labels remain part of this build.

## QA focus

- Pick up a Trail Map and verify SCOUT counts down in the journey tracker.
- Confirm Scout markers point to upcoming threats without blocking the player.
- Clear obstacles closely and confirm CLEAN VAULT rewards feel fair.
- Enter a boss RECOVER window and confirm the FIRE NOW reticle appears and disappears correctly.
- Confirm pause/resume still works after Scout and boss changes.
