# Release 3.28 Beta - Gear and Obstacle Sprites

Feature and readability pass focused on making obstacles look like real Alaska
objects, adding a scarce survival-gear mechanic, and making the final bear eye
beam read as an actual eye attack.

## Package

```text
versionCode: 328
versionName: 3.2.8-beta
build badge: ALASKA PASSPORT v3.28 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.8-beta-328-debug.apk
```

## What changed

1. Added `obstacle_river_log.xml` as a transparent vector sprite.
2. Added `obstacle_antler_barricade.xml` as a transparent vector sprite.
3. Added `obstacle_iceberg.xml` as a transparent vector sprite.
4. Added `obstacle_snowbank.xml` as a transparent vector sprite.
5. Registered obstacle sprites in `GameAssets`.
6. Wired Salmon Rush river logs to the river-log sprite.
7. Wired Moose Pass obstacles to the antler-barricade sprite.
8. Wired Dark Winter obstacles to the iceberg sprite.
9. Wired Bear Country obstacles to the snowbank sprite.
10. Kept procedural obstacle rendering as fallback if a sprite asset is missing.
11. Added SPRAY pickups as scarce survival gear.
12. Added bear spray charges, cooldown, run counter, and HUD icon.
13. Kept tap-FIRE as snowballs.
14. Added hold-FIRE bear spray activation.
15. Anchored bear spray to the runner's forward hand.
16. Added an orange cone/mist spray visual.
17. Bear spray now stuns close wildlife.
18. Bear spray can interrupt close boss lunges and create breathing room.
19. Final bear eye beams now originate from the eye position, use small eye
    glints, and sweep vertically toward the runner.
20. Added/updated HTML debug tools: `tools/laser-eyes-preview.html` and
    `tools/debug-tuning-dashboard.html`.

## Why vector sprites

These obstacle sprites are Android vector drawable sprites instead of bitmap
PNGs. That keeps their edges crisp on every phone size, avoids transparent-edge
PNG artifacts, and makes the shapes easy to tune in source control.

## QA focus

- Confirm river logs no longer read as racks.
- Confirm snowbanks, icebergs, and antler barricades have distinct silhouettes.
- Confirm bear spray appears from the runner's hand, not the body center or
  screen space.
- Confirm bear spray is useful but scarce and does not replace snowballs.
- Confirm the final bear eye beam starts at the eyes, uses small eye glints,
  sweeps vertically, and can be dodged instead of feeling like a fixed chest
  laser.
- Open `tools/laser-eyes-preview.html` to compare beam style/readability before
  further tuning.
