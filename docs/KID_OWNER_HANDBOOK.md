# Your Game: You Rush

This game now belongs to you. You are allowed to play it, test it, make notes,
change it, make backups, and ask another developer for help. You do not need to
understand every file before you begin.

## The three rules that protect your game

1. Make a new Git branch before a big experiment.
2. Never share a signing key, password, private photo, or `local.properties`.
3. Keep the last APK that worked. If a new build is broken, reinstall the last
   working one and do not panic.

Git is the game's time machine. A **commit** is a named save point. A **branch**
is a safe copy of the timeline. An **APK** is the Android install file.

## Playing and reviewing

On the map, choose one of the five Alaska stages. A stage is complete only after
you find its key, rescue both people, defeat its boss, and reach the beacon.
Progress and scores save on the device.

Pickup jobs:

- Aurora badge: +12 base score, extends the combo, and increases `AURORA`.
- Red key: required to activate the final beacon.
- Red rescue radio: one rescue signal; collect both to finish.
- Aurora ring: increases `AURORA`, adds score, and gives a temporary lift/speed
  surge.
- Checkpoint: moves the recovery point after a fall.
- Supply block: releases extra Aurora score pickups.

The final beacon requires the key, both rescue signals, and the defeated boss.

Turn on `ACCESSIBILITY > REVIEW MODE · IDS + NOTES` when you want to review the
game. Normal play stays clean. During a review run, tap `IDS` to control labels
or `NOTE` to pause. The small notebook stays in the top-right so the play area
remains visible and shows the nearest item automatically. Choose `FEEL`, `JUMP`,
`SPACE`, `ART`, or `BUG`, type a short note (or tap `MIC` and speak), then save.
Use `FIX FIRST` only for a problem that blocks play. Every note includes the
date, player position, game state, and nearby item IDs. `COPY ALL` copies the
full notebook so it can be pasted into an email or issue.

Examples of useful notes:

- `MIDNIGHT-SUN-RUN-PF-4 is too far from PF-3`
- `SALMON-RUSH-AN-12 hits me before I can see it`
- `DARK-WINTER-BOSS-18 recovery is too short`

The letters include: `PF` platform/slope, `AN` animal, `WT` water, `IC`
breakable ice, `CP` checkpoint, `GO` finish, and `BOSS` boss. Press F10 with a
keyboard to hide/show IDs. Press N to open the notebook. These shortcuts only
work while Review Mode is enabled.

## Controls

### Circular movement pad

Put your left thumb anywhere on the round pad. Slide left or right to run. Slide
up to jump, or diagonally up and toward the trail to run and jump together. You
do not need to hit an individual arrow exactly, and you can drift a little
outside the circle without suddenly stopping. The gold dot shows what direction
the game is reading.

The right side has three clearly named actions: **JUMP** (large gold circle),
**SNOW** (blue circle), and **DASH** (purple button). You can use the dedicated
JUMP button when you want to keep horizontal movement steady with your left
thumb.

### Reading the run display

The top-left card shows health, score, Aurora pickups, combo, and your current
movement/route. The middle card always shows the three things needed to finish:
the key, two rescues, and the boss. A thin green line below the cards shows how
far you have traveled. Important messages appear briefly in their own small box
below the display, so they do not overwrite the objective list.

If **Large Text** is enabled from Accessibility, HUD and review-note text grows
without moving the three top sections into each other.

- Left/right: move
- Run: sprint
- Jump: jump; release early for a shorter jump
- Dash: quick burst
- Crouch in air: stomp
- Fire: throw a snowball
- Pause: stop the run; choose `EXIT TO MAP` separately
- Note: pause and write or speak a review note

Pause and Note freeze the runner, wildlife, bosses, hazards, and moving
platforms. Any held movement is cleared; touch the direction again after
resuming.

## Customizing safely

Choose `CUSTOMIZE RUNNER`, then choose a photo. The image stays on the device;
the game does not upload it. Use a photo you have permission to use. Never put a
private photo in Git. `RESET PHOTO` returns to the default runner.

Accessibility has audio mute, optional vibration, larger HUD text, reduced
camera motion, and a high-contrast background. These settings also save locally.

## Making a small change

The new game lives in `godot/`. Open that folder with Godot 4.7.1.

- Stage spacing and objects: `godot/scripts/world.gd`
- Running and jumping numbers: `godot/scripts/player.gd`
- Boss timing and patterns: `godot/scripts/trail_boss.gd`
- Menus and map: `godot/scripts/main.gd`
- Saved progress: `godot/scripts/game_session.gd`
- Review notebook: `godot/scripts/review_notebook.gd`

Change one idea at a time. Play the affected stage. Also test one stage before
and after it. Write down why the change is better, then commit it.

Useful Git commands (ask an adult/developer before pushing to a shared remote):

```bash
git switch -c my-change
git status
git add godot docs
git commit -m "Explain the change"
git push -u origin my-change
```

## Building an Android APK

Install Godot 4.7.1, its matching Android export templates, Android SDK 36, and
Java 17. Open `godot/project.godot`. In Godot choose **Project > Install Android
Build Template** if `godot/android/build` is absent, then reapply/copy the native
bridge files described in `docs/TECHNICAL_MAINTENANCE.md`. Choose **Project >
Export > Android Debug > Export Project**.

The expected package is `com.jtripppiie.mooserush`, version code 500 or higher.
Never lower the version code for a build meant to update an installed copy.

## Before giving a build to somebody

Follow every box in `docs/DEVICE_ACCEPTANCE_CHECKLIST.md`. A computer build is
not proof that touch, speech recognition, vibration, or photo selection works on
a particular phone. Test on at least one real Android phone.

## If something goes wrong

- Game will not open: reinstall the last working APK and save the broken APK.
- Progress looks wrong: do not clear app data; make a note and ask for help.
- Controls stick: lift all fingers, pause/map out, and restart the stage.
- Voice button fails: type the note; some devices have no speech recognizer.
- Build fails: copy the first actual `ERROR` plus 20 lines after it.
- Git looks scary: run `git status`; do not use `git reset --hard`.

The Java 4.2.2 build remains a rollback/reference build, but 5.0 is the primary
game. Old documents are history, not the current source of truth.
