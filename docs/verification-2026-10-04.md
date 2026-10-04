# Physical-device verification: October 4, 2026

The searchable catalog, detail UI selection, bottom navigation, live comparison,
and appearance settings passed verification on a physical Samsung tablet.
These are current-device runs on Android 16. Historical OS captures remain
uncollected, and this prototype is one milestone toward broader coverage.

## Environment

| Field | Recorded value |
| --- | --- |
| App source revision | `59d47af` |
| App version | 1.0 / version code 1 |
| Device | Samsung SM-X800, `gts8pwifi` |
| Android | 16 / API 36 |
| OS build | `BP2A.250605.031.A3.X800XXSBEZH3` |
| Target / minimum SDK | 37 / 24 |
| App display and captures | 2800 × 1752 px, landscape |
| Display density | 340 dpi |
| Font scale | 1.0 |
| Locale | `ko-KR` |
| App appearance | System, light, and dark verified |

OS fingerprint:

```text
samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZH3:user/release-keys
```

## Component sources

| Family | Implementation used in this run |
| --- | --- |
| Classic | Actual framework widgets with `android:Theme.Light` |
| Holo | Actual framework widgets with `android:Theme.Holo.Light` |
| Platform Material | Actual framework widgets with `android:Theme.Material.Light` |
| Material 2 | `androidx.compose.material:material:1.10.4` |
| Material 3 | `androidx.compose.material3:material3:1.4.0` |

All samples use their own light theme. The catalog contains Button, Checkbox,
Radio buttons, Switch, Text field, Slider, Progress, and Dialog. Platform samples
use Android framework classes directly. Library samples use the selected
library's actual composables and display their source and dependency version.

## Checks and outcomes

- Spotless formatting and checks passed.
- Android debug lint passed with zero errors and 16 existing warnings: starter
  resources, a redundant activity label, and dependency update notices.
- Local unit tests passed: two search behavior tests and the starter arithmetic
  test. The arithmetic test is not evidence of UI behavior.
- Debug application and instrumentation APK builds passed.
- All **22 instrumentation tests passed**, with zero failures, in **64.687 s**.

| Test class | Tests | Behavior checked |
| --- | --- | --- |
| [PlatformComparisonTest](../app/src/androidTest/java/xyz/gaon/componentory/lab/PlatformComparisonTest.kt) | 3 | Actual framework button and theme identity, touch, disabled buttons, independent panels, reset, and family changes. |
| [PlatformComponentsTest](../app/src/androidTest/java/xyz/gaon/componentory/lab/PlatformComponentsTest.kt) | 6 | Selection controls, text entry, real enabled and disabled slider drags, native progress bounds in all three families, dialog actions, restored checkbox state, and stacked panels at 360 dp. |
| [LibraryComparisonTest](../app/src/androidTest/java/xyz/gaon/componentory/lab/LibraryComparisonTest.kt) | 5 | Both real Compose libraries, version labels, control interactions, disabled buttons and sliders, dialog actions, and independent mixed-source panels. |
| [CatalogNavigationTest](../app/src/androidTest/java/xyz/gaon/componentory/navigation/CatalogNavigationTest.kt) | 5 | Search and clearing, detail selection across five families, preserved search and sample state, comparison transfer, Back, and compact navigation. |
| [AppearanceSettingsTest](../app/src/androidTest/java/xyz/gaon/componentory/settings/AppearanceSettingsTest.kt) | 2 | Actual light and dark pixels, system bar flags, restored preferences, and preservation of the native sample theme and interaction state. |
| [ExampleInstrumentedTest](../app/src/androidTest/java/xyz/gaon/componentory/ExampleInstrumentedTest.kt) | 1 | Application package identity. |

An additional manual check selected dark appearance, stopped the app process,
started the app again, and confirmed that dark appearance was still selected.
The app preference was returned to System after this check.

The device suite sends real input and checks resulting widget state or Compose
semantics. Native slider gestures send the complete pointer stream before waiting
for Compose to idle, so a held pointer cannot stall the verification helper.

## Captures and local outputs

The following real-device captures were saved and visually reviewed:

- `.local/componentory-list.png`
- `.local/componentory-detail.png`
- `.local/componentory-compare.png`
- `.local/componentory-settings.png`
- `.local/componentory-settings-dark.png`

The final runner output is in `.local/final-device-tests.txt`. Raw outputs,
captures, and APKs are kept outside Git. These screenshots show the current
device UI; interaction outcomes are supported by the tests above.

Only this tablet environment was verified. The 360 dp checks constrain the app
on the same device; they are not runs on a separate phone or historical OS.
Saved experiment history, exports, additional components, and historical
execution remain later milestones.
