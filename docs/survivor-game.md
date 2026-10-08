# Android resource survival game

The hidden destination will host a landscape, twenty-minute survival game.
All 37 Android API identities are selectable. Minor releases share source
families. Version numbers never multiply damage, health, prices, or scores.
Classic identities before Android 2.3 use the retained Android 2.3.7 controls.
Android 4.4W uses retained Holo controls and does not invent a Watch Easter egg.

The game reuses AOSP control artwork and the pinned Easter egg ports.
These resources are game artwork on the installed OS, not original OS captures.
The resource manifest links each image or Drawable to its existing provenance.
Generated cat and octopus sprites retain the imported drawing code's attribution.
Rasterization, scaling, rotation, and game animation are host adaptations.

## Delivery and checks

Deliver focused changes for the resource catalog, landscape entry, battle,
upgrades and evolutions, waves and bosses, saves and progression, records, and
Play Games integration. For each change, format and lint before running tests,
then review and commit. Document actual results at each milestone.

Play Games project and leaderboard IDs have not been supplied. Offline play
must remain usable. An unconfigured or mocked client is not a successful
online score submission.

### Resource catalog milestone

Spotless apply/check and app debug lint passed in the original checkout before
these unchanged files were copied into the isolated game worktree. Three
standalone JUnit catalog tests passed, and the resource verifier checked thirty
source files for twenty-five artwork entries. This is a resource and model check;
no gameplay, rendering, or online behavior has been verified at this milestone.

### Landscape lobby milestone

The seventh Settings tap launches a private landscape Activity for result.
The parent keeps its saved Settings page and scroll position. The lobby can
select every API identity and displays the actual shared artwork source.
Portrait windows show a rotation prompt and disable battle start actions.

Spotless apply/check and app debug lint passed. The complete working-tree JVM
suite passed 189 tests: 183 existing/catalog tests and six independent battle
model tests that will be committed separately. Both debug APKs compiled.
Physical artwork, launcher, and lobby interaction checks remain pending while
another validation task reserves the devices. Compilation is not an interaction
result. The new tests will run before final delivery.

### Basic battle milestone

All identities now start a fixed-tick battle with equal base health and family
weapons and skills. A native Canvas reuses the cached source sprites. Movement
normalizes diagonal input, automatic attacks consume bounded projectile buffers,
and skills share a twenty-second cooldown. Portrait, Back, and Activity pause
stop the clock and clear held controls. The lobby details scroll on short windows.

Spotless apply/check and app debug lint passed. The JVM suite passed 189 tests,
including six combat cases for movement, deterministic ticks, automatic attacks,
skills, ranked setup, and equal common upgrades across all identities. Both debug
APKs compiled, and thirty source-file hashes still matched. Device artwork and
interaction tests are ready but require the locked phone and tablet to be unlocked.
No device interaction result is claimed here. Waves, growth, saves, and online
submission are still subsequent milestones.
