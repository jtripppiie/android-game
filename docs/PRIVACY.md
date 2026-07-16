# Privacy Notes

**You Rush** is designed as a local-first Android game.

## Local-first design

The current app does not require an account.

The current app does not use a server profile.

The current app does not need network access for normal play.

## Photo personalization

The player may choose an image through the Android system file picker.

The image is used locally for the game character.

The app keeps a local reference so the selected image can be restored later on the same device when Android allows access.

## Local preferences

The app stores simple local preferences, including:

- Best score
- Selected stage
- Selected season
- Unlocked stage
- Debug overlay setting
- Selected image reference
- Local XP, levels, Trail Tokens, Trail Passport badges, Daily Rush streak, and Expedition Logs

The Godot build stores campaign progress, five best scores, total score,
accessibility options, a local photo path, and a one-time Java migration flag.

## Permissions and network

The current app declares no network permission.

The photo picker uses Android's system document picker. The selected image is
decoded locally, sampled down for memory safety, and never uploaded by the app.

Voice notes launch the speech-recognition provider installed on the Android
device. That provider may process speech according to its own privacy policy.
You Rush receives only the returned transcription and saves review notes in
local app storage; it does not upload them itself.

## Public release note

Before a public app-store launch, publish this policy at a stable public URL and
use that URL in the store listing. The app behavior described here is the
intended launch behavior for the local-first beta.
