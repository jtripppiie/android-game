# Release 3.25 Beta - Defensive Boss Combat

Five-item milestone pass to make boss fights more interactive and worthy of a larger `3.25` beta label.

## Version

versionCode: 325
versionName: 3.2.5-beta
build badge: ALASKA PASSPORT v3.25 BETA
expected APK: app/build/outputs/apk/debug/you-rush-alaska-3.2.5-beta-325-debug.apk

## What changed

1. Snowballs can now shatter boss ice projectiles before they hit the player.
2. Throw aim now prioritizes nearby shootable boss projectiles before the boss body.
3. Boss projectile tells now teach `JUMP OR FIRE`.
4. The final bear boss now stands up for a new eye-laser attack that must be jumped over.
5. Debug boss-attack badges now identify shootable projectiles as `ICE FIRE`, lasers as `LASER JUMP`, and shockwaves as `WAVE DODGE`.

## QA focus

- Enter any boss fight with an ice/projectile pattern.
- Confirm the tell reads `JUMP OR FIRE`.
- Fire snowballs into ice projectiles and confirm they shatter with score, particles, and meter reward.
- Confirm the boss HP text shows `FIRE PROJECTILES` while shootable attacks are active.
- Reach the final bear boss after phase two and confirm it stands up before the eye-laser attack.
- Jump over the eye laser and confirm DEBUG labels it `LASER JUMP`.
- Turn DEBUG on and confirm attack badges distinguish `ICE FIRE`, `LASER JUMP`, and `WAVE DODGE`.
