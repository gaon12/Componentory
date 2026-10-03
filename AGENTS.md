# Development instructions

## Required workflow

For each focused implementation change, complete these steps in order:

1. Change the code.
2. Run the formatter and relevant lint checks. Fix problems before continuing.
3. Run the tests that verify the behavior affected by the change.
4. Review the diff and create a focused Git commit.

Split work by feature or by a coherent code or file change. Do not collect unrelated
features into one final commit. A change that needs several files to work should
keep those files together.

Write commit messages in English. Use a clear subject and a detailed body that
explains the problem, the resulting behavior, and the checks performed. Report
checks that could not run accurately. Do not describe attempted checks as passing.

For documentation-only changes, review the text and links and run
`git diff --check`. Application tests are required when application behavior or
build configuration changes, not for text edits alone.

## Code and writing

- Prefer simple, direct code and familiar structures.
- Group application code by the feature it supports.
- Introduce shared abstractions only when there is a real repeated need.
- Write documentation, code comments, and commit messages in easy English.
- Explain why a decision matters instead of commenting on obvious code.
- Keep machine-specific SDK paths, generated outputs, and secrets out of Git.

## Original UI accuracy

- Run historical components on their actual Android OS when claiming original
  appearance or behavior.
- Keep Android platform widgets separate from AppCompat, Material, and Compose
  library components. Record library versions when a library supplies the UI.
- Record the OS build, theme, target SDK, device or emulator identity, and display
  settings with each run.
- Show missing captures and unsupported components explicitly.
- Never present a recreation, fixture, or planned sample as an original capture.
- Treat screenshots as visual evidence. A screenshot alone does not prove that
  interaction behavior passed a test.
- Preserve the goal of broad historical coverage. The first experiment is an
  implementation milestone, not the finished product.
