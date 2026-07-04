# Alaska 0.9 Beta Readiness

The current target is **0.9.0-beta**, not final 2.0.

```text
versionCode 19
versionName 0.9.0-beta
badge ALASKA BETA v0.9.0
```

## What 0.9 means

0.9 means the main Alaska game loop is ready for serious APK testing and tuning.

It does not mean final release.

It does mean the game should now have enough complete systems to judge whether it is fun.

## Included beta systems

- Splash screen
- Main menu
- Alaska map
- Photo personalization
- Local saved progress
- Five Alaska stages
- Stage intro overlay
- Main arcade run
- Stage challenge phase
- Stage clear / retry loop
- Directional controls
- Jump control
- Snowball throw control
- Tree timing mechanic
- Snowball interactions
- Near-miss scoring
- Combo streaks
- Incoming callouts
- Touch ripple polish
- Haptic feedback where supported
- Debug overlay
- Version badge
- Hardened manifest
- Safer launcher icon vector paths
- APK workflow with logs and artifact output

## 0.9 must pass

1. GitHub Actions builds a fresh APK.
2. APK installs on a real Android phone.
3. Main menu works.
4. Customization opens.
5. Photo picker works.
6. Photo restores after app restart.
7. All five stages start.
8. Stage intro appears.
9. Movement controls respond.
10. Jump responds.
11. Snowball throw responds.
12. Tree button appears only when close.
13. Near-miss scoring appears without spam.
14. Combo HUD appears and expires.
15. Incoming callouts appear before obstacles reach the player.
16. Stage challenge phase starts after enough gates.
17. Stage clear unlocks the next stage.
18. Game over retry works.
19. Pause/resume does not crash.
20. A 15-minute play session has no obvious crash.

## Known not-final items

These are acceptable for beta but must be improved before 1.0 or 2.0:

- Reflection-heavy wrapper layers
- Placeholder-style art
- Limited sound design
- No final store screenshots
- No final hosted privacy policy
- No signed release package
- Difficulty still needs phone-based tuning

## Next work after 0.9

1. Run the APK workflow.
2. Fix build errors.
3. Install on device.
4. Tune controls and difficulty.
5. Fold stable wrapper mechanics into cleaner game state.
6. Disable the visible badge for public release builds.
