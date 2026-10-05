package xyz.gaon.componentory.lab

import android.app.DatePickerDialog
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
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
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.hamcrest.Matchers.not
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
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class DatePickerDialogsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val originalTimeZone = TimeZone.getDefault()
    private val initial = TestDate(2024, 1, 15)
    private val leapDay = TestDate(2024, 2, 29)
    private val draft = TestDate(2024, 3, 12)
    private val yearEnd = TestDate(2024, 12, 31)

    @Before
    fun prepareEnglishComparisonInNegativeProcessTimeZone() {
        // A UTC date must survive a process timezone where UTC midnight is the previous local day.
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        chooseDateComponent()
    }

    @After
    fun restoreProcessTimeZoneAndEnglishBaseline() {
        TimeZone.setDefault(originalTimeZone)
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun nativeThemesConfirmWidgetApiDatesAndDiscardCancelledOrBackDismissedDrafts() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            assertNativeIdentity("LEFT", family)
            feedback("LEFT", initial, "Not opened")

            openNative("LEFT")
            assertNativeDate("LEFT", initial)
            setNativeDateUsingWidgetApi("LEFT", leapDay)
            clickNativeDialogButton(android.R.id.button1)
            feedback("LEFT", leapDay, "Confirmed")
            feedback("RIGHT", initial, "Not opened")

            openNative("LEFT")
            assertNativeDate("LEFT", leapDay)
            setNativeDateUsingWidgetApi("LEFT", draft)
            clickNativeDialogButton(android.R.id.button2)
            feedback("LEFT", leapDay, "Cancelled")

            openNative("LEFT")
            assertNativeDate("LEFT", leapDay)
            setNativeDateUsingWidgetApi("LEFT", yearEnd)
            pressBack()
            feedback("LEFT", leapDay, "Dismissed")

            openNative("LEFT")
            assertNativeDate("LEFT", leapDay)
            clickNativeDialogButton(android.R.id.button2)
        }
    }

    @Test
    fun classicSpinnerDayFieldEditingUsesTheOriginalPositiveButtonFocusCommit() {
        chooseFamily("LEFT", DesignFamily.CLASSIC)
        resetSamples()
        openNative("LEFT")
        assertNativeDate("LEFT", initial)

        // Edit the actual spinner day field of the live dialog. Matching the displayed
        // text instead would couple the check to the seeded date. The framework positive
        // button commits its pending focus.
        val dayField: View = compose.runOnIdle { dayInput(requireNativeDialog("LEFT").datePicker) }
        onView(sameInstance(dayField))
            .inRoot(isDialog())
            .perform(replaceText("22"), nativeCloseSoftKeyboard())
        clickNativeDialogButton(android.R.id.button1)
        feedback("LEFT", TestDate(2024, 1, 22), "Confirmed")
        openNative("LEFT")
        assertNativeDate("LEFT", TestDate(2024, 1, 22))
        clickNativeDialogButton(android.R.id.button2)
    }

    @Test
    fun material3OriginalInputControlsValidateDatesAndDiscardCancelledOrBackDismissedDrafts() {
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        resetSamples()
        assertMaterial3Identity("LEFT")
        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", initial)

        libraryInput("LEFT").performTextReplacement("02")
        closeSoftKeyboard()
        compose.onNodeWithTag("date_confirm_LEFT").assertIsNotEnabled().performTouchInput {
            click()
        }
        compose.onNodeWithTag("date_dialog_LEFT").assertIsDisplayed()
        libraryInput("LEFT").performTextReplacement(leapDay.englishInputDigits)
        closeSoftKeyboard()
        confirmLibrary("LEFT")
        feedback("LEFT", leapDay, "Confirmed")
        feedback("RIGHT", initial, "Not opened")

        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", leapDay)
        libraryInput("LEFT").performTextReplacement(draft.englishInputDigits)
        closeSoftKeyboard()
        cancelLibrary("LEFT")
        feedback("LEFT", leapDay, "Cancelled")

        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", leapDay)
        libraryInput("LEFT").performTextReplacement(yearEnd.englishInputDigits)
        closeSoftKeyboard()
        pressBack()
        feedback("LEFT", leapDay, "Dismissed")
        compose.onNodeWithTag("date_dialog_LEFT").assertDoesNotExist()

        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", leapDay)
        cancelLibrary("LEFT")
    }

    @Test
    fun mixedProvidersPreserveIndependentCommittedDatesAndOpenDraftsAcrossRecreation() {
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        chooseFamily("RIGHT", DesignFamily.HOLO)
        resetSamples()
        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", initial)
        libraryInput("LEFT").performTextReplacement(leapDay.englishInputDigits)
        closeSoftKeyboard()
        confirmLibrary("LEFT")
        openNative("RIGHT")
        setNativeDateUsingWidgetApi("RIGHT", yearEnd)
        clickNativeDialogButton(android.R.id.button1)
        recreateActivity()
        feedback("LEFT", leapDay, "Confirmed")
        feedback("RIGHT", yearEnd, "Confirmed")

        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", leapDay)
        libraryInput("LEFT").performTextReplacement(draft.englishInputDigits)
        closeSoftKeyboard()
        recreateActivity()
        compose.onNodeWithTag("date_dialog_LEFT").assertIsDisplayed()
        assertLibraryInputDate("LEFT", draft)
        cancelLibrary("LEFT")
        feedback("LEFT", leapDay, "Cancelled")
        feedback("RIGHT", yearEnd, "Confirmed")
        openLibrary("LEFT")
        assertLibraryInputDate("LEFT", leapDay)
        cancelLibrary("LEFT")

        openNative("RIGHT")
        setNativeDateUsingWidgetApi("RIGHT", draft)
        recreateActivity()
        assertNativeDate("RIGHT", draft)
        clickNativeDialogButton(android.R.id.button1)
        feedback("RIGHT", draft, "Confirmed")
        feedback("LEFT", leapDay, "Cancelled")

        resetSamples()
        feedback("LEFT", initial, "Not opened")
        feedback("RIGHT", initial, "Not opened")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        compose
            .onNodeWithTag("library_LEFT")
            .assertIsNotEnabled()
            .performScrollTo()
            .performTouchInput { click() }
        compose.onNodeWithTag("date_dialog_LEFT").assertDoesNotExist()
        onView(withId(R.id.sample_right)).check(matches(not(isEnabled())))
        compose.onNodeWithTag("native_RIGHT").performScrollTo().performTouchInput { click() }
        assertNoNativeDialog("RIGHT")
        feedback("LEFT", initial, "Not opened")
        feedback("RIGHT", initial, "Not opened")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        openLibrary("LEFT")
        cancelLibrary("LEFT")
        openNative("RIGHT")
        assertNativeDate("RIGHT", initial)
        clickNativeDialogButton(android.R.id.button2)
    }

    @Test
    fun material2ShowsAnExplicitUnsupportedReasonWithoutAReplacementPicker() {
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
        compose
            .onNodeWithText("The Material 2 library does not provide Date picker dialog.")
            .assertExists()
        compose.onNodeWithTag("source_LEFT").assertTextEquals("Not provided")
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("status_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("date_dialog_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("date_picker_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("date_confirm_LEFT").assertDoesNotExist()
    }

    @Test
    fun fiveSettingsLanguagesRenderLocalActionsAndUtcDatesForActualDialogs() {
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
            chooseDateComponent()
            resetSamples()
            assertMaterial3Identity("LEFT")
            assertNativeIdentity("RIGHT", DesignFamily.MATERIAL)

            openLibrary("LEFT")
            compose
                .onNodeWithTag("date_confirm_LEFT")
                .assertTextEquals(compose.activity.getString(R.string.dialog_confirm))
            compose
                .onNodeWithTag("date_cancel_LEFT")
                .assertTextEquals(compose.activity.getString(R.string.dialog_cancel))
            confirmLibrary("LEFT")
            feedback("LEFT", initial, confirmed, language)
            openNative("RIGHT")
            assertNativeDate("RIGHT", initial)
            setNativeDateUsingWidgetApi("RIGHT", leapDay)
            clickNativeDialogButton(android.R.id.button1)
            feedback("RIGHT", leapDay, confirmed, language)
            feedback("LEFT", initial, confirmed, language)
        }
    }

    private fun chooseDateComponent() {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("picker_search")
            .performTextReplacement(LabComponent.DATE_PICKER_DIALOG.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_DATE_PICKER_DIALOG"))
        compose.onNodeWithTag("component_DATE_PICKER_DIALOG").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun resetSamples() {
        compose.onNodeWithTag("reset").performScrollTo().performClick()
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
            .assertTextEquals(compose.activity.getString(R.string.open_date_picker))
            .assertHasClickAction()
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag("date_dialog_$panel").assertIsDisplayed()
        compose.onNodeWithTag("date_picker_$panel").assertExists()
    }

    private fun assertNativeIdentity(panel: String, family: DesignFamily) {
        val platform = requireNotNull(family.platform)
        compose.onNodeWithTag("source_$panel").assertTextEquals("android.app.DatePickerDialog")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains("android:${platform.themeName}", substring = true)
        compose.onNodeWithTag("library_$panel").assertDoesNotExist()
        compose.runOnIdle {
            val launcher = compose.activity.findViewById<Button>(nativeId(panel))
            assertEquals(Button::class.java, launcher.javaClass)
            assertEquals(ContextThemeWrapper::class.java, launcher.context.javaClass)
            val expectedTheme = ContextThemeWrapper(compose.activity, platform.themeId).theme
            listOf(android.R.attr.buttonStyle, android.R.attr.datePickerStyle).forEach { attribute
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
        compose
            .onNodeWithTag("source_$panel")
            .assertTextEquals("androidx.compose.material3.DatePickerDialog")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains(
                "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}",
                substring = true,
            )
        compose.onNodeWithTag("native_$panel").assertDoesNotExist()
    }

    private fun assertNativeDate(panel: String, date: TestDate) {
        compose.runOnIdle {
            val dialog = requireNativeDialog(panel)
            assertTrue(dialog.isShowing)
            assertEquals(DatePickerDialog::class.java, dialog.javaClass)
            assertEquals(DatePicker::class.java, dialog.datePicker.javaClass)
            assertEquals(date.year, dialog.datePicker.year)
            assertEquals(date.month - 1, dialog.datePicker.month)
            assertEquals(date.day, dialog.datePicker.dayOfMonth)
        }
    }

    private fun setNativeDateUsingWidgetApi(panel: String, date: TestDate) {
        // This is framework widget API setup, not evidence of touching a calendar day.
        // Keep the original DatePicker listener so the actual positive button must commit it.
        compose.runOnIdle {
            requireNativeDialog(panel).datePicker.updateDate(date.year, date.month - 1, date.day)
        }
        assertNativeDate(panel, date)
    }

    private fun requireNativeDialog(panel: String): DatePickerDialog =
        requireNotNull(
            compose.activity.findViewById<Button>(nativeId(panel)).tag as? DatePickerDialog
        )

    // The day spinner is the only field under the picker holding a number at most 31;
    // the English month field spells its name and the year input holds a larger number.
    private fun dayInput(picker: DatePicker): EditText {
        val fields = ArrayList<EditText>()
        fun collect(view: View) {
            if (view is EditText) fields += view
            if (view is ViewGroup)
                for (index in 0 until view.childCount) collect(view.getChildAt(index))
        }
        collect(picker)
        return fields.single { it.text.toString().toIntOrNull() in 1..31 }
    }

    private fun assertNoNativeDialog(panel: String) {
        compose.runOnIdle {
            val dialog =
                compose.activity.findViewById<Button>(nativeId(panel)).tag as? DatePickerDialog
            assertFalse(dialog?.isShowing == true)
        }
    }

    private fun clickNativeDialogButton(id: Int) {
        onView(withId(id)).inRoot(isDialog()).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun libraryInput(panel: String): SemanticsNodeInteraction =
        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("date_picker_$panel")))

    private fun assertLibraryInputDate(panel: String, date: TestDate) {
        val inputMatcher = hasSetTextAction() and hasAnyAncestor(hasTestTag("date_picker_$panel"))
        if (compose.onAllNodes(inputMatcher).fetchSemanticsNodes().isEmpty()) {
            // Input-mode checks use the English baseline and the original library's own label.
            compose
                .onNode(
                    hasContentDescription("Switch to text input mode") and
                        hasAnyAncestor(hasTestTag("date_picker_$panel"))
                )
                .performScrollTo()
                .performClick()
        }
        val digits =
            libraryInput(panel)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text
                .filter(Char::isDigit)
        assertEquals(date.englishInputDigits, digits)
    }

    private fun confirmLibrary(panel: String) {
        closeSoftKeyboard()
        compose
            .onNodeWithTag("date_confirm_$panel")
            .assertIsEnabled()
            .assertIsDisplayed()
            .performTouchInput { click() }
        compose.onNodeWithTag("date_dialog_$panel").assertDoesNotExist()
    }

    private fun cancelLibrary(panel: String) {
        closeSoftKeyboard()
        compose
            .onNodeWithTag("date_cancel_$panel")
            .assertIsEnabled()
            .assertIsDisplayed()
            .performTouchInput { click() }
        compose.onNodeWithTag("date_dialog_$panel").assertDoesNotExist()
    }

    private fun feedback(
        panel: String,
        date: TestDate,
        action: String,
        language: AppLanguage = AppLanguage.ENGLISH,
    ) {
        compose
            .onNodeWithTag("status_$panel")
            .assertTextContains(date.localizedMediumDate(language), substring = true)
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

    private data class TestDate(val year: Int, val month: Int, val day: Int) {
        val englishInputDigits: String
            get() = String.format(Locale.US, "%02d%02d%04d", month, day, year)

        fun localizedMediumDate(language: AppLanguage): String {
            val utc = TimeZone.getTimeZone("UTC")
            val calendar =
                Calendar.getInstance(utc).apply {
                    clear()
                    set(year, month - 1, day)
                }
            return DateFormat.getDateInstance(
                    DateFormat.MEDIUM,
                    Locale.forLanguageTag(language.tag),
                )
                .apply { timeZone = utc }
                .format(Date(calendar.timeInMillis))
        }
    }
}
