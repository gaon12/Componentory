# Android history source analysis

The historical catalog starts with public SDK API signatures. A Java source file
does not establish that a class was available to applications. The classifier
follows public View, Dialog, PopupWindow, and Preference ancestry. It also includes
an explicit list of visual controllers, such as ActionBar and EdgeEffect. Abstract
classes remain marked as abstract. Adapters, listeners, and implementation helpers
are excluded. This is a documented scope, not a claim that every Android UI API has
already been classified.

## Version inventory

Run from the project root with Python 3.11 or newer:

```powershell
python -m scripts.history inventory --output .local/history-inventory
```

The default SDK source is the immutable `platform/prebuilts/sdk` commit
`3af7c93524be6f51e092b87b17f009f13ee98b43`. The tool reads API 1 through 36 and
records a source URL and SHA-256 for each signature. A missing signature stops the
export; a later API is never inferred from the current device or a source folder.
Use `--sdk-commit` and `--max-api` to audit another immutable snapshot.

`public-ui.csv` contains the actual class, parent, classification, abstract flag,
and deprecation flag for every selected API. Compare successive snapshots to find
introductions, deprecations, removals, and reintroductions. A deprecated API can
still be public and supported. Identical signatures are retained as identical
source evidence, not treated as proof that an OS release has no UI changes.

## Original framework resource graphs

```powershell
python -m scripts.history export --release android-4.4.4_r2 --api 19 --component android.widget.Button --component android.widget.CheckBox --output .local/history-export
```

Omit `--component` to analyze all classified public UI classes in that release.
Repeat `--theme` to choose themes. The default themes are `Theme.Holo` and
`Theme.Holo.Light`; choose appropriate themes for another release. The release
tag is resolved to a framework commit. Its declared SDK version is checked against
the same release's pinned `platform/build` metadata. When a release has no text
`api/current.txt`, the tool uses that API revision's pinned public SDK signature
and records this separate source basis in the manifest. Internal qualifier-folder
aliases are materialized without OS symlinks and their original targets are recorded.
Unsupported layouts or source locations must be
reported and investigated rather than silently replaced with modern widgets.

The tool combines constructor style attributes with Java `R.layout`, `drawable`,
`color`, `dimen`, `style`, `styleable`, `id`, and `attr` references. It reads public
class ancestors, follows theme bindings, style parents, XML dependencies, and
styleable attributes recursively. Every density and configuration variant keeps
its original directory and bytes, including `.9.png` files. A fresh export
directory prevents old files from being mistaken for the new result.

```text
versions/<release>/
  manifest.json
  components/<qualified-class>.json
  resources/<original-qualified-directory>/<original-file>
```

The manifest includes source hashes, requested themes, exported file hashes,
missing dependencies, and analysis limits. Original values files are copied whole
and can contain declarations outside the graph. The graph retains all qualifier
variants and does not choose a configuration for a device. Constructor paths are
collected conservatively; an inherited constructor default is not necessarily
used by a subclass. Delegates, reflective resource names, and implementation in
other repositories need further analysis. This export is source evidence, not a
ready-to-build resource port.

Analysis grades are conservative planning hints:

- `RESOURCE_ONLY`: a simple widget whose visual resources are a useful starting point.
- `RESOURCE_AND_LAYOUT`: a widget which also needs its original layout structure.
- `IMPLEMENTATION_DEPENDENT`: original implementation work is expected or uncertain.

Every grade records `originalCapture: MISSING` and `historicalBehavior: UNVERIFIED`.
Neither a resource export nor a modern-device recreation is an original OS capture.
Original appearance and behavior require runs on the corresponding historical OS,
with build, theme, target SDK, device, and display settings recorded separately.
Broad historical coverage remains the goal; one release export is a milestone.

## Verification

```powershell
python -m ruff check scripts/history
python -m ruff format --check scripts/history
python -m unittest scripts.history.test_history -v
```

Unit fixtures verify classification, constructor references, style inheritance,
recursive resource graphs, qualifier retention, binary hashes, missing references,
and safe archive extraction. They are synthetic analysis tests, not UI captures.
Network exports should also be reviewed against original source files before
their metadata becomes a reviewed input for the app catalog. Keep caches,
archives, exported graphs, and generated application assets outside Git.

Sources: [public SDK repository](https://android.googlesource.com/platform/prebuilts/sdk/),
[KitKat framework release](https://android.googlesource.com/platform/frameworks/base/+/android-4.4.4_r2/).
Original AOSP files retain their upstream licenses; source metadata does not remove
those obligations when resource files are redistributed.
