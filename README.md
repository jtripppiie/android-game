# Moto Thrash: Retro Racing

**Original stadium motocross time-trial game for Android and the web, made in Godot 4.**

Moto Thrash is a compact, entirely original, retro-inspired arcade racer with smooth analog thumbstick movement. The artwork, course names, code, gameplay logic, and procedurally generated courses are original project content; no Nintendo or Excitebike assets, tracks, names, or music are used.

## Play

The on-screen pad and boost button remain visible during human play in both the browser and Android. They support simultaneous touches and sit below the top dashboard; keyboard controls also work.

- **Left virtual joystick:** up/down changes between four track lanes. Left brakes on the ground; right speeds up. While airborne, left/right leans the bike to stabilize your landing.
- **Flat-ground balance:** down-left lifts the front wheel; down-right lifts the rear wheel. The pad angle and distance from center control the tilt. The lane stays fixed while balancing. Ease toward down/center to recover; leaning past the balance marker builds wobble and eventually causes a fall. Ramp riding and airborne controls take priority.
- **Right BOOST button:** hold for turbo; release to cool your engine. READY pulses cyan, ACTIVE animates amber, and COOL shows recovery progress. The outer ring shows remaining heat capacity; during cooldown it fills toward reactivation. WAIT means countdown, pause, or crash recovery.
- Ride the banked jumps, avoid mud and builder barriers, cool the engine on arrow strips, and finish the time trial.
- 24 increasingly challenging courses across four cups with seeded layouts, local best times, unlock progression, and medals.
- Pause/resume. No login, ads, analytics, network requests, or purchases in the game.

**Desktop/testing:** WASD or arrow keys to steer, Space or Shift for boost, P/Esc for pause, M to toggle sound, Enter to start/advance, R to restart.

## Watch the computer

Choose **WATCH COMPUTER** on the main menu or press **C** to watch an AI rider race the selected course. It steers, boosts, avoids obstacles, and balances jumps using the normal race physics. Press **P/Esc** or click pause to pause/resume; use **TRACK SELECT** from the pause menu to exit. At the finish, choose **NEXT TRACK** to watch the next course or **RETRY** to replay. Computer runs do not save personal bests or unlock player courses. Choose **RACE NOW** or press **Enter** in the main menu to play yourself.

## Development status

The menu's **STUNT SHOW** button opens a separate bus-jump competition. Set 1–20 buses, a LOW/MID/HIGH ramp, and FAST/FASTER/MAX run-up speed, then choose BUILD + JUMP. The lane is locked for this event; left/right balances the bike in the air and boost adds speed. A wider stadium camera keeps long jumps visible. Clean landings earn 100 points per bus plus a distance bonus; failed attempts earn no points. Best score, most buses cleared, and setup preferences are saved locally, separately from campaign and custom-track records. The HIGH/MAX setup can clear all 20 buses; smaller setups are not guaranteed to clear long lines. EDIT STUNT returns to setup and RETRY repeats the same attempt. Validate with `tests/stunt_show_test.gd`.

Playable Godot source with original synthesized engine, wind, countdown, boost/cooling, jump, landing, crash and finish sounds. Physical-device QA, difficulty/playtesting, store art, accessibility review, and signed Android release testing remain before commercial launch. The intention is a **$0.99, one-time paid download**, not an in-app purchase.

## Run locally

1. Open this folder using **Godot 4.3+** (Godot 4.4/4.5/4.6 recommended).
2. Press **F6** on `scenes/game.tscn`, or **F5** for the project.
3. Android export requires Android SDK, Java SDK, and Godot's Android export templates; see [Android notes](docs/ANDROID.md).

For a local Web export, serve its output folder with `python3 tools/serve_web.py /path/to/web/export` and open `http://localhost:8060/`. This server supplies the cross-origin isolation headers required by the Godot browser runtime.

The arena uses original pixel sprites and bitmap lettering in a 480×270 logical layout rendered at the window resolution. Stadium spectators, varied tree borders, four marked lanes, and a top instrument panel follow a consistent arcade palette. Ramps are continuous height fields shared by rendering, ground contact, jumping, landing, AI and rider shadows. Courses contain wide shared jumps, split routes, whoops, mud and cooling/boost arrows. Terrain samples and rider textures are cached; the rider artwork is bundled in an original transparent PNG sheet, and no external fonts are required.

## Project layout

- `scripts/game.gd` — main game loop, retro drawing, controls, HUD, persistence.
- `scripts/race_rules.gd` — repeatable track generation, heat and speed logic.
- `scenes/game.tscn` — tiny scene entry point.
- `tests/race_rules_test.gd` — headless logic test; see command below.

```bash
godot --headless --path . --script res://tests/race_rules_test.gd
```

## Commercial note

Keep the game visually and mechanically distinct from Nintendo's Excitebike; don't use third-party sprites, audio, copied tracks or Nintendo branding. Check the final game's store name and potential trademarks before release. The payment price is configured in the store, not in Godot.

## Install on Android (testing)

The `Android test APK` GitHub Actions workflow creates a debug build on each push to the `dirt-rush` branch. Open the workflow run, download the `dirt-rush-android-debug-apk` artifact, extract the ZIP, and install the APK on an Android phone. This uses disposable debug signing, **not** a Play Store release signature.

## Build your own track

