# You Rush 4.2.1 Stable Camera

Version 4.2.1 corrects a navigation-level presentation error discovered during
hands-on review of 4.2.0.

## Root cause

`drawWorld()` rotated the complete playfield by 1.2 degrees whenever left or
right was held. This was intentional “camera juice,” but it was inappropriate
for a precision runner: steering changed the visual horizon used to judge every
obstacle, platform, and jump arc.

## Corrected contract

- Horizontal input moves the player only. It never rotates the world camera.
- Impact shake remains event-driven and short; it is not coupled to held input.
- Dark Winter wind is visual only and no longer moves the player without input.
- Normal running keeps the player in the forward 58% lane, preserving reaction
  space; boss arenas allow 72% for combat positioning.
- Full-screen flashes cap at alpha 92 and final-blizzard tint caps at alpha 42.
- Routine landings no longer shake the camera or severely deform the runner.
- Collision sizes, input speed, encounter spacing, and jump physics are unchanged.
