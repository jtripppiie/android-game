# Release 5.4.1 — Gameplay Refinement

Version 5.4.1 is a game-feel, fairness, and readability release built on the
5.4.0 production refactor. It targets the current `godot/` game; the legacy
Java game was not modified.

Starting commit:

```text
09312de8e40c7ad69669a22df351d9e54ef8c442
```

## Movement and controls

- Horizontal input is read before dash begins, so a same-frame left-plus-dash
  immediately travels left instead of using stale facing.
- Ground reversal accelerates faster, while low air friction preserves useful
  momentum without removing air control.
- Rising, apex, and falling gravity now differ. The measured full jump is
  292.9 px, while an early release produces a distinct 103.5 px short jump.
- Falling speed is capped, and the existing coyote time, jump buffer, one air
  jump, dash, stomp, and snowball rules remain intact.
- The D-pad separates horizontal intent from deliberate vertical travel.
  Shallow thumb drift continues running instead of causing surprise jumps or
  stomps.
- Routine landings no longer flood the HUD with messages; stomp and meaningful
  fast-land feedback remain.

Measured mechanics audit:

```text
right=120.9
left=84.7
short_jump=103.5
full_jump=292.9
dash=104.0
reverse_dash=91.9
air_speed=392.3
air_jump=one
third_jump=blocked
stomp=true
snowball=1
game_over=true
clean_restart=true
```

## Collision, stages, and objectives

- Wildlife damage uses an actual contact sensor instead of a loose
  center-distance approximation. Stomps require a believable above-body
  contact and late-apex/downward movement.
- Keys, survivors, patrol envelopes, checkpoints, and boss staging were
  re-spaced across all five stages. Ordinary enemies are kept at least 900 px
  from each boss.
- The stacked final Moose Pass bear and the Bear Country wolf that crowded the
  boss approach were removed.
- Required objective support, edge clearance, enemy clearance, minimum landing
  width, checkpoint support, and boss-checkpoint attack clearance are now
  asserted by the geometry audit.
- Checkpoints span the playable vertical range, so a launch-pad or high-route
  jump cannot skip them. Respawns are placed just above support rather than
  dropped high enough to repeat the next gap.
- Dark Winter's mover convergence has a reliable ground transfer even when the
  moving platform is out of phase.
- The exit beacon clearly reads `LOCKED · COMPLETE OBJECTIVES`, then changes to
  `EXIT READY` after the key, two rescues, and boss are complete. Its pulse is
  disabled by Reduced Motion.
- Locked-goal feedback lists the objectives that are actually missing. The key
  message no longer incorrectly claims the exit is ready before rescues/boss.

## Boss readability and pacing

- Each boss has stage-specific tell, attack, recovery, and reach values.
- A visible countdown, ground target marker, `DODGE!`, and `WEAK · FIRE!`
  status make the state machine readable without relying on color alone.
- The attack target locks when the windup begins. Eagle and salmon art keep
  facing that target, preventing mid-attack flip flicker.
- The boss checkpoint is 620 px before the arena, outside every attack reach.
- Recovery windows are 1.82–2.18 seconds. A 0.34-second hit cadence prevents
  accidental instant melting while still allowing a decisive punish window.
- Automated boss behavior now responds to the real telegraph and reports
  health, state, and active hazards during traversal.

This changed Moose Pass's deterministic completion from a marginal 108.97
seconds during development to 38.53 seconds in the final candidate.

## HUD and flow

- The runner card is 500 px wide, health has a reserved 120 px column, and the
  objective card has a dedicated non-overlapping region.
- Health is expressed as `HP n/3`; status is shortened to rings, combo, state,
  and route. Large Text omits the least important route suffix.
- HUD layout validation now checks worst-case six-digit current/best scores,
  route/state text length, objective text, and exact halfway progress placement.
- Game over is labeled `RUN ENDED`, not `EXPEDITION PAUSED`, and explains that
  saved progress is not erased.

## Final automated verification

