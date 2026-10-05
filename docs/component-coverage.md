# Component coverage

The broad catalog is still in progress. The committed catalog implements
**82 component entries**, with **207 runnable component/family combinations**.
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
- **Pending:** A source exists, but its sample has not been implemented.
  Verification is recorded separately. Never turn this status into an unsupported
  label to reduce the list.
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

Application and latest verification source revision: `96d861b`. Executed resource
runs keep their original revision and dirty paths; the earlier text inventory
result is recorded under its separate milestone below.

| Source family | Inventory rows | Implemented sources | Pending sources |
| --- | ---: | ---: | ---: |
| Android framework | 74 | 37 | 37 |
| Compose Material 2 1.10.4 | 52 | 31 | 21 |
| Compose Material 3 1.4.0 | 113 | 59 | 54 |
| Total | 239 | 127 | 112 |

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

Date picker dialogs add four combinations: the three framework themes use
`android.app.DatePickerDialog`, and Material 3 uses its real DatePickerDialog
with DatePicker content. Material 2 does not supply these APIs. Dialog dates use
midnight UTC, start at January 15, 2024, and distinguish drafts from confirmed values.

Three inline entries add eight combinations: DatePicker uses the three framework
themes or Material 3, CalendarView uses the three framework themes, and
DateRangePicker uses Material 3. All three are unsupported in Material 2.
The existing canonical Material 3 DatePicker source row names both the inline
sample and its supporting use inside DatePickerDialog. It remains one source API;
only framework DatePicker, framework CalendarView and Material 3 DateRangePicker
increase the implemented-source count.

Inline selections update immediately. Single library dates can be empty, and
range state can be empty, start-only or complete, including a same-day range.
The sixteen-field sample save format keeps those nullable values and the
library's editor mode and browsed month while accepting older saves.
Framework DatePicker receives its public enabled flag; that flag alone is not
proof that every child gesture stops. CalendarView and both Material 3 pickers
show that global Enabled does not apply. Their original interactions remain
available without a synthetic blocker or replacement selection policy.

Selected civil dates use UTC storage and UTC localized feedback. CalendarView's
local timestamp API receives the same civil date at local noon, avoiding an
ordinary midnight daylight-saving gap or a previous-day shift from UTC midnight.
Empty and partial inputs remain eligible when copied. An empty single date cannot
be represented by a framework picker, so that copy explains its reason and leaves
the target unchanged. A fresh copied library calendar derives its opening month
from the eligible selected date or range start; source editor modes and browsed
months are excluded.

Host viewports retain readable original widths with horizontal scrolling.
The range picker's finite height grows with font scale. Its original Crossfade
can retain the calendar list while showing input, so a finite maximum bounds that
list without forcing the input editor to fill an unnecessarily tall viewport.
These layout decisions are implemented source, not physical usability evidence.

Time picker dialogs add four combinations using the exact framework
`android.app.TimePickerDialog` or Material 3 TimePickerDialog. Material 2
has no dedicated supplier. Each panel starts at 10:30 and keeps committed time,
open drafts, and the 12/24-hour setting separate. Material 3 retains its input
mode across recreation and uses text input when the host window is at most the
library's 300 dp height breakpoint. The explanatory note stays outside the modal.

Two standalone entries add five combinations through the same three audited
sources: inline TimePicker uses framework `android.widget.TimePicker` in the
three platform themes or Material 3 `TimePicker`, and TimeInput uses Material 3
`TimeInput` alone. The framework families and Material 2 report explicit
unsupported reasons for the missing inline input. The native widget is built
inside the selected theme with only public `setIs24HourView`, `hour`, `minute`
and `OnTimeChangedListener` APIs. Material 3 rebuilds its original control when
the format flag changes because `is24Hour` is an initialization argument, and
keeps the library's lack of an enabled parameter visible rather than inventing
one. Inline edits apply immediately as civil minutes; copying transfers the
selected time and format only, never dialog drafts, input modes, or action
history.

Four standalone clock entries add twelve combinations through the three
platform themes only; both Material libraries report explicit unsupported
reasons. TextClock (`android.widget.TextClock`, API 17) renders the live device
time and pins both of its format fields to the panel's own 12/24-hour switch so
the display does not silently follow the system preference; copying transfers
that format choice only. AnalogClock and DigitalClock keep their original
framework widgets in the legacy category and label their API 23 and API 17
deprecations; they own no inputs, so copying reports no inputs. Chronometer
drives the real `android.widget.Chronometer` with framework Start, Stop and
Reset buttons, stores a wall-clock anchor that doubles as the frozen elapsed
value while stopped, and resumes a copied or recreated running timer at the
same offset. Older saved panels without the anchor default to a stopped
zero state.

