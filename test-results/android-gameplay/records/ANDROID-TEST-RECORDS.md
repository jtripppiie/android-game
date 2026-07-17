# Android Test Records

## Baseline launch

File: `before/01-launch.png`
Build: `f4739229ce49020ef56269d6094d87de2132fe39`, emulator-only x86_64 export
Stage: none
Starting state: clean app launch on Android 11 x86_64 AVD
Input: `am start` / launcher intent
Expected: Godot boot and visible splash
Observed: CPU-emulated Android System UI displayed an ANR dialog
Relevant log: `before/logcat.txt`
Tester type: emulator automation
Status: FAIL

## Baseline main menu

File: `before/02-main-menu.png`
Build: `f4739229ce49020ef56269d6094d87de2132fe39`, emulator-only x86_64 export
Stage: none
Starting state: APK launched
Input: wait for main scene
Expected: rendered main menu
Observed: gray app frame; GLES shader requested 261 uniform vectors, driver max 256
Relevant log: `before/logcat.txt`
Tester type: emulator automation
Status: FAIL

## Reworked main menu attempt

File: `after/02-main-menu.png`
Build: `FINAL_IMPLEMENTATION_SHA`, x86_64 debug APK
Stage: none
Starting state: in-place upgraded package with migration fixture
Input: debug intent scenario `main-menu`
Expected: verification-ready marker and rendered main menu
Observed: Android System UI ANR under CPU emulation; Godot log repeated the
261-vs-256 shader failure and no verification-ready frame was produced
Relevant log: `after/logcat.txt`
Tester type: emulator automation
Status: FAIL

## Profile migration fixture

File: `records/migration-baseline-profile.cfg`
Build: 5.3.2 upgraded in-place to 5.4.0
Stage: campaign profile
Starting state: injected documented baseline fixture; app data not cleared
Input: `adb install -r`
Expected: original profile remains byte-for-byte readable
Observed: SHA-256 remained
`f5a0209cb1487c4567225f650752f7d8b552c81926cbe37be8e06f1b641ad865`
Tester type: emulator automation
Status: PASS

## Required screenshots and videos not produced

Files: screenshots 03–32 and all requested gameplay videos
Build: 5.4.0 x86_64 debug APK
Stage: 0–4 as applicable
Starting state: installed APK
Input: deterministic production-system scenario hooks
Expected: rendered gameplay state and verification-ready log
Observed: renderer failed before a trustworthy frame
Relevant log: `after/logcat.txt`
Tester type: emulator automation
Status: NOT VERIFIED

These missing files were not replaced with mock scenes, manually positioned
sprites, browser captures, or fabricated gameplay evidence.
