# 5.2.2 Touch Controls Emergency Fix

Status: source verified; Android APK export pending.

This release replaces the faint fixed-coordinate overlay after device feedback
reported unreadable buttons, missing direction controls, and an unusable jump
button.

Controls now lay themselves out from the actual viewport with safe edge and
bottom margins. Left/right are 104-by-96 pixels, Jump is 116-by-106, and every
button uses an 88% opaque dark fill, a 3-pixel cyan border, white 19-pixel text,
and a bright high-contrast pressed state. The directional pair also receives a
shared backing panel so it reads as one control group.

Native multi-touch and synthesized mouse input are both supported. The built-in
`--touch-audit` startup test presses Right and Jump with separate touch indexes,
verifies both actions simultaneously, releases Jump without canceling Right,
then releases Right. The audit passed, followed by a complete 5/5 traversal run.

Physical-device acceptance is still required because Android screen cutouts,
navigation modes, and touch ergonomics cannot be reproduced by the server's
headless display.

The source passed structural validation, the real startup-path multi-touch
audit, and a 5/5 traversal run. Two unsandboxed export approvals timed out
before launch; the sandboxed fallback was blocked when Gradle attempted to open
its required local daemon socket. No 5.2.2 APK is claimed as accepted yet.
Version 5.2.1 remains the rollback package.
