# Sprite Sheet Asset Pipeline

This document captures the repeatable process for creating and validating raster
sprite assets used by the Android game.

## Goal

Move the game away from placeholder vectors and hand-drawn Canvas animal shapes
into real raster game art:

- Multi-frame animal sprite sheets
- Multi-frame runner sprite sheets
- Full-screen background plates
- Transparent foreground tree sprites
- Documented prompts and validation steps so the process can be repeated

## Current Package

```text
versionCode: 331
versionName: 3.2.11-beta
badge: ALASKA PASSPORT v3.31 BETA
```

## Generated Assets

All project-bound assets live in:

```text
app/src/main/res/drawable-nodpi/
```

Sprite sheets:

```text
sheet_moose_walk.png
sheet_bear_walk.png
sheet_polar_bear_walk.png
sheet_wolf_run.png
sheet_salmon_swim.png
sheet_eagle_fly.png
sheet_player_run_headless.png
sheet_mom_run.png
sheet_dad_run.png
sprite_bear_roar.png
sprite_polar_bear_roar.png
```

Environment art:

```text
background_midnight_sun_art.png
background_dark_winter_art.png
sprite_tree_summer.png
sprite_tree_winter.png
```

## Sheet Format

Animal sheets:

```text
frames: 6
layout: one horizontal row
source size: 2172 x 724
frame width: 362 px
format: PNG RGBA
background: transparent after chroma-key conversion
```

Player/family sheets:

```text
frames: 6
layout: one horizontal row
format: PNG RGBA
background: transparent after chroma-key conversion
```

The generated character sheet sizes vary slightly:

```text
sheet_player_run_headless.png  1972 x 798
sheet_mom_run.png              1983 x 793
sheet_dad_run.png              2027 x 776
```

Animal sheets use fixed 362 px atlas cells. Runtime rendering applies guarded
trim rectangles and disables bitmap filtering for atlas draws so adjacent frame
pixels do not bleed across the source rectangle.

Family runner sheets can have odd total widths, so the renderer uses rounded
proportional frame boundaries instead of integer-dividing the sheet width. This
keeps late frames from drifting left and clipping hands, boots, or faces.

The sprite sheet audit tool shows raw frame boxes plus the Android runtime-safe
crop in green. If art crosses a yellow raw frame boundary but sits outside the
green crop, it should not render in-game. The best source asset still has clean
transparent gutters, because runtime trims are a guardrail rather than a
replacement for clean source art.

Animal frame width:

```java
frameWidth = sheet.getWidth() / frameCount
```

Family runner frame bounds:

```java
left = round(sheetWidth * frameIndex / frameCount)
right = round(sheetWidth * (frameIndex + 1) / frameCount)
```

## Prompts

These prompts were used with the built-in image generation tool. Each
transparent asset was generated on a flat `#ff00ff` chroma-key background.

### Moose Walk Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame moose walking sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same full-body bull moose in every frame, side profile facing left, long body, tall legs, dark shoulder hump, long muzzle, beard, broad palmate antlers. Six sequential walking frames: contact, down, passing, up, opposite contact, opposite passing.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and baseline, full body visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, no cast shadow.
Color palette: dark brown body, cream/tan antlers, black hooves, no magenta in the moose.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Bear Walk Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame bear walking sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same full-body Alaskan brown bear in every frame, side profile facing left, heavy shoulder hump, rounded ears, long snout, thick paws with visible claws. Six sequential walking frames: contact, down, passing, up, opposite contact, opposite passing.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and baseline, full body visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, no cast shadow.
Color palette: dark brown and warm brown fur, black claws and nose, no magenta in the bear.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Polar Bear Walk Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame polar bear walking sprite sheet for a winter mode enemy in an Alaska mobile runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same full-body polar bear in every frame, side profile facing left, visually distinct from a brown bear, powerful shoulders, long white body, black nose, small rounded ears, heavy paws with dark claws, subtle blue-gray winter shading. Six sequential walking frames: contact, down, passing, up, opposite contact, opposite passing.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet, matching the existing moose/bear/salmon/eagle runner-game world.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and baseline, full body visible in every frame, generous padding, no cropping.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation.
```

### Wolf Run Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame gray wolf running/walking sprite sheet for a winter mode enemy in an Alaska mobile runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same full-body gray wolf in every frame, side profile facing left, lean athletic body, pointed ears, long muzzle, bushy tail, visible paws, alert winter predator silhouette. Six sequential locomotion frames: contact, down, passing, up, opposite contact, opposite passing.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet, matching the existing Alaska runner-game world.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and baseline, full body visible in every frame, generous padding, no cropping.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation.
```