Two scroll container entries add six combinations through the platform themes
only. ScrollView (`android.widget.ScrollView`, API 1) holds a fixed column of
themed text lines inside a bounded 240 dp height so the original container
clips and scrolls vertically; HorizontalScrollView
(`android.widget.HorizontalScrollView`, API 3) holds a row of fixed-width lines
that overflows the panel for sideways scrolling. Both libraries report explicit
unsupported reasons. The samples own no inputs — scroll position is ephemeral
touch state, so copying reports no inputs rather than inventing a saved offset.

Five view-switcher entries add fifteen combinations through the platform themes
only. ViewAnimator (`android.widget.ViewAnimator`, API 1) and ViewFlipper
(`android.widget.ViewFlipper`, API 1) each hold four themed text pages;
ViewSwitcher (`android.widget.ViewSwitcher`, API 1) holds two. TextSwitcher
(`android.widget.TextSwitcher`, API 1) cycles four localized lines through its
two factory children, and ImageSwitcher (`android.widget.ImageSwitcher`, API 1)
alternates two AOSP star drawables. Widgets constructed in code ship no default
transition, so the samples load the framework `fade_in`/`fade_out` resources.
Themed Previous and Next buttons step each container with its own
showNext/showPrevious, setText or setImageResource call. The displayed child
index is the copyable state and restores without an animation; timed
auto-flipping is not enabled on the ViewFlipper sample.

Four container entries add six library combinations: Card and Surface in both
Material libraries, plus Material 3 ElevatedCard and OutlinedCard. Each uses the
real clickable or plain overload with default shape, color, border and elevation.
The host switch follows the preview and keeps its mode per panel. Clickable
containers count taps; plain overloads have no click or enabled parameter, and
the panel explains that the global Enabled setting applies to clickable samples.
Old saved panels default to clickable mode. Framework providers and Material 2
card variants without a supplier show an explicit unsupported reason.

Popup menu adds five combinations. Framework themes use the exact
`android.widget.PopupMenu` with the themed button as its anchor. Both Material
libraries use their actual DropdownMenu and DropdownMenuItem functions, keeping
original popup and row defaults. Two active choices and a disabled choice expose
real item behavior. Selection closes this sample menu; the Compose caller
explicitly closes it after a choice. Back and outside dismissal retain the last
choice. Pause, disable and preview disposal close transient windows without
inventing a user dismissal. Last choice and last action survive recreation;
open windows do not. Supporting item rows are counted inside this menu, with no
standalone item, icon, submenu or exposed-menu coverage claim. Exposed dropdowns,
ListPopupWindow and PopupWindow remain planned.

Text and CheckedTextView add eight combinations. Text uses exact framework
`android.widget.TextView` in all three themes or the pinned libraries' real Text
functions. The caller supplies a fixed, localized three-line fixture without
explicit text-style arguments. Library Text has no enabled parameter, so its
note explains that global Enabled does not apply. Framework TextView receives
its public enabled flag without adding a click action.

The Material 3 sample theme explicitly supplies a cached pinned `Typography()`.
This prevents the caller's app typography from entering the sample. The previous
host bodyLarge used FontFamily.Default while the pinned default uses SansSerif.
Its numeric size, line height, letter spacing and weight matched; default
LocalTextStyle merging could preserve the library's paragraph/platform fields.
This was a source isolation defect, not an observed spacing or pixel defect.

CheckedTextView uses the exact framework class in all three themes; neither
Material library provides a dedicated supplier. The sample explicitly assigns
the theme's public `listChoiceIndicatorMultiple` drawable when available, rather
than claiming that a bare constructor supplies that mark. A missing drawable
has its own note. The named host Checked switch changes the real checked value;
tapping the original text does not toggle it. The host setting remains usable
when the sample is disabled. Both behavior notes follow feedback before source
metadata, keeping the limitations beside the sample and its configuration.

No save fields were added: the existing sixteen-field format retains checked
input as zero or one. Copying transfers that actual input only between supported
framework samples, including an unchecked value. Fixed Text has no per-panel
input to copy, so the copy action explains its no-input reason. Four canonical
rows become implemented: TextView, CheckedTextView, Material 2 Text and Material
3 Text. The baseline remains 239 sources.

The shared panel shows the real sample before host adjustments and icon selection.
Feedback precedes icon source metadata, while provider identity and behavioral
guidance remain visible. Compare shows the component name in its picker once,
with Enabled, Copy inputs and Reset in wrapping host controls. Detail can seed
Left with eligible inputs while Right starts at its defaults. Directional copying
starts a fresh target with independent inputs and neutral interaction history.
Exact icon compatibility is required, and unsupported copies keep the target
unchanged. These changes shorten the path to the sample; narrow screens still
stack full panels.

