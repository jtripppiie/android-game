# Alaska 0.9 Beta Readiness

The current target is **0.9.2-beta**, not final 2.0.

```text
versionCode 21
versionName 0.9.2-beta
badge ALASKA BETA v0.9.2
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
- Three-life run system
- Checkpoint respawn after passed gates
- Contra-code unlimited-lives cheat
- Directional controls
- Jump control
- Snowball throw control
- Tree timing mechanic
- Snowball interactions
- Near-miss scoring
- Combo streaks
- Incoming callouts
- Pause and quick-help overlay
- Touch ripple polish
- Haptic feedback where supported
- Debug overlay
- Version badge
- Hardened manifest
- Safer launcher icon vector paths
- APK workflow with logs and artifact output

## Contra code mapping

Classic sequence:

```text
UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT B A START
```

Mobile mapping:

- `UP` = tap upper screen
- `DOWN` = tap lower middle screen, away from controls
- `LEFT` = left control
- `RIGHT` = right control
- `B` = throw control
- `A` = jump control
- `START` = pause control

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
16. Pause button appears during gameplay.
17. Pause overlay stops the game and resumes cleanly.
18. Three lives appear during a run.
19. Losing a life respawns at the latest checkpoint area.
20. Final life still reaches normal game over.
21. Contra code activates unlimited lives and shows `∞ LIVES`.
22. Stage challenge phase starts after enough gates.
23. Stage clear unlocks the next stage.
24. Game over retry works.
25. Pause/resume from Android app lifecycle does not crash.
26. A 15-minute play session has no obvious crash.

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
