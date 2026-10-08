# Component resource follow-up: October 8, 2026

This follow-up checks concrete rendering paths beyond clocks. A theme wrapper
does not replace the installed framework's layouts, internal child views,
popup rows, dimensions, or behavior. A pinned drawable also does not establish
the whole component's historical appearance.

The [earlier matrix](component-design-audit-2026-10-08.csv) still records the
same 73 framework-backed entries and seven design choices. Its counts have
not changed: 38 framework-theme cells bind AOSP artwork, 180 use current-OS
samples, and Classic ActionBar has no compatible native theme. Fifty modern
concepts still lack a mapped library API or implemented alternative.

## Confirmed rendering paths

| Component | Source path | Finding |
| --- | --- | --- |
| Spinner | [PlatformInputs](../app/src/main/java/xyz/gaon/componentory/lab/PlatformInputs.kt), [HistoricalControls](../app/src/main/java/xyz/gaon/componentory/lab/recreation/HistoricalControls.kt) | The closed control's background uses the pinned drawable. Its selected row and popup row still use android.R.layout.simple_spinner_item and simple_spinner_dropdown_item. It is a partial artwork recreation. |
| AutoCompleteTextView / MultiAutoCompleteTextView | [PlatformInputs](../app/src/main/java/xyz/gaon/componentory/lab/PlatformInputs.kt) | Both adapters use the installed android.R.layout.simple_dropdown_item_1line. The text fields and popup internals also remain current-OS implementations. |
| SearchView / NumberPicker | [PlatformInputs](../app/src/main/java/xyz/gaon/componentory/lab/PlatformInputs.kt), [PlatformSample](../app/src/main/java/xyz/gaon/componentory/lab/PlatformSample.kt) | Direct framework constructors receive the selected theme. No historical internal layout or selector closure is bound. These remain current-OS samples. |
| DatePicker / CalendarView / TimePicker | [Inline dates](../app/src/main/java/xyz/gaon/componentory/lab/PlatformInlineDateSample.kt), [Inline time](../app/src/main/java/xyz/gaon/componentory/lab/PlatformInlineTimeSample.kt) | Direct installed-OS widgets receive Theme.Light, Holo.Light, or Material.Light. Scroll and calendar viewport hosts improve usability; they do not import old picker delegates or internal child resources. |
| DatePickerDialog / TimePickerDialog | [Date dialog](../app/src/main/java/xyz/gaon/componentory/lab/PlatformDatePickerDialogSample.kt), [Time dialog](../app/src/main/java/xyz/gaon/componentory/lab/PlatformTimePickerDialogSample.kt) | The actual framework dialog classes use the themed context. Dialog windows, picker delegates, and internal controls still belong to the installed framework. |
| AlertDialog / Dialog / ProgressDialog | [PlatformSample](../app/src/main/java/xyz/gaon/componentory/lab/PlatformSample.kt) | The framework constructors and window styles remain current-OS resources. The app supplies sample content and launcher actions, not a pinned historical dialog layout. |
| PopupWindow / ListPopupWindow | [Popup windows](../app/src/main/java/xyz/gaon/componentory/lab/PlatformPopupWindowSample.kt) | PopupWindow contains an app-created TextView and a resolved theme background. ListPopupWindow uses android.R.layout.simple_list_item_1. Neither is a full historical resource recreation. |
| ListView / GridView / ExpandableListView | [Lists](../app/src/main/java/xyz/gaon/componentory/lab/PlatformListSample.kt) | List and grid choice rows use the installed simple_list_item_single_choice. Expandable rows are app-created sample views. Their native container classes are real; the rows are not old-OS captures. |
| Toolbar / ActionMenuView / ActionBar | [Menu hosts](../app/src/main/java/xyz/gaon/componentory/lab/PlatformMenuHostSample.kt), [Native ActionBar host](../app/src/main/java/xyz/gaon/componentory/lab/ActionBarSampleActivity.kt) | Native hosting does not import the old bar, menu, or overflow resources. Theme.Light has no ActionBar; Holo and Material use the installed native bar. |
| CheckedTextView / framework icons | [Text samples](../app/src/main/java/xyz/gaon/componentory/lab/PlatformTextSample.kt), [Icon catalog](../app/src/main/java/xyz/gaon/componentory/icons/IconCatalog.kt) | Choice marks and public framework drawable IDs resolve from the current OS. They are separate from the pinned checkbox/radio artwork and versioned Compose icon artifact. |

