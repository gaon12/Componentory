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
