# Componentory

Componentory is an Android UI lab for exploring, interacting with, testing, and
comparing Android design families on a physical device. Platform Classic, Holo,
and Material themes provide live framework controls. Library components are
separate entries with their own version identity.

The working prototype has three bottom navigation destinations:

- **List:** Search 57 implemented components by translated name or description,
  or by their English class/function names. Combine search with category filters.
  Open a detail page, select a UI version, and interact with the real component.
- **Compare:** Choose two UI families for the same component. Each sample keeps
  its own state. Wide screens show two columns; narrow screens stack the samples.
- **Settings:** Choose system, light, or dark app appearance and an app language.
  Inspect the device, OS build, display configuration, target SDK, and libraries.

The catalog includes the original eight basic types, four framework-only controls,
and sixteen Material button/icon/FAB variants, eleven chip and selection
variants, seven additional input types, and an icon browser. Ten more entries add
range sliders, circular and indeterminate progress, dividers, and badges.
It offers Classic, Holo, and platform Material light themes, Compose
Material 2 **1.10.4**, and Compose Material 3 **1.4.0**. Changing the app
appearance keeps the selected samples in their own light themes.

The icon picker searches all **11,385 icon variants** provided by the pinned
Compose Material icons **1.7.8** core and extended artifacts. Filter by Filled,
Outlined, Rounded, Sharp, Two tone, or auto mirroring, and apply the choice to
icons, icon buttons, floating action buttons, or badge anchors. Framework samples
use the current OS's public `android.R.drawable` resources. Resources that the device
cannot load remain visible with an unavailable label.

Menus, descriptions, accessibility labels, feedback, and sample text support
**Korean, English, Japanese, Simplified Chinese, and Traditional Chinese**.
Settings offers native language names and a System option. All translations
are packaged together, so language changes work offline. English API and icon
names stay searchable.

Unavailable combinations show a reason rather than a substitute. Components that
are still being implemented are tracked as pending in the coverage inventory.

Search and live sample state survive tab changes and Activity recreation.
An open comparison currently loses or restores stale values when its width
crosses the two-column layout boundary. The
[independent review](docs/review-2026-10-04.md) records this defect and the next
UX priorities.
The prototype does not yet store or export experiment history, and no original
historical OS captures have been collected. These are later milestones.

## Project decisions

- Use actual framework widgets or the selected library's real components.
- The main experience runs directly on a physical device.
- A design family does not change the device's Android version.
- Platform widgets and UI library components have separate identities.
- Verified results record the environment in which they were produced.
- The long-term catalog covers Android history. Exact historical OS execution
  can be added as a separate verification mode.

## Project documents

- [Product plan](docs/product-plan.md) describes the screens, user journey,
  comparison rules, and acceptance criteria.
- [Development instructions](AGENTS.md) describe the required change, lint,
  format, test, and commit workflow.
- [Physical-device verification](docs/verification-2026-10-04.md) records the
  earlier navigation milestone, environment, and local captures.
- [Component coverage](docs/component-coverage.md) records passing and failed
  verification runs and the source inventory that remains to be implemented.

## Development and verification

The app uses Kotlin and Jetpack Compose, with minimum API 24 and target SDK 37.
Actual framework widgets run inside `AndroidView` with the selected platform
theme. Compose library samples have separate source and exact version labels.

On Windows, set `JAVA_HOME` to JDK 25, then select the connected device:

```powershell
adb devices -l
.\scripts\test-device.ps1 -Device '<device serial>'
```

The script formats and lints first, runs unit tests and builds both APKs, stops the
Gradle daemon, and then installs and tests on the explicitly selected device.
Builds use one worker and a 1 GiB heap. Unlock the device before touch tests.
The script saves system animation settings, disables them during instrumentation,
and restores them on success or failure, following the
[Espresso test setup](https://developer.android.com/training/testing/espresso/setup).
Native input tests inject hardware key events with the software keyboard hidden;
they still touch real widgets and popup items. Keyboard UI and animation
appearance need separate tests.
Set `ANDROID_HOME`, provide an ignored `local.properties`, or pass `-AdbPath`.
Use `-SkipBuild` only when both APKs already match the current source.
Pass `-TestClass 'package.TestClass'` to run a focused class, or
`-TestClass 'package.TestClass#method'` to run one method. Omit it for the full suite.

The instrumentation report is written to `.local/device-tests.txt`, outside Git.
The script requires a successful test summary; an ADB exit code alone is not
enough. Selecting a legacy theme is a current-device experiment. Exact historical
OS appearance and behavior require running on that historical OS.

## License

Componentory is licensed under the [MIT License](LICENSE).
Third-party dependencies retain their own licenses.
