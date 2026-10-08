# Design families and resource recreations

The selector follows Classic → Holo → Material Design 1 → Material Design 2 →
Material Design 3 / Material You → Material 3 Expressive. Material You has its
own choice because it reads device colors, while standard Material Design 3
uses the library light palette. A design choice does not change the device OS.

| Choice | Supplier and configuration | Historical evidence |
| --- | --- | --- |
| Classic | Current framework engine, Theme.Light; selected Android 2.3.7 artwork | Resource recreation; original OS captures missing |
| Holo | Current framework engine, Theme.Holo.Light; selected Android 4.4.4 artwork | Resource recreation; original OS captures missing |
| Material Design 1 | Current framework engine, Theme.Material.Light; selected Android 5.0.2 artwork | Resource recreation; original OS captures missing |
| Material Design 2 | Compose Material 1.10.4, lightColors | Versioned library sample |
| Material Design 3 | Compose Material 3 1.5.0-alpha01, lightColorScheme and standard motion | Versioned library sample |
| Material You | Same Material 3 pin, dynamicLightColorScheme on API 31+ | Versioned library sample with device colors |
| Material 3 Expressive | Same Material 3 pin, MaterialExpressiveTheme, expressiveLightColorScheme and expressive motion | Experimental versioned library sample |

Material You is explicitly unavailable below Android 12/API 31. The app does
not silently substitute static colors. Standard Material Design 3 owns its
shapes, typography, and standard motion, so the app shell or an adjacent
Expressive sample cannot change them. Saved runs include resolved palette
colors, theme, motion, OS fingerprint, display settings, and library pins.

## The Expressive pin

Material 3 1.5.0-alpha01 is a frozen sample of the first public Expressive
library generation, released July 30, 2025. It is an experimental historical
pin, not a claim to use the newest available library. The stable 1.4.0 line
removed the experimental Expressive APIs in beta01. The standard, dynamic,
and Expressive variants therefore share one explicit 1.5.0-alpha01 artifact
and remain separate through their actual themes and APIs.

The catalog adds ButtonGroup, SplitButtonLayout, LoadingIndicator,
LinearWavyProgressIndicator, CircularWavyProgressIndicator,
HorizontalFloatingToolbar, VerticalFloatingToolbar, and
FloatingActionButtonMenu, plus the public ToggleButton API. All nine work
under standard Material 3, Material You, and Expressive because those choices
share the artifact. They keep the actual selected theme instead of forcing an
Expressive palette. Experimental APIs carry an explicit note in the app.
They call the real public library functions. Dedicated device assertions
exercise overflow, selection, enabled state, progress, toolbar expansion,
and FAB menu actions.

Reviewed Google Maven artifacts:

- [Source JAR](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3-android/1.5.0-alpha01/material3-android-1.5.0-alpha01-sources.jar), SHA-256 `fb0d733398347c8977877a8afdda9fe79da409c0c3ca638b3bcaa4c2bf582bd7`.
- [Android AAR](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3-android/1.5.0-alpha01/material3-android-1.5.0-alpha01.aar), SHA-256 `621b9dd8142eb13690aab42f110d4a409b52dd3aef89e7ba20e9a939995218aa`.
- [Official Material 3 release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3).

## Bundled AOSP controls

The app now uses selected upstream resources instead of merely exporting
resource graphs. `aosp-resources/controls.json` records 1,042 source variants
and 35 component/design pairs. They supply button, checkbox, radio, toggle,
text-field, progress, slider, spinner, rating, analog-clock dial/hand, and
available switch artwork.
The retained Android 2.3.7, 4.4.4, and 5.0.2 release commits identify their
bitmap, nine-patch, drawable XML, colors, and animation dependencies.

Original bitmap and nine-patch source bytes remain unchanged in the repo.
Private XML references, IDs, interpolation resources, and colors receive
explicit app bindings. The imported controls use the installed OS interaction
engine and a readable 16sp text floor. The badge and saved-run metadata call
this a resource recreation. Other framework components carry a current-OS
badge. Neither label implies an original historical OS capture or behavior pass.

Regenerate the audited closure with `python -m scripts.history.bundle`.
Review resource provenance and run `python -m unittest scripts.history.test_bundle`
after regeneration. AOSP resource hashes describe the imported bytes before
Android resource compilation; compiled nine-patches have a different encoding.

## Clock displays and alternative samples

