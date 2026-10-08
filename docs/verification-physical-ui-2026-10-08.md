# Physical UI verification — October 8, 2026

This report records interactive checks on an unlocked phone and tablet. It
extends the earlier [resource-only verification](verification-easter-eggs-2026-10-08.md).
The retained Android Easter eggs are code ports running on the installed OS.
Neither these runs nor their screenshots establish original historical OS UI.

## Repairs found during testing

Commit `847b421` updates catalog test navigation. The 26 Easter egg entries now
precede the regular component rows, so tests search for Button and scroll to its
row instead of assuming that it is initially visible. Category assertions use
the runtime category. Search, reset, recreation, and comparison behavior passed
after the test repair.

Commit `a7978a9` fixes a real game process termination on the Android 16 tablet.
Jelly Bean's BeanBag enabled its own dream component with flags set to zero.
Android terminated the host when that component state changed. The port now
checks the state and uses `DONT_KILL_APP`. The regression sweep first disables
the dream, launches BeanBag through the catalog action, checks the unlock, and
continues to later screens in the same test process. Its original/imported hashes
and the adaptation are recorded in [provenance](../eastereggs/provenance.json).

The new game fixture uses lifecycle callbacks for continuously redrawing games.
It waits for a resumed, focused window and its first draw. Screen captures wait
two seconds for retained entrance delays. Real pointer injection reaches the
wallpaper-backed nonogram window. The Marshmallow preview that only displays a
toast and immediately finishes has a separate accessibility-event/lifecycle
assertion. It is not treated as a persistent screen.

Commit `8b537a3` repairs the UI runner's lock detection. The Android 17 phone
reports `KeyguardServiceDelegate`'s `showing` field instead of `mIsShowing`.
The runner now recognizes both forms and refuses to proceed when the state is
missing or malformed. Text formatting and PowerShell syntax checks passed,
followed by all 38 evidence-script checks and the unlocked phone's nine latest
entry-screen checks. PSScriptAnalyzer was not installed; its rule checks did not
run. This test-runner change does not alter the app binary.

Earlier runs failed on catalog visibility, an installation still in progress,
an idle wait on a live game, incorrect painting-child selection, target-UID touch
injection, bypassed palette activation, and BeanBag's process termination. These
attempts remain recorded as failed and do not count toward the passing totals.

## Ordered checks for the BeanBag repair

Code changes were followed by formatting/lint, relevant tests, diff review, and
the focused English commit. The final scopes were:

```text
spotlessApply spotlessCheck :app:lintDebug :eastereggs:lintDebug
:app:testDebugUnitTest :eastereggs:testDebugUnitTest
:app:assembleDebug :app:assembleDebugAndroidTest
```

The first full lint analysis passed in 5m 31s. A further regression-fixture
improvement was then formatted and linted in 34s (seven executed tasks, 70
up-to-date). The following test/build phase passed in 54s (12 executed tasks,
94 up-to-date). All 177 app JVM tests and both port model tests executed with
zero failures or skipped tests. Both APKs built.

Lint retains zero errors and 77 app warnings, plus zero errors, 439 port warnings,
and six port hints. It is not warning-free. The provenance verifier checked all
834 retained files against upstream commit
`63d3e4549efbd6f714f6c19764d9520906c18c57`, including 648 unchanged resources.
Thirty PowerShell evidence checks passed. `git diff --check` passed before the
focused commit.

## Devices and configuration

| Setting | Phone | Tablet |
| --- | --- | --- |
| Model/device | Samsung SM-S731N / r13s | Samsung SM-X800 / gts8pwifi |
| Installed OS | Android 17 / API 37 | Android 16 / API 36 |
| Build | CP2A.260605.016 | BP2A.250605.031.A3 |
| Physical display | 1080 × 2340 | 1752 × 2800 |
| Test orientation | Portrait | Landscape, logical 2800 × 1752 |
| Density | 450 | 340 |
| Device font scale | 1.15 | 1.0 |
| System locale | ko-KR | ko-KR |

