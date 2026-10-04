package xyz.gaon.componentory.lab

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
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
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@RunWith(AndroidJUnit4::class)
class InputCopyNavigationTest {
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
    fun detailStartsFreshPanelsWithTextAndEnabledThenNeverReplaysItsEntryAfterEditingOrReset() {
        configureComparison(LabComponent.TEXT_FIELD, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        replaceText("LEFT", "old comparison A")
        replaceText("RIGHT", "old comparison B")
        openDetail(LabComponent.TEXT_FIELD, DesignFamily.MATERIAL2)
        replaceText("LEFT", "from detail")
        setEnabled(false)
        enterComparison()

        assertLibraryIdentity("LEFT", "TextField", DesignFamily.MATERIAL2)
        assertLibraryIdentity("RIGHT", "TextField", DesignFamily.MATERIAL3)
        assertText("LEFT", "from detail")
        assertText("RIGHT", "")
        compose.onNodeWithTag("enabled").assertIsOff()
        sample("LEFT").assertIsNotEnabled()
        sample("RIGHT").assertIsNotEnabled()

        setEnabled(true)
        replaceText("LEFT", "edited A")
        replaceText("RIGHT", "edited B")
        recreateActivity()
        assertText("LEFT", "edited A")
        assertText("RIGHT", "edited B")
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        assertText("LEFT", "")
        assertText("RIGHT", "edited B")
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        assertText("LEFT", "")
        assertText("RIGHT", "edited B")
        resetSamples()
        assertText("LEFT", "")
        assertText("RIGHT", "")
        recreateActivity()
        assertText("LEFT", "")
        assertText("RIGHT", "")

        // The preserved Detail has its own state; Compare edits and Reset must not alias it.
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
        assertText("LEFT", "from detail")
        compose.onNodeWithTag("enabled").assertIsOff()
    }

    @Test
    fun directionsCopyRealCheckboxAndBothRangeThumbsWithoutJoiningEqualProviderState() {
        configureComparison(LabComponent.CHECKBOX, DesignFamily.MATERIAL2, DesignFamily.MATERIAL2)
        touchSample("LEFT")
        sample("LEFT").assertIsOn()
        sample("RIGHT").assertIsOff()
        copyInputs("LEFT_TO_RIGHT")
        sample("LEFT").assertIsOn()
        sample("RIGHT").assertIsOn()
        touchSample("RIGHT")
        sample("LEFT").assertIsOn()
        sample("RIGHT").assertIsOff()
        copyInputs("RIGHT_TO_LEFT")
        sample("LEFT").assertIsOff()
        sample("RIGHT").assertIsOff()
        recreateActivity()
        sample("LEFT").assertIsOff()
        sample("RIGHT").assertIsOff()

        configureComparison(
            LabComponent.RANGE_SLIDER,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL3,
        )
        swipeRange("LEFT", .2f, .35f)
        swipeRange("LEFT", .8f, .65f)
        val source = range("LEFT")
        assertTrue("Start thumb must move through the actual slider", source.first > 20)
        assertTrue("End thumb must move through the actual slider", source.second < 80)
        assertEquals(20 to 80, range("RIGHT"))
        copyInputs("LEFT_TO_RIGHT")
        assertEquals(source, range("RIGHT"))
        assertEquals(source, range("LEFT"))
        swipeRange("RIGHT", source.first / 100f, .1f)
        val edited = range("RIGHT")
        assertTrue(
            "The destination must remain independently editable",
            edited.first < source.first,
        )
        assertEquals(source, range("LEFT"))
        copyInputs("RIGHT_TO_LEFT")
        assertEquals(edited, range("LEFT"))
        assertEquals(edited, range("RIGHT"))
        recreateActivity()
        assertEquals(edited, range("LEFT"))
        assertEquals(edited, range("RIGHT"))
        resetSamples()
        assertEquals(20 to 80, range("LEFT"))
        assertEquals(20 to 80, range("RIGHT"))
    }

    @Test
    fun badgeAndContainerConfigurationCopyButOldDestinationClicksStartFresh() {
        configureComparison(LabComponent.BADGE, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        touchTag("increase_ten_LEFT")
        touchTag("increase_LEFT")
        badge("LEFT", 18)
        badge("RIGHT", 7)
        copyInputs("LEFT_TO_RIGHT")
        badge("LEFT", 18)
        badge("RIGHT", 18)
        touchTag("decrease_RIGHT")
        badge("LEFT", 18)
        badge("RIGHT", 17)

        configureComparison(LabComponent.CARD, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        repeat(3) { touchSample("LEFT") }
        repeat(2) { touchSample("RIGHT") }
        touchTag("container_clickable_LEFT")
        sample("LEFT").assertHasNoClickAction()
        copyInputs("LEFT_TO_RIGHT")
        compose.onNodeWithTag("container_clickable_LEFT").assertIsOff()
        compose.onNodeWithTag("container_clickable_RIGHT").assertIsOff()
        sample("RIGHT").assertHasNoClickAction()
        clicks("LEFT", 3)
        clicks("RIGHT", 0)
        touchSample("RIGHT")
        clicks("RIGHT", 0)
        touchTag("container_clickable_RIGHT")
        touchSample("RIGHT")
        clicks("RIGHT", 1)
        clicks("LEFT", 3)
        recreateActivity()
        compose.onNodeWithTag("container_clickable_LEFT").assertIsOff()
        compose.onNodeWithTag("container_clickable_RIGHT").assertIsOn()
        clicks("LEFT", 3)
        clicks("RIGHT", 1)
        assertLibraryIdentity("LEFT", "Card", DesignFamily.MATERIAL2)
        assertLibraryIdentity("RIGHT", "Card", DesignFamily.MATERIAL3)
    }

    @Test
    fun confirmedDateAndTimeSeedRealConstructorsWithoutCopyingOpenDraftsOrActionHistory() {
        configureComparison(
            LabComponent.DATE_PICKER_DIALOG,
            DesignFamily.MATERIAL3,
            DesignFamily.CLASSIC,
        )
        openDetail(LabComponent.DATE_PICKER_DIALOG, DesignFamily.MATERIAL3)
        openLibraryDialog("date", "LEFT")
        dateInput("LEFT").performTextReplacement("01222024")
        closeSoftKeyboard()
        touchTag("date_confirm_LEFT", scroll = false)
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextContains("Jan 22, 2024")
            .assertTextContains("Confirmed")
        openLibraryDialog("date", "LEFT")
        dateInput("LEFT").performTextReplacement("02292024")
        closeSoftKeyboard()

        // The modal owns pointer focus. This dispatch tests host navigation and cleanup,
        // separately from the real input editing and dialog-button pointer actions above.
        compose.onNodeWithTag("detail_compare").performClick()
        assertNeutralDate("LEFT", "Jan 22, 2024")
        assertNeutralDate("RIGHT", "Jan 15, 2024")
        assertLibraryIdentity("LEFT", "DatePickerDialog", DesignFamily.MATERIAL3)
        compose.onNodeWithTag("source_RIGHT").assertTextEquals("android.app.DatePickerDialog")
        compose.onNodeWithTag("date_dialog_LEFT").assertDoesNotExist()
        openNative("RIGHT")
        // Public widget API fixture setup does not prove calendar touch. Keep the original
        // picker listener and confirm with the actual framework dialog button.
        compose.runOnIdle {
            requireNotNull(nativeLauncher("RIGHT").tag as? DatePickerDialog)
                .datePicker
                .updateDate(2024, 2, 12)
        }
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(nativeClick())
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextContains("Mar 12, 2024")
            .assertTextContains("Confirmed")
        openNative("RIGHT")
        val oldTargetDialog =
            compose.runOnIdle {
                requireNotNull(nativeLauncher("RIGHT").tag as? DatePickerDialog).also {
                    it.datePicker.updateDate(2024, 11, 31)
                    assertTrue(it.isShowing)
                }
            }
        dispatchHostCopyWhileModal("LEFT_TO_RIGHT")
        compose.runOnIdle {
            assertFalse(
                "Replacing the target must close its old original window",
                oldTargetDialog.isShowing,
            )
        }
        assertNeutralDate("LEFT", "Jan 22, 2024")
        assertNeutralDate("RIGHT", "Jan 22, 2024")
        openNative("RIGHT")
        compose.runOnIdle {
            val dialog = requireNotNull(nativeLauncher("RIGHT").tag as? DatePickerDialog)
            assertTrue("The copied target must create a fresh dialog", dialog !== oldTargetDialog)
            assertEquals(DatePickerDialog::class.java, dialog.javaClass)
            assertEquals(DatePicker::class.java, dialog.datePicker.javaClass)
            assertEquals(2024, dialog.datePicker.year)
            assertEquals(0, dialog.datePicker.month)
            assertEquals(22, dialog.datePicker.dayOfMonth)
        }
        nativeCancel()

        configureComparison(
            LabComponent.TIME_PICKER_DIALOG,
            DesignFamily.MATERIAL3,
            DesignFamily.CLASSIC,
        )
        openDetail(LabComponent.TIME_PICKER_DIALOG, DesignFamily.MATERIAL3)
        touchTag("time_24_hour_LEFT")
        compose.onNodeWithTag("time_24_hour_LEFT").assertIsOff()
        openLibraryDialog("time", "LEFT")
        enterLibraryTime("LEFT", 9, 5)
        selectPm("LEFT")
        touchTag("time_confirm_LEFT", scroll = false)
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextContains("9:05")
            .assertTextContains("PM")
            .assertTextContains("Confirmed")
        openLibraryDialog("time", "LEFT")
        enterLibraryTime("LEFT", 11, 45)
        selectPm("LEFT")
        compose.onNodeWithTag("detail_compare").performClick()
        compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextContains("9:05")
            .assertTextContains("PM")
            .assertTextContains("Not opened")
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextContains("10:30")
            .assertTextContains("Not opened")
        compose.onNodeWithTag("time_24_hour_LEFT").assertIsOff()
        compose.onNodeWithTag("time_24_hour_RIGHT").assertIsOn()
        assertLibraryIdentity("LEFT", "TimePickerDialog", DesignFamily.MATERIAL3)
        compose.onNodeWithTag("source_RIGHT").assertTextEquals("android.app.TimePickerDialog")
        copyInputs("LEFT_TO_RIGHT")
        compose.onNodeWithTag("time_24_hour_RIGHT").assertIsOff()
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextContains("9:05")
            .assertTextContains("PM")
            .assertTextContains("Not opened")
        recreateActivity()
        compose.onNodeWithTag("time_dialog_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("time_dialog_RIGHT").assertDoesNotExist()
        openNative("RIGHT")
        compose.runOnIdle {
            val dialog = requireNotNull(nativeLauncher("RIGHT").tag as? TimePickerDialog)
            assertEquals(TimePickerDialog::class.java, dialog.javaClass)
            val picker = requireNotNull(findTimePicker(requireNotNull(dialog.window).decorView))
            assertEquals(TimePicker::class.java, picker.javaClass)
            assertEquals(21, picker.hour)
            assertEquals(5, picker.minute)
            assertEquals(false, picker.is24HourView())
        }
        nativeCancel()
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material3.TimePickerDialog")
        compose.onNodeWithTag("source_RIGHT").assertTextEquals("android.app.TimePickerDialog")
    }

    @Test
    fun menuSecretsAndActionOnlySamplesHaveNoCopyInputsAndUnsupportedPairsRemainHonest() {
        configureComparison(LabComponent.POPUP_MENU, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        touchSample("LEFT")
        touchTag("menu_item_LEFT_B")
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals("Choice: Option B · Last action: Selected")
        assertBlockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals("Choice: Option B · Last action: Selected")
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextEquals("Choice: No selection · Last action: Not opened")

        configureComparison(
            LabComponent.SECURE_TEXT_FIELD,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL3,
        )
        sample("LEFT").performScrollTo().performTextInput("sample123")
        closeSoftKeyboard()
        sample("RIGHT").performScrollTo().performTextInput("abc")
        closeSoftKeyboard()
        assertBlockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Characters: 9")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Characters: 3")

        configureComparison(LabComponent.BUTTON, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        repeat(3) { touchSample("LEFT") }
        repeat(2) { touchSample("RIGHT") }
        assertBlockedCopy("RIGHT_TO_LEFT", "This sample has no inputs to copy.")
        clicks("LEFT", 3)
        clicks("RIGHT", 2)

        configureComparison(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL3, DesignFamily.CLASSIC)
        swipeRange("LEFT", .2f, .35f)
        val source = range("LEFT")
        assertBlockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
        assertEquals(source, range("LEFT"))
        compose.onNodeWithTag("unsupported_RIGHT").assertExists()
        compose.onNodeWithTag("native_RIGHT").assertDoesNotExist()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        openDetail(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)
        compose.onNodeWithTag("unsupported_LEFT").assertExists()
        enterComparison()
        compose.onNodeWithTag("unsupported_LEFT").assertExists()
        compose.onNodeWithTag("source_LEFT").assertTextEquals("Not provided")
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        assertEquals(20 to 80, range("RIGHT"))
        assertBlockedCopy("LEFT_TO_RIGHT", "The source provider does not support this sample.")
        recreateActivity()
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        assertEquals(20 to 80, range("LEFT"))
        assertEquals(20 to 80, range("RIGHT"))
    }

    @Test
    fun compatibleIconsKeepExactStyleAndMirroringAndCrossDomainIconOnlyCopyDoesNothing() {
        val arrow = "androidx.compose.material.icons.automirrored.outlined.ArrowBackKt"
        val favorite = "androidx.compose.material.icons.rounded.FavoriteKt"
        configureComparison(LabComponent.ICON, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        assertLibraryIdentity("LEFT", "Icon", DesignFamily.MATERIAL2)
        assertLibraryIdentity("RIGHT", "Icon", DesignFamily.MATERIAL3)
        chooseIcon("LEFT", arrow, "arrow_back", "OUTLINED", mirrored = true)
        chooseIcon("RIGHT", favorite, "favorite", "ROUNDED")
        copyInputs("LEFT_TO_RIGHT")
        assertMaterialIcon("LEFT", arrow, "ArrowBack")
        assertMaterialIcon("RIGHT", arrow, "ArrowBack")
        chooseIcon("RIGHT", favorite, "favorite", "ROUNDED")
        assertMaterialIcon("LEFT", arrow, "ArrowBack")
        copyInputs("RIGHT_TO_LEFT")
        assertMaterialIcon("LEFT", favorite, "Favorite")
        assertMaterialIcon("RIGHT", favorite, "Favorite")
        recreateActivity()
        assertMaterialIcon("LEFT", favorite, "Favorite")

        configureComparison(LabComponent.ICON, DesignFamily.CLASSIC, DesignFamily.HOLO)
        chooseIcon("LEFT", "android:ic_menu_camera", "ic_menu_camera")
        chooseIcon("RIGHT", "android:ic_menu_zoom", "ic_menu_zoom")
        copyInputs("LEFT_TO_RIGHT")
        assertNativeIcon("LEFT", "ic_menu_camera")
        assertNativeIcon("RIGHT", "ic_menu_camera")

        configureComparison(LabComponent.ICON, DesignFamily.MATERIAL3, DesignFamily.CLASSIC)
        chooseIcon("LEFT", favorite, "favorite", "ROUNDED")
        chooseIcon("RIGHT", "android:ic_menu_camera", "ic_menu_camera")
        // ICON is the only currently reachable cross-domain component. It has no second
        // transferable field, so mismatch is an honest no-op rather than a partial-copy proof.
        assertBlockedCopy("LEFT_TO_RIGHT", "This icon is unavailable in the target provider.")
        assertMaterialIcon("LEFT", favorite, "Favorite")
        assertNativeIcon("RIGHT", "ic_menu_camera")
        assertBlockedCopy("RIGHT_TO_LEFT", "This icon is unavailable in the target provider.")
        assertMaterialIcon("LEFT", favorite, "Favorite")
        assertNativeIcon("RIGHT", "ic_menu_camera")
    }

    @Test
    fun fiveSettingsLanguagesNameCopyActionsAndCopyEmptyTextEvenWhenOriginalWidgetsAreDisabled() {
        val names =
            listOf(
                CopyNames(
                    AppLanguage.ENGLISH,
                    "Copy inputs",
                    "Copy Left UI → Right UI",
                    "Copy Right UI → Left UI",
                    "Inputs copied to Right UI. Started a new sample.",
                    "Inputs copied to Left UI. Started a new sample.",
                ),
                CopyNames(
                    AppLanguage.KOREAN,
                    "입력 복사",
                    "왼쪽 UI → 오른쪽 UI 입력 복사",
                    "오른쪽 UI → 왼쪽 UI 입력 복사",
                    "오른쪽 UI에 입력을 복사하고 새 체험을 시작했습니다.",
                    "왼쪽 UI에 입력을 복사하고 새 체험을 시작했습니다.",
                ),
                CopyNames(
                    AppLanguage.JAPANESE,
                    "入力をコピー",
                    "左の UI → 右の UI に入力をコピー",
                    "右の UI → 左の UI に入力をコピー",
                    "右の UI に入力をコピーし、新しい体験を開始しました。",
                    "左の UI に入力をコピーし、新しい体験を開始しました。",
                ),
                CopyNames(
                    AppLanguage.SIMPLIFIED_CHINESE,
                    "复制输入",
                    "复制 左侧 UI → 右侧 UI 的输入",
                    "复制 右侧 UI → 左侧 UI 的输入",
                    "已将输入复制到 右侧 UI，并开始新的体验。",
                    "已将输入复制到 左侧 UI，并开始新的体验。",
                ),
                CopyNames(
                    AppLanguage.TRADITIONAL_CHINESE,
                    "複製輸入",
                    "複製 左側 UI → 右側 UI 的輸入",
                    "複製 右側 UI → 左側 UI 的輸入",
                    "已將輸入複製到 右側 UI，並開始新的體驗。",
                    "已將輸入複製到 左側 UI，並開始新的體驗。",
                ),
            )
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configureComparison(
                LabComponent.TEXT_FIELD,
                DesignFamily.MATERIAL2,
                DesignFamily.MATERIAL3,
            )
            replaceText("LEFT", "localized source")
            replaceText("RIGHT", "previous destination")
            setEnabled(false)
            sample("LEFT").assertIsNotEnabled()
            sample("RIGHT").assertIsNotEnabled()
            compose
                .onNodeWithTag("copy_inputs")
                .assertTextEquals(text.title)
                .assertIsEnabled()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            copyInputs("LEFT_TO_RIGHT", text.leftToRight, text.leftResult)
            assertText("LEFT", "localized source")
            assertText("RIGHT", "localized source")
            sample("LEFT").assertIsNotEnabled()
            sample("RIGHT").assertIsNotEnabled()
            compose.onNodeWithTag("enabled").assertIsOff()
            copyInputs("RIGHT_TO_LEFT", text.rightToLeft, text.rightResult)
            compose.onNodeWithTag("enabled").assertIsOff()

            setEnabled(true)
            replaceText("LEFT", "")
            assertText("RIGHT", "localized source")
            copyInputs("LEFT_TO_RIGHT", text.leftToRight, text.leftResult)
            assertText("LEFT", "")
            assertText("RIGHT", "")
            recreateActivity()
            assertText("LEFT", "")
            assertText("RIGHT", "")
        }

        changeLanguage(AppLanguage.ENGLISH)
        // This bounds the real app host and changes its Compose font scale. It verifies
        // host layout and reachable actions, not a device display setting or modal size.
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
        configureComparison(LabComponent.TEXT_FIELD, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        replaceText("LEFT", "large text host")
        setEnabled(false)
        compose.onNodeWithTag("copy_inputs").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("reset").assertIsDisplayed()
        val hostBounds = compose.onNodeWithTag("compare_screen").fetchSemanticsNode().boundsInRoot
        val copyBounds = compose.onNodeWithTag("copy_inputs").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Copy control must stay within the bounded host width",
            copyBounds.left >= hostBounds.left && copyBounds.right <= hostBounds.right,
        )
        copyInputs("LEFT_TO_RIGHT", names.first().leftToRight, names.first().leftResult)
        assertText("RIGHT", "large text host")
        sample("RIGHT").assertIsNotEnabled()
        compose.onNodeWithTag("copy_setup_result").performScrollTo().assertIsDisplayed()
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
        compose.onNodeWithTag("component_search").performTextReplacement(component.label)
        compose.onNodeWithTag("component_search").performImeAction()
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_${component.name}"))
        compose.onNodeWithTag("list_${component.name}").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
        chooseFamily("LEFT", family)
        setEnabled(true)
        resetSamples()
    }

    private fun enterComparison() {
        compose.onNodeWithTag("detail_compare").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithTag("compare_screen").assertExists()
    }

    private fun copyInputs(direction: String, name: String? = null, result: String? = null) {
        touchTag("copy_inputs")
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        val item =
            compose
                .onNodeWithTag("copy_setup_$direction")
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
        if (name != null) item.assertTextContains(name)
        item.performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
        if (result != null) compose.onNodeWithTag("copy_setup_result").assertTextEquals(result)
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
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
    }

    private fun dispatchHostCopyWhileModal(direction: String) {
        // The original modal owns pointer focus. This intentionally dispatches host actions
        // to verify target disposal, not a claim that these controls can be touched through it.
        compose.onNodeWithTag("copy_inputs").performClick()
        compose.onNodeWithTag("copy_setup_$direction").assertIsEnabled().performClick()
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun replaceText(panel: String, value: String) {
        sample(panel)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTextReplacement(value)
        closeSoftKeyboard()
        assertText(panel, value)
    }

    private fun assertText(panel: String, expected: String) {
        assertEquals(
            expected,
            sample(panel).fetchSemanticsNode().config[SemanticsProperties.EditableText].text,
        )
    }

    private fun sample(panel: String): SemanticsNodeInteraction =
        compose.onNodeWithTag("library_$panel")

    private fun touchSample(panel: String) = touchTag("library_$panel")

    private fun touchTag(tag: String, scroll: Boolean = true) {
        val target = compose.onNodeWithTag(tag)
        if (scroll) target.performScrollTo()
        target.assertIsDisplayed().performTouchInput { click() }
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        val current =
            control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                androidx.compose.ui.state.ToggleableState.On
        if (current != enabled) control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun assertLibraryIdentity(panel: String, api: String, family: DesignFamily) {
        val packageName =
            if (family == DesignFamily.MATERIAL2) "androidx.compose.material"
            else "androidx.compose.material3"
        val version =
            if (family == DesignFamily.MATERIAL2) BuildConfig.MATERIAL2_VERSION
            else BuildConfig.MATERIAL3_VERSION
        val artifact = if (family == DesignFamily.MATERIAL2) "material" else "material3"
        compose.onNodeWithTag("source_$panel").assertTextEquals("$packageName.$api")
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains("$packageName:$artifact:$version")
    }

    private fun swipeRange(panel: String, start: Float, end: Float) {
        sample(panel).performScrollTo().assertIsDisplayed().performTouchInput {
            swipe(Offset(width * start, center.y), Offset(width * end, center.y))
        }
    }

    private fun range(panel: String): Pair<Int, Int> {
        val text =
            compose
                .onNodeWithTag("status_$panel")
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .single()
                .text
        val values =
            requireNotNull(Regex("Range: (\\d+)–(\\d+) / 100").matchEntire(text)).groupValues
        val expected = values[1].toInt() to values[2].toInt()
        val rendered =
            compose
                .onAllNodes(
                    hasAnyAncestor(hasTestTag("library_$panel")) and
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo),
                    useUnmergedTree = true,
                )
                .fetchSemanticsNodes()
                .map { it.config[SemanticsProperties.ProgressBarRangeInfo].current }
                .sorted()
        assertEquals(
            "Both original thumbs must render the reported input range",
            listOf(expected.first.toFloat(), expected.second.toFloat()),
            rendered,
        )
        return expected
    }

    private fun badge(panel: String, expected: Int) {
        compose
            .onNodeWithTag("badge_count_$panel", useUnmergedTree = true)
            .assertTextEquals(expected.toString())
        compose.onNodeWithTag("status_$panel").assertTextEquals("Badge count: $expected")
    }

    private fun clicks(panel: String, expected: Int) =
        compose.onNodeWithTag("status_$panel").assertTextEquals("Clicks: $expected")

    private fun openLibraryDialog(type: String, panel: String) {
        touchSample(panel)
        compose.onNodeWithTag("${type}_dialog_$panel").assertIsDisplayed()
    }

    private fun dateInput(panel: String): SemanticsNodeInteraction {
        val matcher = hasSetTextAction() and hasAnyAncestor(hasTestTag("date_picker_$panel"))
        if (compose.onAllNodes(matcher).fetchSemanticsNodes().isEmpty()) {
            compose
                .onNode(
                    hasContentDescription("Switch to text input mode") and
                        hasAnyAncestor(hasTestTag("date_picker_$panel"))
                )
                .performScrollTo()
                .assertIsDisplayed()
                .performTouchInput { click() }
        }
        return compose.onNode(matcher).assertIsDisplayed()
    }

    private fun assertNeutralDate(panel: String, date: String) {
        compose
            .onNodeWithTag("status_$panel")
            .assertTextContains(date)
            .assertTextContains("Not opened")
    }

    private fun enterLibraryTime(panel: String, hour: Int, minute: Int) {
        if (compose.onAllNodes(hasTestTag("time_input_$panel")).fetchSemanticsNodes().isEmpty()) {
            touchTag("time_mode_$panel", scroll = false)
        }
        timeInput(panel, hour = true).performTextReplacement(hour.toString())
        timeInput(panel, hour = false).performTextReplacement(minute.toString())
        closeSoftKeyboard()
    }

    private fun timeInput(panel: String, hour: Boolean): SemanticsNodeInteraction {
        val ancestor = hasAnyAncestor(hasTestTag("time_input_$panel"))
        val selector =
            hasContentDescription(if (hour) "Select hour" else "Select minutes") and
                ancestor and
                hasClickAction()
        if (compose.onAllNodes(selector).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNode(selector).assertIsDisplayed().performTouchInput { click() }
        }
        return compose
            .onNode(
                hasSetTextAction() and
                    hasContentDescription(if (hour) "for hour" else "for minutes") and
                    ancestor
            )
            .assertIsDisplayed()
    }

    private fun selectPm(panel: String) {
        compose
            .onNode(
                hasText("PM") and
                    hasClickAction() and
                    hasAnyAncestor(hasTestTag("time_input_$panel"))
            )
            .assertIsDisplayed()
            .performTouchInput { click() }
    }

    private fun openNative(panel: String) {
        compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
        onView(withId(nativeId(panel))).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun nativeLauncher(panel: String): Button =
        compose.activity.findViewById(nativeId(panel))

    private fun nativeCancel() {
        onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun findTimePicker(view: View): TimePicker? {
        if (view is TimePicker) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findTimePicker(view.getChildAt(index))?.let {
                    return it
                }
            }
        }
        return null
    }

    private fun chooseIcon(
        panel: String,
        id: String,
        query: String,
        style: String? = null,
        mirrored: Boolean = false,
    ) {
        touchTag("icon_picker_$panel")
        compose.onNodeWithTag("icon_search").performTextReplacement(query)
        closeSoftKeyboard()
        if (style != null) {
            touchTag("icon_style_$style")
            val control = compose.onNodeWithTag("icon_mirrored")
            val selected = control.fetchSemanticsNode().config[SemanticsProperties.Selected]
            if (selected != mirrored) control.assertIsDisplayed().performTouchInput { click() }
        }
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
        compose
            .onNodeWithTag("icon_entry_$id")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("icon_dialog").assertDoesNotExist()
    }

    private fun assertMaterialIcon(panel: String, id: String, name: String) {
        compose
            .onNodeWithTag("icon_source_$panel")
            .assertTextEquals("$id · icons ${BuildConfig.MATERIAL_ICONS_VERSION}")
        sample(panel).assertContentDescriptionEquals(name)
    }

    private fun assertNativeIcon(panel: String, name: String) {
        compose.onNodeWithTag("icon_source_$panel").assertTextEquals("android.R.drawable.$name")
        onView(withId(nativeId(panel))).check(matches(withContentDescription(name)))
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
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

    private data class CopyNames(
        val language: AppLanguage,
        val title: String,
        val leftToRight: String,
        val rightToLeft: String,
        val leftResult: String,
        val rightResult: String,
    )
}
