# You Rush 5.4 Gameplay Verification

Date: 2026-07-17
Starting commit: `f4739229ce49020ef56269d6094d87de2132fe39`
Final tested implementation commit: `71491403290e4cc7c8608775fe7aea15d7fc9cdb`
Evidence snapshot commit: `71491403290e4cc7c8608775fe7aea15d7fc9cdb`

## 1. Scope and source selection

Status: PASS

The README, current release notes, export presets, Android template, and source
layout were inspected before modification. `godot/` is the production game.
`app/src/main/` is the legacy Java rollback/migration source and was not
refactored as the current game.

The original commit is preserved in the clean worktree
`/tmp/android-game-baseline-f473922`. An emulator-only copy at
`/tmp/android-game-baseline-emulator` changes only the export architecture so
the same original source could be installed on an x86_64 AVD.

## 2. Test environment

| Item | Value |
|---|---|
| Godot | 4.7.1.stable.official.a13da4feb |
| Java | OpenJDK 17.0.19 |
| Android tools | sdkmanager 12.0; adb 37.0.0 |
| SDK / target | 36 / 36 |
| Build Tools | 36.1.0 |
| NDK | 29.0.14206865 |
| Emulator | AVD `yourush`, Android SDK built for x86_64 |
| Android | 11 / API 30 |
| Physical display | 1080×2340, 440 dpi |
| App orientation | Landscape |
| Captured app frame | 2340×1080 |
| Acceleration | CPU/TCG; KVM unavailable |
| Renderer tested | Android Emulator OpenGL ES Translator / SwiftShader GLES 3 |

Physical device status: NOT VERIFIED ON PHYSICAL DEVICE

## 3. Build and install commands

Original ARM64 build:

```bash
cd /tmp/android-game-baseline-f473922
/tmp/godot-471-check --headless --path godot --export-debug "Android Debug"
```

Reworked builds:

```bash
cd /home/jt/projects/android-game
/tmp/godot-471-check --headless --path godot --export-debug "Android Debug"
/tmp/godot-471-check --headless --path godot --export-debug "Android Emulator Debug"
```

Install and launch:

```bash
adb install -r app/build/outputs/apk/debug/you-rush-alaska-5.4.0-x86_64-debug.apk
adb shell am start \
  -n com.jtripppiie.mooserush/com.godot.game.GodotAppLauncher \
  --es verification_scenario main-menu \
  --ei verification_stage 0
```

Reproducible scenario capture is implemented in
`godot/tools/capture_android_evidence.sh`.

## 4. APK metadata and checksums

| APK | Metadata | Size | SHA-256 | Status |
|---|---|---:|---|---|
| Baseline ARM64 5.3.2 | versionCode 532; min 24; target 36 | 94,980,899 bytes | `e4fa8926792491edad0296c3fbeef92830a89507bebeaf793715ed33b609809b` | PASS |
| Reworked ARM64 5.4.0 | versionCode 540; min 24; target 36 | 95,027,990 bytes | `eda9e54de358261d3d4222a4cf18255efd45fde2ae985715e947ef1553fb5e96` | PASS |
| Reworked x86_64 5.4.0 | versionCode 540; min 24; target 36 | 100,000,152 bytes | `cf68ee08aa39d7eed073af79c051773a4f48e7854833ff4f10d9a79210eadb0a` | PASS |

Both reworked APKs use Android v2 debug signing. Debug signing is not a public
release signature.

The architecture split is intentional. A combined ARM64+x86_64 debug export was
about 170 MB; separate test APKs avoid shipping an unused native engine.
Inspection of the x86_64 APK found the unstripped `libgodot_android.so` was
81,182,312 bytes, so asset deletion alone would not materially solve debug APK
size.

## 5. Baseline validation and audits

Status: PASS

Commands were confirmed from the source and run at the starting commit:

```bash
python3 godot/validate_project.py
godot --headless --path godot -- --touch-audit
godot --headless --path godot -- --system-audit
godot --headless --path godot -- --lifecycle-audit
godot --headless --path godot -- --pause-audit
godot --headless --path godot -- --geometry-audit=STAGE
godot --headless --path godot -- --autoplay-audit=STAGE
```

Baseline results:

| Stage | Geometry | Autoplay result |
|---:|---|---|
| 0 | 7 surfaces; max gap 120; rise 90 | PASS — 36.18 s, x 5397.2, 25 jumps, 4 hits, score 376 |
| 1 | 7 surfaces; max gap 180; rise 80 | PASS — 24.51 s, x 5400.7, 15 jumps, 3 hits, score 392 |
| 2 | 9 surfaces; max gap 110; rise 120 | PASS — 17.15 s, x 5393.7, 10 jumps, 2 hits, score 760 |
| 3 | 8 surfaces; max gap 170; rise 140 | PASS — 48.66 s, x 5938.1, 28 jumps, 2 hits, score 284 |
| 4 | 7 surfaces; max gap 180; rise 180 | PASS — 36.70 s, x 5397.2, 19 jumps, 6 hits, score 658 |

Touch, system, lifecycle, and pause audits: PASS

## 6. Baseline Android findings

Status: FAIL

The baseline x86_64 APK installed and launched, but SwiftShader could not link
Godot’s canvas/scene shader:

```text
Fragment shader active uniforms exceed GL_MAX_FRAGMENT_UNIFORM_VECTORS (261)
```

The driver maximum is 256. The result was a gray frame, not rendered gameplay.
`before/01-launch.png`, `before/02-main-menu.png`, and `before/logcat.txt`
record the result. CPU-only emulation also caused Android System UI ANR dialogs.
Compilation and headless traversal were not substituted for Android visual
verification.

## 7. Defects found before refactoring

- Player collision, sprite, photo, and camera were constructed invisibly in
  `_ready()`.
- A zero-health hit immediately conflicted with clean game-over/restart flow.
- Completion returned too quickly and did not present useful run results.
- Review registration and multiple audit modes were embedded in `world.gd`.
- The HUD was flat, compact, and updated text/layout more often than necessary.
- The main menu lacked a strong framed hierarchy.
- The validator used brittle source strings and could report success even when
  Godot logged a parse failure with exit code 0.
- One export bundled ARM64 and x86_64, inflating the test APK.
- Photo files were decoded and resized for each new player instance.
- Autoplay could freeze permanently when the new game-over overlay paused the
  tree; this regression was discovered and fixed during this work.

## 8. Architecture changes

Status: INTENTIONALLY CHANGED

- Added editable `player.tscn`.
- Extracted `PlayerPresentation`, `RunnerCamera`, and `RunnerEffects`.
- Extracted `ReviewRegistry`, `GameplayAuditor`, and `MechanicsAuditor`.
- Added focused game-over and stage-complete overlays.
- Added debug-only Android deterministic verification harness.
- Centralized world disposal/restart/advance ownership in `main.gd`.
- Profile schema 3 now keeps backup, completion count, best time, best stars,
  touch scale, bounded photo cache, and existing migration state.
- CI now validates/builds the Godot game, not the legacy Java source.

## 9. Gameplay and presentation changes

Status: INTENTIONALLY CHANGED

- Six runner frames are alpha-measured; maximum shoe/contact error is 0.21 px.
- Landing shadow, bounded snow particles, camera look-ahead, and deterministic
  trauma improve grounding. Reduced Motion disables motion effects.
- Dash retains bounded vertical momentum.
- Game over offers restart/map; completion offers next/replay/map.
- Result screen grades score, elapsed time, hits, and 0–3 stars.
- Stage map shows best score, best time, and stars.
- HUD has 74 px cards, larger type, shadows, explicit objective states, 8 px
  progress, and route marker.
- Main menu uses a centered responsive frame and 88 px primary actions.
- Touch size can be saved as Small, Standard, or Large.
- Terrain has mipmapped filtering and overlapping snow crests.
- Mountains and trees use separate parallax depths.
- Compact review IDs use `S3-PF04` form with four nearby labels maximum.

## 10. Reworked automated audit results

Status: PASS

Exact logs are under `after/audits/`. Final results:

