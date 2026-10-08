# Component design audit: October 8, 2026

The lab confused a design choice with a dedicated API supplier. It also used
current-OS defaults for historical AnalogClock artwork. This audit separates
API identity, theme, resource provenance, and original historical evidence.

The [full matrix](component-design-audit-2026-10-08.csv) records all 73
framework-backed catalog entries across the seven design choices. Its
availability snapshot assumes API 37. It is a source audit, not a device-test
result or a set of original OS captures.

## Corrected problems

| Problem | Result |
| --- | --- |
| AnalogClock constructors read the installed framework dial and hands | Classic, Holo, and Material Design 1 inflate XML with public dial, hand_hour, and hand_minute attributes bound to pinned AOSP resources. |
| Eight experimental APIs were gated behind the Expressive design | ButtonGroup, SplitButtonLayout, LoadingIndicator, both wavy indicators, both floating toolbars, and FloatingActionButtonMenu work with standard, dynamic, and Expressive Material 3 themes. |
| ToggleButton existed in the same artifact but was hidden from standard Material 3 and Material You | All three Material 3 choices call the public ToggleButton API under the selected theme. |
| A missing dedicated Material clock API prevented all modern clock displays | Text clock, analog clock, digital clock, and chronometer now have explicitly labeled Componentory theme demos in all four modern choices. |
| Clock text and timer actions could exceed narrow panels | Clock text auto-sizes down to 16sp and can use two lines. Timer actions flow onto another row. Analog dials keep a square bounded by available width and 224dp. |
| Deprecated was translated as support being discontinued | Notes state that the framework widget is still available on the current OS. Custom demos do not inherit framework deprecation notes. |

The modern clocks use Compose drawing and text plus the selected library colors,
typography, and buttons. Their source is
`xyz.gaon.componentory.lab.ThemedClockSample`, never an invented
`androidx.compose.material3.Clock`. Saved runs identify `THEMED_DEMO` and
`COMPONENTORY_COMPOSE`. Clock format and timer state can move between framework
samples and these demos. Timers use monotonic time during a running session and
restore the saved wall-clock anchor when recreated.

A live clock display is different from a TimePicker clock face. The existing
Material 3 TimePicker remains a real input API and keeps its separate source.

The unavailable panel and chooser now say **Sample unavailable**. A missing-library
hint explains that other Compose APIs or Android widgets can implement the UI
concept. It is not shown for native theme requirements or a dynamic-color OS
requirement.

## Historical appearance still needs work

At API 37, the three framework theme choices have 218 available catalog
combinations. Only 38 use pinned artwork: 35 control/design pairs plus three
Toast recreations. The other 180 use the current OS. The unavailable Classic
ActionBar is the remaining cell in the 73 × 3 matrix.

The [resource follow-up](component-resource-audit-2026-10-08.md) checks concrete
constructor, adapter, and drawable paths beyond clocks. It identifies the
Spinner popup gap, other current-OS layouts, and the partial scope of imported
control artwork. The app now labels that scope explicitly. These counts are
unchanged; a selected drawable does not recreate the entire historical widget.

The control bundle now contains 1,042 variants, including 33 clock PNG variants.
They retain upstream bytes, density variants, immutable revisions, license
records, and hashes. AnalogClock uses these three releases:

| Choice | Release | Immutable commit |
| --- | --- | --- |
| Classic | android-2.3.7_r1 | 3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07 |
| Holo | android-4.4.4_r2 | 63ade05d76785975fc3292ca030abbaa1dda8891 |
| Material Design 1 | android-5.0.2_r1 | 0ac9664a9ee2e6d1d3b68fe00c264ad94fe98966 |

All three default mdpi dial images are byte-identical, with SHA-256
`9f32571b6f4991af7486a48efc9c773727f2622c0ec80ffb02c3bc2aa07aa5a7`.
The app preserves that fact. A framework AnalogClock is not the Clock app or a
home-screen clock widget. Different products can have different artwork.

These source bindings still use the installed framework engine. Original
historical OS captures and old-OS interaction checks are missing.

| Priority | Other affected components | Remaining evidence or implementation gap |
| --- | --- | --- |
| High | DatePicker, CalendarView, TimePicker and their dialogs | Current framework layout, internal controls, and typography can differ from the selected historical release. Their full historical resource closures are not imported. |
| High | Dialog, ProgressDialog, PopupWindow, ListPopupWindow, popup menus | Windows, backgrounds, list rows, and system-owned layout resources still depend on the installed OS. |
| High | SearchView, NumberPicker, AutoCompleteTextView, MultiAutoCompleteTextView | Internal child views, selectors, popup rows, and text styles need a source-specific audit. |
| Medium | TextClock, DigitalClock, Chronometer | Framework API instances are real, but old typography, layout, and behavior are not established by the current-OS sample. |
| Medium | ListView, GridView, ExpandableListView, Toolbar, ActionBar, TabHost | Theme wrappers do not establish historical selectors, bars, or navigation behavior. |
| Separate concern | ScrollView, framework layouts and animation containers | Many containers have no independent design-language artwork. Audit child controls and interaction semantics rather than inventing a historical skin. |
| Separate concern | WebView, VideoView, MediaController and platform-service hosts | These are OS capabilities. Their runtime/provider version is distinct from a Material theme. |

