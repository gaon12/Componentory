# Componentory product plan

Componentory is an Android UI lab. Users choose a component, try real controls,
and compare the design families available on their device. The current direction
uses a physical Android device. PC emulators are optional future tools for exact
historical OS validation, rather than a requirement for using the app.

## What a version means

Keep the design family and the execution environment visible together:

| Family | Source | Meaning |
| --- | --- | --- |
| Classic | Current framework engine, Theme.Light; selected Android 2.3.7 resources | Resource recreation where artwork is bundled. |
| Holo | Current framework engine, Theme.Holo.Light; selected Android 4.4.4 resources | Resource recreation where artwork is bundled. |
| Material Design 1 | Current framework engine, Theme.Material.Light; selected Android 5.0.2 resources | Resource recreation where artwork is bundled. |
| Material Design 2 | Compose Material 1.10.4 | Separately versioned library implementation. |
| Material Design 3 | Compose Material 3 1.5.0-alpha01 | Standard light palette and standard motion. |
| Material You | Same Material 3 pin; Android 12+ dynamic colors | Real device palette, without a static fallback below API 31. |
| Material 3 Expressive | Same experimental pin; MaterialExpressiveTheme | Frozen first public Expressive library generation. |

A theme does not switch the OS. Resource recreations use explicitly pinned
artwork and a disclosed current-device engine. Other framework components carry
a current-OS badge. Toast has its own AOSP preview and app popup rather than
invoking the OS Toast renderer. Do not label any of these as an original OS
capture. The [design guide](design-families.md) records exact suppliers and pins.

The long-term catalog should cover more components, theme variants, library
releases, and Android history. Do not imply that the first supported families
cover every release. Missing components and uncollected captures stay explicit.

## Main experience

Use bottom navigation on phones and a navigation rail on wide windows with
**List**, **Compare**, **Runs**, and **Settings**. The list contains the
**Components** and **Version history** modes.
Open the component list first. Browsing, single-component exploration, and
comparison have their own screens, rather than sharing one long lab page.

1. Search the list by translated name or description, or English API name, and
   combine the query with a component category.
2. Open a component and select its Android version/theme or pinned library version.
   On large screens, keep the catalog beside the selected component.
3. Touch the real control, inspect the feedback, and try disabled state or reset.
4. Use the detail page's comparison action to compare that component and family
   against another family. Left starts with the current eligible Detail inputs;
   Right starts with its provider defaults. Both share Detail's Enabled setting.
   Copy inputs in either direction while keeping each panel independently editable.
5. Open Settings categories, then their dedicated pages for appearance, language,
   device identity, libraries, licenses, and About. Replay the first-launch
   introduction from About.

Keep the search and scroll position when returning from a detail page. Preserve
tab state across navigation and Activity recreation. Back from a detail returns
to its list context. Selecting the already active List tab returns to the list.
Keep app appearance separate from the samples' explicit light configuration,
including night-qualified framework resources. Use a 16 sp floor for native
sample labels and disclose that this adjusts current-device appearance.

Offer Korean, English, Japanese, Simplified Chinese, Traditional Chinese, and
System language. Translate the browsing interface, sample text, accessibility
labels, and interaction feedback. Keep API identifiers and icon names intact.
Persist language choices across restarts and keep translations available offline.

Use two columns when there is enough width and stack panels on narrow screens.
Keep the two panels' interaction state independent. In comparison, changing a
component or theme starts a fresh sample so previous state does not silently
enter a new experiment.

An explicit Detail comparison action starts a new comparison session. Apply its
inputs once; later Reset, tab restoration, provider changes and Activity recreation
must not replay the original entry. Directional copying also starts a fresh target
sample and clears its observed results without changing the source or providers.
Copy only inputs used by the actual control: safe text, selections, configured
values, committed date/time, container mode and exact compatible icons. Passwords,
action counts, menu results and unconfirmed drafts are excluded. Explain a copy
that cannot run and leave the target unchanged. Keep these host tools usable when
the original sample controls are disabled.

Inline date selections apply immediately. Preserve a library picker's empty date
and a range's empty, start-only or complete selection as actual inputs. Copying
an empty date to a framework picker cannot represent that input; explain the
limitation and leave its current sample unchanged. Keep dates as UTC civil days,
and bridge CalendarView's local timestamp API without changing the selected day.
Fresh copies open at the copied date or range-start month. Editor modes and the
source's browsed month stay out of the copied setup.

Explain when the supplier has no control that disables all its interactions.
Global Enabled does not apply to CalendarView or the Material 3 inline date
pickers. Framework DatePicker receives its original public enabled flag; verify
child interactions separately. Preserve original picker controls in readable,
scrollable viewports and bound the range calendar's own month list. A host layout
fixture is not evidence that a physical small screen or larger font is usable.

