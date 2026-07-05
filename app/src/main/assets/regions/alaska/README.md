# Alaska Asset Pack

This folder is the first real regional asset pack for You Rush.

The Android runtime now uses generated raster assets from `app/src/main/res/drawable-nodpi/`.
This folder keeps Alaska region metadata only.

## Runtime asset registry

- `region.json`

## Rule

Do not keep unused SVG placeholders here. Future region folders should name the
real runtime resources they use in their own `region.json`.