A theme-compatible API on a current device is not proof that the API existed in
an older release. TextClock was added at API 17 and was absent from Android
2.3.7. Toolbar was added at API 21 and was absent from Android 4.4.4. Displaying
them in Classic or Holo on a new OS remains a current-OS sample. The version
history's SDK membership and pinned source evidence must answer historical
availability separately.

## Other modern concepts still lack alternatives

Fifty framework-backed entries still have neither a mapped Material 2/3 API
nor a Componentory modern demo. `NO_SAMPLE` in the matrix means an implementation
gap in this lab. It does not mean that Android or a design language cannot show
the concept. Four clocks, Toast, and ToggleButton are excluded from this count.

| Entries or group | Related implementation to assess | Required distinction |
| --- | --- | --- |
| ScrollView / HorizontalScrollView | Foundation verticalScroll / horizontalScroll | Scrolling is a Foundation capability, not a Material widget API. |
| ListView / GridView / ExpandableListView | Foundation LazyColumn / lazy grids plus list-item styling | Selection, expansion, and recycling behavior need an explicit new sample. |
| FrameLayout / LinearLayout / TableLayout / GridLayout / RelativeLayout / Space | Compose Box, Row, Column, custom layout | These are layout concepts, not missing Material controls. AbsoluteLayout remains a legacy source. |
| CalendarView | Material 3 DatePicker | Related calendar input already has a separate catalog sample; it is not the same framework API. |
| SearchView | Material 3 SearchBar | Existing related sample should be linked without pretending it is SearchView. |
| Spinner / autocomplete controls | ExposedDropdownMenuBox and editable suggestions | A dropdown sample exists; tokenization and suggestion behavior need their own mapping. |
| Toolbar / ActionBar / ActionMenuView | TopAppBar and action menus | Existing related samples have different sources and hosting behavior. |
| RatingBar / NumberPicker | Explicitly named composed demos | No dedicated counterpart is mapped in the recorded Material artifacts. |
| WebView / VideoView / MediaController | AndroidView or a separately identified media/browser supplier | Platform interoperability must record the actual platform source and engine. |
| ViewSwitcher / ViewFlipper / adapter animations | Compose animation/container demos | Timing, child identity, and controls cannot be inferred from a visual similarity. |
| Gallery / SlidingDrawer / ProgressDialog / DialerFilter / other deprecated APIs | Separately identified modern alternatives | Deprecation does not remove the UI concept or create an official modern API with the same name. |
| Contact, sharing, inline autofill, zoom and edge effects | Platform integration or explicitly composed behavior | Theme selection does not control platform capability availability. |

This audit does not enable all missing cells with one generic fallback. Each
alternative needs an accurate supplier label, actual behavior, and focused
verification. It also does not mark the 180 current-OS cells as historically
recreated.

## Verification and limits

The [verification report](verification-component-design-audit-2026-10-08.md)
records focused commits, formatter/lint, JVM and device outcomes, APK identities,
OS build, theme, display, and font settings.
The 73-entry CSV is static audit evidence. The seven-choice catalog has 168
entries and 248 canonical API rows; custom clock demos add available comparison
cells without inventing new canonical Material clock API rows.

At API 37, available cells are Classic 72, Holo 73, Material Design 1 73,
Material Design 2 54, and 115 each for Material 3, Material You, and Expressive:
617 of 1,176 catalog combinations. These are source availability counts, not
1,176 passing interaction tests.

## Primary sources

- [AnalogClock API and public drawable attributes](https://developer.android.com/reference/android/widget/AnalogClock).
- [Current AnalogClock implementation and its fallback resources](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/AnalogClock.java).
- [TextClock API introduction](https://developer.android.com/reference/android/widget/TextClock).
- [Toolbar API introduction](https://developer.android.com/reference/android/widget/Toolbar).
- [Pinned Material 3 1.5.0-alpha01 source JAR](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3-android/1.5.0-alpha01/material3-android-1.5.0-alpha01-sources.jar).
- [Material 3 API reference](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary).
- [Compose lists and grids](https://developer.android.com/develop/ui/compose/lists).
- [Compose layout basics](https://developer.android.com/develop/ui/compose/layouts/basics).
- [Material 3 search bar](https://developer.android.com/develop/ui/compose/components/search-bar).
- [Compose AndroidView interoperability](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose).
