# Component design verification: October 8, 2026

The [source audit](component-design-audit-2026-10-08.md) records historical
artwork, API identity, and remaining alternatives. This report records only
the checks executed for the focused fixes. Package `xyz.gaon.componentory`,
debug version `1.0.0`, code `2`, minimum API 24, target SDK 37, JDK 25.

## Source and ordered checks

| Commit | Change | Checks before commit |
| --- | --- | --- |
| 75303ee | Bind native analog clocks to pinned AOSP dials and hands | Ruff lint/format, four resource/provenance tests, Spotless apply/check, debug lint, 171 JVM tests, debug and test APK builds. The device test compiled here and ran in the following scope. |
| 8339988 | Remove false Expressive-only API restrictions | Spotless apply/check, debug lint, 171 JVM tests, both APK builds, seven tablet tests. |
| 7257130 | Add four explicitly labeled modern clock concepts | Spotless apply/check, debug lint, 173 JVM tests, both APK builds, seven tablet clock tests. |
| bbf1285 | Explain missing alternative samples in five languages | Spotless apply/check, debug lint, 173 JVM tests, both APK builds, all fourteen final tablet tests. |

Every application change followed code, formatter/lint, tests, diff review,
and focused commit. Tests therefore ran on pre-commit working trees. Device
manifests retain the parent revision, changed paths, exact scope, and APK
hashes. The documentation commit does not alter compiled application inputs.

Final debug lint has **zero errors and 75 warnings**. Twelve additional
warnings identify intentionally identical upstream clock artwork; earlier
release lint had 63. This is not a warning-free result. All **173 JVM tests**
passed across 21 reports, with zero failures, errors, or skips. The clock
resource tests also verify public XML bindings, immutable provenance, source
hashes, retained notices, and unchanged bitmap bytes.

## Physical tablet

Tests used Samsung SM-X800 (`gts8pwifi`, device identity `R54T202R4BK`), Android
16/API 36, build `BP2A.250605.031.A3`. Fingerprint:
`samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZH3:user/release-keys`.
Physical size is 1752 x 2800; logical size is 2800 x 1752 in landscape,
rotation 90 degrees, density 340, physical font scale 1.0, system locale ko-KR.
The test runner uses English app strings; the native clock locale test also
executes all five translations. An empty per-app locale and animation scales
1.0 were recorded before each final run and restored afterward. Every passing
run below has an empty restoration-error list.

| Sample choice | Actual sample configuration |
| --- | --- |
| Classic | android:Theme.Light; selected Android 2.3.7 resource bindings |
| Holo | android:Theme.Holo.Light; selected Android 4.4.4 resource bindings |
| Material Design 1 | android:Theme.Material.Light; selected Android 5.0.2 resource bindings |
| Material Design 2 | Material 1.10.4, lightColors, own Typography |
| Material Design 3 | Material 3 1.5.0-alpha01, lightColorScheme, standard motion |
| Material You | Same artifact, dynamicLightColorScheme, API 31+ |
| Expressive | Same artifact, MaterialExpressiveTheme, expressive colors and motion |

Icons are 1.7.8 and the optional Autofill helper is 1.3.0. The app shell and
system appearance are separate from these explicit sample themes. Device
manifests retain the actual configuration and display snapshots. The clock
layout test's 150dp viewport and font scale 1.8 are local Compose overrides;
the physical device settings were not changed to those values.

## Executed device scopes

| Run ID | Scope | Result |
| --- | --- | --- |
| 20261007T232704719Z-18b06b9b | ExpressiveDesignsTest (6), pinned AnalogClock viewport (1) | 7 passed |
| 20261008T000443230Z-317e217d | ThemedClocksTest (3), ClockSamplesTest (4) | 7 passed |
| 20261008T002304036Z-2401f45e | MissingLibrarySamplesTest (2), ThemedClocksTest (3), ExpressiveDesignsTest (6), pinned AnalogClock viewport (1), ComponentInventoryResourceTest (2) | 14 passed |

The final scope asserts the actual missing-sample title and explanation,
distinct native requirements, all four modern clock choices, live ticking,
narrow text overflow, start/stop/resume/reset, disabled timer controls, 16sp
action labels, real experimental-library actions, selected theme colors,
pinned native clock bounds, and the bundled canonical inventory/hash.

The earlier seven-clock scope additionally asserts native widget identities,
format switches, copies to modern clocks, recreation, and five-language notes.
It used its own binary; these checks are not counted as extra tests in the
final fourteen-test run. CatalogRenderingSmokeTest compiled with the new clock
branch but the full catalog sweep was not rerun in this audit.

Retained unsuccessful attempts:

- `20261007T232612355Z-9cacde47`: six passed and one initialization error due
  to a mistyped clock-test package. The corrected seven-test scope passed.
- `20261007T234358509Z-a101974c`: six passed and one stale Chinese punctuation
  fixture failed. The translated fixture was corrected and all seven passed.
- `20261008T001618330Z-ce985918`: thirteen passed; the new title check read
  the parent layout rather than its text child. The corrected assertion
  retains the text and visibility check; the complete fourteen-test scope
  then passed. The app APK remained unchanged by this test-only correction.

These failed attempts are not described as passing. Full instrumentation text
and structured evidence remain under `.local/device-runs/<run-id>/` outside Git.
No smartphone tests ran in this audit, following the request to use the tablet.

## APK identities

| Scope / APK | SHA-256 |
| --- | --- |
| API-theme fix app | `91a13bb350ae3f6201b9edb0be4fd33e85409761c7ec0a7f80971097d8d709ad` |
| API-theme fix tests | `2e42a6631a12ef7512cd37deadb4256990b7578465f8ee36fe6ce4da7ef89409` |
| Seven-clock scope app | `c8610ab6206ff91b1d49d6562bdb18bcf34052d0a6381fe89a006276e89f561c` |
| Seven-clock scope tests | `a76ecf050603153fb2d050b3de2ef0eba6ca290609b8c1800fab7dc73dd49a1e` |
| Final app | `9c62c4d60c78431afe285ac37d4de4426e159f56693d1453bf7bde560b5e5ed9` |
| Final tests | `8b1ca0261479302fd283dcc22fd96745a926e29ae1b09599112be6cd24cd83b7` |

## Evidence limits and deployment

Classic, Holo, and Material Design 1 AnalogClock artwork is bound to immutable
AOSP releases. Its engine still runs on Android 16. These checks do not prove
original historical OS appearance or behavior. Original captures are missing.
Modern clocks are Componentory theme demos, not official Material clock APIs.
The audit matrix is source evidence and does not imply every cell passed an
interaction test. Remaining current-OS resource closures and alternative
samples are documented explicitly in the audit.

The final debug build was installed with `adb install -r` during the tablet
checks, preserving existing app data. APK identities and installation results
remain in the local run evidence. Final installation and screen-off checks
have a separate local finalization record; they do not add interaction coverage.
The published GitHub 1.0.0 release and its original
store screenshots remain the separate artifacts described in
[release verification](verification-release-2026-10-08.md). This development
audit does not publish a new Play listing or silently replace those assets.
