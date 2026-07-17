# You Rush 5.4 Technical Maintenance

## Source of truth

`godot/` is the current game. `godot/scenes/main.tscn` starts `main.gd`.
`app/src/main/` is the legacy Java rollback/migration source; do not implement
current gameplay there.

The current responsibilities are:

- `main.gd`: screen/world ownership, transitions, lifecycle, audit dispatch.
- `world.gd`: five authored stage layouts and gameplay composition.
- `player.tscn`: editable runner collision, presentation, effects, and camera.
- `player.gd`: movement/combat/scoring rules.
- `player_presentation.gd`: frame choice, measured shoe alignment, photo head.
- `runner_camera.gd`: bounded look-ahead and reduced-motion-aware trauma.
- `runner_effects.gd`: bounded deterministic snow contact particles.
- `game_hud.gd` and `touch_controls.gd`: responsive mobile interface.
- `review_registry.gd` and `review_notebook.gd`: optional compact review tools.
- `gameplay_auditor.gd` and `mechanics_auditor.gd`: production-code audits.
- `android_verification_harness.gd`: debug-only deterministic Android states.
- `GameSession`: schema-versioned profile, backup, migration, customization.
- `FeedbackService`: generated cues and haptic routing.
- `AndroidBridge`: speech notes, migration, debug intent test hooks.

Only `main.gd` owns the active stage. A transition detaches and frees the old
world before creating another. Restart audits must keep exactly one active
stage and one player.

## Save and migration contract

Godot saves `user://profile.cfg` with schema 3 and maintains
`profile-backup.cfg`. It stores selected/unlocked stage, best scores,
completion counts, best times/stars, total score, touch size,
accessibility/review settings, local photo
path, and migration state.

The Android bridge reads the old `moose_rush` SharedPreferences once. Never
rename the package, preference file, or bridge migration keys during an update
that must preserve existing players. Test updates with `adb install -r`; do not
clear data. Photo inputs are limited to 20 MB, center-cropped, resized once to
96×96, and cached.

## Validation

Use Godot 4.7.1 exactly:

```bash
GODOT_BIN=/path/to/godot python3 godot/validate_project.py
GODOT_BIN=/path/to/godot godot/tools/run_gameplay_audits.sh
```

The suite runs touch ownership, system/state ownership, lifecycle, pause,
movement mechanics, geometry, debug overlay, and full autoplay traversal for
all five stages. The validator checks the six runner alpha bounds and requires
every shoe line to meet the collision ground within 0.5 px. It also scans
Godot output because the editor can return exit code 0 while reporting a
GDScript parse failure.

Useful individual commands:

```bash
godot --headless --path godot -- --touch-audit
godot --headless --path godot -- --mechanics-audit
godot --headless --path godot -- --geometry-audit=0
godot --headless --path godot -- --debug-overlay-audit=0
godot --headless --path godot -- --autoplay-audit=0
```

Repeat stage arguments 0–4. Autoplay is traversal evidence, not proof of touch
feel, visual quality, haptics, or physical-device performance.

## Android build

Required: matching Godot 4.7.1 export templates, Java 17, SDK/target 36,
Build Tools 36.1.0, and NDK 29.0.14206865.

```bash
godot --headless --path godot --export-debug "Android Debug"
godot --headless --path godot --export-debug "Android Emulator Debug"
```

The first preset is ARM64 and produces
`app/build/outputs/apk/debug/you-rush-alaska-5.4.1-debug.apk`. The second is
x86_64 and produces `you-rush-alaska-5.4.1-x86_64-debug.apk`. Keeping them
separate avoids shipping two large Godot engine libraries in one test APK.

Inspect every handoff:

```bash
aapt dump badging path/to.apk
apksigner verify --verbose --print-certs path/to.apk
sha256sum path/to.apk
```

The custom Godot Android build template is generated/ignored because its engine
AARs are very large. Preserve the tracked `.gdignore`, plugin manifest, and
`YouRushBridge.java` when reinstalling the template.

## Deterministic Android verification

Debug APKs accept intent extras `verification_scenario` and
`verification_stage`. `ApplicationInfo.FLAG_DEBUGGABLE` and
`OS.is_debug_build()` both guard the hook, so a release build cannot activate
it. The harness uses the real player, objectives, boss, scoring, overlays, and
transition methods.

```bash
adb shell am start \
  -n com.jtripppiie.mooserush/com.godot.game.GodotAppLauncher \
  --es verification_scenario air-jump \
  --ei verification_stage 2
adb logcat -d | rg "VERIFICATION READY|VERIFICATION FAIL"
adb exec-out screencap -p > air-jump.png
```

Do not call a scenario PASS unless the ready record, expected production state,
runtime log, and evidence all agree.

## Performance and release rules

- The debug APK is dominated by the unstripped Godot native library; compare
  like-for-like architecture and build types before claiming asset savings.
- Repeated trees use parallax layers; contact particles are capped at 30.
- HUD text/layout updates are snapshot-cached, not rewritten every frame.
- Photo decode is cached; projectile/enemy pooling is not yet justified by the
  current bounded entity counts.
- Keep debug signing for direct tests only. The owner’s release key never
  belongs in Git.
- Complete the physical-device checklist before distribution. Emulator tests
  cannot certify true multi-touch, haptics, speech providers, OEM lifecycle,
  thermals, battery, or touch ergonomics.

The authoritative test record is
`test-results/android-gameplay/GAMEPLAY-VERIFICATION.md`.
