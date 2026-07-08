# You Rush 2.2.0 Beta Launch Readiness

Release owner pass for a conservative public beta candidate.

```text
versionCode: 220
versionName: 2.2.0-beta
build badge: ALASKA PASSPORT v2.2 BETA
APK: app/build/outputs/apk/debug/you-rush-alaska-2.2.0-beta-220-debug.apk
```

## Release focus

This pass prioritizes launch quality over new scope:

- Build, lint, and unit-test readiness
- Crash-risk reduction
- Version metadata consistency
- Store and privacy documentation
- Install-test documentation
- Clear remaining launch risks

## Code changes

- Hardened photo personalization import against large gallery images.
- Added sampled decoding for pre-Android P devices instead of full-size bitmap loading.
- Added Android P+ decode target sizing so very large photos are reduced before gameplay use.
- Catches image decode memory failures and falls back to the existing friendly rejection flow.
- Preserves local-first photo behavior: the picked image is decoded on device and is not uploaded.

## Documentation changes

- Updated README current beta status, version metadata, APK filename, and beta checklist.
- Updated VERSIONING current version, Gradle fields, and version path.
- Updated Android QA checklist for 2.2.0-beta install testing and large-photo import testing.
- Updated privacy notes for local progression data, photo decoding, and no-network behavior.
- Updated store listing draft with current beta systems and privacy/store-readiness notes.

## Verification checklist

Completed on July 6, 2026:

```bash
./gradlew testDebugUnitTest  # PASS
./gradlew lintDebug          # PASS
./gradlew assembleDebug      # PASS
```

Debug APK size: 21 MB

Debug APK SHA-256:

```text
d13dedd7b5e9f3379cdcb17fafb946edaccd52bcdb58ecc1761697a714f6a5c9
```

Then install the generated APK on a real Android phone and complete:

```text
docs/ANDROID_TEST_CHECKLIST.md
```

## Remaining launch risks

- A real-device 15-minute smoke test is still required before public distribution.
- A signed release APK or Play-ready AAB still requires release signing credentials.
- A hosted privacy policy URL is still required for store publication.
- Store screenshots, content rating, target audience answers, and Play Console review still require external store access.
- Generated tone SFX are functional beta placeholders.
- Some boss attack patterns remain simple and should be tuned after phone QA.
