# Google Play release preparation

Requirements were checked against Google's official documentation on October 8
and 10, 2026. Console requirements depend on the selected developer account and can change.
Recheck the linked sources before each submission.

## Store assets and text

Upload-ready copy is in
[`distribution/google-play/listings`](../distribution/google-play/listings).
The five locales are `ko-KR`, `en-US`, `ja-JP`, `zh-CN`, and `zh-TW`; the proposed
default is Korean. Use the plain text files without adding Markdown fences.

Keep the listing and primary screenshots focused on Components, Compare,
Version history, and Settings. Hidden features should remain a discovery in the
app. Keep their captures in internal QA evidence. Privacy disclosures, reviewer
access instructions, and content-rating answers must still cover the actual
features in the release build.

| Field or image | Required specification | Prepared material |
| --- | --- | --- |
| App name | Up to 30 characters | `Componentory` in each locale |
| Short description | Up to 80 characters | `short-description.txt` |
| Full description | Up to 4,000 characters | `full-description.txt` |
| Release notes | Up to 500 characters per language | `release-notes.txt` |
| Store icon | 512 x 512, 32-bit PNG, at most 1,024 KB | `assets/icon.png` |
| Feature graphic | 1024 x 500, JPEG or 24-bit PNG without alpha | Five localized `assets/feature-*.png` files |
| Screenshots | At least two; JPEG or 24-bit PNG without alpha; each side 320–3840 px, longest no more than twice shortest | Actual phone captures in `screenshots/` and tablet captures in `tablet-screenshots/` |

For recommendation eligibility, Google requests at least four app screenshots
at 1080 x 1920 portrait or 1920 x 1080 landscape. Up to eight can be uploaded per
device type. Tablet/Chromebook guidance calls for four 16:9 or 9:16 captures,
1080–7680 px. Do not stretch phone images into tablet evidence. A preview video
is optional for this app and has not been created.

