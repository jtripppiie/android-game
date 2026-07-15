# Wildlife Scale Contract

Animated wildlife uses one source of truth: `WildlifeScale.java`. Values are
gameplay dp before responsive viewport scaling.

| Wildlife | Collision radius | Visible height | Silhouette half-width | Read |
|---|---:|---:|---:|---|
| Salmon | 16 | 30 | 32 | Small, long swimming threat |
| Wolf | 21 | 56 | 42 | Fast low predator |
| Eagle | 17 | 50 | 44 | Compact body with readable wingspan |
| Brown bear | 36 | 104 | 74 | Heavy quadruped, roughly runner height |
| Polar bear | 40 | 116 | 82 | Largest bear and greater than runner height |
| Moose | 42 | 132 | 92 | Clearly taller/longer than the human runner |

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