The debug app is 1.0.0 (code 2), min SDK 24, target SDK 37. Per-run private
manifests retain full OS fingerprints, actual logical bounds, theme settings,
provider versions, APK hashes, and source state. Public reports omit network
addresses and device serials.

The app shell uses Compose Material 3 `1.5.0-alpha01`. Samples also exercise
framework Theme/Theme.Holo/Theme.Material and the pinned library families,
including Compose Material 2 `1.10.4`. These are current-device theme/library
checks. Tests begin in English and explicitly exercise the five supported
languages. Animation scales are temporarily zero for stationary UI tests; live
games use animator scale 1.0. Preference values and component states are restored
by the game fixture, and locale/animation restoration is recorded per run.

## Binary identities

The BeanBag repair and its game/input probes used:

```text
app:  91ae9d12763c242107e0c8637df723402b204bc75c43eb3d58a60ff1c0dbe8a8
test: 3df27fc8bc9217c9cad0504e45ef64ddbedaa1c8ac9a1ba71ec2caa566385cd7
```

The subsequent integration includes the separately developed Componentory hidden
screen and three-tap entry, committed through `4bba23f`:

```text
app:  d3e3fb3b2bbd93d062258a14bffe10af3f6fcbbedb5bcae63821234cca7bd153
test: 995ebb1616956af51e68e894f07efe008d65bd4a72f69326b7847290456023f0
```

Concurrent feature work changed build outputs during this session. The two earlier
APK pairs were copied to ignored private directories. The final seven-tap
countdown implementation in `47020bb` used a third retained pair:

```text
app:  c26998a0a6bf6c53a4194a4010c7872e17d53c26e3d7baf12290c8588e0c1f4f
test: e3d382e51e8ac21ff47ac6a28a2bfd8b2099b4f1cbaba4d4d4c4b57e2c32a447
```

The local runner checks
installed base and test APK hashes before reusing them; it rejects mismatches.
It otherwise uses the repository's evidence, locale, animation, and scope checks.
An unchanged non-streaming tablet install had stalled earlier; successful
streamed installations and exact installed hashes were confirmed before reuse.
Run-start Git snapshots may include other uncompiled concurrent changes. The
identities above identify the tested binaries, not every file visible at that
later snapshot time.

## Passing interaction scopes

The final game runs each passed six JUnit methods:

| Device | Run ID | Methods | Visible screen captures |
| --- | --- | --- | --- |
| Phone | 20261008T110413574Z-a58d910c | Six passed | 43 distinct screens; 51 total captures |
| Tablet | 20261008T110117450Z-e18c1d17 | Six passed | 43 distinct screens; 51 total captures |

The screen sweep launches every eligible distinct catalog logo/additional screen
through the host action, checks first draw/focus and continued lifetime, and
captures its frame. The other assertions check actual nonogram input and marked
cell restoration, painting pixels and clearing, Marshmallow participant controls,
nonzero thrust and its release in all four space engines, and the transient
Marshmallow preview toast. Captures have private PNG/SHA-256 records. The screen
sweep alone is not proof that each game's interaction or completion passed.

A separate tablet check opened Lollipop's Flappy Droid using its actual catalog
button. Holding a real touch started play: the player and obstacles rendered,
and the score field became visible with value zero. Before/held/after frames and
the visible score bounds are retained privately. The initial sky-only capture
is not counted as gameplay success or an original OS capture.

The major UI scope contains 56 methods across 21 existing suites. It covers:

- Compact settings categories, version/links/license/contributor rows, modal
  restoration, offline privacy, and Android trademark text.
- Light/dark appearance, native widget pixels, system bars, five languages,
  search and unsupported explanations, and readable control labels.
- Catalog filters, reset and recreation, sample/comparison selection, back and
  tab behavior, and wide/compact layout transitions.
