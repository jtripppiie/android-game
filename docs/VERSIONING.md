# Versioning

The active Godot Android package is `com.jtripppiie.mooserush`.

Current development version:

```text
versionCode: 541
versionName: 5.4.1
engine: Godot 4.7.1
metadata: godot/export_presets.cfg
```

Version 5.4.1 is the current compiled candidate. Version 5.4.0 remains the
rollback until 5.4.1 passes real-device visual and gameplay acceptance.
The project produces one ARM64 phone/tablet APK. There is no x86_64 emulator
APK preset.

The Java 4.2.2 project remains rollback/reference code. Its version fields in
`app/build.gradle` do not describe the current Godot APK.

For every installable release:

1. Increase `version/code`; Android rejects an equal or lower update.
2. Set the matching human-readable `version/name`.
3. Update the root README, maintenance commands, and release record.
4. Run three complete traversals of all five stages.
5. Export Android, inspect package/version/bridge metadata, and verify signing.
6. Confirm the update certificate matches the previous accepted build.
7. Run the real-device acceptance checklist before public distribution.

Debug builds use the established Android debug certificate for update and save
migration testing. A public store build must use the owner’s private release key;
never commit that key.
