# Android Test Checklist

Use this after GitHub Actions produces the debug APK artifact.

Current packaged beta:

```text
versionCode: 351
versionName: 3.2.31-beta
build badge: ALASKA PASSPORT v3.51 BETA
```

## Install test

1. Download `you-rush-alaska-debug-apk` from the latest successful workflow run.
2. Extract the ZIP if GitHub downloaded it as a ZIP.
3. Install `you-rush-alaska-3.2.31-beta-351-debug.apk` on an Android phone.
4. Allow installation from the browser/files app if Android prompts you.
5. Open **You Rush**.
6. Confirm the debug/version badge is visible and matches the packaged beta.

## Smoke test

Confirm these screens work:

- Splash screen appears.
- Splash title words stack on narrow screens, spread apart on wide screens, and do not collide.
- Main menu appears after splash or tap.
- Main menu runner preview shows full standing legs on landscape screens, and
  the default head is smaller and cleaner than the old square fallback.
- Alaska Map opens.
- Locked map stages do not start early and show an unlock message.
- Alaska Map bottom route intel updates for the selected stage.
- Customize opens.
- Photo picker opens from Customize.
- A picked player photo appears in-game.
- Large modern phone photos do not crash import; unusable photos show a friendly rejection.
- The selected player photo restores after closing and reopening the app.
- Outfit color selection persists after closing and reopening the app.
- Body style selection cycles through photo/default, female runner, and male
  runner, then persists after closing and reopening the app.
- Back buttons return to menu.
- Play starts the selected stage.
- Runner appears readable in gameplay without the fallback/photo head feeling
  oversized against the body.
- A log followed by a wolf can be cleared with a timed jump plus double jump.
- Midnight Sun Run uses WOLF as the running hazard and a clearly labeled
  Midnight Sun boss with face/ray art, laser tells, and a short dark eclipse
  after the boss is defeated.
- Midnight Sun laser tells/attacks visibly charge from the sun's eyes, and the
  active beam starts on the eye instead of appearing behind the face art.
- Active Midnight Sun lasers draw as an eye-origin beam aimed toward the runner
  lane, not as a big red rectangle at runner height.
- Boss lasers match the laser-eyes HTML preview style: thin gradient beam,
  visible eye origin, eased sweep toward the runner lane, and no thick red bar.
- Boss laser tells also use the thin eye-origin preview beam, not a wide
  rectangular warning lane.
- Midnight Sun takes several snowball hits to defeat; it should not clear after
  only a few normal shots, and now has 14 boss health.
- Bear Country snowbank piles appear low at runner leg/feet level, not as tall
  mound walls.
- The on-screen controls include a separate SPRAY button, and bear spray works
  from that button instead of requiring hold-FIRE.
- The left on-screen control is a D-pad; sliding the thumb left/right moves the
  runner, and sliding high/low while tapping FIRE changes snowball aim,
  including diagonal throws.
- The D-pad up/down arrow buttons are the same visible size as left/right.
- Polar Bear Boss lasts long enough to reach later attack patterns, including
  eye-beam/laser pressure, instead of dying after a few hits.
- Moose Pass moose hazards and boss art read larger than before.
- Dark Winter iceberg obstacles sit lower, closer to runner height, instead of
  tall spike-wall height.
- Eagle Boss attacks do not show eye-beam/laser-style attacks.
- Left-side bear chases start threatening, then slow down and visibly fall
  farther behind in dust as the runner survives.
- Some wolves pounce upward near the runner and can threaten a mistimed jump.
- Computer Run starts from the main menu, auto-starts the ready screen, jumps
  and fires during play, and advances to the next stage after clears.
- Computer Run is invincible and dry: hits do not end the run, and clears do not
  persist rewards, unlocks, best scores, daily streaks, badges, or mission totals.
- Default red runner running frames do not show neighboring-frame artifacts,
  edge slivers, or flickering sprite seams.
- Default red runner frame 2 does not show a detached boot at the far-left edge.
- Daily Rush starts the rotating daily stage.
- Main menu shows Trail Passport badge progress.
- Turning DEBUG on shows numbered badges on active obstacles, wildlife, pickups, throws, boss attacks, and bosses.
- The DEBUG info panel is compact and does not block normal play.
- DEBUG wildlife and boss badges show sprite/frame detail such as `BEAR sheet f3 T`.
- DEBUG gate badges identify Salmon Rush logs as `LOG FIRE`, and throw badges show `SNOW` or `POWER`.
- DEBUG boss attack badges identify shootable projectiles as `ICE FIRE`.
- DEBUG numbers line up with collision feel: wildlife contact, boss attacks, pickups, snowballs, and logs should match what the numbered badges imply on screen.
- DEBUG mode shows translucent hitboxes for the player, gates, wildlife, pickups, throws, boss attacks, and the boss contact zone without hiding the action.
- DEBUG sprite labels distinguish `sheet`, `roar png`, and `drawn` render paths, with `T` marking trimmed sprite-sheet frames.
- DEBUG should make the new obstacle sprites and bear spray pickup identifiable enough to report by number.

