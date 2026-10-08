# Componentory's hidden screen

Componentory has its own Easter egg screen, separate from the Android release
ports in the Components catalog. This first step supplies the destination screen:
the app artwork, a welcome message, and the installed app version. It uses the
app's Compose Material 3 theme and all five supported languages.

The close button and Android Back both return to the caller. The screen respects
system insets, limits text width in wide windows, and scrolls its body when a
short window or large text needs more room. The close button stays outside the
scrolling body.

This is an initial screen, with no game or original historical Android capture.
The next focused change connects repeated taps on Settings > App version to it.

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
