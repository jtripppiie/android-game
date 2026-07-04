# Versioning

You Rush uses explicit Android version metadata.

## Current version

```text
versionCode: 20
versionName: 0.9.1-beta
build channel: ALASKA BETA
badge: ALASKA BETA v0.9.1
```

## File

Primary version settings live in:

```text
app/build.gradle
```

## Current fields

```gradle
versionCode 20
versionName "0.9.1-beta"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA BETA"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA BETA v0.9.1"'
buildConfigField "boolean", "SHOW_VERSION_BADGE", "true"
```

## Badge rule

Keep the badge visible in test builds.

Turn it off for a public store package.

## Version path

```text
0.9.1-beta   current Alaska beta test build
0.9.2-beta   next beta fix or tuning build
1.0.0        first public-ready package after device QA
2.0.0        later cleanup and polish milestone
```

## Release rule

1. Increase `versionCode` for every package.
2. Set `versionName` to the package version.
3. Keep `BUILD_CHANNEL` accurate.
4. Disable the badge for public builds.
5. Confirm the package builds and installs on a phone.
