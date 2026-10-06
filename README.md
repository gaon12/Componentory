# Componentory

Componentory is an Android UI lab for exploring, interacting with, testing, and
comparing Android design families on a physical device. Platform Classic, Holo,
and Material themes provide live framework controls. Library components are
separate entries with their own version identity.

The working prototype has three bottom navigation destinations:

- **List:** Search 145 implemented components by translated name or description,
  or by their English class/function names. Combine search with category filters.
  Switch to Planned APIs to inspect 16 source APIs awaiting interactive samples,
  with source-name search and provider filters. Open a sample detail page, select
  a theme or library, and interact with the real component.
- **Compare:** Choose two UI families for the same component. Each sample keeps
  its own state. Detail starts Left with its current eligible inputs and Right
  with provider defaults. Copy inputs in either direction to start a fresh target
  sample. Wide screens show two columns; narrow screens stack the samples.
- **Settings:** Choose system, light, or dark app appearance and an app language.
  Inspect the device, OS build, display configuration, target SDK, and libraries.

The catalog includes the original eight basic types, four framework-only controls,
and sixteen Material button/icon/FAB variants, eleven chip and selection
variants, seven additional input types, and an icon browser. Ten more entries add
range sliders, circular and indeterminate progress, dividers, and badges.
Date picker dialogs use the framework and Material 3 suppliers, with separate
confirmed and draft dates. Material 2 has no date picker dialog supplier.
Inline DatePicker uses the framework or Material 3, CalendarView uses the
framework, and DateRangePicker uses Material 3. Inline selections apply
immediately; the library supports an empty date and empty or partial ranges.
CalendarView and the Material 3 pickers explain that global Enabled does not
apply. Framework DatePicker receives its public enabled flag; blocking every
child interaction has not been verified. Narrow previews scroll horizontally,
and the original range calendar has a finite height that grows with font size.
Time picker dialogs also use genuine framework and Material 3 suppliers, with
12/24-hour settings and the library's clock and text input modes. Confirmed times
stay separate from open drafts. Live samples appear before icon and time settings.
Standalone TimePicker uses the framework widget in the three platform themes or
the Material 3 clock; TimeInput uses the Material 3 text-entry control, while
framework families and Material 2 explain their missing suppliers. Inline edits
apply the committed time immediately and store civil minutes rather than a
timestamp. The framework control follows host Enabled; the library controls have
no enabled parameter and say so.
Four framework-only clock entries wrap the original widgets. TextClock follows
the panel's own 12/24-hour switch rather than the system preference. AnalogClock
and DigitalClock stay in the legacy category with their deprecation levels
explained, while Chronometer drives the real timer through framework Start, Stop
and Reset buttons and keeps its running anchor across copying and recreation.
ScrollView and HorizontalScrollView host fixed themed line content inside a
bounded viewport, so real touch scrolling moves the original container while
the panel notes that scroll position is never part of a copied setup.
ViewAnimator, ViewSwitcher, ViewFlipper, TextSwitcher, and ImageSwitcher render
the original framework containers with themed child pages, framework fade
transitions, and themed Previous/Next buttons; the displayed child index is the
copyable state and restores without an animation.
Card and Surface use both libraries' genuine clickable and plain overloads.
Material 3 also provides ElevatedCard and OutlinedCard. Each panel keeps its own
mode and click count; plain containers explain their lack of an enabled state.
Popup menus use the genuine framework or Material library popup and menu items.
They include a disabled choice and keep the last choice and user action separate
from transient open windows. Recreation closes menus while preserving feedback.
Text uses exact framework TextView or each library's actual Text with a fixed,
localized multiline fixture. Library Text explains that Enabled does not apply.
Framework-only CheckedTextView uses an explicitly configured themed check mark;
its named host Checked switch changes the state, while tapping the text does not.
A missing theme drawable is reported. Host configuration remains usable with the
sample disabled, and copying transfers its checked input without inventing taps.
The catalog offers Classic, Holo, and platform Material light themes, Compose
Material 2 **1.10.4**, and Compose Material 3 **1.4.0**. Changing the app
appearance keeps the selected samples in their own light themes.
Material 3 samples explicitly use the pinned library's Typography defaults,
keeping the app's typography out of the sample theme.

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

Unavailable combinations show a reason rather than a substitute. Planned APIs
explicitly show that their samples are not implemented; this does not mean that
the API is unsupported. A first detail visit chooses an available provider, and
later visits remember the component's selected provider, including deliberate
unsupported choices.