- Native and recreated clock labels, wrapping at narrow widths with large text,
  timer start/pause/reset and restoration, and live ticking.
- Toast family resources, palettes/fonts, popup lifetime and dismissal, and
  narrow layouts; history detail navigation, icons, and the introduction.
- Selectors under long localized text and keyboard input, real share action
  bounds, and media widget bounds plus preparation of the bundled clip.

The phone run `20261008T110902486Z-ea90844c` passed 55 methods and skipped
`wideSettingsKeepCategoriesBesideTheSelectedPage`, which requires a genuinely
wide device. It used the first binary pair. The tablet integration result is
`20261008T113610311Z-c7803cb2`: all 56 passed in 170.366 seconds using the second
binary pair. Together with the six game methods per device, these final scopes
contain 61 passing methods and one width-dependent skip on the phone, and 62
passing methods on the tablet. All final runs recorded zero restoration errors.

The earlier tablet integration run `20261008T112709549Z-48fc70b6` failed one
method: the 195,805-character AOSP resource notice still showed its loading text
at the ten-second limit. The other 55 methods passed. Without application code
changes, `SourceNoticesTest` then passed both methods in 6.797 seconds in
`20261008T113348307Z-030ca7ae`; the subsequent complete 56-method run also passed.
The timeout was not reproduced. Its failed run remains part of the evidence;
these results do not establish that loading can never time out.

The final seven-tap entry checks passed with the third binary pair:

| Device | Run ID | Scope | Result |
| --- | --- | --- | --- |
| Phone | 20261008T115218262Z-ee8e5d26 | Countdown Toast, entry navigation, hidden screen | Nine passed in 9.128 seconds |
| Tablet | 20261008T114152583Z-b92b99db | Same checks plus three Settings navigation checks | Twelve passed in 13.695 seconds |

The tablet run was completed by the separate feature-development chat before
device ownership returned to this verification task. Its manifest and report
were reviewed. The phone's previous locked attempt failed all nine checks and
is retained separately; it is not a passing run. Both final scopes recorded zero
restoration errors. These counts are additional executed scopes, not a total of
unique methods across all runs. The [hidden-screen guide](componentory-easter-egg.md)
records their configuration and the superseded three-tap implementation.

## Final installation and device restoration

Both devices received the final seven-tap debug app through `adb install
--streaming -r`. Each install returned Success, and the installed base APK's
SHA-256 matched the third pair above. The final application change is `47020bb`;
later verification scripts and documentation do not change that APK.

The phone's original five-minute screen timeout and fixed portrait settings
were restored. The tablet's original ten-minute timeout and automatic rotation
settings were restored. Both devices retained their original system-following
app locale and animation scales of 1.0. After `KEYCODE_SLEEP`, each device
initially reported `screenState=SCREEN_STATE_OFF`. The settled power state was
Dozing on both devices; the phone's display reported `DOZE_SUSPEND` and the
tablet's display reported `OFF`, both with brightness 0.0. Their original ambient
display settings were preserved. Keyguard later reported `SCREEN_STATE_ON`
despite these sleeping displays, so final confirmation uses the power and
display states rather than keyguard alone. A private delivery record retains
the successful installations, installed hashes, checked settings, and both
initial and settled screen states.

## Limits

These are selected behavior checks, not a claim that the entire instrumentation
suite or every component/family combination ran. Screen screenshots are visual
evidence; only the listed input/state assertions establish interaction results.
Full game victories, all logo gesture chains, cat feeding/job delivery,
notification prompts, widget pinning, controls providers, selected dream
services, and autopilot completion were not exercised. External project-link
intent destinations/browser selectors were checked through interception;
end-to-end navigation in a real external browser was not checked.

Historical original-OS runs and captures remain necessary for original UI
accuracy. These debug checks do not publish a Play release or replace older
signed release artifacts.