The list and comparison picker combine category filters with translated labels
and descriptions and the source names that actually supply each sample.
English, Korean, Japanese, Simplified Chinese, and Traditional Chinese resources
cover menus, accessibility labels, sample text, and feedback. The Settings picker
persists an explicit language or follows System. Android 13 and later use
LocaleManager; older supported devices use a stored locale context. All language
resources are kept in the app bundle for offline switching.

## Next implementation groups

1. Remaining pickers and date/time variants.
2. Lists, images, remaining text variants, and legacy content controls.
3. Remaining menu variants, toolbars, app bars, navigation bars/rails, tabs, and drawers.
4. Sheets, snackbar, tooltip, swipe dismissal, refresh, and carousel.
5. Framework layouts, zoom, media, and system-hosted UI.

Keep available but unimplemented combinations pending. Review each group,
format/lint, and run relevant tests before a focused commit. Attempt physical UI
checks and record any checks that cannot run; compilation and resource checks
must not be described as interaction passes.

## Verification

Latest implementation: `6290f59`, preceded by the sweep baseline correction in
`96d861b` and the instrumentation repair in `34ceff1`. Spotless and lint pass
with zero errors. All 91 JVM tests actually executed and passed without
failures, errors or skips. Both debug APKs build: the app APK is 19,715,431
bytes with SHA-256
`7387b6be0e2e03c33045fe441e6da16084ce5d87676a9388d3cc109254b087ff` and the test
APK is 1,466,191 bytes with SHA-256
`1c01596f51b1feea0a7718016e88bed557d7775967de56667dd96a7fd781d17a`.

Six [inline-time UI scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/InlineTimeSamplesTest.kt)
compile. They cover the real framework `TimePicker` and Material 3
`TimePicker`/`TimeInput` suppliers, the configured 10:30 24-hour start,
12/24-hour switching, Material 3's absent enabled parameter, civil-minute state,
copying without shared editors, recreation, five locales, and bounded host
widths. They are authored assertions awaiting a clean device run.

Four [clock UI scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/ClockSamplesTest.kt)
compile in the same state. They cover the real `TextClock`, `AnalogClock`,
`DigitalClock` and `Chronometer` classes inside each platform theme, the pinned
format fields, chronometer start/stop/reset behavior including a frozen stopped
display and a running anchor across recreation, format-only and running-state
copies, unsupported Compose targets, and localized labels, notes and buttons in
five locales. These are likewise authored assertions awaiting a clean device
run on the unlocked hardware.

Four [scroll container scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/ScrollContainersTest.kt)
compile in the same state. They verify the real `ScrollView` and
`HorizontalScrollView` classes clip overflowing themed content, move under
actual Compose touch swipes, expose no copyable inputs, reject Compose library
targets, and keep localized labels, notes and line text in five locales. They
also await a clean unlocked-device run.

Five [view-switcher scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/ViewSwitchersTest.kt)
compile in the same state. They verify the real `ViewAnimator`, `ViewSwitcher`,
`ViewFlipper`, `TextSwitcher` and `ImageSwitcher` classes inside platform themes,
Previous/Next stepping with wrap-around, localized lines and drawables, disabled
controls, index copying, recreation restore, independent panels and five
locales. They also await a clean unlocked-device run.

The catalog now declares 82 entries and 207 supported combinations. The smoke
sweep spans 404 cells: 202 supported and 202 unsupported ordinary cells; the
six native animated cells use the separate UiAutomation scope. The sweep
baselines were recomputed from the enum in `96d861b` after a hand count
under-counted platform cells by two and Material 3 cells by one at `cd82cc6`;
the earlier numbers were never executed on a device, so no run is contradicted.

A device instrumentation attempt ran `.local/device-runs/20261005T103758416Z-abf2e279/`
on the Samsung SM-X800 (Android 16, API 36, build `BP2A.250605.031.A3`,
1752×2800 at 340 dpi, font scale 1.0). 107 of the 134 selected tests started;
the stream recorded 73 pass and 34 failure results before the secure keyguard
returned mid-run and the test process crashed during
`fiveLanguagesLocalizeProviderGuidanceBeforeTheSampleWhileKeepingApiIdentity`.
The attempt cannot be described as passing. Thirty-one failures traced to a
suite-wide assertion defect: the resolved Compose UI test version defaults
`assertTextContains` to exact equality while the tests intended substring
matching, and the catalog smoke cells wrapped the same defect. The remaining
three were independent test bugs — an unsupported right provider in the
date-range copy test, a displayed-text matcher for the spinner day field, and a
stale accessibility node in the animated progress sweep. `34ceff1` repairs all
of them, so the failed run is evidence of test defects rather than product
regressions; the repaired suite, including the six new inline-time scenarios,
still requires a clean run on the unlocked device. The resource-only
`packagedInventoryRetainsAuditedSourcesAndStatuses` test did pass in that run,
verifying the new `TIME_PICKER` and `TIME_INPUT` catalog identifiers in the
packaged inventory.

