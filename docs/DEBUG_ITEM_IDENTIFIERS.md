# Debug Item Identifiers

Debug builds place a compact stable label beside gameplay objects. The current
format is:

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

Categories: `PF` platform, `AN` animal, `WT` water, `PD` launch pad, `BL`
supply block, `RG` ring, `PU` pickup, and `BOSS` boss. Numbers are stable for
that authored run and intentionally short enough for screenshots and spoken
notes.

Tap `NOTE` to pause into the shallow top-right notebook. Type or use `MIC`, mark
`FIX FIRST` only for a blocker, then save. The note automatically includes stage,
position, score, combo, objectives, and nearby IDs. `COPY` copies the local log.
Press N with a keyboard to open notes and F10 to hide/show labels.

Useful report:

```text
Dark Winter: DARK-WINTER-PF-7 hides the landing behind DARK-WINTER-AN-11.
I was sprinting and used one full jump, no air jump.
```
