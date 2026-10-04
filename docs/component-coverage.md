# Component coverage

The broad catalog is still in progress. The committed and verified catalog has
**39 component entries**, with **87 runnable component/family combinations**.
A missing implementation is work to do, not proof that a family does not support it.

## Baseline and completion rule

[The source inventory](component-inventory.csv) records the first audited
baseline: 74 framework UI APIs, 52 Compose Material 2 APIs, and 113 Compose
Material 3 APIs. These are 239 source entries, not 239 different catalog screens.
A radio sample, for example, contains both real RadioButton and RadioGroup APIs.

This baseline uses the public compile SDK 37 API, Material 2 1.10.4 source, and
Material 3 1.4.0 source. Framework controls, visible UI helpers, deprecated
widgets, and layout containers are included. Library rows include experimental
UI components. Constructor overloads share one source row. State factories,
themes, adapters, listener interfaces, abstract base classes, and internal
platform bridges are not standalone catalog components.

The audit must be checked against the public package references before claiming
complete coverage. Add a public UI API if a later source review finds it missing.
AppCompat, Material Views, and Compose Foundation need separate source identities
when those families are added; they must not be silently labelled as platform UI.

The goal is complete only when each source row has a real sample in every family
that provides it, or a verified runtime prerequisite explanation. Common visual
variants and component-specific interaction tests must also be covered.
Historical OS captures and stored experiments remain separate product milestones.

## Status rules

- **Implemented:** The catalog has a real sample for this source. This does not
  mean every overload, style, theme variant, or historical OS has been tested.
- **Pending:** A source exists, but its dedicated sample has not been added or
  verified. Never turn this status into an unsupported label to reduce the list.
- **Unsupported in a selected family:** That exact family does not provide the
  component, or the running OS is below its framework API requirement. Show the
  reason and do not render another family's widget as a substitute.
- **Runtime prerequisite missing:** The API needs a system host, content, or
  setup that is not available. Explain that requirement separately from OS or
  library support.

Deprecated does not mean removed. Check the public API and the actual runtime.
The device's API level determines framework availability; a theme's introduction
date does not change the running OS. Library versions remain pinned and visible.

## Current coverage

Application source revision: `7338696`.

| Source family | Inventory rows | Implemented sources | Pending sources |
| --- | ---: | ---: | ---: |
| Android framework | 74 | 13 | 61 |
| Compose Material 2 1.10.4 | 52 | 17 | 35 |
| Compose Material 3 1.4.0 | 113 | 35 | 78 |
| Total | 239 | 65 | 174 |

The original eight types are Button, Checkbox, Radio buttons, Switch, Text field,
Slider, horizontal Progress, and Alert dialog. Four framework-only additions
provide ToggleButton, ImageButton, RatingBar, and NumberPicker.

Sixteen library action variants add outlined/text/elevated/tonal buttons, icon
buttons and icon toggles, filled/tonal/outlined icon variants, and standard,
extended, small, and large FABs. Material 3-only APIs are unavailable in Material
2. Framework-only widgets are unavailable as dedicated Material library widgets.

Eleven selection variants add Material 2 Chip, assist/filter/input/suggestion
chips and their elevated variants, tri-state checkboxes, and single/multiple
segmented rows. Each segmented row contains real SegmentedButton controls.
The generic Material 2 Chip has no public Material 3 counterpart. Unsupported
families show that absence explicitly.

The list and comparison picker combine category filters with searches by labels,
Korean descriptions, and the source names that actually supply each sample.

## Next implementation groups

1. Outlined and secure input, autocomplete, spinner, and search.
2. Range slider, circular and indeterminate progress, dividers, and badges.
3. Date/time/calendar controls and picker dialogs.
4. Cards, lists, images, text, and legacy content controls.
5. Menus, toolbars, app bars, navigation bars/rails, tabs, and drawers.
6. Sheets, snackbar, tooltip, swipe dismissal, refresh, and carousel.
7. Framework layouts, view switchers, clocks, zoom, media, and system-hosted UI.

Keep available but unimplemented combinations pending until their group is
implemented and tested. Review each group, format/lint, run relevant tests on the
physical device, and commit that coherent change before starting another group.

## Verification

On SM-X800 / Android 16 API 36, the 3 new selection tests and 2 existing action
tests passed in **45.642 seconds**. The first 33-test run then passed all 31
existing tests and failed two new category-filter assertions. After correcting
the assertions, both category-filter tests passed in **5.864 seconds**. This is
evidence from separate runs, not a claim that the first 33-test run passed.
All **6 unit tests** passed. Formatting and Android lint passed with zero errors
and 16 existing warnings.
The source remains a current-device theme/library experiment.

The test environment is Samsung SM-X800, OS build
`BP2A.250605.031.A3.X800XXSBEZH3`, app 1.0 (version code 1), target SDK 37.
The physical display is 1752 x 2800, used in landscape at 2800 x 1752, with
340 dpi, font scale 1.0, and locale ko-KR. The three framework light themes and
both pinned library light themes remain separate samples.

New tests cover native toggle/image/rating touch in all three framework themes,
Classic NumberPicker buttons and Holo/Material wheels, explicit unsupported
library combinations, and all 22 available new library action combinations.
They verify real touch, disabled actions, reset, source identity, and retained
navigation behavior. The broader suite also verifies independent panels,
dialogs, input, sliders, appearance, and Activity recreation.
Selection tests also verify the 13 new supported combinations, tri-state
transitions, and independent segmented-row state after Activity recreation.

The prior 28-test full-suite report for `e6b2eed` is locally saved at
`.local/actions-device-tests.txt`. Selection evidence is saved at
`.local/selection-device-tests.txt`. The initial category-filter failures are
saved at `.local/filter-device-tests-failed.txt`. Later input experiments are
still uncommitted and do not count toward the verified catalog or inventory.
Their first 38-test run passed 36 tests and failed the autocomplete and spinner
popup tests. The updated popup tests await a fresh unlocked-device run. Failed
runs and runs stopped before instrumentation are not passing evidence.

The previous [navigation verification](verification-2026-10-04.md) retains its
original revision and captures. Screenshots from that record do not show all new
components and do not prove new interaction behavior.

## Primary references

- [Framework widget package](https://developer.android.com/reference/android/widget/package-summary)
- [Material 2 API package](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary)
- [Material 3 API package](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)
- [Framework styles](https://developer.android.com/reference/android/R.style)
- [Material design systems in Compose](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)

The installed SDK's public API classes and API introduction data, plus the
pinned libraries' source archives, supplied the source-name audit. Local SDK
paths and generated inspection files stay outside Git.
