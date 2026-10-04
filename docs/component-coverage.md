# Component coverage

The broad catalog is still in progress. The committed catalog implements
**57 component entries**, with **134 runnable component/family combinations**.
Implementation counts are separate from the verification outcomes below.
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

Application source revision: `843377a`.

| Source family | Inventory rows | Implemented sources | Pending sources |
| --- | ---: | ---: | ---: |
| Android framework | 74 | 18 | 56 |
| Compose Material 2 1.10.4 | 52 | 26 | 26 |
| Compose Material 3 1.4.0 | 113 | 46 | 67 |
| Total | 239 | 90 | 149 |

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

Seven input entries add outlined, secure, and outlined secure library fields,
plus framework AutoCompleteTextView, MultiAutoCompleteTextView, Spinner, and
SearchView. Secure samples retain only their character count in saved feedback;
their input clears when the sample is recreated.

The Icon entry uses framework ImageView or each library's actual Icon function.
Its picker indexes all 11,385 public icon getters in the pinned Material icons
1.7.8 core and extended artifacts: 2,277 variants in each of five styles,
including auto-mirrored variants. Framework choices come from every public
`android.R.drawable` field on the current OS. The Samsung runtime exposes
`ic_safety_protection` but cannot load it, so the picker labels it unavailable.
Selections also apply to icon buttons, toggles, FABs, and badge anchors and
survive recreation at the same layout width.

Ten additional entries provide real Material range sliders, determinate circular
progress, horizontal and circular indeterminate progress, horizontal/vertical
dividers, the deprecated Material 3 Divider API, number/dot badges, and badged
icons. Framework ProgressBar supplies both indeterminate styles and determinate
horizontal progress. Its circular styles support indeterminate progress only.
Material 2 has no dedicated vertical divider; the sample does not recreate one.
Badges and dividers explain their read-only appearance. Counter controls change
the number badge from zero to 100; badge anchors use the complete icon picker.

The list and comparison picker combine category filters with translated labels
and descriptions and the source names that actually supply each sample.
English, Korean, Japanese, Simplified Chinese, and Traditional Chinese resources
cover menus, accessibility labels, sample text, and feedback. The Settings picker
persists an explicit language or follows System. Android 13 and later use
LocaleManager; older supported devices use a stored locale context. All language
resources are kept in the app bundle for offline switching.

## Next implementation groups

1. Date/time/calendar controls and picker dialogs.
2. Cards, lists, images, text, and legacy content controls.
3. Menus, toolbars, app bars, navigation bars/rails, tabs, and drawers.
4. Sheets, snackbar, tooltip, swipe dismissal, refresh, and carousel.
5. Framework layouts, view switchers, clocks, zoom, media, and system-hosted UI.

Keep available but unimplemented combinations pending until their group is
implemented and tested. Review each group, format/lint, run relevant tests on the
physical device, and commit that coherent change before starting another group.

## Verification

Commit `dd0df74` packages an exact generated copy of the audited CSV as an app
asset. The small [inventory reader](../app/src/main/java/xyz/gaon/componentory/catalog/ComponentInventory.kt)
preserves source providers, API introductions, statuses, sample IDs, and notes.
Pending API search uses source names and an optional provider filter. Invalid or
empty inventories fail explicitly instead of becoming an empty pending list.
Commit `ffa6e08` adds the Planned APIs list with source search, provider filtering,
explicit loading and failure states, and five-language pending labels. Its cards
are read-only API metadata, not unsupported components or original captures.
Sample coverage remains 57 entries and 134 runnable combinations.

The current source passes formatting, lint with zero errors and 16 existing
warnings, all 28 JVM tests, and both debug APK builds. Five Planned-list tests,
four detail-provider navigation tests, and five comparison-state regressions
compile. That selected 41-test physical UI attempt installed the APKs but
stopped at the locked-screen guard before instrumentation. No tests in that
attempt executed. Planned rendering, navigation, localized UI, and state
restoration therefore still need physical confirmation. Loading-failure injection,
TalkBack, larger fonts, and a separate phone also remain unverified.

