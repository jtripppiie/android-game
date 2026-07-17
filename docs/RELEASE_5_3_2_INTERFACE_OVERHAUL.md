# 5.3.2 Interface and Review Overhaul

Status: source validated; Android compilation intentionally deferred.

This pass replaces the remaining monolithic run interface with dedicated,
testable systems. The goal is not merely to move controls around. It establishes
clear ownership for HUD layout, gameplay-message priority, review notes, text
size, objective visibility, and progress feedback.

## Legibility contract

The 1280 × 720 reference layout uses these minimum text sizes:

- HUD health: 20 px
- HUD score and objective: 18 px
- HUD state and route: 16 px
- HUD message toast: 18 px
- Pause button: 20 px
- Review panel title, target, and note input: 16 px
- Review tag and action buttons: 15 px
- Review quick-note buttons: 14 px
- Review status and recent-note line: 15 px
- Main-menu buttons: 26 px
- Stage-map buttons: 26 px

Large Text adds 3 px to HUD text and 2 px to review-panel text. Text is drawn
over high-opacity navy cards with light outlines. Important HUD text also uses
a dark font outline so snow, aurora, or mountain highlights cannot erase it.

## HUD ownership

`AlaskaGameHud` now owns the complete run interface. The world sends it a
snapshot of current state rather than manually formatting several unrelated
labels.

The fixed layout has three non-overlapping top regions:

1. Runner card, x=14–434
2. Objective card, x=446–1054
3. Pause button, x=1066–1266

The cards retain at least 12 px of horizontal separation. The lifecycle audit
asserts that their rectangles do not intersect.

The runner card shows:

- an ASCII-safe `HP 0/3` through `HP 3/3` value that cannot become a missing-glyph box;
- current score and saved best score;
- Aurora count;
- combo only when the multiplier chain is active;
- current animation state;
- current high, precision, or low route.

The objective card always shows all three completion requirements:

- key found or still required;
- rescues found out of two;
- boss alive or defeated.

The four-pixel progress rail spans the screen directly below the top cards. It
maps the runner’s world x-coordinate to the authored 5710 px expedition length.

## Gameplay-message queue

Gameplay events no longer write directly into one shared label. The old behavior
allowed a low-value `LAND` message to replace `KEY FOUND`, `HIT`, or a boss
instruction on the next frame.

Messages now have a priority and duration:

- priority 5: stage clear and boss defeat;
- priority 4: damage and route-loss warnings;
- priority 3: keys, rescues, and checkpoints;
- priority 2: boss instructions and armor/weak-state feedback;
- priority 1: movement, landing, rings, and ordinary combo feedback.

A higher-priority event immediately replaces a lower-priority toast and queues
the interrupted message briefly. Equal or lower priorities wait in a bounded
four-message queue. Duplicate active or queued messages are collapsed.

The message panel appears below the HUD instead of occupying the objective card.
Its fade retains a minimum readable opacity until the message expires.

## Review notebook

The notebook remains a compact top-right panel. At 480 × 292 it uses less than
16 percent of the reference screen and leaves the runner, route, and most nearby
objects visible.

New review features:

- active visual state for FEEL, JUMP, SPACE, ART, and BUG tags;
- one-tap starters for TOO HARD, TOO SMALL, BLOCKS VIEW, and WRONG ART;
- Enter-to-save from the note field;
- explicit empty-note guidance;
- FIX FIRST priority checkbox;
- save, copy, and undo-last actions;
- clear file-open and write-failure messages;
- current local note count;
- one-line preview of the latest saved note;
- 220-character limit to encourage actionable observations;
- scene context and nearest debug identifier appended to every note;
- existing pause state restored after closing.

New notes use `---` delimiters while remaining compatible with the earlier
plain-text log. Undo finds the last timestamped note header, removes only that
entry, and preserves all earlier notes.

## Automated evidence

The source pass adds or extends checks for:

- HUD class availability and world integration;
- minimum HUD and notebook font sizes;
- non-overlapping status, objective, and pause regions;
- priority interruption and message queue behavior;
- compact notebook dimensions;
- five review tags and four quick-note starters;
- note input length;
- clean respawn lifecycle;
- one active world and one runner after map transitions;
- circular D-pad, diagonal jump, and drift margin;
- real pause freeze and touch release;
- stage geometry;
- complete headless autoplay traversal.

The deterministic composition frames now reconstruct the redesigned HUD at its
exact reference coordinates so text density and overlap can be reviewed without
an Android screenshot.

## Files introduced or materially changed

- `scripts/game_hud.gd`
- `scripts/world.gd`
- `scripts/review_notebook.gd`
- `scripts/main.gd`
- `tools/build_composition_audit.py`
- `validate_project.py`
- `verification/current/stage-*.png`
- `verification/current/composition-review.mp4`

No APK is produced by this pass.
