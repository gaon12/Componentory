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