### Earlier verification milestones

Typography isolation milestone: `a0cc043`. Spotless and lint passed in 1 minute
29 seconds with zero errors and 16 existing warnings. All 78 JVM tests actually
executed again and passed without failures, errors or skips; result timestamps
are October 4, 2026 at 22:39:30 UTC. Both debug APKs built in 31 seconds and
installed. Gradle reported one daemon stopped afterward. The logs are
`.local/typography-isolation-format-lint.txt`,
`.local/typography-isolation-tests-build.txt`,
`.local/typography-isolation-unit-results.json` and
`.local/typography-isolation-gradle-stop.txt`.

TextSamplesTest now has five compiled methods. The added regression runs the
production Material 3 Text renderer under a caller with Serif, 42 sp text and
64 sp line height. Its original TextLayoutResult is checked against the pinned
library typography, including font family, metrics and paragraph/platform
fields. This is an authored rendering regression awaiting device execution.

The catalog then declared 69 entries and 169 supported combinations. The
ordinary smoke scope remained 163 supported and 176 unsupported cells, or 339 of
the 345 cells; six native animated cells use the separate existing test. The
latest normal 98-test scope across 21 classes installed both APKs but stopped at
the secure-keyguard guard before instrumentation. None executed. The separate
native animation scope was not attempted while the same lock remained active.
Attempt, raw keyguard state and declared scope are retained in
`.local/typography-isolation-ui-verification-attempt.txt`,
`.local/typography-isolation-keyguard-state.txt` and
`.local/typography-isolation-declared-test-scope.json`.

The milestone app APK is 19,676,899 bytes with SHA-256:
`2c66d2ecd2ce7c09105d112fa0b8b8b9770d2671124d7a325d4ccf942958bf36`.
The milestone test APK is 1,391,905 bytes with SHA-256:
`1f55c9d065dcc722ae77c9316c7d23106f7f747f1db2853b784a5b0e8b65a9e9`.
Identities are retained in `.local/typography-isolation-apk-hashes.json`.
`.local/typography-isolation-inventory-identity.json` confirms that the latest
packaged 21,506-byte inventory matches the unchanged source and the earlier
runtime digest below. No resource tests reran for this fix. The older
two-test resource result and its hashes belong to the text milestone below;
they are not a test of these latest binaries. No new renderer, interaction,
capture or historical OS pass is claimed.

Text milestone: `96dea3f`. Spotless and lint passed with zero errors and
16 existing warnings in 2 minutes 45 seconds. All 78 JVM tests actually executed
and passed with no failures, errors or skips. Five new methods cover actual
supplier availability, source search, canonical rows and the distinction between
copyable checked input and fixed read-only text. Both debug APKs built in 1 minute
3 seconds and installed. The focused host build used one worker and a 768 MiB
heap; no emulator was started. Gradle reported one daemon stopped afterward.
Logs are `.local/text-samples-format-lint.txt`,
`.local/text-samples-tests-build.txt`, `.local/text-samples-unit-results.json`
and `.local/text-samples-gradle-stop.txt`.

Four [text UI scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/TextSamplesTest.kt)
compile. Their assertions check all five Text suppliers, exact native classes
and limited constructor-default metrics, original library text layout and
read-only semantics, host-controlled checked values, copy/reset/recreation,
unsupported-library recovery and five literal language fixtures. Actual pointer
actions distinguish non-toggling original text from the named host switch.
Conditional missing-mark assertions follow the actual theme result; they do not
prove that a missing-drawable theme was exercised. A bounded 360 dp/font-scale-2
host checks Compose text and host reachability. It does not resize the physical
OS window, change native widget font configuration or verify TalkBack speech.

The updated catalog sweep spans 345 cells. Its five ordinary tests declare
163 supported and 176 unsupported cells, or 339 cells; six native animated cells
remain in the separate existing one-method UiAutomation scope. Each framework
theme has 24 ordinary supported and 43 unsupported cells; Material 2 has 33 and
36, and Material 3 has 58 and 11. The normal 97-test scope across 21 classes
installed both APKs but stopped at `test-device.ps1:76`, the secure-keyguard
guard before instrumentation. None of the selected UI tests executed. The
separate animation scope was not attempted while the lock remained active.
The attempt file `.local/text-samples-ui-verification-attempt.txt` includes a
host-recorded terminal error, not an instrumentation result. Fresh keyguard
state is retained in `.local/text-samples-keyguard-state.txt`, and declared scope
is `.local/text-samples-declared-test-scope.json`.

