# Release verification: October 8, 2026

Version `1.0.0`, code `2`, package `xyz.gaon.componentory`, minimum API 24,
target API 37. Application signing/version configuration is `03ea995`; the
saved-run test correction is `607de45`. Final APK/AAB build metadata records
`607de45e90fe94b806e47581b23ab33f0c7cb544`. Later listing and documentation
commits do not change compiled application inputs.

## Build and artifact checks

Spotless apply/check and debug/release lint passed. Each lint variant has zero
errors and 63 retained warnings. All **171 debug JVM tests** passed. This AGP
configuration does not provide `testReleaseUnitTest`; its attempted invocation
failed task selection and is not a passing check.

Explicit private upload signing produced the APK and AAB. Missing signing
credentials fail with the expected message; partial configuration is rejected.
The guard uses a typed task and stores/reuses Gradle's configuration cache.
The earlier script-capturing task failed configuration-cache validation and was
replaced before the final checks.

| Artifact | SHA-256 |
| --- | --- |
| Upload-signed release APK | `5880757a21ca8d137cd611296767f179c54718a5e93f3cfb0aec384094893097` |
| Upload-signed release AAB | `731195406d0f1423a33c533cf7b1fd0041be33f29e18dd25c666ac64d854562b` |
| Debug APK used for captures and debug UI checks | `89dee5ac962f989a118ffdceafeb38a3d79b03f03047fbf599c48fbd90083b10` |
| Android test APK | `dcaacef79f4c3955f4a4d409b3a9907f41828f3ae319f6013c136f704abd7caa` |
| AAB-derived local release test APK | `db1958af09a6cfe8c38c47d1d0b8cf458aaf970e2fe2aa973b78b9ff038e01ba` |

SDK 37 `apksigner` verified the release APK's v2 signature and RSA 4096-bit key.
Upload certificate SHA-256:
`a8759906836cae6755b90a415ef1f0ddb44d8c7232a8ccc5041e26fca8990950`.
The key and Windows-protected password remain outside Git. This is a prepared
first-registration identity; any existing Play registration must use its
registered upload identity.

JDK `jarsigner` reported **jar verified** for the AAB, with self-signed chain,
missing timestamp, POSIX-attribute, and JarInputStream ordering warnings. It was
not a warning-free JAR check. Official Google bundletool 1.18.3 validated the AAB
structure and generated a universal APK. Bundled privacy text matches the
canonical bytes; all seven offline license/notice documents are present.

`zipalign -c -P 16 4` passed. Static inspection confirmed 16 KB alignment of
64-bit native LOAD segments and no writable-data intersection with rounded
RELRO protection pages. An initial checker incorrectly rejected the raw RELRO
end alone; inspection of actual address ranges corrected that false positive.
No native dependency was changed to conceal a failure. Both connected devices
report 4096-byte pages; **16 KB runtime behavior remains untested**.

## Physical phone behavior

The device is Samsung SM-S731N (`r13s`), Android 17/API 37, build
`CP2A.260605.016`, fingerprint
`samsung/r13sksx/r13s:17/CP2A.260605.016/S731NKSU9CZIF_OKR9CZIF:user/release-keys`.
Tests used portrait 1080 x 2340, density 450, font scale 1.15, a dark system
configuration, and English app strings. App appearance follows the recorded
system setting; selected component samples retain their explicit light themes.
Material 2 is 1.10.4, Material 3 is 1.5.0-alpha01, icons are 1.7.8, and the
optional inline helper is Autofill 1.3.0. Original historical OS captures remain
missing; these runs do not establish historical OS behavior.

| Scope | Tests | Final result |
| --- | ---: | --- |
| PrivacyPolicyTest | 2 | Passed |
| SourceNoticesTest | 2 | Passed |
| SettingsNavigationTest | 2 | Passed |
| RunsTest | 4 | Passed |

Debug evidence is `.local/device-runs/20261007T222349805Z-c996e1d4/`.
The installed APK hashes match the built artifacts. All ten tests passed and
restoration errors are empty. They verify the complete offline privacy and
notice text, document recreation/closing, installed backup/network flags,
category navigation, saved comparison inputs, environment expansion, deletion,
and export availability. The earlier run
`20261007T221525470Z-d1fe08e4` passed eight and failed two because the old fixture
did not scroll to the checkbox and target-SDK field; it is retained as a failure.
The first privacy run also retained its missing-category failure before the
Privacy category was added to the displayed group.

The same ten tests also passed against the **non-debuggable release payload**
generated from the final AAB. That universal APK uses the existing local debug
certificate solely to update local installations without deleting their data.
It is different from the upload-signed downloadable APK and from a Play-delivered
APK. Evidence is `.local/release-payload-test/`; its installed hash was verified
before instrumentation. Original animation settings and app locale were restored
with no restoration errors. This check does not prove Google Play delivery.

The tablet is SM-X800, Android 16/API 36, 2800 x 1752, density 340, font scale
1.0, landscape. It was locked, so no tablet interaction tests or store captures
are claimed for this release. Earlier picker and project-link checks remain
separately documented in
[the previous report](verification-project-links-pickers-2026-10-08.md).

## Listing and deployment evidence

Five language listings meet the 30/80/4000/500-character limits. The icon is
512 x 512 RGBA PNG and feature graphics are 1024 x 500 RGB PNG. Four Korean and
four English phone screenshots are 1080 x 1920 RGB PNG. Ruff lint/format, actual
image modes/dimensions/counts, screenshot hashes, and alt text checks passed.
All five graphics and eight screenshots were visually reviewed.

The [capture manifest](../distribution/google-play/screenshots/manifest.json)
records the debug APK, source revision, build, themes, display override, and
capture times. Temporary 1080 x 1920 display sizing was restored to the real
1080 x 2340 screen. Original app locale, appearance, introduction preference,
and demo settings were restored. SystemUI demo broadcasts did not hide this
Samsung build's real status bar. RGB encoding was the only image processing;
there was no crop, stretch, or invented UI. Screenshots are visual evidence,
not additional interaction-test passes.

GitHub Pages deployed successfully through
[run 37693821863](https://github.com/gaon12/Componentory/actions/runs/37693821863).
The [project website](https://gaon12.github.io/Componentory/) and
[privacy policy](https://gaon12.github.io/Componentory/privacy.html) are public.
The policy returned HTTP 200 and matched the locally generated canonical HTML.

Google Play is **not submitted or published**. The available Console session is
at Google's sign-in page; no authenticated developer account, public support
email, existing package/upload-key confirmation, audience decision, distribution
countries, or assigned IARC rating has been supplied. Personal-account production
testing/access gates may also apply. The
[release guide](google-play-release.md) explains these requirements using
official sources. A GitHub download, local installation, or website deployment
does not bypass Play's account and review requirements.
