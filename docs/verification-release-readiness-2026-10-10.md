# Physical-device audit and release readiness, October 10, 2026

The main app pages were exercised and captured on an Android 17 phone and an
Android 16 tablet. The audit found comparison-test failures and unfinished
coverage. A new signed release candidate still needs verification before
publication. No release or Play submission was made during this audit.

## Source and build identity

The full test runs used `e097cb4a74c1419a9f9ab076736a813bd58197d2` and debug APK
SHA-256 `09664ec7a3a6878b28ab48d01c8504dd832eac4fb757dcb7e2e4f2e0c83179e7`.
Three documentation files already had local edits before the audit. They were
preserved and excluded from the new commits.

The subsequent countdown wording change is in `9a13a77`. Its debug APK SHA-256
is `fb8c84ef392bd57f3d237f62d678bed91bbeda35a7469526e922d0d79241945d`.
Focused tests verified that change. The proposed store captures use this APK;
the full 103-test scope was not repeated after the wording-only change.

Both builds use package `xyz.gaon.componentory`, version `1.0.0` (code `2`),
minimum SDK 24, and target/compile SDK 37. Library identities are:

| Library | Pinned version |
| --- | --- |
| Compose Material 2 | 1.10.4 |
| Compose Material 3 | 1.5.0-alpha01 |
| Compose Material icons | 1.7.8 |
| AndroidX Autofill | 1.3.0 |
| Play Games Services v2 | 22.1.0 |

## Device environments

| Setting | Phone | Tablet |
| --- | --- | --- |
| Model | Samsung SM-S731N | Samsung SM-X800 |
| Android / API | 17 / 37 | 16 / 36 |
| OS build | CP2A.260605.016 | BP2A.250605.031.A3 |
| Physical display | 1080 x 2340 px | 1752 x 2800 px |
| Density | 450 dpi | 340 dpi |
| Font scale | 1.15 | 1.0 |
| App appearance before/after | System | System |
| App locale before/after | English | System fallback |
| Original rotation | Locked portrait, rotation 0 | Automatic, stored rotation 0 |
| Original screen timeout | 300,000 ms | 600,000 ms |

Device fingerprints and ADB identities are retained in the local run manifests
and capture environment records. UI tests temporarily used English and disabled
animations. The wrapper restored its original locale and animation settings.
QA captures used Korean, the current OS, and mostly the light system appearance.
One tablet capture explicitly uses the dark app theme. Game screens use their
custom renderer. Platform and Compose samples retain their stated light themes.

Screen timeouts were temporarily extended for capture. The proposed phone store
images used a real Android display override of 1080 x 1920, with density and font
scale unchanged. The override, app languages, appearance, rotation, timeouts,
and animations were restored. Both devices were left with their screens asleep.
The updated debug APK remains installed.

Private app-file hashes were compared before and after all work. Settings,
saved runs, and game data were unchanged. Only generated profile/runtime
metadata changed. Private backups and machine-specific evidence remain ignored
under `.local/`.

## Checks and results

| Check | Result |
| --- | --- |
| Baseline `spotlessCheck`, Debug and Release lint | Passed; zero errors, 94 warnings per variant |
| Fresh app JVM tests | 218 passed, zero failures/errors/skips |
| Fresh Easter egg module JVM tests | 2 passed, zero failures/errors/skips |
| Debug app and instrumentation APK builds | Passed |
| Phone full UI scope | 103 tests: 97 passed, 6 failed |
| Tablet full UI scope | 103 tests: 85 passed, 18 failed |
| Tablet focus-failure recheck | All 16 selected tests passed after closing the notification panel |
| Countdown change formatting and both lint variants | Passed; same warning counts, zero errors |
| Countdown change JVM scope | 5 passed: translation resources and tap sequence |
| Countdown native toast and entry test | Passed separately on each device |
| Store text/assets validator | Passed for text limits, dimensions, image modes, and existing screenshot counts |

