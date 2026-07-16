# You Rush 5.0 Technical Maintenance

## Active architecture

`godot/scenes/main.tscn` loads `main.gd`. `GameSession` owns local persistence;
`FeedbackService` owns generated audio/haptics; `AndroidBridge` wraps the v2
Godot Android plugin for speech notes and one-time Java SharedPreferences
migration. `AlaskaStage` authors five deterministic stages rather than stacking
procedural obstacles. The Java app is rollback/reference code only.

The bridge metadata is in `godot/android/build/src/main/AndroidManifest.xml` and
the Java implementation is `.../YouRushBridge.java`. The build template itself
is generated/ignored because its two engine AARs exceed 200 MB. After reinstalling
a Godot Android build template, preserve/reapply those tracked bridge files and
the empty `.gdignore`; it prevents Gradle resources from being imported as game
assets. `godot/android/.build_version` must match `4.7.1.stable`.

## Save contract

Godot saves `user://profile.cfg`: unlocked stage, five best scores, total score,
accessibility, photo path, and migration flag. The first Android launch reads
the existing `moose_rush` SharedPreferences. It maps unlock, selected stage,
best score, XP/total, and mute, then marks the migration complete. Never rename
the package or preference file during an update that must migrate players.

## Automated verification

```bash
python3 godot/validate_project.py
./gradlew testDebugUnitTest
```

Run every stage headlessly (replace the executable path as needed):

```bash
godot --headless --path godot -- --stage-smoke=0 --quit-after 180
```

Repeat for 0 through 4. Android export requires the matching Godot 4.7.1 binary,
templates, Java 17, SDK/target 36, and Gradle network cache. Inspect the result:

```bash
aapt dump badging app/build/outputs/apk/debug/you-rush-alaska-5.2.0-debug.apk
apksigner verify --verbose app/build/outputs/apk/debug/you-rush-alaska-5.2.0-debug.apk
```

## Release rules

- Increment `version/code`; Android refuses updates with an equal/lower code.
- Debug signing is for direct testing, not an app-store production release.
- A production key belongs to the owner and must never enter Git.
- Run the real-device checklist. Emulator/headless tests cannot certify touch,
  speech providers, haptics, lifecycle interruptions, or OEM photo pickers.
- Tag the exact accepted commit and archive its APK plus checksum.

## Privacy and recovery

Normal play needs no account/network. Photos and notes remain in app-local/device
storage; speech recognition is provided by the installed Android recognizer and
may follow that provider's privacy terms. Do not put user notes or photos in Git.
Keep 4.2.2 and the last accepted 5.x APK as rollback artifacts.
