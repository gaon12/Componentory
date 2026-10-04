# Componentory

Componentory is an Android UI lab for exploring, interacting with, testing, and
comparing Android design families on a physical device. Platform Classic, Holo,
and Material themes provide live framework controls. Library components are
separate entries with their own version identity.

The project is currently in the planning stage. The application still contains
the Android Studio Compose starter screen. No historical captures or test results
have been collected yet.

## Project decisions

- Use actual framework widgets or the selected library's real components.
- The main experience runs directly on a physical device.
- A design family does not change the device's Android version.
- Platform widgets and UI library components have separate identities.
- Verified results record the environment in which they were produced.
- The long-term catalog covers Android history. Exact historical OS execution
  can be added as a separate verification mode.

## Project documents

- [Product plan](docs/product-plan.md) describes the screens, user journey,
  comparison rules, and acceptance criteria.
- [Development instructions](AGENTS.md) describe the required change, lint,
  format, test, and commit workflow.

## Existing Android project

The app uses Kotlin and Jetpack Compose. Its current minimum Android API level is
24. This app is the starting point for the catalog and comparison interface.
The Compose interface can host real platform widgets using `AndroidView` and a
themed context.

On Windows, use the Gradle wrapper after setting `JAVA_HOME` to a suitable JDK:

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat spotlessCheck
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
```

Use a connected physical device for instrumentation tests. Selecting a legacy
theme on a modern device is a current-device experiment, not execution of an old
Android OS.
