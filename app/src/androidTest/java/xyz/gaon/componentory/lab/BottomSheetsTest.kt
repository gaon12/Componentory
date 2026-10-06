package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class BottomSheetsTest {
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
    fun scaffoldSheetStateCopiesAcrossLibraries() {
        configure(
            LabComponent.BOTTOM_SHEET_SCAFFOLD,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL3,
        )
        expandDetails("LEFT")
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material.BottomSheetScaffold")
        expandDetails("RIGHT")
        compose
            .onNodeWithTag("source_RIGHT")
            .assertTextEquals("androidx.compose.material3.BottomSheetScaffold")
        status("LEFT", "Sheet closed")
        status("RIGHT", "Sheet closed")

        compose.onNodeWithTag("library_LEFT_open").performScrollTo().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("library_LEFT_item_2").assertIsDisplayed()
        status("LEFT", "Sheet open")

        copyInputs("LEFT_TO_RIGHT")
        compose.waitForIdle()
        status("RIGHT", "Sheet open")

        recreateActivity()
        status("LEFT", "Sheet open")
        status("RIGHT", "Sheet open")
        listOf("LEFT", "RIGHT").forEach { panel ->
            compose.onNodeWithTag("library_${panel}_item_2").performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun modalSheetShowsAndDismissesRealOverlay() {
        configure(LabComponent.MODAL_BOTTOM_SHEET, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        expandDetails("LEFT")
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material.ModalBottomSheetLayout")
        expandDetails("RIGHT")
        compose
            .onNodeWithTag("source_RIGHT")
            .assertTextEquals("androidx.compose.material3.ModalBottomSheet")

        compose.onNodeWithTag("library_RIGHT_open").performScrollTo().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("library_RIGHT_item_1").assertIsDisplayed()
        status("RIGHT", "Sheet open")
        compose.onNodeWithTag("library_RIGHT_item_1").performClick()
        compose.waitForIdle()
        status("RIGHT", "Sheet closed")
    }

    @Test
    fun backdropScaffoldIsHonestMaterial2Only() {
        configure(LabComponent.BACKDROP_SCAFFOLD, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        expandDetails("LEFT")
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material.BackdropScaffold")
        expandDetails("RIGHT")
        compose.onNodeWithTag("source_RIGHT").assertTextEquals("Not provided")
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("library_LEFT_open").performScrollTo().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("library_LEFT_back").assertIsDisplayed()
        status("LEFT", "Sheet open")
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun disabledButtonKeepsSheetClosed() {
        configure(
            LabComponent.BOTTOM_SHEET_SCAFFOLD,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL2,
        )
        setEnabled(false)
        compose
            .onNodeWithTag("library_LEFT_open")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.waitForIdle()
        status("LEFT", "Sheet closed")
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

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsDisplayed()
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

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
