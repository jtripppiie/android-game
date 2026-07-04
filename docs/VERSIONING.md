# Versioning

You Rush uses explicit Android version metadata.

## Current version

```text
versionCode: 126
versionName: 1.2.6-beta
build channel: ALASKA BETA
badge: ALASKA BETA v1.2.6
```

## File

Primary version settings live in:

```text
app/build.gradle
```

## Current fields

```gradle
versionCode 126
versionName "1.2.6-beta"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA BETA"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA BETA v1.2.6"'
buildConfigField "boolean", "SHOW_VERSION_BADGE", "true"
```

## Badge rule

Keep the badge visible in test builds.

Turn it off for a public store package.

## Version path

```text
1.2.6-beta   current milestone beta test build
1.2.7-beta   next beta fix or tuning build
1.3.0        next larger feature milestone after device QA
2.0.0        later cleanup and polish milestone
```

## Release rule

1. Increase `versionCode` for every package.
2. Set `versionName` to the package version.
3. Keep `BUILD_CHANNEL` accurate.
4. Disable the badge for public builds.
5. Confirm the package builds and installs on a phone.
