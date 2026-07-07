# Versioning

You Rush uses explicit Android version metadata.

## Current version

```text
versionCode: 272
versionName: 2.7.2-beta
build channel: ALASKA BETA
badge: ALASKA PASSPORT v2.7.2 BETA
```

## File

Primary version settings live in:

```text
app/build.gradle
```

## Current fields

```gradle
versionCode 272
versionName "2.7.2-beta"

buildConfigField "String", "BUILD_CHANNEL", '"ALASKA BETA"'
buildConfigField "String", "BUILD_BADGE", '"ALASKA PASSPORT v2.7.2 BETA"'
buildConfigField "boolean", "SHOW_VERSION_BADGE", "true"
```

## Badge rule

Keep the badge visible in test builds.

Turn it off for a public store package.

## Version path

```text
2.7.2-beta   splash title separation beta
2.7.1-beta   splash title spacing beta
2.7.0-beta   scout and boss clarity beta
2.6.0-beta   control and route-intel beta
2.5.0-beta   visual contract beta
2.4.0-beta   progression polish beta
2.3.0-beta   gameplay polish beta
2.2.0-beta   launch-readiness beta
2.1.0-beta   expedition systems beta
2.0.0-beta   Alaska Passport beta baseline
1.9.6-alpha  Trail Passport alpha
1.9.5-alpha  Daily Rush alpha
1.9.4-alpha  replay-value alpha
1.8.7-alpha  smooth-retention alpha
1.8.6-alpha  launch-polish alpha
1.8.5-alpha  asset cleanup alpha
1.8.4-alpha  runner scale and cadence alpha
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