Fixed Text uses localized multiline sample content and the original Text API
without explicit text-style arguments. It has no per-panel input to copy.
Explain that library Text has no enabled parameter instead of simulating a
disabled library control. Framework TextView uses its public enabled flag.

CheckedTextView is framework-only. Disclose the explicitly configured themed
multiple-choice drawable and report a missing mark. A named host Checked switch
sets the original widget's checked value; the original text has no synthetic
click listener or automatic toggle. Preserve and copy checked or unchecked input
only to a supported supplier. Keep the host switch usable while the original
widget is disabled, with the behavior note beside its feedback and configuration.

Explain the selected family's origin without presenting each Android release as
a distinct theme that the device can necessarily provide.

History entries open descriptions, availability metadata, source revisions, and
capture/behavior evidence. Offer a current-device sample only when a real mapping
exists. Keep missing original captures and unverified historical behavior visible.

Use two settings columns when there is enough width. Keep full language names,
provider names, and search results usable at 320 dp and with the software keyboard.
Do not keep an empty Planned APIs tab once the audited baseline is implemented.

The app's own [Easter egg](componentory-easter-egg.md) opens after seven taps on
Settings > App version. Follow Android's build-number countdown: the third
through sixth taps show four, three, two, and one remaining taps in a native
Toast, with no timing limit. Keep its destination separate from
the version-specific Android ports in the catalog. The first milestone supplies
the entry gesture and a localized screen. Save the open screen across Activity
recreation, discard incomplete gestures, and return to the saved Settings page
when it closes. Additional game content remains future work.

## Component coverage

The first prototype included eight basic types in five families. The current
baseline has 168 entries and seven design choices, including eight experimental
Material 3 components, the public ToggleButton, and AOSP-backed Toast recreations.
The nine APIs work under all three Material 3 themes from the same artifact.
Modern clock concepts have separately identified Componentory theme demos.
The catalog now includes framework-only controls, library action variants,
chip and selection samples, additional input types, inline dates, fixed Text,
framework CheckedTextView,
and icon browsing. Use the
[coverage inventory](component-coverage.md) to keep the broad expansion auditable.
Continue adding components, visual variants, and historical coverage in focused
changes.
Show the actual class or library package alongside the sample. Direct platform
samples use `android.widget` or framework dialogs. AOSP artwork replays must be labeled
as recreations; Toast uses an explicit app popup. The Compose shell must not
silently substitute an AppCompat or Material control for a claimed platform API.

Library samples are separate families with exact dependency version labels.
Supply the selected library's own sample theme explicitly. Material 3 samples
use the pinned Typography defaults instead of inheriting app typography. Verify
the production renderer under a caller with a deliberately different font and
text metrics; a compiled regression is not an executed rendering result.
Only offer actions that the selected component supports. A read-only progress
indicator should not pretend to respond to taps. Missing dedicated APIs do not
make a UI concept impossible. An explicitly implemented alternative must show
its own source, rendering badge, theme, and tested behavior. Unimplemented
alternatives remain Sample unavailable with an explanation. Do not silently
use a current-OS widget as a claimed Material or historical implementation.
Prioritize remaining work using the
[component design audit](component-design-audit-2026-10-08.md).

An icon sample must offer the selected source's complete available catalog,
with search, style filters, and preview. Let users apply a chosen icon to the
current sample and keep comparison selections independent. A resource exposed
by the framework but missing on a device needs an explicit unavailable label.
Record the icon artifact version separately from the component library version.

See [current component verification](verification-component-design-audit-2026-10-08.md)
for clock and API checks and [release verification](verification-release-2026-10-08.md)
for the published artifact. Retained milestone results describe their original
source states.

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

Runs stores and exports user comparisons with their environment and observed
feedback. Automated verification has its own retained reports and does not turn
those saved comparisons into test results. Keep development verification records
in the repository documentation and machine-specific outputs outside Git. Never
label fixtures or planned samples as completed experiments.

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

## Publishing and privacy

Maintain five localized Google Play listings and genuine, attributed screenshots.
Use the [release guide](google-play-release.md) for current specifications and
account gates. A signed bundle, a GitHub release, a Console upload, submission
for review, and a live store listing are separate outcomes.

Bundle the [canonical privacy policy](privacy-policy.txt) unchanged and generate
its public HTML from that same source. Keep local preferences and saved runs out
of automatic backup. Networking, accounts, analytics, or automatic export would
require reassessing the privacy text and Data safety declarations.

Keep signing credentials outside Git. Existing Play registrations require their
registered upload identity. Preserve local user data when installing builds with
different certificates and turn off test-device screens after final verification.

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
- [Per-app languages](https://developer.android.com/guide/topics/resources/app-languages)
  describe locale resources and Android's app language setting.
- [Compose resources](https://developer.android.com/develop/ui/compose/resources)
  describe icon styles and the extended Material icons artifact.