The tablet's first run had 16 `RootViewWithoutFocusException` failures while
the notification panel held focus. They passed in a separate selected recheck.
Those results do not constitute a new clean full-suite run. Two comparison
failures remain on the tablet; six remain on the phone.

The UI scope contains 36 classes covering catalog navigation and filters,
detail/provider navigation, tab reset, adaptive layouts, settings and languages,
licenses/privacy/links, introduction, version history, comparison selectors and
state, saved runs, platform/library samples, and game UI/storage/rendering.
It does not test every interaction of every catalog entry.

### Remaining comparison failures

| Scope | Observed failure | Assessment and follow-up |
| --- | --- | --- |
| `CompareStateTest.chosenIconStylesAndBadgeCountsSurviveLayoutChanges` on both devices | `icon_style_FILLED` is absent in the compact icon picker | `IconPicker` hides style/mirroring filters while the keyboard is visible or height is below 560 dp. The test selects a filter in that state. Decide how compact users should reach the filters and align the test with that behavior. General icon selection is not established as broken by this failure. |
| Three `PlatformComparisonTest` methods on the phone | Click count stayed at zero, or Espresso rejected a native sample as insufficiently visible | Samples are at or below the viewport edge; the tests tap without first scrolling the sample into view. Improve visibility handling and rerun. A successful screenshot does not establish touch behavior. |
| `LibraryComparisonTest.buttonsUseSeparateLibraryIdentitiesAndIgnoreDisabledTouch` on both devices | Expected metadata ends with `light`; actual metadata also includes `MaterialTheme` | The expected string is stale relative to the implementation identity. Update the assertion, then verify the disabled-touch checks that currently follow it. |
| `LibraryComparisonTest.frameworkAndLibraryPanelsAcceptTouchWithoutSharingState` on the phone | `family_RIGHT_MATERIAL3` is absent | The test opens the right selector without scrolling it into view. Repair the test navigation and rerun. |

These tests were investigated, but their code and behavior were not repaired in
this audit. Their outcomes must remain failed until meaningful rechecks pass.

Local evidence directory: `.local/audit-2026-10-10/`. Full scope and logs are
`ui-test-scope.txt`, `ui-tests.txt`, `tablet-ui-tests.txt`, and
`tablet-focus-recheck.txt`. Durable per-run evidence is in:

| Run | Local directory under `.local/device-runs/` |
| --- | --- |
| Phone full scope | `20261010T004551730Z-862eba31` |
| Tablet full scope | `20261010T005023456Z-cc983d5b` |
| Tablet focus recheck | `20261010T005734431Z-337b7ee2` |
| Updated phone countdown | `20261010T011012706Z-7e73c3b0` |
| Updated tablet countdown | `20261010T011051494Z-ca91710b` |

### Game verification limits

The existing synthetic stress tests passed on both devices. They render 150
enemies and four evolved weapons with an invulnerable player for ten seconds.
Measured frame P95 values were 10.38/11.37 ms on the phone and 16.12/17.08 ms on
the tablet for the moving/frozen fixtures. These are bounded rendering checks,
not completed normal games. A full physical 20-minute run and thermal soak were
not performed during this audit. Online rankings were not exercised.

## Captures and introduction policy

The local gallery is `.local/audit-2026-10-10/index.html`. It contains 33 valid
QA captures across the two devices and eight proposed store images, four each
in Korean and English. QA includes catalog/detail, comparison, runs, settings,
appearance/language/device/libraries, privacy, contributors, notices/license
text, About/introduction, history, and internal game screens. The tablet provides
wide-layout and dark-theme evidence.

One initial Contributors capture showed Settings instead of the named page.
It is excluded; a replacement was checked against the actual Contributors page.
The animated game lobby did not yield a fresh UiAutomator hierarchy. Its stale
Settings XML is marked invalid, and the game PNG is screenshot-only evidence.
The ranking PNG used an app-focus guard and makes no hierarchy or test claim.

