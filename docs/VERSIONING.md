# Versioning

You Rush now has explicit debug-build version metadata.

## Current version

```text
versionCode: 2
versionName: 0.2.0-alpha
build channel: ALASKA DEV
badge: ALASKA DEV v0.2.0-alpha
```

## Where versioning lives

Primary version settings are in:

```text
app/build.gradle
```

Current fields:

```gradle
versionCode 2
versionName "0.2.0-alpha"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA DEV"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA DEV v0.2.0-alpha"'
buildConfigField "boolean", "SHOW_VERSION_BADGE", "true"
```

## Temporary version badge

`MainActivity` overlays the version badge on top of the game view when `SHOW_VERSION_BADGE` is true.

The badge is intentionally temporary and should stay visible in internal/debug builds so screenshots and APK tests are easy to identify.

## Suggested version pattern

Use this pattern while Alaska is still the main prototype:

```text
0.2.0-alpha    Alaska gameplay prototype
0.2.1-alpha    small tuning/fix build
0.3.0-alpha    new major Alaska feature
0.4.0-beta     Alaska feature-complete test build
1.0.0          first public release candidate/release
```

## Release rule

Before a public/release build:

1. Increment `versionCode`.
2. Set `versionName` to the release version.
3. Change `BUILD_CHANNEL` from `ALASKA DEV` to the correct release channel.
4. Set `SHOW_VERSION_BADGE` to `false` unless intentionally shipping a visible version badge.
5. Confirm the APK/AAB is signed with a release key, not only a debug key.
