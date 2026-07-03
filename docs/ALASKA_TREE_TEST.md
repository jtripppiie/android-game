# Alaska Tree Test

This build adds a first prototype of a temporary tree escape button.

## Files

```text
app/src/main/java/com/jtripppiie/mooserush/AlaskaSurvivalMooseRushView.java
app/src/main/java/com/jtripppiie/mooserush/MainActivity.java
app/build.gradle
```

## Version

```text
ALASKA DEV v0.2.2-alpha
versionCode 4
```

## What to test

1. Install the latest APK.
2. Start an Alaska stage.
3. Look for the tree in the play area.
4. Tap the **CLIMB TREE** button.
5. Confirm the character moves to the branch.
6. Confirm the timer/cooldown appears.
7. Confirm normal play resumes after the escape window.
8. Confirm the normal throw control still works during the stage.

## Notes

This is a prototype layer. It is intentionally separated from the large core game file so the mechanic can be tested before it is merged deeper into the main gameplay loop.
