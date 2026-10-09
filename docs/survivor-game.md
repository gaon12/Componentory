# Android resource survival game

## Game direction

This is one landscape survival roguelite. Android releases supply artwork for
items, enemies, bosses, and effects inside the same run. They are not selectable
game levels, difficulties, or characters. The player uses the retained Neko
artwork and chooses a starting weapon. Health and combat growth belong to the
run and equipment, never to an Android version number.

The internal API 1–37 catalog describes source coverage only. Minor releases
share retained artwork families. Missing early files use retained Android 2.3.7
controls; Android 4.4W uses Holo controls without inventing a Watch Easter egg.
Keep those substitutions visible in source information.

## Reference games and decisions

The developer descriptions of [Vampire Survivors](https://store.steampowered.com/app/1794680/Vampire_Survivors/),
[Brotato](https://store.steampowered.com/app/1942280/Brotato/), and
[Halls of Torment](https://store.steampowered.com/app/2218750/Halls_of_Torment/)
were reviewed on October 8, 2026. Vampire Survivors centers on surviving crowds
and choices that grow the build. Brotato uses automatic weapons, short runs,
and equipment choices around enemy waves. Halls of Torment emphasizes new
ability and item combinations during a run, with distinct boss patterns.
These descriptions inform the combat loop; their art and code are not imported.

Our loop is movement and dodging, enemy defeats, experience collection, three
random growth choices, equipment synergy and evolution, then timed bosses.
Each run builds its equipment anew. Local currency supports limited permanent
progression between normal runs. Ranked runs share a seed and omit permanent
power. The approved duration remains twenty minutes, with bosses at five,
ten, fifteen, and twenty minutes.

The first equipment set combines classic buttons and sliders, Holo switches
and progress artwork, Jelly Bean, Neko, and the Oreo octopus. Different release
families can appear as enemies together. Enemy strength is determined by its
combat role and elapsed time; source release is visual metadata.

## Source accuracy

The game reuses AOSP control artwork and pinned Easter egg ports. These are
artwork rendered on the installed OS, not historical OS captures. The resource
manifest links originals, hashes, commits, paths, notices, and host adaptations.
Generated cat and octopus sprites retain the imported drawing code attribution.
Rasterization, scaling, rotation, and game animation are host adaptations.
The original files remain separate and unchanged. Platform resources are not
attributed to Compose or Material libraries.

## Delivery and verification

Deliver focused changes for sources, landscape entry, combat, growth and
evolutions, waves and bosses, saves and progression, records, and Play Games.
For each change, format and lint before tests, then review and commit. Document
actual outcomes and do not treat compilation as an interaction test.

Play Games project and leaderboard IDs have not been supplied. Offline play
must remain usable. An unconfigured or mocked client is not a successful online
score submission. Prepare Console registration instructions and distinguish
local adapter tests from future real submissions.

The [October 9 verification report](verification-survivor-2026-10-09.md) summarizes
current device checks, installed binary identity, cleanup, and pending work.

### Game presentation checkpoint

The owner said the hidden game should start with one Start tap, look like a
real game, and run fullscreen. Five focused changes made this:

- **Fullscreen.** `SurvivorActivity` hides both system bars with transient
  swipe behavior. It hides them again when focus returns, and it draws into
  short-edge cutouts.
- **Title screen.** An animated backdrop of drifting Android artwork, the
  Neko player, and a large START button. Start begins a normal run with the
  default weapon. The weapon chip, Upgrades, Records, and Ranked open separate
  panels. The ranked challenge now starts from the Ranked panel.
- **Arena.** A checker floor with a glowing edge, shadows, auras, glows, a
  blink and red edge after a hit, and a vignette. Sprite sizes changed only
  visually; collisions did not change.
- **HUD and menus.** The arena fills the screen under a floating HUD: level
  badge, HP and experience bars, timer, boss bar, kill count, a round pause
  key, and a skill key with a cooldown ring. Level-up uses three cards with
  NEW and EVOLVE badges. Pause is an in-window panel instead of a dialog
  window.
- **Game over.** An outcome banner, time, kill, score, and reward tiles, the
  final build, and a Play again button that starts the same mode and weapon.

Game rules, saves, scores, and Play Games behavior did not change. Shared
palette, buttons, panels, bars, and the backdrop live in `GameStyle.kt`.

These changes were checked on an Android 15 (API 35) x86_64 Pixel 7 emulator
with software rendering, not on the physical phone and tablet above. Spotless,
debug lint, 218 JVM tests, and both APK builds passed for each commit. The
lobby, save, ranking, records entry, battle, canvas clip, and artwork tests
passed there, including a new Play again test and a one-tap Start test.
Screenshots of real play were inspected in English and Korean.

Two emulator failures had the same cause and are now fixed. A landscape game
and a portrait screen rotate the display when one covers the other, and the
rotation recreated the Activity that was waiting behind:

- `leavingTheForegroundRequiresAnExplicitResume` could not find `game_resume`.
  The test's portrait cover Activity recreated `SurvivorActivity`, and the
  running battle was lost. Real players hit the same bug after visiting a
  portrait home screen: they returned to the title screen with Continue
  instead of the paused battle. `SurvivorActivity` now handles orientation
  and size changes in place.
- The `ComponentoryEasterEggScreenTest` return checks failed on and off. Closing
  the game rotated the display back and sometimes recreated the portrait
  caller, which dropped the test's own caller content. The real Settings
  screen restores its state after that recreation, so only the test changed:
  its caller now uses the game's orientation and waits for the result.

Emulator run `20261009T071651344Z-33c43fd1` passed the Easter egg screen,
Easter egg navigation, and battle tests, 13 tests. Eight repeated runs of the
Easter egg screen test also passed, while six of seven failed before the fix.

One emulator result is still open:

- `GamePerformanceTest` cannot measure frame time on a software renderer. The
  moving case reported a 261 ms P95, and the frozen case drew no frames.

### Physical device runs for the presentation checkpoint

The game and Easter egg device classes, 27 tests, then ran on both physical
devices with the code from `fd5f930` and `0bfba41`:

- Tablet SM-X800, run `20261009T083859330Z-e36c71a9`: 25 passed. Frame time
  P95 was 25.0 ms for the moving crowd and 23.4 ms for the crowded artwork,
  below the 50 ms limit but slower than one 60 Hz frame.
- Phone SM-S731N, run `20261009T083856015Z-ba66cefe`: the first 20 passed,
  including both performance cases at 16.1 ms and 16.3 ms P95. The run then
  hung and was stopped.

The two tablet failures and the phone hang came from the Easter egg tests,
not the game. After the game closed, the tests checked Settings before its
window was back. On the phone, the two rotations also recreated MainActivity
several times in a row, and Compose test idling waited forever on a root that
never attached. A screenshot showed the app itself on the right Settings
screen. Commit `29c950e` makes those tests wait for Settings and open the
game in its own orientation. Afterwards, ten repeated phone runs of both
Easter egg classes passed, 80 tests in all.

The full 27-test runs then passed on both devices at `e36cebe`:

- Phone SM-S731N, run `20261009T091617733Z-6fd2a148`: 27 passed. Frame time
  P95 was 16.8 ms and 16.0 ms.
- Tablet SM-X800, run `20261009T092027990Z-79143c5e`: 27 passed. Frame time
  P95 was 24.8 ms and 24.9 ms, below the 50 ms limit but still slower than
  one 60 Hz frame on this tablet.

### Battle rendering performance

The tablet runs at 120 Hz with a 2800x1752 screen. Frame stage timing from
`dumpsys gfxinfo framestats` and simpleperf showed two causes. First, every
engine tick recomposed and measured the whole battle screen. Second, the arena
issued hundreds of separate draw calls, which made RenderThread and the GPU
slow. Two commits changed this without changing the picture:

- `d72055c` reads the tick only in the draw phase, keeps the HUD values in
  derived state, and gives the arena, HUD, and skill key their own layers.
- `d4bbd69` draws the checker floor with one shader, the grid with one call,
  glows from prerendered bitmaps, and enemies in layers of the same draw.

`GamePerformanceTest` frame time P95, moving and frozen crowd:

| Device | Before | After |
|---|---|---|
| Tablet SM-X800 | 24.8 / 24.9 ms | 19.7 / 20.9 ms |
| Phone SM-S731N | 16.8 / 16.0 ms | 13.1 / 14.9 ms |

All 27 device tests passed afterwards on the tablet
(`20261009T102438584Z-6affb9af`) and the phone
(`20261009T102439272Z-26bcfdb9`). The game advances 60 ticks a second, so it
draws about 60 frames a second on both devices. On the tablet, frame time
still exceeds the 8.3 ms budget of one 120 Hz frame. These frames are
pipelined, so the game still shows a new frame on every tick. What remains
is mostly recording and issuing the 150-enemy stress scene.

The new arena and HUD therefore need fresh physical phone and tablet runs
before they are called verified for frame time or interaction.

### Earlier checkpoints

The catalog verifier checked thirty original source files for twenty-five
artwork entries. Spotless apply/check and app debug lint passed. The basic
combat checkpoint passed 189 JVM tests and compiled both debug APKs. Its version
character selection was removed after the user clarified the intended resource
roles. The fixed-tick simulation and cached sprites remain useful foundations.

The initial phone interaction run `20261008T140131104Z-6b89e216` passed twelve
of fourteen tests. All registered original artwork drew nonempty cached sprites,
and movement plus skill worked with independent pointers. Two checks failed:
the manual Compose clock did not advance the result UI, and the old recreation test targeted the background caller instead of the
foreground game Activity. A later run was stopped because the manual test clock
blocked lobby scrolling. These failures are
retained as evidence; fixes must pass fresh runs before they are called verified.

### Mixed resource combat checkpoint

Android version character selection and API-based player fields have been removed.
The lobby selects a starting weapon. One run draws enemies from multiple release
families without using their release to compute power or collisions. Source
coverage still includes all 37 API identities as metadata.

Spotless apply/check and app debug lint passed before the JVM suite: 192 tests,
zero failures. Both debug APKs compiled. Fourteen device tests passed on each
physical device: phone `20261008T142759054Z-931590e8` and tablet
`20261008T142840016Z-582eab07`. The scopes include original sprite rasterization
and cache reuse, starting weapons, portrait constraints, independent pointers,
paused time, seven taps and native toast countdown, foreground game recreation,
and return to the saved Settings location. Run manifests retain exact binaries
and environment snapshots. They are gameplay checks on the current OS, not
historical appearance captures. Full waves, saves, and online scores are not
verified by these entry and basic combat tests.

### Growth and evolution checkpoint

Experience pauses combat for three distinct choices. Weapons and supports have
four slots each and five basic levels. Evolution requires weapon level five
and its paired support at least level three, replaces that weapon, and cannot
repeat. The first set includes charged button bursts, bouncing slider pierce,
Neko summons with periodic shielding, and orbiting octopus tentacles.

Progress shortens attack intervals, Jelly Bean increases weapon damage, Neko
increases experience, and octopus increases area and orbit reach. Each support
adds five percent per level. Fractional experience is retained so small drops
receive their common five-percent bonuses over time. Offers use a separate saved
random state from encounters. Remaining choice slots use recovery and currency
when too few equipment upgrades remain; three distinct fallback choices include
health, currency, and a smaller amount of both.

Spotless apply/check and app debug lint passed; 198 JVM tests passed. Both debug
APKs compiled. Fifteen scoped tests passed on each device, including growth
pause and resume: phone `20261008T143612806Z-4536f7cb` and tablet
`20261008T143654227Z-230125b0`. Model cases cover equipment limits, locked supports,
evolution prerequisites and replacement, exhausted offers, fractional experience,
distinct evolved actions, and independence from encounter randomness.

### Waves and bosses checkpoint

Bosses appear once at five, ten, fifteen, and twenty minutes. Their source art
is Oreo, Jelly Bean, KitKat, and Honeycomb; this order is not an Android release
progression. They fire hostile projectiles. The final spawn stops ordinary
spawns, and defeating the final boss wins. A simultaneous player death takes
priority. Pause and result screens show the final mixed equipment.

Spotless apply/check and debug lint passed before 203 JVM tests, all passing.
The model advanced 72,000 fixed ticks to check the full twenty-minute schedule,
bounded encounters, and final spawn behavior. This accelerated model check is
not twenty minutes of physical device play. Both APKs compiled. Eighteen scoped
tests passed on each device: phone `20261008T150640779Z-a780edb1` and tablet
`20261008T150733541Z-f47ece20`.

The physical rendering fixture used 150 frozen enemies with mixed source art,
four evolved weapons, and invulnerability. In ten real seconds both devices
advanced 600 ticks. The phone rendered 599 measured frames with a 13.20 ms
95th-percentile frame duration; the tablet rendered 600 with 18.68 ms. Captures
were inspected for actual sprites and visible health, time, and controls. This
fixture measures rendering load and is not a normal completed run. A native
Canvas background initially painted over the header; explicit clipping and a
neighbor-color regression test now prevent that. A background pause assertion
initially sampled before the lifecycle transition; it now samples after the
transition and checks that no simulation runs until explicit resume.

### Save model and atomic storage checkpoint

Game data uses a separate private `survivor/save.json` document. Explicit JSON
fields retain the fixed tick, both random streams, actors, projectile hit sets,
timers, equipment, pending growth choices, and starting permanent levels.
Malformed or unknown save schemas are rejected instead of silently resetting
the wallet. Completed records, rewards, and pending scores change together.
Repeated run IDs do not earn another reward. Stale checkpoints cannot restore
a completed run or replace a newer tick or growth level.

Common upgrades cost 50 currency times the next level and stop at level five.
Progress support is initially unlocked; each remaining support costs 100.
Prices are unrelated to source release. Rewards are one per ten ordinary kills,
one per elite, ten per boss, one per thirty survival seconds (capped at twenty
minutes), fifty for victory, plus growth currency. Abandoned runs retain their
earned local reward but never queue an online score.

Spotless apply/check and debug lint passed, then all 209 JVM tests passed.
Tests include checkpoint replay, pending choices, shared upgrades, budgets,
exclusive score counts, survival cap, duplicate completion, and corrupt schema
rejection. Both APKs compiled. The Android atomic-file rollback and stale-write
test passed on phone `20261008T151712453Z-72eb3b35` and tablet
`20261008T151741073Z-a0273fce`. Lobby integration and actual resume interaction
remain the next focused change.

### Progression and resume UI checkpoint

The lobby reads the private save and offers explicit continue or end actions for
an unfinished run. Starting a new run is disabled until that run is completed
or abandoned. Continue requires an actual wide window and the current ruleset.
Older-rule runs can be ended locally. The lobby shows currency, common upgrades,
and support unlocks; the result shows the recorded score and reward.

Combat takes immutable snapshots every five seconds and writes them outside
the frame loop. Lifecycle pause and disposal flush a final checkpoint. Restoring
an Activity shows the lobby rather than silently resuming combat. Save errors
pause combat and keep the previous document, with retry or close actions.
Normal coroutine cancellation is not reported as a storage failure.

Spotless apply/check and debug lint passed, the 209 JVM tests remained passing,
and both APKs built. Twenty scoped device tests passed on phone
`20261008T153414500Z-0801feb7` and tablet `20261008T153509495Z-c2ca961b`.
The resume UI test loaded a prepared checkpoint, waited for continue, displayed
its support build, ended it, and checked one record and reward. It is not a
process-kill recovery test. An earlier assertion read the pause dialog before
the manual clock advanced; a subsequent run was stopped because result scrolling
also needed clock progress. These test-clock issues were fixed before the fresh
passing runs.

### Personal records checkpoint

Records show the Neko player through its starting weapon and final mixed build.
Filters separate normal and ranked play, each ruleset, and each starting weapon.
The best completed score uses only victory or death records; abandoned records
remain visible. Two distinct runs in the same mode and ruleset can be compared
side by side, including starting permanent levels, seed, survival, exclusive
kill categories, result, reward, weapons, supports, and evolutions.

The shared score function uses ordinary kills times ten, elites times one
hundred, bosses times one thousand, survival seconds times five capped at 1,200
seconds, and ten thousand for victory. Android source version is never a score
input. Local records do not upload detailed builds.

Spotless apply/check and debug lint passed before all 211 JVM tests. An initial
lint error found a configuration read that could leave the date locale stale;
the record screen now reads Compose LocalConfiguration. Both APKs built and
twenty-one scoped tests passed on each device: phone
`20261008T154548597Z-f84dbe1e` and tablet `20261008T154644750Z-80fef990`.
The new model tests check scoring caps and mode/ruleset separation. The UI test
selects two builds, opens their comparison, and checks that changing mode clears
the selection and cannot combine it with the other mode.

### Play Games client checkpoint

The optional PGS v2 client keeps a durable score queue with retry timestamps.
Each ranked run retains its starting game profile. Submission checks the current
profile again; another profile cannot automatically submit those scores. Scores
earned without a profile require explicit binding. Only completed current-rule
challenges can reach the current board; normal and abandoned runs stay local.

Unconfigured builds remove the SDK automatic initializer and do not initialize
it at application startup. The lobby explains that online rankings are not yet
configured. The SDK third-party notices are retained byte for byte and available
in the source notice reader. Privacy and release documents describe the optional
Google identity, analytics, and diagnostics processing. Console registration and
real verification steps are in [the setup guide](survivor-play-games.md).

Spotless apply/check and debug lint passed before all 215 JVM tests. Both APKs
compiled. Thirty scoped tests passed on phone `20261008T160721385Z-7e434b63` and
tablet `20261008T160830998Z-a2ab2432`. Mocked queue tests cover failure, backoff,
retry, completed submission, missing profiles, changed profiles, and old rules.
Device tests cover ownership binding, disabled SDK initialization, the complete
offline policy reader, and retained notice hashes. An earlier source-notice test
expected four files before the two SDK notice files were added; its audited hash
list was updated before the fresh passing runs.

Real authentication, online submission, remote period queries, and Console score
protection remain unverified because the owner has no PGS project or leaderboard
IDs yet. Compiled SDK calls and mocked tests do not complete online verification.

### Mixed-source encounter rules

Ruleset `survival-v2` uses common challenge seed `20261009`. This separate ruleset
and its future Console board keep the changed combat conditions away from v1
scores. Source API numbers remain metadata throughout these changes.

The five-minute Oreo octopus fires radial volleys. At ten minutes, Jelly Bean
fires aimed fans that bounce twice from arena edges. At fifteen minutes, KitKat
fires rotating ribbons. The twenty-minute Honeycomb boss fires slow shots that
track the player. Boss health is 2,200, 4,400, 6,600, and 11,000 respectively;
these values belong to encounter time and role, not release age. The final boss
uses older artwork and has the largest health budget. Weapons, supports, and
evolutions still mix across source families in every run.

Moving enemies separate nearby bodies so crowds remain visible rather than
stacking into one sprite. Separation is deterministic and does not use source
version. Frozen enemies do not move through this separation step. Hostile
tracking aims at the player; friendly Neko summons aim at enemies. Hostile
bouncing and friendly bouncing pierce remain separate collision behaviors.

Battle UI tests use an isolated cache save, keeping test records and rewards out
of the player's private history. Both frozen and moving crowd stress fixtures
measure the real frame clock with four evolved weapons and mixed-source art.
They use invulnerability and are not completed normal runs or historical OS
appearance evidence.

Spotless apply/check and debug lint passed before all 218 JVM tests, including
distinct boss attacks, hostile tracking, edge bounces, and source-independent
crowd separation. The accelerated full twenty-minute schedule still passed.
Both APKs compiled. Thirty-one scoped tests passed on phone
`20261008T162412219Z-f0f21843` and tablet `20261008T162532469Z-a47df142`.
Both moving fixtures advanced 600 ticks and rendered 600 measured frames in ten
real seconds. The 95th-percentile frame time was 12.28 ms on the phone and
17.64 ms on the tablet. Frozen fixtures also passed. Moving captures were
inspected for retained artwork, visible health and time, and separate controls.
These short stress measurements do not establish long-run thermal performance
or final game balance across the complete duration.

### Circular progress source correction

Final source review found that the orbit weapon used a horizontal Holo progress
drawable. It now uses the actual [circular progress layer](https://android.googlesource.com/platform/frameworks/base/+/63ade05d76785975fc3292ca030abbaa1dda8891/core/res/res/drawable/progress_medium_holo.xml)
and its two retained ring images from Android 4.4.4_r2. A cached static frame is
rotated by the game. This is a game adaptation, not a claim of original progress
animation timing on an old OS.

Every game source entry now links its repository and pinned file URL and names
the preserved original path. Adapted XML and Java files have original Git bytes
in `data/survivor/originals`; unchanged imported images already preserve those
bytes in the port. Five Java hashes in the earlier port manifest were computed
from a Windows CRLF checkout. The game manifest retains those checkout hashes
separately and uses the original LF Git blob hash for source verification.
The verifier checks both original bytes and bundled game inputs. The wider
platform control manifest and imported Easter egg manifest remain separate.

Spotless apply/check, debug lint, and Ruff format/check for the Python verifier
passed. Both original and bundled hashes matched for all 32 source entries.
The unchanged port verifier still matched 834 retained files and 648 resources.
All 218 JVM tests remained passing, and both APKs compiled. Forty-two evidence
script checks passed, including rejection of interactive tests in resource-only
mode. The reviewed GameArtworkTest opens no Activity and uses no input; both
of its actual Drawable and cache tests passed in resource-only mode on phone
`20261008T165129511Z-81398a61` and tablet `20261008T165157944Z-475ad13d`.
The new circle assertion checks a transparent center with visible rings on both
sides. These are real rendering checks on the current OS; they do not verify
screen unlock interaction. The preceding 31-test interactive scopes used the
earlier APK before this artwork correction.
