# Wildlife Scale Contract

Animated wildlife uses one source of truth: `WildlifeScale.java`. Values are
gameplay dp before responsive viewport scaling.

| Wildlife | Collision radius | Visible height | Silhouette half-width | Read |
|---|---:|---:|---:|---|
| Salmon | 15 | 28 | 30 | Small, long swimming threat |
| Wolf | 19 | 42 | 36 | Fast low predator |
| Eagle | 16 | 44 | 40 | Compact body with readable wingspan |
| Brown bear | 32 | 78 | 62 | Heavy quadruped, near runner height |
| Polar bear | 36 | 88 | 70 | Largest bear and greatest mass |
| Moose | 34 | 92 | 78 | Tallest/longest ground silhouette due to legs and antlers |

## Rendering contract

- Each animation frame is cropped to its visible alpha content before scaling.
- Visible height comes from the table, not transparent sprite-sheet padding.
- Ground wildlife is bottom-anchored to the same ground line.
- Eagle and salmon preserve their authored airborne/swimming centers.
- Shadows, warning effects and badges derive from the same width/height scales.
- Collision remains smaller than visible art for fair near misses, but its base
  radius comes from the same species entry.

## Regression coverage

`WildlifeScaleTest` enforces the major relationships: bears and moose are larger
than wolves, polar bears exceed brown bears, moose owns the longest ground
silhouette, and eagle/salmon mass stays below heavy ground wildlife.

