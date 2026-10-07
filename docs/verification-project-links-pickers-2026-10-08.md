# Verification: project links and picker layout, October 8, 2026

About now opens the [GitHub repository](https://github.com/gaon12/Componentory)
and the [new issue page](https://github.com/gaon12/Componentory/issues/new).
Both rows show their destinations, wrap long addresses, and report a missing
browser with a localized inline message. Tests intercept outgoing intents;
they do not submit issues or access a GitHub account.

The picker update fixes three layout problems:

- A framework TimePicker collapsed to 32dp when measured inside an unbounded
  horizontal viewport. It now receives the panel width, with a 280dp floor for
  its original spinner editors. Its selectors and dial receive finite bounds.
- CalendarView retained a 360dp width in narrow panels, hiding the final weekday.
  Its native calendar now uses the panel width and a font-scaled height.
- Material 3 chose its clock layout from the device orientation. Narrow panels
  now use its original vertical layout; horizontal layout starts at 600dp.

Framework and Material 3 date/time previews share visible overflow guidance and
buttons that reveal both edges. These appear only when content exceeds the
panel width. The controls retain their original APIs and text sizes.

The [earlier design verification](verification-design-update-2026-10-08.md)
retains the larger design, resource, Toast, and catalog checks for its own APKs.
The native previews below use the recorded modern OS and light themes.
Original historical OS captures remain missing.

## Source and binary identity

- Project links: `2a2269a`.
- Picker layout and affected tests: `3104eaf`.
- App APK SHA-256: `09ff564448faf940325dbe23c401ed25a15fe9c77b33e6370d937231bc3eb0ac`.
- Test APK SHA-256: `faae7bc007bfcacda7e9f0087bff01e17b04d68865c67569fda315b8d2950516`.

Run manifests retain their actual pre-commit revision and working-tree changes.
The final application and test binaries match the hashes above. Documentation
changes do not change the APKs. Run IDs and timestamps use UTC; the runs occurred
on October 8 in Korea Standard Time.

## Phone configuration

| Item | Recorded value |
| --- | --- |
| Device | Samsung SM-S731N, r13s |
| OS / build | Android 17, API 37 / CP2A.260605.016 |
| Fingerprint | samsung/r13sksx/r13s:17/CP2A.260605.016/S731NKSU9CZIF_OKR9CZIF:user/release-keys |
| Display | 1080 × 2340, portrait, 450 dpi |
| Font scale | 1.15 |
| Target / minimum SDK | 37 / 24 |
| System / app appearance | Dark system, app follows system |
| Framework themes | Theme.Light, Theme.Holo.Light, Theme.Material.Light; explicit light resources |
| Material 2 | 1.10.4 |
| Material 3 / You / Expressive | 1.5.0-alpha01; separate light palettes and recorded motion configuration |
| Icons / inline helper | 1.7.8 / AndroidX Autofill 1.3.0 |

## Executed checks

Each implementation change ran Spotless and Android lint before affected tests.
Lint reports zero errors and 63 warnings. All 171 JVM tests pass with no failures,
errors, or skips. Both debug APKs build successfully.

| Run ID | Scope | Result |
| --- | --- | --- |
| 20261007T195408084Z-984e4dee | Project links, settings navigation, introduction | 6 passed; earlier links APK |
| 20261007T210449975Z-9a30cc50 | Native picker measurement, landscape host clock, overflow buttons | 3 passed; final APKs |
| 20261007T210530496Z-37414827 | Date pickers, time pickers, clocks, native readability, project links | 20 passed; final APKs |

The final suite includes seven date tests, six time tests, two viewport tests,
one native clock test across three themes, two readability tests, and two link
tests. It uses real pointers, original native editor focus and IME actions,
and the original Holo wheel's idle callback before preserving a selected time.
The native calendar's last weekday and the library calendar's last column are
selected by touch. Activity recreation and 12/24-hour changes retain values.

Intermediate failures remain in `.local/device-runs/`, including the initial
20-test run `20261007T201944484Z-b3699434` with four failures and the next run
`20261007T202948086Z-f17740ba` with three. The investigation corrected ambiguous
test selectors, focused native editors before input, and waited for native
fling completion. Diagnostic run `20261007T205511849Z-f85dfe02` exposed the
32dp TimePicker width and failed until the application measurement was fixed.
These intermediate runs are not passing evidence.

The landscape regression uses a 280dp Compose host and a landscape
LocalConfiguration on the phone. Large-font tests use bounded Compose hosts;
they establish control reachability rather than drawn-text or TalkBack results.
The tablet remained locked, so this update has no new tablet touch results.
Device test manifests report no restoration errors for the final passing runs.

## Visual review and recovery

The installed APK hash matches the application hash above. Final screenshots
are retained with hashes and capture metadata in:

- `.local/ui-review/picker-update-final`: analog clock, Material 3 clock, calendar
  overflow guidance and both edges, and Korean About links.
- `.local/ui-review/native-picker-update-complete`: the complete native time
  selector and dial, and all seven columns of the native calendar.

The aligned native capture completed at `2026-10-07T21:24:42Z` from `3104eaf`.
The full time picker and calendar fit inside the visible phone page. The two
Korean link addresses wrap within the About card. Original library calendars
retain their wider content and expose both edges through the visible controls.
Screenshots establish visual evidence; interaction results come from the
separate tests above.

Both review passes restore the original app locale, SYSTEM appearance, and
incomplete first-launch introduction preference. The review also retained
recovery records for attempts whose capture-driver positioning failed; those
attempts are excluded from the final complete native images.
