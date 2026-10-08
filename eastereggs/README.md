# Android Easter egg code ports

This Android library retains the complete game and auxiliary-screen sources from
18 egg modules in [Android Easter Eggs](https://github.com/hushenghao/AndroidEasterEggs),
pinned to commit `63d3e4549efbd6f714f6c19764d9520906c18c57`. Hu Shenghao maintains
that Apache 2.0 project. AOSP copyright headers and its complete license are retained.

These are AOSP-derived code ports running on the installed Android OS. They are
not original historical OS captures. Fonts, system dialogs, dynamic colors,
notification delivery, device controls, and launcher behavior belong to the current
OS. Actual historical builds remain necessary to claim original behavior.

The import includes logo gestures, Nyandroid, BeanBag, DessertCase, LLand, MLand,
Neko collection and feeding services, Ocquarium, painting, Quares, color chips and
widgets, space exploration and autopilot, and the corresponding dream services.
Developer-preview screens are retained as additional screens. Fake pre-2.3 entries,
future-version metadata, the upstream host app, Hilt registration, and screenshot
fixtures are excluded. Shared release families must not be presented as distinct
artwork when the pinned source supplies the same code.

## Adaptations

`provenance.json` records original and imported SHA-256 hashes, source paths,
component declarations, and changes for every retained file. Resource bytes,
including images and nine-patches, are unchanged. Values filenames are prefixed
to coexist in one module; resource names remain unchanged. Each original manifest
is retained in `upstream-manifests`. The merged manifest keeps activities private except the Android 7 tile preferences
entry, which Android opens through a binding-permission-protected activity. System
services retain their Android binding permissions. Optional
widgets, controls, tiles, and dreams begin disabled.

Imports use a shared resource namespace. Only the used inset mask is retained
from an upstream helper that called private AndroidX methods. Rounded corners use
a guarded public Android 12 API. Reading the full SDK field is guarded on Android
16 and later. Space-game labels retain their release dessert code. Below API 31,
system colors use retained static fallbacks instead of a synthesized wallpaper
palette. Cat adapters also resolve live positions, retain the chosen deletion target, and
request storage permissions only when missing. API 31-only layouts keep their
original bytes in layout-v31. Scoped lint exceptions preserve platform views and
platform tinting; no broad API-error baseline is used. All changes are recorded.

The host app must gate each entry point and integration by its required API and
let the user opt into Android-managed integrations. Rich progress notifications
require the API expected by the corresponding game; older devices can run space
exploration without that notification style. No Internet permission is introduced.

## Dependency pins

The module uses the repository's Compose BOM 2026.02.01 / Material 1.10.4, plus
AppCompat 1.7.1, Core KTX 1.16.0, Activity Compose 1.8.0, Lifecycle Runtime KTX
2.6.1, RecyclerView 1.3.2, Startup 1.2.0, Window 1.3.0, DynamicAnimation 1.1.0,
and Okio 3.9.1. These Apache 2.0 dependencies are ports' implementation tools,
not evidence that an old OS supplied Compose or AndroidX widgets.

## Verification

Run `python scripts/verify_easter_egg_port.py`. Add `--upstream <checkout>` to
also verify every original byte against the pinned upstream Git revision.
Run `:eastereggs:lintDebug :eastereggs:testDebugUnitTest` for Android checks and
real game-model tests. Vendored formatting is retained for readable provenance;
new host code, tests, and Gradle files follow the repository formatter.
