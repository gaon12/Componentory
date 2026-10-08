# Navigation and Easter egg flow verification — 2026-10-08

This report covers the normal Easter egg detail pages, original logo-to-game
continuation, return to the app with Back, searchable license pages, and active
main-tab reselection. It supersedes older modal/navigation behavior only for the
scopes below. Earlier historical and release reports keep their original scope.

## Implementation checkpoints

- `cf916f0`: reselecting any active main tab returns it to its default screen.
  Switching to another tab preserves the previous visit. Saved comparisons and
  settings preferences remain stored.
- `df7e65b`: licenses open a searchable offline list and complete document pages.
  Titles and filenames are searchable without case sensitivity. Document Back
  restores the filtered list and scroll position, including after recreation.
- `701641e`: Android release rows open normal detail pages with one launch button.
  Original logo gestures continue into their games. Extra tools/previews remain
  separate. Games use the host task, and Android 14–17 logo entries use
  `FLAG_ACTIVITY_NO_HISTORY` so one Back returns to the matching detail page.

The source pin, native Java/Kotlin game code, and all imported resource bytes are
unchanged. Nine merged activity declarations no longer use `singleInstance`.
The generated manifest adaptation and its byte hash are recorded in
[provenance](../eastereggs/provenance.json).

## Build and source checks

Code was changed, formatted/linted, tested, and reviewed before each feature
commit. Final local commands used one Gradle worker, the in-process Kotlin
compiler, and `-Pkotlin.incremental=false`. These flags also apply to the build
and unit-test command:

```powershell
./gradlew.bat spotlessApply spotlessCheck :app:lintDebug :eastereggs:lintDebug --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process -Pkotlin.incremental=false
./gradlew.bat :app:testDebugUnitTest :eastereggs:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
python scripts/verify_easter_egg_port.py --upstream .local/android-easter-eggs-upstream
```

Spotless passed. App lint reported zero errors and 77 warnings. Port lint reported
zero errors, 439 warnings, and six informational notes. The app JVM suite passed
184 tests; the port suite passed two. The observed app suite includes three tests
from concurrent survivor-atlas work outside these navigation commits. That work
was preserved and is not validated as a playable game by this report.

The upstream verifier checked all 834 retained files against pinned revision
`63d3e4549efbd6f714f6c19764d9520906c18c57`, including 648 unchanged resource files
and 65 component declarations. No original document/license bytes were edited.

## Final APK pair

Both final scopes used the same frozen pair in `.local/egg-star-ime-apks`:

| Artifact | SHA-256 |
| --- | --- |
| App APK | `6a8d84f40ca567fca41f569ae33462ddecfa976f8672441f141c203024ad360e` |
| Instrumentation APK | `7db846a3a4d226d94a0979f87dc83ae18e6a2ab72ce37886269d9f28a8a38e0d` |

The APK metadata remains version name 1.0.0, version code 2, min SDK 24, and target
SDK 37. These are debug verification artifacts, not a new signed Play release.
The build snapshot contained the navigation changes before their focused commit
and concurrent immutable atlas code/resources. Manifests record the dirty source
snapshot and exact hashes; the concurrent survivor UI/game is outside this scope.

Streaming installs succeeded. The ignored local runner verifies the installed
base/test APK hashes before reusing them, rather than silently testing a different
build. Repository evidence and locale helpers are retained. Each run has a JSON
manifest and complete instrumentation output under `.local/device-runs/<run-id>`.

## Physical devices

| Setting | Smartphone | Tablet |
| --- | --- | --- |
| Identity | Samsung SM-S731N / r13s | Samsung SM-X800 / gts8pwifi |
| OS/API | Android 17 / API 37 | Android 16 / API 36 |
| Build | CP2A.260605.016 | BP2A.250605.031.A3 |
| Physical pixels | 1080 × 2340 | 1752 × 2800 |
| Tested logical window | 1080 × 2340, portrait | 2800 × 1752, landscape |
| Density / font scale | 450 dpi / 1.15 | 340 dpi / 1.0 |
| App appearance | SYSTEM, dark (`uiMode 0x21`) | SYSTEM, light (`uiMode 0x11`) |
| Device locale | ko-KR | ko-KR |

The behavior runner temporarily uses English app feedback, then restores the
original app language and Android app locale. The app locale before/following the
final runs is the system default `[]`. Exact fingerprints, display configuration,
provider themes, library pins, and animation values are retained in each manifest.
Native ports use their retained activity themes on these current OS builds.

## Final results

| Scope | Device | Run ID | Result |
| --- | --- | --- | --- |
| Original gesture chains | Phone | `20261008T132138198Z-85950aec` | 3 passed |
| Original gesture chains | Tablet | `20261008T132138889Z-5e0bfa16` | 3 passed |
| UI and existing game regression | Phone | `20261008T132417606Z-e321ce22` | 34 passed, 1 wide-only assumption skip |
| UI and existing game regression | Tablet | `20261008T132416911Z-ade1f80b` | 35 passed |

Together the final scopes passed 37 methods on the phone with one wide-window
assumption skip, and all 38 methods on the tablet. Custom capture/status messages
are not counted as additional JUnit tests. All four runner manifests have empty
restoration-error lists.

The three original-gesture methods cover 13 paths: Android 4.0, 4.1, 4.4, 5.0,
6.0, 8.0, 8.1, 9, 10, 14, 15, 16, and 17. They enter through the catalog detail
button, inject actual tap/hold/drag input, verify the same task, and send one Back
to assert return to that release page. Android 10 rotates and joins the original
logo before unlocking the puzzle. Android 17 draws through all 17 dots in one
continuous stroke, returns to the starting dot, releases to reveal the logo, and
holds that logo to reach space exploration. No private unlock counter or launch
method is changed by the tests.

The 35-method scope includes catalog detail/search/provider navigation, all four
main-tab resets, run preservation, adaptive navigation, license search and all
eight original documents, privacy behavior, and Componentory's own hidden-entry
navigation. It also opens 43 persistent native logo/game/tool/preview screens and
checks that Back resumes the host. Separate interaction assertions cover drawing
and clearing pixels, nonogram marks after recreation, multiplayer participant
changes, all four space flight sticks, and the transient shrug Toast/action.

Game fixtures preserve and restore imported component enablement and all app
shared preferences. The runner restores animation scales to their original 1.0
values. The native fixture enables real animator frames while those tests run.
The gesture scope saves 13 screenshots per device and the remaining game scope
saves 51; these captures support draw evidence, while input/state assertions prove
the interactions described above.

## Earlier attempts and limits

Earlier failures remain recorded, not relabeled as passes. Initial license search
assertions compared the editable query with its merged label; the final tests
assert only the editable value. A wide-layout category assertion was corrected to
scroll the preserved settings category list into view. An early main-tab reset
prototype failed recreation; generation-specific saveable provider keys fixed it.

The first combined egg/UI retry specified the wrong catalog-test package and
failed test initialization. Earlier native harness attempts also failed because
of incomplete Back event timestamps/source and a keyboard-show/dismiss race.
Android 17 additionally required its retained dot-drawing stage before the hold.
The final harness supplies valid keyboard events, waits for actual IME visibility,
and exercises that original stage. A separate ADB-server interruption happened
before any application tests began and was recovered before rerunning.

These results verify current-OS compatibility ports and the exercised app flows.
They are not original historical Android captures and do not establish all games,
notifications, launchers, widgets, screen savers, or older API levels as tested.
Android-managed integrations launched externally retain their OS navigation.
The existing signed 1.0.0 release files and store screenshots are not replaced by
these debug checks; see [release preparation](google-play-release.md).