Two inventory-only device tests passed in 0.185 seconds with native exit 0,
`OK (2 tests)` and no restoration errors. They verify the 239-source inventory,
116 implemented and 123 pending rows, canonical mappings and metadata. The
executed run is `.local/device-runs/20261004T222237952Z-c08a1caa/`. Its original
manifest records `7c8d42e` plus dirty changes later committed as `96dea3f`;
it must not be relabelled as a clean run of that later commit. It records the
SM-X800, Android 16/API 36 Build.ID `BP2A.250605.031.A3`, the full Samsung
fingerprint, target SDK 37, 340 dpi, font scale 1.0, System app locale `[]`,
display/window snapshots, pinned libraries and configured light sample themes.
No Activity or UI input ran, and configured themes do not prove rendering.

Source CSV, packaged APK asset and runtime targetContext asset have matching
21,506-byte content with SHA-256:
`f5102d83b9271fc899913ee35e0ea6411a97e60b2554584f9097956e6f4fcd88`.
The runtime digest was emitted by ComponentInventoryResourceTest into
`instrumentation.txt`; no installed APK was pulled. `asset-consistency.json`
retains that evidence distinction. The app APK is 19,676,899 bytes with SHA-256:
`0504cb581f486aa0fcdd886bac19f830a7422793ca9039a38fe7e5090cbcdf0c`.
The test APK is 1,387,815 bytes with SHA-256:
`de244c24340cbeea41cfad70e9242b81d2d13f823befb0ffc58b82e4cae9143d`.
These APK identities are retained in `.local/text-samples-apk-hashes.json`.
Resource checks supply no renderer, interaction, screenshot, stored experiment
or historical OS pass. All new UI assertions and earlier repairs still require
unlocked physical verification.

Inline-date milestone: `f39c2cd`. Spotless and lint passed with zero errors and
16 existing warnings. All 73 JVM tests actually executed and passed, including
eleven new checks for genuine suppliers, old save compatibility, nullable
selections, civil dates across time zones and input-copy presence. Both debug
APKs built and installed, and Gradle was stopped after the builds. Host logs are
`.local/inline-dates-format-lint.txt` (2 minutes 26 seconds),
`.local/inline-dates-tests-build.txt` (1 minute 27 seconds) and
`.local/inline-dates-unit-results.json`.

Six [inline-date UI scenarios](../app/src/androidTest/java/xyz/gaon/componentory/lab/InlineDatePickersTest.kt)
compile. They include genuine Material 3 day pointers and text editing, nullable
single dates and partial/complete/same-day ranges, independent panels,
recreation/reset, one-shot Detail and directional copying, honest native
empty-date rejection, five locales and bounded 360 dp/font-scale-2 host actions.
The test code uses the actual Classic increment button, Holo wheel and Material
virtual day bounds through public accessibility and touch for DatePicker.
The authored CalendarView scenario includes a Material day gesture; the legacy calendars have
class, selected-date and recreation assertions, without manufactured day geometry.
Native enabled-flag checks do not claim child gestures are blocked.
Host ScrollBy recovery is semantic navigation to the original control, not proof
of an OS display resize, native keyboard behavior or TalkBack speech.

The updated five-test catalog sweep now spans 335 cells: 155 supported and 174
unsupported ordinary cells, plus six native animated cells in the separate
one-method UiAutomation scope. Each framework theme has 22 ordinary supported
and 43 unsupported cells; Material 2 has 32 and 35, and Material 3 has 57 and 10.
The normal 93-test scope across twenty classes installed both APKs but stopped
at the secure-keyguard guard before instrumentation. None of its selected tests
executed. The separate native animation scope was not attempted while the same
lock remained active. The attempt is `.local/inline-dates-ui-verification-attempt.txt`;
scope declarations are `.local/inline-dates-declared-test-scope.json`.

Two inventory-only device tests passed in 1.241 seconds on the SM-X800, with
native exit 0, `OK (2 tests)` and no restoration errors. They verify the packaged
239-source inventory, 112 implemented and 127 pending rows, canonical inline and
dialog mappings, source metadata and pending-query behavior. The retained run is
`.local/device-runs/20261004T214749565Z-849c6b2c/`. Its manifest records source
`a1d4030` plus dirty feature changes later committed as `f39c2cd`, exact APK hashes,
Android 16/API 36 Build.ID `BP2A.250605.031.A3`, the full Samsung OS fingerprint,
target SDK 37, 340 dpi, font scale 1.0,
System app locale, display/window snapshots, pinned libraries and configured
sample themes. No Activity or input ran, and the recorded theme configuration
does not prove any provider rendered.

