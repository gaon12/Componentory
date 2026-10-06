package xyz.gaon.componentory.lab

import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.compose.ui.semantics.Role
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
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard as nativeCloseSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
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
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class TimePickerDialogsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val originalTimeZone = TimeZone.getDefault()
    private val initial = TestTime(10, 30)
    private val selected = TestTime(9, 5)
    private val draft = TestTime(11, 45)
    private val other = TestTime(7, 10)
    private val evening = TestTime(21, 5)

    @Before
    fun prepareEnglishComparisonInNegativeProcessTimeZone() {
        // Civil hours and minutes must not shift when the process timezone changes.
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        chooseTimeComponent()
    }

    @After
    fun restoreProcessTimeZoneAndEnglishBaseline() {
        TimeZone.setDefault(originalTimeZone)
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun nativeThemesConfirmProgrammaticWidgetTimesAndDiscardCancelledOrBackDismissedDrafts() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            assertNativeIdentity("LEFT", family)
            feedback("LEFT", initial, "Not opened")
            openNative("LEFT")
            assertNativeTime("LEFT", initial)
            setNativeTimeUsingWidgetApi("LEFT", selected)
            clickNativeDialogButton(android.R.id.button1)
            feedback("LEFT", selected, "Confirmed")
            feedback("RIGHT", initial, "Not opened")

            openNative("LEFT")
            assertNativeTime("LEFT", selected)
            setNativeTimeUsingWidgetApi("LEFT", draft)
            clickNativeDialogButton(android.R.id.button2)
            feedback("LEFT", selected, "Cancelled")
            openNative("LEFT")
            assertNativeTime("LEFT", selected)
            setNativeTimeUsingWidgetApi("LEFT", other)
            pressBack()
            feedback("LEFT", selected, "Dismissed")
            openNative("LEFT")
            assertNativeTime("LEFT", selected)
            clickNativeDialogButton(android.R.id.button2)

            set24HourFormat("LEFT", false)
            openNative("LEFT")
            assertNativeTime("LEFT", selected, use24Hour = false)
            setNativeTimeUsingWidgetApi("LEFT", evening)
            clickNativeDialogButton(android.R.id.button1)
            feedback("LEFT", evening, "Confirmed", use24Hour = false)
            set24HourFormat("LEFT", true)
            feedback("LEFT", evening, "Confirmed")
            openNative("LEFT")
            assertNativeTime("LEFT", evening)
            clickNativeDialogButton(android.R.id.button2)
        }
    }

    @Test
    fun classicSpinnerMinuteFieldEditingCommitsThroughActualImeDoneBeforeConfirm() {
        chooseFamily("LEFT", DesignFamily.CLASSIC)
        resetSamples()
        openNative("LEFT")
        assertNativeTime("LEFT", initial)
        // TimePicker's positive callback may precede focus clearing. Commit pending text with Done.
        onView(
                allOf(
                    isAssignableFrom(EditText::class.java),
                    withText("30"),
                    isDescendantOfA(isAssignableFrom(TimePicker::class.java)),
                )
            )
            .inRoot(isDialog())
            .perform(replaceText("45"), pressImeActionButton(), nativeCloseSoftKeyboard())
        assertNativeTime("LEFT", TestTime(10, 45))
        clickNativeDialogButton(android.R.id.button1)
        feedback("LEFT", TestTime(10, 45), "Confirmed")
        openNative("LEFT")
        assertNativeTime("LEFT", TestTime(10, 45))
        clickNativeDialogButton(android.R.id.button2)
    }

    @Test
    fun material3ClockPointerAndOriginalInputControlsConfirmTimesAndDiscardUnconfirmedEdits() {
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        resetSamples()
        assertMaterial3Identity("LEFT")
        openLibrary("LEFT")
        compose.onNodeWithTag("time_picker_LEFT").assertIsDisplayed()
        // Pointer coordinates come from the original dial numbers, not from application state.
        compose
            .onNode(
                (hasContentDescription("9 hours") or hasContentDescription("9 o'clock")) and
                    hasAnyAncestor(hasTestTag("time_picker_LEFT")) and
                    hasClickAction()
            )
            .assertIsDisplayed()
            .performTouchInput { click() }
        // Accessibility services can disable automatic switching after the hour touch.
        compose
            .onNode(
                hasContentDescription("Select minutes") and
                    hasAnyAncestor(hasTestTag("time_picker_LEFT")) and
                    hasClickAction()
            )
            .assertIsDisplayed()
            .performTouchInput { click() }
        compose
            .onNode(
                hasContentDescription("5 minutes") and
                    hasAnyAncestor(hasTestTag("time_picker_LEFT")) and
                    hasClickAction()
            )
            .assertIsDisplayed()
            .performTouchInput { click() }
        confirmLibrary("LEFT")
        feedback("LEFT", selected, "Confirmed")
        feedback("RIGHT", initial, "Not opened")

        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", selected)
        enterLibraryTime("LEFT", other)
        confirmLibrary("LEFT")
        feedback("LEFT", other, "Confirmed")
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", other)
        enterLibraryTime("LEFT", draft)
        cancelLibrary("LEFT")
        feedback("LEFT", other, "Cancelled")
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", other)
        enterLibraryTime("LEFT", initial)
        closeSoftKeyboard()
        pressBack()
        feedback("LEFT", other, "Dismissed")
        compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", other)
        cancelLibrary("LEFT")

        set24HourFormat("LEFT", false)
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", other)
        enterLibraryTime("LEFT", selected)
        compose
            .onNode(
                hasText("PM") and hasClickAction() and hasAnyAncestor(hasTestTag("time_input_LEFT"))
            )
            .assertIsDisplayed()
            .performTouchInput { click() }
        confirmLibrary("LEFT")
        feedback("LEFT", evening, "Confirmed", use24Hour = false)
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", selected)
        compose
            .onNode(
                hasText("PM") and hasClickAction() and hasAnyAncestor(hasTestTag("time_input_LEFT"))
            )
            .assertIsSelected()
        cancelLibrary("LEFT")
        set24HourFormat("LEFT", true)
        feedback("LEFT", evening, "Cancelled")
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", evening)
        cancelLibrary("LEFT")
    }

    @Test
    fun mixedProvidersRestoreIndependentCommittedTimesAndOpenDraftsThenResetAndIgnoreDisabledTouch() {
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        chooseFamily("RIGHT", DesignFamily.HOLO)
        resetSamples()
        openLibrary("LEFT")
        enterLibraryTime("LEFT", selected)
        confirmLibrary("LEFT")
        openNative("RIGHT")
        setNativeTimeUsingWidgetApi("RIGHT", other)
        clickNativeDialogButton(android.R.id.button1)
        recreateActivity()
        feedback("LEFT", selected, "Confirmed")
        feedback("RIGHT", other, "Confirmed")

        openLibrary("LEFT")
        enterLibraryTime("LEFT", draft)
        closeSoftKeyboard()
        recreateActivity()
        compose.onNodeWithTag("time_dialog_LEFT").assertIsDisplayed()
        compose.onNodeWithTag("time_input_LEFT").assertIsDisplayed()
        assertLibraryInputTime("LEFT", draft)
        cancelLibrary("LEFT")
        feedback("LEFT", selected, "Cancelled")
        feedback("RIGHT", other, "Confirmed")
        openLibrary("LEFT")
        assertLibraryInputTime("LEFT", selected)
        cancelLibrary("LEFT")

        openNative("RIGHT")
        setNativeTimeUsingWidgetApi("RIGHT", draft)
        recreateActivity()
        assertNativeTime("RIGHT", draft)
        clickNativeDialogButton(android.R.id.button1)
        feedback("RIGHT", draft, "Confirmed")
        feedback("LEFT", selected, "Cancelled")
        resetSamples()
        feedback("LEFT", initial, "Not opened")
        feedback("RIGHT", initial, "Not opened")
        compose.onNodeWithTag("time_24_hour_LEFT").assertIsOn()
        compose.onNodeWithTag("time_24_hour_RIGHT").assertIsOn()

        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        compose
            .onNodeWithTag("library_LEFT")
            .assertIsNotEnabled()
            .performScrollTo()
            .performTouchInput { click() }
        compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
        onView(withId(R.id.sample_right)).check(matches(not(isEnabled())))
        compose.onNodeWithTag("native_RIGHT").performScrollTo().performTouchInput { click() }
        assertNoNativeDialog("RIGHT")
        feedback("LEFT", initial, "Not opened")
        feedback("RIGHT", initial, "Not opened")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        openLibrary("LEFT")
        cancelLibrary("LEFT")
        openNative("RIGHT")
        assertNativeTime("RIGHT", initial)
        clickNativeDialogButton(android.R.id.button2)
    }

    @Test
    fun material2ShowsAnExplicitUnsupportedReasonWithoutAReplacementTimePicker() {
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
        compose
            .onNodeWithText("The Material 2 library does not provide Time picker dialog.")
            .assertExists()
        expandDetails("LEFT")
        compose.onNodeWithTag("source_LEFT").assertTextEquals("Not provided")
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("status_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("time_picker_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("time_input_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("time_confirm_LEFT").assertDoesNotExist()
    }

    @Test
    fun fiveSettingsLanguagesRenderLocalActionsAndCivilTimesForActualDialogs() {
        val languages =
            listOf(
                AppLanguage.ENGLISH to "Confirmed",
                AppLanguage.KOREAN to "확인함",
                AppLanguage.JAPANESE to "確認しました",
                AppLanguage.SIMPLIFIED_CHINESE to "已确认",
                AppLanguage.TRADITIONAL_CHINESE to "已確認",
            )
        languages.forEach { (language, confirmed) ->
            changeLanguage(language)
            compose.onNodeWithTag("nav_compare").performClick()
            chooseFamily("LEFT", DesignFamily.MATERIAL3)
            chooseFamily("RIGHT", DesignFamily.MATERIAL)
            chooseTimeComponent()
            resetSamples()
            assertMaterial3Identity("LEFT")
            assertNativeIdentity("RIGHT", DesignFamily.MATERIAL)
            openLibrary("LEFT")
            compose
                .onNodeWithTag("time_confirm_LEFT")
                .assertTextEquals(compose.activity.getString(R.string.dialog_confirm))
            compose
                .onNodeWithTag("time_cancel_LEFT")
                .assertTextEquals(compose.activity.getString(R.string.dialog_cancel))
            confirmLibrary("LEFT")
            feedback("LEFT", initial, confirmed, language)
            openNative("RIGHT")
            assertNativeTime("RIGHT", initial)
            setNativeTimeUsingWidgetApi("RIGHT", selected)
            clickNativeDialogButton(android.R.id.button1)
            feedback("RIGHT", selected, confirmed, language)
            feedback("LEFT", initial, confirmed, language)
        }
    }

    private fun chooseTimeComponent() {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("picker_search")
            .performTextReplacement(LabComponent.TIME_PICKER_DIALOG.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_TIME_PICKER_DIALOG"))
        compose.onNodeWithTag("component_TIME_PICKER_DIALOG").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun resetSamples() {
        compose.onNodeWithTag("reset").performScrollTo().performClick()
    }

    private fun set24HourFormat(panel: String, enabled: Boolean) {
        val control = compose.onNodeWithTag("time_24_hour_$panel").performScrollTo()
        if (enabled) control.assertIsOff() else control.assertIsOn()
        control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun openNative(panel: String) {
        compose.onNodeWithTag("native_$panel").performScrollTo()
        onView(withId(nativeId(panel))).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun openLibrary(panel: String) {
        compose
            .onNodeWithTag("library_$panel")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertTextEquals(compose.activity.getString(R.string.open_time_picker))
            .assertHasClickAction()
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag("time_dialog_$panel").assertIsDisplayed()
    }

    private fun assertNativeIdentity(panel: String, family: DesignFamily) {
        val platform = requireNotNull(family.platform)
        expandDetails(panel)
        compose.onNodeWithTag("source_$panel").assertTextEquals("android.app.TimePickerDialog")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains("android:${platform.themeName}", substring = true)
        compose.onNodeWithTag("library_$panel").assertDoesNotExist()
        compose.runOnIdle {
            val launcher = compose.activity.findViewById<Button>(nativeId(panel))
            assertEquals(Button::class.java, launcher.javaClass)
            assertEquals(ContextThemeWrapper::class.java, launcher.context.javaClass)
            val expectedTheme = ContextThemeWrapper(compose.activity, platform.themeId).theme
            listOf(android.R.attr.buttonStyle, android.R.attr.timePickerStyle).forEach { attribute
                ->
                val expected = TypedValue()
                val actual = TypedValue()
                assertTrue(expectedTheme.resolveAttribute(attribute, expected, true))
                assertTrue(launcher.context.theme.resolveAttribute(attribute, actual, true))
                assertEquals(expected.resourceId, actual.resourceId)
            }
        }
    }

    private fun assertMaterial3Identity(panel: String) {
        expandDetails(panel)
        compose
            .onNodeWithTag("source_$panel")
            .assertTextEquals("androidx.compose.material3.TimePickerDialog")
        expandDetails(panel)
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains(
                "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}",
                substring = true,
            )
        compose.onNodeWithTag("native_$panel").assertDoesNotExist()
    }

    private fun assertNativeTime(panel: String, time: TestTime, use24Hour: Boolean = true) {
        compose.runOnIdle {
            val dialog = requireNativeDialog(panel)
            assertTrue(dialog.isShowing)
            assertEquals(TimePickerDialog::class.java, dialog.javaClass)
            val picker = requireNativePicker(dialog)
            assertEquals(TimePicker::class.java, picker.javaClass)
            assertEquals(time.hour, picker.hour)
            assertEquals(time.minute, picker.minute)
            assertEquals(use24Hour, picker.is24HourView())
        }
    }

    private fun setNativeTimeUsingWidgetApi(panel: String, time: TestTime) {
        // Programmatic widget API setup is not clock-touch or spinner-editing evidence.
        // The framework's original picker and positive-button listeners stay installed.
        compose.runOnIdle { requireNativeDialog(panel).updateTime(time.hour, time.minute) }
        compose.runOnIdle {
            val picker = requireNativePicker(requireNativeDialog(panel))
            assertEquals(time.hour, picker.hour)
            assertEquals(time.minute, picker.minute)
        }
    }

    private fun requireNativeDialog(panel: String): TimePickerDialog =
        requireNotNull(
            compose.activity.findViewById<Button>(nativeId(panel)).tag as? TimePickerDialog
        )

    private fun requireNativePicker(dialog: TimePickerDialog): TimePicker =
        requireNotNull(findNativePicker(requireNotNull(dialog.window).decorView))

    private fun findNativePicker(view: View): TimePicker? {
        if (view is TimePicker) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findNativePicker(view.getChildAt(index))?.let {
                    return it
                }
            }
        }
        return null
    }

    private fun assertNoNativeDialog(panel: String) {
        compose.runOnIdle {
            val dialog =
                compose.activity.findViewById<Button>(nativeId(panel)).tag as? TimePickerDialog
            assertFalse(dialog?.isShowing == true)
        }
    }

    private fun clickNativeDialogButton(id: Int) {
        onView(withId(id)).inRoot(isDialog()).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun showLibraryInput(panel: String) {
        if (compose.onAllNodes(hasTestTag("time_input_$panel")).fetchSemanticsNodes().isEmpty()) {
            compose.onNodeWithTag("time_mode_$panel").assertIsDisplayed().performTouchInput {
                click()
            }
        }
        compose.onNodeWithTag("time_input_$panel").assertIsDisplayed()
    }

    private fun libraryField(panel: String, hour: Boolean): SemanticsNodeInteraction {
        showLibraryInput(panel)
        // English-only input checks target the pinned library's original named controls.
        val selector =
            hasContentDescription(if (hour) "Select hour" else "Select minutes") and
                hasAnyAncestor(hasTestTag("time_input_$panel")) and
                hasClickAction()
        if (compose.onAllNodes(selector).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNode(selector).assertIsDisplayed().performTouchInput { click() }
        }
        return compose
            .onNode(
                hasSetTextAction() and
                    hasContentDescription(if (hour) "for hour" else "for minutes") and
                    hasAnyAncestor(hasTestTag("time_input_$panel"))
            )
            .assertIsDisplayed()
    }

    private fun assertLibraryInputTime(panel: String, time: TestTime) {
        val hour =
            libraryField(panel, hour = true)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text
                .toInt()
        val minute =
            libraryField(panel, hour = false)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text
                .toInt()
        assertEquals(time.hour, hour)
        assertEquals(time.minute, minute)
    }

    private fun enterLibraryTime(panel: String, time: TestTime) {
        libraryField(panel, hour = true).performTextReplacement(time.hour.toString())
        libraryField(panel, hour = false).performTextReplacement(time.minute.toString())
        closeSoftKeyboard()
    }

    private fun confirmLibrary(panel: String) {
        closeSoftKeyboard()
        compose
            .onNodeWithTag("time_confirm_$panel")
            .assertIsEnabled()
            .assertIsDisplayed()
            .performTouchInput { click() }
        compose.onNodeWithTag("time_dialog_$panel").assertDoesNotExist()
    }

    private fun cancelLibrary(panel: String) {
        closeSoftKeyboard()
        compose
            .onNodeWithTag("time_cancel_$panel")
            .assertIsEnabled()
            .assertIsDisplayed()
            .performTouchInput { click() }
        compose.onNodeWithTag("time_dialog_$panel").assertDoesNotExist()
    }

    private fun feedback(
        panel: String,
        time: TestTime,
        action: String,
        language: AppLanguage = AppLanguage.ENGLISH,
        use24Hour: Boolean = true,
    ) {
        compose
            .onNodeWithTag("status_$panel")
            .assertTextContains(time.localizedTime(language, use24Hour), substring = true)
            .assertTextContains(action, substring = true)
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private data class TestTime(val hour: Int, val minute: Int) {
        fun localizedTime(language: AppLanguage, use24Hour: Boolean): String {
            val locale = Locale.forLanguageTag(language.tag)
            val pattern = DateFormat.getBestDateTimePattern(locale, if (use24Hour) "Hm" else "hm")
            return SimpleDateFormat(pattern, locale)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date((hour * 60L + minute) * 60_000L))
        }
    }

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
