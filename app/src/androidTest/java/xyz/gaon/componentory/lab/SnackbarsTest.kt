package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class SnackbarsTest {
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
    fun bothLibrariesShowRealSnackbarSurfaces() {
        configure(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        compose.onNodeWithTag("source_LEFT").assertTextEquals("androidx.compose.material.Snackbar")
        compose
            .onNodeWithTag("source_RIGHT")
            .assertTextEquals("androidx.compose.material3.Snackbar")
        compose.onNodeWithTag("library_LEFT_host").assertIsDisplayed()
        compose.onNodeWithTag("library_RIGHT_host").assertIsDisplayed()
        status("LEFT", "Shown 0 times")
        status("RIGHT", "Shown 0 times")
    }

    @Test
    fun tapsQueueRealSnackbarsAndTheCountCopies() {
        configure(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        show("LEFT")
        status("LEFT", "Shown 1 times")
        // The real Snackbar surface appears inside this panel's host.
        val message = compose.activity.getString(R.string.snackbar_message)
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(message, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(message, substring = true).assertIsDisplayed()

        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Shown 1 times")

        recreateActivity()
        status("LEFT", "Shown 1 times")
        status("RIGHT", "Shown 1 times")
    }

    @Test
    fun disabledButtonsSkipTheHostAndPlatformCellsExplainThemselves() {
        configure(DesignFamily.MATERIAL2, DesignFamily.CLASSIC)
        setEnabled(false)
        compose.onNodeWithTag("library_LEFT_show").assertIsNotEnabled()
        show("LEFT")
        status("LEFT", "Shown 0 times")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Android platform does not provide a dedicated Snackbar component.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    private fun configure(left: DesignFamily, right: DesignFamily) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(LabComponent.SNACKBAR.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_SNACKBAR"))
        compose.onNodeWithTag("component_SNACKBAR").performClick()
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
    }

    private fun show(panel: String) {
        compose.onNodeWithTag("library_${panel}_show").performScrollTo().performTouchInput {
            click()
        }
        compose.waitForIdle()
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
}
