# Godot Overhaul Vertical Slice

The `godot/` project is the next-generation You Rush gameplay prototype. It is
kept beside the shipping Java/Canvas app so migration can be tested without
destroying the stable Android implementation.

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

## Local validation

```bash
python3 godot/validate_project.py
godot --headless --path godot --editor --quit
godot --headless --path godot --export-debug "Android Debug"
```

Godot 4.7 and its matching export templates are required for editor validation
and APK export. `export_presets.cfg` writes the prototype APK to the existing
Android debug-output folder.

The project has been parsed in Godot 4.7 and run headlessly for 120 frames with
no GDScript parse or runtime errors.

## Migration boundary

The current Android app remains the installable beta until the Godot slice has
device-tested input, audio, persistence, photo customization, all five Alaska
levels, and feature parity for accessibility and local-first privacy.

Open Surge informed momentum and modular content goals, but no GPL code or
assets are copied. FinalRozGameNew informed handcrafted-level structure; no
Godot project files from that game are copied.
