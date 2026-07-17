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
| Salmon boss | about 356 px | about 147 px | 286 × 92 px |
| Moose boss | 208–224 px | 183–193 px | 176 × 126 px |
| Eagle boss | about 179 px | about 200 px | 148 × 112 px |
| Polar boss | 298–316 px | 181–183 px | 244 × 124 px |

The runner, wolf, bear, moose, and polar-bear feet align to their collision
ground line. The hitboxes remain smaller than the painted silhouettes to allow
fair near misses. Moose is the tallest/longest wildlife silhouette; the polar
bear is approximately runner height but substantially wider; the brown bear is
clearly heavier than the wolf without obscuring its platform.

## Jump and route envelope

The first jump uses speed 900 and gravity 1550. Ignoring early release, its
calculated apex is about 261 px above takeoff and its same-height sprint range
is about 627 px. The single air jump uses 88% of the original jump speed.
Coyote time is 0.18 seconds and input buffering is 0.20 seconds. The geometry
audit enforces a maximum 190-pixel main-route gap and 190-pixel upward step;
the current measured maxima are both 180 pixels.

Every authored stage must complete through the real controller/collision path
after a scale or hitbox change. A completion proves mechanical clearance, not
artistic quality. Pixel-level approval still requires a rendered desktop or
physical-device review because the server's dummy headless renderer cannot
produce viewport captures.
