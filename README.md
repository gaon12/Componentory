# Componentory

Componentory is an Android UI lab for comparing live framework widgets and
versioned Compose components. The app uses a quiet blue palette, readable text,
and Korean, English, Japanese, Simplified Chinese, and Traditional Chinese.

## Explore and compare

- **Components:** Search 160 entries by translated name, description, or English
  API name. Filter by category and select an Android version/theme or a Compose
  library version. Unsupported combinations explain their actual requirements.
- **Version history:** Browse public SDK snapshots from API 1 through 36. Select
  a class to read its description, availability, source revision, and evidence
  status. Open its current-device sample when a real mapping exists.
- **Compare:** Use two independently themed samples. Copy eligible inputs, reset
  either panel, and save a run with its OS, build, display, and library identity.
- **Runs:** Reopen, delete, or export stored comparisons.
- **Settings:** Choose appearance and language, inspect the environment, and read
  the complete bundled licenses and upstream notices offline.

Phones use bottom navigation. Wide windows use a navigation rail with the catalog
beside the selected component. Settings uses two columns when space permits.
Search, provider choices, and sample state survive tab changes, recreation, and
changes between wide and compact layouts. Search and selection dialogs adapt to
the software keyboard; long names wrap rather than being truncated.

The audited baseline maps all 239 source rows to real samples: 74 framework APIs,
52 Compose Material 2 APIs, and 113 Compose Material 3 APIs. Supporting controls
can share one catalog entry. There is no Planned APIs tab. This baseline does not
claim every Android UI API, overload, interaction, or historical OS is covered.

## Sample identity

| Supplier | Identity |
| --- | --- |
| Android framework | Current OS with Theme.Light, Theme.Holo.Light, or Theme.Material.Light |
| Compose Material 2 | androidx.compose.material:material:1.10.4 |
| Compose Material 3 | androidx.compose.material3:material3:1.4.0 |
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
Native sample labels have a 16 sp minimum for readability. These adjusted samples
run on the installed OS. Selecting Holo does not reproduce Android 3.0's original
implementation. Original appearance and interaction require the corresponding
historical OS. Missing original captures and unverified historical behavior remain
explicit in the history browser and source exports.

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

See [current verification](docs/verification-2026-10-08.md) for executed results
and limitations. Executed results distinguish final device checks from earlier
milestones and retain their APK hashes. Lint passes with zero errors; existing
warnings remain listed in its report.

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
- [Earlier verification](docs/verification-2026-10-04.md)