The proposed store images show Components, Compare, Version history, and app
appearance. Hidden features are excluded from promotion. The countdown hint is
now discreet in all five languages. Store copy and the main README introduction
also focus on the learning features. Privacy disclosures and technical QA
documentation continue to describe functionality present in the app.

The local `store-ready/manifest.json` records the APK hash, source, environment,
times, and image hashes. The images are 1080 x 1920 RGB PNGs, encoded without
cropping, stretching, or invented UI. Actual SystemUI indicators remain visible.
The local ZIP contains these eight images and the manifest. These are debug-build
captures; the next signed release candidate still requires verification.

The checked-in [`screenshots`](../distribution/google-play/screenshots/manifest.json)
remain evidence of the older package. They were not overwritten. Native phone
QA captures at 1080 x 2340 exceed Play's maximum 2:1 aspect ratio and should not
be uploaded directly. Tablet QA captures at 2800 x 1752 are native wide-layout
evidence and are not the recommended 16:9 presentation set.
See Google's [preview asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

## Development and coverage still needed

1. Resolve the comparison failures above and obtain a clean relevant device run.
2. Add appropriate modern samples for missing combinations. The
   [design audit](component-design-audit-2026-10-08.csv) contains 73 framework
   entries; 50 have `NO_SAMPLE` in both Material 2 and Material 3 columns.
   Examples include calendar, scroll containers, view switchers, and layouts.
   These are missing alternatives, not proof that the UI concepts are impossible
   in modern libraries.
3. Capture and test historical widgets on their actual Android releases.
   Current-OS widgets and AOSP resource recreations cannot establish original
   appearance or behavior. Classic/Holo/Material 1 rows still contain many
   `CURRENT_OS` entries. The app's minimum SDK 24 requires separate compatible
   capture tooling for earlier OS releases.
4. Extend execution coverage beyond the API 36 and 37 devices used here. API 24
   through 35, other vendors, and broader display/accessibility configurations
   were not verified in this audit.
5. Finish real Play Games configuration and validation if rankings will be
   enabled. Both generated build variants have project ID `0` and empty
   leaderboard/ruleset IDs. Client code and unconfigured fallback UI exist;
   actual authentication, score submission/retry, leaderboard queries, profile
   changes, and Console integrity settings remain unverified. See the
   [Play Games setup guide](survivor-play-games.md).

## GitHub and Google Play release preparation

The existing [GitHub v1.0.0 release](https://github.com/gaon12/Componentory/releases/tag/v1.0.0)
is from source `2d6082ec8a717437500ee8a530a24434d28fcd9c`, published October 8
in Korea. Its APK, AAB, listing ZIP, and checksums refer to that older source.
They do not distribute or verify the current branch.

Choose a new version name and a version code greater than the highest code in
the intended Play account. Build new signed APK/AAB artifacts, test those exact
artifacts on devices, and prepare matching checksums, notes, tag, and assets.
This audit checked debug binaries and lint models, not a new signed release.
No conclusion about availability of the publisher's signing keys follows from
the current shell environment. See Google's
[release and listing guidance](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

The repository's [`app-content.json`](../distribution/google-play/app-content.json)
is a preparation draft: support email is empty, content rating is unassigned,
and audience/countries still need publisher decisions. No authenticated Console
review was performed, so the draft is not evidence of actual account or track
status. Verify contact details, IARC answers, audience, reviewer access, and
data safety against the final build. Hidden games still count toward these
answers. Enabling Play Games also requires reviewing the Google SDK's actual
[data collection](https://developer.android.com/games/pgs/data-collection).

The public [privacy policy](https://gaon12.github.io/Componentory/privacy.html)
returned HTTP 200 and matched the current canonical policy after normalizing
line endings. It already includes Play Games disclosures and the October 9
effective date. It was not outdated at the time of this check.

For personal developer accounts created after November 13, 2023, Google's
[production-access requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
include a closed test with at least 12 continuously opted-in testers for 14 days.
Account eligibility and any completed testing were not checked here. Consult
the [release preparation guide](google-play-release.md) when selecting the
actual track and submission plan.