### Bear Roar Sprites

```text
Create a polished single-frame brown bear standing upright on its hind legs and roaring for an Alaska winter runner game. Use a perfectly flat solid #ff00ff chroma-key background only. The bear should be full-body, side profile facing left, mouth open roaring, raised forepaws, heavy shoulder hump, rounded ears, long snout, thick paws with visible claws.
```

```text
Create a polished single-frame polar bear standing upright on its hind legs and roaring for an Alaska winter runner game. Use a perfectly flat solid #ff00ff chroma-key background only. The bear should be full-body, side profile facing left, mouth open roaring, raised forepaws, powerful shoulders, long white body, small rounded ears, black nose, heavy paws with dark claws.
```

### Salmon Swim Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame salmon swimming sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same full-body salmon fish in every frame, side profile facing left, clear fins, tail, eye, streamlined body. Six sequential swimming frames showing body/tail wave: neutral, tail up, body curve up, neutral, tail down, body curve down.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and centerline, full fish visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, no cast shadow.
Color palette: silver blue body, salmon red accents, darker back, no magenta in the fish.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Eagle Fly Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame bald eagle flying sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same bald eagle in every frame, side view facing left, white head and tail, yellow beak and talons, dark brown wings. Six sequential wing-flap frames: wings high, wings mid-down, wings low, wings mid-up, wings high again, gliding extension.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and body center, full eagle visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, no cast shadow.
Color palette: dark brown feathers, white head/tail, yellow beak/talons, no magenta in the eagle.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Headless Player Run Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame headless runner body sprite sheet for an Alaska runner game, designed so the app can place an uploaded face photo as the head.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same athletic runner body in every frame, no head and no face, wearing red Alaska winter parka, dark snow pants, tan gloves, black boots, scarf/hood collar. Six sequential running frames facing right: contact, recoil, passing, high point, landing, push-off.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and foot baseline, full body from neck/collar to boots visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, no cast shadow.
Color palette: red jacket, dark pants, tan gloves, black boots, cream trim, no magenta in body.
Constraints: absolutely no head, no face, no hair. No text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Mom Run Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame mom runner character sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same adult woman runner in every frame, side view facing right, friendly heroic look, teal Alaska winter jacket, navy snow pants, cream trim, gloves, boots. Six sequential running frames: contact, recoil, passing, high point, landing, push-off.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and foot baseline, full body visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, upbeat adventure mood, no cast shadow.
Color palette: teal jacket, navy pants, cream trim, black boots, warm skin tones, no magenta in character.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Dad Run Sheet

```text
Use case: stylized-concept
Asset type: mobile 2D runner game sprite sheet
Primary request: Create a polished 6-frame dad runner character sprite sheet for an Alaska runner game.
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background only, including all gutters between frames.
Subject: same adult man runner in every frame, side view facing right, friendly heroic look, orange Alaska winter jacket, charcoal snow pants, cream trim, gloves, boots. Six sequential running frames: contact, recoil, passing, high point, landing, push-off.
Style/medium: high-quality 2D game art, hand-painted cartoon realism, crisp silhouette, professional mobile game sprite sheet.
Composition/framing: one horizontal row of exactly 6 evenly sized frames, consistent scale and foot baseline, full body visible in every frame, generous padding, no cropping.
Lighting/mood: soft game-art lighting, upbeat adventure mood, no cast shadow.
Color palette: orange jacket, charcoal pants, cream trim, black boots, warm skin tones, no magenta in character.
Constraints: no text, no labels, no frame numbers, no watermark, no border, no visible grid lines. Background must be one uniform #ff00ff with no gradients, no shadows, no floor, no texture, no lighting variation. Keep crisp edges for chroma-key removal.
```

### Background Plates

Two full-screen background plates were generated:

```text
background_midnight_sun_art.png
background_dark_winter_art.png
```

The prompts requested wide 16:9 Alaska side-scroller backdrops with distant
mountains, clear lower-third gameplay space, no text, no UI, and no characters.

### Tree Sprites

Two tree cutouts were generated on the same `#ff00ff` key:

```text
sprite_tree_summer.png
sprite_tree_winter.png
```

