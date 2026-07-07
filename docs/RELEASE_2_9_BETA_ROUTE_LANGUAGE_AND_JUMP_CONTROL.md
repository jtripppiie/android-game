# Release 2.9 Beta - Route Language and Jump Control

Package metadata:

```text
versionCode: 290
versionName: 2.9.0-beta
build badge: ALASKA PASSPORT v2.9 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-2.9.0-beta-290-debug.apk
```

## Next 10 items

1. Added stage-specific action verbs: CLEAR, VAULT, LEAP, and SURVIVE.
2. Updated run-start callouts to use the stage action instead of generic JUMP.
3. Updated the HUD objective to use the stage action and short obstacle label.
4. Updated the map route-intel line to use the selected stage action.
5. Updated ready-screen briefing chips to use the stage action.
6. Updated pause-panel help text to use the stage action.
7. Updated Daily Rush and mission progress away from generic jump language.
8. Added variable jump release so early button release creates a shorter hop.
9. Added clean-vault run tracking and included clean vaults in expedition grading/results.
10. Replaced remaining player-facing hurdle/route wording with route and obstacle language.

## QA focus

- Tap JUMP lightly and confirm the hop is shorter.
- Hold JUMP and confirm the full jump still clears route obstacles.
- Confirm Salmon Rush reads as vaulting river logs while salmon remain hazards/boss theme.
- Confirm map, ready, HUD, pause, missions, and results no longer imply every stage is just "jump over random props."
