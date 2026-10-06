package xyz.gaon.componentory.catalog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.AbsoluteLayout
import android.widget.ActionMenuView
import android.widget.AdapterViewFlipper
import android.widget.AnalogClock
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.Chronometer
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.DigitalClock
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.Gallery
import android.widget.GridLayout
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.ListView
import android.widget.MultiAutoCompleteTextView
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.QuickContactBadge
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.SlidingDrawer
import android.widget.Space
import android.widget.Spinner
import android.widget.StackView
import android.widget.Switch
import android.widget.TabHost
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextClock
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.TimePicker
import android.widget.ToggleButton
import android.widget.Toolbar
import android.widget.TwoLineListItem
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import android.widget.ZoomButton
import android.widget.ZoomControls
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import org.hamcrest.Description
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

// This is a current-device rendering sweep, not historical OS or complete interaction coverage.
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class CatalogRenderingSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        assertEquals(
            "This sweep uses the English instrumentation runner locale.",
            "en",
            compose.activity.resources.configuration.locales[0].language,
        )
        compose.onNodeWithTag("nav_compare").performClick()
        // A native infinite animation can keep Espresso busy even while another panel changes.
        chooseComponent(LabComponent.BUTTON)
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
    }

    @Test
    fun classicCatalogCellsRenderOrExplainTheirAbsence() =
        verifyFamily(DesignFamily.CLASSIC, 65, 43)

    @Test
    fun holoCatalogCellsRenderOrExplainTheirAbsence() = verifyFamily(DesignFamily.HOLO, 65, 43)

    @Test
    fun materialPlatformCatalogCellsRenderOrExplainTheirAbsence() =
        verifyFamily(DesignFamily.MATERIAL, 65, 43)

    @Test
    fun material2CatalogCellsRenderOrExplainTheirAbsence() =
        verifyFamily(DesignFamily.MATERIAL2, 33, 77)

    @Test
    fun material3CatalogCellsRenderOrExplainTheirAbsence() =
        verifyFamily(DesignFamily.MATERIAL3, 61, 49)

    private fun verifyFamily(
        family: DesignFamily,
        expectedSupported: Int,
        expectedUnsupported: Int,
    ) {
        assertEquals(
            "Update the sweep baseline when the runnable catalog changes.",
            110,
            LabComponent.entries.size,
        )
        chooseComponent(LabComponent.BUTTON)
        chooseFamily("LEFT", family)
        var supported = 0
        var unsupported = 0
        var skipped = 0
        LabComponent.entries.forEach { component ->
            if (family.platform != null && component.isIndeterminateProgress) {
                // NativeProgressIndicatorsTest verifies these six original animated controls.
                skipped++
                return@forEach
            }
            try {
                chooseComponent(component)
                compose
                    .onNodeWithTag("family_LEFT")
                    .assertTextContains(family.selectionLabel, substring = true)
                val reason =
                    family.unsupportedReason(component, Build.VERSION.SDK_INT, compose.activity)
                if (reason != null) {
                    verifyUnsupported(reason)
                    unsupported++
                } else {
                    verifyMetadata(component, family)
                    compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
                    if (family.platform != null) verifyNative(component, family)
                    else verifyLibrary(component, family)
                    supported++
                }
            } catch (failure: Throwable) {
                throw AssertionError(
                    "Catalog cell ${family.name}/${component.name}, API ${Build.VERSION.SDK_INT}, " +
                        "provider ${family.selectionLabel}, expected source ${family.source(component)}",
                    failure,
                )
            }
        }
        assertEquals("${family.name}: supported cells", expectedSupported, supported)
        assertEquals("${family.name}: unsupported cells", expectedUnsupported, unsupported)
        assertEquals(
            "${family.name}: separately tested animation cells",
            if (family.platform != null) 2 else 0,
            skipped,
        )
        chooseComponent(LabComponent.BUTTON)
        recordCoverage(family, supported, unsupported, skipped)
    }

    private fun verifyUnsupported(reason: String) {
        compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
        compose
            .onNode(hasText(reason) and hasAnyAncestor(hasTestTag("unsupported_LEFT")))
            .assertExists()
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT_1").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT_2").assertDoesNotExist()
        compose.onNodeWithTag("status_LEFT").assertDoesNotExist()
    }

    private fun verifyMetadata(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
        val implementation =
            when (family) {
                DesignFamily.MATERIAL2 ->
                    "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION}"
                DesignFamily.MATERIAL3 ->
                    "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}"
                else -> "android:${requireNotNull(family.platform).themeName}"
            }
        compose
            .onNodeWithTag("implementation_LEFT")
            .assertTextContains(implementation, substring = true)
        if (family.platform != null) {
            compose
                .onNodeWithTag("implementation_LEFT")
                .assertTextContains(
                    compose.activity.getString(R.string.widget_api, component.minimumApi),
                    substring = true,
                )
        }
    }

    private fun verifyNative(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        if (component == LabComponent.DATE_PICKER || component == LabComponent.CALENDAR_VIEW) {
            compose.onNodeWithTag("date_viewport_LEFT").performScrollTo().assertIsDisplayed()
        }
        if (component == LabComponent.TIME_PICKER) {
            compose.onNodeWithTag("time_viewport_LEFT").performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithTag("native_LEFT").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            val view = requireNotNull(compose.activity.findViewById<View>(R.id.sample_left))
            assertEquals(nativeClass(component), view.javaClass)
            assertTrue(view.isShown && view.width > 0 && view.height > 0)
            assertTrue(view.isEnabled)
            assertNativeTheme(view, family, component)
            when (component) {
                LabComponent.TEXT,
                LabComponent.CHECKED_TEXT_VIEW -> {
                    val text = view as TextView
                    assertEquals(
                        compose.activity.getString(R.string.sample_display_text),
                        text.text.toString(),
                    )
                    assertFalse(text.isClickable)
                    assertFalse(text.hasOnClickListeners())
                    assertNotNull(text.layout)
                    assertTrue(text.layout.lineCount >= 3)
                    if (text is CheckedTextView) {
                        assertFalse(text.isChecked)
                        val attributes =
                            text.context.obtainStyledAttributes(
                                intArrayOf(android.R.attr.listChoiceIndicatorMultiple)
                            )
                        try {
                            val expected = attributes.getDrawable(0)
                            if (expected == null) assertNull(text.checkMarkDrawable)
                            else {
                                val actual = requireNotNull(text.checkMarkDrawable)
                                expected.state = text.drawableState
                                assertEquals(expected.javaClass, actual.javaClass)
                                assertEquals(expected.intrinsicWidth, actual.intrinsicWidth)
                                assertEquals(expected.intrinsicHeight, actual.intrinsicHeight)
                            }
                        } finally {
                            attributes.recycle()
                        }
                        val node = text.createAccessibilityNodeInfo()
                        assertTrue(node.isCheckable)
                        assertFalse(node.isChecked)
                        assertFalse(node.isClickable)
                    }
                }
                LabComponent.CHECKBOX,
                LabComponent.SWITCH,
                LabComponent.TOGGLE_BUTTON -> {
                    assertFalse((view as CompoundButton).isChecked)
                    assertTrue(view.text.isNotEmpty())
                }
                LabComponent.RADIO -> {
                    val group = view as RadioGroup
                    assertEquals(2, group.childCount)
                    assertEquals(-1, group.checkedRadioButtonId)
                    for (index in 0..1) {
                        val radio = group.getChildAt(index) as RadioButton
                        assertEquals(RadioButton::class.java, radio.javaClass)
                        assertFalse(radio.isChecked)
                        assertEquals(
                            compose.activity.getString(
                                if (index == 0) R.string.option_a else R.string.option_b
                            ),
                            radio.text.toString(),
                        )
                    }
                }
                LabComponent.TEXT_FIELD,
                LabComponent.AUTOCOMPLETE,
                LabComponent.MULTI_AUTOCOMPLETE -> {
                    val field = view as EditText
                    assertEquals("", field.text.toString())
                    assertTrue(field.hint.isNotEmpty())
                    assertEquals(
                        InputType.TYPE_CLASS_TEXT,
                        field.inputType and InputType.TYPE_MASK_CLASS,
                    )
                    if (view is AutoCompleteTextView) {
                        assertEquals(1, view.threshold)
                        assertEquals(3, view.adapter.count)
                        assertEquals("Alpha", view.adapter.getItem(0))
                    }
                }
                LabComponent.SLIDER -> {
                    assertEquals(100, (view as SeekBar).max)
                    assertEquals(50, view.progress)
                }
                LabComponent.PROGRESS -> {
                    assertFalse((view as ProgressBar).isIndeterminate)
                    assertEquals(100, view.max)
                    assertEquals(50, view.progress)
                    assertNotNull(view.progressDrawable)
                }
                LabComponent.RATING -> {
                    assertEquals(5, (view as RatingBar).numStars)
                    assertEquals(1f, view.stepSize, 0f)
                    assertEquals(0f, view.rating, 0f)
                }
                LabComponent.NUMBER_PICKER -> {
                    assertEquals(0, (view as NumberPicker).minValue)
                    assertEquals(10, view.maxValue)
                    assertEquals(5, view.value)
                }
                LabComponent.SPINNER -> {
                    assertEquals(3, (view as Spinner).adapter.count)
                    assertEquals(0, view.selectedItemPosition)
                    assertEquals("Alpha", view.selectedItem)
                }
                LabComponent.SEARCH_VIEW -> {
                    assertEquals("", (view as SearchView).query.toString())
                    // The corrected getter requires API 29; this public getter also works on API
                    // 24.
                    assertFalse(view.isIconfiedByDefault())
                    assertTrue(view.isSubmitButtonEnabled)
                }
                LabComponent.DATE_PICKER -> {
                    val picker = view as DatePicker
                    assertEquals(2024, picker.year)
                    assertEquals(Calendar.JANUARY, picker.month)
                    assertEquals(15, picker.dayOfMonth)
                    assertTrue(picker.minDate < picker.maxDate)
                }
                LabComponent.TIME_PICKER -> {
                    val picker = view as TimePicker
                    assertEquals(10, picker.hour)
                    assertEquals(30, picker.minute)
                    assertTrue(picker.is24HourView())
                    assertTrue(
                        "The original inline TimePicker must contain its controls",
                        picker.childCount > 0,
                    )
                }
                LabComponent.CALENDAR_VIEW -> {
                    val calendar = view as CalendarView
                    val selected =
                        GregorianCalendar(TimeZone.getDefault()).apply {
                            timeInMillis = calendar.date
                        }
                    assertEquals(2024, selected.get(Calendar.YEAR))
                    assertEquals(Calendar.JANUARY, selected.get(Calendar.MONTH))
                    assertEquals(15, selected.get(Calendar.DAY_OF_MONTH))
                    assertTrue(calendar.minDate < calendar.maxDate)
                }
                LabComponent.TEXT_CLOCK -> {
                    val clock = view as TextClock
                    assertEquals("HH:mm:ss", clock.format24Hour.toString())
                    assertEquals("HH:mm:ss", clock.format12Hour.toString())
                }
                LabComponent.ANALOG_CLOCK -> Unit
                LabComponent.DIGITAL_CLOCK -> assertTrue((view as DigitalClock).text.isNotEmpty())
                LabComponent.CHRONOMETER -> assertTrue((view as Chronometer).text.isNotEmpty())
                LabComponent.SCROLL_VIEW,
                LabComponent.HORIZONTAL_SCROLL_VIEW -> {
                    assertEquals(1, (view as ViewGroup).childCount)
                    val lines = view.getChildAt(0) as ViewGroup
                    assertEquals(24, lines.childCount)
                }
                LabComponent.VIEW_ANIMATOR,
                LabComponent.VIEW_SWITCHER,
                LabComponent.VIEW_FLIPPER -> {
                    val animator = view as ViewAnimator
                    assertEquals(0, animator.displayedChild)
                    assertEquals(component.switcherPageCount, animator.childCount)
                    repeat(animator.childCount) { index ->
                        assertEquals(
                            compose.activity.getString(R.string.switcher_page, index + 1),
                            (animator.getChildAt(index) as TextView).text.toString(),
                        )
                    }
                }
                LabComponent.TEXT_SWITCHER -> {
                    val switcher = view as TextSwitcher
                    assertEquals(2, switcher.childCount)
                    assertEquals(
                        compose.activity.getString(R.string.switcher_line, 1),
                        (switcher.currentView as TextView).text.toString(),
                    )
                }
                LabComponent.IMAGE_SWITCHER -> {
                    val switcher = view as ImageSwitcher
                    assertEquals(2, switcher.childCount)
                    assertNotNull((switcher.currentView as ImageView).drawable)
                }
                LabComponent.FRAME_LAYOUT,
                LabComponent.ABSOLUTE_LAYOUT -> assertEquals(2, (view as ViewGroup).childCount)
                LabComponent.LINEAR_LAYOUT -> {
                    val layout = view as LinearLayout
                    assertEquals(LinearLayout.HORIZONTAL, layout.orientation)
                    assertEquals(3, layout.childCount)
                }
                LabComponent.TABLE_LAYOUT -> {
                    val table = view as TableLayout
                    assertEquals(2, table.childCount)
                    repeat(2) { row ->
                        val tableRow = table.getChildAt(row) as TableRow
                        assertEquals(2, tableRow.childCount)
                    }
                }
                LabComponent.GRID_LAYOUT -> {
                    val grid = view as GridLayout
                    assertEquals(2, grid.columnCount)
                    assertEquals(4, grid.childCount)
                }
                LabComponent.RELATIVE_LAYOUT -> assertEquals(2, (view as RelativeLayout).childCount)
                LabComponent.SPACE -> assertEquals(View.VISIBLE, view.visibility)
                LabComponent.LIST_VIEW -> {
                    val list = view as ListView
                    assertEquals(6, list.adapter.count)
                    assertEquals(AbsListView.CHOICE_MODE_SINGLE, list.choiceMode)
                }
                LabComponent.GRID_VIEW -> {
                    val grid = view as GridView
                    assertEquals(9, grid.adapter.count)
                    assertEquals(3, grid.numColumns)
                }
                LabComponent.EXPANDABLE_LIST_VIEW -> {
                    val list = view as ExpandableListView
                    assertEquals(3, list.expandableListAdapter.groupCount)
                    assertEquals(2, list.expandableListAdapter.getChildrenCount(0))
                }
                LabComponent.ZOOM_CONTROLS -> {
                    val controls = view as ZoomControls
                    assertEquals(2, controls.childCount)
                    assertTrue(controls.getChildAt(0).isEnabled)
                    assertTrue(controls.getChildAt(1).isEnabled)
                }
                LabComponent.ZOOM_BUTTON -> assertTrue((view as ZoomButton).isEnabled)
                // The controller host holds the owner view; its controls attach
                // once the owner is laid out.
                LabComponent.ZOOM_BUTTONS_CONTROLLER ->
                    assertTrue((view as FrameLayout).childCount >= 1)
                LabComponent.ADAPTER_VIEW_FLIPPER ->
                    assertEquals(4, (view as AdapterViewFlipper).adapter.count)
                LabComponent.STACK_VIEW -> assertEquals(6, (view as StackView).adapter.count)
                LabComponent.TAB_HOST -> {
                    val host = view as TabHost
                    assertEquals(3, host.tabWidget.childCount)
                    assertEquals(0, host.currentTab)
                    assertEquals(
                        compose.activity.getString(R.string.tab_content, 1),
                        (host.tabContentView.getChildAt(0) as? TextView)?.text?.toString(),
                    )
                }
                LabComponent.GALLERY -> assertEquals(6, (view as Gallery).adapter.count)
                LabComponent.SLIDING_DRAWER -> {
                    val drawer = view as SlidingDrawer
                    assertNotNull(drawer.findViewById<View>(R.id.sliding_handle))
                    assertNotNull(drawer.findViewById<View>(R.id.sliding_content))
                }
                LabComponent.TWO_LINE_LIST_ITEM -> {
                    val item = view as TwoLineListItem
                    assertEquals(
                        compose.activity.getString(R.string.two_line_primary),
                        item.findViewById<TextView>(android.R.id.text1).text.toString(),
                    )
                    assertEquals(
                        compose.activity.getString(R.string.two_line_secondary),
                        item.findViewById<TextView>(android.R.id.text2).text.toString(),
                    )
                }
                LabComponent.TOOLBAR -> {
                    val toolbar = view as Toolbar
                    assertEquals(
                        compose.activity.getString(R.string.toolbar_title),
                        toolbar.title.toString(),
                    )
                    assertEquals(2, toolbar.menu.size())
                    assertNotNull(toolbar.navigationIcon)
                }
                LabComponent.ACTION_MENU_VIEW ->
                    assertEquals(2, (view as ActionMenuView).menu.size())
                LabComponent.QUICK_CONTACT_BADGE ->
                    assertNotNull((view as QuickContactBadge).drawable)
                LabComponent.WEB_VIEW -> Unit
                LabComponent.ICON,
                LabComponent.IMAGE_BUTTON -> {
                    assertNotNull((view as ImageView).drawable)
                    assertEquals("ic_input_add", view.contentDescription.toString())
                }
                else -> assertTrue((view as Button).text.isNotEmpty())
            }
        }
        if (component == LabComponent.CHECKED_TEXT_VIEW) {
            val missing =
                compose.runOnIdle {
                    compose.activity
                        .findViewById<CheckedTextView>(R.id.sample_left)
                        .checkMarkDrawable == null
                }
            if (missing)
                compose
                    .onNodeWithTag("checked_text_missing_mark_LEFT")
                    .assertTextEquals(
                        compose.activity.getString(R.string.checked_text_missing_mark)
                    )
            else compose.onNodeWithTag("checked_text_missing_mark_LEFT").assertDoesNotExist()
        }
        when (component) {
            LabComponent.DIALOG,
            LabComponent.DATE_PICKER_DIALOG,
            LabComponent.TIME_PICKER_DIALOG -> verifyNativeDialog(component)
            LabComponent.POPUP_MENU -> verifyNativeMenu()
            LabComponent.PLAIN_DIALOG -> verifyTransientWindow(Dialog::class.java)
            LabComponent.PROGRESS_DIALOG -> verifyTransientWindow(ProgressDialog::class.java)
            LabComponent.POPUP_WINDOW -> verifyPopupWindow()
            LabComponent.LIST_POPUP_WINDOW -> verifyListPopupWindow()
            else -> Unit
        }
    }

    private fun nativeClass(component: LabComponent): Class<out View> =
        when (component) {
            LabComponent.BUTTON,
            LabComponent.DIALOG,
            LabComponent.DATE_PICKER_DIALOG,
            LabComponent.TIME_PICKER_DIALOG,
            LabComponent.POPUP_MENU,
            LabComponent.PLAIN_DIALOG,
            LabComponent.PROGRESS_DIALOG,
            LabComponent.TOAST,
            LabComponent.POPUP_WINDOW,
            LabComponent.LIST_POPUP_WINDOW -> Button::class.java
            LabComponent.CHECKBOX -> CheckBox::class.java
            LabComponent.RADIO -> RadioGroup::class.java
            LabComponent.SWITCH -> Switch::class.java
            LabComponent.TEXT_FIELD -> EditText::class.java
            LabComponent.SLIDER -> SeekBar::class.java
            LabComponent.PROGRESS -> ProgressBar::class.java
            LabComponent.TOGGLE_BUTTON -> ToggleButton::class.java
            LabComponent.IMAGE_BUTTON -> ImageButton::class.java
            LabComponent.RATING -> RatingBar::class.java
            LabComponent.NUMBER_PICKER -> NumberPicker::class.java
            LabComponent.AUTOCOMPLETE -> AutoCompleteTextView::class.java
            LabComponent.MULTI_AUTOCOMPLETE -> MultiAutoCompleteTextView::class.java
            LabComponent.SPINNER -> Spinner::class.java
            LabComponent.ICON -> ImageView::class.java
            LabComponent.SEARCH_VIEW -> SearchView::class.java
            LabComponent.DATE_PICKER -> DatePicker::class.java
            LabComponent.TIME_PICKER -> TimePicker::class.java
            LabComponent.CALENDAR_VIEW -> CalendarView::class.java
            LabComponent.TEXT_CLOCK -> TextClock::class.java
            LabComponent.ANALOG_CLOCK -> AnalogClock::class.java
            LabComponent.DIGITAL_CLOCK -> DigitalClock::class.java
            LabComponent.CHRONOMETER -> Chronometer::class.java
            LabComponent.SCROLL_VIEW -> ScrollView::class.java
            LabComponent.HORIZONTAL_SCROLL_VIEW -> HorizontalScrollView::class.java
            LabComponent.VIEW_ANIMATOR -> ViewAnimator::class.java
            LabComponent.VIEW_SWITCHER -> ViewSwitcher::class.java
            LabComponent.VIEW_FLIPPER -> ViewFlipper::class.java
            LabComponent.TEXT_SWITCHER -> TextSwitcher::class.java
            LabComponent.IMAGE_SWITCHER -> ImageSwitcher::class.java
            LabComponent.FRAME_LAYOUT -> FrameLayout::class.java
            LabComponent.LINEAR_LAYOUT -> LinearLayout::class.java
            LabComponent.TABLE_LAYOUT -> TableLayout::class.java
            LabComponent.GRID_LAYOUT -> GridLayout::class.java
            LabComponent.RELATIVE_LAYOUT -> RelativeLayout::class.java
            LabComponent.SPACE -> Space::class.java
            LabComponent.ABSOLUTE_LAYOUT -> AbsoluteLayout::class.java
            LabComponent.LIST_VIEW -> ListView::class.java
            LabComponent.GRID_VIEW -> GridView::class.java
            LabComponent.EXPANDABLE_LIST_VIEW -> ExpandableListView::class.java
            LabComponent.ZOOM_CONTROLS -> ZoomControls::class.java
            LabComponent.ZOOM_BUTTON -> ZoomButton::class.java
            // ZoomButtonsController is not a View; the host frame carries the id.
            LabComponent.ZOOM_BUTTONS_CONTROLLER -> FrameLayout::class.java
            LabComponent.ADAPTER_VIEW_FLIPPER -> AdapterViewFlipper::class.java
            LabComponent.STACK_VIEW -> StackView::class.java
            LabComponent.TAB_HOST -> TabHost::class.java
            LabComponent.GALLERY -> Gallery::class.java
            LabComponent.SLIDING_DRAWER -> SlidingDrawer::class.java
            LabComponent.TWO_LINE_LIST_ITEM -> TwoLineListItem::class.java
            LabComponent.TOOLBAR -> Toolbar::class.java
            LabComponent.ACTION_MENU_VIEW -> ActionMenuView::class.java
            LabComponent.WEB_VIEW -> WebView::class.java
            LabComponent.QUICK_CONTACT_BADGE -> QuickContactBadge::class.java
            LabComponent.TEXT -> TextView::class.java
            LabComponent.CHECKED_TEXT_VIEW -> CheckedTextView::class.java
            else -> error("No ordinary framework rendering assertion for ${component.name}")
        }

    private fun assertNativeTheme(view: View, family: DesignFamily, component: LabComponent) {
        assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
        val expectedTheme =
            ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
        val style =
            when (component) {
                LabComponent.CHECKBOX -> android.R.attr.checkboxStyle
                LabComponent.TEXT -> android.R.attr.textViewStyle
                LabComponent.CHECKED_TEXT_VIEW -> android.R.attr.checkedTextViewStyle
                LabComponent.RADIO -> android.R.attr.radioButtonStyle
                LabComponent.SWITCH -> android.R.attr.switchStyle
                LabComponent.TEXT_FIELD -> android.R.attr.editTextStyle
                LabComponent.SLIDER -> android.R.attr.seekBarStyle
                LabComponent.PROGRESS -> android.R.attr.progressBarStyleHorizontal
                LabComponent.DATE_PICKER,
                LabComponent.DATE_PICKER_DIALOG -> android.R.attr.datePickerStyle
                LabComponent.CALENDAR_VIEW -> android.R.attr.calendarViewStyle
                LabComponent.TIME_PICKER,
                LabComponent.TIME_PICKER_DIALOG -> android.R.attr.timePickerStyle
                LabComponent.POPUP_MENU -> android.R.attr.popupMenuStyle
                LabComponent.TOGGLE_BUTTON -> android.R.attr.buttonStyleToggle
                LabComponent.IMAGE_BUTTON -> android.R.attr.imageButtonStyle
                LabComponent.RATING -> android.R.attr.ratingBarStyle
                LabComponent.NUMBER_PICKER -> android.R.attr.numberPickerStyle
                LabComponent.AUTOCOMPLETE,
                LabComponent.MULTI_AUTOCOMPLETE -> android.R.attr.autoCompleteTextViewStyle
                LabComponent.SPINNER -> android.R.attr.spinnerStyle
                LabComponent.SEARCH_VIEW -> android.R.attr.searchViewStyle
                LabComponent.TEXT_CLOCK,
                LabComponent.DIGITAL_CLOCK,
                LabComponent.CHRONOMETER -> android.R.attr.textViewStyle
                // AnalogClock styles its dial from an internal attribute; the generic
                // fallback still proves the view runs inside the family's theme.
                LabComponent.ANALOG_CLOCK -> android.R.attr.buttonStyle
                else -> android.R.attr.buttonStyle
            }
        listOf(android.R.attr.colorBackground, style).forEach { attribute ->
            val expected = TypedValue()
            val actual = TypedValue()
            assertEquals(
                expectedTheme.resolveAttribute(attribute, expected, true),
                view.context.theme.resolveAttribute(attribute, actual, true),
            )
            assertEquals(expected.type, actual.type)
            assertEquals(expected.resourceId, actual.resourceId)
            assertEquals(expected.data, actual.data)
        }
    }

    private fun verifyNativeDialog(component: LabComponent) {
        onView(withId(R.id.sample_left)).perform(nativeClick())
        lateinit var opened: AlertDialog
        compose.runOnIdle {
            opened = compose.activity.findViewById<Button>(R.id.sample_left).tag as AlertDialog
            assertTrue(opened.isShowing)
            assertNotNull(opened.window)
            when (component) {
                LabComponent.DATE_PICKER_DIALOG -> {
                    assertEquals(DatePickerDialog::class.java, opened.javaClass)
                    val picker = (opened as DatePickerDialog).datePicker
                    assertEquals(DatePicker::class.java, picker.javaClass)
                    assertEquals(2024, picker.year)
                    assertEquals(0, picker.month)
                    assertEquals(15, picker.dayOfMonth)
                }
                LabComponent.TIME_PICKER_DIALOG -> {
                    assertEquals(TimePickerDialog::class.java, opened.javaClass)
                    val picker =
                        requireNotNull(findTimePicker(requireNotNull(opened.window).decorView))
                    assertEquals(TimePicker::class.java, picker.javaClass)
                    assertEquals(10, picker.hour)
                    assertEquals(30, picker.minute)
                    assertTrue(picker.is24HourView())
                }
                else -> {
                    assertEquals(AlertDialog::class.java, opened.javaClass)
                    assertEquals(
                        compose.activity.getString(R.string.dialog_confirm),
                        opened.getButton(AlertDialog.BUTTON_POSITIVE).text.toString(),
                    )
                }
            }
        }
        onView(withId(android.R.id.button2))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(nativeClick())
        compose.runOnIdle { assertFalse(opened.isShowing) }
    }

    // The plain Dialog and ProgressDialog expose no Espresso-friendly buttons;
    // the sample keeps the created window on the trigger button's tag.
    private fun verifyTransientWindow(type: Class<out Dialog>) {
        onView(withId(R.id.sample_left)).perform(nativeClick())
        compose.runOnIdle {
            val opened = compose.activity.findViewById<Button>(R.id.sample_left).tag as Dialog
            assertEquals(type, opened.javaClass)
            assertTrue(opened.isShowing)
            assertNotNull(opened.window)
            opened.dismiss()
            assertFalse(opened.isShowing)
        }
    }

    // The raw popup objects live on the trigger button's tag, like the dialogs.
    private fun verifyPopupWindow() {
        onView(withId(R.id.sample_left)).perform(nativeClick())
        compose.runOnIdle {
            val opened = compose.activity.findViewById<Button>(R.id.sample_left).tag as PopupWindow
            assertEquals(PopupWindow::class.java, opened.javaClass)
            assertTrue(opened.isShowing)
            opened.dismiss()
            assertFalse(opened.isShowing)
        }
    }

    private fun verifyListPopupWindow() {
        onView(withId(R.id.sample_left)).perform(nativeClick())
        compose.runOnIdle {
            val opened =
                compose.activity.findViewById<Button>(R.id.sample_left).tag as ListPopupWindow
            assertEquals(ListPopupWindow::class.java, opened.javaClass)
            assertTrue(opened.isShowing)
            assertEquals(4, opened.listView?.adapter?.count)
            opened.dismiss()
            assertFalse(opened.isShowing)
        }
    }

    private fun findTimePicker(view: View): TimePicker? {
        if (view is TimePicker) return view
        if (view is ViewGroup)
            for (index in 0 until view.childCount) {
                findTimePicker(view.getChildAt(index))?.let {
                    return it
                }
            }
        return null
    }

    private fun verifyNativeMenu() {
        onView(withId(R.id.sample_left)).perform(nativeClick())
        compose.runOnIdle {
            val popup = compose.activity.findViewById<Button>(R.id.sample_left).tag as PopupMenu
            assertEquals(PopupMenu::class.java, popup.javaClass)
            assertEquals(3, popup.menu.size())
            assertTrue(popup.menu.findItem(1).isEnabled)
            assertTrue(popup.menu.findItem(2).isEnabled)
            assertFalse(popup.menu.findItem(3).isEnabled)
        }
        onData(nativeMenuItem(3))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .check(matches(not(isEnabled())))
            .check(
                matches(
                    hasDescendant(
                        withText(compose.activity.getString(R.string.menu_disabled_choice))
                    )
                )
            )
        onData(nativeMenuItem(2))
            .inRoot(isPlatformPopup())
            .check(matches(hasDescendant(withText(compose.activity.getString(R.string.option_b)))))
            .perform(nativeClick())
        compose.runOnIdle {
            assertNull(compose.activity.findViewById<Button>(R.id.sample_left).tag)
        }
        assertMenuSelection()
    }

    private fun nativeMenuItem(id: Int) =
        object : TypeSafeMatcher<Any>() {
            override fun matchesSafely(item: Any) = item is MenuItem && item.itemId == id

            override fun describeTo(description: Description) {
                description.appendText("public framework menu item $id")
            }
        }

    private fun verifyLibrary(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        if (component == LabComponent.RADIO) {
            for (option in 1..2) {
                val radio = displayed("library_LEFT_$option")
                radio
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assertIsNotSelected()
                    .assert(role(Role.RadioButton))
                    .assertContentDescriptionEquals(
                        compose.activity.getString(
                            if (option == 1) R.string.option_a else R.string.option_b
                        )
                    )
            }
            return
        }
        val inlineDate =
            component == LabComponent.DATE_PICKER || component == LabComponent.DATE_RANGE_PICKER
        val inlineTime = component.isInlineTime
        if (inlineDate)
            compose.onNodeWithTag("date_viewport_LEFT").performScrollTo().assertIsDisplayed()
        if (inlineTime)
            compose.onNodeWithTag("time_viewport_LEFT").performScrollTo().assertIsDisplayed()
        val sample = displayed("library_LEFT", scroll = !inlineDate && !inlineTime)
        // Runtime semantics complement the source/version labels; labels alone cannot prove a
        // renderer.
        when {
            component == LabComponent.TIME_PICKER -> {
                listOf(true to "10", false to "30").forEach { (hour, value) ->
                    val selector =
                        compose.onNode(
                            hasAnyAncestor(hasTestTag("library_LEFT")) and
                                hasClickAction() and
                                hasContentDescription(if (hour) "Select hour" else "Select minutes")
                        )
                    revealInlineTimeControl(selector)
                        .assertHasClickAction()
                        .assertIsEnabled()
                        .assertTextContains(value, substring = true)
                }
                val dial =
                    compose.onNode(
                        hasAnyAncestor(hasTestTag("library_LEFT")) and
                            hasClickAction() and
                            hasContentDescription("9 hours")
                    )
                revealInlineTimeControl(dial).assertHasClickAction().assertIsEnabled()
            }
            component == LabComponent.TIME_INPUT -> {
                assertTimeInputField(hour = true, expected = 10, inline = true)
                assertTimeInputField(hour = false, expected = 30, inline = true)
            }
            component == LabComponent.TEXT -> {
                sample
                    .assertTextEquals(compose.activity.getString(R.string.sample_display_text))
                    .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                    .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult))
                val size = sample.fetchSemanticsNode().size
                assertTrue(
                    "The original library text must have positive measured bounds",
                    size.width > 0 && size.height > 0,
                )
                val layouts = mutableListOf<TextLayoutResult>()
                sample.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                    assertTrue(action(layouts))
                }
                val layout = layouts.single()
                assertEquals(
                    compose.activity.getString(R.string.sample_display_text),
                    layout.layoutInput.text.text,
                )
                assertTrue(layout.lineCount >= 3)
                assertFalse(layout.hasVisualOverflow)
            }
            inlineDate -> {
                // Inspect an original day control, not only the host's source caption.
                val day =
                    compose.onNode(
                        hasAnyAncestor(hasTestTag("library_LEFT")) and
                            hasText("January 15, 2024", substring = true) and
                            hasClickAction()
                    )
                day.assertIsDisplayed()
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assert(role(Role.Button))
                val size = day.fetchSemanticsNode().size
                assertTrue(
                    "The original calendar day must have positive measured bounds",
                    size.width > 0 && size.height > 0,
                )
                if (component == LabComponent.DATE_PICKER) day.assertIsSelected()
                else day.assertIsNotSelected()
                compose
                    .onNode(
                        hasAnyAncestor(hasTestTag("library_LEFT")) and
                            hasContentDescription("Switch to text input mode")
                    )
                    .assertHasClickAction()
            }
            component.isDivider -> {
                sample.assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                val size = sample.fetchSemanticsNode().size
                if (component == LabComponent.VERTICAL_DIVIDER)
                    assertTrue(size.height > size.width * 10)
                else assertTrue(size.width > size.height * 10)
            }
            component.isBadge -> {
                sample.assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                if (component.isCountedBadge)
                    compose
                        .onNodeWithTag("badge_count_LEFT", useUnmergedTree = true)
                        .assertTextEquals("7")
                if (component == LabComponent.BADGED_BOX)
                    compose
                        .onNodeWithTag("badge_icon_LEFT", useUnmergedTree = true)
                        .assertContentDescriptionEquals("Add")
                if (component == LabComponent.DOT_BADGE)
                    compose
                        .onNodeWithTag("badge_count_LEFT", useUnmergedTree = true)
                        .assertDoesNotExist()
            }
            component.isDeterminateProgress || component.isIndeterminateProgress -> {
                val info =
                    sample.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
                if (component.isIndeterminateProgress)
                    assertEquals(ProgressBarRangeInfo.Indeterminate, info)
                else {
                    assertEquals(0.5f, info.current, 0.001f)
                    assertEquals(0f..1f, info.range)
                }
            }
            component == LabComponent.ICON -> sample.assertContentDescriptionEquals("Add")
            component.isContainer ->
                sample
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
                    .assertTextContains(
                        compose.activity.getString(R.string.sample_container_content),
                        substring = true,
                    )
            component.isSecureInput ->
                sample
                    .assertIsEnabled()
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            component == LabComponent.TEXT_FIELD ||
                component == LabComponent.OUTLINED_TEXT_FIELD -> {
                sample
                    .assertIsEnabled()
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
                assertEquals(
                    "",
                    sample.fetchSemanticsNode().config[SemanticsProperties.EditableText].text,
                )
            }
            component == LabComponent.SLIDER -> {
                sample
                    .assertIsEnabled()
                    .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
                val info =
                    sample.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
                assertEquals(50f, info.current, 0f)
                assertEquals(0f..100f, info.range)
            }
            component == LabComponent.RANGE_SLIDER -> {
                val thumbs =
                    compose
                        .onAllNodes(
                            hasAnyAncestor(hasTestTag("library_LEFT")) and
                                SemanticsMatcher.keyIsDefined(
                                    SemanticsProperties.ProgressBarRangeInfo
                                ),
                            useUnmergedTree = true,
                        )
                        .fetchSemanticsNodes()
                assertEquals(2, thumbs.size)
                assertEquals(
                    listOf(20f, 80f),
                    thumbs
                        .map { it.config[SemanticsProperties.ProgressBarRangeInfo].current }
                        .sorted(),
                )
                assertTrue(thumbs.all { it.config.contains(SemanticsActions.SetProgress) })
            }
            component == LabComponent.CHECKBOX ||
                component == LabComponent.SWITCH ||
                component == LabComponent.TRI_STATE_CHECKBOX -> {
                sample
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assertIsOff()
                    .assert(
                        role(if (component == LabComponent.SWITCH) Role.Switch else Role.Checkbox)
                    )
                sample.assertContentDescriptionEquals(
                    compose.activity.getString(
                        when (component) {
                            LabComponent.CHECKBOX -> R.string.sample_checkbox
                            LabComponent.SWITCH -> R.string.sample_switch
                            else -> R.string.component_tri_state_checkbox
                        }
                    )
                )
            }
            component.isIconToggle ->
                sample
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assertIsOff()
                    .assertContentDescriptionEquals("Add")
            component == LabComponent.FILTER_CHIP ||
                component == LabComponent.ELEVATED_FILTER_CHIP ||
                component == LabComponent.INPUT_CHIP -> {
                sample
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assertIsNotSelected()
                    .assert(role(Role.Checkbox))
            }
            component == LabComponent.SINGLE_SEGMENTED ||
                component == LabComponent.MULTI_SEGMENTED -> {
                for (index in 0..2) {
                    val choice =
                        displayed("library_LEFT_$index")
                            .assertTextEquals(('A'.code + index).toChar().toString())
                            .assertHasClickAction()
                            .assertIsEnabled()
                    if (component == LabComponent.SINGLE_SEGMENTED)
                        choice.assertIsNotSelected().assert(role(Role.RadioButton))
                    else choice.assertIsOff()
                }
            }
            component == LabComponent.DIALOG ||
                component == LabComponent.DATE_PICKER_DIALOG ||
                component == LabComponent.TIME_PICKER_DIALOG ||
                component == LabComponent.POPUP_MENU -> {
                sample.assertHasClickAction().assertIsEnabled().assert(role(Role.Button))
                sample.performTouchInput { click() }
                verifyLibraryWindow(component, family)
            }
            else -> {
                sample.assertHasClickAction().assertIsEnabled()
                if (component.usesIcon) sample.assertContentDescriptionEquals("Add")
                else sample.assert(role(Role.Button))
            }
        }
    }

    private fun verifyLibraryWindow(component: LabComponent, family: DesignFamily) {
        when (component) {
            LabComponent.DIALOG -> {
                compose
                    .onNodeWithText(
                        compose.activity.getString(R.string.dialog_library_message, family.label)
                    )
                    .assertIsDisplayed()
                compose.onNodeWithTag("dialog_confirm").assertHasClickAction().assertIsEnabled()
                compose.onNodeWithTag("dialog_cancel").assertIsDisplayed().performTouchInput {
                    click()
                }
                compose.onNodeWithTag("dialog_cancel").assertDoesNotExist()
            }
            LabComponent.DATE_PICKER_DIALOG -> {
                displayed("date_dialog_LEFT", scroll = false)
                displayed("date_picker_LEFT", scroll = false)
                compose
                    .onNode(
                        hasContentDescription("Switch to text input mode") and
                            hasAnyAncestor(hasTestTag("date_picker_LEFT"))
                    )
                    .assertIsDisplayed()
                    .assertHasClickAction()
                    .performTouchInput { click() }
                compose
                    .onNode(hasAnyAncestor(hasTestTag("date_picker_LEFT")) and hasSetTextAction())
                    .assertIsDisplayed()
                closeSoftKeyboard()
                compose.onNodeWithTag("date_cancel_LEFT").assertIsDisplayed().performTouchInput {
                    click()
                }
                compose.onNodeWithTag("date_dialog_LEFT").assertDoesNotExist()
            }
            LabComponent.TIME_PICKER_DIALOG -> {
                displayed("time_dialog_LEFT", scroll = false)
                val clock = compose.onAllNodes(hasTestTag("time_picker_LEFT")).fetchSemanticsNodes()
                if (clock.isNotEmpty()) {
                    displayed("time_picker_LEFT", scroll = false)
                    compose
                        .onNodeWithTag("time_mode_LEFT")
                        .assertHasClickAction()
                        .performTouchInput { click() }
                }
                displayed("time_input_LEFT", scroll = false)
                assertTimeInputField(hour = true, expected = 10)
                assertTimeInputField(hour = false, expected = 30)
                closeSoftKeyboard()
                compose.onNodeWithTag("time_cancel_LEFT").assertIsDisplayed().performTouchInput {
                    click()
                }
                compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
            }
            LabComponent.POPUP_MENU -> {
                displayed("menu_LEFT", scroll = false)
                listOf(
                        "A" to R.string.option_a,
                        "B" to R.string.option_b,
                        "DISABLED" to R.string.menu_disabled_choice,
                    )
                    .forEach { (tag, text) ->
                        val item =
                            displayed("menu_item_LEFT_$tag")
                                .assertTextEquals(compose.activity.getString(text))
                                .assertHasClickAction()
                                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
                        if (tag == "DISABLED") item.assertIsNotEnabled() else item.assertIsEnabled()
                    }
                compose
                    .onNodeWithTag("menu_item_source_LEFT")
                    .assertTextEquals(
                        if (family == DesignFamily.MATERIAL2)
                            "androidx.compose.material.DropdownMenuItem"
                        else "androidx.compose.material3.DropdownMenuItem"
                    )
                displayed("menu_item_LEFT_B").performTouchInput { click() }
                compose.onNodeWithTag("menu_LEFT").assertDoesNotExist()
                assertMenuSelection()
            }
            else -> error("No library window assertion for ${component.name}")
        }
    }

    private fun displayed(tag: String, scroll: Boolean = true): SemanticsNodeInteraction {
        val node = compose.onNodeWithTag(tag)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed()
        val size = node.fetchSemanticsNode().size
        assertTrue("$tag must have positive measured bounds", size.width > 0 && size.height > 0)
        return node
    }

    private fun role(expected: Role) =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, expected)

    private fun assertTimeInputField(hour: Boolean, expected: Int, inline: Boolean = false) {
        // The original inactive editor is unplaced; activate its visible selector before
        // inspection.
        val rootTag = if (inline) "library_LEFT" else "time_input_LEFT"
        val selector =
            hasAnyAncestor(hasTestTag(rootTag)) and
                hasContentDescription(if (hour) "Select hour" else "Select minutes") and
                hasClickAction()
        if (compose.onAllNodes(selector).fetchSemanticsNodes().isNotEmpty()) {
            val control = compose.onNode(selector)
            if (inline) revealInlineTimeControl(control) else control.assertIsDisplayed()
            control.performTouchInput { click() }
        }
        val field =
            compose.onNode(
                hasAnyAncestor(hasTestTag(rootTag)) and
                    hasContentDescription(if (hour) "for hour" else "for minutes") and
                    hasSetTextAction()
            )
        if (inline) revealInlineTimeControl(field) else field.assertIsDisplayed()
        val size = field.fetchSemanticsNode().size
        assertTrue(
            "The active time editor must have positive measured bounds",
            size.width > 0 && size.height > 0,
        )
        assertEquals(
            expected,
            field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text.toInt(),
        )
        closeSoftKeyboard()
    }

    private fun revealInlineTimeControl(
        control: SemanticsNodeInteraction
    ): SemanticsNodeInteraction {
        control.performScrollTo()
        val page = compose.onNodeWithTag("compare_screen")
        // Reach the original control through its horizontal viewport and the outer host.
        // This host scroll action is not a picker interaction or an OS display resize.
        repeat(4) {
            val node = control.fetchSemanticsNode()
            val viewport = page.fetchSemanticsNode().boundsInRoot
            val top = node.positionInRoot.y
            val bottom = top + node.size.height
            val delta =
                when {
                    top < viewport.top -> top - viewport.top
                    bottom > viewport.bottom -> bottom - viewport.bottom
                    else -> 0f
                }
            if (delta != 0f)
                page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, delta) }
        }
        val bounds = control.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "An original inline time control must have positive visible bounds",
            bounds.width > 0f && bounds.height > 0f,
        )
        return control
    }

    private fun assertMenuSelection() {
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals(
                compose.activity.getString(
                    R.string.status_menu,
                    compose.activity.getString(R.string.option_b),
                    compose.activity.getString(R.string.sample_state_selected),
                )
            )
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun recordCoverage(
        family: DesignFamily,
        supported: Int,
        unsupported: Int,
        skipped: Int,
    ) {
        val metrics = compose.activity.resources.displayMetrics
        InstrumentationRegistry.getInstrumentation()
            .sendStatus(
                0,
                Bundle().apply {
                    putString("catalog_smoke_provider", family.selectionLabel)
                    putInt("catalog_smoke_supported", supported)
                    putInt("catalog_smoke_unsupported", unsupported)
                    putInt("catalog_smoke_separate_animation", skipped)
                    putString("catalog_smoke_os_build", Build.FINGERPRINT)
                    putString("catalog_smoke_device", "${Build.MANUFACTURER} ${Build.MODEL}")
                    putInt(
                        "catalog_smoke_target_sdk",
                        compose.activity.applicationInfo.targetSdkVersion,
                    )
                    putString(
                        "catalog_smoke_display",
                        "${metrics.widthPixels}x${metrics.heightPixels} at ${metrics.densityDpi}dpi, font scale ${compose.activity.resources.configuration.fontScale}",
                    )
                },
            )
    }
}
