# Debug Item Identifiers

Turn on `ACCESSIBILITY > REVIEW MODE · IDS + NOTES`. Normal play now has no
identifier clutter and no review buttons. Review Mode persists after restarting
the game and places compact labels beside gameplay objects. The format is:

```text
S#-CATEGORY##
```

Examples:

```text
S1-PF04
S2-WT03
S3-AN02
S4-BOSS01
S5-PU07
```

`S1` through `S5` identify the stage. Categories: `PF` platform/slope, `AN`
animal, `WT` water, `IC` breakable ice,
`PD` launch pad, `BL` supply block, `RG` ring, `PU` pickup, `CP` checkpoint,
`GO` finish beacon, and `BOSS` boss. Each category has its own short sequence.
Numbers are stable for that authored run. At most the four nearest items are
labeled, and the nearest item is highlighted in green for readable screenshots.
Every badge uses 16 px text with a dark outline. Long platform and slope badges
follow the nearest visible point on their surface instead of remaining at a
possibly off-screen left edge.

In Review Mode, tap `IDS` to hide/show labels and `NOTE` to pause into the small
top-right notebook. The notebook names the nearest item automatically. Tap
`FEEL`, `JUMP`, `SPACE`, `ART`, or `BUG`, type a short note (or use `MIC`), and
mark `FIX FIRST` only for a blocker. Saving attaches stage, position, score,
combo, objectives, and nearby IDs. `COPY` copies the local log and `UNDO`
removes only the last saved note. Press N with a keyboard to open notes and F10
to hide/show labels.

Useful report:

```text
Dark Winter: S4-PF07 hides the landing behind S4-AN02.
I was sprinting and used one full jump, no air jump.
```