Classic, Holo, and Material Design 1 AnalogClock samples inflate XML with
public dial, hand_hour, and hand_minute attributes bound to the selected
release. Their default mdpi dial bytes are identical in the upstream source.
The installed framework still supplies their interaction and time engine.

Material 2/3 libraries have no dedicated live-clock or chronometer API in
the recorded sources. All four modern design choices instead show labeled
Componentory theme demos using Compose drawing, text, and the selected
library's colors, typography, and buttons. Saved runs record THEMED_DEMO
and COMPONENTORY_COMPOSE; their source identifies ThemedClockSample.
A TimePicker remains a separate, real input API.

Clock text auto-sizes with a 16sp floor and a two-line allowance. Timer
actions use 16sp labels and flow onto another row. Their running elapsed
time is monotonic, and stopped values, formats, and eligible saved states
can move between native clocks and these demos.

An unavailable alternative says Sample unavailable and explains the
implementation gap. It does not imply that the UI concept is impossible
under that design. Native theme and dynamic-color OS requirements keep
their own explanations. The
[full audit](component-design-audit-2026-10-08.md) and its 73-entry matrix
record current-OS resource gaps and remaining modern alternatives.

## Toasts across all designs

Toast remains an Android platform facility. Compose Material does not supply
its own Toast function. The lab now offers a separately identified AOSP toast
recreation for every design choice, including modern library choices. It shows
a persistent comparison preview and an app-owned, short-lived popup without
calling the installed OS Toast renderer.

| Artwork choice | Pinned AOSP release | Retained source |
| --- | --- | --- |
| Classic | android-2.3.7_r1 | transient_notification.xml and original nine-patches |
| Holo | android-4.4.4_r2 | transient_notification.xml and original nine-patches |
| Material Design 1 | android-5.0.2_r1 | transient_notification.xml and original nine-patches |
| Material Design 2 | android-9.0.0_r1 | transient_notification.xml and 22dp toast frame |
| Material Design 3 / Material You | android-12.0.0_r1 | SystemUI text_toast.xml and 28dp toast frame |
| Material 3 Expressive | android-16.0.0_r1 | SystemUI text_toast.xml and 28dp toast frame |

The source toast artwork is associated with an Android era, not attributed to
a Compose Toast API. Holo and Android 5.0.2 share identical upstream nine-patch
bytes; Android 12 and 16 share the 28dp frame geometry. The app preserves those
facts instead of inventing differences. Modern toast layouts retain the 24dp
app icon, padding, ellipsis, and two-line limit. Modern colors explicitly use
the selected library palette, including Android dynamic colors for Material You.

`aosp-resources/toasts.json` retains six immutable commits, 22 original resource
variants, original XML text, source and bundled SHA-256 hashes, ancestor notices,
and adaptations. App-owned IDs, 16sp text and font bindings, source-compatible
line height, wrap-content layouts, a 320dp width bound, 4dp elevation, and popup
positioning are disclosed adaptations. Decorative icons are excluded from the
accessibility reading order.

The popup timeout respects Android's recommended accessibility timeout.
Repeated taps reset its lifetime and increase the shown count. Disabling,
disposing, resetting, or replacing a sample with copied inputs removes its
popup. Re-enabling does not reopen an old message. Copying and saved setup carry only the shown count.
The canonical `android.widget.Toast` inventory row is **Recreated**, rather than
claiming an instantiated system Toast or inventing library Toast rows.

Regenerate with `python -m scripts.history.toasts`; verify with
`python -m unittest scripts.history.test_toasts`. Both resource generators
collect the retained release notices for the offline reader.

## Onboarding, settings, and attribution

First launch presents three localized introduction pages covering browsing,
comparison, and source evidence. Completion is stored locally. Settings →
About can replay the introduction. Settings opens a grouped category list:
appearance, language, device, libraries, licenses, and About each has a page
with Back navigation. Wide layouts keep the category list beside its page.

The launcher, monochrome icon, and in-app branding use the supplied flask and
cube vector. The app retains Apache 2.0 text, original release notices, and
individual third-party terms. This is not a blanket license assignment for
all SDK or AOSP files. About and NOTICE state: Android is a trademark of
Google LLC. Componentory is independent and is not endorsed by Google LLC.

Original historical OS captures are still missing. Current-device tests prove
only the behaviors and configurations actually exercised on that device.