## Offline visual preview test

Before installing a new APK, open `tools/index.html` and use the Offline Debug
Workbench to check:

- Default red runner feet are fully visible in every running frame.
- Mom/dad runner frames do not show stray adjacent-frame pixels.
- Eagle and polar bear sheets do not show edge fragments from neighboring frames.
- Runner and boss ground/contact lines match the intended lane.
- Log obstacles still read as logs in the gear/obstacle preview.

## Gameplay test

Test these controls:

- LEFT moves the character left.
- RIGHT moves the character right.
- JUMP bounces the character upward.
- A second JUMP works once while airborne.
- Jumping feels snappy and grounded, not floaty or odd.
- Releasing JUMP early produces a shorter hop.
- FIRE launches a snowball.
- Holding FIRE with SPRAY charges emits bear spray from the runner's forward hand.
- Bear spray shows an orange cone, consumes one charge, and does not replace snowballs.
- Snowballs can still destroy Salmon Rush river logs and shatter boss ice projectiles.
- PAUSE opens a frozen run overlay with Resume, Map, and Sprite options.
- Resume returns to the same run without stuck movement or forced jump/fire input.
- Normal screen tap also requests a jump.

## Survival system test

Confirm the beta loop behaves consistently:

- The run starts with three lives.
- Passing named obstacles advances the progress HUD and checkpoint.
- Salmon Rush asks the player to vault river logs, not fish racks.
- Salmon Rush river logs use the river-log sprite, look like logs, and can be destroyed with snowballs.
- Moose Pass antler barricades, Dark Winter icebergs, and Bear Country snowbanks use distinct obstacle sprites.
- Salmon Rush ready/rule text says FIRE can blast logs.
- Snowballs arc toward nearby river logs and hit them without requiring pixel-perfect aim.
- River logs show a small target mark and give clear blast feedback when destroyed.
- Active weather fronts do not appear during gameplay.
- HUD, map, ready screen, pause, missions, and results use stage-specific language such as CLEAR, VAULT, LEAP, and SURVIVE.
- The live objective uses short stage labels and stays readable on common phone widths.
- Tight obstacle clears can award a CLEAN VAULT popup without feeling mandatory.
- Trail Map pickups activate SCOUT and show upcoming danger markers.
- Boss RECOVER windows show a FIRE NOW reticle on the boss.
- Boss health text shows phase two, enrage, or weak-window status when active.
- Boss health text shows `FIRE PROJECTILES` when shootable projectiles are active.
- The final bear boss stands up before its eye-laser attack.
- Crashing with no shield spends one life.
- Respawn resumes safely when lives remain.
- Respawn grace shows a visible aura and prevents immediate repeat damage.
- Losing the last life opens game over.

## Scoring test

Confirm score changes:

- Clearing named stage obstacles increases score.
- Collecting stars increases score and the HUD star count.
- Dodging hazards gives small score bumps.
- Combo streaks build from successful play, unlock score multipliers, and reset on mistakes.
- Tight dodges can trigger a near-miss score popup without feeling unfair.
- Run missions appear before/during runs and report progress on result screens.
- Shield pickups appear, activate an obvious aura, and absorb one hit.
- Aurora Rush meter fills from skilled play and triggers a readable score-burst mode.
- Trail Tokens are awarded on game over or stage clear and persist after restarting the app.
- Daily Rush awards its bonus once per local day, shows the streak on result screens, and persists after restarting the app.
- Trail Passport badges unlock from skill/progression moments and add token rewards only once.
- Hitting a boss with snowballs increases score.
- Powered snowballs during aurora focus or weak windows visibly hit harder.
- Bosses summon wildlife after phase two and become faster when enraged.
- Snowballs can shatter boss ice projectiles for score, meter, and defensive breathing room.
- Eye beams originate from the boss eyes, sweep vertically toward the runner, and should show `BEAM DODGE` in DEBUG attack badges.
- Eye beams should appear as thin laser lines from the eye area, not a thick chest/mouth beam.
- The side-view bear should show one small visible eye glint, not two oversized red dots.
- Bear spray can stun close wildlife and interrupt a boss lunge when timed well.
- Boss phase-two and enrage timing still feel readable, with tells before attacks and short weak windows after recovery.
- Defeating a boss gives a larger stage-clear bonus with combo and star rewards.
- Expedition results can include blasted log credit and no longer mention weather fronts.
- Best score persists after restarting the app.
- XP and level persist after restarting the app.
- Cosmetic outfit unlocks spend Trail Tokens only and never change gameplay power.
- Trail Passport badge count and badge token rewards persist after restarting the app.
- Expedition Logs persist after restarting the app.