| Stage | Geometry / objectives | Autoplay |
|---:|---|---|
| 0 | PASS — 7 surfaces, gap 120, rise 90, 1 key, 2 rescues, 1 boss, 1 goal | PASS — 36.20 s, 25 jumps, 4 hits, 1 recovery, score 301 |
| 1 | PASS — 7 surfaces, gap 180, rise 80, 1 key, 2 rescues, 1 boss, 1 goal | PASS — 21.85 s, 16 jumps, 2 hits, 1 recovery, score 392 |
| 2 | PASS — 9 surfaces, gap 110, rise 120, 1 key, 2 rescues, 1 boss, 1 goal | PASS — 17.14 s, 10 jumps, 2 hits, 0 recoveries, score 760 |
| 3 | PASS — 8 surfaces, gap 170, rise 140, 1 key, 2 rescues, 1 boss, 1 goal | PASS — 46.13 s, 29 jumps, 2 hits, 1 recovery, score 160 |
| 4 | PASS — 7 surfaces, gap 180, rise 180, 1 key, 2 rescues, 1 boss, 1 goal | PASS — 36.70 s, 19 jumps, 6 hits, 2 recoveries, score 658 |

Other final audits:

| Audit | Result |
|---|---|
| Project structure/assets/Godot parse | PASS |
| Touch ownership, drift, diagonal, sizes 0.85–1.15 | PASS |
| Movement mechanics | PASS |
| Pause/resume and frozen feedback clock | PASS |
| Lifecycle, one world, one runner | PASS |
| Score, 3-star result, save, clean replay | PASS |
| Debug IDs, notes size, HUD layout, stages 0–4 | PASS |

Mechanics facts:

```text
right=120.9 left=84.7 short_jump=96.1 full_jump=268.9 dash=104.0
air_jump=one third_jump=blocked stomp=true snowball=1
game_over=true clean_restart=true
```

## 11. Five-stage verification matrix

These statuses are based on automated production-code audits, not Android
visual rendering.

| Scenario | S1 | S2 | S3 | S4 | S5 |
|---|---|---|---|---|---|
| Reachable main geometry | PASS | PASS | PASS | PASS | PASS |
| Exactly one key/two rescues | PASS | PASS | PASS | PASS | PASS |
| Exactly one boss/goal | PASS | PASS | PASS | PASS | PASS |
| Full objective traversal | PASS | PASS | PASS | PASS | PASS |
| Boss defeated and goal completes | PASS | PASS | PASS | PASS | PASS |
| Duplicate world/player prevention | PASS | PASS | PASS | PASS | PASS |
| Android rendered play | FAIL | FAIL | FAIL | FAIL | FAIL |
| Physical touch feel | NOT VERIFIED ON PHYSICAL DEVICE | NOT VERIFIED ON PHYSICAL DEVICE | NOT VERIFIED ON PHYSICAL DEVICE | NOT VERIFIED ON PHYSICAL DEVICE | NOT VERIFIED ON PHYSICAL DEVICE |

## 12. Controls and flow verification

| Test | Method | Status |
|---|---|---|
| Left/right and sprint | Mechanics audit | PASS |
| Short vs full jump | Measured 96.1 vs 268.9 | PASS |
| One air jump, no third | Mechanics audit | PASS |
| Dash / stomp / snowball | Mechanics audit | PASS |
| Simultaneous move+jump action ownership | Touch audit | PASS |
| Game over and clean restart | Mechanics audit | PASS |
| Pause freezes player and HUD timer | Pause audit | PASS |
| Completion/save/stars/replay | System audit | PASS |
| Touch buttons visible on rendered Android frame | Android SwiftShader | FAIL |
| True simultaneous multi-touch | Physical device | NOT VERIFIED ON PHYSICAL DEVICE |
| Background/foreground OEM recovery | Physical device | NOT VERIFIED ON PHYSICAL DEVICE |
| Haptic behavior | Physical device | NOT VERIFIED ON PHYSICAL DEVICE |
| Speech recognition | Physical device | NOT VERIFIED ON PHYSICAL DEVICE |

## 13. Screenshot evidence

| Evidence | State | Status |
|---|---|---|
| `before/01-launch.png` | Baseline launch with CPU-emulator System UI ANR | FAIL |
| `before/02-main-menu.png` | Baseline gray shader-failure frame | FAIL |
| `after/02-main-menu.png` | Reworked launch with CPU-emulator System UI ANR | FAIL |
| `comparison/02-main-menu-side-by-side.png` | Derived comparison of the two captured failures | NOT VERIFIED |
| Required gameplay screenshots 03–32 | Blocked before a rendered frame | NOT VERIFIED |

No sprites were manually moved and no mock gameplay scene was used.

