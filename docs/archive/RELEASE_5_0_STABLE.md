# You Rush 5.0 Stable

Godot 4.7.1 is now the primary engine for all five Alaska stages. This release
replaces the side-by-side alpha package with the original app ID and version
code 500, and uses the same debug certificate as 4.2.2 so test installations can
update in place and migrate local progress.

Major changes: deterministic authored routes; corrected human/wildlife scale;
stage-specific palettes, hazards, animals and boss movement; key/two-rescue/
checkpoint/boss/beacon flow; scoring and unlock persistence; stable level-name
debug IDs; compact typed/voice notes; photo runner; audio/haptics; and large
text, reduced motion, high contrast, and mute options.

Automated release evidence: project validation passed; five stages completed
180-frame headless smoke runs without warnings; 99 Java rollback tests passed;
Android Gradle export passed; APK signature v2 verified; package/version/SDK and
native plugin manifest metadata inspected. APK SHA-256:
`a1f40845ae6382c6db3600999eaee6f32cf9c4521471989b9036900ae4a27b86`.

Real-device acceptance is still a human release gate. Use
`DEVICE_ACCEPTANCE_CHECKLIST.md`, especially update migration, multitouch,
speech provider, photo picker, haptics, lifecycle, and a 20-minute run.
