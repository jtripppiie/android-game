# Computer Review Mode

Computer Review Mode runs the real Godot game on a desktop computer without
the phone touch overlay. It uses the same movement, collision, objectives,
bosses, scoring, saves, IDs, and notebook as Android play.

## Start it

Open `godot/project.godot` and run the project, then choose:

```text
COMPUTER REVIEW · IDS + NOTES
```

Or launch directly from a terminal:

```bash
godot --path godot -- --computer-review
```

Normal computer play is also available with:

```bash
godot --path godot
```

Use `--touch-preview` only when intentionally reviewing the phone control
layout on a computer.

## Keyboard controls

| Action | Key |
|---|---|
| Move | A/D or left/right arrows |
| Sprint | Shift |
| Jump / air jump | Space |
| Dash | E |
| Crouch / airborne stomp | S or down arrow |
| Snowball | F |
| Toggle Review Mode | F1 |
| Open compact notebook | N |
| Hide/show nearby IDs | F10 |
| Pause | Escape |

The small bottom toolbar never covers the center of play. Outside Review Mode
it shows the core keyboard controls. Inside Review Mode it shows the nearest
stable ID and exposes clickable `IDS`, `NOTE`, and `EXIT` actions.

## Identifier format

IDs use:

```text
S#-CATEGORY##
```

Examples are `S1-PF04`, `S3-AN02`, and `S5-BOSS01`. Only the four nearest
badges appear. The closest badge uses a bright green pill; platforms,
wildlife, objectives, water/ice, and bosses use distinct category colors.
World badges show only the compact ID. The desktop toolbar and notebook show
the full description.

## Taking a useful note

1. Stand near the item or landing that needs work.
2. Press `N`; gameplay pauses but the scene remains visible.
3. Choose `FEEL`, `JUMP`, `SPACE`, `ART`, or `BUG`.
4. Write what happened and what should change.
5. Mark `FIX FIRST` only for a blocker, then save.

The note automatically records the stage, runner position, score, combo,
objectives, nearest ID, and nearby IDs. `COPY` places the full local note log
on the computer clipboard.

Review Mode remains disabled during ordinary Android play unless it is enabled
under Accessibility. Computer Review Mode does not add a second game build or
an emulator APK.
