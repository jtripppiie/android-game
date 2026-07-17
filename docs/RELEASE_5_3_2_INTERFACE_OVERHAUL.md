# 5.3.2 Interface and Review Overhaul

Status: compiled and package-inspected; physical-device acceptance pending.

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

Bosses remain dormant until the runner enters the arena approach. They no
longer cycle attacks, sounds, haptics, and `BOSS WINDUP` messages from the first
frame while they are still thousands of pixels off-screen. The old world-space
stage title at y=92 was also removed because camera movement carried it into the
fixed HUD. Stage instructions now use the bounded message system.

Audio and vibration now follow event profiles instead of firing for every text
message. Damage and route loss use the strongest warning, objectives use a
clear reward cue, boss tells are audible without vibration, and ordinary LAND,
AIR JUMP, DASH, ring, and combo updates stay silent. Normal traversal no longer
produces a constant buzz-and-beep loop.

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

## Touch ownership

Every finger now keeps the control role it acquired at touch-down. A JUMP thumb
that drifts across DASH or the D-pad can release JUMP when it leaves the
forgiving margin, but it cannot silently become a different action. Likewise,
only the first active D-pad finger controls movement; a second finger landing on
the pad cannot cancel or reverse the movement thumb.

The D-pad retains its 24 px outside drift margin. JUMP, SNOW, DASH, NOTE, and IDS
use a 14 px drift margin around their original targets. Drag events for unknown
touch IDs are ignored instead of manufacturing a new control press.

Gameplay-message countdowns also stop while the tree is paused. A checkpoint,
damage warning, or objective message cannot disappear while the player reads
the pause panel or writes a field note.

Review Mode’s NOTE and IDS controls now begin at y=92 below the 82 px HUD. Their
rectangles are audited against both the objective card and pause button; the
review controls can no longer sit invisibly on top of either HUD target.

## Debug overlay density

Review Mode now displays at most five nearby IDs. The closest item remains green
and all badges use 16 px text with a five-pixel dark outline. Long platform and
slope badges follow the nearest point on the surface, keeping the identifier in
view instead of anchoring it at a distant left origin.

Stage-map and accessibility controls now use explicit bound callbacks instead
of loop-captured anonymous functions. The system audit presses the second map
button and verifies stage index 1 launches, then emits the Mute toggle and
verifies only that whitelisted property changes before restoring it.

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
- fixed touch ownership, cross-control drag rejection, and one D-pad owner;
- real pause freeze and touch release;
- paused HUD-message timing;
- five-item debug-overlay cap, 16 px badge text, and surface-following IDs;
- dormant boss state before the arena approach;
- stage geometry;
- complete headless autoplay traversal.

The deterministic composition frames now reconstruct the redesigned HUD at its
exact reference coordinates so text density and overlap can be reviewed without
an Android screenshot. `verification/current/review-mode-overlay.png` separately
reconstructs the optional NOTE/IDS controls and bounded identifier treatment so
normal-play evidence remains uncluttered.

## Files introduced or materially changed

- `scripts/game_hud.gd`
- `scripts/world.gd`
- `scripts/review_notebook.gd`
- `scripts/main.gd`
- `tools/build_composition_audit.py`
- `validate_project.py`
- `verification/current/stage-*.png`
- `verification/current/composition-review.mp4`

The final combined source was exported afterward as
`app/build/outputs/apk/debug/you-rush-alaska-5.3.2-debug.apk`.

Build evidence:

- size: 94,980,899 bytes;
- SHA-256: `59930a6c69dd27ac536a561b61f22b16e511e0bcd0c46b09aff18b61b92728fa`;
- package: `com.jtripppiie.mooserush`;
- versionCode: 532;
- versionName: 5.3.2;
- minimum SDK: 24;
- target/compile SDK: 36;
- APK Signature Scheme v2: verified;
- 16KB ZIP alignment: verified;
- archive integrity: verified.