Commit `3d44b98` clarifies the theme/library selector and runtime OS note, adds
availability hints without disabling unsupported choices, and exposes selected
menu semantics. Three additional provider-identity tests compile. The latest
44-test physical attempt was also rejected by the locked-screen guard before
instrumentation; none of those selected tests executed. Current formatting, lint,
28 JVM tests and APK builds pass. Source/provider identity is kept separate from
theme introduction dates and actual historical OS execution.

Seven parser checks cover quoted fields, escaped notes, metadata validation,
sample references, filtering, future API metadata, and a completed inventory with
no pending rows. All 21 JVM tests passed at that data milestone. Two resource-only
tests passed on the Samsung SM-X800, Android 16/API 36, in 0.148 seconds. They read
all 239 packaged sources and verified 149 pending entries, provider filtering,
notes, and sample IDs. The source file, APK asset, and installed asset have the same SHA-256:
`c7b23a296bbbc70e3cb25fabe1096b11b133416091516d0118875687d1be26d1`.

The unique local run is `.local/device-runs/20261004T162138822Z-a1f61114/`. Its
manifest records the exact scope, APK hashes, source state, device fingerprint,
target SDK, display configuration, and locale. `asset-consistency.json` retains
the three-way hash comparison. No restoration errors were recorded. These
checks verify inventory data and packaging; rendering, planned-list navigation,
touch behavior, runtime support, and historical OS accuracy remain separate.

The four JVM checks in
[ComponentInventoryTest](../app/src/test/java/xyz/gaon/componentory/catalog/ComponentInventoryTest.kt)
check provider counts and unique source identities, provider and API metadata,
implemented sample mappings, and inventory entries for every declared supported
combination. Remaining Pending rows must not name runnable samples; completing
all pending entries is allowed. These are metadata consistency checks. They do
not independently pin every audited source name, render widgets, test touch
behavior, or verify historical OS execution.

The additions through `843377a` passed the following separate focused runs on
the physical device. These are not a full latest-suite result.

| Source revision | Test scope | Passing tests | Seconds |
| --- | --- | ---: | ---: |
| `c9f07fb` | Range sliders, icons, languages | 8 | 28.498 |
| `1add203` | Progress, libraries, navigation, picker, languages | 17 | 63.452 |
| `3856bad` | Library dividers and explicit unsupported families | 2 | 12.084 |
| `843377a` | Badges, progress regression, icons, languages | 12 | 64.559 |

All 10 unit tests passed, including saved range-state compatibility. Formatting,
lint, and both debug APK builds passed for each addition. Lint retained zero
errors and the same 16 existing warnings. The current debug app bundle was not
rebuilt in these focused runs.

Native indeterminate progress initially stalled instrumentation with animator
duration scale zero on this device. Those interrupted runs are failed evidence,
not passing tests. The native progress test now temporarily uses animator scale
1.0 and closes its samples before restoring the prior test value. The outer
device script restores the user's original animation settings. This passing
condition does not verify disabled-animation behavior or animation appearance.
The reports are `.local/range-icon-language-device-tests.txt`,
`.local/progress-navigation-language-device-tests.txt`,
`.local/divider-device-tests.txt`, and
`.local/badge-icon-language-device-tests.txt`.

An independent review also reproduced comparison state loss when changing
between wide and compact layouts. Same-width recreation passing in the tests
does not cover that defect. See [the review](review-2026-10-04.md).

The latest full suite at `b51546f` passed **all 44 physical-device tests** in
**180.839 seconds**, with zero failures or ignored tests. It includes the icon,
language, native popup, appearance, navigation, and earlier component checks.
The report is `.local/full-device-tests-2026-10-04.txt`. All **8 unit tests**,
formatting, lint, both debug APK builds, and the debug app bundle build also
passed at that earlier revision. Lint had zero errors and 16 existing warnings.
This verifies the tested device and pinned libraries at that revision;
historical OS execution remains unverified.

On SM-X800 / Android 16 API 36, the 3 new selection tests and 2 existing action
tests passed in **45.642 seconds**. The first 33-test run then passed all 31
existing tests and failed two new category-filter assertions. After correcting
the assertions, both category-filter tests passed in **5.864 seconds**. This is
evidence from separate runs, not a claim that the first 33-test run passed.
All **6 unit tests** passed. Formatting and Android lint passed with zero errors
and 16 existing warnings.
The source remains a current-device theme/library experiment.

