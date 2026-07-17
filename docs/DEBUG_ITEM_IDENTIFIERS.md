# Debug Item Identifiers

Turn on `ACCESSIBILITY > REVIEW MODE · IDS + NOTES`. Normal play now has no
identifier clutter and no review buttons. Review Mode persists after restarting
the game and places compact labels beside gameplay objects. The format is:

```text
LEVEL-NAME-CATEGORY-NUMBER
```

Examples:

```text
MIDNIGHT-SUN-RUN-PF-4
SALMON-RUSH-WT-9
MOOSE-PASS-AN-18
DARK-WINTER-BOSS-21
BEAR-COUNTRY-PU-12
```

Categories: `PF` platform/slope, `AN` animal, `WT` water, `IC` breakable ice,
`PD` launch pad, `BL` supply block, `RG` ring, `PU` pickup, `CP` checkpoint,
`GO` finish beacon, and `BOSS` boss. Each category has its own short sequence,
so the first animal is `AN-1` even if platforms were authored before it.
Numbers are stable for that authored run. Only items near the player are
labeled, and the nearest item is highlighted in green for readable screenshots.
Long platforms are ranked by the nearest point on their surface rather than
their distant left edge.

In Review Mode, tap `IDS` to hide/show labels and `NOTE` to pause into the small
top-right notebook. The notebook names the nearest item automatically. Tap
`FEEL`, `JUMP`, `SPACE`, `ART`, or `BUG`, type a short note (or use `MIC`), and
mark `FIX FIRST` only for a blocker. Saving attaches stage, position, score,
combo, objectives, and nearby IDs. `COPY ALL` copies the local log. Press N with
a keyboard to open notes and F10 to hide/show labels.

Useful report:

```text
Dark Winter: DARK-WINTER-PF-7 hides the landing behind DARK-WINTER-AN-11.
I was sprinting and used one full jump, no air jump.
```
