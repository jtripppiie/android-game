# Release 3.0.1 Beta - Debug Number Overlay

Focused phone-QA tooling update for faster visual and collision feedback.

## Version

versionCode: 301
versionName: 3.0.1-beta
build badge: ALASKA PASSPORT v3.0.1 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.0.1-beta-301-debug.apk

## What changed

- Added debug-only numbered badges over visible gameplay objects.
- Badges use type suffixes: `G` obstacle, `H` wildlife/sprite, `*` star, `P` pickup, `T` thrown snowball, `A` boss attack, and `B` boss.
- Expanded the debug panel with a short legend so phone-test notes can say things like `H3 has sprite artifacts` or `G1 collision feels too wide`.
- Kept the markers out of normal play; they render only when DEBUG is ON.

## QA focus

- Toggle DEBUG from the menu.
- Start a stage and confirm numbered badges appear only while DEBUG is ON.
- Confirm wildlife sprite artifact reports can be tied to the visible `H` number.
- Confirm the version badge reads `ALASKA PASSPORT v3.0.1 BETA`.
