# Share Card Feature

This is the next viral feature target for **You Rush: Alaska**.

## Goal

After a run ends or a boss is defeated, the player should get a simple shareable recap card.

The card should make the player want to send a screenshot to someone because it features:

- Their uploaded face character
- The Alaska stage name
- Score
- Boss result
- A funny local line
- You Rush branding

## First version

Do not overbuild this.

The first version can be a static in-game panel that says:

```text
YOU RUSH: ALASKA
I survived Moose Pass.
Score: 240
Boss: Moose Boss defeated
TripperDeeLabs
```

For failed runs:

```text
YOU RUSH: ALASKA
Alaska bonked me.
Score: 80
Cause: Antler gate
TripperDeeLabs
```

## UI placement

Add a button on the game-over and stage-clear panels:

- `SHARE CARD`

Tap should open a new share-card preview screen.

## Screen states

Recommended new state:

```java
STATE_SHARE_CARD
```

The screen should include:

- Alaska background
- Dark overlay panel
- Character preview
- Large score
- Stage name
- Boss result or fail cause
- Buttons: `BACK`, `SAVE LATER` or `DONE`

## Why this matters

The personal photo is the hook. The share card turns a normal score screen into a viral artifact.

The share feature should stay local and privacy-respecting. Do not upload the photo. Do not create accounts.

## Later version

Later, use Android sharing APIs to export the card as an image.

Possible flow:

1. Render share card to Bitmap.
2. Save temporary image through FileProvider.
3. Launch Android share sheet.
4. Let user choose messages, email, socials, etc.

## Copy ideas

- `Alaska remains undefeated.`
- `A moose ended my run. Respectfully.`
- `I got bonked in Bear Country.`
- `Certified local chaos survivor.`
- `My face fought the Salmon Boss.`
- `Dark Winter was rude.`

## Keep out of scope for now

- Public leaderboards
- Accounts
- Cloud saves
- Auto-posting
- Uploading photos
- Social login
