# Componentory's hidden screen

Componentory has its own Easter egg screen, separate from the Android release
ports in the Components catalog. The initial destination screen shows
the app artwork, a welcome message, and the installed app version. It uses the
app's Compose Material 3 theme and all five supported languages.

## Entry and return

Open Settings, scroll to **App version**, and tap its row three times within one
second. One or two taps leave Settings open. If the burst takes longer than one
second, its next tap starts a new burst. The whole row accepts touch and
accessibility click actions; it keeps its compact appearance without an arrow.

Tap timing uses Android uptime, so changing the wall clock cannot unlock the
screen. Partial taps are not saved. Changing tabs or selected settings pages, or
recreating the Activity, discards them. Every opening needs a fresh burst.

The hidden screen takes over the app window. The navigation bar or rail is absent
until it closes. Its open state survives Activity recreation, while the saved
Settings page and scroll position return after closing. There is no permanent
unlock preference, additional permission, or exported Activity.

The close button and Android Back both return to Settings. The screen respects
system insets, limits text width in wide windows, and scrolls its body when a
short window or large text needs more room. The close button stays outside the
scrolling body.

This is an initial screen, with no game or original historical Android capture.

## Screen verification — October 8, 2026

Spotless apply/check and app debug lint passed in that order. Lint reported no
errors and 77 existing warnings. All 177 app JVM tests passed, and the debug app
and instrumentation APKs built successfully.

`ComponentoryEasterEggScreenTest` passed all three checks on a Samsung SM-X800,
Android 16 / API 36, build `BP2A.250605.031.A3`, target SDK 37. The checks exercised
the close button, Android Back, and a 320 × 300dp viewport with font scale 2.0.
The app ran with Compose Material 3 `1.5.0-alpha01` and English text. The physical
display was 1752 × 2800 at density 340; the device font scale was 1.0 outside the
test's local override. No screenshot was collected for this screen-only scope.

Run `20261008T110928330Z-0f06f55c` retains APK hashes, source state, display,
orientation, and restored settings in ignored `.local/device-runs/`.

## Entry verification — October 8, 2026

The entry change passed Spotless apply/check and app debug lint before tests.
Lint still reported zero errors and 77 warnings. All 183 app JVM tests passed,
including six burst-timing checks. Both debug APKs built successfully.

The final APK passed ten interactive checks on the Samsung SM-X800, Android 16 /
API 36, and seven on the Samsung SM-S731N, Android 17 / API 37. Both scopes covered
three-tap entry, rejection of partial bursts, repeat entry after closing, tab and
Activity reset of partial taps, open-state restoration, Back, the close button,
and the short-window/large-text layout. The tablet also ran the three existing
Settings navigation checks. Tests used English; a separate Korean dark-theme
phone capture was inspected after the tests.

The final run IDs are `20261008T111624407Z-f79e71fd` and
`20261008T111731242Z-e6fc8e71`. Each retains the exact source state and binary
identities. This verifies the app's entry screen on current devices; it does not
establish original appearance or behavior for the Android release ports.
