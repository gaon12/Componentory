# Survivor Play Games setup

The Android client uses Google Play Games Services v2 22.1.0 from Google Maven.
The current source has no Console project or leaderboard IDs. Builds without
valid configuration disable SDK initialization and keep normal play and local
records available. Local or mocked tests do not verify an online submission.

## Register the project

1. Create or select a Play Games Services project in Play Console. Record its
   numeric game services project ID, not a Google Cloud project name.
2. Link Android credentials for package `xyz.gaon.componentory`. Register the
   SHA-1 fingerprint of each certificate actually used to install the app:
   local debug, any direct test release, and Play App Signing when installed
   from a Play test track. The upload certificate may differ from the certificate
   on a Play-delivered APK. Do not upload a keystore or password to Git.
3. Add the intended Google accounts as PGS testers. Register an Android app
   credential for each required signing identity; a correct package with the
   wrong signing certificate will not authenticate.
4. Create one leaderboard for `survival-v1`. Use integer scores, zero decimals,
   and larger scores as better. All starting weapons share this leaderboard.
   Daily, weekly, and all-time views use the same leaderboard ID.
5. Check that leaderboard tamper protection is enabled. It is managed by Google;
   this app does not provide a separate server that replays every combat run.
6. Publish the required PGS configuration for the chosen test audience. Publishing
   PGS configuration is separate from publishing an app release.

Official references: [Console setup](https://developer.android.com/games/pgs/console/setup),
[platform authentication](https://developer.android.com/games/pgs/android/android-signin),
[leaderboards](https://developer.android.com/games/pgs/android/leaderboards),
[score protection](https://developer.android.com/games/pgs/leaderboards).

## Configure a build

Keep local configuration outside tracked files, for example in the user Gradle
properties file, or pass these public IDs at build time. No OAuth client secret
or service-account key is needed by this Android client.

```powershell
.\gradlew.bat :app:assembleDebug `
  -PplayGamesProjectId=YOUR_NUMERIC_PROJECT_ID `
  -PplayGamesLeaderboardId=YOUR_LEADERBOARD_ID `
  -PplayGamesLeaderboardRuleset=survival-v1
```

Replace the placeholders with the Console values. Empty IDs leave rankings
unavailable. The declared leaderboard ruleset must match `GameCatalog.RULESET`;
otherwise the app does not initialize the SDK. When combat balance or score
rules change, create a new leaderboard and update the ruleset and common seed.
Never reuse the old board ID for different conditions.

The SDK automatic initializer is removed from the merged manifest. Application
startup explicitly initializes it only for a configured build. Automatic profile
creation prompts are suppressed; the player can choose Connect game profile.
An existing game profile can authenticate automatically in a configured build.
Profile changes are managed in Android/Play Games settings.

## Verify with real test accounts

- Record app version, ruleset, project and board IDs, package, APK hash, installed
  certificate fingerprint, device OS/build, and tester account identity locally.
  Keep private account details outside Git.
- Connect a registered profile and open all three native leaderboard periods.
- Complete a ranked death and victory; check immediate submission and the actual
  remote score under that profile. A lower score may not replace a personal best.
- Disconnect networking, finish a ranked run, and check that its score remains
  queued. Reconnect and verify retry plus the remote board, not only a UI message.
- Switch to a second registered profile. Pending scores owned by the first must
  stay pending. Switch back and submit them. Unbound offline scores need the
  explicit Submit offline scores action; it must never reassign owned scores.
- Confirm that normal runs and abandoned challenges never submit online.
- Confirm that old-rule queues never reach the current board. Their detailed
  records remain local; this first client has one current ruleset board mapping.
- Recheck Console score protection and store Data safety declarations before
  enabling the feature for a release audience.

The queue stores attempts and retry time. Automatic retries run from the lobby
once per minute, with exponential failure backoff up to one hour. Manual retry
bypasses the delay. Server acceptance followed by a local interruption can cause
an idempotent score retry; local record and currency rewards still occur once.
Detailed equipment builds and combat checkpoints are not sent to the board.

Google documents SDK identity, analytics, and diagnostics processing in its
[PGS data disclosure guide](https://developer.android.com/games/pgs/data-collection).
Use the actual configured artifact and observed behavior for the publisher's
Data safety answers. The earlier offline-only declaration does not cover a
PGS-enabled release. Update the public policy from the canonical source before
publishing that release.

## Verification status

Real authentication, remote score submission, remote period queries, and Console
score protection are pending: the owner has not created the project or IDs.
Local queue tests, unconfigured-build tests, and compiled SDK calls are separate
implementation evidence. Do not mark the online feature complete until the real
checks above have passing evidence.