The prompts requested single full spruce trees, generous padding, no text, no
watermark, no frame, and clean edges for key removal.

## Conversion

The built-in image tool saved source images under:

```text
/home/jt/.codex/generated_images/019f2e9f-4ed6-7862-919a-43818b1a888f/
```

Project-bound copies were converted into transparent PNGs using a temporary
local Java utility created in `tmp/` during the asset pass.

Reason:

- The bundled Python chroma-key helper required Pillow.
- This repo environment did not have Pillow available.
- The JDK was available, so a local Java converter was used instead.

The converter:

- Reads source PNG with `ImageIO`
- Detects magenta-family background pixels
- Sets those pixels to alpha `0`
- Leaves non-magenta subject pixels opaque
- Writes an RGBA PNG

The scratch converter files were cleaned up after conversion. The local `tmp/`
workspace is intentionally ignored through `.gitignore`:

```text
tmp/
```

For future production work, either:

- Recreate the simple Java converter in a scratch directory, or
- Install Pillow and use the bundled helper at:

```text
/home/jt/.codex/skills/.system/imagegen/scripts/remove_chroma_key.py
```

## Validation

Validation used a temporary local Java helper created in `tmp/` during the asset
pass.

Checks:

- PNG is RGBA
- Corners have alpha `0`
- There are many transparent pixels
- There are no visible magenta pixels with alpha greater than `8`

Example output:

```text
sheet_moose_walk.png
  size=2172x724 transparent=1332363 partial=1442 opaque=238723 magentaVisible=0 cornerAlpha=0,0,0,0
```

## Runtime Wiring

### GameAssets

`GameAssets` now loads:

- Real background plates
- Transparent summer/winter tree sprites
- Moose walk sheet
- Bear walk sheet
- Salmon swim sheet
- Eagle fly sheet

### MooseRushView

`MooseRushView` now uses animated sprite sheets for:

- Moose hazard
- Bear hazard
- Polar bear hazard and boss
- Wolf winter hazard
- Salmon hazard
- Eagle hazard
- Salmon Boss
- Moose Boss
- Eagle Boss
- Polar Bear Boss
- Brown bear / polar bear roar warning poses

Motion behavior:

- Moose and bear use walking frames.
- Polar bear uses walking frames and a dedicated upright roar sprite in winter mode.
- Wolf uses faster locomotion frames as a lower-profile winter threat.
- Salmon uses swimming frames plus a small sine-wave wiggle and rotation.
- Eagle uses flapping frames plus a small bob and rotation.
- Trees use transparent raster sprites in multiple stable parallax bands.

### SpriteRenderer

`SpriteRenderer` now uses:

```text
sheet_player_run_headless.png
```

The uploaded/default head is still drawn separately on top of the headless
runner body.

`1.8.4-alpha` fixes runtime runner readability:

- The game uses a separate runner animation clock instead of the faster world
  sprite clock.
- Runtime drawing trims each headless runner sheet frame to its non-transparent
  body bounds before scaling.
- The body renders from a larger visible target height so the runner no longer
  appears tiny next to the uploaded/default head.

`1.8.5-alpha` removes the old SVG placeholder files from the Alaska region
asset folder. `region.json` now records runtime drawable/sprite-sheet resource
names instead of placeholder filenames.

`1.8.6-alpha` adds runtime source-rect guard pixels for wildlife and player
sprite sheets so adjacent atlas frames do not bleed into each other during
animation. It also removes the vector-style triangle mountain overlay from the
runtime scene and uses denser generated tree-sprite layers, including extra
snow-covered tree bands in winter/dark stages. The same pass adds polar bear,
wolf, and roaring bear assets for winter-mode variety.

`1.8.7-alpha` keeps the same generated sheets but tightens runtime sampling:
wildlife sheets use wider atlas-edge guards, the player body uses an extra
source inset, and flying/walking animation cadence is driven by animal age
instead of the faster world scroll clock. This is the reference behavior for
future sprite-sheet imports.

## Not Yet Wired

These sheets are imported but not yet selectable in gameplay:

```text
sheet_mom_run.png
sheet_dad_run.png
```

They are ready for a character-selection pass.

## Future Upgrade

Recommended next steps:

- Add character selection for Player / Mom / Dad.
- Split the sprite-sheet renderer into a reusable class.
- Use a true native-transparent generation path if available.
- Generate damage, jump, and idle sheets for each character.
- Add a small preview harness that cycles every sheet before packaging.
