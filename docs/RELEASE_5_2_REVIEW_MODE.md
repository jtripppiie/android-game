# 5.2 Review Mode

Version 5.2 makes review tools explicit instead of covering normal gameplay.
Enable `REVIEW MODE · IDS + NOTES` from Accessibility. The preference persists;
when it is off, object labels and the mobile `NOTE`/`IDS` controls are absent.

During review, `IDS` toggles the compact authored identifiers. `NOTE` pauses the
simulation and opens a 454-by-166 panel in the top-right, leaving most of the
scene visible. It identifies the nearest authored item, offers five fast issue
tags (`FEEL`, `JUMP`, `SPACE`, `ART`, `BUG`), accepts typed or Android voice
input, tracks the local note count, and copies the full log on request.

Saved entries include the tag, optional `FIX FIRST` priority, timestamp, stage,
player position, score, combo, objective state, and all nearby identifiers. The
log remains app-local at `user://debug-review-notes.txt` until the player copies
or clears application data.

Identifiers use independent per-category counters (`PF-1`, `AN-1`, `PU-1`),
render only within the nearby review radius, and highlight the nearest item in
green. This keeps screenshots legible while preserving an exact tweak target.

The automated traversal harness was also corrected during this release. It now
slows and settles under objectives, initiates pickup jumps from the ground,
separates traversal recovery from boss behavior, and holds a deterministic
firing direction in arenas. Five fresh full-campaign passes completed 25/25
stages after these changes.
