# Release 5.4.2 — Computer Review and Debug Identifiers

Version 5.4.2 correctly versions the Computer Review and identifier overhaul as
a new Android release. It targets the current `godot/` game and produces one
ARM64 phone/tablet APK.

Starting source commit:

```text
827ee0f615d16d54f1b37b6cba405990c97dec39
```

Validated implementation commit:

```text
75d3233d1bbd886c834e66f60386dfacacc533b9
```

## Release identity

```text
package: com.jtripppiie.mooserush
versionCode: 542
versionName: 5.4.2
engine: Godot 4.7.1 stable
Java: OpenJDK 17.0.19
compile/target SDK: 36
minimum SDK: 24
Build Tools: 36.1.0
ABI: arm64-v8a only
```

The old `Android Emulator Debug` preset remains removed. Validation rejects an
x86_64 export path or `architectures/x86_64=true`.

## Computer Review

- Desktop gameplay no longer draws the phone D-pad and action buttons.
- The main menu offers `COMPUTER REVIEW · IDS + NOTES`.
- `godot --path godot -- --computer-review` starts directly at the map with
  Review Mode enabled.
- A 46 px bottom toolbar shows keyboard controls during normal computer play.
  In Review Mode it shows the nearest stable item identifier and clickable
  IDS, NOTE, and EXIT actions.
- F1 toggles Review Mode, F10 toggles IDs, and N opens the notebook.
- `--touch-preview` remains available only when deliberately reviewing the
  Android control layout on a computer.

## Identifiers and notes

- IDs remain stable and compact: `S#-CATEGORY##`.
- Only the four nearest IDs are displayed.
- Platforms/checkpoints, wildlife/bosses, pickups, water/ice, and goals use
  distinct pill colors.
- The closest item uses a bright green highlighted pill.
- World labels show only the ID; full descriptions stay in the toolbar and
  notebook.
- Long-platform badges follow the nearest point on that surface.
- Notes automatically record the nearest ID, nearby IDs, stage, position,
  score, combo, key, and rescue status.
- The notebook remains a 480×292 top-right panel, preserving the game view.

## Bug found during release audit

When entering a stage with Review Mode already enabled, every authored badge
could be visible for one frame before distance filtering ran. Badges now start
hidden and are revealed only through the four-nearest filter.

## Verification

The complete final suite passed:

```text
project validation
touch-control ownership and drift
score/save/system flow
world lifecycle and duplicate prevention
pause/resume
movement mechanics and clean restart
Computer Review menu/no-touch/nearest-ID/F1-F10-N flow
geometry for stages 0–4
debug overlay for stages 0–4
autoplay traversal for stages 0–4
```

Stage results:

| Stage | Time | Key | Rescues | Boss | Hits | Deaths | Result |
|---|---:|---|---:|---|---:|---:|---|
| 0 Midnight Sun | 24.11s | yes | 2 | defeated | 2 | 1 | PASS |
| 1 Salmon Rush | 22.40s | yes | 2 | defeated | 0 | 0 | PASS |
| 2 Moose Pass | 38.53s | yes | 2 | defeated | 1 | 0 | PASS |
| 3 Dark Winter | 40.36s | yes | 2 | defeated | 0 | 0 | PASS |
| 4 Bear Country | 25.53s | yes | 2 | defeated | 1 | 0 | PASS |

Stage 0 exercises the production game-over and checkpoint recovery path. All
other deterministic runs complete without death. Every stage collected the
key, completed two rescues, defeated one boss, and reached one goal.

Committed logs are under:

```text
test-results/android-gameplay/after/audits-5.4.2/
```

## ARM64 APK

```text
path: app/build/outputs/apk/debug/you-rush-alaska-5.4.2-debug.apk
bytes: 95,045,941
SHA-256: 3d237191980b96be875f3150fee94adabcf730f7bf879a3c9df4d14b5fb09369
signature: APK Signature Scheme v2 PASS
16 KiB page-aware alignment: PASS
native library: lib/arm64-v8a/libgodot_android.so
x86_64 library: absent
```

All older APKs were removed from the local output directory. Only the 5.4.2
ARM64 APK remains.

## Honest limitations

Headless audits verify gameplay state, traversal, geometry, UI contracts, and
the Computer Review workflow. They do not prove final appearance or touch feel
on the child's phone. Physical-device visuals, simultaneous multi-touch,
haptics, interruptions, thermals, and a 20-minute session remain
`NOT VERIFIED ON PHYSICAL DEVICE`.

Use `docs/DEVICE_ACCEPTANCE_CHECKLIST.md` before treating this as a
physical-device-accepted release.