The source paths above establish resource selection, not original historical
pixel geometry or old-OS behavior. Layout-only containers, WebView, media,
sharing, and platform-service hosts need capability and behavior records in
addition to visual evidence. They cannot acquire an original old-OS appearance
merely by choosing Classic or Holo.

## Scope of imported control artwork

[HistoricalControls](../app/src/main/java/xyz/gaon/componentory/lab/recreation/HistoricalControls.kt)
currently binds these specific slots:

| Control | Pinned slots | Remaining scope |
| --- | --- | --- |
| Button / ToggleButton | Background drawables | Text metrics, view dimensions, and interaction engine are current-OS/adapted. |
| CheckBox / RadioButton | Checked-state button drawables | View layout, text metrics, and interaction engine remain current-OS/adapted. |
| EditText | Input background | Editable text, selection handles, keyboard, and internal behavior are not historical captures. |
| Switch in Holo / Material Design 1 | Thumb and track drawables | Native measurement, internal text placement, and behavior remain current-OS. Classic Switch has no imported source binding. |
| Horizontal ProgressBar | Progress layers and horizontal indeterminate drawable | Native measurement and animation execution remain current-OS. Circular progress is not part of this binding. |
| SeekBar | Progress track and thumb | Native sizing, touch behavior, and other unbound styling remain current-OS. |
| RatingBar | AOSP rating style and its tiled star resources | Native tiling and sizing are runtime behavior, not proof of an old-OS capture. |
| Spinner | Closed-control background | Selected/popup rows and popup window resources are not pinned. |

AnalogClock and the separately implemented Toast popup retain the clock/toast
scope recorded in the earlier audit. No additional controls gained historical
resource coverage in this follow-up. Imported AOSP hashes, release commits,
and notices remain unchanged.

## Modern equivalents are separate suppliers

The missing cells are often API-mapping gaps rather than a design restriction:

| Framework concept | Existing related catalog entry | What still needs work |
| --- | --- | --- |
| ImageButton | IconButton | Map behavior and chosen icon explicitly; do not claim an instantiated ImageButton in the Compose sample. |
| SearchView | Material 3 SearchBar | Submission, suggestions, expansion, and saved state need an explicit counterpart policy. |
| CalendarView | Material 3 DatePicker | Calendar selection and DatePicker input modes differ; a counterpart needs documented behavior. |
| Spinner / autocomplete | ExposedDropdownMenuBox | Editable suggestions and tokenization are separate from a simple dropdown. |
| Toolbar / ActionBar | TopAppBar and action menus | Native Activity hosting differs from a Compose app bar. |
| List / grid / scrolling / layout | Compose Foundation and layout APIs | These are not dedicated Material widget functions. Supplier and behavior labels need to match an implemented sample. |

The nine erroneous Material 3 API restrictions were already removed in the
earlier fix. This follow-up does not invent additional dedicated Material APIs
or change the availability counts through an unverified generic fallback.

## User-visible corrections and evidence

The main app no longer reserves a global branding bar. Main tabs start at the
safe system inset, and component detail retains a compact Back/Compare action
row. Sample widgets and library app-bar components remain available in the lab.

The recreation badge now says **Partial AOSP artwork recreation**. Its
five-language explanation identifies selected artwork and the current-OS
layout/popup scope. Toast keeps its own app-popup explanation. The completed
checks and exact device/build identities are recorded in the verification
section below.

Original Classic, Holo, and Material Design 1 OS captures remain missing. A
current-device test can verify a native control and its bound artwork without
establishing its original historical OS appearance.

## Ordered verification

Package xyz.gaon.componentory, debug version 1.0.0, code 2, minimum API 24,
target SDK 37, JDK 25. Each application change followed code, formatter/lint,
tests, diff review, and a focused commit:

| Commit | Change | Completed checks before commit |
| --- | --- | --- |
| 922e12c | Remove the global app-name bar; retain compact detail actions and safe system insets | Spotless apply/check, lintDebug, 173 JVM tests, debug and instrumentation APK builds, two resource-only inventory checks, diff review and whitespace check. |
| e86044e | Explain partial artwork recreation in five locales | Spotless apply/check, lintDebug, 173 JVM tests, both APK builds, two inventory and four icon resource checks, diff review and whitespace check. |