## Stage test

For each Alaska stage:

1. Start the stage from the Alaska Map.
2. Clear the required number of named obstacles.
3. Confirm the boss phase starts.
4. Hit the boss until its HP reaches zero.
5. Confirm the Stage Clear screen appears.
6. Confirm the next stage unlocks.

Stage feel targets:

- Stage 1 is easy and readable.
- Stage 2 gets the player to the boss quickly.
- Stage 3 asks for better timing.
- Stage 4 feels darker and harder, but fair.
- Stage 5 feels like the big Alaska challenge with bear, polar bear, and wolf variety.
- Brown bears and polar bears occasionally stand on hind legs and roar as a readable warning.

## Layout and readability test

Check small and large screens:

- Top HUD elements do not overlap.
- Route/boss progress, lives, combo multiplier, XP level, score, run score, and best score remain readable.
- Boss incoming and combo callout overlays appear without covering the player.
- The ready screen shows goal, boss, and bonus briefing chips.
- The ready screen names the current obstacle type instead of generic route props.
- Boss health bar never overlaps the top HUD.
- Boss tells show a clear readable action hint before the attack lands.
- Boss lunge movement feels like a wind-up, attack, and recovery instead of a random jump.
- Boss health includes an escape countdown during the boss fight.
- Game-over and stage-clear content fits on small phones.
- New best-score runs are called out on the result screen.
- Button labels and touch targets remain usable in portrait orientation.
- Customize shows locked outfit prices, unlocked outfits, and current token count.
- Generated background plates carry the mountains; old vector-style mountain layers should not appear.
- Winter/dark stages show denser snow-covered tree layers without jumpy resets.
- Trees should visually connect to the ground and not float above the lane.
- Polar bear and wolf sprites animate cleanly without magenta background artifacts.
- Eye-beam tells and active beams follow the same diagonal lane; touching empty
  space inside the beam's rectangular bounds does not cause damage.
- Polar Bear eye beams originate on the visible eye instead of floating above
  the sprite, and no fake parallel duplicate beam is drawn.
- Wildlife created by a boss summon moves into the arena and can be dodged or
  stunned while the boss remains active.
- Roaring bear sprites do not show edge artifacts from the original image border.
- Player runner sprite edges do not show leftover source-frame pixels.
- Eagle wing animation reads as deliberate flapping, not strobing.
- Salmon animation reads as swimming, not jittering.
- Stage obstacles have distinct identities: driftwood rails, river logs, antler barricades, icebergs, and snowbanks.
- Vector obstacle sprites have crisp transparent edges and no bitmap fringe/artifact halos.
- Moose/polar/default bosses should be grounded; eagle/salmon bosses may fly or swim but should have grounded shadows and constrained lanes.
- Bosses should visibly advance toward the player and retreat instead of parking at the far right.
- Eagle wings flap at a natural pace instead of strobing.
- Bear and polar bear movement stays planted instead of jumping vertically.
- Animal sprites do not have decorative halo/circle glows around them.
- Double-jumps have enough headroom after the ground/play area shift.

## Debug test

On the main menu:

1. Toggle DEBUG on.
2. Start a stage.
3. Confirm the overlay shows state, score, boss status, player position, shots, hazards, and recent events.
4. Toggle DEBUG off and confirm the overlay hides.

## Current known rough edges

- Boss attack patterns are functional but still need phone-feel tuning.
- Some encounter labels and boss behavior are still simple; runtime hazards now use generated raster/sprite-sheet art with frame trim guards.
- Photo placement is automatic; crop/position controls are not built yet.
- Map unlock logic exists, but the map still displays every Alaska stage for easy inspection.
- Difficulty is not balanced yet.
- Procedural SFX are improved, but final authored audio assets would still help.
- This checklist still requires a real phone pass before public submission.

## First tuning targets

- Route obstacle spacing
- Gravity and jump height
- D-pad button size
- Boss HP
- Hazard spawn rate
- Bear Country difficulty
- Stage-specific reward frequency
- Summary screen vertical spacing
