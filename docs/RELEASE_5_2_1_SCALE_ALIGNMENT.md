# 5.2.1 Scale and Alignment Correction

This patch follows a measured alpha-bound and collision audit rather than relying
only on automated completion. It corrects the runner artwork being drawn about
35 pixels below its collision ground line, increases the runner's body capsule
from 76 to 96 pixels, enlarges and grounds brown bears, enlarges the polar boss
to runner-scale height, and aligns boss artwork without moving proven attack
collision geometry.

An attempted 132-pixel runner capsule was explicitly rejected after all five
stage audits failed; it produced repeated damage and blocked Moose Pass. The
accepted 96-pixel capsule completed all five stages with the real movement,
jump, objective, boss, and finish logic.

The project also accepts `--visual-audit=STAGE`, which captures periodic viewport
frames when run with a real graphics display. The dummy server renderer cannot
read viewport textures, so physical-device or rendered-desktop inspection
remains the honest visual acceptance gate.
