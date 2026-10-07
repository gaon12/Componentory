# Verification: October 8, 2026

The revised app has 160 catalog entries mapped to all 239 audited source rows.
All requested UI changes are implemented. These results verify the recorded
current OS and library versions. Historical OS appearance and full interaction
coverage remain separate work. The user resumed smartphone testing after an
earlier deferral. Final checks and earlier milestones retain separate identities.

Run IDs and manifest timestamps use UTC. Verification spans October 7-8 in
Korea Standard Time.

## Source and binary identity

- Application source: `47f9901`, including stable native media, share-menu, and web preview bounds.
- Final test maintenance: `791748a`, `a3e2cd6`, and `5c9d0fa`.
- App APK SHA-256: `9a991402e838f9c888168ac79ddaec92931aa3ac30b9cf63a922d51b1554209d`.
- Final test APK SHA-256: `41cb5714d3d28375a92c8d8d936090a441b39bfcc3f09ce3df68516e0860ed4a`.
- Build outputs: `app/build/outputs/apk/debug/app-debug.apk` and
  `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

The retained manifests record the parent Git revision and any uncommitted
changes at test time. A focused change is committed after verification, so a
pre-commit run correctly records a dirty source tree. APK hashes identify the
executed binaries; documentation edits do not change them.

## Environment

| Item | Recorded value |
| --- | --- |
| Tablet | Samsung SM-X800, gts8pwifi |
| Installed OS | Android 16, API 36 |
| Build | BP2A.250605.031.A3 |
| Fingerprint | samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZH3:user/release-keys |
| Display | Physical 1752 × 2800; landscape logical 2800 × 1752; 340 dpi |
| Font scale | 1.0 |
| Application target/minimum SDK | 37 / 24 |
| Framework sample themes | Theme.Light, Theme.Holo.Light, Theme.Material.Light |
| Compose Material 2 | 1.10.4; lightColors |
| Compose Material 3 | 1.4.0; lightColorScheme |
| Compose icons | 1.7.8 |
| Inline template helper | AndroidX Autofill 1.3.0; inline UI v1 |

The instrumentation runner uses English and restores the original app locale.
Responsive tests also exercise 320 dp windows and all five supported languages;
those constrained test hosts do not replace a final physical-phone pass.
Appearance tests cover app light mode with a dark resource configuration. Native
samples use explicit light resources and a disclosed 16 sp text floor.

## Executed checks

- Spotless formatting and checks passed before the affected tests.
- Android lint passed with zero errors and 34 existing warnings. The warnings
  include pinned dependency updates and intentional legacy widget usage.
- All 166 JVM tests passed with no failures, errors, or skips.
- Both debug APKs built successfully.
- History tool Ruff lint and format checks passed; 17 Python tests passed.
- Evidence script fixtures passed 26 checks; locale fixtures passed 11 checks.
  These are host-side script checks, not device interaction tests.
- Final documentation text and links were reviewed; relative links resolved,
  external reference pages were reachable, and `git diff --check` passed.

### Tablet scopes

The following tablet scopes passed without skipped tests or restoration errors:

| Run ID | Scope | Passed |
| --- | --- | ---: |
| 20261007T141911483Z-f993acf4 | Classic: all 160 catalog entries | 1 |
| 20261007T142254918Z-af6c9e78 | Holo: all 160 catalog entries | 1 |
| 20261007T142649034Z-81e82297 | Framework Material: all 160 catalog entries | 1 |
| 20261007T144012989Z-35ce9a8e | Material 2: all 160 catalog entries | 1 |
| 20261007T144339011Z-fbe1683e | Material 3: all 160 catalog entries | 1 |
| 20261007T144712426Z-58d8f190 | Six native animated styles and DialerFilter behavior | 5 |
| 20261007T145215719Z-ff770864 | Adaptive layout, selectors, appearance, languages, history, readability, catalog, native ActionBar, inline safety, inventory/notices, and media/web/share bounds | 33 |
| 20261007T150350402Z-2bfcb768 | Real Classic/Holo/Material inline autofill hosts with live surfaces | 3 |
| Total | Distinct final test methods | 46 |

Every scope executed the app APK listed above. Catalog and progress scopes used
test APK `2d865abd001a430212daa0099443053e13805a2169cfe159c276891bf07a2378`.
The combined UI scope used test APK
`b6ec3d6623b25cec4b90a6ea98934537e852bb73bf68b6cdcc92efcd5941373a`
with settings IME guards. The tablet inline scope used test APK
`f4caab9dc7fa95031ef462f139ef640c5cb47631644048d28dd722643a0d924b`
with live-surface waits. The final test-only system-idle/local-visibility
addition passed on the phone. Its extra tablet rerun could not start because
the tablet had locked; the role wrapper restored all five settings. This
additional test version is not claimed as executed on the tablet. The earlier
3-test run, `20261007T145507578Z-ff9b6e3e`, also passed and is not counted twice.
These test-only changes do not alter the application binary.

On API 36, the catalog sweep examines 800 entry/family cells:

| Family | Rendered supported cells | Explained unsupported cells | Dedicated host/animation cells |
| --- | ---: | ---: | ---: |
| Classic | 69 | 88 | 3 |
| Holo | 69 | 87 | 4 |
| Material framework | 69 | 87 | 4 |
| Material 2 | 49 | 111 | 0 |
| Material 3 | 101 | 59 | 0 |
| Total | 357 | 432 | 11 |

The 11 dedicated cells are six continuously animated native ProgressBars, two
Activity-owned ActionBars, and three InlineContentView hosts. The sweep does not
count their launcher buttons as the actual APIs. Separate tests verify their
real classes and behavior. The supported baseline is 368 cells on API 30+ and
365 on API 24; source-row counts and rendered cells measure different things.

The inline tests use the real public request/inflate flow, touch the actual
suggestion to fill only `componentory.demo`, and exercise attachment and surface
order. They temporarily choose the demo keyboard and autofill service. Their
wrapper captures and restores the exact original enabled IMEs, selected IME,
selected subtype, subtype history, and autofill service. A separate safety test
verifies the ordinary provider cannot request the demo field before setup.

The media regression checks positive native bounds and preparation of the real
bundled clip across all three framework themes. It does not claim every media
transport interaction passed. Rendering assertions and screenshots also do not
establish complete interaction coverage for all 160 entries.

### Retained failures

Failed attempts remain in `.local/device-runs/`; they are not counted as passes.
The earlier sweep `20261007T130636758Z-762ca774` had four failures: two zero-height
video samples and two selections made while the keyboard resized the picker.
The media host now reserves space before video dimensions arrive. The picker
checks dismiss the IME, wait for the category row, and verify the selected label.
A later sweep, `20261007T133811731Z-db9b4091`, found a zero-height share
host and was subsequently interrupted with another app in the foreground.
It is recorded as failed, with its recovery completed. A focused regression,
`20261007T135247399Z-68d48305`, confirmed the real ActionMenuView had zero
height. The corrected host supplies its expected 56 dp action-bar height.
The subsequent Holo run, `20261007T140833088Z-3c0af926`, reached WebView and
exposed another zero-height asynchronous preview. Web previews now reserve space before HTML
layout, and release their WebView when the sample leaves the composition.
The first media regression, `20261007T132840595Z-c8ff077c`, stopped because both
comparison panels shared a sample tag; the corrected test scopes the left panel.

Earlier inline attempts exposed a restricted secure-setting read in the test,
a stale pre-keyboard autofill session, and an artificial recreation host mismatch.
The final implementation uses public setup checks, waits for its keyboard, starts
a new autofill session, and tests recreation through the normal app root.

The first final-phone combined run, `20261007T144424396Z-499b06c0`,
retains two settings-test failures and one wide-only skip. A separate notice
run, `20261007T144820628Z-fdf6d574`, passed both tests. The corrected settings
journeys finish catalog IME search, confirm the detail title, and wait for
keyboard insets before opening notice settings. The next combined run passed
with the expected wide-only skip. These are test synchronization changes; the
application binary did not change.

The first phone inline run, `20261007T145736054Z-ade5c37b`, passed Holo and
Material but failed the Classic suggestion-fill assertion. It still restored all
five original secure settings. The surface-only follow-up,
`20261007T151619988Z-4b2ea515`, retained the same Classic fill failure. The next attempt, `20261007T152400127Z-8f73485c`, found a
test coordinate error in all three families: a root-window visible rectangle
was compared against screen coordinates. The corrected test checks the local
visible rectangle against the local center, waits for system accessibility
idle before calculating screen coordinates, and still injects a real touch.
It also waits for a valid public backing surface before reading surface order
and after reattachment. The final phone run, `20261007T153029384Z-479dcb91`,
passed all three families after the app process was restarted. The final
system-idle/visibility rerun on the tablet did not start because it was locked;
its earlier live-surface run passed. The unchanged app APK is identified above.
The live-surface check uses [InlineContentView.getSurfaceControl](https://developer.android.com/reference/android/widget/inline/InlineContentView#getSurfaceControl%28%29)
and preserves the real injected touch and fixed-value assertion.

Other interrupted and failed milestone runs retain their original outcomes
and recovery records. Only the successful scopes above verify their stated behaviors.

## Visual review and cleanup

Reviewed the tablet catalog/detail, history detail, side-by-side comparison,
empty Runs screen, and two-column settings in Korean/light appearance.
The phone review covers its portrait catalog/detail, history description,
stacked comparison panels, empty Runs screen, and single-column settings at
font scale 1.15. Full names and metadata wrap; long bodies remain scrollable.

The local evidence is `.local/final-tablet-captures/manifest.json` and
`.local/final-phone-captures-complete/manifest.json`. Both installed APK hashes
match the app APK listed above. Locale and appearance recovery records match
their original values. The first phone capture attempt stopped before reaching
the lower comparison panel; its recovery completed. The successful review
scrolls beside the native control before selecting the right panel.
The tablet capture helper reported a redundant temporary-file deletion error
after saving its complete manifest and restoring settings; the cleanup is now
idempotent. Neither helper failure is presented as an app test failure.

Screenshots show the current app, not original historical OS captures. The review
compares the installed APK SHA-256 against the built APK before capture. Its
manifest retains OS, display, font scale, theme/library identity, source state,
locale, and each PNG hash. Capture language/appearance are temporary and restored.

The tablet screen timeout was restored to its recorded 600,000 ms value, and
its temporary stay-awake setting was cleared. The original screen-timeout value
is retained in `.local/tablet-original-screen-timeout.txt`. Each device scope
restored its recorded app locale and three animation settings with no errors.
Inline role recovery records preserve all five exact original secure settings,
including subtype strings containing semicolons. The tablet's final inline
recovery is `.local/inline-settings-20261007T150317496Z.json`.
The phone's screen timeout remained 300,000 ms, font scale remained 1.15,
and its existing stay-awake value remained 15. The final phone inline recovery
is `.local/inline-settings-20261007T153006491Z.json`; all five settings matched.
The locked tablet's later role wrapper also restored all five settings in
`.local/inline-settings-20261007T152342798Z.json`. Capture app locale and appearance
were restored, and own temporary device XML/PNG files were removed.
The complete screenshots and raw reports stay outside Git.

## Smartphone verification

The phone is Samsung SM-S731N/r13s on Android 17/API 37, build
CP2A.260605.016, fingerprint
`samsung/r13sksx/r13s:17/CP2A.260605.016/S731NKSU9CZIF_OKR9CZIF:user/release-keys`.
Its physical portrait display is 1080 × 2340 at 450 dpi with font scale **1.15**.
The font scale was retained. These earlier runs verify their earlier recorded
APKs only:

| Run ID | Earlier result |
| --- | --- |
| 20261007T110818196Z-1b330f4c | 15 adaptive/selector/settings tests passed; one wide-only test skipped |
| 20261007T113649917Z-6a3c5d65 | Eight settings/language tests passed after the locale recreation guard |
| 20261007T114443877Z-2b6c4fd9 | Ten history-detail/browser and responsive-selector tests passed |

Final phone scopes execute the app APK listed above:

| Run ID | Scope | Passed | Skipped |
| --- | --- | ---: | ---: |
| 20261007T145415754Z-571cfa76 | Combined UI/native scope, including all five languages, real IME selectors, theme isolation, history, ActionBar, readability, notices, and native media/web/share/progress/DialerFilter | 37 | 1 |
| 20261007T145931876Z-ccff5665 | Classic: all 160 entries | 1 | 0 |
| 20261007T150347004Z-5bb7f3b4 | Holo: all 160 entries | 1 | 0 |
| 20261007T150642399Z-97098848 | Framework Material: all 160 entries | 1 | 0 |
| 20261007T150941085Z-16732569 | Material 2: all 160 entries | 1 | 0 |
| 20261007T151232839Z-4d570bec | Material 3: all 160 entries | 1 | 0 |
| 20261007T153029384Z-479dcb91 | Real Classic/Holo/Material inline hosts | 3 | 0 |
| Total | Distinct final test methods | 45 | 1 |

The skip is `wideSettingsPlacePreferencesBesideDeviceAndSourceInformation`:
this physical portrait window is narrower than the required wide settings layout.
The tablet executes that test. All phone scopes have zero restoration errors.
The combined UI and Classic scopes used test APK
`b6ec3d6623b25cec4b90a6ea98934537e852bb73bf68b6cdcc92efcd5941373a`.
The later catalog scopes used test APK
`f4caab9dc7fa95031ef462f139ef640c5cb47631644048d28dd722643a0d924b`
with live surface waits. The final inline scope used the listed final test APK
with system-idle and local-visibility checks.
All test variants execute the same app binary. The phone catalog sweep verifies
800 cells separately from the tablet sweep, with the same
357 rendered, 432 unsupported, and 11 dedicated-host/animation counts.
It does not create historical OS evidence.

## Historical source evidence and attribution

The public SDK inventory covers API 1 through 36 at immutable commit
`3af7c93524be6f51e092b87b17f009f13ee98b43`, with 3,269 rows and CSV SHA-256
`098cfd378be9f717cb955ae00b937be493134e16838d55446c66d95b7d1f8471`.
The pinned API 35/36 signatures are identical; this is source evidence, not proof
of identical OS UI. API 37 is not fabricated from the current target SDK.

The final local audit rechecked every exported resource and retained notice/license
hash in these source graphs, with no hash errors or missing dependencies:

| Release | Immutable framework commit | Classes | Resources | Internal aliases |
| --- | --- | ---: | ---: | ---: |
| android-2.3.7_r1 | 3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07 | 3 selected | 90 | 0 |
| android-4.4.4_r2 | 63ade05d76785975fc3292ca030abbaa1dda8891 | 97 classified | 2,707 | 0 |
| android-5.0.2_r1 | 0ac9664a9ee2e6d1d3b68fe00c264ad94fe98966 | 4 selected | 1,271 | 2 |

All three exports remain `SOURCE_ANALYSIS`, with original capture `MISSING` and
historical behavior `UNVERIFIED`. No historical OS emulator/device run is claimed.
The inspected release subsets do not finish broad historical coverage.

[NOTICE](../NOTICE), [license provenance](../licenses/provenance.json), and the six
offline settings documents retain upstream attribution. AOSP module declarations,
whole ancestor notices, and original resource headers remain distinct from SDK
repository terms. The AndroidX Autofill license is copied from the pinned AAR
without rewriting its bytes. Its identity is verified by the resource tests.

See [source analysis](android-history.md), [coverage](component-coverage.md), and
[development workflow](../AGENTS.md) for scope and repeatable commands.