`asset-consistency.json` confirms identical 21,094-byte source CSV, APK asset and
installed asset content with SHA-256:
`466398c73a2c4b6b80519a390aad625518a726dc16c91dcda8e43b2bf5e92092`.
The app APK is 19,669,079 bytes with SHA-256:
`530455048a478d227c8535dbb2bbd8fe185c255988771d334b35f2c1ef5cb07b`.
The test APK is 1,373,660 bytes with SHA-256:
`ea6494f9152d38d7136f811a35d95ca95377171137233d2ba2fbc6324a689a8d`.
APK identities are retained in `.local/inline-dates-apk-hashes.json`.
The resource result supplies no rendering, interaction, screenshot, experiment
history or historical OS evidence. All new UI assertions and earlier repairs
still require unlocked physical verification.

Catalog-smoke milestone: `ec64799`. Formatting and lint passed with zero errors
and 16 existing warnings, and both debug APK build tasks passed. The JVM task was
`UP-TO-DATE`: it retained the 62 passing results from the input-copy milestone
and did not rerun for this test-only change. Application source remained `9c73239`.

Five [catalog smoke tests](../app/src/androidTest/java/xyz/gaon/componentory/catalog/CatalogRenderingSmokeTest.kt)
compiled. Their ordinary scope covered 147 supported and 167 unsupported cells:
20 supported and 42 unsupported for each framework theme, 32 and 32 for Material
2, and 55 and 9 for Material 3. At that milestone the catalog had 64 entries across five
providers, or 320 cells. Six native indeterminate progress cells are excluded
from the ordinary scope and remain in the separate existing
[native progress test](../app/src/androidTest/java/xyz/gaon/componentory/lab/NativeProgressIndicatorsTest.kt).
Its single method covers both styles across all three framework themes with
UiAutomation and animator scale 1.0. The ordinary Espresso scope excludes these
continuously animated controls to avoid an infinite-animation idle wait.

The smoke assertions check actual framework classes and selected theme styles,
pinned library identity and provider semantics, rendered controls and their
defaults, original dialog/menu windows, and explicit unsupported reasons. The
animated test adds Left-panel source and theme checks, visible pointer targets,
bounded host scrolling and per-cell diagnostics. A detached original constructor
provides a drawable-class and intrinsic-size reference; those comparisons are
limited default-style evidence, not pixel or historical appearance checks.
Neither scope replaces component-specific behavior tests.

Both APKs installed on the SM-X800, but the normal 87-test attempt stopped at the
secure-keyguard guard before instrumentation. None executed, and no new executed
run manifest was produced. The separate one-test native animation scope was not
attempted while the same locked state remained active. No resource-only test was
rerun. The authored assertions and successful test APK build supply no new
rendering, interaction, compact-device or historical OS evidence.

The unchanged app APK is 19,660,199 bytes with SHA-256:
`577c331f0782ad87afc6c40e5a6f6298ae5de47ccec7f5525c0e68b82ef66617`.
The new test APK is 1,350,454 bytes with SHA-256:
`6e042760268d0265d2186de5ae00a8fbe72d4e685245c018f1643bd6f8c7c776`.
Local evidence is retained in `.local/catalog-smoke-format-lint.txt`,
`.local/catalog-smoke-tests-build.txt`, `.local/catalog-smoke-unit-results.json`,
`.local/catalog-smoke-apk-hashes.json`, `.local/catalog-smoke-declared-test-scope.json`
and `.local/catalog-smoke-ui-verification-attempt.txt`. Gradle was stopped after
the builds. These checks and all earlier UI repairs still need unlocked physical
verification.

Input-copy milestone: `9c73239`. Formatting, lint with zero errors and 16 existing
warnings, all 62 JVM tests, and both debug APK builds passed. Ten new JVM tests
cover the positive input whitelist, empty/false/zero values, independent targets,
excluded results and secrets, committed dates/times, exact icon compatibility,
synthetic partial-icon omission, and sanitized saved entries. The existing
eleven-field sample save format is unchanged. Three source reviewers found no
static blocker.

Seven [input-copy tests](../app/src/androidTest/java/xyz/gaon/componentory/lab/InputCopyNavigationTest.kt)
compile, including real Detail navigation, independent directional copies,
original range thumbs and container/badge state, native dialog constructors and
old-window cleanup, honest no-op reasons, exact reachable icon paths, five
Settings languages and a bounded 360 dp host with font scale 2.0. Modal host
dispatch and public widget API fixture setup are explicitly separate from pointer
actions. The bounded host is not an actual OS display change. The partial-icon
policy has no currently reachable compound cross-catalog UI case; only its JVM
test supplies synthetic unavailability.

That milestone's normal 82-test UI attempt installed both new APKs on the SM-X800
but stopped at the secure-keyguard guard before instrumentation. None executed.
The attempt is `.local/input-copy-ui-verification-attempt.txt`; host results and
APK identities are retained in `.local/input-copy-unit-results.json` and
`.local/input-copy-apk-hashes.json`. No executed UI receipt or new resource-only
result was produced. The inventory is unchanged; earlier resource receipts below
retain their own APK identities. Gradle was stopped after the builds. Input
copying, compact rendering, actual OS resize, spoken accessibility and earlier
UI repairs still need physical verification.