## 14. Video evidence

Status: NOT VERIFIED

No gameplay video is claimed. The renderer failed before a trustworthy frame,
so recording gray/ANR output would not establish behavior. Reproducible real
scenario/video capture commands are in `capture_android_evidence.sh` for a
compatible emulator or physical debug device.

## 15. Android logcat findings

Status: FAIL

Both baseline and reworked logs contain the 261-vs-256 fragment uniform shader
link failure. The reworked log contains no Java `FATAL EXCEPTION`, native fatal
signal, missing asset, invalid Godot node, audio lifecycle crash, or app OOM.
The Android System process produced slow-dispatch/ANR behavior under TCG.

This is an emulator/driver incompatibility that blocks the completion gate for
rendered Android gameplay on this host. It is not converted to PASS.

## 16. Migration test

Status: PASS

Because baseline rendering could not navigate the UI, a documented baseline
profile fixture was injected through `run-as`, without clearing app data:

`records/migration-baseline-profile.cfg`

It contains stage 2 unlocked, total score 1234, and best scores 500/650. Baseline
fixture SHA-256:

```text
f5a0209cb1487c4567225f650752f7d8b552c81926cbe37be8e06f1b641ad865
```

After `adb install -r` from 5.3.2 to 5.4.0, the private profile checksum was
identical. The app package version became 540 without clearing data. Schema 3
loads missing new fields with safe defaults.

## 17. Performance findings

| Finding | Status |
|---|---|
| HUD snapshot/progress caching | PASS |
| Photo decode/resize caching | PASS |
| Effects capped at 30 particles | PASS |
| Separate architecture APKs | INTENTIONALLY CHANGED |
| Repeated restart duplicate nodes | PASS |
| Headless traversal times comparable | PASS |
| Android frame stability/input latency | NOT VERIFIED |
| Android memory growth | NOT VERIFIED |
| 20-minute Android rendered session | NOT VERIFIED |
| Low-end physical performance/thermals/battery | NOT VERIFIED ON PHYSICAL DEVICE |

No claim is made that Android runtime performance improved. The available AVD
could not render and CPU emulation was not a representative performance target.

## 18. Regressions introduced and fixed

| Regression | Resolution | Status |
|---|---|---|
| Typed packed-array constant caused a GDScript parse failure | Replaced with a constant Array and strengthened log-aware validator | PASS |
| Validator accepted Godot exit 0 despite parse error | Fatal script markers now fail validation | PASS |
| New game-over pause froze autoplay after zero health | Auditor runs always, records death, resumes through production respawn | PASS |
| Completion compared score after overwriting previous best | Previous best is captured before save/result configuration | PASS |
| Profile test mutated new completion/star fields | System audit now snapshots/restores all profile arrays | PASS |

## 19. Intentional behavior changes

- Zero health opens game over instead of silently respawning.
- Falling to a checkpoint applies a small score penalty but does not count as
  zero-health game over.
- Stage completion waits for a player choice.
- Results now grade score/time/damage and save stars/best time.
- Camera motion feedback is bounded and accessibility-aware.
- Review IDs are shorter and less numerous.
- The Android debug test harness is unavailable in release builds.

Status: INTENTIONALLY CHANGED

## 20. Remaining limitations

- Android SwiftShader on this AVD cannot link the Godot 4.7.1 shader.
- Visual review of final menus, HUD, terrain seams, parallax, bosses, and touch
  placement must be repeated on a compatible renderer/device.
- No signed store release was produced.
- No cloud save, leaderboard, endless mode, or online service was added.
- Audio remains local generated cues rather than a full recorded soundtrack.

Status: NOT VERIFIED

## 21. Physical-device-only items

True multi-touch, haptic strength, speech recognizer integration, OEM photo
picker behavior, interruptions, rotation, thermals, battery, one-handed
ergonomics, and 20-minute device stability:

Status: NOT VERIFIED ON PHYSICAL DEVICE

## 22. Exact reproduction

