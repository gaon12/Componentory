# Componentory product plan

Componentory lets users inspect and interact with original Android UI components,
test their behavior, and compare results across Android releases. Original UI
accuracy is the main requirement. The user has selected an Android app with PC
emulator integration: users explore and compare in the app, and directly operate
historical Android environments on the PC.

This document records the agreed direction and proposed screen behavior. It does
not claim that the execution tools, screens, or historical coverage already exist.

## Scope and accuracy

The long-term goal is a catalog spanning Android history, including early
releases. The first implementation milestone uses Android 4.4 and Android 5.0 with
Button and AlertDialog to verify the complete workflow. This milestone does not
replace the broader scope. The exact release-by-release rollout is still open.

Start with Android platform widgets in AOSP environments. Add library components
as separately labeled catalog entries. Manufacturer-specific UI can be a later
extension with its own device and build identity.

An Android version does not uniquely identify a component's appearance. Themes,
the target SDK, library versions, and device settings also affect the result. A
modern device displaying an old-looking theme is evidence from that modern device;
it cannot be labeled as a capture from the historical OS.

## Main user journey

1. Open Explore and choose a component or Android release.
2. Read the component's supported scenarios and available captures.
3. Select two releases and one scenario for an experiment.
4. Export the experiment configuration to the PC execution tool.
5. Run the original samples on the selected emulators or devices.
6. Directly interact with the samples, or run the selected automated scenario.
7. Import the result bundle into Componentory.
8. Compare matching captured states, events, and test results.

The initial integration proposal uses files for experiment configurations and
results. This keeps the execution contract explicit. Automatic transfer can use
the same contract later. The user has confirmed PC integration; file transfer is a
proposed implementation choice, not a separately confirmed requirement.

## Explore screen

Explore supports browsing by component and by Android release. A component entry
shows its name, source, supported scenarios, and capture availability. Release
entries keep the exact OS version and API identity rather than grouping every
release into one design generation.

The component detail screen presents these sections in order:

1. The component name and source, such as `android.widget.Button`.
2. Available Android releases and capture status.
3. A selected real capture, when one has been imported.
4. Scenario and input controls supported by that component.
5. Actions to prepare an experiment or choose a second release for comparison.
6. A short explanation of documented changes and links to their sources.

If no capture exists, show an empty preview with a clear action to prepare a run.
Do not fill the preview with an imitation of historical UI. Only show controls
that are valid for the selected component and runtime. For example, Button can
support enabled and disabled scenarios, while AlertDialog has confirm, cancel,
and dismissal scenarios.

Keep these states distinct:

| State | Meaning |
| --- | --- |
| Captured | A real capture has been imported with execution metadata. |
| Not run | The catalog contains the case, but no run exists yet. |
| Component unavailable | The selected runtime does not provide the component. |
| Environment unavailable | An appropriate runtime has not been acquired or verified. |

Test results appear separately. Captured does not imply that a behavioral test
passed.

## Compare screen

Compare starts with one component and two selected runs. The header identifies
both exact Android releases, component sources, themes, and capture times. The
user chooses a state that exists in both runs, such as default or disabled.

On a narrow phone, stack the two previews so that original text remains readable.
On a larger display, place them side by side. Allow users to enlarge an individual
capture. Preserve its aspect ratio and show its recorded pixel size and density.
Do not stretch a capture to make components appear equally sized.

Below the previews, show behavior results and important differences in execution
conditions. Use expandable environment details for the complete metadata. A
missing state produces an empty result for that side, not a copied screenshot.

Visual differences between releases are expected. A different screenshot is not
automatically a failed test. Behavioral tests compare each run against the
expected behavior for that scenario. Users can inspect event differences as a
separate comparison.

Two comparison modes are proposed:

- Controlled comparison uses the same supported target SDK, content, and settings
  to help isolate differences caused by the OS.
- Historical configuration comparison uses the recorded configuration chosen for
  each period, and makes target SDK or theme differences visible.

The interface must identify mismatched scenario inputs, component sources, or
settings. A user can still inspect those runs, but the app must not imply that all
conditions match.

## Runs screen

Runs lists imported experiments with their component, runtime, date, and test
status. Users can open execution details, inspect individual captures and events,
select runs for comparison, and import another result bundle.

Keep test pass, test failure, test not run, and execution failure separate. An
emulator that could not start must not produce a passed or failed component test.

## Original samples and PC execution

Use the existing Kotlin and Compose app for the browsing and comparison interface.
Use separate native samples for historical platform widgets. Those samples should
use the OS framework directly, without a compatibility library replacing the
widget being examined. Build configurations may need to differ across runtime
groups; verify the oldest supported group before promising that one APK supports
every historical version.

The PC tool has a small set of responsibilities:

- Identify the selected runtime and sample build.
- Install and open the appropriate sample.
- Leave the emulator available for direct user interaction.
- Execute supported repeatable scenarios when requested.
- Collect captures, events, checks, and execution metadata.
- Export a result bundle that the Android app can import.

Begin with explicit emulator or device selection. Do not silently choose the first
ADB device when multiple devices are connected. Keep Android 4.4 and 5.0 execution
compatible with their available tooling rather than assuming modern test APIs
exist on those systems.

## Result bundle proposal

A result bundle is a ZIP file containing a versioned JSON manifest, capture files,
and event or test data. All referenced files use relative paths within the bundle.
The importer validates file paths, size limits, and required metadata before
adding the run to the local collection.

The manifest records:

- The format version and run identity.
- Component source, component identity, scenario, and input values.
- Android release, API level, build fingerprint, manufacturer, model, and image
  source when known.
- Sample package, version, source revision, APK hash, and target SDK.
- Theme and library versions when applicable.
- Screen size, density, font scale, locale, orientation, and animation settings.
- Capture state, file reference, and timestamp.
- Events and test outcomes, with explicit execution failures or skipped checks.

Keep a single run's evidence together. Imported records must retain their original
metadata. Schema examples and fixtures must be labeled as test data and must not
appear in the user's history as real runs.

## Implementation milestones

| Milestone | Evidence needed |
| --- | --- |
| Establish the first historical environments | Both selected OS images boot and report their actual versions. |
| Run original samples | Button and AlertDialog run on both OS versions and accept real interaction. |
| Capture repeatable scenarios | The PC tool records real states, events, and checks with environment metadata. |
| Import and compare | The app imports both bundles and displays matching states and test outcomes. |
| Expand historical coverage | More releases and components have verified execution evidence and support records. |

Build each milestone in focused changes. Follow code changes, formatting and lint,
relevant tests, then a detailed English Git commit. The full product is unfinished
while historical execution, direct interaction, testing, or comparison is missing.

## Current project evidence

As inspected on October 4, 2026, the project contains a Compose starter activity.
The main app has minimum API level 24. Local SDK platforms include `android-36.1`
and `android-37.0`; no historical system images or AVD definitions were found in the
inspected default locations. These facts describe this development machine, not
the availability of every historical image elsewhere.

## Technical sources

- [Android API version constants](https://developer.android.com/reference/android/os/Build.VERSION_CODES)
  provide platform version identities.
- [Android SDK requirements](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
  explain minimum SDK installation limits and target SDK compatibility behavior.
- [Android virtual devices](https://developer.android.com/studio/run/managing-avds)
  describe runtime images and device configurations.
- [Managed test devices](https://developer.android.com/studio/test/managed-devices)
  support API level 27 and higher; older targets need a different execution path.
- [Compose Material migration](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)
  identifies Material 2 and Material 3 as separate libraries and implementations.