Popup-menu milestone: `b8a5766`. Formatting, lint (zero errors and 16 existing
warnings), all 52 JVM tests, and both debug APK builds pass. Three added JVM
checks cover menu API metadata, primary/supporting source search, and saved
choice/action fields. Inventory checks require notes for supporting menu items.
Three independent source reviews found no static blocker.

Six [popup menu tests](../app/src/androidTest/java/xyz/gaon/componentory/lab/PopupMenusTest.kt)
compile. They cover exact native class/theme identity, original library item
semantics, real pointers, disabled rows and launchers, Back and measured outside
touches, independent panels, recreation/reset, and five languages. Host-disable
and Settings disposal use separately labelled semantic host-action dispatch,
not a claim that a user touched background controls through a focusable popup.
Geometry logging is compiled but has not executed; it is not compact-device evidence.

The normal 75-test UI attempt installed both APKs but stopped at the secure
keyguard guard before instrumentation. None executed. The output is
`.local/popup-menus-ui-verification-attempt.txt`. Actual menu geometry and
interaction, compact/large-font rendering, spoken accessibility and earlier UI
repairs remain unverified.

Two separate inventory-only device tests passed in 0.172 seconds on the SM-X800.
They verify 239 rows, 109 implemented and 130 pending sources, five menu mappings,
supporting notes, provider filters and pending queries. The run is
`.local/device-runs/20261004T201006345Z-4500aa4f/`. Its manifest records the
`9789ca3` working tree with changes later committed as `b8a5766`, exact APK and
environment identities, native exit 0, and no restoration errors.
`asset-consistency.json` confirms identical 21,029-byte source CSV, APK asset and
installed asset content with SHA-256:
`94dfc2042cd299741a2003c2fdab535619f042fdba15071f71479a94236fc94e`.
This resource-only result supplies no rendering, interaction, screenshot or
historical evidence. Gradle was stopped after the builds.

Container milestone: `c3484ea`. Formatting, lint (zero errors and 16 existing
warnings), all 49 JVM tests, and both debug APK builds passed. Four new JVM tests
cover genuine container suppliers, source search, older saved panels, and plain
mode with a retained click count. Independent code, UX and test source reviews
found no static blocker.

Six [container tests](../app/src/androidTest/java/xyz/gaon/componentory/lab/ContainerSamplesTest.kt)
compile. They cover all six supported pairs, actual pointer clicks, plain and
disabled behavior, independent modes and counts, recreation/reset, fourteen
unavailable pairs, and five Settings languages. They preserve the libraries'
absence of an invented role or enabled state on plain overloads.

The normal 69-test UI attempt installed both APKs but stopped at the secure
keyguard guard before instrumentation. None of the selected tests executed.
The attempt is `.local/containers-ui-verification-attempt.txt`. Runtime taps,
compact rendering, TalkBack and the earlier UI repairs remain unverified.

Two separately scoped inventory-only device tests passed in 0.169 seconds on
the SM-X800. They verify 239 source rows, 104 implemented and 135 pending sources,
six new container mappings, provider filtering and pending queries. The run is
`.local/device-runs/20261004T194439311Z-a965e7ef/`. Its manifest records the
`dbcdae7` working tree with changes later committed as `c3484ea`, exact APK and
environment identities, resource-only scope, native exit 0, and no restoration
errors. `asset-consistency.json` confirms identical 20,525-byte source CSV, APK
asset and installed asset content with SHA-256:
`645681580407d9da8762556fb098b175978166ce30471b65fde384ea505f2622`.
These results provide no rendering, interaction, screenshot or historical evidence.
Gradle was stopped after the builds.

Time dialogs and preview order: `8620d92` and `c5fbcdf`. Formatting, lint (zero
errors and 16 existing warnings), all 45 JVM tests, and both debug APK builds
passed. The time dialog
feature in `8620d92` adds nine JVM checks for civil-time boundaries, formatting,
time zones, saved-state compatibility, provider availability, and search for supporting
sources. Its first lint run introduced two window size warnings. The fallback now
reads actual host window height through LocalWindowInfo, and the ordered checks
were repeated with those warnings removed.

Six [time dialog tests](../app/src/androidTest/java/xyz/gaon/componentory/lab/TimePickerDialogsTest.kt)
compile. They cover real Material 3 clock, text and AM/PM controls, Classic spinner
editing through IME Done, native theme/class identity, independent committed and
draft values, recreation, reset, disabled launchers, explicit Material 2 absence,
and all five Settings languages. Native widget API setup is named separately
from real pointer editing. The existing six date-dialog regressions also compile.