Sources: [listing fields](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en),
[preview assets](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en),
[icon masking and color space](https://developer.android.com/distribute/google-play/resources/icon-design-specifications),
[release preparation](https://support.google.com/googleplay/android-developer/answer/9859348?hl=en).

The Play icon retains the owner's flask and cube artwork with a full square
background. Play applies the outer mask and shadow. Feature graphics are vector
illustrations, not claims of captured app screens. Screenshots are real app
captures; their provenance, display overrides, and hashes accompany the files.
Do not portray recreated resources as original historical OS captures.

There are four Korean and four English phone captures at 1080 x 1920. The real
phone's physical screen is 1080 x 2340, so capture used a temporary display-size
override; density and font scale were unchanged. Images were encoded as RGB
without cropping or stretching. This Samsung build ignored SystemUI demo
broadcasts and retains its real status bar. Four Korean tablet captures use the
tablet's native 2800 x 1752 landscape display. Their aspect ratio meets the general
image limits but is not the recommended 16:9 presentation ratio. App preferences,
language, display overrides, rotation, timeouts, and animations were restored.
Other listing locales can initially use the English
phone images; they are not localized Japanese or Chinese UI evidence.

All 12 captures use the verified 1.1.0 release payload generated from the AAB,
re-signed with the existing debug certificate to preserve device data. This is
not Play delivery. The [release verification report](verification-release-1.1.0-2026-10-10.md)
and both capture manifests record source, device settings, and artifact hashes.

Regenerate artwork with Python, Pillow 12.2.0, and resvg-py 0.5.0:

```powershell
python -m pip install Pillow==12.2.0 resvg-py==0.5.0
python scripts/store/build-assets.py
python scripts/store/build-assets.py --check-only
```

Rendering uses locally available fonts. Review all five graphics after rendering
on another machine. The checked PNG files are publishing assets; app build outputs,
signing keys, passwords, and machine-specific recovery files remain outside Git.

## App identity and content declarations

| Console field | Value or decision |
| --- | --- |
| Package | `xyz.gaon.componentory`; cannot be changed for updates |
| App type | App |
| Category | Education, saved in Console because the app teaches UI components |
| Price | Free; selected when the Console draft app was created |
| Advertising | No advertising SDK or advertising ID permission |
| App access | Learning, normal play, and local records need no account; configured online rankings use a Google game profile |
| Website | `https://gaon12.github.io/Componentory/` |
| Privacy policy | `https://gaon12.github.io/Componentory/privacy.html` |
| Support | `gokirito12@gmail.com`, confirmed by the publisher and saved in Console |
| Feedback | `https://github.com/gaon12/Componentory/issues/new` |
| Content rating | IARC completed on October 10, 2026; Korea 3+, PEGI 3, USK 6+, ESRB Everyone |
| Audience | All six age groups, confirmed by the publisher and saved in Console; Teacher Approved program declined |
| Countries | All Google Play-supported countries, confirmed by the publisher; Console selection pending |
| Account deletion | No app account creation; account-deletion requirement does not apply |
| Government, finance, health | The app supplies none of these services |

Do not claim an IARC result before the Console assigns it. Saved local sample
inputs are not a social network or an in-app user-content feed. Links to public
GitHub issues use an external browser. Keep sensitive data out of issue reports.
Apply the confirmed countries and support contact in the actual account.
Personal addresses and verification documents must never be
committed to this repository.

Reviewers can browse Components, open a supported sample, compare two designs,
read Version history, and open Settings without credentials. Inline autofill
demonstrations additionally require Android 11+ and explicit keyboard/provider
selection in system settings. They use fixed fictional values and are optional;
return to the normal services after trying them. This is not a password manager.

## Changes after the published 1.0.0 package

The current branch adds compact settings rows, searchable license/document
pages, contributor credits, and 26 version-specific Easter egg ports with complete
games and hidden screens. Easter eggs have normal detail pages and original
logo-to-game gestures; Back returns to the app. Main-tab reselection resets the
selected tab to its default screen. The earlier signed APK/AAB, listing ZIP, and
screenshots remain artifacts of the published 1.0.0 source; they do not show or validate these later changes.
Version 1.1.0, code 3, has now been built and signed with the existing certificate.
Formatting, Debug/Release lint, 218 app JVM tests, and 108 release-payload UI tests
on each physical device passed. Current store text and 12 screenshots were
validated. The [verification report](verification-release-1.1.0-2026-10-10.md)
records the exact artifacts and remaining coverage gaps. Older release evidence
continues to describe only its own source and artifacts.

Optional cat controls, quick settings, widgets, screen savers, notification
permission, and legacy generated-image storage access now need to be considered
in review instructions. Cat game progress remains local. The Android 2.3 artwork
contains cartoon zombies and several entries include mini-games; reassess the
actual IARC content answers and intended audience when submitting the new build.
Follow the [content rating requirements](https://support.google.com/googleplay/android-developer/answer/9859655?hl=en)
and update the questionnaire when new content changes its answers.
IARC is complete for this draft app. It does not establish Play publication.

## Privacy and Data safety

The [canonical privacy text](privacy-policy.txt) is bundled unchanged in the app.
Settings > Privacy policy opens the full offline English document. Localized
summaries explain the document. The website build uses this same source, so the
public and packaged policies cannot drift through hand-edited copies.

The survivor client adds Internet permission and Play Games Services v2 22.1.0.
Learning screens, normal play, and personal records remain available offline.
Unconfigured builds disable the SDK automatic provider and guarded initializer.
Configured builds can authenticate an existing Google game profile automatically;
manual connection is available, and automatic profile creation is suppressed.
Scores, gamer identity, and Google SDK diagnostics/analytics must be assessed
under [PGS data disclosure](https://developer.android.com/games/pgs/data-collection).
The previous offline-only "no data collected" preparation does not cover a
PGS-enabled release. Version 1.1.0 uses project ID `0` and empty leaderboard and
ruleset IDs, so the guarded initializer and automatic SDK provider are disabled.
The prepared no-collection/no-sharing answers apply to this unconfigured artifact.
The no-collection/no-sharing Data safety declaration is saved in Console for
this unconfigured artifact. It has not yet been sent for application review.

There is no advertising SDK or developer server. Comparison inputs and technical
context, detailed survivor builds, wallets, and combat checkpoints stay private
on the device. Pending profile IDs prevent scores being reassigned automatically.
Cloud backup and app-managed device transfer remain disabled. User-chosen share
sheet exports and imported cat image saves remain documented in the policy.
Google service score deletion is managed through the player's game profile.

Register the project, certificates, testers, leaderboard, and score protection
using [the survivor setup guide](survivor-play-games.md). Real online checks remain
pending until Console IDs and registered accounts exist. Update the public policy
from its canonical source before publishing a configured release.

Google requires a public, accessible, non-PDF privacy URL and an in-app policy
even for apps without collection. Verify the published URL before submitting.
Sources: [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en),
[Data safety definitions](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

## Account gates and publishing

New phone/tablet apps and updates must target API 36 or later from August 31,
2026. This project targets API 37 and supports API 24+. Target compliance alone
does not establish release approval.

For personal accounts created after November 13, 2023, production access requires
a closed test with at least 12 testers continuously opted in for 14 days, followed
by an application for production access. Local ADB tests do not replace this
requirement. Internal testing is useful but does not count as the required closed
test. Identity/device verification and any Console account warnings must also be
completed by the account owner.

Sources: [target API policy](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en),
[personal account testing](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).

Use a signed Android App Bundle for Google Play. Keep the upload key separate
from the Play-managed app signing key. A locally signed APK and an APK delivered
by Play can have different certificates; do not expect an in-place update between
them. Never replace an existing app's registered upload key casually. New releases
need a version code greater than every previously uploaded code.

Source: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en).

## Signed build

The current prepared release is version `1.1.0`, code `3`; the older published
GitHub release is `1.0.0`, code `2`. Release tasks require
all four environment variables and reject an unsigned configuration:

- `COMPONENTORY_KEYSTORE_FILE`: private keystore path, resolved from the repository root if relative.
- `COMPONENTORY_KEYSTORE_PASSWORD`: keystore password.
- `COMPONENTORY_KEY_ALIAS`: registered upload-key alias.
- `COMPONENTORY_KEY_PASSWORD`: key password.

Load passwords from a private credential store rather than putting them in a
command, source file, or shell history. Without these variables, normal debug
builds remain available. Partial signing configuration is rejected immediately.

```powershell
.\gradlew.bat spotlessApply spotlessCheck :app:lintDebug :app:lintRelease --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process'
.\gradlew.bat :app:verifyReleaseSigning :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:bundleRelease :app:assembleRelease --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process'
```

This AGP configuration creates debug JVM tests; `testReleaseUnitTest` is not an
available task. Do not report it as passing. Verify release APK signatures with
SDK `apksigner`, AAB structure with official `bundletool`, and native packaging
with `zipalign -c -P 16 4`. Also check bundled native ELF alignment; a ZIP check
alone does not establish runtime compatibility on a 16 KB device.
See [Android's page-size guidance](https://developer.android.com/guide/practices/page-sizes).

Static 16 KB verification checks LOAD alignment and writable data against the
rounded RELRO protection pages. A raw RELRO end that is not page-aligned is not
itself a failure if writable data starts on a later page. Record runtime checks
separately; both connected devices currently use 4 KB pages.

The local first-registration upload key is stored outside the repository under
the current user's `.componentory/signing/` directory. Its password is protected
by Windows DPAPI for that Windows account. Keep the keystore and its password in
a secure backup; the encrypted XML alone cannot be decrypted on another computer.
If this package already exists in Play, use its registered upload identity instead
of registering this new key. Public certificates and fingerprints are not private
keys. A Play-generated app signing certificate can differ from this upload key.

GitHub release assets contain signed APK/AAB files and a store-material archive.
They do not contain keystores, encrypted credentials, passwords, or service-account
JSON. Play publication still requires an authenticated developer account and its
app-content declarations.

For local update testing, a universal APK can be generated from the release AAB
and signed with the existing local debug certificate. Its release payload stays
non-debuggable, while the matching local certificate permits an update without
deleting app data. This local test APK is not the upload-signed downloadable APK
and is not an APK delivered by Play. Never upload its debug key as the Play key.

The intended sequence is:

1. Verify the developer account, existing package, signing identity, public
   support email, audience, and distribution countries.
2. Complete formatting, lint, affected tests, and focused commits.
3. Build and verify signed release artifacts, preserve their hashes, and inspect
   current screenshots and privacy copy.
4. Push Git and publish the project/privacy website. Check public URLs.
5. Upload the AAB and listing assets, complete app-content questionnaires, and
   submit the permitted track. Record upload, review, and publication separately.
6. Install the final current build on the connected devices and put their screens
   to sleep after all verification.

An uploaded bundle, a saved draft, or submission for review is not a live store
release. Record actual Console status. Production review and personal-account test
gates may prevent immediate public availability.

## Public website

`scripts/store/build-website.py` creates `.local/site/` from the canonical policy.
The `pages.yml` workflow publishes only that small public site. Enable Pages with
GitHub Actions as the publishing source. Generated HTML stays outside Git.

```powershell
python scripts/store/build-website.py
```

Source: [GitHub Pages workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

## Published GitHub releases

[GitHub v1.1.0](https://github.com/gaon12/Componentory/releases/tag/v1.1.0) is
published at tag `2cb4f190ff3a8a61a099fed8acdd55ff0cbfa365`. Its APK/AAB were
built from `8cc1c2f6df6c3375abb2a8207ad81f6f29ae03fd`; the later tag commit adds
only store assets and documentation. It provides `Componentory-1.1.0.apk`,
`Componentory-1.1.0.aab`, `Componentory-1.1.0-store-materials.zip`, and
`SHA256SUMS.txt`. All four server-reported SHA-256 digests match the local files.
The [verification report](verification-release-1.1.0-2026-10-10.md) records
application checks, device restoration, screenshots, and outstanding work.

[GitHub v1.0.0](https://github.com/gaon12/Componentory/releases/tag/v1.0.0) is
published at tag `2d6082ec8a717437500ee8a530a24434d28fcd9c`. It provides the
upload-signed APK/AAB, `Componentory-1.0.0-store-materials.zip`, and
`SHA256SUMS.txt`. Server-reported SHA-256 digests match every local upload.
The project website and canonical privacy page are also live.

The authenticated Google Play draft exists. All current app-content declarations
are saved, and Console reports no remaining declarations needing attention:
privacy policy, no advertising, unrestricted app access, IARC, all six audience
age groups, no data collection/sharing, no advertising ID, and no government,
financial, or health features. Education category, support email, and website
are saved. All supported countries are confirmed by the publisher.
Listing, signing, AAB upload, and track submission are pending.
See [app-content.json](../distribution/google-play/app-content.json) for the
recorded Console state. Prepared answers are not submitted declarations.
The downloadable archive preserves the requirements and verification
state when the release was created; consult this guide for later status updates.
