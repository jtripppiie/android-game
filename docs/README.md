# You Rush Documentation

This folder contains the active project documentation. Historical iteration
notes are kept in `archive/` so the main docs stay useful for development,
testing, and release preparation.

## Active Docs

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
- QA checklist: `docs/ANDROID_TEST_CHECKLIST.md`
- Offline visual debugging: `docs/OFFLINE_DEBUG_PREVIEWS.md`
- Privacy/store readiness: `docs/PRIVACY.md` and `docs/STORE_LISTING_DRAFT.md`