```bash
git worktree add /tmp/verify-baseline f4739229ce49020ef56269d6094d87de2132fe39
cd /home/jt/projects/android-game
GODOT_BIN=/tmp/godot-471-check python3 godot/validate_project.py
GODOT_BIN=/tmp/godot-471-check godot/tools/run_gameplay_audits.sh
/tmp/godot-471-check --headless --path godot --export-debug "Android Debug"
/tmp/godot-471-check --headless --path godot --export-debug "Android Emulator Debug"
sha256sum app/build/outputs/apk/debug/you-rush-alaska-5.4.0-*-debug.apk
adb install -r app/build/outputs/apk/debug/you-rush-alaska-5.4.0-x86_64-debug.apk
ADB=/path/to/adb godot/tools/capture_android_evidence.sh \
  app/build/outputs/apk/debug/you-rush-alaska-5.4.0-x86_64-debug.apk
```

## 23. Commit and push

Implementation commit: `71491403290e4cc7c8608775fe7aea15d7fc9cdb`
Evidence snapshot commit: `71491403290e4cc7c8608775fe7aea15d7fc9cdb`
Remote push: `origin/main` through the evidence snapshot, confirmed 2026-07-17

The baseline worktree remains independently buildable at the exact starting
commit. APK binaries are build outputs and are not committed.

## 24. Version 5.4.1 supplemental gameplay pass

The follow-up gameplay refinement began at
`09312de8e40c7ad69669a22df351d9e54ef8c442` and targeted movement intent,
touch drift, wildlife contact, stage spacing, checkpoint grounding, boss
telegraphs/pacing, HUD density, and objective clarity.

All touch, system, lifecycle, pause, mechanics, geometry, debug-overlay, and
autoplay audits passed in the final 5.4.1 source state. Per-stage evidence is
stored under `after/audits-5.4.1/`. Traversal times were 24.11s, 22.40s,
38.53s, 40.36s, and 25.53s for stages 0–4. Every run collected one key,
completed two rescues, defeated one boss, and reached one goal.

The ARM64 and x86_64 Android APKs compiled as versionCode 541/versionName 5.4.1.
They passed package inspection, ABI inspection, v2 signature verification, and
16 KiB page-aware alignment:

| APK | Bytes | SHA-256 | Status |
|---|---:|---|---|
| ARM64 device | 95,036,246 | `f1ffb0ba445c46b42101d4be426f7406e4e720089dcee397c9523a3406d3f516` | PASS |
| x86_64 emulator | 100,008,408 | `3649d1caed71a01df74ea11f59d0937925f02888719ab8c8969b20687e7ea5cd` | PASS |

No new rendered Android session is claimed for 5.4.1: the final build host had
no running ADB emulator, and the previously recorded SwiftShader limitation
still applies. Physical-device visual, ergonomic, haptic, thermal, and
long-session checks remain `NOT VERIFIED ON PHYSICAL DEVICE`.

See `docs/RELEASE_5_4_1_GAMEPLAY_REFINEMENT.md` for the complete change and
build record. The 5.4.1 implementation commit is
`761999be44c7585d78c498831bb24797fab9b042`.

## 25. Single Android APK decision

After the 5.4.1 verification build, the owner removed the separate emulator
artifact from the production workflow. `godot/export_presets.cfg` now contains
one Android preset:

```text
Android Debug → arm64-v8a → you-rush-alaska-5.4.1-debug.apk
```

The earlier x86_64 commands and checksums in this report remain historical
evidence of tests already performed; they are not current build instructions.
Current validation explicitly rejects an `Android Emulator Debug` preset,
`architectures/x86_64=true`, or an `x86_64-debug.apk` export path.

## 26. Computer Review Mode and restored identifiers

Desktop play now omits the Android touch overlay and provides a compact bottom
toolbar. The toolbar shows keyboard controls during ordinary computer play and
the nearest stable item ID during Review Mode. F1 toggles review, F10 toggles
the four nearest color-coded ID pills, and N opens the 480×292 tagged notebook.
The same actions are available as small toolbar buttons.

The standard audit runner now includes `--computer-review-audit`. It verifies
the desktop menu entry, no touch overlay, nearest `S1-` identifier, ID
hide/show, notebook pause/close, and clean Review Mode exit.

Evidence:

```text
after/audits-5.4.1/computer-review.log
COMPUTER REVIEW AUDIT PASS · menu · no touch overlay · nearest ID · F1/F10/N flow
```

The existing five-stage traversal suite was repeated after the overlay changes
and passed without script errors. No Android APK was compiled for this
desktop/debug-tool follow-up.
