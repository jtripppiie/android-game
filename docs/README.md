# You Rush Documentation

This folder contains the active project documentation. Historical iteration
notes are kept in `archive/` so the main docs stay useful for development,
testing, and release preparation.

## Active Docs

- [3.19 aerial ring routes](RELEASE_3_19_BETA_AERIAL_RING_ROUTES.md):
  launch-directed ring arcs, trick chains, lift preservation, and speed surges.
- [Wildlife scale contract](WILDLIFE_SCALE_CONTRACT.md): explicit visual and
  collision dimensions for moose, bears, wolves, eagles, and salmon.
- [3.18 secrets and chains release](RELEASE_3_18_BETA_SECRETS_AND_CHAINS.md):
  escalating stomp chains, hidden supply blocks, reward arcs, and gear caches.
- [3.17 arcade joy release](RELEASE_3_17_BETA_ARCADE_JOY.md): wildlife
  stomps, rebound chains, launch pads, aerial routes, and Godot parity.
- [3.16 reactive river release](RELEASE_3_16_BETA_REACTIVE_RIVER.md):
  reversible freezing, bridge destruction, reopened currents, and refreezing.
- [3.15 system integrity release](RELEASE_3_15_BETA_SYSTEM_INTEGRITY.md):
  platform, laser, encounter ownership, Godot integration, and validation fixes.
- [3.14 visual gameplay release](RELEASE_3_14_BETA_VISUAL_GAMEPLAY_PASS.md):
  current artwork integration, implementation map, verification, and device QA.
- [Release notes](RELEASE_NOTES.md): current and previous beta highlights.
- [Generated assets](GENERATED_ASSETS.md): raster-art prompt summaries and
  transparency pipeline provenance.
- [Versioning](VERSIONING.md): Android version fields, build badge rules, and
  release checklist.
- [Android test checklist](ANDROID_TEST_CHECKLIST.md): phone QA checklist for
  each debug APK.
- [Gameplay revamp contract](GAMEPLAY_REVAMP.md): encounter-beat pacing,
  high-risk FLOW behavior, and rules for future gameplay systems.
- [Godot overhaul vertical slice](GODOT_OVERHAUL.md): next-generation movement,
  handcrafted-level architecture, validation, and migration boundaries.
- [Offline debug previews](OFFLINE_DEBUG_PREVIEWS.md): local HTML preview
  workflow for sprite crops, ground/contact lines, gameplay composition, and
  obstacle readability without reinstalling the app.
- [Privacy notes](PRIVACY.md): current local-first privacy behavior and launch
  policy notes.
- [Store listing draft](STORE_LISTING_DRAFT.md): draft store copy and screenshot
  plan.
- [Branching and releases](BRANCHING_AND_RELEASES.md): branch/version workflow
  and APK artifact rules.
- [Sprite sheet asset pipeline](SPRITE_SHEET_ASSET_PIPELINE.md): repeatable art
  asset and sprite-sheet guidance.
- [App duplication guide](APP_DUPLICATION_GUIDE.md): high-level guide for
  cloning the app structure.
- [Region replication playbook](REGION_REPLICATION_PLAYBOOK.md): detailed
  process for adapting the game to another region.

## Historical Notes

Older release notes, milestone notes, and rapid QA passes live in
[archive](archive/). They are useful for context, but they are not the primary
source of truth for the current beta.

## Current Source Of Truth

- Version metadata: `app/build.gradle`
- Current build summary: root `README.md`
- Current implementation release: `docs/RELEASE_3_19_BETA_AERIAL_RING_ROUTES.md`
- QA checklist: `docs/ANDROID_TEST_CHECKLIST.md`
- Offline visual debugging: `docs/OFFLINE_DEBUG_PREVIEWS.md`
- Privacy/store readiness: `docs/PRIVACY.md` and `docs/STORE_LISTING_DRAFT.md`
