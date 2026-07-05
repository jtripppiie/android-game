# Branching and Releases

This project uses Git branches and Android package versions for different
things. They should not be expected to share the same number.

## Current Rule

`main` is the release/integration branch.

Feature branches should describe the work, not the Android version:

```text
graphics-1.8.0          historical graphics branch
alaska-winter-polish    example future feature branch
replay-economy          example future feature branch
```

Android versions live in `app/build.gradle`:

```gradle
versionCode 194
versionName "1.9.4-alpha"
```

The visible in-app test badge also lives in `app/build.gradle`:

```gradle
buildConfigField "String", "BUILD_BADGE", '"ALASKA RUSH v1.9.4"'
```

## Why They Differed

The branch named `graphics-1.8.0` began as the graphics architecture branch.
Work continued there after the Android package advanced to `1.8.6-alpha`,
`1.8.7-alpha`, and `1.9.4-alpha`.

That was not a runtime bug. It was branch naming drift.

## Going Forward

Use this flow:

1. Start work from `main`.
2. Create a branch named after the feature or milestone.
3. Make the changes.
4. Bump `versionCode` and `versionName` only when creating a package build.
5. Run `./gradlew testDebugUnitTest`.
6. Run `./gradlew assembleDebug`.
7. Merge back to `main`.
8. Push `main`.

## APK Rule

APK files are build artifacts. They are ignored by git.

Local APKs are generated here:

```text
app/build/outputs/apk/debug/
```

GitHub Actions uploads the debug APK as:

```text
you-rush-alaska-debug-apk
```

## Required Release Checklist

Before calling a package ready for device testing:

1. Confirm `docs/VERSIONING.md` matches `app/build.gradle`.
2. Confirm `README.md` has the current APK filename.
3. Confirm `docs/ANDROID_TEST_CHECKLIST.md` has the current package version.
4. Run unit tests.
5. Build the debug APK.
6. Push `main`.
7. Download the GitHub Actions APK artifact.
8. Install on a real Android device.
9. Run the phone checklist.
