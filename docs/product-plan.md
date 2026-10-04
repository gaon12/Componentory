# Componentory product plan

Componentory is an Android UI lab. Users choose a component, try real controls,
and compare the design families available on their device. The current direction
uses a physical Android device. PC emulators are optional future tools for exact
historical OS validation, rather than a requirement for using the app.

## What a version means

Keep the design family and the execution environment visible together:

| Family | Source | Meaning |
| --- | --- | --- |
| Classic | Android framework `Theme.Light` | Platform styling available since API 1. |
| Holo | Android framework `Theme.Holo.Light` | Platform styling introduced with Android 3.0 / API 11. |
| Material | Android framework `Theme.Material.Light` | Platform styling introduced with Android 5.0 / API 21. |
| Material 2 | Compose Material library | A separately versioned library implementation. |
| Material 3 | Compose Material 3 library | A separately versioned library implementation. |

The live controls use the selected framework theme or actual library component.
A theme does not switch the OS. For example, Holo on Android 16 is a Holo-themed
framework control running on Android 16. Label it that way. Do not claim it is an
Android 3.0 capture or that its implementation has remained identical since 3.0.
Manufacturer changes can also affect the platform widgets.

The long-term catalog should cover more components, theme variants, library
releases, and Android history. Do not imply that the first supported families
cover every release. Missing components and uncollected captures stay explicit.

## Main experience

Use bottom navigation with **List**, **Compare**, and **Settings**. Open the
component list first. Browsing, single-component exploration, and comparison
have their own screens, rather than sharing one long lab page.

1. Search the list by component name, Android class, or Korean description.
2. Open a component detail page and choose its Android UI family or library.
3. Touch the real control, inspect the feedback, and try disabled state or reset.
4. Use the detail page's comparison action to compare that component and family
   against another family. Each panel has independent interaction state.
5. Switch to Settings to choose app appearance and inspect the device, OS build,
   target SDK, display settings, and exact library versions.

Keep the search and scroll position when returning from a detail page. Preserve
tab state across navigation and Activity recreation. Back from a detail returns
to its list context. Selecting the already active List tab returns to the list.
Keep app appearance separate from the selected samples' light themes.

Use two columns when there is enough width and stack panels on narrow screens.
Keep the two panels' interaction state independent. In comparison, changing a
component or theme starts a fresh sample so previous state does not silently
enter a new experiment.
Explain the selected family's origin without presenting each Android release as
a distinct theme that the device can necessarily provide.

## Component coverage

The first prototype includes Button, Checkbox, Radio buttons, Switch, Text field,
Slider, Progress, and Dialog in all five supported families. Continue expanding
components, theme variants, and historical coverage in focused changes.
Show the actual class or library package alongside the sample. Platform widgets
must use `android.widget` or framework dialogs directly. The Compose shell must
not replace them with an AppCompat or Material library equivalent.

Library samples are separate families with exact dependency version labels.
Only offer actions that the selected component supports. A read-only progress
indicator should not pretend to respond to taps. Unsupported combinations show
an explanation rather than a substitute from another family.

## Evidence and tests

Interaction feedback inside the app describes user actions; it is not an
automated test result. Automated tests send touch input and assert the resulting
state. Test enabled and disabled behavior, independent comparison panels, theme
changes, reset, and component-specific actions. Screenshots provide additional
visual evidence and do not replace behavioral assertions.

Record the following with a verified run:

- App revision and version, target SDK, and component source.
- Selected theme or exact library version.
- Android release, API level, OS build, manufacturer, and model.
- Display size, density, font scale, locale, and orientation.
- Test names, outcomes, and capture availability.

A future Runs feature can store and export this evidence. Until that exists,
development verification records belong in the repository documentation, with
machine-specific outputs outside Git. Do not populate an in-app history with
fixtures or planned samples as if they were completed experiments.

## Implementation milestones

| Milestone | Acceptance evidence |
| --- | --- |
| Live platform comparison | Three explicit platform themes, real Button interaction, responsive panels, real-device tests. |
| Broader component lab | Selection, input, range, and dialog scenarios work in each supported family. |
| Library comparison | Material 2 and Material 3 use their real components with version labels. |
| Catalog navigation | Searchable list, component detail with UI selection, bottom navigation, and a direct comparison action. |
| Saved experiments | Persist real states and environment details; reopen and export runs. |
| Historical expansion | Add verified coverage and optional actual historical OS execution. |

Follow the code, formatter/lint, tests, and focused commit order for each change.
Keep builds sequential and memory use modest. Use the connected physical device;
do not start emulators unless the user later requests them.

## Technical sources

- [Android framework styles](https://developer.android.com/reference/android/R.style)
  document the public platform theme resources and their API introductions.
- [Styles and themes](https://developer.android.com/develop/ui/views/theming/themes)
  explain how themes supply component styling.
- [Views in Compose](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose)
  describe embedding actual platform views in a Compose interface.
- [Compose Material 2 and Material 3](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)
  distinguish the two library implementations.
- [Android Debug Bridge](https://developer.android.com/tools/adb)
  documents wireless device connection and instrumentation execution.