From the main menu tap **TRACK BUILDER**. Choose one of six local, saved course slots. Select a tool, then click/tap the track to place it; this is tap-to-place, not drag-and-drop. Move through ten areas with the arrows. The TOOLS 1/2 button switches between two pages: RAMP, HIGH, TABLE, BUMPS, MUD, BOOST, BLOCK, OIL; then JUMP, WATER, RIVER, CROCS, LAVA, CAR, BUS, ERASE. Tap a lane to place or replace an obstacle on the 40-pixel grid. Jump hazards automatically include an approach ramp 80 units before them, visible in the editor and test ride; erase the hazard to remove its automatic ramp. Keep room around jumps when designing a course. There are up to 200 placed objects per track. Press UNDO, press CLEAR twice to empty a track, TEST RIDE to play it, or CPU TEST to watch the computer try your layout. The best time for each custom track is saved on the device.

Keyboard controls in the builder: Left/Right changes area, Tab changes custom track slot, 1-8 selects a tool on the current page, 9 switches tool pages, Z undoes, Enter tests the course, C starts a computer test, and Escape returns to the main menu. No accounts, cloud storage, or borrowed game assets.

## Design references

The height-based lanes and ground/air/recovery state separation were informed by reading [matildeopbravo/excitebike](https://github.com/matildeopbravo/excitebike). [HashNuke/excite-bike](https://github.com/HashNuke/excite-bike) is an early track-layout prototype. [PostRockFTW/ExcitingBike](https://github.com/PostRockFTW/ExcitingBike) was useful for per-lane height tables and sprite state separation, and [Fytex/ExciteBike-LI1](https://github.com/Fytex/ExciteBike-LI1) for terrain-aligned and crash rendering. Their source and assets were inspected as references; this project's terrain, pixel sprites and bitmap alphabet are original implementations.

Additional checks: `godot --headless --path . --script res://tests/terrain_test.gd` and `godot --headless --path . --script res://tests/computer_mode_test.gd`.

The campaign increases course length, tightens recovery straights, introduces table tops and alternating routes, and raises rival pace across Rookie, Club, Pro and Legend cups. Medals and personal bests provide replay targets. Existing custom layouts are retained; expanded-course records use a fresh versioned table so old short-course times do not become unbeatable.

Validation also includes `tests/touch_controls_test.gd` for simultaneous steering/boost and `tests/content_audio_test.gd` for generated PCM, saved campaign progress, six custom slots and computer test rides.

The menu CREDITS button includes the running Godot engine license and its bundled third-party copyright/license notices, available offline. See [asset and IP review](docs/IP-REVIEW.md) for the review scope and release follow-ups.

The app title is **Moto Thrash: Retro Racing**, with **MOTO THRASH** on the title screen. Existing Android package identifiers, build artifact names and save filenames retain their legacy values for compatibility. Desktop saves from Cinderspoke (or the earlier Dirt Rush title) are imported when no Moto Thrash save exists.

Store short description: Pixel motocross. Race 24 tracks, master big jumps, and build your own courses.

Rider art: `assets/sprites/moto-rider-sheet-v1.png` is an AI-generated original eight-pose sheet, created with the built-in OpenAI image-generation tool. Its exact prompt is saved alongside it. The runtime extracts higher-resolution frames with softer filtering, applies rival paint colors, and selects riding, acceleration, takeoff, airborne and crash/recovery poses. It no longer builds rider artwork from geometric drawing commands. Validate with `tests/rider_sheet_test.gd`.

Trackside art: `assets/sprites/trackside-trees-v1.png` contains eight generated tree variants (oak, pine, palm, acacia, autumn maple, bare tree, cypress and olive). The renderer caches higher-resolution atlas frames, varies them by course theme, and uses separate upper/lower scrolling rows. The race crowd occupies a visible strip below the top dashboard. The generation prompt is saved next to the PNG.

Stadium fans: `assets/sprites/stadium-fans-v1.png` supplies eight original generated cheering characters: Sasquatch, two aliens, two cats, a dog, a mouse and a yeti. Two taller rows replace the tiny human crowd. Lineups vary by course and scrolling section, with staggered cheering and stable positions; the crowd texture cache is bounded. The exact generation prompt is saved alongside the sheet.

`assets/sprites/stadium-fans-v2.png` adds twelve more fans, for twenty total: a checkered-jacket tourist, three more aliens, marmot, meerkat, civil engineer, owl-masked spectator, mushroom dream monster, fox-masked spectator, ghost and astronaut. These original designs are mixed into the same randomized crowd.

`assets/sprites/stadium-people-v1.png` adds eight human spectators, bringing the mixed roster to 28. The two rows each have ten consistently spaced spectators per scrolling section. Seeded shuffled rosters use every design before cycling, reducing duplicate clusters without reshuffling people between animation frames.

Human fans use the original `stadium-people-v1.png` faces and clothing on a coarse pixel grid with nearest-neighbor enlargement. Four original code-drawn stick-figure fans bring the roster to 32. All fans receive the same darker, muted stadium palette; the earlier silhouette sheet remains as an unused art variant.

Visual rendering retains twice the previous bike/tree texture resolution and alpha edges, with linear texture filtering and canvas-item scaling. Older procedural shrub and straw-bale rows were removed. Tree chunks use course/row/chunk seeds for randomized spacing, species, heights and mirroring, stable while scrolling. Track surfaces use `assets/sprites/track-surfaces-v1.png` (original AI-generated oil, mud and boost sheet; exact prompt adjacent). Oil is available in the custom builder.

Jump hazards use `assets/sprites/jump-hazards-v1.png`, generated from its adjacent prompt. Starting at track 3, occasional water, river, crocodile-pool, lava, car and bus sections have takeoff ramps and one bypass lane. Collision covers the full hazard width for players and rivals. Updated campaign layouts use new `records_v3` and `medals_v3` entries, preserving previous records in the save file and retaining unlock progress. The first two unchanged tracks keep their existing records. Validate with `tests/jump_hazards_test.gd`; balance tricks have `tests/wheelie_test.gd`.
