# Visual Verification

## Current 5.3.1 composition audit

`current/` contains deterministic 1280-by-720 review frames built from the exact
game backgrounds and sprite sheets:

- `stage-1-midnight-sun.png`
- `stage-2-salmon-rush.png`
- `stage-3-moose-pass.png`
- `stage-4-dark-winter.png`
- `stage-5-bear-country.png`
- `composition-review.mp4`

These are clearly labeled composition reconstructions, not Godot screenshots.
They allow review of authored facing, relative sprite scale, ground alignment,
contrast, and approximate control crowding on a headless server. Regenerate:

```bash
python3 godot/tools/build_composition_audit.py
ffmpeg -y -framerate 0.33 -pattern_type glob \
  -i 'verification/current/stage-*.png' \
  -vf 'fps=30,format=yuv420p' -c:v libx264 -movflags +faststart \
  verification/current/composition-review.mp4
```

They do not replace real engine/phone captures. Camera transforms, animation,
physics timing, touch ergonomics, and final UI rasterization still require a
device or desktop graphics display.

## Legacy snapshots

The `sprite-running-*.png` files predate the Godot overhaul. They remain only
for historical comparison and are not current acceptance evidence.
