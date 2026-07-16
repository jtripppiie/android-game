# Godot Engine Migration

The `godot/` project is now the primary You Rush gameplay development target.
The Java/Canvas APK remains available as a rollback build until the engine
version passes device acceptance and all five Alaska stages reach parity.

## Implemented

- Handcrafted Chugach level rather than random obstacle spawning.
- Momentum acceleration, sprinting, friction, air control, variable jump,
  coyote time, jump buffering, crouching, trail dash, aerial stomp, landing
  feedback, and distinct animation states.
- Camera follow with smoothing and forward look-ahead.
- Sloped terrain, ledges, gaps, elevated and moving platforms, differentiated
  wolf/bear behavior, contact damage, knockback, invulnerability, stomp combat,
  combo snowball fire, and defeat feedback.
- Coin routes, required rescue key, two survivor rescues, checkpoint, respawn,
  and a rescue-beacon goal that validates all objectives.
- Smaller translucent touch controls that preserve visibility behind the player.
- Versioned six-frame `TRAIL RUNNER 2.0` sprite candidate with transparency.
- Authored-only Chugach geometry; the procedural overlay that stacked hazards
  over handcrafted terrain is disabled.
- Timed 2.15–2.8 second action chains, x1–x4 scoring, and saved best score.
- A complete readable boss loop: tell, attack, recovery-only damage, defeat,
  and objective-gated rescue beacon.
- Stable debug IDs beside level entities and automatic visible-ID note context.
- Engine-native 112 px compact notebook with priority, save, cancel, copy, and
  local-only `user://debug-review-notes.txt` storage.

## Local validation

```bash
python3 godot/validate_project.py
godot --headless --path godot --editor --quit
godot --headless --path godot --quit-after 240
godot --headless --path godot --export-debug "Android Debug"
```

Godot 4.7.1 and its matching export templates are required for editor validation
and APK export. `export_presets.cfg` writes the engine alpha APK to the existing
Android debug-output folder.

The project has been parsed in Godot 4.7.1, run headlessly for 240 frames with no
GDScript parse/runtime errors, exported as arm64 Android, signed, and verified.

## Migration boundary

The engine alpha is the primary development build and installs beside the Java
app under `com.jtripppiie.mooserush.overhaul`. It does not replace the store
package until device-tested input, audio, photo customization, all five Alaska
levels, and accessibility/privacy parity are complete.

Open Surge informed momentum and modular content goals, but no GPL code or
assets are copied. FinalRozGameNew informed handcrafted-level structure; no
Godot project files from that game are copied.