Search and live sample state survive tab changes and Activity recreation.
Input copying preserves text, selections, configured values, committed dates and
times, container mode, and compatible icons. It leaves passwords, action history,
menu results, and open drafts out. Unsupported copies explain their reason and
keep the target unchanged. Reset and provider changes cannot replay Detail inputs.
Empty library dates stay empty when copied. A framework date picker requires a
selected date, so copying an empty date to it explains the limitation and keeps
the target. Fresh copied calendars open at the selected input's month without
copying the source's editor mode or browsed month.

The latest checks for `54f8c6f` pass formatting, lint, all 122 executed JVM
tests, and both APK builds. The authored catalog smoke tests cover 356 supported
and 378 unsupported ordinary cells after the baselines were recomputed from the
enum's resolved suppliers; the six native animated cells
use a separate existing test. A 107-test instrumentation run on the Samsung SM-X800 executed 73
passes and 34 failures before the secure keyguard returned and the test process
crashed, so it cannot be described as passing. Nearly every failure traced to a
suite-wide assertion defect: the resolved Compose UI test version defaults
`assertTextContains` to exact equality, while the tests intended substring
matching. That defect plus three unrelated test bugs are repaired in `34ceff1`.
Catalog rendering, comparison state, the Planned view, provider selection,
accessibility, text, inline dates, inline time, the clock, scroll and view
switcher samples, framework layouts, adapter lists, zoom widgets, adapter
animators, transient windows, the deprecated tab host, the deprecated
containers, the popup windows, the menu hosts, the content surfaces, the dialer
filter, the media widgets, the share action provider, the edge effect host, the Material navigation
bars and rails, the Material tab rows and styled variants, the Material snackbars, the Material list items, the Material app bars, the Material navigation drawers, the Material scaffolds, the Material exposed dropdowns, the Material bottom sheets and backdrop, the Material 3 tooltips, the Material swipe-to-dismiss rows, the Material 3 search bars, date/time dialogs, cards,
surfaces, popup menus, input copying, and the preview reorder still require a
clean run on the unlocked device. The
[independent review](docs/review-2026-10-04.md) records the original defect,
repair commits, and remaining UX priorities.
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

The resource-only `IconCatalogResourceTest` and `ComponentInventoryResourceTest`
can run with `-NoUi` while the screen is locked. Select one exact class or one
of its methods explicitly. These checks load catalogs and verify resource data
without an Activity or input. This mode leaves animation settings unchanged.
Other test classes still require the normal unlocked-screen path. Resource checks
do not verify rendering, navigation, touch behavior, or historical appearance.

The catalog smoke class checks current-device rendering, provider identity,
original dialog and menu windows, and explicit unsupported reasons. Its five
tests cover 349 of the 355 component/provider cells. Run
`xyz.gaon.componentory.lab.NativeProgressIndicatorsTest` separately for the six
animated framework cells. It uses UiAutomation with animator scale 1.0; the
ordinary Espresso sweep excludes continuously animated native controls.
These scopes are not complete interaction, pixel, or historical OS tests.

Each executed run has its own `.local/device-runs/<run ID>/` directory with
`instrumentation.txt` and `manifest.json`, outside Git. The manifest records the
Git revision and dirty paths, APK hashes, exact test scope, device build, display
and window state before testing, app locale, source-pinned library versions, and
animation settings. Missing metadata and setting restoration errors are explicit.
The themes listed in the manifest describe sample configuration; they do not
prove that every provider was exercised by the selected tests. Tests that change
their own window or settings still need their individual scenario descriptions.
The latest report is also copied to `.local/device-tests.txt` for convenience.

On Android 13 and later, the host script also saves the original app locale and
user before instrumentation. It restores and verifies them in `finally`, even
when the test runner fails to finish. `.local/device-app-locale.json` retains the
latest recovery data. Older devices still rely on the runner's normal finish;
host recovery of their stored language preference remains pending. Killing the
host process can prevent its cleanup too, so a retained record is recovery data,
not proof that restoration happened.

Run `.\scripts\test-evidence-tests.ps1` to check evidence storage and result
handling with fixtures, and `.\scripts\test-locale-tests.ps1` to check locale
capture and recovery. These checks do not operate the device. The script
requires a successful test summary; an ADB exit code alone is not enough.
Selecting a legacy theme is a current-device experiment. Exact historical OS
appearance and behavior require running on that historical OS.

## License

Componentory is licensed under the [MIT License](LICENSE).
Third-party dependencies retain their own licenses.
