# 5.3.1 Duplicate Entity Correction

Physical-device feedback exposed two authored/lifecycle defects that traversal
completion did not detect:

- Returning to the map left the previous `AlaskaStage` attached. Starting
  another stage added a second complete world and a second runner.
- Moose Pass combined its stage wildlife with an extra branch-route bear,
  crowding two large bear encounters into the same convergence section.

The main scene now owns exactly one stage. Menu/map transitions immediately
detach and free the previous world, stage startup refuses duplicate activation,
and a transition lock prevents rapid repeated button activation. The shared
route bear was removed because Moose Pass already authors wildlife around that
route.

The `--lifecycle-audit` regression starts stage 0, returns to the map, starts
stage 1, and asserts exactly one `active_stage` and one player after both
transitions. It passed, followed by a 5/5 traversal run.

The same source revision replaces the two-button movement cluster with a visible
four-way D-pad. Left/right auto-run, Up jumps, and Down crouches/stomps. The
right side now has deliberately distinct controls instead of matching generic
rectangles: a 150-by-150 gold Jump button, circular cyan Snow button, and
separate purple Dash button. The touch regression passed D-pad Up, simultaneous
movement/jump, auto-run, and independent release.

Status: validated source only. Do not claim or distribute a 5.3.1 APK until the
owner explicitly requests compilation.

Menu follow-up: primary menu buttons are now 82 pixels tall with 26-pixel
labels and 16-pixel corner radii. Main/customization screens use 20 pixels of
vertical separation, the six-button stage map uses 12 pixels so every option
and Back remain inside the viewport, and accessibility rows increase to 66
pixels with explicit spacing.

Readability follow-up: every gameplay action retains an internal label (`JUMP`,
`SNOW`, `DASH`) and the D-pad uses four directional symbols. Jump text is dark
on both its orange and bright pressed states; other actions use white on dark
cyan/purple. Menu pressed, focused, and disabled states now have explicit text,
fill, and border colors instead of inheriting ambiguous theme defaults.
