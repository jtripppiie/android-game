# You Rush: Alaska Google Play release master guide

Use this as the authoritative entry point for every Play release.

## Source of truth

- Package: `com.jtripppiie.mooserush`
- Production source: `godot/`
- README production build: `5.4.2` (`versionCode 542`)
- `app/src/main/` is legacy Java for rollback/save migration only

Never publish from the legacy module. Confirm the Godot export version against
App bundle explorer, increment the code, and target the current required API.

## Release workflow

1. Freeze a clean commit and run gameplay, deterministic, computer-review,
   full-playthrough, save-migration, and Android tests.
2. Audit the exported manifest, permissions, ABIs, components, backup,
   networking, orientation, SDKs, and dependencies.
3. Reconcile `docs/PRIVACY.md`, Data safety, ads, access, content rating, target
   audience, and listing with the final build.
4. Keep the upload key and Godot export secrets outside Git. Export the
   optimized ARM64 production AAB, verify its signature, and record commit,
   engine/SDK versions, version name/code, hashes, manifest, and test results.
5. Validate store graphics/screenshots from the exact candidate.
6. Test Play delivery on supported Android versions: controls, all stages,
   bosses/objectives, checkpoints, pause, save migration, process death, audio,
   performance, thermals, offline play, accessibility, and aspect ratios.
7. Resolve pre-launch/vitals, complete required testing, and stage production.

Accurately rate fantasy/wildlife combat. Verify rights for all art, fonts,
audio, music, plugins, and data. Do not target children without Families review.
Retain commit/tag, preset, versions, hashes, tests, assets, declarations,
track/date, known issues, and reviewer messages for every release.

