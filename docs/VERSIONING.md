# Versioning

You Rush uses explicit Android version metadata.

## Current version

```text
versionCode: 184
versionName: 1.8.4-alpha
build channel: ALASKA BETA
badge: ALASKA ART v1.8.4
```

## File

Primary version settings live in:

```text
app/build.gradle
```

## Current fields

```gradle
versionCode 184
versionName "1.8.4-alpha"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA BETA"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA ART v1.8.4"'
buildConfigField "boolean", "SHOW_VERSION_BADGE", "true"
```

## Badge rule

Keep the badge visible in test builds.

Turn it off for a public store package.

## Version path

```text
1.8.4-alpha  current runner scale and cadence alpha
1.8.3-alpha  sprite-sheet art alpha
1.8.2-alpha  direct moose renderer alpha
1.8.1-alpha  moose and hurdle readability alpha
1.8.0-alpha  graphics architecture alpha
1.7.5-beta   graphics, performance, and playability beta
1.6.5-beta   graphics polish beta
1.5.0-beta   milestone polish beta
1.5.1-beta   next graphics or tuning build
1.4.0        next larger feature milestone after device QA
2.0.0        later cleanup and polish milestone
```

## Release rule

1. Increase `versionCode` for every package.
2. Set `versionName` to the package version.
3. Keep `BUILD_CHANNEL` accurate.
4. Disable the badge for public builds.
5. Confirm the package builds and installs on a phone.
