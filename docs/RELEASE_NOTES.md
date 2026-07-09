# Release Notes

Current release details are tracked in `docs/VERSIONING.md` and the root
`README.md`.

## Latest Beta

```text
3.2.21-beta
versionCode 341
ALASKA PASSPORT v3.41 BETA
```

Highlights:

- Reframed the main-menu runner preview so the standing legs stay inside the
  visible landscape preview area instead of clipping off the bottom.
- Reduced and refined the default fallback head proportions so the menu sprite
  reads cleaner and less blocky.
- Updated the menu layout preview to match the Android runner framing and head
  proportions.
- Tightened the default red runner-body atlas crop with the shared seam guard
  and disabled smoothing on that draw path to stop running-frame artifacts from
  neighboring sprite frames.
- Updated the offline sprite audit and debug workbench runtime crop overlays to
  match the Android guarded crop.
- Anchored driftwood and river logs to the ground/waterline so they no longer
  appear to float in runs or the obstacle preview.
- Added the straighter `1.5` degree vector river log to the Android asset and
  mirrored it in the obstacle HTML preview.
- Added a temporary left-side bear chase that speeds up the run, can be escaped
  by clean vaults or survival time, and can be interrupted with rear bear spray.
- Reworked driftwood and river-log obstacle art with organic silhouettes,
  stage-specific coloring, and no baked-in target marker on driftwood.
- Added HUD and result-panel text fitting so dense labels stay inside their
  available space across preview/device sizes.
- Added FLOW gameplay for clean vault streaks, with bonus scoring, pickup pull,
  HUD timer feedback, and run-summary stats.
- Improved runner reset behavior and proportional ground-line placement.
- Hardened runner and wildlife sprite-sheet sampling to reduce adjacent-frame
  bleed and late-frame clipping.
- Added an offline debug workbench and local tools index for sprite crops,
  contact-line checks, gameplay composition, and PNG visual snapshots.
- Refreshed launcher branding across adaptive, monochrome, and legacy icons.
- Expanded the sprite-sheet audit tool with Android runtime crop overlays.
- Preserved detailed historical release notes under `docs/archive/`.

## Historical Releases

Detailed older release notes are archived in `docs/archive/`.
