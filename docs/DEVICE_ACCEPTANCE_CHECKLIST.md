# You Rush 5.4 Device Acceptance

Tester: __________  Device/model: __________  Android: __________  Date: ______

Do not call a release accepted until every required line passes on a real phone.

## Install and migration

- [ ] Back up the previous working APK.
- [ ] With the last supported APK installed, create a score and unlock stage 2.
- [ ] Install 5.4 as an update without clearing app data.
- [ ] The app opens and the old unlock/best score appears.
- [ ] Close and reopen; migrated progress remains.

## Every stage

For all five stages:

- [ ] Start, move left/right, sprint, short jump, full jump, dash, stomp, fire.
- [ ] Runner shoes meet the snow line in idle, run, crouch, landing, and restart.
- [ ] No camera tilt occurs when changing direction.
- [ ] Every required jump is readable and reachable without a blind leap.
- [ ] Hazards do not spawn directly on the player or cover the landing.
- [ ] Key, two rescues, checkpoint, boss, and goal all function.
- [ ] The boss has a visible tell, damaging attack, and fair recovery window.
- [ ] Completing the stage records score and unlocks the next stage.
- [ ] MAP exits safely and does not corrupt progress.

## Notes and debug IDs

- [ ] NOTE uses a shallow top-right strip; the important scene stays visible.
- [ ] Typing, FIX FIRST, CANCEL, SAVE, and COPY work.
- [ ] MIC opens Android speech recognition and inserts recognized words.
- [ ] Canceling speech does not lose the run or leave it paused forever.
- [ ] Nearby IDs and position are included in a saved note.

## Device features and accessibility

- [ ] A chosen photo displays and remains after restart; reset removes it.
- [ ] Mute stops game cues; haptics toggle controls vibration.
- [ ] Large text enlarges menus and HUD without clipping critical information.
- [ ] Reduced motion removes camera smoothing.
- [ ] High contrast makes stage silhouettes clearer.
- [ ] Touch targets work with two fingers and do not overlap MAP/NOTE.
- [ ] Rotate/lock/reopen, background/resume, and incoming interruption recover.

## Long run and release gate

- [ ] Play for 20 minutes with no crash, runaway sound, stuck input, or severe lag.
- [ ] Force close and reopen; progress is intact.
- [ ] APK package/version/signature were checked and the file was virus-scanned.
- [ ] A second person can follow the owner handbook without verbal coaching.
- [ ] Record device-only results in `test-results/android-gameplay/` and do not
      turn an untested item into PASS.

Failed item, exact stage/item ID, and what happened:

____________________________________________________________________________
