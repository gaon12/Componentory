# Componentory

Componentory is an Android UI lab for comparing live framework widgets and
versioned Compose components, explicit AOSP resource recreations, and clearly
identified Componentory theme demos. The app uses a quiet blue palette, readable text,
and Korean, English, Japanese, Simplified Chinese, and Traditional Chinese.

## Explore and compare

- **Components:** Search 168 UI API entries and 26 version-specific Easter eggs by
  translated name, Android release, nickname, description, or English
  API name. Filter by category and select an Android version/theme or a Compose
  library version. Unavailable samples explain their API or OS requirements
  and distinguish a missing alternative implementation from an unsupported concept.
- **Version history:** Browse public SDK snapshots from API 1 through 36. Select
  a class to read its description, availability, source revision, and evidence
  status. Open its current-device sample when a real mapping exists.
- **Compare:** Use two independently themed samples. Copy eligible inputs, reset
  either panel, and save a run with its OS, build, display, and library identity.
- **Runs:** Reopen, delete, or export stored comparisons.
- **Settings:** Open grouped categories, then dedicated appearance, language, device,
  library, privacy, contributor, or About pages. App and library versions use compact
  rows. Links open an external browser; licenses open an offline document chooser
  and modal. Replay the first-launch introduction from About.

Tap **Settings > App version** seven times to open
[Componentory's own Easter egg screen](docs/componentory-easter-egg.md). Close it
or use Android Back to return to Settings. This initial screen shows the app
artwork, a welcome message, and its version in the selected app theme.
The third through sixth taps show a native Toast counting down four remaining
taps to one. Tap speed does not affect the count.

The settings list also links to the [GitHub repository](https://github.com/gaon12/Componentory)
and [new issue page](https://github.com/gaon12/Componentory/issues/new) for bug
reports and improvement requests.

[Easter eggs](docs/easter-eggs.md) retain complete AOSP-derived mini-games and
hidden screens from Android 2.3 through 17. Release rows stay separate when they
share a source family; Android 8.1 has its own logo entry. Source labels identify
code ports on the current OS, with original historical captures still unavailable.
Optional cat tiles, device controls, palette widgets, screen savers, and game
notifications follow explicit game actions and Android or launcher selection.

Phones use bottom navigation. Wide windows use a navigation rail with the catalog
beside the selected component. Settings keeps its category list beside the
selected page when space permits.
Main tabs start below the safe system inset without a global app-name bar.
Component detail keeps a compact Back/Compare action row.
Search, provider choices, and sample state survive tab changes, recreation, and
changes between wide and compact layouts. Search and selection dialogs adapt to
the software keyboard; long names wrap rather than being truncated.

The audited baseline maps 248 canonical source rows to 168 catalog entries:
74 framework APIs, 52 Compose Material 2 APIs, and 122 Compose Material 3 APIs.
Of these, 247 have direct API implementations and the Toast row is explicitly
Recreated using AOSP artwork in an app popup. Supporting controls can share
one catalog entry. There is no Planned APIs tab. This baseline does not claim
every Android UI API, overload, interaction, or historical OS is covered.

The design progression includes Classic, Holo, Material Design 1, Material
Design 2, Material Design 3, Material You, and Material 3 Expressive. See
[design families and source evidence](docs/design-families.md) for actual
suppliers, resource bindings, experimental pins, and remaining capture gaps.

## Sample identity

| Supplier | Identity |
| --- | --- |
| Android framework | Current OS engine with explicit light themes; selected Classic/Holo/Material Design 1 AOSP artwork |
| Compose Material 2 | androidx.compose.material:material:1.10.4 |
| Compose Material 3 / Material You | androidx.compose.material3:material3:1.5.0-alpha01; standard or Android 12+ dynamic colors |
| Material 3 Expressive | Same experimental pin; real MaterialExpressiveTheme and Expressive APIs |
| Compose Material icons | 1.7.8; 11,385 searchable variants |
| Inline template helper | androidx.autofill:autofill:1.3.0; inline UI v1 |

The platform ActionBar runs in a native Activity with Holo or Material. Theme.Light
does not provide it. Its actual menu selections and visibility return to the lab.
InlineContentView requires API 30 and an inline autofill session. Its sample has
an isolated keyboard and autofill service that supply only `componentory.demo` in
its own demo field. Enable these through the system screens shown in the sample.
Return to your usual autofill service afterward. The sample returns to the previous
keyboard when it finishes. Android creates the actual InlineContentView through
InlineSuggestion.inflate; the AndroidX helper supplies the suggestion template.

Samples retain their own light configuration when the system or app is dark.
Native sample labels have a 16sp minimum for readability. Selected controls
use 1,042 imported AOSP resource variants for 35 component/design pairs,
including explicit historical analog-clock dials and hands.
The **Partial AOSP artwork recreation** badge identifies these selected graphics.
Unbound layouts, popup content, sizing, and interaction still use the installed
OS. Other framework samples carry a current-OS badge. Original historical
captures are missing. The
[resource follow-up](docs/component-resource-audit-2026-10-08.md) records concrete
control, popup, picker, list, and icon paths and the remaining imported-artwork scope.

Modern text, digital, and analog clocks and chronometers use the selected
Material 2/3 theme with Componentory drawing and text. Their badge and source
identify them as theme demos, without inventing dedicated Material clock APIs.
Eight experimental APIs and ToggleButton work under all three Material 3
themes supplied by the same pinned artifact. The
[component design audit](docs/component-design-audit-2026-10-08.md) records
these fixes and the remaining historical and alternative-sample gaps.

Toasts use six pinned AOSP releases and 22 original resource variants in a
separately labeled custom preview and popup. They do not call the installed
OS Toast renderer or claim a Compose Toast API. Modern artwork includes the
app icon and two-line limit; colors follow the selected library or dynamic
palette. Repeated taps reset the timeout, and the popup does not block touches.

## Build and verify

The app uses Kotlin, Compose, minimum API 24, and target SDK 37. On Windows, set
`JAVA_HOME` to JDK 25 and configure the Android SDK through `ANDROID_HOME`,
`ANDROID_SDK_ROOT`, or an ignored `local.properties`.

```powershell
.\gradlew.bat spotlessApply spotlessCheck :app:lintDebug --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process'
.\scripts\test-device.ps1 -Device '<device serial>' -AdbPath '<adb path>' -SkipBuild -TestClass 'xyz.gaon.componentory.catalog.CatalogRenderingSmokeTest'
```

Use `-SkipBuild` only with matching APKs. Keep the selected device unlocked for
UI tests. The device script retains APK hashes, source state, exact scope, OS
build, display, theme configuration, and outcomes in `.local/device-runs/`.
It restores animation settings and, on API 33+, the original app locale in a
finally block. Stored recovery records and restoration errors remain explicit.
Resource-only tests can use `-NoUi` with the reviewed class whitelist.

The catalog sweep excludes continuously animated controls and Activity/IME hosts.
Use NativeProgressIndicatorsTest and the ActionBar tests for those actual widgets.
Run the real inline host tests through the dedicated script:

```powershell
.\scripts\test-inline-device.ps1 -Device '<device serial>' -AdbPath '<adb path>' -SkipBuild
```

This script temporarily selects the demo keyboard and autofill service. It records
and restores the original keyboard, enabled methods, selected subtype, subtype
history, and autofill service, including IME strings containing semicolons.
Its tests touch the actual suggestion, fill the fixed value, and exercise the
public host's attachment and surface order. Other apps' input is never handled
by the demo keyboard or inspected by the demo autofill service.

See the [current resource and header verification](docs/component-resource-audit-2026-10-08.md#ordered-verification)
for executed resource checks and the deferred locked-tablet UI scope. The
[clock and API verification](docs/verification-component-design-audit-2026-10-08.md)
records the preceding fixes and their exact tested binaries. The
[release verification](docs/verification-release-2026-10-08.md) records the
separate published 1.0.0 artifact and listing checks. Executed results distinguish
final device checks from earlier milestones and retain their APK hashes. Lint
passes with zero errors; existing warnings remain listed in its report.

## Release and privacy

Version 1.0.0 (code 2) has prepared Google Play listings in five languages,
the supplied app icon, localized feature graphics, and genuine phone screenshots.
See the [release guide](docs/google-play-release.md) for current image and text
limits, signing, account requirements, app-content declarations, and submission
steps. Prepared materials and a GitHub release do not establish Play publication.

The app works offline without an account, advertising, analytics, or Internet
permission. Settings > Privacy policy contains the full offline document.
Preferences, comparison runs, and game progress stay in the app sandbox; automatic
backup is disabled. Optional game notifications and Android integrations are
described in the policy. Saving a generated cat can write a gallery image; sharing
opens Android's share sheet only at the user's request.
Read the [published privacy policy](https://gaon12.github.io/Componentory/privacy.html)
or its [canonical source](docs/privacy-policy.txt).

The [project website](https://gaon12.github.io/Componentory/) and privacy page
are generated from repository sources and published through GitHub Pages.
The [1.0.0 release](https://github.com/gaon12/Componentory/releases/tag/v1.0.0)
provides signed APK/AAB files, a store-material ZIP, and SHA-256 checksums.
Those published release artifacts and screenshots predate the later settings and
Easter egg changes; the current debug APK is a separate tested build.
An upload-signed APK uses a different certificate from local debug builds and may
also differ from Play-delivered APKs. Keep user data when changing build channels.

## Sources and licenses

[Historical source analysis](docs/android-history.md) combines pinned public SDK
signatures with AOSP Java classes and recursive resource graphs. It preserves
XML, style inheritance, qualifiers, and nine-patch bytes with source hashes.
Exports remain source evidence rather than original OS captures.

Componentory's code uses [MIT](LICENSE). [NOTICE](NOTICE) explains upstream
attribution. Bundled AOSP, SDK, and AndroidX license documents retain their original
bytes and audited provenance. SDK repository terms remain separate from Apache 2.0.
The settings screen includes the full offline documents. Source exports retain
release-specific licenses, notices, and resource headers.

- [Audited source inventory](docs/component-inventory.csv)
- [Coverage and retained milestones](docs/component-coverage.md)
- [Development workflow](AGENTS.md)
- [Product plan](docs/product-plan.md)
- [Settings and Easter egg verification](docs/verification-easter-eggs-2026-10-08.md)
- [Earlier UI verification](docs/verification-2026-10-08.md)
- [Earlier verification](docs/verification-2026-10-04.md)

Android is a trademark of Google LLC. See the [official Android brand notice](https://developer.android.com/legal).
