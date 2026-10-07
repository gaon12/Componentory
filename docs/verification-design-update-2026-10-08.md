# Verification: design and resource update, October 8, 2026

This update adds the supplied flask-and-cube icon, category-based settings,
first-launch introduction, seven design choices, real Expressive APIs, imported
AOSP control artwork, and app-owned Toast recreations. The catalog now contains
168 entries and 248 canonical source rows: 247 Implemented and one Recreated
(`android.widget.Toast`). No Planned rows remain.

The results below concern the installed modern phone OS and recorded library
pins. A resource recreation does not establish original historical Android
appearance or behavior. Original historical OS captures remain missing.
The [earlier verification](verification-2026-10-08.md) covers the previous
160-entry application binary and retains its separate tablet evidence.

Run IDs and capture timestamps use UTC. The device verification and final
review occurred on October 8 in Korea Standard Time.

## Source and binary identity

Application changes end at `6b1caf9`; later focused changes maintain device
tests. Final test maintenance ends at `bb7817d`.

- App APK SHA-256: `db9c0e977970bcaa07e49142b73105d9934b34a005022b6b66d2ae4f0f008488`.
- Test APK SHA-256: `a9ad42d89bf0dd75061f9b78f9391a5f396b60058e9cb9bd7ac84a18db958f89`.
- Outputs: `app/build/outputs/apk/debug/app-debug.apk` and
  `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

Run manifests record their actual parent revision and working-tree changes.
Pre-commit verification can therefore correctly record a dirty tree. Markdown
documentation changes do not affect either APK.

## Phone environment

| Item | Recorded value |
| --- | --- |
| Device | Samsung SM-S731N, r13s |
| OS | Android 17, API 37 |
| Build | CP2A.260605.016 |
| Fingerprint | samsung/r13sksx/r13s:17/CP2A.260605.016/S731NKSU9CZIF_OKR9CZIF:user/release-keys |
| Display | 1080 × 2340, portrait, 450 dpi |
| Font scale | 1.15 |
| Target/minimum SDK | 37 / 24 |
| Framework themes | Theme.Light, Theme.Holo.Light, Theme.Material.Light; explicit light resources |
| Material 2 | 1.10.4, lightColors |
| Material 3 | 1.5.0-alpha01, standard lightColorScheme and standard motion |
| Material You | Same Material 3 pin, dynamicLightColorScheme on API 31+ |
| Expressive | Same experimental pin, MaterialExpressiveTheme, expressive palette and motion |
| Icons / inline helper | 1.7.8 / AndroidX Autofill 1.3.0 |

The system uses dark mode. Device tests cover app light and dark settings,
system bars, native light resource isolation, and saved sample state. The
instrumentation runner uses English and restores the original app locale.
Visual review uses Korean and restores saved preferences and locale.

## Checks and retained milestones

Spotless and Android lint run before the affected tests for each focused code
change. Lint reports zero errors and 62 warnings, including deliberately pinned
libraries, legacy APIs, duplicate upstream artwork, and source resource warnings.
The current JVM suite has 171 passing tests with no failures, errors, or skips.
Both debug APKs build. AOSP control and Toast Python fixtures have six passing
tests; Ruff lint and format checks also pass for the affected history tools.

| Run ID | Scope | Result |
| --- | --- | --- |
| 20261007T160452823Z-168bdd7f | Category settings, appearance, language, notices, navigation | 11 passed; one wide-device test skipped |
| 20261007T161646373Z-91bda99f | Introduction and settings replay | 4 passed |
| 20261007T165744795Z-7cf92aa7 | Historical resource controls and notices | 5 passed |
| 20261007T171951366Z-847bf17c | Expressive themes/APIs and provider identity | 8 passed |
| 20261007T172721756Z-dc7a4501 | Three modern design catalog sweeps, 504 cells | 3 passed |
| 20261007T174057290Z-dbc05b0d | Material 2 catalog sweep, 168 cells | 1 passed |
| 20261007T174555414Z-278bb6a0 | Canonical inventory and resource identity | 2 passed |
| 20261007T182309744Z-4285408e | Toast artwork, palette, timeout, repeated taps, copying, recreation, languages | 11 passed |
| 20261007T183731097Z-d44b67bc | Long provider labels and detail navigation | 7 passed |
| 20261007T184309523Z-4833d461 | Six native animated progress styles with actual resource bindings | 1 passed |

These milestones have their own binaries in their manifests and are not counted
as extra final-binary passes. The first Toast regression exposed a real touch
interception bug: a visible popup blocked a repeated native trigger tap. The
popup now uses non-touchable, non-focusable window flags, and the repeat-tap
and timeout checks passed after that correction.

The first combined 40-test regression, `20261007T184550948Z-711c1a6b`, had
37 passes, two failures, and one wide-device skip. Its failures and subsequent
corrections are recorded with the final rerun rather than described as passes.
The appearance test previously tapped a native button without bringing its host
into the phone viewport. It now scrolls before the physical tap and asserts the
click count before leaving for settings. Transient tests also scroll their
triggers and assert the selected component and both design labels. The old
transient configuration failure did not recur with those explicit input guards.
No unsupported assertion was removed or weakened.

The targeted rerun, `20261007T190257712Z-ea2ae590`, passed all eight appearance
and transient tests. The final combined run, `20261007T190413027Z-19baa1e2`,
passed 42 methods with no failures and one wide-device skip. It also verifies
the real Holo/Material ActionBar activities, their menu navigation, and the
inline demo isolation from the usual autofill provider. Device-setting
restoration reported no errors. These runs use the final APK hashes above.

The actual inline host run, `20261007T191201134Z-57fd4cbe`, passed all three
Classic/Holo/Material methods with the same final APK hashes. The wrapper
restored the original keyboard, enabled IMEs, subtype, subtype history, and
autofill provider, with no recovery errors. Installation twice required the
visible Play Protect choice; the APK was not submitted for analysis.

The native animation run used the same final app APK and the previous test APK
`e616146d06389231fae6623a923b57bd95e210823ac7a9b754d630033cd944fb`.
It checks actual imported drawable bindings and visible Animatable execution
for six styles with animator duration scale 1.0, then restores the device
scales. It does not infer historical behavior from a screenshot.

## Final catalog sweep

Run `20261007T191306908Z-78789bee` passed all seven family methods with no
skips or restoration errors, using the final APK hashes above. The sweep
examines all 168 entries for each design and checks an actual sample or its
explicit unsupported explanation. It reserves real host and continuously
animated APIs for their dedicated tests.

| Design | Rendered supported samples | Explained unsupported | Dedicated cells |
| --- | ---: | ---: | ---: |
| Classic | 69 | 96 | 3 |
| Holo | 69 | 95 | 4 |
| Material Design 1 | 69 | 95 | 4 |
| Material Design 2 | 50 | 118 | 0 |
| Material Design 3 | 102 | 66 | 0 |
| Material You | 102 | 66 | 0 |
| Material 3 Expressive | 111 | 57 | 0 |
| Total | 572 | 593 | 11 |

The 11 dedicated cells comprise six native animated styles, two actual
ActionBar activities, and three actual inline hosts. All have passing
current-phone tests described above with this final app APK. The rendered
count includes the seven explicitly recreated Toast samples and does not
claim 572 direct platform/library APIs or exhaustive interaction coverage.

On the final app binary, the retained scopes have 53 distinct passing methods
and one wide-device skip: 42 combined regression, three inline, seven catalog,
and one six-style animation method. The animation method uses its recorded
previous test APK; the other final scopes use the final test APK.

## Final visual review and restoration

Nine phone screens were captured and inspected at
`2026-10-07T19:33:38.451834+00:00`, revision `bb7817d`, with the final app APK.
The installed package hash matched the built APK. The ignored local evidence
is under `.local/ui-review/design-update-final-phone`, with a per-screen
manifest and preference recovery record.

The review covers the original system-dark introduction, its light replay,
settings categories, appearance page, About and Google trademark text,
Classic Toast artwork, Expressive Toast artwork, all seven design choices,
and the installed adaptive icon in this app's system information page.
Korean text uses the physical phone's font scale 1.15. The long Expressive
selector label wraps beside its chevron; no isolated arrow line remains.
Normal scrolling can put the next settings row or a preceding sample above
or below a captured viewport. Captures are not claims of interaction passes.

The reader pages used app LIGHT mode while the system stayed dark. The first
introduction uses the original SYSTEM mode; the app information capture is a
system page. Review recovery restored app locale `[]`, appearance SYSTEM,
and onboarding completion false. The final test runs restored animation
scales to their original 1.0 values. The inline wrapper restored all five
original keyboard/autofill settings.

## Coverage limits

The seven-choice matrix contains 1,176 entry/design cells. At API 31+, 583 are
supported: 572 generic rendered cells and 11 dedicated host/animation cells.
The remaining 593 have explicit unsupported explanations. Supported counts are
481 on API 30 and 478 on API 24, including the disclosed Material You API gate.
Canonical source rows and entry/design cells measure different things.

The eight Expressive-only entries call actual library APIs. The library pin is
the first public Expressive generation, not the newest release. The
[design guide](design-families.md) explains the pin, palette and motion choices,
retained source artifacts, AOSP release mappings, and disclosed adaptations.

This update imports 1,009 AOSP control resource variants for 32 component/design
pairs and 22 original Toast variants from six immutable releases. Other platform
widgets explicitly identify the installed OS renderer. Holo and Android 5.0.2
share upstream Toast artwork; Android 12 and 16 share 28dp Toast geometry. The
app preserves those source facts. Current-device interaction engines and
readable app text adjustments are recorded as adaptations.

The SM-X800 tablet remained behind its security lock during this update.
No final-update tablet UI pass is claimed. The earlier tablet results belong
to the earlier APK identified in the previous report. Screenshot review proves
visible layout only, not interactions or original historical OS behavior.
