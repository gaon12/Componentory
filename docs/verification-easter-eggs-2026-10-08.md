# Settings and Easter egg verification — October 8, 2026

## Delivered changes

- `ac2009e`: compact settings links/version rows, explicit external-browser intents,
  and a license chooser/document modal.
- `9e78262`: verified offline direct contributors with external profiles and graph.
- `8865abb`: complete AOSP-derived game sources, auxiliary components, unchanged
  resources, dependency pins, attribution, and file-level provenance.
- `15dd792`: 26 version-specific catalog rows, full game/extra entry points,
  optional Android integrations, five-language guidance, upstream credits, and
  the packaged game privacy policy.
- `1f48002`: the website renderer recognizes the same optional game privacy section.

Application behavior under test is the code committed in `15dd792`. Device
manifests were collected before that focused commit and therefore accurately
record `8865abb` plus the dirty changes later included in `15dd792`. Later website
and documentation edits do not change the APK. No new release artifact or Play
publication is implied by a successful debug build.

## Ordered local checks

Each implementation completed formatting/lint, relevant tests, diff review, and
a focused English commit. Settings rows and contributors each passed Spotless,
app lint (zero errors, 75 warnings), all 173 existing JVM tests, and both debug
APK builds. Their UI test assertions compiled but did not execute while locked.

The port module initially lacked Okio, dynamic animation, and compatible helper
imports. Compilation failures and 28 initial lint errors were repaired before
passing. Runtime SDK guards, API 31 resource qualifiers, cat adapter positions,
delete targets, and missing storage permission checks were corrected. Scoped
exceptions retain deliberate framework widgets/tints and the guarded legacy tile
overload; API errors were not hidden behind a broad baseline.

The integration test APK initially failed nullable PackageInfo array access. The
arrays were explicitly checked before use, then formatter/lint and tests repeated.
The final catalog API conditions distinguish API 30 cat galleries from API 31
palette pages and widgets. These failed attempts are not counted as passing.

Final Gradle scopes:

```text
spotlessApply spotlessCheck :app:lintDebug :eastereggs:lintDebug
:eastereggs:testDebugUnitTest :app:testDebugUnitTest
:app:assembleDebug :app:assembleDebugAndroidTest
```

The final lint phase completed in 1m 18s (77 tasks: 12 executed, 65 up-to-date).
The final test/build phase completed in 17s (106 tasks: seven executed, 99
up-to-date). All 177 app JVM tests passed. The two port game-model tests were
executed earlier and reused after unchanged game code; they check nonogram marks
and selection/reset behavior plus rotation contracts in all four space engines.
Both APKs built; no UI execution is implied by compilation.

App lint: zero errors, 77 warnings. Port lint: zero errors, 439 warnings and six
hints, mostly retained upstream concerns. Warnings remain visible and are not
reported as a warning-free result. Ruff formatting/lint and the provenance verifier
passed. The verifier checked all 834 imports against the pinned upstream checkout,
including 648 unchanged resource files, component boundaries, and manifest hash.
All initial staged imported blobs also matched their recorded hashes.
Thirty PowerShell evidence-script checks passed, including rejection of the new
interactive catalog test in resource-only mode.

## Physical tablet checks

Device: Samsung SM-X800 (`gts8pwifi`), Android 16 / API 36, build
`BP2A.250605.031.A3`. Fingerprint:

```text
samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZH3:user/release-keys
```

Display: physical 1752 × 2800, logical 2800 × 1752 landscape, rotation 90 degrees,
density 340, font scale 1.0, system locale ko-KR. App: 1.0.0 (code 2), min SDK 24,
target SDK 37. The evidence records the exact library pins and sample theme
configuration; resource-only checks did not render those sample themes.
The tablet remained locked with its screen off. No Activity, gesture, permission
prompt, job scheduling, integration chooser, or screen preparation was performed.
The test runner temporarily selected English and skipped the introduction, then
restored the previous preferences and app locale without restoration errors.
Animation settings stayed at 1.0; there are no new captures.

| Scope | Run ID | Result |
| --- | --- | --- |
| EasterEggResourceTest | 20261008T023236005Z-d7c20c88 | Two passed |
| ComponentInventoryResourceTest | 20261008T023437566Z-6bdeec17 | Two passed |
| IconCatalogResourceTest | 20261008T023514864Z-b8a6d4a1 | Four passed |

The Easter egg scope loaded every one of the 65 imported component classes,
resolved all catalog stages/integrations against packaged metadata, verified
private or permission-protected activity boundaries and service permissions,
and confirmed Startup initialized the shared context. A real bitmap loaded the
nonogram engine; separated row/column clues, player marks, partial checks, and
Android Parcel restoration passed. The other scopes checked the existing 248-row
inventory and pinned framework/Material icon identities under this same binary.
These are model/resource results, not evidence that games were played successfully.

All eight checks used these SHA-256 identities:

```text
app:  b309f15c59f6f80732f8612a20a71a4af98e5d7937743fc90df6b217c482bdc3
test: 2587c319273b66a1885552ee7341cdb20b1a88517edf04f17db9e9466b22620f
```

Private command logs and per-run metadata are in ignored `.local/device-runs/`.
The new settings/modal/contributor and catalog-recreation UI assertions compiled
but remain unexecuted. Full game gestures, multiplayer, feeding delivery,
notification prompts, widget pinning, device controls, dreams, browser handoff,
font-scale/layout checks, and original historical OS behavior remain unverified.
The user asked work to continue without unlocking the tablet; no security lock
was bypassed and no phone result was substituted.

The public privacy renderer also passed Ruff and generated HTML with every
canonical policy paragraph and all six section headings intact.
