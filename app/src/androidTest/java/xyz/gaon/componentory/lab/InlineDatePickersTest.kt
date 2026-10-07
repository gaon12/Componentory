package xyz.gaon.componentory.lab

import android.graphics.Rect
import android.os.SystemClock
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.action.ViewActions.swipeUp as nativeSwipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import org.hamcrest.Matchers.sameInstance
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.navigation.ComponentoryApp
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class InlineDatePickersTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val initial = TestDate(2024, 1, 15)
    private val january22 = TestDate(2024, 1, 22)
    private val leapDay = TestDate(2024, 2, 29)
    private val march1 = TestDate(2024, 3, 1)
    private val march12 = TestDate(2024, 3, 12)

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun nativeDatePickerUsesSelectedThemeAndOriginalSpinnerOrCalendarGestures() {
        nativeFamilies.forEach { family ->
            configureComparison(LabComponent.DATE_PICKER, family, DesignFamily.MATERIAL3)
            val picker = nativeRoot<DatePicker>("LEFT")
            assertNativeIdentity(
                "LEFT",
                family,
                "DatePicker",
                android.R.attr.datePickerStyle,
                picker,
            )
            assertNativeDate("LEFT", initial)
            showPicker("LEFT")

            if (family == DesignFamily.MATERIAL) {
                touchNativeDay("LEFT", january22)
                assertNativeDate("LEFT", january22)
            } else {
                // The Classic theme uses the original increment buttons; Holo uses a wheel.
                // No hidden mode getter or private view ID is used to choose the control.
                val day =
                    compose.runOnIdle {
                        descendants(picker).filterIsInstance<NumberPicker>().single {
                            it.value == 15
                        }
                    }
                if (family == DesignFamily.CLASSIC) {
                    val button =
                        compose.runOnIdle {
                            descendants(day).filterIsInstance<ImageButton>().single {
                                it.contentDescription?.toString() == "Increase day" &&
                                    it.isShown &&
                                    it.isEnabled
                            }
                        }
                    onView(sameInstance(button)).perform(nativeClick())
                    assertNativeDate("LEFT", TestDate(2024, 1, 16))
                } else onView(sameInstance(day)).perform(nativeSwipeUp())
                compose.waitForIdle()
                assertTrue("The original day spinner must change", nativeDate("LEFT").day != 15)
            }
            val selected = nativeDate("LEFT")
            assertDateFeedback("LEFT", selected)
            assertDateFeedback("RIGHT", initial)
            recreateActivity()
            assertNativeDate("LEFT", selected)
            assertDateFeedback("LEFT", selected)

            setEnabled(false)
            assertTrue(
                "DatePicker receives its public enabled flag",
                !nativeRoot<DatePicker>("LEFT").isEnabled,
            )
            compose.onNodeWithTag("date_enabled_note_LEFT").assertDoesNotExist()
            // A root flag does not prove that every framework calendar child blocks touch.
            setEnabled(true)
            assertTrue(nativeRoot<DatePicker>("LEFT").isEnabled)
            assertNativeDate("LEFT", selected)
            resetSamples()
            assertNativeDate("LEFT", initial)
        }
    }

    @Test
    fun calendarViewKeepsCivilDateAndMaterialDayTouchWhileHostEnabledIsOff() {
        nativeFamilies.forEach { family ->
            configureComparison(LabComponent.CALENDAR_VIEW, family, DesignFamily.MATERIAL3)
            val calendar = nativeRoot<CalendarView>("LEFT")
            assertNativeIdentity(
                "LEFT",
                family,
                "CalendarView",
                android.R.attr.calendarViewStyle,
                calendar,
            )
            assertEquals(initial, nativeCalendarDate("LEFT"))
            assertDateFeedback("LEFT", initial)
            setEnabled(false)
            assertTrue(
                "CalendarView has no sample-wide disabled delegate",
                nativeRoot<CalendarView>("LEFT").isEnabled,
            )
            compose
                .onNodeWithTag("date_enabled_note_LEFT")
                .assertTextEquals(englishNames.enabledNote)
            recreateActivity()
            assertEquals(initial, nativeCalendarDate("LEFT"))
        }

        // Only the Material provider's original virtual day nodes are touched here. The legacy
        // week renderer's private geometry is not reproduced to manufacture gesture evidence.
        touchNativeDay("LEFT", january22)
        assertEquals(january22, nativeCalendarDate("LEFT"))
        assertDateFeedback("LEFT", january22)
        compose.onNodeWithTag("enabled").assertIsOff()
        recreateActivity()
        assertEquals(january22, nativeCalendarDate("LEFT"))
        assertDateFeedback("LEFT", january22)
        resetSamples()
        assertEquals(initial, nativeCalendarDate("LEFT"))
    }

    @Test
    fun material3SinglePickerKeepsOriginalCalendarInputAndNullableSelection() {
        configureComparison(
            LabComponent.DATE_PICKER,
            DesignFamily.MATERIAL3,
            DesignFamily.MATERIAL3,
        )
        assertLibraryIdentity("LEFT", "DatePicker")
        touchLibraryDay("LEFT", 22)
        assertDateFeedback("LEFT", january22)
        assertDateFeedback("RIGHT", initial)
        libraryDay("LEFT", 22)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))

        switchToInput("LEFT")
        replaceDateInput("LEFT", "02292024")
        assertDateFeedback("LEFT", leapDay)
        assertInputDigits("LEFT", "02292024")
        replaceDateInput("LEFT", "")
        assertDateFeedback("LEFT", null)
        recreateActivity()
        assertInputDigits("LEFT", "")
        assertDateFeedback("LEFT", null)
        assertDateFeedback("RIGHT", initial)

        setEnabled(false)
        dateInput("LEFT").assertIsEnabled()
        compose.onNodeWithTag("date_enabled_note_LEFT").assertTextEquals(englishNames.enabledNote)
        replaceDateInput("LEFT", "01222024")
        assertDateFeedback("LEFT", january22)
        compose.onNodeWithTag("enabled").assertIsOff()
        resetSamples()
        assertDateFeedback("LEFT", initial)
        inputToggle("LEFT").assertExists()
        libraryDay("LEFT", 15)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }

    @Test
    fun material3RangeKeepsEmptyPartialCompleteSameDayAndIndependentInputSelections() {
        configureComparison(
            LabComponent.DATE_RANGE_PICKER,
            DesignFamily.MATERIAL3,
            DesignFamily.MATERIAL3,
        )
        assertLibraryIdentity("LEFT", "DateRangePicker")
        assertRangeFeedback("LEFT", null, null)
        touchLibraryDay("LEFT", 15, range = true)
        assertRangeFeedback("LEFT", initial, null)
        copyInputs("LEFT_TO_RIGHT")
        assertRangeFeedback("RIGHT", initial, null)
        touchLibraryDay("LEFT", 22, range = true)
        assertRangeFeedback("LEFT", initial, january22)
        assertRangeFeedback("RIGHT", initial, null)
        copyInputs("LEFT_TO_RIGHT")
        assertRangeFeedback("RIGHT", initial, january22)

        // A completed range starts a new selection on the next original day click.
        touchLibraryDay("LEFT", 22, range = true)
        assertRangeFeedback("LEFT", january22, null)
        touchLibraryDay("LEFT", 22, range = true)
        assertRangeFeedback("LEFT", january22, january22)
        switchToInput("LEFT")
        // Clear the old end first; entering a later start against that end is originally invalid.
        replaceDateInput("LEFT", "", "End date")
        replaceDateInput("LEFT", "03012024", "Start date")
        replaceDateInput("LEFT", "03122024", "End date")
        assertRangeFeedback("LEFT", march1, march12)
        assertInputDigits("LEFT", "03012024", "Start date")
        assertInputDigits("LEFT", "03122024", "End date")
        recreateActivity()
        assertRangeFeedback("LEFT", march1, march12)
        assertRangeFeedback("RIGHT", initial, january22)
        assertInputDigits("LEFT", "03122024", "End date")

        replaceDateInput("LEFT", "", "Start date")
        assertRangeFeedback("LEFT", null, null)
        copyInputs("LEFT_TO_RIGHT")
        assertRangeFeedback("RIGHT", null, null)
        resetSamples()
        assertRangeFeedback("LEFT", null, null)
        assertRangeFeedback("RIGHT", null, null)
        inputToggle("LEFT").assertExists()
    }

    @Test
    fun detailAndDirectionalCopiesKeepNullableDatesWithoutInventingNativeSelection() {
        openDetail(LabComponent.DATE_PICKER, DesignFamily.MATERIAL3)
        switchToInput("LEFT")
        replaceDateInput("LEFT", "02292024")
        setEnabled(false)
        enterComparison()
        assertDateFeedback("LEFT", leapDay)
        assertDateFeedback("RIGHT", initial)
        compose.onNodeWithTag("enabled").assertIsOff()
        // A copied selection starts with the original calendar, rather than the source editor.
        inputToggle("LEFT").assertExists()
        chooseFamily("RIGHT", DesignFamily.MATERIAL)
        copyInputs("LEFT_TO_RIGHT")
        assertNativeDate("RIGHT", leapDay)
        assertDateFeedback("RIGHT", leapDay)
        assertTrue(!nativeRoot<DatePicker>("RIGHT").isEnabled)
        switchToInput("LEFT")
        replaceDateInput("LEFT", "")
        assertBlockedCopy("LEFT_TO_RIGHT", englishNames.dateRequired)
        assertDateFeedback("LEFT", null)
        assertNativeDate("RIGHT", leapDay)
        copyInputs("RIGHT_TO_LEFT")
        assertDateFeedback("LEFT", leapDay)
        inputToggle("LEFT").assertExists()
        recreateActivity()
        assertDateFeedback("LEFT", leapDay)
        assertNativeDate("RIGHT", leapDay)
        resetSamples()
        assertDateFeedback("LEFT", initial)
        assertNativeDate("RIGHT", initial)

        openDetail(LabComponent.DATE_RANGE_PICKER, DesignFamily.MATERIAL3)
        switchToInput("LEFT")
        replaceDateInput("LEFT", "03012024", "Start date")
        assertRangeFeedback("LEFT", march1, null)
        enterComparison()
        // Entering from detail keeps the remembered right family, which cannot host
        // the Material 3-only range picker, so pick a supported family explicitly.
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        assertRangeFeedback("LEFT", march1, null)
        assertRangeFeedback("RIGHT", null, null)
        copyInputs("LEFT_TO_RIGHT")
        assertRangeFeedback("RIGHT", march1, null)
        recreateActivity()
        assertRangeFeedback("LEFT", march1, null)
        assertRangeFeedback("RIGHT", march1, null)
        resetSamples()
        assertRangeFeedback("LEFT", null, null)
        assertRangeFeedback("RIGHT", null, null)
    }

    @Test
    fun fiveSettingsLanguagesExplainEnabledScopeAndNarrowHostKeepsOriginalRangeFieldsReachable() {
        localizedNames.forEach { names ->
            changeLanguage(names.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configureComparison(
                LabComponent.DATE_PICKER,
                DesignFamily.MATERIAL3,
                DesignFamily.MATERIAL3,
            )
            setEnabled(false)
            compose.onNodeWithTag("date_enabled_note_LEFT").assertTextEquals(names.enabledNote)
            touchLibraryDay("LEFT", 22)
            assertDateFeedback("LEFT", january22, names)
            compose.onNodeWithTag("enabled").assertIsOff()
            configureComparison(
                LabComponent.DATE_RANGE_PICKER,
                DesignFamily.MATERIAL3,
                DesignFamily.MATERIAL3,
            )
            compose.onNodeWithTag("date_enabled_note_LEFT").assertTextEquals(names.enabledNote)
            assertRangeFeedback("LEFT", null, null, names)
            configureComparison(
                LabComponent.CALENDAR_VIEW,
                DesignFamily.MATERIAL,
                DesignFamily.MATERIAL3,
            )
            setEnabled(false)
            compose.onNodeWithTag("date_enabled_note_LEFT").assertTextEquals(names.enabledNote)
            assertTrue(nativeRoot<CalendarView>("LEFT").isEnabled)
            assertDateFeedback("LEFT", initial, names)
        }

        changeLanguage(AppLanguage.ENGLISH)
        // This is the real app in a bounded Compose host, not a device display setting or
        // historical capture. Both original range editors must remain reachable at large font.
        compose.runOnUiThread {
            compose.activity.setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale = 2f)
                ) {
                    Box(Modifier.width(360.dp)) { ComponentoryApp() }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav_compare").performClick()
        configureComparison(
            LabComponent.DATE_RANGE_PICKER,
            DesignFamily.MATERIAL3,
            DesignFamily.MATERIAL3,
        )
        showPicker("LEFT")
        val host = compose.onNodeWithTag("compare_screen").fetchSemanticsNode().boundsInRoot
        val viewport = compose.onNodeWithTag("date_viewport_LEFT").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The original preview scroll viewport must stay in the host",
            viewport.left >= host.left && viewport.right <= host.right,
        )
        switchToInput("LEFT")
        replaceDateInput("LEFT", "03012024", "Start date")
        replaceDateInput("LEFT", "03122024", "End date")
        assertInputDigits("LEFT", "03012024", "Start date")
        assertInputDigits("LEFT", "03122024", "End date")
        assertRangeFeedback("LEFT", march1, march12)
        showPicker("LEFT")
        val picker = compose.onNodeWithTag("library_LEFT").fetchSemanticsNode()
        val density = compose.activity.resources.displayMetrics.density
        assertTrue(
            "The host preserves the original calendar's readable minimum width",
            picker.size.width >= 360f * density - 1f,
        )
        assertTrue(
            "The original range preview stays within the scaled host height ceiling",
            picker.size.height <= 1040f * density + 1f,
        )
        val calendarToggle =
            compose.onNode(
                hasContentDescription("Switch to calendar input mode") and
                    hasAnyAncestor(hasTestTag("library_LEFT"))
            )
        revealOriginalControl(calendarToggle).assertIsEnabled().performTouchInput { click() }
        revealOriginalControl(inputToggle("LEFT"))
    }

    private fun configureComparison(
        component: LabComponent,
        left: DesignFamily,
        right: DesignFamily,
    ) {
        chooseComponent(component)
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
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

    private fun openDetail(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("nav_list").performClick()
        if (compose.onAllNodes(hasTestTag("detail_back")).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("detail_back").performClick()
        }
        val search = compose.onNodeWithTag("component_search")
        search.performTextReplacement(component.label)
        search.performImeAction()
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_${component.name}"))
        compose.onNodeWithTag("list_${component.name}").performClick()
        chooseFamily("LEFT", family)
        setEnabled(true)
        resetSamples()
    }

    private fun enterComparison() {
        compose.onNodeWithTag("detail_compare").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithTag("compare_screen").assertExists()
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun assertBlockedCopy(direction: String, reason: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_$direction", useUnmergedTree = true)
            .assertTextEquals(reason)
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
    }

    private fun showPicker(panel: String) {
        compose.onNodeWithTag("date_viewport_$panel").performScrollTo().assertIsDisplayed()
    }

    private fun libraryDay(
        panel: String,
        day: Int,
        range: Boolean = false,
    ): SemanticsNodeInteraction {
        // The original Day Surface exposes its full date as Text; the drawn number clears
        // semantics.
        var matcher =
            hasText(day.toString(), substring = true) and
                hasClickAction() and
                hasAnyAncestor(hasTestTag("library_$panel"))
        if (range)
            matcher =
                matcher and
                    hasText("January", substring = true) and
                    hasText("2024", substring = true)
        return compose.onNode(matcher)
    }

    private fun touchLibraryDay(panel: String, day: Int, range: Boolean = false) {
        showPicker(panel)
        revealOriginalControl(libraryDay(panel, day, range))
            .assertIsEnabled()
            .assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performTouchInput { click() }
    }

    private fun inputToggle(panel: String): SemanticsNodeInteraction =
        compose.onNode(
            hasContentDescription("Switch to text input mode") and
                hasAnyAncestor(hasTestTag("library_$panel"))
        )

    private fun switchToInput(panel: String) {
        showPicker(panel)
        revealOriginalControl(inputToggle(panel)).assertIsEnabled().performTouchInput { click() }
    }

    private fun dateInput(panel: String, label: String? = null): SemanticsNodeInteraction {
        var matcher = hasSetTextAction() and hasAnyAncestor(hasTestTag("library_$panel"))
        if (label != null) matcher = matcher and hasText(label)
        return compose.onNode(matcher)
    }

    private fun replaceDateInput(panel: String, digits: String, label: String? = null) {
        showPicker(panel)
        val field = revealOriginalControl(dateInput(panel, label)).assertIsEnabled()
        field.performTouchInput { click() }
        field.performTextReplacement(digits)
        field.performImeAction()
        closeSoftKeyboard()
        compose.waitForIdle()
        assertInputDigits(panel, digits, label)
    }

    private fun assertInputDigits(panel: String, digits: String, label: String? = null) {
        showPicker(panel)
        val node = revealOriginalControl(dateInput(panel, label)).fetchSemanticsNode()
        assertTrue(
            "The original date editor must have positive visible bounds",
            node.boundsInRoot.width > 0f && node.boundsInRoot.height > 0f,
        )
        val text = node.config[SemanticsProperties.EditableText].text
        assertEquals(digits, text.filter(Char::isDigit))
    }

    private fun revealOriginalControl(control: SemanticsNodeInteraction): SemanticsNodeInteraction {
        control.performScrollTo()
        val pageTag =
            if (compose.onAllNodes(hasTestTag("detail_screen")).fetchSemanticsNodes().isNotEmpty())
                "detail_screen"
            else "compare_screen"
        val page = compose.onNodeWithTag(pageTag)
        // performScrollTo targets the closest scroll parent. The original picker also nests
        // a month list or horizontal viewport, so reveal its control in the outer host as well.
        // Host scrolling dispatches a public semantics action; picker controls use real pointers.
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
                page.performSemanticsAction(SemanticsActions.ScrollBy) { action ->
                    action(0f, delta)
                }
        }
        return control.assertIsDisplayed()
    }

    private fun assertDateFeedback(
        panel: String,
        date: TestDate?,
        names: DateNames = englishNames,
    ) {
        val text = date?.let { format(it) } ?: names.empty
        compose.onNodeWithTag("status_$panel").assertTextEquals("${names.datePrefix}: $text")
    }

    private fun assertRangeFeedback(
        panel: String,
        start: TestDate?,
        end: TestDate?,
        names: DateNames = englishNames,
    ) {
        val startText = start?.let { format(it) } ?: names.empty
        val endText = end?.let { format(it) } ?: names.empty
        compose
            .onNodeWithTag("status_$panel")
            .assertTextEquals("${names.startPrefix}: $startText · ${names.endPrefix}: $endText")
    }

    private fun format(date: TestDate): String =
        DateFormat.getDateInstance(
                DateFormat.MEDIUM,
                compose.activity.resources.configuration.locales[0],
            )
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(date.utcMillis()))

    private inline fun <reified T : View> nativeRoot(panel: String): T =
        compose.runOnIdle {
            val view = compose.activity.findViewById<View>(nativeId(panel))
            assertEquals(
                "The sample must use the exact original framework class",
                T::class.java,
                view.javaClass,
            )
            view as T
        }

    private fun nativeDate(panel: String): TestDate =
        compose.runOnIdle {
            val picker = compose.activity.findViewById<DatePicker>(nativeId(panel))
            TestDate(picker.year, picker.month + 1, picker.dayOfMonth)
        }

    private fun assertNativeDate(panel: String, expected: TestDate) =
        assertEquals(expected, nativeDate(panel))

    private fun nativeCalendarDate(panel: String): TestDate =
        compose.runOnIdle {
            val date = compose.activity.findViewById<CalendarView>(nativeId(panel)).date
            val calendar =
                GregorianCalendar(TimeZone.getDefault(), Locale.US).apply { timeInMillis = date }
            TestDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
            )
        }

    private fun assertNativeIdentity(
        panel: String,
        family: DesignFamily,
        api: String,
        style: Int,
        view: View,
    ) {
        val platform = requireNotNull(family.platform)
        expandDetails(panel)
        compose.onNodeWithTag("source_$panel").assertTextEquals("android.widget.$api")
        val minimumApi = if (api == "DatePicker") 1 else 11
        expandDetails(panel)
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextEquals(
                "android:${platform.themeName} · ${compose.activity.getString(R.string.widget_api, minimumApi)}"
            )
        compose.runOnIdle {
            assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val reference = ContextThemeWrapper(compose.activity, platform.themeId)
            assertTrue(reference.theme.resolveAttribute(style, expected, true))
            assertTrue(view.context.theme.resolveAttribute(style, actual, true))
            assertEquals(
                "The original default style must follow the selected native theme",
                expected.resourceId,
                actual.resourceId,
            )
        }
    }

    private fun assertLibraryIdentity(panel: String, api: String) {
        expandDetails(panel)
        compose.onNodeWithTag("source_$panel").assertTextEquals("androidx.compose.material3.$api")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains(
                "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}",
                substring = true,
            )
    }

    private fun descendants(view: View): List<View> = buildList {
        add(view)
        if (view is ViewGroup) repeat(view.childCount) { addAll(descendants(view.getChildAt(it))) }
    }

    private fun touchNativeDay(panel: String, date: TestDate) {
        showPicker(panel)
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        var bounds: Rect? = null
        repeat(5) {
            if (bounds == null) {
                val rootBounds =
                    compose.runOnIdle {
                        val view = compose.activity.findViewById<View>(nativeId(panel))
                        val rootOnScreen = IntArray(2)
                        view.rootView.getLocationOnScreen(rootOnScreen)
                        Rect().also { rect ->
                            assertTrue(view.getGlobalVisibleRect(rect))
                            // Accessibility day bounds use screen coordinates, including window
                            // offset.
                            rect.offset(rootOnScreen[0], rootOnScreen[1])
                        }
                    }
                val root = automation.rootInActiveWindow
                bounds = root?.let { findNativeDay(it, date, rootBounds) }
                if (bounds == null) {
                    compose.onNodeWithTag("compare_screen").performTouchInput { swipeUp() }
                    compose.waitForIdle()
                }
            }
        }
        val target =
            requireNotNull(bounds) { "No visible original native day $date in panel $panel" }
        val downTime = SystemClock.uptimeMillis()
        val down =
            MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                target.exactCenterX(),
                target.exactCenterY(),
                0,
            )
        val up =
            MotionEvent.obtain(
                downTime,
                downTime + 40,
                MotionEvent.ACTION_UP,
                target.exactCenterX(),
                target.exactCenterY(),
                0,
            )
        down.source = InputDevice.SOURCE_TOUCHSCREEN
        up.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            assertTrue(automation.injectInputEvent(down, true))
            SystemClock.sleep(40)
            assertTrue(automation.injectInputEvent(up, true))
        } finally {
            down.recycle()
            up.recycle()
        }
        compose.waitForIdle()
    }

    private fun findNativeDay(
        node: AccessibilityNodeInfo,
        date: TestDate,
        rootBounds: Rect,
    ): Rect? {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val description = node.contentDescription?.toString().orEmpty()
        val month =
            date
                .utcCalendar()
                .getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)
                .orEmpty()
        val civilDescription =
            String.format(Locale.ENGLISH, "%02d %s %d", date.day, month, date.year)
        if (
            node.isVisibleToUser &&
                node.isEnabled &&
                node.isClickable &&
                description.contains(civilDescription) &&
                rootBounds.contains(bounds.centerX(), bounds.centerY())
        ) {
            return bounds
        }
        repeat(node.childCount) { index ->
            val child = node.getChild(index)
            if (child != null)
                findNativeDay(child, date, rootBounds)?.let {
                    return it
                }
        }
        return null
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        val current =
            control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On
        if (current != enabled) control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.openSettingsPage("LANGUAGE")
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class TestDate(val year: Int, val month: Int, val day: Int) {
        fun utcCalendar() =
            GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US).apply {
                clear()
                isLenient = false
                set(year, month - 1, day)
            }

        fun utcMillis() = utcCalendar().timeInMillis
    }

    private data class DateNames(
        val language: AppLanguage,
        val datePrefix: String,
        val startPrefix: String,
        val endPrefix: String,
        val empty: String,
        val enabledNote: String,
        val dateRequired: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
    private val localizedNames =
        listOf(
            DateNames(
                AppLanguage.ENGLISH,
                "Date",
                "Start",
                "End",
                "No date selected",
                "Enabled does not apply to this picker. This provider has no control that disables all date interactions.",
                "The target requires a selected date.",
            ),
            DateNames(
                AppLanguage.KOREAN,
                "날짜",
                "시작일",
                "종료일",
                "선택한 날짜 없음",
                "이 선택기에는 활성화 설정이 적용되지 않습니다. 이 제공자는 모든 날짜 조작을 비활성화하는 기능을 제공하지 않습니다.",
                "대상에는 선택한 날짜가 필요합니다.",
            ),
            DateNames(
                AppLanguage.JAPANESE,
                "日付",
                "開始日",
                "終了日",
                "日付未選択",
                "この選択画面には有効・無効の設定が適用されません。この提供元にはすべての日付操作を無効にする機能がありません。",
                "コピー先には選択した日付が必要です。",
            ),
            DateNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "日期",
                "开始",
                "结束",
                "未选择日期",
                "启用设置不适用于此选择器。此提供方没有禁用所有日期操作的功能。",
                "目标必须有已选日期。",
            ),
            DateNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "日期",
                "開始",
                "結束",
                "未選擇日期",
                "啟用設定不適用於此選擇器。此提供者沒有停用所有日期操作的功能。",
                "目標必須有已選日期。",
            ),
        )
    private val englishNames
        get() = localizedNames.first()

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