That milestone's normal 63-test UI attempt installed both APKs but stopped at the
locked-screen guard before instrumentation. None of the selected tests ran.
Its output is `.local/preview-first-ui-verification-attempt.txt`; the time feature
attempt is `.local/time-picker-dialogs-ui-verification-attempt.txt`. The preview
reorder in `c5fbcdf` also passed source review and the ordered host checks.
Date/time interactions, compact modal usability, actual display resizing,
TalkBack, and the earlier UI repairs still need physical verification.

Two separate inventory-only tests passed in 1.409 seconds on the SM-X800.
They verify all 239 packaged sources, 98 implemented and 141 pending entries,
dialog mappings, notes for supporting sources, provider filtering, and pending queries.
The local run is `.local/device-runs/20261004T190856897Z-4ab8fc34/`. Its manifest
records the `5899ec5` working tree with changes later committed as `8620d92`,
the exact APK hashes, device/build/display configuration, resource-only scope,
result, and no restoration errors. `asset-consistency.json` confirms identical
20,099-byte source CSV, APK asset, and installed asset content with SHA-256:
`e836a4856633a7c00250eb971f99a2fe82197f791c2f3f2ddaa4ea8f2da6d36b`.
This run predates the preview reorder and provides no rendering, touch,
screenshot, or historical OS evidence.

Date dialog milestone: `ac9facb`. Formatting, lint (zero errors and 16 existing
warnings), all 36 JVM tests, and both debug APK builds passed. Eight additional
JVM checks cover date suppliers, saved-state compatibility, leap and invalid
dates, time zones, the Thai default locale, and localized UTC formatting.
Six [date dialog tests](../app/src/androidTest/java/xyz/gaon/componentory/lab/DatePickerDialogsTest.kt)
compile, covering original input editing, Confirm/Cancel/Back, independent
panels, open drafts across recreation, reset, disabled launchers, unavailable
Material 2, and five-language date feedback. Native `updateDate` setup is
explicitly distinguished from touching a calendar day.

That milestone's normal 57-test UI attempt installed both APKs but stopped at the
locked-screen guard before instrumentation. None of the selected tests ran.
Its output is `.local/date-picker-dialogs-ui-verification-attempt.txt`.
Date interactions, compact modal usability, native calendar-day gestures,
and the earlier UI repairs remain unverified.

Two separate inventory-only tests passed in 0.163 seconds on the SM-X800.
They verify all 239 packaged sources, 93 implemented and 146 pending source
entries, new dialog mappings, provider filtering, and pending queries. The local
run is `.local/device-runs/20261004T183239891Z-3004eeff/`. Its manifest records
the `fd9df18` working tree with the changes later committed as `ac9facb`, exact
APK hashes, device/build/display configuration, resource-only scope, result,
and no restoration errors. `asset-consistency.json` confirms that the source
CSV, APK asset, and installed asset have identical 19,721-byte content and SHA-256:
`33318178221e3454ad4a6eaa95953fed6bf66593bf0265be0e0a3835c515b6c1`.
This run provides no rendering, touch, screenshot, or historical OS evidence.

Commit `dd0df74` packages an exact generated copy of the audited CSV as an app
asset. The small [inventory reader](../app/src/main/java/xyz/gaon/componentory/catalog/ComponentInventory.kt)
preserves source providers, API introductions, statuses, sample IDs, and notes.
Pending API search uses source names and an optional provider filter. Invalid or
empty inventories fail explicitly instead of becoming an empty pending list.
Commit `ffa6e08` adds the Planned APIs list with source search, provider filtering,
explicit loading and failure states, and five-language pending labels. Its cards
are read-only API metadata, not unsupported components or original captures.
That milestone kept coverage at 57 entries and 134 runnable combinations.

At that milestone, formatting, lint with zero errors and 16 existing
warnings, all 28 JVM tests, and both debug APK builds passed. Five Planned-list
tests, four detail-provider navigation tests, and five comparison-state regressions
compile. That selected 41-test physical UI attempt installed the APKs but
stopped at the locked-screen guard before instrumentation. No tests in that
attempt executed. Planned rendering, navigation, localized UI, and state
restoration therefore still need physical confirmation. Loading-failure injection,
TalkBack, larger fonts, and a separate phone also remain unverified.

Commit `3d44b98` clarifies the theme/library selector and runtime OS note, adds
availability hints without disabling unsupported choices, and exposes selected
menu semantics. Three additional provider-identity tests compile. Its selected
44-test physical attempt was also rejected by the locked-screen guard before
instrumentation; none of those selected tests executed. At that milestone,
formatting, lint, 28 JVM tests and APK builds passed. Source/provider identity
is kept separate from theme introduction dates and actual historical OS execution.

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
