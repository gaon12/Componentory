# Survivor implementation verification: October 9, 2026

## Implemented direction

Android Survivors is one landscape survival roguelite. Android source releases
supply mixed item, enemy, boss, and effect artwork. They do not select levels,
difficulties, player characters, prices, or score multipliers. The player uses
Neko and chooses a starting weapon. The API 1–37 catalog records source coverage.

The implementation includes movement and manual skills, automatic attacks,
three paused growth choices, four weapon and four support slots, four combination
evolutions, the twenty-minute boss schedule, results, shared permanent upgrades,
support unlocks, checkpoints and explicit resume, local records and two-build
comparison. Ranked challenges omit permanent power, offer all equipment, and
use the shared `survival-v2` seed. The existing seven-tap entry and four-to-one
native toast countdown remain in place.

The [game document](survivor-game.md) retains each focused change and its ordered
checks. [Play Games setup](survivor-play-games.md) explains registration, public
build IDs, score ownership, retries, and the remaining real online checks.

## Executed checks

Changes followed code, formatter/lint, affected tests, diff review, and focused
English commits. Documentation-only changes used text/link review, formatting,
and `git diff --check`. The final game source verifier matched original and
bundled bytes for 32 entries. The port verifier still matched 834 retained files,
including 648 unchanged resources. Spotless, debug lint, and the Python verifier's
Ruff checks passed. The app JVM suite contains 218 passing tests. Both debug APKs
built; the evidence helper passed 42 checks.

| Physical scope | Phone SM-S731N, Android 17 / API 37 | Tablet SM-X800, Android 16 / API 36 |
| --- | --- | --- |
| Latest full interaction scope | 31 tests, `20261008T162412219Z-f0f21843` | 31 tests, `20261008T162532469Z-a47df142` |
| Final circular artwork and cache scope | 2 resource-only tests, `20261008T165129511Z-81398a61` | 2 resource-only tests, `20261008T165157944Z-475ad13d` |
| Moving stress fixture, ten real seconds | 600 ticks, 600 measured frames, P95 12.28 ms | 600 ticks, 600 measured frames, P95 17.64 ms |

The interaction scopes cover entry/countdown, Settings return, private Activity
recreation, independent movement/skill pointers, pause, growth choice/resume,
save integration, records comparison, atomic storage, ownership binding,
unconfigured rankings, and policy/notices. They used the APK before the circular
artwork correction. The final two-test scopes verify that correction through
actual Drawable rasterization and cache reuse without an Activity or input.
Do not combine these as one full interaction run on the final binary.

Stress fixtures used 150 mixed-source enemies, four evolved weapons, and
invulnerability. Frozen and moving variants passed. Captures were reviewed for
real retained sprites, the HUD, and controls. They are synthetic stress fixtures
on the installed OS, not completed normal runs or original historical captures.
Run manifests retain APK hashes, OS fingerprints, target SDK, theme, display,
font scale, app locale, and restored animation settings.

## Device lifecycle smoke checks

ADB smoke checks used the `98e1e2c` APK before the artwork correction, Korean
text, and the existing system appearance. The phone used its actual landscape
game window. The tablet used portrait and landscape system rotation settings
because its large window can ignore the Activity orientation request.

- On the phone, a live battle was paused and the package was force-stopped. Its
  PID disappeared. Reopening through Settings retained the same run at tick 438
  in the lobby. Waiting did not advance it. Explicit Continue advanced the clock;
  moving to Home saved tick 507, and returning showed the pause menu.
- On the tablet, an actual portrait lobby disabled Start. Landscape enabled it.
  After a short battle, rotating to portrait retained tick 366 and disabled
  Continue. Returning to landscape waited at that tick until explicit Continue,
  which advanced the clock.
- Both devices were put to sleep during resumed battles. The phone saved tick
  601 and the tablet tick 579. Repeated reads while locked kept those values.
  Secure keyguard required the owner to unlock again. Unlock return interaction
  is still pending; it was not replaced by a simulated lifecycle assertion.

One initial smoke pause assertion read an older five-second checkpoint just
before the next checkpoint completed. The check was corrected to sample the
settled paused state. A first reentry script swiped outside the phone's landscape
viewport; bounds were corrected before successful process recovery verification.
These harness adjustments are not recorded as passing attempts.

## Device cleanup and final binary

The early battle UI fixture wrote six zero-reward test records on the phone and
three on the tablet. Each record's exact fixture fields and start/end timestamps
matched an owned instrumentation run. They were backed up and removed without
changing other app data. Later battle UI tests use isolated cache saves.
Manual smoke saves were backed up and the game baseline was restored after
checking the run IDs, locked ticks, and unchanged wallet/records. Imported Easter
egg saves, lab comparison records, and user preferences were not reset.

Both devices retain the final tested debug APK, verified by pulling the installed
base APK and comparing its SHA-256:

```text
454f48559fa35f0036a3a7b273dca03107bed3d1afc824deb9d3462585742ea1
```

The phone timeout was restored to 300,000 ms, rotation lock to 0, and user rotation
to 0. The tablet timeout was restored to 600,000 ms, automatic rotation to 1, and
user rotation to 0. App locales returned to the system default on both devices.
Both were returned to sleep. Local evidence and private backups remain ignored.

## Remaining verification

- Real PGS authentication, remote score submission/retry, daily/weekly/all-time
  queries, profile changes, and Console score protection require the owner's
  project, leaderboard, certificate registrations, and test accounts. None of
  these real online checks has passed yet. The current unconfigured build keeps
  SDK initialization disabled and normal/local play available.
- Secure unlock return requires another owner unlock. Pausing and saving while
  locked passed; the interaction after unlock remains unverified.
- The complete twenty-minute schedule passed through 72,000 accelerated model
  ticks. A full normal run, long thermal soak, and final balance across complete
  runs have not been verified on physical devices. The ten-second stress results
  do not establish those outcomes.
- Physical coverage is these API 36 and 37 devices. Minimum SDK remains 24, but
  this work does not claim new physical coverage for every supported OS release.
- The public policy and Play Data safety declaration need the actual configured
  release review before publication. Local commits and installed debug APKs are
  not a published app release.
