# Release 3.29 Beta - Eye-Beam Calibration

Focused visual correction after device/preview feedback showed the final bear
beam still reading like a mouth/chest laser.

## Package

```text
versionCode: 329
versionName: 3.2.9-beta
build badge: ALASKA PASSPORT v3.29 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.9-beta-329-debug.apk
```

## What changed

- Moved the final bear eye-beam origin higher and slightly right to better line
  up with the polar bear sprite's eyes.
- Replaced the thick filled laser slab with thin stroked twin laser lines.
- Reduced the laser collision radius to match the thinner visual.
- Shrunk the eye glints so they read as eyes, not large red target dots.
- Updated `tools/laser-eyes-preview.html` with the same eye-origin and thin-line
  tuning.

## QA focus

- Confirm beams originate from the eyes, not the mouth, chest, or air in front
  of the face.
- Confirm beams are thin lines with glow, not a thick rectangular bar.
- Confirm the hitbox still feels fair while the beam sweeps vertically.