The header scope executed all 173 JVM tests across 21 reports, with zero
failures, errors, or skips. The later strings-only scope invoked the same task;
Gradle marked it up-to-date and retained those passing results. Both lint
scopes completed with zero errors and 75 retained
warnings. The four scripts.history.test_bundle source/provenance checks also
passed; the 1,042 imported control variants, hashes, and notices are unchanged.
These source tests do not verify native interaction or original OS appearance.

### Locked-tablet resource checks

The tablet was locked, and the user requested continuing while an unlock was
unavailable. Only the test-device.ps1 reviewed -NoUi resource scopes ran.
They launch no Activity, prepare no screen, and send no input. They do not
test the removed header's layout, navigation, popup interaction, or clipping.

| Run ID | Exact class | Result |
| --- | --- | --- |
| 20261008T004834082Z-c713b526 | ComponentInventoryResourceTest | 2 passed on the header change |
| 20261008T010057444Z-1c9ab9eb | ComponentInventoryResourceTest | 2 passed on the final application change |
| 20261008T010139817Z-5aa2d79e | IconCatalogResourceTest | 4 passed on the same final APK |

The final six checks verify canonical source mappings, packaged inventory
identity, every public framework drawable's catalog entry and runtime
availability, 11,385 pinned Material icon identities, selection/fallback
behavior, and separate theme/configuration resource contexts. The framework
resources in those assertions belong to the installed OS. They are not
original Classic or Holo resources just because a theme wrapper was used.

All three manifests have status passed, native exit code 0, and no restoration
errors. Per-app locale was empty and animation scales were 1.0. Resource-only
runs did not override the animation scales or physical display settings.
The icon test uses separate configured contexts for Japanese, night mode,
and alternate density; it asserts that the application configuration is unchanged.

Tests ran before their focused commit, so manifests accurately retain a dirty
parent revision and changed paths. The header run records parent 5a689ce and
only ComponentoryApp.kt. Both final runs record parent 922e12c and only the
five localized strings.xml files. Their app and instrumentation hashes match:

| Artifact | Header run SHA-256 | Final resource runs SHA-256 |
| --- | --- | --- |
| app-debug.apk | a14a09400c8ebfcac1dd2443e6b88c72ba0e2fdbb8f5dea23a7bed1623a1b95c | f01c6e6691865a876745faa71dff6b2f586574aacddcb118c92e65b5dc55010a |
| app-debug-androidTest.apk | 8b1ca0261479302fd283dcc22fd96745a926e29ae1b09599112be6cd24cd83b7 | 8b1ca0261479302fd283dcc22fd96745a926e29ae1b09599112be6cd24cd83b7 |

### Device, themes, and deferred UI scope

Samsung SM-X800 (gts8pwifi, identity R54T202R4BK), Android 16/API 36, build
BP2A.250605.031.A3. Fingerprint:
`samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZH3:user/release-keys`.
Physical size 1752 x 2800; recorded logical size 2800 x 1752, landscape,
rotation 90 degrees, density 340, font scale 1.0, system locale ko-KR.
These settings identify the device, not an executed app viewport assertion.

The icon scope explicitly resolves resources in android:Theme.Light,
android:Theme.Holo.Light, and android:Theme.Material.Light contexts, plus a
night/density/locale configuration context. The manifests also record the
application's seven sample configurations: those three framework themes,
Material 2 lightColors, Material 3 lightColorScheme, Android 12+ dynamic colors,
and MaterialExpressiveTheme with expressive colors and motion. No Compose UI
was rendered by these resource tests. Supplier pins remain Material 2 1.10.4,
Material 3 1.5.0-alpha01, Material icons 1.7.8, and Autofill helper 1.3.0.

CatalogNavigationTest (five checks) and AdaptiveNavigationTest (two checks)
compiled into the instrumentation APK but were **not executed**. Physical
header spacing, Back/Compare handoff, compact/wide navigation, and long-label
layout remain deferred until the tablet can be unlocked. No smartphone UI
test ran in this follow-up. Earlier clock/UI results belong to the exact
binaries in their separate [verification report](verification-component-design-audit-2026-10-08.md).

Run manifests and raw instrumentation output are retained under
.local/device-runs with the IDs above. A separate local finalization record
can verify the final APK installation/hash and screen-off action. Installation
while locked cannot establish a touch or rendering pass.

The published GitHub v1.0.0 release, signed APK/AAB, and store-material bundle
remain separate earlier artifacts. This follow-up does not establish a new
Google Play submission or publication.
