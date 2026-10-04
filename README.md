# Componentory

Componentory is an Android UI lab for exploring, interacting with, testing, and
comparing Android design families on a physical device. Platform Classic, Holo,
and Material themes provide live framework controls. Library components are
separate entries with their own version identity.

The working prototype has three bottom navigation destinations:

- **List:** Search 28 components by name, class/function, or Korean description.
  Open a detail page, select a UI version, and interact with the real component.
- **Compare:** Choose two UI families for the same component. Each sample keeps
  its own state. Wide screens show two columns; narrow screens stack the samples.
- **Settings:** Choose system, light, or dark app appearance. Inspect the actual
  device, OS build, display configuration, target SDK, and library versions.

The catalog includes the original eight basic types, four framework-only controls,
and sixteen Material button/icon/FAB variants. It offers Classic, Holo, and
platform Material light themes,
Compose Material 2 **1.10.4**, and Compose Material 3 **1.4.0**. Changing the app
appearance keeps the selected samples in their own light themes.

Unavailable combinations show a reason rather than a substitute. Components that
are still being implemented are tracked as pending in the coverage inventory.

Search and live sample state survive tab changes and Activity recreation.
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
- [Component coverage](docs/component-coverage.md) records the current 28 passing
  device tests and the broad source inventory that remains to be implemented.

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