The complete audit suite passed after the final code changes:

| Stage | Time | Key | Rescues | Boss | Hits | Deaths | Result |
|---|---:|---|---:|---|---:|---:|---|
| 0 Midnight Sun | 24.11s | yes | 2 | defeated | 2 | 1 | PASS |
| 1 Salmon Rush | 22.40s | yes | 2 | defeated | 0 | 0 | PASS |
| 2 Moose Pass | 38.53s | yes | 2 | defeated | 1 | 0 | PASS |
| 3 Dark Winter | 40.36s | yes | 2 | defeated | 0 | 0 | PASS |
| 4 Bear Country | 25.53s | yes | 2 | defeated | 1 | 0 | PASS |

Stage 0 intentionally exercises the production game-over and checkpoint
recovery path; it resumes at x=4540 and still completes. The other four
deterministic runs complete without death.

Evidence is stored in:

```text
test-results/android-gameplay/after/audits-5.4.1/
```

## Android build record

Build environment:

```text
Godot: 4.7.1 stable
Java: OpenJDK 17.0.19
Android compile/target SDK: 36
Android minimum SDK: 24
Android Build Tools: 36.1.0
package: com.jtripppiie.mooserush
versionCode: 541
versionName: 5.4.1
```

Built and verified APKs:

| APK | ABI | Bytes | SHA-256 |
|---|---|---:|---|
| `you-rush-alaska-5.4.1-debug.apk` | arm64-v8a | 95,036,246 | `f1ffb0ba445c46b42101d4be426f7406e4e720089dcee397c9523a3406d3f516` |
| `you-rush-alaska-5.4.1-x86_64-debug.apk` | x86_64 | 100,008,408 | `3649d1caed71a01df74ea11f59d0937925f02888719ab8c8969b20687e7ea5cd` |

Both APKs pass 16 KiB page-aware zip alignment and APK Signature Scheme v2
verification. They use the established Godot debug certificate, not a private
store signing key.

The x86_64 row is retained only as historical verification evidence. After
this build, the owner chose a single Android output: the ARM64 device APK. The
`Android Emulator Debug` preset was removed and must not be used for current
builds.

## Honest limitations

- This pass compiled both Android APKs but did not claim a new rendered Android
  play session. No ADB emulator daemon was available during the final export.
- The previously documented AVD still cannot render Godot 4.7.1 because its
  SwiftShader fragment-uniform limit is below the engine shader requirement.
- Final visual appearance, true simultaneous multi-touch, haptics, physical
  ergonomics, interruptions, thermals, and a 20-minute session remain
  `NOT VERIFIED ON PHYSICAL DEVICE`.
- Headless traversal proves system behavior and completion, not subjective
  touch feel or final renderer output.

Use `docs/DEVICE_ACCEPTANCE_CHECKLIST.md` on the child's Android device before
calling this a store-ready or physical-device-accepted release.

## Source record

```text
implementation commit: 761999be44c7585d78c498831bb24797fab9b042
push target: origin/main
```

The implementation commit contains the source, complete audit logs, release
record, and APK checksums. APK binaries remain ignored build outputs and are not
stored in Git.

## Computer review follow-up

The production game can now run directly on a computer without drawing Android
touch controls. The desktop menu exposes `COMPUTER REVIEW · IDS + NOTES`, and
`--computer-review` starts at the stage map with Review Mode enabled. A compact
bottom toolbar reports the nearest ID and provides clickable IDS/NOTE/EXIT
actions. F1, F10, and N provide the same actions from the keyboard.

World identifiers are now compact category-colored pills rather than long
ID-plus-description labels. The closest is highlighted, while the toolbar and
notebook retain the full description. This remains the same production game,
not a parallel desktop gameplay implementation or emulator APK.

The dedicated computer-review audit passes the desktop menu, absence of touch
controls, nearest-ID context, ID visibility toggle, compact notebook
pause/close flow, and Review Mode exit. It is part of the standard audit script
and therefore runs in CI.
