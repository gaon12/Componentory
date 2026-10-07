package xyz.gaon.componentory.lab

import android.graphics.Rect
import android.os.SystemClock
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
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
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard as nativeCloseKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean
import org.hamcrest.Matchers.sameInstance
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class InlineTimeSamplesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

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
    fun nativeThemesUseOriginalSpinnerOrDialInteractionsAndPublicTimePickerState() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.TIME_PICKER, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", family)
            assertNativeTime("LEFT", 10, 30, true)
            val viewportWidth =
                compose.onNodeWithTag("time_viewport_LEFT").fetchSemanticsNode().size.width
            compose.runOnIdle {
                assertTrue(
                    "The original picker fills its bounded preview instead of collapsing",
                    nativePicker("LEFT").width >= viewportWidth,
                )
            }
            when (family) {
                DesignFamily.CLASSIC -> {
                    // Edit the original spinner child and commit through its original IME action.
                    showPicker("LEFT")
                    val minute =
                        compose.runOnIdle {
                            descendants(nativePicker("LEFT")).filterIsInstance<EditText>().single {
                                it.isShown && it.text.toString().toIntOrNull() == 30
                            }
                        }
                    revealNativeControl("LEFT", minute)
                    onView(sameInstance(minute))
                        .perform(
                            nativeClick(),
                            replaceText("45"),
                            pressImeActionButton(),
                            nativeCloseKeyboard(),
                        )
                    // Closing the IME does not commit a NumberPicker's focused editor on every OS.
                    // Moving focus through its original hour editor commits the original input.
                    val hour =
                        compose.runOnIdle {
                            descendants(nativePicker("LEFT")).filterIsInstance<EditText>().single {
                                it.isShown && it.text.toString().toIntOrNull() == 10
                            }
                        }
                    revealNativeControl("LEFT", hour)
                    onView(sameInstance(hour)).perform(nativeClick(), nativeCloseKeyboard())
                    assertNativeTime("LEFT", 10, 45, true)
                }
                DesignFamily.HOLO -> {
                    showPicker("LEFT")
                    val minute =
                        compose.runOnIdle {
                            descendants(nativePicker("LEFT"))
                                .filterIsInstance<NumberPicker>()
                                .single { it.isShown && it.value == 30 }
                        }
                    // The native fling continues after Espresso's gesture finishes.
                    val idle = AtomicBoolean(true)
                    compose.runOnIdle {
                        minute.setOnScrollListener { _, scrollState ->
                            idle.set(scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE)
                        }
                    }
                    try {
                        revealNativeControl("LEFT", minute)
                        onView(sameInstance(minute)).perform(swipeUp())
                        compose.waitUntil(5000) { idle.get() }
                    } finally {
                        compose.runOnIdle { minute.setOnScrollListener(null) }
                    }
                    assertTrue(
                        "The original Holo minute wheel must change",
                        nativeTime("LEFT").second != 30,
                    )
                }
                DesignFamily.MATERIAL -> {
                    touchNativeDial("LEFT", 9)
                    showPicker("LEFT")
                    val minutes =
                        compose.runOnIdle {
                            descendants(nativePicker("LEFT")).filterIsInstance<TextView>().single {
                                it.isShown && it.isClickable && it.text.toString() == "30"
                            }
                        }
                    revealNativeControl("LEFT", minutes)
                    onView(sameInstance(minutes)).perform(nativeClick())
                    touchNativeDial("LEFT", 5)
                    assertNativeTime("LEFT", 9, 5, true)
                }
                else -> error("Not a framework theme")
            }
            val selected = nativeTime("LEFT")
            feedback("LEFT", selected.first, selected.second, true)
            feedback("RIGHT", 10, 30, true)
            setFormat("LEFT", false)
            assertNativeTime("LEFT", selected.first, selected.second, false)
            recreateActivity()
            assertNativeTime("LEFT", selected.first, selected.second, false)
            setEnabled(false)
            compose.runOnIdle { assertFalse(nativePicker("LEFT").isEnabled) }
            setFormat("LEFT", true)
            assertNativeTime("LEFT", selected.first, selected.second, true)
            compose.onNodeWithTag("time_enabled_note_LEFT").assertTextEquals(english.nativeNote)
            if (family == DesignFamily.MATERIAL) checkOriginalDisabledKeyboardPath("LEFT")
        }
    }

    @Test
    fun material3ClockChangesImmediatelyAndRemainsInteractiveWhenHostEnabledIsOff() {
        configure(LabComponent.TIME_PICKER, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        assertLibraryIdentity("LEFT", "TimePicker")
        touchLibraryClock("LEFT", 9, 5)
        assertClock("LEFT", 9, 5, true)
        feedback("LEFT", 9, 5, true)
        feedback("RIGHT", 10, 30, true)
        setFormat("LEFT", false)
        assertClock("LEFT", 9, 5, false)
        touchPeriod("LEFT", "PM")
        feedback("LEFT", 21, 5, false)
        setEnabled(false)
        compose.onNodeWithTag("time_enabled_note_LEFT").assertTextEquals(english.libraryNote)
        setFormat("LEFT", true)
        touchLibraryClock("LEFT", 9, 5)
        feedback("LEFT", 9, 5, true)
        feedback("RIGHT", 10, 30, true)
        compose.onNodeWithTag("enabled").assertIsOff()
        recreateActivity()
        assertClock("LEFT", 9, 5, true)
        assertClock("RIGHT", 10, 30, true)
    }

    @Test
    fun originalTimeInputHandlesValidRejectedAndEmptyTextAndMidnightNoonPeriods() {
        configure(LabComponent.TIME_INPUT, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        assertLibraryIdentity("LEFT", "TimeInput")
        enterTime("LEFT", 23, 59, true)
        feedback("LEFT", 23, 59, true)
        replaceInput("LEFT", true, "24")
        assertInput("LEFT", true, "23")
        feedback("LEFT", 23, 59, true)
        replaceInput("LEFT", false, "60")
        assertInput("LEFT", false, "59")
        feedback("LEFT", 23, 59, true)
        replaceInput("LEFT", true, "")
        assertInput("LEFT", true, "")
        feedback("LEFT", 0, 59, true)
        replaceInput("LEFT", false, "")
        assertInput("LEFT", false, "")
        feedback("LEFT", 0, 0, true)
        feedback("RIGHT", 10, 30, true)
        setFormat("LEFT", false)
        assertInput("LEFT", true, "12")
        assertPeriod("LEFT", "AM")
        touchPeriod("LEFT", "PM")
        feedback("LEFT", 12, 0, false)
        setFormat("LEFT", true)
        assertInput("LEFT", true, "12")
        feedback("LEFT", 12, 0, true)
        setEnabled(false)
        enterTime("LEFT", 0, 0, false)
        feedback("LEFT", 0, 0, false)
        compose.onNodeWithTag("enabled").assertIsOff()
        compose.onNodeWithTag("time_enabled_note_LEFT").assertTextEquals(english.libraryNote)
    }

    @Test
    fun copiesUseSelectedCivilTimeAndFormatWithoutSharingEditorsOrReplayingAfterReset() {
        configure(LabComponent.TIME_INPUT, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        enterTime("LEFT", 23, 59, false)
        enterTime("RIGHT", 7, 10, true)
        setEnabled(false)
        copyInputs("LEFT_TO_RIGHT")
        feedback("RIGHT", 23, 59, false)
        assertInput("RIGHT", true, "11")
        assertInput("RIGHT", false, "59")
        enterTime("RIGHT", 0, 0, true)
        feedback("LEFT", 23, 59, false)
        copyInputs("RIGHT_TO_LEFT")
        feedback("LEFT", 0, 0, true)
        recreateActivity()
        feedback("LEFT", 0, 0, true)
        feedback("RIGHT", 0, 0, true)
        compose.onNodeWithTag("enabled").assertIsOff()
        resetSamples()
        assertInput("LEFT", true, "10")
        assertInput("RIGHT", false, "30")
        feedback("LEFT", 10, 30, true)
        chooseFamily("RIGHT", DesignFamily.CLASSIC)
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
        chooseFamily("RIGHT", DesignFamily.MATERIAL2)
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        feedback("RIGHT", 10, 30, true)

        configure(LabComponent.TIME_PICKER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        // Public setters provide a clearly separate fixture, not clock touch evidence.
        compose.runOnIdle {
            nativePicker("LEFT").apply {
                hour = 23
                minute = 59
            }
        }
        setFormat("LEFT", false)
        copyInputs("LEFT_TO_RIGHT")
        assertClock("RIGHT", 23, 59, false)
        setFormat("RIGHT", true)
        touchLibraryClock("RIGHT", 9, 5)
        copyInputs("RIGHT_TO_LEFT")
        assertNativeIdentity("LEFT", DesignFamily.CLASSIC)
        assertNativeTime("LEFT", 9, 5, true)
        feedback("RIGHT", 9, 5, true)
    }

    @Test
    fun detailCarriesOnlySelectedSetupToFreshComparisonOnce() {
        compose.onNodeWithTag("nav_list").performClick()
        if (compose.onAllNodes(hasTestTag("detail_back")).fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithTag("detail_back").performClick()
        val search = compose.onNodeWithTag("component_search")
        search.performTextReplacement(LabComponent.TIME_INPUT.label)
        search.performImeAction()
        compose.onNodeWithTag("component_list").performScrollToNode(hasTestTag("list_TIME_INPUT"))
        compose.onNodeWithTag("list_TIME_INPUT").performClick()
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        resetSamples()
        enterTime("LEFT", 23, 59, false)
        setEnabled(false)
        compose.onNodeWithTag("detail_compare").performTouchInput { click() }
        compose.onNodeWithTag("compare_screen").assertExists()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        feedback("LEFT", 23, 59, false)
        feedback("RIGHT", 10, 30, true)
        compose.onNodeWithTag("enabled").assertIsOff()
        enterTime("LEFT", 7, 10, true)
        recreateActivity()
        feedback("LEFT", 7, 10, true)
        feedback("RIGHT", 10, 30, true)
        resetSamples()
        feedback("LEFT", 10, 30, true)
        feedback("RIGHT", 10, 30, true)
    }

    @Test
    fun fiveSettingsLanguagesAndNarrowLargeFontHostKeepOriginalControlsReachable() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.TIME_INPUT, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
            setEnabled(false)
            setFormat("LEFT", false, text.format)
            feedback("LEFT", 10, 30, false, text)
            feedback("RIGHT", 10, 30, true, text)
            compose.onNodeWithTag("time_configuration_LEFT").assertTextEquals(text.configuration)
            compose.onNodeWithTag("time_enabled_note_LEFT").assertTextEquals(text.libraryNote)
            setFormat("LEFT", true, text.format)
            configure(LabComponent.TIME_PICKER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            setEnabled(false)
            setFormat("LEFT", false, text.format)
            assertNativeTime("LEFT", 10, 30, false)
            compose.onNodeWithTag("time_enabled_note_LEFT").assertTextEquals(text.nativeNote)
        }
        changeLanguage(AppLanguage.ENGLISH)
        // This bounds only the production Compose host. It is not an OS resize, native font
        // change, TalkBack run, historical capture or a claim about drawn text overflow.
        compose.runOnUiThread {
            compose.activity.setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                    Box(Modifier.width(360.dp)) { ComponentoryApp() }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav_compare").performClick()
        configure(LabComponent.TIME_INPUT, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        enterTime("LEFT", 23, 59, false)
        assertInput("LEFT", true, "11")
        assertInput("LEFT", false, "59")
        assertPeriod("LEFT", "PM")
        feedback("LEFT", 23, 59, false)
        val host = compose.onNodeWithTag("compare_screen").fetchSemanticsNode().boundsInRoot
        val viewport = compose.onNodeWithTag("time_viewport_LEFT").fetchSemanticsNode().boundsInRoot
        assertTrue(
            viewport.width > 0f && viewport.left >= host.left && viewport.right <= host.right
        )
        configure(LabComponent.TIME_PICKER, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        showPicker("LEFT")
        val root = compose.onNodeWithTag("library_LEFT").fetchSemanticsNode()
        assertTrue(
            "A narrow panel uses the supplier's vertical layout even on a landscape device",
            root.size.height > root.size.width,
        )
        touchLibraryClock("LEFT", 9, 5)
        feedback("LEFT", 9, 5, true)
    }

    private fun configure(component: LabComponent, left: DesignFamily, right: DesignFamily) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun showPicker(panel: String) {
        compose.onNodeWithTag("time_viewport_$panel").performScrollTo().assertIsDisplayed()
    }

    private fun revealNativeControl(panel: String, view: View) {
        // Interop children do not have Compose scroll-to actions. Reveal the real view in
        // both host viewports before Espresso checks visibility and sends an actual gesture.
        val horizontal = compose.onNodeWithTag("time_viewport_$panel")
        val page = compose.onNodeWithTag("compare_screen")
        repeat(4) {
            val location =
                compose.runOnIdle {
                    val position = IntArray(2)
                    view.getLocationInWindow(position)
                    androidx.compose.ui.geometry.Rect(
                        position[0].toFloat(),
                        position[1].toFloat(),
                        (position[0] + view.width).toFloat(),
                        (position[1] + view.height).toFloat(),
                    )
                }
            val viewport = horizontal.fetchSemanticsNode().boundsInRoot
            val pageBounds = page.fetchSemanticsNode().boundsInRoot
            val dx =
                when {
                    location.left < viewport.left -> location.left - viewport.left
                    location.right > viewport.right -> location.right - viewport.right
                    else -> 0f
                }
            val dy =
                when {
                    location.top < pageBounds.top -> location.top - pageBounds.top
                    location.bottom > pageBounds.bottom -> location.bottom - pageBounds.bottom
                    else -> 0f
                }
            if (dx != 0f)
                horizontal.performSemanticsAction(SemanticsActions.ScrollBy) { it(dx, 0f) }
            if (dy != 0f) page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, dy) }
        }
        compose.runOnIdle {
            assertTrue(
                "The original control has measured bounds",
                view.width > 0 && view.height > 0,
            )
            val visible = Rect()
            val shown = view.getGlobalVisibleRect(visible)
            val fraction = visible.width().toFloat() * visible.height() / (view.width * view.height)
            assertTrue(
                "The original control is exposed before a gesture: $fraction",
                shown && fraction >= .9f,
            )
        }
    }

    private fun reveal(control: SemanticsNodeInteraction): SemanticsNodeInteraction {
        control.performScrollTo()
        val pageTag =
            if (compose.onAllNodes(hasTestTag("detail_screen")).fetchSemanticsNodes().isNotEmpty())
                "detail_screen"
            else "compare_screen"
        val page = compose.onNodeWithTag(pageTag)
        // The closest scroll parent is horizontal. Dispatch only outer host scrolling here;
        // original picker selections below use actual pointers and original editor actions.
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
        val node = control.assertIsDisplayed().fetchSemanticsNode()
        assertTrue(
            "Original control must be placed with positive visible bounds",
            node.boundsInRoot.width > 0f && node.boundsInRoot.height > 0f,
        )
        return control
    }

    private fun selector(panel: String, hour: Boolean) =
        compose.onNode(
            hasAnyAncestor(hasTestTag("library_$panel")) and
                hasClickAction() and
                hasContentDescription(if (hour) "Select hour" else "Select minutes")
        )

    private fun activeInput(panel: String, hour: Boolean): SemanticsNodeInteraction {
        showPicker(panel)
        val selectorMatcher =
            hasAnyAncestor(hasTestTag("library_$panel")) and
                hasClickAction() and
                hasContentDescription(if (hour) "Select hour" else "Select minutes")
        if (compose.onAllNodes(selectorMatcher).fetchSemanticsNodes().isNotEmpty())
            reveal(compose.onNode(selectorMatcher)).performTouchInput { click() }
        return reveal(
                compose.onNode(
                    hasAnyAncestor(hasTestTag("library_$panel")) and
                        hasSetTextAction() and
                        hasContentDescription(if (hour) "for hour" else "for minutes")
                )
            )
            .assertIsEnabled()
    }

    private fun replaceInput(panel: String, hour: Boolean, value: String) {
        val field = activeInput(panel, hour)
        field.performTouchInput { click() }
        field.performTextReplacement(value)
        closeSoftKeyboard()
        compose.waitForIdle()
    }

    private fun assertInput(panel: String, hour: Boolean, expected: String) {
        val text =
            activeInput(panel, hour)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text
        assertEquals(expected, text)
        closeSoftKeyboard()
    }

    private fun enterTime(panel: String, hour: Int, minute: Int, use24: Boolean) {
        setFormat(panel, use24)
        if (!use24) touchPeriod(panel, if (hour >= 12) "PM" else "AM")
        val displayedHour = if (use24) hour else (hour % 12).let { if (it == 0) 12 else it }
        replaceInput(panel, true, displayedHour.toString())
        replaceInput(panel, false, minute.toString())
    }

    private fun touchLibraryClock(panel: String, hour: Int, minute: Int) {
        showPicker(panel)
        reveal(selector(panel, true)).performTouchInput { click() }
        val scope =
            hasAnyAncestor(hasTestTag("library_$panel")) and
                hasClickAction() and
                !hasContentDescription("Select hours") and
                !hasContentDescription("Select minutes")
        reveal(
                compose.onNode(
                    scope and
                        (hasContentDescription("$hour hours") or
                            hasContentDescription("$hour o'clock"))
                )
            )
            .assertIsEnabled()
            .performTouchInput { click() }
        // Explicit selection works with either accessibility auto-advance policy.
        reveal(selector(panel, false)).performTouchInput { click() }
        reveal(compose.onNode(scope and hasContentDescription("$minute minutes")))
            .assertIsEnabled()
            .performTouchInput { click() }
    }

    private fun assertClock(panel: String, hour: Int, minute: Int, use24: Boolean) {
        showPicker(panel)
        val displayedHour = if (use24) hour else (hour % 12).let { if (it == 0) 12 else it }
        reveal(selector(panel, true))
            .assertHasClickAction()
            .assertIsEnabled()
            .assertTextContains(displayedHour.toString().padStart(2, '0'), substring = true)
        reveal(selector(panel, false))
            .assertHasClickAction()
            .assertIsEnabled()
            .assertTextContains(minute.toString().padStart(2, '0'), substring = true)
        if (!use24) assertPeriod(panel, if (hour >= 12) "PM" else "AM")
    }

    private fun period(panel: String, text: String) =
        compose.onNode(
            hasAnyAncestor(hasTestTag("library_$panel")) and hasClickAction() and hasText(text)
        )

    private fun touchPeriod(panel: String, text: String) {
        showPicker(panel)
        reveal(period(panel, text)).assertIsEnabled().performTouchInput { click() }
    }

    private fun assertPeriod(panel: String, text: String) {
        showPicker(panel)
        reveal(period(panel, text)).assertIsEnabled().assertIsSelected()
    }

    private fun setFormat(panel: String, use24: Boolean, label: String = english.format) {
        val control =
            compose.onNodeWithTag("time_24_hour_$panel").performScrollTo().assertIsDisplayed()
        control
            .assertIsEnabled()
            .assertContentDescriptionEquals(label)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != use24
        )
            control.performTouchInput { click() }
        if (use24) control.assertIsOn() else control.assertIsOff()
    }

    private fun feedback(
        panel: String,
        hour: Int,
        minute: Int,
        use24: Boolean,
        text: TimeNames = english,
    ) {
        val locale = compose.activity.resources.configuration.locales[0]
        val pattern = DateFormat.getBestDateTimePattern(locale, if (use24) "Hm" else "hm")
        val selected =
            SimpleDateFormat(pattern, locale)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date((hour * 60L + minute) * 60_000L))
        compose.onNodeWithTag("status_$panel").assertTextEquals("${text.prefix}: $selected")
    }

    private fun nativePicker(panel: String): TimePicker =
        compose.activity.findViewById(nativeId(panel))

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun nativeTime(panel: String) =
        compose.runOnIdle { nativePicker(panel).let { it.hour to it.minute } }

    private fun assertNativeTime(panel: String, hour: Int, minute: Int, use24: Boolean) {
        showPicker(panel)
        compose.runOnIdle {
            val picker = nativePicker(panel)
            assertEquals(TimePicker::class.java, picker.javaClass)
            assertEquals(hour, picker.hour)
            assertEquals(minute, picker.minute)
            assertEquals(use24, picker.is24HourView())
            assertTrue(
                picker.isShown && picker.width > 0 && picker.height > 0 && picker.childCount > 0
            )
        }
    }

    private fun assertNativeIdentity(panel: String, family: DesignFamily) {
        expandDetails(panel)
        compose.onNodeWithTag("source_$panel").assertTextEquals("android.widget.TimePicker")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains(
                "android:${requireNotNull(family.platform).themeName}",
                substring = true,
            )
            .assertTextContains(
                compose.activity.getString(R.string.widget_api, 1),
                substring = true,
            )
        compose.runOnIdle {
            val picker = nativePicker(panel)
            assertEquals(ContextThemeWrapper::class.java, picker.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.timePickerStyle, expected, true),
                picker.context.theme.resolveAttribute(android.R.attr.timePickerStyle, actual, true),
            )
            assertEquals(expected.resourceId, actual.resourceId)
            assertEquals(expected.data, actual.data)
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

    private fun touchNativeDial(panel: String, value: Int) {
        showPicker(panel)
        val rootBounds =
            compose.runOnIdle {
                val picker = nativePicker(panel)
                val offset = IntArray(2)
                picker.rootView.getLocationOnScreen(offset)
                Rect().also {
                    assertTrue(picker.getGlobalVisibleRect(it))
                    it.offset(offset[0], offset[1])
                }
            }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bounds = findDialValue(requireNotNull(automation.rootInActiveWindow), value, rootBounds)
        assertTrue("The original visible native dial must expose value $value", bounds != null)
        val target = requireNotNull(bounds)
        val downTime = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEachIndexed { index, action ->
            val event =
                MotionEvent.obtain(
                    downTime,
                    downTime + index * 40L,
                    action,
                    target.exactCenterX(),
                    target.exactCenterY(),
                    0,
                )
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                assertTrue(automation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
        compose.waitForIdle()
    }

    private fun findDialValue(node: AccessibilityNodeInfo, value: Int, root: Rect): Rect? {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (
            node.contentDescription?.toString() == value.toString() &&
                node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK } &&
                Rect.intersects(root, bounds) &&
                bounds.width() > 0 &&
                bounds.height() > 0
        )
            return bounds
        repeat(node.childCount) { index ->
            node.getChild(index)?.let { child ->
                findDialValue(child, value, root)?.let {
                    return it
                }
            }
        }
        return null
    }

    private fun checkOriginalDisabledKeyboardPath(panel: String) {
        showPicker(panel)
        val button =
            compose.runOnIdle {
                descendants(nativePicker(panel)).filterIsInstance<ImageButton>().singleOrNull {
                    it.isShown &&
                        it.contentDescription?.toString() ==
                            "Switch to text input mode for the time input."
                }
            } ?: return
        // Public Enabled does not promise that every OEM keyboard path is blocked. Exercise
        // an original path only when that actual button/editor remains enabled on this device.
        if (!compose.runOnIdle { button.isEnabled }) return
        onView(sameInstance(button)).perform(nativeClick())
        val before = nativeTime(panel)
        val minute =
            compose.runOnIdle {
                descendants(nativePicker(panel)).filterIsInstance<EditText>().singleOrNull {
                    it.isShown && it.isEnabled && it.text.toString().toIntOrNull() == before.second
                }
            } ?: return
        val changed = if (before.second == 45) 15 else 45
        onView(sameInstance(minute)).perform(replaceText(changed.toString()), nativeCloseKeyboard())
        assertNativeTime(panel, before.first, changed, true)
        feedback(panel, before.first, changed, true)
        compose.runOnIdle { assertFalse(nativePicker(panel).isEnabled) }
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun blockedCopy(direction: String, reason: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_$direction", useUnmergedTree = true)
            .assertTextEquals(reason)
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != enabled
        )
            control.performTouchInput { click() }
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

    private data class TimeNames(
        val language: AppLanguage,
        val prefix: String,
        val format: String,
        val configuration: String,
        val libraryNote: String,
        val nativeNote: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
    private val names =
        listOf(
            TimeNames(
                AppLanguage.ENGLISH,
                "Selected time",
                "24-hour clock",
                "Changes apply immediately. Default setup is 10:30 in 24-hour format. The value is a time of day, not a timestamp.",
                "These library controls have no Enabled parameter. They remain interactive when Enabled is off.",
                "Enabled uses the original TimePicker API. Internal controls follow this device implementation.",
            ),
            TimeNames(
                AppLanguage.KOREAN,
                "선택한 시간",
                "24시간 표시",
                "변경하면 바로 적용됩니다. 기본 설정은 24시간 표시의 10:30입니다. 값은 하루 중 시간이며 타임스탬프가 아닙니다.",
                "이 라이브러리 컨트롤에는 Enabled 매개변수가 없습니다. 사용 설정을 꺼도 계속 조작할 수 있습니다.",
                "사용 설정은 원본 TimePicker API로 적용합니다. 내부 컨트롤의 동작은 이 기기의 구현을 따릅니다.",
            ),
            TimeNames(
                AppLanguage.JAPANESE,
                "選択した時刻",
                "24時間表示",
                "変更はすぐに反映されます。初期設定は24時間表示の10:30です。値は一日の時刻であり、タイムスタンプではありません。",
                "このライブラリのコントロールにEnabledパラメータはありません。有効の設定をオフにしても操作できます。",
                "有効の設定は元のTimePicker APIに適用します。内部コントロールの動作はこの端末の実装に従います。",
            ),
            TimeNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "所选时间",
                "24小时制",
                "更改会立即应用。默认设置为24小时制的10:30。该值是一天中的时间，而不是时间戳。",
                "这些库控件没有 Enabled 参数。关闭启用设置后仍可操作。",
                "启用设置使用原始 TimePicker API。内部控件的行为取决于此设备的实现。",
            ),
            TimeNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "所選時間",
                "24小時制",
                "變更會立即套用。預設設定為24小時制的10:30。此值是一天中的時間，而不是時間戳記。",
                "這些函式庫控制項沒有 Enabled 參數。關閉啟用設定後仍可操作。",
                "啟用設定使用原始 TimePicker API。內部控制項的行為取決於此裝置的實作。",
            ),
        )
    private val english
        get() = names.first()

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
