# Visual Scale and Clearance Contract

Godot sprite scales are checked from each frame's visible alpha bounds rather
than the transparent atlas size. Version 5.2.1 establishes these approximate
on-screen ranges at the 1280-by-720 reference viewport:

| Subject | Visible width | Visible height | Collision body |
|---|---:|---:|---:|
| Runner | 75–118 px | 156–180 px | 44 × 96 px capsule |
| Salmon | 82–90 px | 35–49 px | 66 × 34 px |
| Wolf | 100–109 px | 56–61 px | 74 × 46 px |
| Brown bear | 162–173 px | 100–113 px | 92 × 68 px |
| Moose boss | 208–224 px | 183–193 px | 116 px diameter |
| Polar boss | 298–316 px | 181–183 px | 116 px diameter |

The runner, wolf, bear, moose, and polar-bear feet align to their collision
ground line. The hitboxes remain smaller than the painted silhouettes to allow
fair near misses. Moose is the tallest/longest wildlife silhouette; the polar
bear is approximately runner height but substantially wider; the brown bear is
clearly heavier than the wolf without obscuring its platform.

## Jump and route envelope

The first jump uses speed 750 and gravity 1450. Ignoring early release, its
calculated apex is about 194 px above takeoff and its same-height sprint range is
about 445 px. The single 88% air jump contributes another potential 150 px of
rise. Coyote time is 0.14 seconds and input buffering is 0.16 seconds.

Every authored stage must complete through the real controller/collision path
after a scale or hitbox change. A completion proves mechanical clearance, not
artistic quality. Pixel-level approval still requires a rendered desktop or
physical-device review because the server's dummy headless renderer cannot
produce viewport captures.
