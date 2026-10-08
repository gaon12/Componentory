# Version-specific Easter egg ports

The app's own hidden destination is described in the
[Componentory Easter egg guide](componentory-easter-egg.md). It opens from repeated
App version taps in Settings. The Android release ports below remain catalog
entries with their own source and runtime requirements.

The Components catalog has 26 Android release rows across 18 source families,
in addition to the 168 UI API entries. Search by release, nickname, translated
Easter egg name, or English. Each row opens gesture instructions, game and hidden
screen buttons, public Android integrations, a source link, and the current OS.
Selection survives activity recreation. Minor releases with shared source also
share that family's local game progress; separate rows do not imply distinct art.

## Retained coverage

| Android release rows | Source family | Game and additional features |
| --- | --- | --- |
| 2.3 | Gingerbread | Zombie artwork and artist credit; no separate mini-game |
| 3.0, 3.1, 3.2 | Honeycomb | Bee artwork and reaction; no separate mini-game |
| 4.0 | Ice Cream Sandwich | Flying Nyandroid and developer-preview logo |
| 4.1, 4.2, 4.3 | Jelly Bean | Movable BeanBag playground and dream service |
| 4.4 | KitKat | Interactive DessertCase mosaic, dream, and preview |
| 5.0, 5.1 | Lollipop | Full LLand flying obstacle game and preview |
| 6.0 | Marshmallow | Full multiplayer MLand, preview, and hidden shrug screen |
| 7.0, 7.1 | Nougat | Cat collection, feeding tile, lock-screen food dialog, jobs, naming, deletion, saving, sharing |
| 8.0, 8.1 | Oreo | Draggable octopus; 8.1 uses a separate Point1 logo entry |
| 9 | Pie | Animated logo and hidden drawing canvas |
| 10 | Q | Logo interaction and complete Quares nonogram engine |
| 11 | R | Dial unlock, cats, food/water/toy device controls, jobs, and collection |
| 12, 12L | S | Clock unlock, color bubbles, palette page/widget, and retained cats/controls |
| 13 | Tiramisu | Clock/emoji interaction, palette/widget, cats/controls, and beta logo |
| 14 | Upside Down Cake | Complete space simulation, steering, planet exploration, and landing |
| 15 | Vanilla Ice Cream | Space exploration, autopilot, and space dream |
| 16 | Baklava | Space exploration, autopilot, dream, and optional progress notifications |
| 17 | Cinnamon Bun | Its logo, full space game, autopilot, dream, and optional progress notifications |

The imported manifest retains 50 activities, 13 Android-bound services, and two
widget receivers. Auxiliary activation activities remain reachable through the
retained game flow; they are not replaced with a static logo-only demonstration.
Developer-preview screens are labeled as previews within their source family.
No pre-2.3 or future 17.1 placeholder rows are invented.

## Runtime requirements and user choices

The host app requires API 24+. Logo ports run on that supported range. Android
11–13 cat galleries and device-control providers require API 30. Palette pages
and widgets require API 31; framework dynamic colors come from the installed
system wallpaper. Earlier logo ports can use the retained static color fallback.
Android 16 rich progress notifications require API 36; Android 17's style requires
API 37. The underlying space game remains available on older supported devices.

The detail modal offers direct game screens in addition to the original code's
gesture flow. Widget and tile requests use public Android APIs when available;
older tile hosts receive manual quick-settings instructions. Cat controls are
enabled for selection from the device's controls panel. Screen saver selection
opens Android settings. The manufacturer may omit a chooser or launcher feature,
in which case the app explains the limitation. Each optional integration can be
disabled here, and state is refreshed after returning from a native game.
Original unlock gestures can also change the same components' enabled state.

No app asks for signature binding permissions. Android protects the exported
services with its own binding permissions. The Android 7 tile preferences activity
is exported only behind BIND_QUICK_SETTINGS_TILE so SystemUI can open it. Other
activities remain private. Optional notification permission is requested explicitly;
cat visits follow Android job and battery limits. Saving/sharing generated cat
images may request legacy storage permission on API 24–28. See the
[canonical privacy policy](privacy-policy.txt) for retention and exports.

## Sources, adaptations, and accuracy

The complete sources and resources come from
[Hu Shenghao's Android Easter Eggs](https://github.com/hushenghao/AndroidEasterEggs/tree/63d3e4549efbd6f714f6c19764d9520906c18c57),
pinned to `63d3e4549efbd6f714f6c19764d9520906c18c57`. This is an AOSP-derived
compatibility port. Its author and original AOSP copyright headers remain in the
library, with Apache 2.0 attribution available offline in the license modal.
Settings distinguishes direct Componentory contributors from upstream credits.

The [module guide](../eastereggs/README.md) records dependency versions and
adaptations. The [provenance inventory](../eastereggs/provenance.json) records
834 original/imported file hashes, 648 unchanged resource files, the source pin,
and generated manifest hash. Git preserves imported bytes without line-ending
conversion. Adaptations cover the shared R namespace, guarded public APIs, API 31
layout qualifiers, static palette fallback, stable release labels, cat adapter
positions/deletion targets, and missing storage permission requests.

Public AOSP manifests were also checked for
[Android 16](https://android.googlesource.com/platform/frameworks/base/+/android-16.0.0_r1/packages/EasterEgg/AndroidManifest.xml)
and [Android 17](https://android.googlesource.com/platform/frameworks/base/+/android-17.0.0_r1/packages/EasterEgg/AndroidManifest.xml).
Those tags establish source availability, not execution on an original OS.

These are explicitly labeled code ports, not original historical captures. Fonts,
emoji glyphs, framework icons used by puzzles, system dialogs, wallpaper palette,
notification delivery, and integration surfaces belong to the installed OS.
Retained artwork and code do not establish original appearance or behavior.
Original OS builds, visual captures, and interaction runs remain required for
that claim. Current verification is recorded
[separately](verification-easter-eggs-2026-10-08.md).
