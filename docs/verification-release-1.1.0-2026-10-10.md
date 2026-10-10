# Release 1.1.0 verification, October 10, 2026

The release payload passed 108 UI tests on each physical device. The compact
icon picker now keeps its style and mirroring filters reachable. Comparison
tests scroll samples and controls into view before touching them and check the
actual Material 3 theme identity. The original interaction and independence
assertions remain in place.

## Build and signing identity

Application source: `8cc1c2f6df6c3375abb2a8207ad81f6f29ae03fd`.
Package: `xyz.gaon.componentory`; version `1.1.0`, code `3`; minimum SDK `24`,
target and compile SDK `37`. The only dirty files during the device tests were
three pre-existing documentation edits, which were preserved and excluded.

| Artifact | SHA-256 |
| --- | --- |
| Public signed APK | `058efb9556ddc32f06e729b5a01d4b74d4c0df5e696f5b3a871f218f19961253` |
| Public signed AAB | `d6def8ae78bebd8f4dbf9136528676ce8befbf057591c111d362b645c412ac0b` |
| Universal release payload used on devices | `db42e3224354bfae0b88134f8d3c5da238bbbe45b64044591b0144c86d22c76b` |

The public APK and AAB use the existing signing certificate, SHA-256
`a8759906836cae6755b90a415ef1f0ddb44d8c7232a8ccc5041e26fca8990950`.
No new local upload key was generated. Google Play manages its delivered app
signing key separately. Bundletool validated the release manifest.
APK v2 signing, AAB signing, packaged privacy/notices, ZIP alignment, and static
16 KB native-library alignment were verified. A 16 KB runtime was not tested.

The device APK was generated from that AAB and re-signed with the existing debug
certificate to preserve installed user data. Its application is non-debuggable
and uses the release payload. It is not an APK delivered by Google Play. After
testing and captures, both devices were updated to the current debug APK with
the same debug certificate so private-file preservation could be verified.

## Checks

| Check | Result |
| --- | --- |
| `spotlessApply`, `spotlessCheck` | Passed |
| Debug and Release lint | Passed; 0 errors and 94 warnings per variant |
| App JVM tests | 218 passed |
| Debug app and instrumentation builds | Passed |
| Signed release APK and AAB builds | Passed |
| Phone release-payload UI scope | 108 passed, 175.883 seconds |
| Tablet release-payload UI scope | 108 passed, 176.774 seconds |
| Five-language store text and phone asset validator | Passed |
| Screenshot provenance | All 12 installed-APK hashes match the tested payload |

Local evidence is retained under `.local/device-runs/`:
`20261010T023518055Z-cc92446b` for the phone and
`20261010T023910392Z-111cfec0` for the tablet. Each manifest records the source,
APK identities, OS fingerprint, display, themes, pinned libraries, scope, result,
and restoration. This is the earlier 103-test scope plus five icon-browser tests.
It covers 37 classes, not every interaction of every catalog entry.

The failed and interrupted development rounds remain separate local evidence.
The earlier [readiness audit](verification-release-readiness-2026-10-10.md)
describes the failures before these repairs; it remains a historical record.

## Devices and restoration

| Setting | Phone | Tablet |
| --- | --- | --- |
| Model | Samsung SM-S731N | Samsung SM-X800 |
| Android / API | 17 / 37 | 16 / 36 |
| OS build | CP2A.260605.016 | BP2A.250605.031.A3 |
| Physical display | 1080 x 2340 | 1752 x 2800 |
| Density | 450 dpi | 340 dpi |
| Font scale | 1.15 | 1.0 |
| Capture display | 1080 x 1920 temporary override | Native 2800 x 1752 landscape |
| Capture app/sample theme | Light / Light | Light / Light |
| Restored app theme | System | System |
| Restored app locale | English (`en`) | System fallback (`[]`) |
| Restored rotation | Locked portrait, rotation 0 | Automatic, stored rotation 0 |
| Restored screen timeout | 300,000 ms | 600,000 ms |

The test wrapper restored locale and animation settings. Capture overrides and
app appearance were restored afterward. Private-file hashes were compared with
the backups taken before this work. Settings, saved runs, and game files match.
Only generated profile/runtime metadata changed. No app data was cleared and
neither app was uninstalled. Backups and machine-specific tools remain ignored.

## Screenshots

These are actual current-OS app captures, encoded as RGB without cropping or
stretching. Screenshots are visual evidence; interaction results come from the
tests above. The manifests include actual installed payload hashes and public
release hashes. Tablet images retain their native aspect ratio rather than the
recommended 16:9 presentation ratio.

| Phone locale | Components | Compare | Version history | Appearance |
| --- | --- | --- | --- | --- |
| Korean | [Image](../distribution/google-play/screenshots/ko-KR/01-components.png) | [Image](../distribution/google-play/screenshots/ko-KR/02-compare.png) | [Image](../distribution/google-play/screenshots/ko-KR/03-history.png) | [Image](../distribution/google-play/screenshots/ko-KR/04-appearance.png) |
| English | [Image](../distribution/google-play/screenshots/en-US/01-components.png) | [Image](../distribution/google-play/screenshots/en-US/02-compare.png) | [Image](../distribution/google-play/screenshots/en-US/03-history.png) | [Image](../distribution/google-play/screenshots/en-US/04-appearance.png) |

| Tablet locale | Components | Compare | Version history | Appearance |
| --- | --- | --- | --- | --- |
| Korean | [Image](../distribution/google-play/tablet-screenshots/ko-KR/01-components.png) | [Image](../distribution/google-play/tablet-screenshots/ko-KR/02-compare.png) | [Image](../distribution/google-play/tablet-screenshots/ko-KR/03-history.png) | [Image](../distribution/google-play/tablet-screenshots/ko-KR/04-appearance.png) |

See the [phone manifest](../distribution/google-play/screenshots/manifest.json)
and [tablet manifest](../distribution/google-play/tablet-screenshots/manifest.json)
for fingerprints, timestamps, display settings, hashes, and library versions.
The store captures focus on learning features. Hidden-feature QA is separate.
Historical resource recreations remain labelled as recreations, and missing
original captures remain explicit in the app.

## Work still needed

- Historical widgets need captures and interaction checks on their actual OS
  releases. Current-OS widgets and resource recreations do not establish this.
- The [design audit](component-design-audit-2026-10-08.csv) still has 50 of 73
  framework entries with `NO_SAMPLE` in both Material 2 and Material 3 columns.
- API 24 through 35, other vendors, broader accessibility configurations, and
  a physical 20-minute game/thermal soak were not verified here.
- Online rankings remain unconfigured: project ID `0`, empty leaderboard and
  ruleset IDs. Real authentication, submission, retries, and profile changes
  need [Play Games configuration and validation](survivor-play-games.md).

## Publication status

[GitHub 1.1.0](https://github.com/gaon12/Componentory/releases/tag/v1.1.0) is public.
Google Play accepted the same AAB hash and the 1.1.0 production submission was
requested on October 10. All five listings, 12 actual device screenshots, and
178 country entries are saved. Console shows Changes in review while quick
checks run; successful checks allow the submission to proceed to review.
Managed publishing is disabled. Google approval and public availability remain
unconfirmed. See [app-content.json](../distribution/google-play/app-content.json)
and the [release guide](google-play-release.md) for the recorded submission state.
