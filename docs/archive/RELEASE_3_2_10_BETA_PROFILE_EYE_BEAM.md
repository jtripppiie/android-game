# Release 3.30 Beta - Profile Eye-Beam

Final bear beam calibration after screenshot feedback showed the side-view
polar bear should not have two large visible red eye dots.

## Package

```text
versionCode: 330
versionName: 3.2.10-beta
build badge: ALASKA PASSPORT v3.30 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.10-beta-330-debug.apk
```

## What changed

- Recalibrated the final bear beam origin from the numbered preview grid:
  set the HTML profile-eye X origin to `717`, then moved the Android origin a
  matching touch right and down.
- Changed the emitter to one small visible profile-eye glint.
- Removed the second oversized eye dot for the hidden far eye.
- Simplified the Android beam renderer to a single thin line from the visible
  eye.
- Updated `tools/laser-eyes-preview.html` to match the profile-eye treatment
  and show numbered calibration grid coordinates in the report.

## QA focus

- Confirm the visible glint sits on the bear eye, not the mouth/chest.
- Confirm the preview report shows the intended profile-eye origin at `x=717`.
- Confirm there is only one small eye dot in the side-view pose.
- Confirm the beam remains thin and dodgeable while sweeping vertically.
