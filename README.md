# Componentory

Componentory is an Android app for exploring, interacting with, testing, and
comparing original Android UI components across Android releases. Historical
components run on their actual Android version in a PC emulator or on a device.
The Android app helps users explore the catalog and compare recorded results.

The project is currently in the planning stage. The application still contains
the Android Studio Compose starter screen. No historical captures or test results
have been collected yet.

## Project decisions

- Original appearance and behavior take priority.
- The first experience combines an Android app with PC emulators.
- Platform widgets and UI library components have separate identities.
- Every result records the environment in which it was produced.
- The long-term catalog covers Android history. A small first experiment checks
  the execution and comparison workflow before coverage expands.

## Project documents

- [Product plan](docs/product-plan.md) describes the screens, user journey,
  comparison rules, and acceptance criteria.
- [Development instructions](AGENTS.md) describe the required change, lint,
  format, test, and commit workflow.

## Existing Android project

The app uses Kotlin and Jetpack Compose. Its current minimum Android API level is
24. This app is the starting point for the catalog and comparison interface.
Historical samples will need their own compatible build configuration.

On Windows, use the Gradle wrapper after setting `JAVA_HOME` to a suitable JDK:

```powershell
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
```

These commands check the existing application. They do not prove that historical
components have been executed or compared.