The later input feature's first 38-test run passed 36 tests and failed two native
popup tests. The input samples were committed as `cf4c040` with that limitation
recorded. After synchronizing Compose scrolling, keyboard closure, and framework
selection feedback, all 3 native input tests passed in **33.987 seconds** at
`4641052`. They touch actual autocomplete and spinner popup items in all three
framework themes and verify search submission, disabled/reset behavior,
recreation, and independent panels. The earlier failed runs remain recorded.

The first later full 44-test run at `4641052` passed 42 tests and failed the
spinner popup and offscreen appearance-option checks. The appearance test now
scrolls to its options; both appearance tests passed in **4.718 seconds** at
`3304799`. The native input tests now inject hardware key events with the
software keyboard hidden, and the device script temporarily disables system
animations. All 3 native input tests passed through that script in
**32.338 seconds** at `b51546f`. Original animation values were restored after
both successful and failed executions. These are widget behavior checks, not
verification of the software keyboard UI or animation appearance.

The 3 icon tests passed on the same device in **12.971 seconds**, covering the
complete indexed getter catalog, available framework drawables, search, style
and mirroring filters, chosen icon rendering, recreation, and a real image-button
tap. The icon feature is committed as `97cd4ff`.

The 3 language tests passed in **16.049 seconds** for `18e563f`. They exercised
all five language selections, persistence across Activity recreation, localized
search, native and Material 2/3 button text and taps, unsupported messages, and
return to System language. All **8 unit tests** passed, including resource parity,
format arguments, and Chinese script handling. Formatting, lint, both debug APK
builds, and the debug app bundle build passed; lint had zero errors and the same
16 existing warnings. The pre-Android-13 locale branch was compiled but has not
yet been tested on an older OS.

The test environment is Samsung SM-X800, OS build
`BP2A.250605.031.A3.X800XXSBEZH3`, app 1.0 (version code 1), target SDK 37.
The physical display is 1752 x 2800, used in landscape at 2800 x 1752, with
340 dpi, font scale 1.0, and system locale ko-KR. The test runner temporarily
uses English for existing behavior assertions and restores the original app
language afterwards. Language tests choose all five app locales explicitly.
The three framework light themes and both pinned library light themes remain
separate samples.
The latest full run temporarily set window animation, transition animation,
and animator duration scales to zero. The native input tests hid the software
keyboard and injected hardware key events before touching real popup items.
The script restored the original animation settings: window 1.0, transition
1.0, and an unset animator duration value. The screen timeout was temporarily
extended during the long development session and restored to its original
300,000 milliseconds. The original System app language was restored as well.

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
saved at `.local/filter-device-tests-failed.txt`. The first input failure report
is `.local/inputs-device-tests-failed.txt`. Passing icon and language reports are
`.local/icon-device-tests.txt` and `.local/language-device-tests.txt`.
The corrected native-input report is `.local/input-synchronized-tests.txt`.
The first full 44-test failure report is `.local/latest-device-tests.txt`.
The input run under explicit test conditions is
`.local/native-input-hardware-tests.txt`.
Failed runs and runs stopped before instrumentation are not passing evidence.

The previous [navigation verification](verification-2026-10-04.md) retains its
original revision and captures. Screenshots from that record do not show all new
components and do not prove new interaction behavior.

## Primary references

- [Framework widget package](https://developer.android.com/reference/android/widget/package-summary)
- [Material 2 API package](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary)
- [Material 3 API package](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)
- [Framework styles](https://developer.android.com/reference/android/R.style)
- [Material design systems in Compose](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)
- [Compose resources and icons](https://developer.android.com/develop/ui/compose/resources)
- [Per-app languages](https://developer.android.com/guide/topics/resources/app-languages)
- [App bundle language configuration](https://developer.android.com/guide/app-bundle/configure-base)
- [Espresso test environment](https://developer.android.com/training/testing/espresso/setup)

The installed SDK's public API classes and API introduction data, plus the
pinned libraries' source archives, supplied the source-name audit. Local SDK
paths and generated inspection files stay outside Git.
