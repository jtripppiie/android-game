# 5.3 Controls, Movement, and Menu Overhaul

Status: compiled and inspected; physical-device acceptance pending.

Version 5.3 responds to physical-device feedback that the controls were
unreadable/unusable, the directional pad appeared absent, jumping felt
pathetic, running felt poor, and menus rendered too small.

The mobile layout now uses actual viewport dimensions and safe margins.
The follow-up 5.3.1 source turns the left controls into a four-way D-pad and
uses a 150-by-150 circular Jump control. Snow and Dash have distinct cyan and
purple treatments rather than sharing one generic button style. Native
multi-touch and synthesized mouse events share the same action map.

Movement changes:

- Walk speed: 260 → 330
- Sprint speed: 430 → 540
- Ground acceleration: 1800 → 3200
- Air acceleration: 1050 → 1350
- Jump impulse: 750 → 900
- Coyote time: 0.14 → 0.18 seconds
- Input buffer: 0.16 → 0.20 seconds
- Dash speed: 720 → 780

The first-jump calculated apex increases from about 194 to 261 pixels. The 90%
air jump can contribute roughly another 235 pixels. Early button release still
cuts the jump short, preserving control rather than forcing maximum height.

Main, map, customization, and accessibility panels now occupy most of the
1280-by-720 reference viewport. Main rows are 72 pixels tall, body text is at
least 22–24 pixels, and titles range from 40 to 48 pixels.

The startup-path multi-touch audit passed simultaneous movement and jumping,
and all five stages completed with the new movement constants.

Phone direction controls automatically engage sprint; a separate RUN hold is no
longer required. This removes an awkward three-finger requirement for running
and jumping at the same time. Keyboard players retain separate walk/sprint
control.

Android inspection: package `com.jtripppiie.mooserush`, version code 530,
version name 5.3.0, minimum SDK 24, target SDK 36, ARM64, and APK signature
scheme v2 verified. SHA-256:

```text
53b36d3ef69a3faa625fa0282d3dbb81b7f49640e948affe1000f3849ff91941
```
